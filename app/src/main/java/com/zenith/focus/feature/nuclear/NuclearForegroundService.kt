package com.zenith.focus.feature.nuclear

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zenith.focus.MainActivity
import com.zenith.focus.R

/**
 * Lightweight persistent Foreground Service designed to prevent Android 13-15's
 * cgroup v2 Cached Apps Freezer from suspending Reel Narcotics during active Nuclear Sessions.
 */
class NuclearForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "zenith_nuclear_focus_channel"
        const val NOTIFICATION_ID = 9001
        const val ACTION_START = "com.zenith.focus.action.START_NUCLEAR_FGS"
        const val ACTION_STOP = "com.zenith.focus.action.STOP_NUCLEAR_FGS"

        fun start(context: Context) {
            val intent = Intent(context, NuclearForegroundService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    androidx.core.content.ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Catches ForegroundServiceStartNotAllowedException on API 31+, SecurityException, IllegalStateException
                android.util.Log.e("NuclearFGS", "Failed to start foreground service: ${e.message}", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NuclearForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                runCatching { context.stopService(intent) }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForegroundService()
            return START_NOT_STICKY
        }

        // Guarantee NotificationChannel is always created before startForeground()
        createNotificationChannel()

        val notification = buildPersistentNotification()
        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // Catches ForegroundServiceStartNotAllowedException (API 31+), SecurityException, IllegalStateException
            android.util.Log.e("NuclearFGS", "Failed to promote to foreground: ${e.message}", e)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun stopForegroundService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (e: Exception) {
            android.util.Log.w("NuclearFGS", "Error stopping foreground: ${e.message}")
        } finally {
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nuclear Focus Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active distraction shield and anti-tamper protection during Nuclear Mode"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildPersistentNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("☢️ Nuclear Focus Shield Active")
            .setContentText("Distraction feeds locked unconditionally. Focus is protected.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }
}
