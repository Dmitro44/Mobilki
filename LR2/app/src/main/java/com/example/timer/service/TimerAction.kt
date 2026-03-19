package com.example.timer.service

/**
 * Sealed class representing all possible actions that can be sent to TimerService
 * This provides type-safe command handling for the service
 */
sealed class TimerAction {
    /**
     * Start a new timer sequence
     * @property sequenceId The ID of the sequence to run
     */
    data class Start(val sequenceId: Long) : TimerAction()
    
    /**
     * Pause the currently running timer
     */
    data object Pause : TimerAction()
    
    /**
     * Resume a paused timer
     */
    data object Resume : TimerAction()
    
    /**
     * Stop the timer and clear all state
     */
    data object Stop : TimerAction()
    
    /**
     * Skip to the next phase or repetition
     */
    data object SkipNext : TimerAction()
    
    /**
     * Skip to the previous phase or repetition
     */
    data object SkipPrevious : TimerAction()
    
    companion object {
        // Intent extra keys
        const val EXTRA_ACTION_TYPE = "action_type"
        const val EXTRA_SEQUENCE_ID = "sequence_id"
        
        // Action type identifiers
        const val ACTION_START = "start"
        const val ACTION_PAUSE = "pause"
        const val ACTION_RESUME = "resume"
        const val ACTION_STOP = "stop"
        const val ACTION_SKIP_NEXT = "skip_next"
        const val ACTION_SKIP_PREVIOUS = "skip_previous"
    }
}
