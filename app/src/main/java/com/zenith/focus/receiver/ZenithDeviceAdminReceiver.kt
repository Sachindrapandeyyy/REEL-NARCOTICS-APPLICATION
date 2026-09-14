package com.zenith.focus.receiver

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import com.zenith.focus.ZenithApplication

class ZenithDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, ZenithDeviceAdminReceiver::class.java)
        }

        fun isAdminActive(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            return dpm?.isAdminActive(getComponentName(context)) == true
        }

        private fun getActivity(context: Context): android.app.Activity? {
            var currentContext = context
            while (currentContext is android.content.ContextWrapper) {
                if (currentContext is android.app.Activity) {
                    return currentContext
                }
                currentContext = currentContext.baseContext
            }
            return null
        }

        fun createAddAdminIntent(context: Context): Intent {
            return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Activate Device Administrator to prevent Reel Narcotics from being uninstalled during active focus sessions. Break the scroll. Take back your attention."
                )
                // Do NOT set FLAG_ACTIVITY_NEW_TASK on ADD_DEVICE_ADMIN!
                // Android's DeviceAdminAdd finishes immediately if launched with FLAG_ACTIVITY_NEW_TASK.
            }
        }

        fun openDeviceAdminActivation(context: Context) {
            val activity = getActivity(context)
            try {
                val intent = createAddAdminIntent(context)
                if (activity != null) {
                    activity.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // fall through to settings list fallback
            }

            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$DeviceAdminSettingsActivity"
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "🛡️ Reel Narcotics: Uninstall Protection Active",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        val app = context.applicationContext as? ZenithApplication
        val isNuclear = app?.container?.nuclearModeRepository?.session?.value?.isCurrentlyActive() == true
        val isLock = app?.container?.lockRepository?.lockState?.value?.isCurrentlyActive() == true
        if (isNuclear || isLock) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            runCatching { dpm?.lockNow() }
            return "⛔ IMMUTABLE FOCUS IS ACTIVE: Reel Narcotics cannot be deactivated or uninstalled until your focus timer expires!"
        }
        return "Deactivating Reel Narcotics will remove OS-level uninstall protection."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}
