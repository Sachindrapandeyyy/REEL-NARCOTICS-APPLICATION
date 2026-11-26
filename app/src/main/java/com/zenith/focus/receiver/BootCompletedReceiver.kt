package com.zenith.focus.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.os.UserManagerCompat
import com.zenith.focus.ZenithApplication
import com.zenith.focus.data.repository.AppLockRepositoryImpl
import com.zenith.focus.data.repository.SettingsRepositoryImpl
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
            // Credential-Encrypted storage (DataStore / SQLite) is locked before user unlock on Direct Boot.
            if (!UserManagerCompat.isUserUnlocked(context)) {
                return
            }
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = runCatching { ZenithApplication.instance }.getOrNull() ?: return@launch
                    val settingsRepo = app.container.settingsRepository
                    val appLockRepo = app.container.appLockRepository
                    (settingsRepo as? SettingsRepositoryImpl)?.ensureInitialized()
                    (appLockRepo as? AppLockRepositoryImpl)?.ensureInitialized()

                    val lockRepo = app.container.lockRepository
                    lockRepo.refreshLockState()
                    val state = lockRepo.lockState.value
                    val now = System.currentTimeMillis()

                    val nuclearRepo = app.container.nuclearModeRepository
                    nuclearRepo.onDeviceRebooted()
                    val nuclearSession = nuclearRepo.session.value

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

                    // Schedule Nuclear Mode expiration wake-up if active (exact alarm when permitted, Doze-safe fallback otherwise)
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
                        scheduleSafeAlarm(alarmManager, nuclearSession.endTimeMillis, nucPending)
                        val protectionConfig = app.container.settingsRepository.protectionConfig.value
                        if (protectionConfig.enableAutoGrayscale) {
                            com.zenith.focus.feature.sensory.GrayscaleController.applyGrayscaleIfEnabled(context, true)
                        }
                    } else {
                        com.zenith.focus.feature.sensory.GrayscaleController.restoreColor(context)
                    }

                    if (state.isActive && now < state.endTimeMillis) {
                        val alarmIntent = Intent(context, LockAlarmReceiver::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            1001,
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        scheduleSafeAlarm(alarmManager, state.endTimeMillis, pendingIntent)
                    } else if (state.isActive && now >= state.endTimeMillis) {
                        lockRepo.endLock()
                    }

                    // Reschedule daily habit notifications on boot
                    val habitConfig = app.container.settingsRepository.habitConfig.value
                    com.zenith.focus.core.notification.HabitNotificationScheduler.reschedule(context, habitConfig)
                } catch (t: Throwable) {
                    android.util.Log.e("BootCompletedReceiver", "Error handling boot/update broadcast: ${t.message}", t)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun scheduleSafeAlarm(
        alarmManager: AlarmManager?,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        if (alarmManager == null) return
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }.onFailure {
            runCatching {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
        }
    }
}
