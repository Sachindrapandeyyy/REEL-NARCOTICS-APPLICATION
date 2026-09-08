package com.zenith.focus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenith.focus.ZenithApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        CoroutineScope(Dispatchers.IO).launch {
            val app = runCatching { ZenithApplication.instance }.getOrNull() ?: return@launch
            app.container.lockRepository.refreshLockState()
            app.container.nuclearModeRepository.checkAndUpdateExpiration()
        }
    }
}
