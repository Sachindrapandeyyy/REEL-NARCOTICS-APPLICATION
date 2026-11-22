package com.zenith.focus.core.security

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.UserManager
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver

/**
 * Manages optional ADB Device Owner ("God Mode") capabilities for ultimate tamper-proofing.
 * When provisioned as Device Owner, Reel Narcotics can natively block uninstallation,
 * disable application control (Force Stop / Clear Data), and prevent Safe Mode reboots.
 */
object DeviceOwnerManager {

    const val ADB_PROVISION_COMMAND = "adb shell dpm set-device-owner com.zenith.focus/.receiver.ZenithDeviceAdminReceiver"

    fun getAdminComponent(context: Context): ComponentName {
        return ZenithDeviceAdminReceiver.getComponentName(context)
    }

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        return try {
            dpm.isDeviceOwnerApp(context.packageName)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Enforces OS-level lockdown during active Nuclear Mode sessions:
     * 1. Blocks uninstallation at the package manager level.
     * 2. Grays out "Force Stop", "Clear Storage", and "Disable" across all system settings.
     * 3. Blocks booting into Android Safe Mode to bypass restrictions.
     */
    fun applyNuclearRestrictions(context: Context): Boolean {
        if (!isDeviceOwner(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        val admin = getAdminComponent(context)

        return try {
            dpm.setUninstallBlocked(admin, context.packageName, true)
            dpm.addUserRestriction(admin, UserManager.DISALLOW_APPS_CONTROL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("DeviceOwnerManager", "Failed to apply nuclear restrictions: ${e.message}")
            false
        }
    }

    /**
     * Cleanly clears user restrictions once Nuclear Mode expires.
     */
    fun clearNuclearRestrictions(context: Context): Boolean {
        if (!isDeviceOwner(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        val admin = getAdminComponent(context)

        return try {
            dpm.setUninstallBlocked(admin, context.packageName, false)
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_APPS_CONTROL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpm.clearUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("DeviceOwnerManager", "Failed to clear nuclear restrictions: ${e.message}")
            false
        }
    }

    /**
     * Deactivates Device Owner status upon user request.
     * STRICT SAFETY RULE: Must ONLY be callable when NO lock session is active.
     */
    fun deactivateDeviceOwner(context: Context): Boolean {
        if (!isDeviceOwner(context)) return false
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false

        return try {
            clearNuclearRestrictions(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dpm.clearDeviceOwnerApp(context.packageName)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("DeviceOwnerManager", "Failed to clear device owner: ${e.message}")
            false
        }
    }
}
