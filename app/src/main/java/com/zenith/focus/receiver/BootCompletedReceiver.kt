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

        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            CoroutineScope(Dispatchers.IO).launch {
                val app = runCatching { ZenithApplication.instance }.getOrNull() ?: return@launch
                val lockRepo = app.container.lockRepository
                val state = lockRepo.lockState.value
                val now = System.currentTimeMillis()

                val nuclearRepo = app.container.nuclearModeRepository
                nuclearRepo.onDeviceRebooted()
                val nuclearSession = nuclearRepo.session.value

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

                // Schedule Nuclear Mode expiration wake-up if active
                if (nuclearSession.isCurrentlyActive(now)) {
                    val nucIntent = Intent(context, LockAlarmReceiver::class.java).apply {
                        putExtra("IS_NUCLEAR", true)
                    }
                    val nucPending = PendingIntent.getBroadcast(
                        context,
                        2002,
                        nucIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    runCatching {
                        alarmManager?.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nuclearSession.endTimeMillis,
                            nucPending
                        )
                    }.onFailure {
                        alarmManager?.set(AlarmManager.RTC_WAKEUP, nuclearSession.endTimeMillis, nucPending)
                    }
                }

                if (state.isActive && now < state.endTimeMillis) {
                    // Reschedule Alarm for normal lock expiration
                    val alarmIntent = Intent(context, LockAlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        1001,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    runCatching {
                        alarmManager?.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            state.endTimeMillis,
                            pendingIntent
                        )
                    }.onFailure {
                        alarmManager?.set(AlarmManager.RTC_WAKEUP, state.endTimeMillis, pendingIntent)
                    }
                } else if (state.isActive && now >= state.endTimeMillis) {
                    lockRepo.endLock()
                }
            }
        }
    }
}
