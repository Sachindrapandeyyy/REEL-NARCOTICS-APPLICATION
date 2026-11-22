package com.zenith.focus.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenith.focus.ZenithApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val action = intent?.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = runCatching { ZenithApplication.instance }.getOrNull() ?: return@launch
                    val lockRepo = app.container.lockRepository
                    lockRepo.refreshLockState()
                    val state = lockRepo.lockState.value
                    val now = System.currentTimeMillis()

                    val nuclearRepo = app.container.nuclearModeRepository
                    nuclearRepo.onDeviceRebooted()
                    val nuclearSession = nuclearRepo.session.value

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

                    // Schedule Nuclear Mode expiration wake-up if active using exact alarm
                    if (nuclearSession.isCurrentlyActive(now)) {
                        try {
                            com.zenith.focus.feature.nuclear.NuclearForegroundService.start(context)
                        } catch (e: Exception) {
                            android.util.Log.w("BootCompletedReceiver", "Failed starting Nuclear FGS on boot: ${e.message}")
                        }
                        val nucIntent = Intent(context, LockAlarmReceiver::class.java).apply {
                            putExtra("IS_NUCLEAR", true)
                        }
                        val nucPending = PendingIntent.getBroadcast(
                            context,
                            2002,
                            nucIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nuclearSession.endTimeMillis, nucPending)
                        } else {
                            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, nuclearSession.endTimeMillis, nucPending)
                        }
                        val protectionConfig = app.container.settingsRepository.protectionConfig.value
                        if (protectionConfig.enableAutoGrayscale) {
                            com.zenith.focus.feature.sensory.GrayscaleController.applyGrayscaleIfEnabled(context, true)
                        }
                    } else {
                        com.zenith.focus.feature.sensory.GrayscaleController.restoreColor(context)
                    }

                    if (state.isActive && now < state.endTimeMillis) {
                        // Reschedule Alarm for normal lock expiration using exact alarm
                        val alarmIntent = Intent(context, LockAlarmReceiver::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            1001,
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, state.endTimeMillis, pendingIntent)
                        } else {
                            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, state.endTimeMillis, pendingIntent)
                        }
                    } else if (state.isActive && now >= state.endTimeMillis) {
                        lockRepo.endLock()
                    }

                    // Reschedule daily habit notifications on boot
                    val habitConfig = app.container.settingsRepository.habitConfig.value
                    com.zenith.focus.core.notification.HabitNotificationScheduler.reschedule(context, habitConfig)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
