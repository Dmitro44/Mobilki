package com.example.timer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.timer.MainActivity
import com.example.timer.R
import com.example.timer.domain.model.getLocalizedName
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoRight
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo
import java.util.Locale.getDefault

class TimerNotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val cachedPendingIntents = mutableMapOf<String, PendingIntent>()
    
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
    
    private fun isXiaomiDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer == "xiaomi" || manufacturer == "poco" || manufacturer == "redmi"
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows timer countdown and controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun buildNotification(state: TimerState): Notification {
        return if (isXiaomiDevice()) {
            buildXiaomiNotification(state)
        } else {
            buildStandardNotification(state)
        }
    }
    
    private fun buildXiaomiNotification(state: TimerState): Notification {
        val timerIcon = HyperPicture("timer_icon", context, R.drawable.ic_timer)
        
        val hyperBuilder = HyperIslandNotification.Builder(
            context = context,
            businessName = "timer_service",
            ticker = CHANNEL_NAME
        )
        
        hyperBuilder.addPicture(timerIcon)
        
        val actionKeys = addIconActions(hyperBuilder, state)
        
        hyperBuilder.setBaseInfo(
            title = state.getFormattedRemainingTime(),
            content = "${getNotificationTitle(state)} • ${state.currentPhaseType.getLocalizedName(context)}",
            actionKeys = actionKeys
        )
        
        hyperBuilder.setIslandConfig(
            priority = 0,
            dismissible = false,
        )
        
        hyperBuilder.setSmallIsland("timer_icon")
        hyperBuilder.setIslandFirstFloat(false)
        hyperBuilder.setEnableFloat(false)
        
        hyperBuilder.setBigIslandInfo(
            left = ImageTextInfoLeft(
                type = 1,
                picInfo = PicInfo(type = 1, pic = "timer_icon"),
                textInfo = TextInfo(
                    title = state.sequenceName + " ",
                    content = state.currentPhaseType.getLocalizedName(context).lowercase(getDefault())
                )
            ),
            right = ImageTextInfoRight(
                type = 2,
                textInfo = TextInfo(
                    title = state.getFormattedRemainingTime() + " ",
                    content = state.getRepetitionDisplay()
                )
            )
        )
        
        val resourceBundle = hyperBuilder.buildResourceBundle()
        val jsonParam = hyperBuilder.buildJsonParam()
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(getNotificationTitle(state))
            .setContentText(getNotificationContent(state))
            .setOngoing(state.playbackState == PlaybackState.RUNNING)
            .setContentIntent(createContentIntent(state))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addExtras(resourceBundle)
            .setExtras(resourceBundle.apply { 
                putString("miui.focus.param", jsonParam)
            })
            .build()
    }
    
    private fun buildStandardNotification(state: TimerState): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(getNotificationTitle(state))
            .setContentText(getNotificationContent(state))
            .setOngoing(state.playbackState == PlaybackState.RUNNING)
            .setContentIntent(createContentIntent(state))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        
        addActionsStandard(builder, state)
        
        return builder.build()
    }
    
    private fun getNotificationTitle(state: TimerState): String {
        return when (state.playbackState) {
            PlaybackState.IDLE -> context.getString(R.string.timer_ready)
            PlaybackState.RUNNING -> state.sequenceName
            PlaybackState.PAUSED -> "${state.sequenceName} (${context.getString(R.string.state_paused)})"
            PlaybackState.COMPLETED -> context.getString(R.string.timer_completed)
        }
    }
    
    private fun getNotificationContent(state: TimerState): String {
        return when (state.playbackState) {
            PlaybackState.IDLE -> context.getString(R.string.ready_to_start)
            PlaybackState.RUNNING, PlaybackState.PAUSED -> {
                val phaseInfo = "${state.currentPhaseType.getLocalizedName(context)} - ${state.getFormattedRemainingTime()}"
                val repInfo = "${context.getString(R.string.rep_label)} ${state.getRepetitionDisplay()}, ${context.getString(R.string.phase_label)} ${state.getPhaseDisplay()}"
                "$phaseInfo • $repInfo"
            }
            PlaybackState.COMPLETED -> context.getString(R.string.all_phases_completed)
        }
    }

    private fun addIconActions(builder: HyperIslandNotification, state: TimerState): List<String> {
        val actionKeys = mutableListOf<String>()
        
        when (state.playbackState) {
            PlaybackState.RUNNING -> {
                val pauseAction = HyperAction(
                    key = "pause",
                    title = context.getString(R.string.pause),
                    icon = Icon.createWithResource(context, R.drawable.ic_pause),
                    pendingIntent = createActionIntent(TimerAction.Pause, REQUEST_CODE_PAUSE),
                    actionIntentType = 1
                )
                val stopAction = HyperAction(
                    key = "stop",
                    title = context.getString(R.string.stop),
                    icon = Icon.createWithResource(context, R.drawable.ic_stop),
                    pendingIntent = createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP),
                    actionIntentType = 1
                )
                val skipAction = HyperAction(
                    key = "skip",
                    title = context.getString(R.string.skip),
                    icon = Icon.createWithResource(context, R.drawable.ic_skip_next),
                    pendingIntent = createActionIntent(TimerAction.SkipNext, REQUEST_CODE_SKIP),
                    actionIntentType = 1
                )
                
                builder.addAction(pauseAction)
                builder.addAction(stopAction)
                builder.addAction(skipAction)
                
                actionKeys.addAll(listOf("pause", "stop", "skip"))
            }
            PlaybackState.PAUSED -> {
                val resumeAction = HyperAction(
                    key = "resume",
                    title = context.getString(R.string.resume),
                    icon = Icon.createWithResource(context, R.drawable.ic_play),
                    pendingIntent = createActionIntent(TimerAction.Resume, REQUEST_CODE_RESUME),
                    actionIntentType = 1
                )
                val stopAction = HyperAction(
                    key = "stop",
                    title = context.getString(R.string.stop),
                    icon = Icon.createWithResource(context, R.drawable.ic_stop),
                    pendingIntent = createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP),
                    actionIntentType = 1
                )
                val skipAction = HyperAction(
                    key = "skip",
                    title = context.getString(R.string.skip),
                    icon = Icon.createWithResource(context, R.drawable.ic_skip_next),
                    pendingIntent = createActionIntent(TimerAction.SkipNext, REQUEST_CODE_SKIP),
                    actionIntentType = 1
                )
                
                builder.addAction(resumeAction)
                builder.addAction(stopAction)
                builder.addAction(skipAction)
                
                actionKeys.addAll(listOf("resume", "stop", "skip"))
            }
            PlaybackState.COMPLETED -> {
                val dismissAction = HyperAction(
                    key = "dismiss",
                    title = context.getString(R.string.dismiss),
                    icon = Icon.createWithResource(context, R.drawable.ic_stop),
                    pendingIntent = createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP),
                    actionIntentType = 1
                )
                
                builder.addAction(dismissAction)
                actionKeys.add("dismiss")
            }
            PlaybackState.IDLE -> {
                // No actions
            }
        }
        
        return actionKeys
    }

    private fun addActionsStandard(builder: NotificationCompat.Builder, state: TimerState) {
        when (state.playbackState) {
            PlaybackState.RUNNING -> {
                builder.addAction(
                    R.drawable.ic_pause,
                    context.getString(R.string.pause),
                    createActionIntent(TimerAction.Pause, REQUEST_CODE_PAUSE)
                )
                builder.addAction(
                    R.drawable.ic_stop,
                    context.getString(R.string.stop),
                    createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP)
                )
                builder.addAction(
                    R.drawable.ic_skip_next,
                    context.getString(R.string.skip),
                    createActionIntent(TimerAction.SkipNext, REQUEST_CODE_SKIP)
                )
            }
            PlaybackState.PAUSED -> {
                builder.addAction(
                    R.drawable.ic_play,
                    context.getString(R.string.resume),
                    createActionIntent(TimerAction.Resume, REQUEST_CODE_RESUME)
                )
                builder.addAction(
                    R.drawable.ic_stop,
                    context.getString(R.string.stop),
                    createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP)
                )
                builder.addAction(
                    R.drawable.ic_skip_next,
                    context.getString(R.string.skip),
                    createActionIntent(TimerAction.SkipNext, REQUEST_CODE_SKIP)
                )
            }
            PlaybackState.COMPLETED -> {
                builder.addAction(
                    R.drawable.ic_stop,
                    context.getString(R.string.dismiss),
                    createActionIntent(TimerAction.Stop, REQUEST_CODE_STOP)
                )
            }
            PlaybackState.IDLE -> {
                // No actions when idle
            }
        }
    }
    
    private fun createContentIntent(state: TimerState): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_SEQUENCE_ID", state.sequenceId)
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createActionIntent(action: TimerAction, requestCode: Int): PendingIntent {
        val cacheKey = "${action.javaClass.simpleName}_$requestCode"
        
        return cachedPendingIntents.getOrPut(cacheKey) {
            val intent = Intent(context, TimerService::class.java).apply {
                putTimerAction(action)
            }
            
            PendingIntent.getService(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
    
    fun updateNotification(state: TimerState) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
    }
}
