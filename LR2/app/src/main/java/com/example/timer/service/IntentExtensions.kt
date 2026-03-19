package com.example.timer.service

import android.content.Intent

/**
 * Extension functions for working with TimerAction and Intent
 * Provides type-safe serialization/deserialization of service commands
 */

/**
 * Put a TimerAction into this Intent's extras
 * @param action The action to serialize
 * @return This Intent for chaining
 */
fun Intent.putTimerAction(action: TimerAction): Intent {
    when (action) {
        is TimerAction.Start -> {
            putExtra(TimerAction.EXTRA_ACTION_TYPE, TimerAction.ACTION_START)
            putExtra(TimerAction.EXTRA_SEQUENCE_ID, action.sequenceId)
        }
        is TimerAction.Pause -> {
            putExtra(TimerAction.EXTRA_ACTION_TYPE, TimerAction.ACTION_PAUSE)
        }
        is TimerAction.Resume -> {
            putExtra(TimerAction.EXTRA_ACTION_TYPE, TimerAction.ACTION_RESUME)
        }
        is TimerAction.Stop -> {
            putExtra(TimerAction.EXTRA_ACTION_TYPE, TimerAction.ACTION_STOP)
        }
        is TimerAction.SkipNext -> {
            putExtra(TimerAction.EXTRA_ACTION_TYPE, TimerAction.ACTION_SKIP_NEXT)
        }
        is TimerAction.SkipPrevious -> {
            putExtra(TimerAction.EXTRA_ACTION_TYPE, TimerAction.ACTION_SKIP_PREVIOUS)
        }
    }
    return this
}

/**
 * Extract a TimerAction from this Intent's extras
 * @return The deserialized TimerAction, or null if not found or invalid
 */
fun Intent.getTimerAction(): TimerAction? {
    val actionType = getStringExtra(TimerAction.EXTRA_ACTION_TYPE) ?: return null
    
    return when (actionType) {
        TimerAction.ACTION_START -> {
            val sequenceId = getLongExtra(TimerAction.EXTRA_SEQUENCE_ID, 0)
            if (sequenceId > 0) TimerAction.Start(sequenceId) else null
        }
        TimerAction.ACTION_PAUSE -> TimerAction.Pause
        TimerAction.ACTION_RESUME -> TimerAction.Resume
        TimerAction.ACTION_STOP -> TimerAction.Stop
        TimerAction.ACTION_SKIP_NEXT -> TimerAction.SkipNext
        TimerAction.ACTION_SKIP_PREVIOUS -> TimerAction.SkipPrevious
        else -> null
    }
}
