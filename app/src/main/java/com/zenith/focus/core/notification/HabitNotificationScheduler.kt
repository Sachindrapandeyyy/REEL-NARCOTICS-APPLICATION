package com.zenith.focus.core.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zenith.focus.domain.model.HabitConfig
import com.zenith.focus.receiver.HabitAlarmReceiver
import java.util.Calendar

object HabitNotificationScheduler {

    const val CHANNEL_ID_HABITS = "reel_narcotics_habits"
    const val REQUEST_CODE_MORNING = 3001
    const val REQUEST_CODE_EVENING = 3002

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager?.getNotificationChannel(CHANNEL_ID_HABITS) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID_HABITS,
                    "Focus Habits & Daily Digest",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily Morning Focus Intentions and Evening Victory Digests"
                    enableVibration(true)
                }
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }

    fun reschedule(context: Context, config: HabitConfig) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        ensureNotificationChannel(context)

        // 1. Morning Focus Pledge Alarm
        val morningIntent = Intent(context, HabitAlarmReceiver::class.java).apply {
            action = HabitAlarmReceiver.ACTION_MORNING_PLEDGE
        }
        val morningPending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MORNING,
            morningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (config.morningPledgeEnabled) {
            val morningMillis = computeNextTriggerTime(config.morningPledgeHour, config.morningPledgeMinute)
            setAlarm(alarmManager, morningMillis, morningPending)
        } else {
            alarmManager.cancel(morningPending)
        }

        // 2. Evening Victory Digest Alarm
        val eveningIntent = Intent(context, HabitAlarmReceiver::class.java).apply {
            action = HabitAlarmReceiver.ACTION_EVENING_SUMMARY
        }
        val eveningPending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_EVENING,
            eveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (config.eveningSummaryEnabled) {
            val eveningMillis = computeNextTriggerTime(config.eveningSummaryHour, config.eveningSummaryMinute)
            setAlarm(alarmManager, eveningMillis, eveningPending)
        } else {
            alarmManager.cancel(eveningPending)
        }
    }

    private fun computeNextTriggerTime(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If scheduled time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private fun setAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
}
