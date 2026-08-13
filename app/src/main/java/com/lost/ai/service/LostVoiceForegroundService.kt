package com.lost.ai.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.lost.ai.MainActivity

/**
 * LOST Voice Foreground Service
 * Keeps Gemini Live voice conversation active in background when user:
 * - Switches apps or returns to home screen
 * - Locks screen or turns off display
 */
class LostVoiceForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "lost_voice_foreground_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_VOICE = "com.lost.ai.action.START_VOICE"
        const val ACTION_PAUSE_VOICE = "com.lost.ai.action.PAUSE_VOICE"
        const val ACTION_RESUME_VOICE = "com.lost.ai.action.RESUME_VOICE"
        const val ACTION_STOP_VOICE = "com.lost.ai.action.STOP_VOICE"

        var isServiceRunning = false
            private set

        var isVoicePaused = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, LostVoiceForegroundService::class.java).apply {
                action = ACTION_START_VOICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LostVoiceForegroundService::class.java).apply {
                action = ACTION_STOP_VOICE
            }
            context.startService(intent)
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_VOICE -> {
                isServiceRunning = true
                isVoicePaused = false
                startForegroundWithNotification()
            }
            ACTION_PAUSE_VOICE -> {
                isVoicePaused = true
                updateNotification("Voice Conversation Paused")
            }
            ACTION_RESUME_VOICE -> {
                isVoicePaused = false
                updateNotification("Microphone Active • Background Voice Call")
            }
            ACTION_STOP_VOICE -> {
                stopSelfAndCleanup()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification("Microphone Active • Background Voice Call")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(statusText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, LostVoiceForegroundService::class.java).apply {
            action = ACTION_PAUSE_VOICE
        }
        val pendingPause = PendingIntent.getService(
            this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = Intent(this, LostVoiceForegroundService::class.java).apply {
            action = ACTION_RESUME_VOICE
        }
        val pendingResume = PendingIntent.getService(
            this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LostVoiceForegroundService::class.java).apply {
            action = ACTION_STOP_VOICE
        }
        val pendingStop = PendingIntent.getService(
            this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LOST Voice Assistant")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (isVoicePaused) {
            builder.addAction(android.R.drawable.ic_media_play, "Resume Voice", pendingResume)
        } else {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause Voice", pendingPause)
        }

        builder.addAction(android.R.drawable.ic_delete, "Stop Voice", pendingStop)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LOST Background Voice Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing status when LOST voice conversation is active in background"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "LOST:BackgroundVoiceWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun stopSelfAndCleanup() {
        isServiceRunning = false
        isVoicePaused = false

        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopSelfAndCleanup()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}