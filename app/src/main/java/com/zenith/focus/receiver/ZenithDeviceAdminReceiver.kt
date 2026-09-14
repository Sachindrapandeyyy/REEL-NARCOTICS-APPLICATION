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

        fun openDeviceAdminActivation(context: Context): Boolean {
            val activity = getActivity(context)

            // 1. First priority: Direct ACTION_ADD_DEVICE_ADMIN intent
            try {
                val intent = createAddAdminIntent(context)
                if (activity != null) {
                    activity.startActivity(intent)
                    return true
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
            } catch (e: SecurityException) {
                // Restricted settings blocked this action on Android 13+ / MIUI / Vivo
                Toast.makeText(
                    context,
                    "⚠️ Unlock 'Allow restricted settings' in App Info first!",
                    Toast.LENGTH_LONG
                ).show()
                com.zenith.focus.core.permission.OemNavigationManager.openAppInfo(context)
                return false
            } catch (_: Exception) {
                // Try fallback device admin screens
            }

            // 2. Candidate Device Admin settings intents across OEMs (POCO, Xiaomi, Vivo, Samsung, AOSP)
            val candidateIntents = listOf(
                Intent("android.settings.DEVICE_ADMIN_SETTINGS"),
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")),
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$DeviceAdminSettingsActivity")),
                // Xiaomi / POCO (MIUI / HyperOS)
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DeviceAdminAdd")),
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.SpecialPermissionActivity")),
                Intent("miui.intent.action.DEVICE_ADMIN_SETTINGS"),
                // Vivo (Funtouch OS / OriginOS)
                Intent().setComponent(ComponentName("com.vivo.settings", "com.vivo.settings.DeviceAdminSettings")),
                Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity"))
            )

            for (cand in candidateIntents) {
                try {
                    cand.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val resolved = context.packageManager.resolveActivity(cand, 0)
                    if (resolved != null) {
                        context.startActivity(cand)
                        return true
                    }
                } catch (_: Exception) {
                    // Try next
                }
            }

            // 3. Fallback: Prompt user and open App Info or Security Settings
            Toast.makeText(
                context,
                "Please enable Device Admin for Reel Narcotics in Special Permissions",
                Toast.LENGTH_LONG
            ).show()
            return try {
                val secIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(secIntent)
                true
            } catch (_: Exception) {
                com.zenith.focus.core.permission.OemNavigationManager.openAppInfo(context)
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
