package com.zenith.focus.core.permission

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

/**
 * Categorized device manufacturer brands with custom ROM and background behavior.
 */
enum class DeviceBrand(val displayName: String, val osSkin: String) {
    XIAOMI("Xiaomi / Redmi / POCO", "MIUI / HyperOS"),
    SAMSUNG("Samsung Galaxy", "One UI"),
    OPPO_REALME_ONEPLUS("OnePlus / OPPO / Realme", "OxygenOS / ColorOS"),
    VIVO("Vivo / iQOO", "Funtouch OS / OriginOS"),
    TRANSSION("Infinix / Tecno / itel", "XOS / HiOS"),
    GENERIC("Android Device", "Standard Android")
}

/**
 * Step-by-step guidance model tailored to a specific phone manufacturer.
 */
data class OemGuidance(
    val brand: DeviceBrand,
    val step1Title: String,
    val step1Desc: String,
    val step2Title: String,
    val step2Desc: String,
    val step3Title: String? = null,
    val step3Desc: String? = null,
    val step3ButtonLabel: String? = null
)

/**
 * Multi-OEM Smart Navigation & Permission Manager.
 *
 * Solves the critical real-world Android problem where users cannot enable Accessibility directly:
 * 1. On Android 13+ (API 33+), sideloaded apps encounter "Restricted setting: For your security, this setting is currently unavailable."
 *    Users MUST open App Info, tap the 3-dots menu (⋮), and tap "Allow restricted settings" before the switch is unlocked.
 * 2. On Xiaomi / Redmi / POCO (MIUI / HyperOS), apps require Autostart and background pop-up permissions to remain active.
 * 3. On Samsung (One UI), services are killed by "Deep sleeping apps" unless battery optimization is disabled.
 */
object OemNavigationManager {

    /**
     * Detects device manufacturer brand from system properties.
     */
    fun detectDeviceBrand(
        manufacturer: String? = runCatching { Build.MANUFACTURER }.getOrNull(),
        brand: String? = runCatching { Build.BRAND }.getOrNull()
    ): DeviceBrand {
        val m = (manufacturer ?: "").lowercase()
        val b = (brand ?: "").lowercase()

        return when {
            m.contains("xiaomi") || m.contains("redmi") || b.contains("xiaomi") || b.contains("redmi") || b.contains("poco") -> {
                DeviceBrand.XIAOMI
            }
            m.contains("samsung") || b.contains("samsung") -> {
                DeviceBrand.SAMSUNG
            }
            m.contains("oneplus") || m.contains("oppo") || m.contains("realme") ||
            b.contains("oneplus") || b.contains("oppo") || b.contains("realme") -> {
                DeviceBrand.OPPO_REALME_ONEPLUS
            }
            m.contains("vivo") || m.contains("iqoo") || b.contains("vivo") || b.contains("iqoo") -> {
                DeviceBrand.VIVO
            }
            m.contains("infinix") || m.contains("tecno") || m.contains("itel") || m.contains("transsion") ||
            b.contains("infinix") || b.contains("tecno") || b.contains("itel") -> {
                DeviceBrand.TRANSSION
            }
            else -> DeviceBrand.GENERIC
        }
    }

    /**
     * Navigates directly to the native Android App Info screen for Reel Narcotics.
     * Guaranteed to work on 100% of devices.
     */
    fun openAppInfo(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open settings: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    /**
     * Navigates directly to the system Accessibility Settings screen.
     */
    fun openAccessibilitySettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openAppInfo(context)
        }
    }

    /**
     * Opens manufacturer-specific Autostart or background startup permissions.
     */
    fun openOemAutostart(context: Context): Boolean {
        val brand = detectDeviceBrand()

        val candidateIntents: List<Intent> = when (brand) {
            DeviceBrand.XIAOMI -> listOf(
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
                Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                    putExtra("extra_pkgname", context.packageName)
                },
                Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT)
            )
            DeviceBrand.OPPO_REALME_ONEPLUS -> listOf(
                Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
                Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
                Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.PermissionManagerActivity")),
                Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"))
            )
            DeviceBrand.VIVO -> listOf(
                Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
                Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
                Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity"))
            )
            DeviceBrand.TRANSSION -> listOf(
                Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity")),
                Intent().setComponent(ComponentName("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity_8_0"))
            )
            DeviceBrand.SAMSUNG -> listOf(
                Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
                Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"))
            )
            DeviceBrand.GENERIC -> emptyList()
        }

        for (intent in candidateIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val resolveInfo = context.packageManager.resolveActivity(intent, 0)
                if (resolveInfo != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (_: Exception) {
                // Try next candidate
            }
        }

        // Fallback: request ignoring battery optimizations or open app info
        return openBatteryOptimization(context)
    }

    /**
     * Opens Battery Optimization / Unrestricted background execution settings.
     */
    fun openBatteryOptimization(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                try {
                    val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallback)
                    return true
                } catch (_: Exception) {
                    // Fallback to App Info
                }
            }
        }
        return openAppInfo(context)
    }

    /**
     * Returns tailored step-by-step guidance for the detected phone manufacturer.
     */
    fun getGuidance(brand: DeviceBrand = detectDeviceBrand()): OemGuidance {
        return when (brand) {
            DeviceBrand.XIAOMI -> OemGuidance(
                brand = brand,
                step1Title = "1. Tap 'Open App Info' below",
                step1Desc = "In App Info, tap the 3-dots (⋮) in the top-right corner and select 'Allow restricted settings'. Confirm with your PIN.",
                step2Title = "2. Turn ON Shield in Accessibility",
                step2Desc = "Tap 'Open Accessibility' -> Go to 'Downloaded apps' -> Tap 'Reel Narcotics Shield' -> Toggle ON.",
                step3Title = "3. Enable MIUI Autostart (Recommended)",
                step3Desc = "Allows the focus shield to stay active when you close recent apps so locks cannot be bypassed.",
                step3ButtonLabel = "OPEN MI AUTOSTART ➔"
            )
            DeviceBrand.SAMSUNG -> OemGuidance(
                brand = brand,
                step1Title = "1. Unlock Restricted Settings",
                step1Desc = "Tap 'Open App Info' -> Tap 3-dots (⋮) at top right -> Select 'Allow restricted settings' (Android 13/14).",
                step2Title = "2. Turn ON in Installed Apps",
                step2Desc = "Tap 'Open Accessibility' -> Select 'Installed apps' -> Tap 'Reel Narcotics Shield' -> Toggle ON.",
                step3Title = "3. Prevent One UI Deep Sleep",
                step3Desc = "Tap 'Battery' in App Info -> Select 'Unrestricted' so Samsung never kills the shield.",
                step3ButtonLabel = "DISABLE BATTERY SLEEP ➔"
            )
            DeviceBrand.TRANSSION -> OemGuidance(
                brand = brand,
                step1Title = "1. Unlock Restricted Settings",
                step1Desc = "Tap 'Open App Info' -> Tap 3-dots (⋮) at top right -> Select 'Allow restricted settings'.",
                step2Title = "2. Turn ON Shield in Accessibility",
                step2Desc = "Tap 'Open Accessibility' -> Go to 'Downloaded apps' -> Tap 'Reel Narcotics Shield' -> Toggle ON.",
                step3Title = "3. Enable Phone Master Auto-Start",
                step3Desc = "Keep Reel Narcotics protected against aggressive freeze and power boost killers.",
                step3ButtonLabel = "OPEN PHONE MASTER AUTOSTART ➔"
            )
            DeviceBrand.OPPO_REALME_ONEPLUS -> OemGuidance(
                brand = brand,
                step1Title = "1. Unlock Restricted Settings",
                step1Desc = "Tap 'Open App Info' -> Tap 3-dots (⋮) -> Select 'Allow restricted settings'.",
                step2Title = "2. Turn ON in Accessibility",
                step2Desc = "Tap 'Open Accessibility' -> Go to 'Downloaded apps' -> Turn 'Reel Narcotics Shield' ON.",
                step3Title = "3. Allow Auto-Launch",
                step3Desc = "Allow background auto-launch so the surgical shield stays armed.",
                step3ButtonLabel = "OPEN AUTO-LAUNCH SETTINGS ➔"
            )
            DeviceBrand.VIVO -> OemGuidance(
                brand = brand,
                step1Title = "1. Unlock Restricted Settings",
                step1Desc = "Tap 'Open App Info' -> Tap 3-dots (⋮) -> Select 'Allow restricted settings'.",
                step2Title = "2. Turn ON in Accessibility",
                step2Desc = "Tap 'Open Accessibility' -> Tap 'Installed services' -> Turn 'Reel Narcotics Shield' ON.",
                step3Title = "3. Background Startup Permission",
                step3Desc = "Allow high background power consumption so the shield is never terminated.",
                step3ButtonLabel = "OPEN VIVO BACKGROUND MANAGER ➔"
            )
            DeviceBrand.GENERIC -> OemGuidance(
                brand = brand,
                step1Title = "1. Unlock Restricted Settings (Android 13+)",
                step1Desc = "Tap 'Open App Info' -> Tap 3-dots (⋮) at top right -> Select 'Allow restricted settings'.",
                step2Title = "2. Turn ON Shield in Accessibility",
                step2Desc = "Tap 'Open Accessibility' -> Look for 'Reel Narcotics Shield' -> Toggle ON.",
                step3Title = "3. Disable Battery Optimization",
                step3Desc = "Allow unrestricted background running so the shield remains active during locks.",
                step3ButtonLabel = "DISABLE BATTERY OPTIMIZATION ➔"
            )
        }
    }
}
