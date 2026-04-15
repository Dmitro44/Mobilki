package com.example.timer.ui.util

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.timer.service.TimerAction
import com.example.timer.service.TimerService
import com.example.timer.service.putTimerAction

object TimerServiceHelper {
    
    fun startTimer(context: Context, sequenceId: Long) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.Start(sequenceId))
        }
        startServiceSafely(context, intent)
    }
    
    fun pauseTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.Pause)
        }
        startServiceSafely(context, intent)
    }
    
    fun resumeTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.Resume)
        }
        startServiceSafely(context, intent)
    }
    
    fun stopTimer(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.Stop)
        }
        startServiceSafely(context, intent)
    }
    
    fun skipNext(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.SkipNext)
        }
        startServiceSafely(context, intent)
    }
    
    fun skipPrevious(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(TimerAction.SkipPrevious)
        }
        startServiceSafely(context, intent)
    }
    
    private fun startServiceSafely(context: Context, intent: Intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
