package com.zenith.focus.receiver

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zenith.focus.MainActivity
import com.zenith.focus.R
import com.zenith.focus.ZenithApplication
import com.zenith.focus.core.notification.HabitNotificationScheduler
import com.zenith.focus.data.repository.SettingsRepositoryImpl
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.feature.stats.FocusScoreCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MORNING_PLEDGE = "com.zenith.focus.action.MORNING_PLEDGE"
        const val ACTION_EVENING_SUMMARY = "com.zenith.focus.action.EVENING_SUMMARY"

        const val NOTIFICATION_ID_MORNING = 301
        const val NOTIFICATION_ID_EVENING = 302
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = runCatching { ZenithApplication.instance }.getOrNull() ?: return@launch
                val settingsRepo = app.container.settingsRepository
                (settingsRepo as? SettingsRepositoryImpl)?.ensureInitialized()
                val statsRepo = app.container.statisticsRepository
                val habitConfig = settingsRepo.habitConfig.value

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return@launch

                HabitNotificationScheduler.ensureNotificationChannel(context)

                val tapIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val tapPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    tapIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val canPostNotifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

                when (action) {
                    ACTION_MORNING_PLEDGE -> {
                        if (habitConfig.morningPledgeEnabled && canPostNotifications) {
                            val notification = NotificationCompat.Builder(context, HabitNotificationScheduler.CHANNEL_ID_HABITS)
                                .setSmallIcon(R.drawable.ic_reel_narcotics_logo)
                                .setContentTitle("🌅 Morning Focus Pledge")
                                .setContentText("Break the scroll today. Set your intention: zero mindless dopamine loops.")
                                .setStyle(NotificationCompat.BigTextStyle().bigText("Break the scroll today. Your time is your life: choose long-term focus over cheap algorithmic dopamine."))
                                .setContentIntent(tapPendingIntent)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .build()

                            runCatching { notificationManager.notify(NOTIFICATION_ID_MORNING, notification) }
                        }
                    }
                    ACTION_EVENING_SUMMARY -> {
                        if (habitConfig.eveningSummaryEnabled && canPostNotifications) {
                            val todayBlocks = runCatching { statsRepo.getTodayBlockCount() }.getOrDefault(0)
                            val todayImpulses = runCatching { statsRepo.getTodayImpulsesDefeated() }.getOrDefault(0)
                            val todayDoomscrolls = runCatching { statsRepo.getTodayCountByCategory(ContentCategory.DOOMSCROLL_BROKEN) }.getOrDefault(0)
                            val todayTamper = runCatching { statsRepo.getTodayCountByCategory(ContentCategory.SYSTEM_TAMPER) }.getOrDefault(0)
                            val shortFormBlocks = (todayBlocks - todayImpulses - todayDoomscrolls - todayTamper).coerceAtLeast(0)
                            val streak = runCatching { statsRepo.getFocusStreakDays() }.getOrDefault(1)
                            val minutesSaved = FocusScoreCalculator.calculateTimeReclaimedMinutes(
                                impulsesDefeated = todayImpulses,
                                doomscrollsBroken = todayDoomscrolls,
                                shortFormBlocks = shortFormBlocks
                            )

                            val (title, text) = if (todayBlocks > 0) {
                                Pair(
                                    "🔥 Evening Victory: $todayBlocks Distractions Blocked!",
                                    "You reclaimed ~$minutesSaved mins today! Focus streak: $streak days strong. Keep the fire burning!"
                                )
                            } else {
                                Pair(
                                    "✨ Pure Day: Zero Distractions",
                                    "Zero impulse shorts detected today. True digital discipline! Streak: $streak days."
                                )
                            }

                            val notification = NotificationCompat.Builder(context, HabitNotificationScheduler.CHANNEL_ID_HABITS)
                                .setSmallIcon(R.drawable.ic_reel_narcotics_logo)
                                .setContentTitle(title)
                                .setContentText(text)
                                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                                .setContentIntent(tapPendingIntent)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .build()

                            runCatching { notificationManager.notify(NOTIFICATION_ID_EVENING, notification) }
                        }
                    }
                }

                // Re-arm alarms for the next day
                HabitNotificationScheduler.reschedule(context, habitConfig)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
