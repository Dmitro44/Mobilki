package com.example.timer.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.timer.service.TimerService
import com.example.timer.service.TimerState
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the Timer screen
 */
class TimerViewModel : ViewModel() {
    
    val timerState: StateFlow<TimerState> = TimerService.timerState
    
    fun isTimerActive(): Boolean {
        return TimerService.isActive()
    }
}
