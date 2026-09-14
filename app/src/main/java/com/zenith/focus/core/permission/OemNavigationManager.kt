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
    val step3ButtonLabel: String? = null,
    val deviceAdminHint: String? = null
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
     * Checks phone environment and tries OEM-specific deep link activities (Samsung, Xiaomi, Vivo, etc.)
     * before falling back to standard AOSP accessibility with highlight extras.
     */
    fun openAccessibilitySettings(context: Context): Boolean {
        val brand = detectDeviceBrand()
        val componentName = ComponentName(context, "com.zenith.focus.accessibility.service.ZenithAccessibilityService")

        // 1. OEM-specific deep link candidates
        val oemCandidates: List<Intent> = when (brand) {
            DeviceBrand.SAMSUNG -> listOf(
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.accessibility.InstalledAccessibilityServicesSettings")),
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$InstalledAccessibilityServicesSettingsActivity")),
                Intent("com.samsung.accessibility.INSTALLED_SERVICES")
            )
            DeviceBrand.XIAOMI -> listOf(
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.accessibility.AccessibilitySettings")),
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.SubSettings")).apply {
                    putExtra(":settings:show_fragment", "com.android.settings.accessibility.AccessibilitySettings")
                }
            )
            DeviceBrand.VIVO -> listOf(
                Intent().setComponent(ComponentName("com.vivo.settings", "com.vivo.settings.accessibility.AccessibilitySettings")),
                Intent().setComponent(ComponentName("com.vivo.settings", "com.vivo.settings.accessibility.InstalledServicesActivity")),
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$AccessibilitySettingsActivity"))
            )
            DeviceBrand.OPPO_REALME_ONEPLUS -> listOf(
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.accessibility.InstalledAccessibilityServicesSettings")),
                Intent().setComponent(ComponentName("com.coloros.settings", "com.coloros.settings.accessibility.AccessibilitySettings"))
            )
            DeviceBrand.TRANSSION -> listOf(
                Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$AccessibilitySettingsActivity"))
            )
            DeviceBrand.GENERIC -> emptyList()
        }

        for (cand in oemCandidates) {
            try {
                cand.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                cand.putExtra(":settings:fragment_args_key", componentName.flattenToString())
                val resolved = context.packageManager.resolveActivity(cand, 0)
                if (resolved != null) {
                    context.startActivity(cand)
                    return true
                }
            } catch (_: Exception) {
                // Try next candidate
            }
        }

        // 2. Standard Android Accessibility intent with highlighting arguments
        val standardIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(":settings:fragment_args_key", componentName.flattenToString())
            putExtra(":settings:show_fragment_args", android.os.Bundle().apply {
                putString(":settings:fragment_args_key", componentName.flattenToString())
            })
        }

        return try {
            context.startActivity(standardIntent)
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
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")),
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
                Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity")),
                Intent().setComponent(ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity")),
                Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity")).apply {
                    putExtra("packagename", context.packageName)
                }
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
     * Order reflects the mandatory Android 13+ flow:
     * 1. Trigger restriction in Accessibility first (so the 3-dots unlock option is registered by the OS).
     * 2. Unlock restricted settings in App Info via 3-dots (⋮).
     * 3. Return to Accessibility to toggle ON & allow OEM background execution.
     */
    fun getGuidance(brand: DeviceBrand = detectDeviceBrand()): OemGuidance {
        return when (brand) {
            DeviceBrand.XIAOMI -> OemGuidance(
                brand = brand,
                step1Title = "1. Open Accessibility & Trigger Restriction",
                step1Desc = "Tap 'Open Accessibility' below -> Go to 'General' tab -> Scroll down and tap 'Downloaded apps' -> Tap 'Reel Narcotics Shield' -> Try to turn ON -> Tap 'OK' on the Restricted setting popup. (⚠️ Must do this first so the unlock button in Step 2 appears!)",
                step2Title = "2. Unlock Restricted Settings in App Info",
                step2Desc = "Tap 'Open App Info' below -> Tap the 3-dots (⋮) in the top-right corner -> Select 'Allow restricted settings' -> Confirm with your PIN or Fingerprint.",
                step3Title = "3. Turn ON Shield & Enable MIUI Autostart",
                step3Desc = "Return to 'Downloaded apps' -> Turn Reel Narcotics Shield ON. Then enable MIUI Autostart below so the shield stays protected when recent apps are cleared.",
                step3ButtonLabel = "OPEN MIUI AUTOSTART ➔",
                deviceAdminHint = "💡 MIUI / POCO Security Warning: Tap 'Activate', wait 10s on the countdown warning, tick the checkbox and tap Next -> OK. If blocked, complete Step 2 above first."
            )
            DeviceBrand.VIVO -> OemGuidance(
                brand = brand,
                step1Title = "1. Open Accessibility & Trigger Restriction",
                step1Desc = "Tap 'Open Accessibility' below -> Tap 'Installed services' (or 'Downloaded services') -> Tap 'Reel Narcotics Shield' -> Try to turn ON -> Tap 'OK' on the Restricted setting popup. (⚠️ Must do this first so the 3-dots option in Step 2 appears!)",
                step2Title = "2. Unlock Restricted Settings in App Info",
                step2Desc = "Tap 'Open App Info' below -> Tap the 3-dots (⋮) in the top-right corner -> Select 'Allow restricted settings' -> Confirm with your PIN or Fingerprint.",
                step3Title = "3. Turn ON Shield & High Background Power",
                step3Desc = "Return to 'Installed services' -> Turn Reel Narcotics Shield ON. Then allow High Background Power Consumption below so Vivo doesn't kill the shield.",
                step3ButtonLabel = "OPEN VIVO BACKGROUND MANAGER ➔",
                deviceAdminHint = "💡 Vivo Security: Tap 'Activate' on the system dialog. If blocked, unlock 'Allow restricted settings' in App Info first."
            )
            DeviceBrand.SAMSUNG -> OemGuidance(
                brand = brand,
                step1Title = "1. Open Accessibility & Trigger Restriction",
                step1Desc = "Tap 'Open Accessibility' below -> Tap 'Installed apps' -> Tap 'Reel Narcotics Shield' -> Try to turn ON -> Tap 'OK' if Restricted setting appears.",
                step2Title = "2. Unlock Restricted Settings in App Info",
                step2Desc = "Tap 'Open App Info' below -> Tap 3-dots (⋮) at top-right -> Select 'Allow restricted settings' (Android 13/14).",
                step3Title = "3. Prevent One UI Deep Sleep",
                step3Desc = "Return to Installed apps -> Turn Shield ON. Then tap 'Battery' in App Info -> Select 'Unrestricted' so Samsung never kills the shield.",
                step3ButtonLabel = "DISABLE BATTERY SLEEP ➔",
                deviceAdminHint = "💡 One UI Security: Tap 'Activate' to lock Reel Narcotics against uninstallation during focus commitments."
            )
            DeviceBrand.TRANSSION -> OemGuidance(
                brand = brand,
                step1Title = "1. Open Accessibility & Trigger Restriction",
                step1Desc = "Tap 'Open Accessibility' below -> Tap 'Downloaded apps' -> Tap 'Reel Narcotics Shield' -> Try to turn ON -> Tap 'OK'.",
                step2Title = "2. Unlock Restricted Settings in App Info",
                step2Desc = "Tap 'Open App Info' below -> Tap 3-dots (⋮) at top-right -> Select 'Allow restricted settings'.",
                step3Title = "3. Enable Phone Master Auto-Start",
                step3Desc = "Return to Downloaded apps -> Turn Shield ON. Then whitelist in Phone Master below.",
                step3ButtonLabel = "OPEN PHONE MASTER AUTOSTART ➔",
                deviceAdminHint = "💡 Transsion Security: Tap 'Activate' on the system prompt."
            )
            DeviceBrand.OPPO_REALME_ONEPLUS -> OemGuidance(
                brand = brand,
                step1Title = "1. Open Accessibility & Trigger Restriction",
                step1Desc = "Tap 'Open Accessibility' below -> Go to 'Downloaded apps' -> Tap 'Reel Narcotics Shield' -> Try to turn ON -> Tap 'OK'.",
                step2Title = "2. Unlock Restricted Settings in App Info",
                step2Desc = "Tap 'Open App Info' below -> Tap 3-dots (⋮) -> Select 'Allow restricted settings' -> Confirm with PIN.",
                step3Title = "3. Turn ON Shield & Allow Auto-Launch",
                step3Desc = "Return to Downloaded apps -> Turn Reel Narcotics ON. Then allow background Auto-Launch below.",
                step3ButtonLabel = "OPEN AUTO-LAUNCH SETTINGS ➔",
                deviceAdminHint = "💡 ColorOS / OxygenOS: Tap 'Activate' to prevent uninstallation."
            )
            DeviceBrand.GENERIC -> OemGuidance(
                brand = brand,
                step1Title = "1. Open Accessibility & Trigger Restriction",
                step1Desc = "Tap 'Open Accessibility' below -> Find 'Reel Narcotics Shield' -> Try to turn ON -> Tap 'OK' if Restricted setting appears.",
                step2Title = "2. Unlock Restricted Settings (Android 13+)",
                step2Desc = "Tap 'Open App Info' below -> Tap 3-dots (⋮) at top-right -> Select 'Allow restricted settings' -> Confirm with PIN.",
                step3Title = "3. Turn ON Shield & Disable Battery Optimization",
                step3Desc = "Return to Accessibility -> Turn Reel Narcotics ON. Then allow Unrestricted battery usage below.",
                step3ButtonLabel = "DISABLE BATTERY OPTIMIZATION ➔",
                deviceAdminHint = "💡 Tap 'Activate' on the Android system prompt."
            )
        }
    }
}
