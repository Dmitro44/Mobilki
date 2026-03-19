package com.example.timer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.timer.MainActivity
import com.example.timer.R

/**
 * Helper class for building and managing timer service notifications
 * 
 * Responsibilities:
 * - Create notification channel
 * - Build foreground service notification with timer information
 * - Add dynamic action buttons based on playback state
 * - Update notification content efficiently
 */
class TimerNotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_ID = "timer_service_channel"
        const val CHANNEL_NAME = "Timer Service"
        const val NOTIFICATION_ID = 1001
        
        private const val REQUEST_CODE_CONTENT = 100
        private const val REQUEST_CODE_PAUSE = 101
        private const val REQUEST_CODE_RESUME = 102
        private const val REQUEST_CODE_STOP = 103
        private const val REQUEST_CODE_SKIP = 104
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Create the notification channel for timer notifications
     * Only needed on Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance to avoid sound/vibration
            ).apply {
                description = "Shows timer countdown and controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Build notification based on current timer state
     * @param state Current timer state to display
     * @return Notification ready for foreground service
     */
    fun buildNotification(state: TimerState): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use app icon
            .setContentTitle(getNotificationTitle(state))
            .setContentText(getNotificationContent(state))
            .setOngoing(state.playbackState == PlaybackState.RUNNING)
            .setContentIntent(createContentIntent(state))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        
        // Add action buttons based on playback state
        addActions(builder, state)
        
        return builder.build()
    }
    
    /**
     * Get notification title based on state
     */
    private fun getNotificationTitle(state: TimerState): String {
        return when (state.playbackState) {
            PlaybackState.IDLE -> context.getString(R.string.timer_ready)
            PlaybackState.RUNNING -> state.sequenceName
            PlaybackState.PAUSED -> "${state.sequenceName} (${context.getString(R.string.state_paused)})"
            PlaybackState.COMPLETED -> context.getString(R.string.timer_completed)
        }
    }
    
    /**
     * Get notification content text based on state
     */
    private fun getNotificationContent(state: TimerState): String {
        return when (state.playbackState) {
            PlaybackState.IDLE -> context.getString(R.string.ready_to_start)
            PlaybackState.RUNNING, PlaybackState.PAUSED -> {
                val phaseInfo = "${state.currentPhaseType.name} - ${state.getFormattedRemainingTime()}"
                val repInfo = "${context.getString(R.string.rep_label)} ${state.getRepetitionDisplay()}, ${context.getString(R.string.phase_label)} ${state.getPhaseDisplay()}"
                "$phaseInfo • $repInfo"
            }
            PlaybackState.COMPLETED -> context.getString(R.string.all_phases_completed)
        }
    }
    
    /**
     * Add action buttons to notification based on current state
     */
    private fun addActions(builder: NotificationCompat.Builder, state: TimerState) {
        when (state.playbackState) {
            PlaybackState.RUNNING -> {
                // Pause and Stop actions
                builder.addAction(
                    android.R.drawable.ic_media_pause,
                    context.getString(R.string.pause),
                    createActionIntent(TimerAction.Pause, REQUEST_CODE_PAUSE)
                )
                builder.addAction(
                    android.R.drawable.ic_delete,
                    context.getString(R.string.stop),
                    createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP)
                )
                builder.addAction(
                    android.R.drawable.ic_media_next,
                    context.getString(R.string.skip),
                    createActionIntent(TimerAction.SkipNext, REQUEST_CODE_SKIP)
                )
            }
            PlaybackState.PAUSED -> {
                // Resume and Stop actions
                builder.addAction(
                    android.R.drawable.ic_media_play,
                    context.getString(R.string.resume),
                    createActionIntent(TimerAction.Resume, REQUEST_CODE_RESUME)
                )
                builder.addAction(
                    android.R.drawable.ic_delete,
                    context.getString(R.string.stop),
                    createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP)
                )
                builder.addAction(
                    android.R.drawable.ic_media_next,
                    context.getString(R.string.skip),
                    createActionIntent(TimerAction.SkipNext, REQUEST_CODE_SKIP)
                )
            }
            PlaybackState.COMPLETED -> {
                // Only stop action
                builder.addAction(
                    android.R.drawable.ic_delete,
                    context.getString(R.string.dismiss),
                    createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP)
                )
            }
            PlaybackState.IDLE -> {
                // No actions when idle
            }
        }
    }
    
    /**
     * Create PendingIntent that opens the main activity when notification is tapped
     * Includes sequenceId to restore the timer screen if the app was closed
     */
    private fun createContentIntent(state: TimerState): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_SEQUENCE_ID", state.sequenceId)
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Create PendingIntent for a timer action button
     */
    private fun createActionIntent(action: TimerAction, requestCode: Int): PendingIntent {
        val intent = Intent(context, TimerService::class.java).apply {
            putTimerAction(action)
        }
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Update the existing notification
     */
    fun updateNotification(state: TimerState) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
    }
}
