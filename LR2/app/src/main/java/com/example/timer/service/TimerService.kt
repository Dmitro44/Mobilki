package com.example.timer.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import com.example.timer.data.local.database.AppDatabase
import com.example.timer.data.repository.TimerRepositoryImpl
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhaseModel
import com.example.timer.domain.model.TimerSequenceModel
import com.example.timer.domain.repository.TimerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground service that manages timer countdown and playback
 * 
 * Responsibilities:
 * - Load and execute timer sequences
 * - Count down phases with second precision
 * - Handle phase transitions and repetitions
 * - Play sound feedback on phase completion
 * - Expose state via singleton StateFlow for UI observation
 * - Show foreground notification with controls
 * 
 * Notification Strategy:
 * - Standard notification (TimerNotificationHelper): Always shown, works on all devices
 * 
 * Lifecycle:
 * - Starts as foreground service when timer begins
 * - Stops foreground when timer is idle or completed
 * - Service stops itself when no longer needed
 */
class TimerService : Service() {
    
    // Repository and notification helpers
    private lateinit var repository: TimerRepository
    private lateinit var notificationHelper: TimerNotificationHelper
    
    // Coroutine scope for async operations
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    // Current sequence and phases
    private var currentSequence: TimerSequenceModel? = null
    private var currentPhasesList: List<TimerPhaseModel> = emptyList()
    
    // Timer state tracking
    private var currentPhaseIndex = 0
    private var currentRepetitionIndex = 0
    private var currentPhaseRemainingSeconds = 0
    private var totalElapsedSeconds = 0
    
    // CountDownTimer for ticking
    private var countDownTimer: CountDownTimer? = null
    
    // Sound feedback
    private var toneGenerator: ToneGenerator? = null
    
    companion object {
        private const val TAG = "TimerService"
        
        // Singleton state flow - accessible by ViewModels
        private val _timerState = MutableStateFlow(TimerState.Idle)
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
        
        /**
         * Check if timer is currently active
         */
        fun isActive(): Boolean {
            return _timerState.value.playbackState == PlaybackState.RUNNING ||
                   _timerState.value.playbackState == PlaybackState.PAUSED
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        // Initialize repository
        val database = AppDatabase.getInstance(this)
        repository = TimerRepositoryImpl(
            database.timerSequenceDao(),
            database.timerPhaseDao()
        )
        
        // Initialize notification helper
        notificationHelper = TimerNotificationHelper(this)
        
        // Initialize tone generator for beep sounds
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ToneGenerator", e)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.getTimerAction()}")
        
        // Extract and handle timer action
        val action = intent?.getTimerAction()
        if (action != null) {
            handleAction(action)
        }
        
        return START_NOT_STICKY // Don't restart if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        // This is a started service, not bound
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        
        // Clean up resources
        stopTimer()
        toneGenerator?.release()
        toneGenerator = null
        serviceScope.cancel()
    }
    
    /**
     * Handle incoming timer actions
     */
    private fun handleAction(action: TimerAction) {
        when (action) {
            is TimerAction.Start -> startSequence(action.sequenceId)
            is TimerAction.Pause -> pauseTimer()
            is TimerAction.Resume -> resumeTimer()
            is TimerAction.Stop -> stopTimer()
            is TimerAction.SkipNext -> skipNext()
            is TimerAction.SkipPrevious -> skipPrevious()
        }
    }
    
    /**
     * Start a new timer sequence
     */
    private fun startSequence(sequenceId: Long) {
        serviceScope.launch {
            try {
                // Load sequence from repository
                val sequence = repository.getSequenceById(sequenceId)
                
                if (sequence == null || sequence.phases.isEmpty()) {
                    Log.e(TAG, "Invalid sequence or no phases found")
                    stopTimer()
                    return@launch
                }
                
                // Store sequence data
                currentSequence = sequence
                currentPhasesList = sequence.phases.sortedBy { it.order }
                
                // Reset state
                currentPhaseIndex = 0
                currentRepetitionIndex = 0
                totalElapsedSeconds = 0
                
                // Calculate total duration
                val totalDuration = currentPhasesList.sumOf { it.totalDurationSeconds }
                
                // Initialize first phase
                val firstPhase = currentPhasesList[0]
                currentPhaseRemainingSeconds = firstPhase.durationSeconds
                
                // Update state to running
                updateState(
                    playbackState = PlaybackState.RUNNING,
                    sequenceId = sequence.id,
                    sequenceName = sequence.name,
                    currentPhaseIndex = currentPhaseIndex,
                    currentRepetitionIndex = currentRepetitionIndex,
                    totalRepetitions = firstPhase.repetitions,
                    currentPhaseType = firstPhase.phaseType,
                    totalPhases = currentPhasesList.size,
                    remainingSeconds = currentPhaseRemainingSeconds,
                    phaseDurationSeconds = firstPhase.durationSeconds,
                    totalSequenceDuration = totalDuration,
                    elapsedSequenceTime = totalElapsedSeconds
                )
                
                // Start foreground service
                startForegroundService()
                
                // Start countdown
                startCountdown()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error starting sequence", e)
                stopTimer()
            }
        }
    }
    
    /**
     * Start the countdown timer
     */
    private fun startCountdown() {
        stopCountdown() // Stop any existing timer
        
        // Create countdown timer (total time + buffer, tick every second)
        countDownTimer = object : CountDownTimer(
            (currentPhaseRemainingSeconds * 1000L) + 500, // Add buffer
            1000L // Tick every second
        ) {
            override fun onTick(millisUntilFinished: Long) {
                currentPhaseRemainingSeconds = (millisUntilFinished / 1000).toInt()
                totalElapsedSeconds++
                
                // Update state with new time
                updateStateTime(
                    remainingSeconds = currentPhaseRemainingSeconds,
                    elapsedSequenceTime = totalElapsedSeconds
                )
            }
            
            override fun onFinish() {
                // Phase completed
                onPhaseComplete()
            }
        }.start()
    }
    
    /**
     * Stop the countdown timer
     */
    private fun stopCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }
    
    /**
     * Handle phase completion
     */
    private fun onPhaseComplete() {
        Log.d(TAG, "Phase complete: phase=$currentPhaseIndex, rep=$currentRepetitionIndex")
        
        // Play completion sound
        playBeep()
        
        val currentPhase = currentPhasesList.getOrNull(currentPhaseIndex) ?: return
        
        // Check if we need to repeat the current phase
        if (currentRepetitionIndex < currentPhase.repetitions - 1) {
            // Move to next repetition
            currentRepetitionIndex++
            currentPhaseRemainingSeconds = currentPhase.durationSeconds
            
            // Update state
            updateState(
                playbackState = PlaybackState.RUNNING,
                currentRepetitionIndex = currentRepetitionIndex,
                remainingSeconds = currentPhaseRemainingSeconds,
                elapsedSequenceTime = totalElapsedSeconds
            )
            
            // Restart countdown
            startCountdown()
        } else {
            // Move to next phase
            if (currentPhaseIndex < currentPhasesList.size - 1) {
                currentPhaseIndex++
                currentRepetitionIndex = 0
                
                val nextPhase = currentPhasesList[currentPhaseIndex]
                currentPhaseRemainingSeconds = nextPhase.durationSeconds
                
                // Update state with new phase
                updateState(
                    playbackState = PlaybackState.RUNNING,
                    currentPhaseIndex = currentPhaseIndex,
                    currentRepetitionIndex = currentRepetitionIndex,
                    totalRepetitions = nextPhase.repetitions,
                    currentPhaseType = nextPhase.phaseType,
                    remainingSeconds = currentPhaseRemainingSeconds,
                    phaseDurationSeconds = nextPhase.durationSeconds,
                    elapsedSequenceTime = totalElapsedSeconds
                )
                
                // Restart countdown
                startCountdown()
            } else {
                // All phases completed
                onSequenceComplete()
            }
        }
    }
    
    /**
     * Handle sequence completion
     */
    private fun onSequenceComplete() {
        Log.d(TAG, "Sequence completed")
        
        // Play completion sound
        playBeep()
        playBeep() // Double beep for completion
        
        // Update state to completed
        updateState(
            playbackState = PlaybackState.COMPLETED,
            remainingSeconds = 0
        )
        
        // Stop foreground after delay to show completion
        serviceScope.launch {
            kotlinx.coroutines.delay(3000) // Show completed state for 3 seconds
            if (_timerState.value.playbackState == PlaybackState.COMPLETED) {
                stopSelf()
            }
        }
    }
    
    /**
     * Pause the timer
     */
    private fun pauseTimer() {
        if (_timerState.value.playbackState != PlaybackState.RUNNING) return
        
        Log.d(TAG, "Timer paused")
        stopCountdown()
        
        updateState(playbackState = PlaybackState.PAUSED)
    }
    
    /**
     * Resume the timer
     */
    private fun resumeTimer() {
        if (_timerState.value.playbackState != PlaybackState.PAUSED) return
        
        Log.d(TAG, "Timer resumed")
        
        updateState(playbackState = PlaybackState.RUNNING)
        startCountdown()
    }
    
    /**
     * Stop the timer and reset state
     */
    private fun stopTimer() {
        Log.d(TAG, "Timer stopped")
        
        stopCountdown()
        currentSequence = null
        currentPhasesList = emptyList()
        
        updateState(
            playbackState = PlaybackState.IDLE,
            sequenceId = 0,
            sequenceName = "",
            currentPhaseIndex = 0,
            currentRepetitionIndex = 0,
            totalRepetitions = 1,
            currentPhaseType = PhaseType.WORK,
            totalPhases = 0,
            remainingSeconds = 0,
            phaseDurationSeconds = 0,
            totalSequenceDuration = 0,
            elapsedSequenceTime = 0
        )
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    /**
     * Skip to next phase/repetition
     */
    private fun skipNext() {
        if (_timerState.value.playbackState == PlaybackState.IDLE) return
        
        val currentPhase = currentPhasesList.getOrNull(currentPhaseIndex) ?: return
        val wasRunning = _timerState.value.playbackState == PlaybackState.RUNNING
        
        stopCountdown()
        
        // Check if we can move to next repetition
        if (currentRepetitionIndex < currentPhase.repetitions - 1) {
            currentRepetitionIndex++
            currentPhaseRemainingSeconds = currentPhase.durationSeconds
            
            updateState(
                playbackState = if (wasRunning) PlaybackState.RUNNING else PlaybackState.PAUSED,
                currentRepetitionIndex = currentRepetitionIndex,
                remainingSeconds = currentPhaseRemainingSeconds
            )
            
            if (wasRunning) startCountdown()
        } else if (currentPhaseIndex < currentPhasesList.size - 1) {
            // Move to next phase
            currentPhaseIndex++
            currentRepetitionIndex = 0
            
            val nextPhase = currentPhasesList[currentPhaseIndex]
            currentPhaseRemainingSeconds = nextPhase.durationSeconds
            
            updateState(
                playbackState = if (wasRunning) PlaybackState.RUNNING else PlaybackState.PAUSED,
                currentPhaseIndex = currentPhaseIndex,
                currentRepetitionIndex = currentRepetitionIndex,
                totalRepetitions = nextPhase.repetitions,
                currentPhaseType = nextPhase.phaseType,
                remainingSeconds = currentPhaseRemainingSeconds,
                phaseDurationSeconds = nextPhase.durationSeconds
            )
            
            if (wasRunning) startCountdown()
        } else {
            // Last phase and last repetition - complete sequence
            Log.d(TAG, "Skipping last phase/repetition - completing sequence")
            onSequenceComplete()
        }
    }
    
    /**
     * Skip to previous phase/repetition
     */
    private fun skipPrevious() {
        if (_timerState.value.playbackState == PlaybackState.IDLE) return
        
        val currentPhase = currentPhasesList.getOrNull(currentPhaseIndex) ?: return
        val wasRunning = _timerState.value.playbackState == PlaybackState.RUNNING
        
        stopCountdown()
        
        // Check if we can move to previous repetition
        if (currentRepetitionIndex > 0) {
            currentRepetitionIndex--
            currentPhaseRemainingSeconds = currentPhase.durationSeconds
            
            updateState(
                playbackState = if (wasRunning) PlaybackState.RUNNING else PlaybackState.PAUSED,
                currentRepetitionIndex = currentRepetitionIndex,
                remainingSeconds = currentPhaseRemainingSeconds
            )
            
            if (wasRunning) startCountdown()
        } else if (currentPhaseIndex > 0) {
            // Move to previous phase
            currentPhaseIndex--
            
            val prevPhase = currentPhasesList[currentPhaseIndex]
            currentRepetitionIndex = prevPhase.repetitions - 1 // Go to last repetition
            currentPhaseRemainingSeconds = prevPhase.durationSeconds
            
            updateState(
                playbackState = if (wasRunning) PlaybackState.RUNNING else PlaybackState.PAUSED,
                currentPhaseIndex = currentPhaseIndex,
                currentRepetitionIndex = currentRepetitionIndex,
                totalRepetitions = prevPhase.repetitions,
                currentPhaseType = prevPhase.phaseType,
                remainingSeconds = currentPhaseRemainingSeconds,
                phaseDurationSeconds = prevPhase.durationSeconds
            )
            
            if (wasRunning) startCountdown()
        } else {
            // Already at first phase/rep
            Log.d(TAG, "Already at first phase/repetition")
        }
    }
    
    /**
     * Update the full timer state
     */
    private fun updateState(
        playbackState: PlaybackState = _timerState.value.playbackState,
        sequenceId: Long = _timerState.value.sequenceId,
        sequenceName: String = _timerState.value.sequenceName,
        currentPhaseIndex: Int = _timerState.value.currentPhaseIndex,
        currentRepetitionIndex: Int = _timerState.value.currentRepetitionIndex,
        totalRepetitions: Int = _timerState.value.totalRepetitions,
        currentPhaseType: PhaseType = _timerState.value.currentPhaseType,
        totalPhases: Int = _timerState.value.totalPhases,
        remainingSeconds: Int = _timerState.value.remainingSeconds,
        phaseDurationSeconds: Int = _timerState.value.phaseDurationSeconds,
        totalSequenceDuration: Int = _timerState.value.totalSequenceDuration,
        elapsedSequenceTime: Int = _timerState.value.elapsedSequenceTime
    ) {
        val newState = TimerState(
            playbackState = playbackState,
            sequenceId = sequenceId,
            sequenceName = sequenceName,
            currentPhaseIndex = currentPhaseIndex,
            currentRepetitionIndex = currentRepetitionIndex,
            totalRepetitions = totalRepetitions,
            currentPhaseType = currentPhaseType,
            totalPhases = totalPhases,
            remainingSeconds = remainingSeconds,
            phaseDurationSeconds = phaseDurationSeconds,
            totalSequenceDuration = totalSequenceDuration,
            elapsedSequenceTime = elapsedSequenceTime
        )
        
        _timerState.value = newState
        
        // Update standard notification
        if (playbackState != PlaybackState.IDLE) {
            notificationHelper.updateNotification(newState)
        }
    }
    
    /**
     * Update only time-related state fields (for efficiency during ticking)
     */
    private fun updateStateTime(
        remainingSeconds: Int,
        elapsedSequenceTime: Int
    ) {
        _timerState.value = _timerState.value.copy(
            remainingSeconds = remainingSeconds,
            elapsedSequenceTime = elapsedSequenceTime
        )
        
        // Update standard notification less frequently (every 1 second is fine)
        notificationHelper.updateNotification(_timerState.value)
    }
    
    /**
     * Start service in foreground with notification
     */
    private fun startForegroundService() {
        val notification = notificationHelper.buildNotification(_timerState.value)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                TimerNotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(TimerNotificationHelper.NOTIFICATION_ID, notification)
        }
    }
    
    /**
     * Play a beep sound for phase completion
     */
    private fun playBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play beep", e)
        }
    }
}
