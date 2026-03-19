package com.example.timer.service

/**
 * Represents the current playback state of the timer service
 */
enum class PlaybackState {
    /**
     * Timer is idle, no active sequence
     */
    IDLE,
    
    /**
     * Timer is actively counting down
     */
    RUNNING,
    
    /**
     * Timer is paused, can be resumed
     */
    PAUSED,
    
    /**
     * Timer has completed all phases
     */
    COMPLETED
}
