package com.example.timer.ui.util

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.timer.service.TimerAction
import com.example.timer.service.TimerService
import com.example.timer.service.putTimerAction

/**
 * Helper object for interacting with TimerService
 * Provides convenient methods to send timer control commands
 * 
 * Responsibilities:
 * - Create and send Intents to TimerService
 * - Handle foreground service starting requirements
 * - Provide type-safe timer control API
 * 
 * Usage:
 * ```
 * TimerServiceHelper.startTimer(context, sequenceId = 123)
 * TimerServiceHelper.pauseTimer(context)
 * TimerServiceHelper.stopTimer(context)
 * ```
 */
object TimerServiceHelper {
    
    /**
     * Start a timer sequence
     * @param context Android context
     * @param sequenceId ID of the sequence to run
     */
    fun startTimer(context: Context, sequenceId: Long) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.Start(sequenceId))
        }
        startServiceSafely(context, intent)
    }
    
    /**
     * Pause the currently running timer
     * @param context Android context
     */
    fun pauseTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.Pause)
        }
        startServiceSafely(context, intent)
    }
    
    /**
     * Resume a paused timer
     * @param context Android context
     */
    fun resumeTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.Resume)
        }
        startServiceSafely(context, intent)
    }
    
    /**
     * Stop the timer and clear state
     * @param context Android context
     */
    fun stopTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.Stop)
        }
        startServiceSafely(context, intent)
    }
    
    /**
     * Skip to the next phase or repetition
     * @param context Android context
     */
    fun skipNext(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.SkipNext)
        }
        startServiceSafely(context, intent)
    }
    
    /**
     * Skip to the previous phase or repetition
     * @param context Android context
     */
    fun skipPrevious(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.SkipPrevious)
        }
        startServiceSafely(context, intent)
    }
    
    /**
     * Start service with proper API level handling
     * Uses startForegroundService on Android O+ when needed
     */
    private fun startServiceSafely(context: Context, intent: Intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            // Handle exception (e.g., service not found, permission issues)
            e.printStackTrace()
        }
    }
}
