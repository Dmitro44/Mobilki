package com.example.timer.service

import com.example.timer.domain.model.PhaseType

/**
 * Represents the complete state of the timer service
 * This state is exposed via StateFlow for observation by the UI layer
 */
data class TimerState(
    /**
     * Current playback state
     */
    val playbackState: PlaybackState = PlaybackState.IDLE,
    
    /**
     * ID of the currently active sequence (0 if no sequence)
     */
    val sequenceId: Long = 0,
    
    /**
     * Name of the current sequence
     */
    val sequenceName: String = "",
    
    /**
     * Current phase index in the sequence
     */
    val currentPhaseIndex: Int = 0,
    
    /**
     * Current repetition index within the phase (0-based)
     */
    val currentRepetitionIndex: Int = 0,
    
    /**
     * Total repetitions for the current phase
     */
    val totalRepetitions: Int = 1,
    
    /**
     * Type of the current phase
     */
    val currentPhaseType: PhaseType = PhaseType.WORK,
    
    /**
     * Total number of phases in the sequence
     */
    val totalPhases: Int = 0,
    
    /**
     * Remaining time in the current phase (in seconds)
     */
    val remainingSeconds: Int = 0,
    
    /**
     * Total duration of the current phase (in seconds)
     */
    val phaseDurationSeconds: Int = 0,
    
    /**
     * Total duration of the entire sequence (in seconds)
     */
    val totalSequenceDuration: Int = 0,
    
    /**
     * Elapsed time in the entire sequence (in seconds)
     */
    val elapsedSequenceTime: Int = 0
) {
    /**
     * Format remaining time as MM:SS or HH:MM:SS
     */
    fun getFormattedRemainingTime(): String {
        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    /**
     * Get progress percentage within current phase
     */
    fun getPhaseProgress(): Float {
        if (phaseDurationSeconds <= 0) return 0f
        val elapsed = phaseDurationSeconds - remainingSeconds
        return (elapsed.toFloat() / phaseDurationSeconds.toFloat()).coerceIn(0f, 1f)
    }
    
    /**
     * Get overall sequence progress percentage
     */
    fun getSequenceProgress(): Float {
        if (totalSequenceDuration <= 0) return 0f
        return (elapsedSequenceTime.toFloat() / totalSequenceDuration.toFloat()).coerceIn(0f, 1f)
    }
    
    /**
     * Get current repetition display string (e.g., "1/3")
     */
    fun getRepetitionDisplay(): String {
        return "${currentRepetitionIndex + 1}/$totalRepetitions"
    }
    
    /**
     * Get current phase display string (e.g., "2/5")
     */
    fun getPhaseDisplay(): String {
        return "${currentPhaseIndex + 1}/$totalPhases"
    }
    
    /**
     * Check if this is the first phase and repetition
     */
    fun isAtStart(): Boolean {
        return currentPhaseIndex == 0 && currentRepetitionIndex == 0
    }
    
    /**
     * Check if this is the last phase and repetition
     */
    fun isAtEnd(): Boolean {
        return currentPhaseIndex >= totalPhases - 1 && 
               currentRepetitionIndex >= totalRepetitions - 1
    }
    
    companion object {
        /**
         * Default idle state
         */
        val Idle = TimerState()
    }
}
