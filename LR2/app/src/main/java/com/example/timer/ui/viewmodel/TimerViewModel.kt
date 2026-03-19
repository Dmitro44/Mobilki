package com.example.timer.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.timer.service.TimerService
import com.example.timer.service.TimerState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the Timer screen
 * 
 * Responsibilities:
 * - Observe TimerService.timerState singleton StateFlow
 * - Expose timer state to UI layer
 * - No business logic - pure state observation
 * 
 * The timer state is managed entirely by TimerService.
 * This ViewModel simply provides a reference to that state
 * for UI observation with lifecycle awareness.
 */
class TimerViewModel : ViewModel() {
    
    /**
     * Reference to TimerService's singleton timer state
     * UI can collect this StateFlow to receive real-time timer updates
     */
    val timerState: StateFlow<TimerState> = TimerService.timerState
    
    /**
     * Check if timer is currently active (running or paused)
     * Useful for UI state decisions
     */
    fun isTimerActive(): Boolean {
        return TimerService.isActive()
    }
}
