package com.example.timer.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import com.example.timer.R
import com.example.timer.data.local.database.AppDatabase
import com.example.timer.data.local.preferences.PreferencesManager
import com.example.timer.data.repository.TimerRepositoryImpl
import com.example.timer.domain.model.PhaseType
import com.example.timer.domain.model.TimerPhaseModel
import com.example.timer.domain.model.TimerSequenceModel
import com.example.timer.domain.repository.TimerRepository
import com.example.timer.ui.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TimerService : Service() {
    
    private lateinit var repository: TimerRepository
    private lateinit var notificationHelper: TimerNotificationHelper
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    private var currentSequence: TimerSequenceModel? = null
    private var currentPhasesList: List<TimerPhaseModel> = emptyList()
    
    private var currentPhaseIndex = 0
    private var currentRepetitionIndex = 0
    private var currentPhaseRemainingSeconds = 0
    
    private var countDownTimer: CountDownTimer? = null
    
    private var toneGenerator: ToneGenerator? = null
    private var mediaPlayer: MediaPlayer? = null
    
    companion object {
        private const val TAG = "TimerService"
        
        private val _timerState = MutableStateFlow(TimerState.Idle)
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
        
        fun isActive(): Boolean {
            return _timerState.value.playbackState == PlaybackState.RUNNING ||
                   _timerState.value.playbackState == PlaybackState.PAUSED
        }
    }
    
    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesManager.getInstance(newBase)
        val language = runBlocking { 
            prefs.userPreferencesFlow.first().language 
        }
        super.attachBaseContext(LocaleHelper.wrapContext(newBase, language))
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        val database = AppDatabase.getInstance(this)
        repository = TimerRepositoryImpl(
            database.timerSequenceDao(),
            database.timerPhaseDao()
        )
        
        notificationHelper = TimerNotificationHelper(this)
        
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ToneGenerator", e)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getTimerAction()
        
        if (action != null) {
            handleAction(action)
        }
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        
        stopTimer()
        toneGenerator?.release()
        toneGenerator = null
        mediaPlayer?.release()
        mediaPlayer = null
        serviceScope.cancel()
    }

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
    
    private fun startSequence(sequenceId: Long) {
        serviceScope.launch {
            try {
                if (_timerState.value.playbackState == PlaybackState.RUNNING ||
                    _timerState.value.playbackState == PlaybackState.PAUSED) {
                    stopCountdown()
                }
                
                val sequence = repository.getSequenceById(sequenceId)
                
                if (sequence == null || sequence.phases.isEmpty()) {
                    Log.e(TAG, "Invalid sequence or no phases found")
                    stopTimer()
                    return@launch
                }
                
                currentSequence = sequence
                currentPhasesList = sequence.phases.sortedBy { it.order }
                
                currentPhaseIndex = 0
                currentRepetitionIndex = 0
                
                val firstPhase = currentPhasesList[0]
                currentPhaseRemainingSeconds = firstPhase.durationSeconds
                
                updateState(
                    playbackState = PlaybackState.RUNNING,
                    sequenceId = sequence.id,
                    sequenceName = sequence.name,
                    currentPhaseIndex = currentPhaseIndex,
                    currentRepetitionIndex = currentRepetitionIndex,
                    totalRepetitions = firstPhase.repetitions,
                    currentPhaseType = firstPhase.phaseType,
                    totalPhases = currentPhasesList.size,
                    allPhases = currentPhasesList,
                    remainingSeconds = currentPhaseRemainingSeconds,
                    phaseDurationSeconds = firstPhase.durationSeconds
                )
                
                startForegroundService()
                
                startCountdown()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error starting sequence", e)
                stopTimer()
            }
        }
    }
    
    private fun startCountdown() {
        stopCountdown()
        
        countDownTimer = object : CountDownTimer(
            (currentPhaseRemainingSeconds * 1000L) + 500,
            1000L
        ) {
            override fun onTick(millisUntilFinished: Long) {
                currentPhaseRemainingSeconds = (millisUntilFinished / 1000).toInt()
                
                updateStateTime(
                    remainingSeconds = currentPhaseRemainingSeconds
                )
            }
            
            override fun onFinish() {
                onPhaseComplete()
            }
        }.start()
    }
    
    private fun stopCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }
    
    private fun onPhaseComplete() {
        Log.d(TAG, "Phase complete: phase=$currentPhaseIndex, rep=$currentRepetitionIndex")
        
        playBeep()
        
        val currentPhase = currentPhasesList.getOrNull(currentPhaseIndex) ?: return
        
        if (currentRepetitionIndex < currentPhase.repetitions - 1) {
            currentRepetitionIndex++
            currentPhaseRemainingSeconds = currentPhase.durationSeconds
            
            updateState(
                playbackState = PlaybackState.RUNNING,
                currentRepetitionIndex = currentRepetitionIndex,
                remainingSeconds = currentPhaseRemainingSeconds
            )
            
            startCountdown()
        } else {
            if (currentPhaseIndex < currentPhasesList.size - 1) {
                currentPhaseIndex++
                currentRepetitionIndex = 0
                
                val nextPhase = currentPhasesList[currentPhaseIndex]
                currentPhaseRemainingSeconds = nextPhase.durationSeconds
                
                updateState(
                    playbackState = PlaybackState.RUNNING,
                    currentPhaseIndex = currentPhaseIndex,
                    currentRepetitionIndex = currentRepetitionIndex,
                    totalRepetitions = nextPhase.repetitions,
                    currentPhaseType = nextPhase.phaseType,
                    remainingSeconds = currentPhaseRemainingSeconds,
                    phaseDurationSeconds = nextPhase.durationSeconds
                )
                
                startCountdown()
            } else {
                onSequenceComplete()
            }
        }
    }
    
    private fun onSequenceComplete() {
        Log.d(TAG, "Sequence completed")
        
        playBeep()
        playBeep()
        
        updateState(
            playbackState = PlaybackState.COMPLETED,
            remainingSeconds = 0
        )
        
        serviceScope.launch {
            kotlinx.coroutines.delay(3000)
            if (_timerState.value.playbackState == PlaybackState.COMPLETED) {
                stopSelf()
            }
        }
    }
    
    private fun pauseTimer() {
        if (_timerState.value.playbackState != PlaybackState.RUNNING) return
        
        Log.d(TAG, "Timer paused")
        stopCountdown()
        
        updateState(playbackState = PlaybackState.PAUSED)
    }
    
    private fun resumeTimer() {
        if (_timerState.value.playbackState != PlaybackState.PAUSED) return
        
        Log.d(TAG, "Timer resumed")
        
        updateState(playbackState = PlaybackState.RUNNING)
        startCountdown()
    }
    
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
            phaseDurationSeconds = 0
        )
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun skipNext() {
        if (_timerState.value.playbackState == PlaybackState.IDLE) return
        
        val currentPhase = currentPhasesList.getOrNull(currentPhaseIndex) ?: return
        val wasRunning = _timerState.value.playbackState == PlaybackState.RUNNING
        
        stopCountdown()
        
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
            Log.d(TAG, "Skipping last phase/repetition - completing sequence")
            onSequenceComplete()
        }
    }
    
    private fun skipPrevious() {
        if (_timerState.value.playbackState == PlaybackState.IDLE) return
        
        val currentPhase = currentPhasesList.getOrNull(currentPhaseIndex) ?: return
        val wasRunning = _timerState.value.playbackState == PlaybackState.RUNNING
        
        stopCountdown()
        
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
            currentPhaseIndex--
            
            val prevPhase = currentPhasesList[currentPhaseIndex]
            currentRepetitionIndex = prevPhase.repetitions - 1
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
            Log.d(TAG, "Already at first phase/repetition")
        }
    }
    
    private fun updateState(
        playbackState: PlaybackState = _timerState.value.playbackState,
        sequenceId: Long = _timerState.value.sequenceId,
        sequenceName: String = _timerState.value.sequenceName,
        currentPhaseIndex: Int = _timerState.value.currentPhaseIndex,
        currentRepetitionIndex: Int = _timerState.value.currentRepetitionIndex,
        totalRepetitions: Int = _timerState.value.totalRepetitions,
        currentPhaseType: PhaseType = _timerState.value.currentPhaseType,
        totalPhases: Int = _timerState.value.totalPhases,
        allPhases: List<TimerPhaseModel> = _timerState.value.allPhases,
        remainingSeconds: Int = _timerState.value.remainingSeconds,
        phaseDurationSeconds: Int = _timerState.value.phaseDurationSeconds
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
            allPhases = allPhases,
            remainingSeconds = remainingSeconds,
            phaseDurationSeconds = phaseDurationSeconds
        )
        
        _timerState.value = newState
        
        if (playbackState != PlaybackState.IDLE) {
            notificationHelper.updateNotification(newState)
        }
    }
    
    private fun updateStateTime(
        remainingSeconds: Int
    ) {
        _timerState.value = _timerState.value.copy(
            remainingSeconds = remainingSeconds
        )
        
        notificationHelper.updateNotification(_timerState.value)
    }
    
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
    
    private fun playBeep() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.taco_bell_bong_sfx)
            }
            
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    player.seekTo(0)
                }
                player.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play custom sound", e)
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            } catch (inner: Exception) {
                Log.e(TAG, "Fallback beep also failed", inner)
            }
        }
    }
}
