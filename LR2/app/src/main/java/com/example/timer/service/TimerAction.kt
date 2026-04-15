package com.example.timer.service

sealed class TimerAction {
    data class Start(val sequenceId: Long) : TimerAction()
    data object Pause : TimerAction()
    data object Resume : TimerAction()
    data object Stop : TimerAction()
    data object SkipNext : TimerAction()
    data object SkipPrevious : TimerAction()
    
    companion object {
        const val EXTRA_ACTION_TYPE = "action_type"
        const val EXTRA_SEQUENCE_ID = "sequence_id"
        
        const val ACTION_START = "start"
        const val ACTION_PAUSE = "pause"
        const val ACTION_RESUME = "resume"
        const val ACTION_STOP = "stop"
        const val ACTION_SKIP_NEXT = "skip_next"
        const val ACTION_SKIP_PREVIOUS = "skip_previous"
    }
}
