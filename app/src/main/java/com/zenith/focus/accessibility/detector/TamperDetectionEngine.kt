package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext

data class TamperDetectionResult(
    val isTamperAttempt: Boolean,
    val reason: String = "",
    val targetPackage: String = ""
)

object TamperDetectionEngine {

    private val BASE_TAMPER_PACKAGES = setOf(
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.android.settings",
        "com.google.android.settings",
        "com.android.vending",
        "android"
    )

    private val TARGET_APP_IDENTIFIERS = setOf(
        "zenith",
        "reel narcotics",
        "narcotics",
        "com.zenith.focus"
    )

    private fun isPotentialTamperPackage(pkg: String): Boolean {
        return pkg in BASE_TAMPER_PACKAGES ||
                pkg.contains("packageinstaller") ||
                pkg.contains("securitycenter") ||
                pkg.contains("safecenter") ||
                pkg.contains("permission") ||
                pkg.contains("phonemaster") ||
                pkg.contains("iqoo.secure") ||
                pkg.contains("cleanmaster") ||
                pkg.contains("settings") ||
                pkg.contains("accessibility") ||
                pkg == "com.android.vending" ||
                pkg == "android"
    }

    fun evaluate(context: ScreenContext): TamperDetectionResult {
        val pkg = context.packageName.lowercase()
        if (!isPotentialTamperPackage(pkg)) {
            return TamperDetectionResult(isTamperAttempt = false)
        }

        val allTexts = context.visibleTexts.map { it.lowercase() }
        val allTokens = context.allNormalizedTokens

        val mentionsTargetApp = TARGET_APP_IDENTIFIERS.any { id ->
            allTexts.any { it.contains(id) } ||
            allTokens.contains(id) ||
            context.viewIds.any { it.contains(id, ignoreCase = true) }
        }

        // Case 1: Package installer attempting to delete/uninstall Reel Narcotics / Zenith
        if (pkg.contains("packageinstaller")) {
            val isUninstallPrompt = allTexts.any { it.contains("uninstall") || it.contains("delete") || it.contains("remove") } ||
                    allTokens.any { it in setOf("uninstall", "delete", "remove") }
            if (mentionsTargetApp && isUninstallPrompt) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "Package uninstallation attempt intercepted",
                    targetPackage = pkg
                )
            }
        }

        // Case 2: Settings, Security Center, or System Framework ("android")
        val isSystemOrSettings = pkg == "android" ||
                pkg.contains("settings") ||
                pkg.contains("securitycenter") ||
                pkg.contains("safecenter") ||
                pkg.contains("permission") ||
                pkg.contains("phonemaster") ||
                pkg.contains("iqoo.secure") ||
                pkg.contains("accessibility")

        if (isSystemOrSettings && mentionsTargetApp) {
            // Explicit stop / turn off prompt (dialog or text)
            val hasExplicitStopPrompt = allTexts.any { text ->
                text.contains("stop reel narcotics") ||
                text.contains("turn off reel narcotics") ||
                text.contains("stop zenith") ||
                text.contains("turn off zenith") ||
                text.contains("stop service") ||
                text.contains("turn off service")
            }
            if (hasExplicitStopPrompt) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "Accessibility service stop or turn off prompt intercepted",
                    targetPackage = pkg
                )
            }

            // Check for deactivation / destructive text actions
            val hasStopOrTurnOffAction = allTexts.any { text ->
                text.contains("stop app") ||
                text.contains("force stop") ||
                text.contains("deactivate") ||
                text.contains("disable") ||
                text.contains("turn off") ||
                text.contains("clear data") ||
                text.contains("clear storage") ||
                text.contains("remove device admin") ||
                text.trim() == "stop"
            } || allTokens.any { token ->
                token in setOf("deactivate", "forcestop", "disable")
            }

            // System confirmation dialog: Cancel alongside Stop / Turn off / OK / Disable / Determine
            val hasCancelButton = allTexts.any { it.trim() == "cancel" || it.contains("cancel") }
            val hasStopButton = allTexts.any {
                it.trim() == "stop" ||
                it.trim() == "turn off" ||
                it.trim() == "ok" ||
                it.trim() == "disable" ||
                it.trim() == "determine"
            }

            if (hasCancelButton && (hasStopButton || hasStopOrTurnOffAction)) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "Accessibility service or app stop confirmation dialog intercepted",
                    targetPackage = pkg
                )
            }

            // Accessibility service detail screen: "Use Reel Narcotics Shield", "Use service", switch widgets, capabilities
            val isAccessibilityServiceScreen = allTexts.any { text ->
                text.contains("use reel narcotics") ||
                text.contains("use zenith") ||
                text.contains("use service") ||
                text.contains("accessibility shortcut") ||
                text.contains("shield shortcut") ||
                text.contains("observe your actions") ||
                text.contains("retrieve window content") ||
                text.contains("view and control screen") ||
                text.contains("interact with your apps") ||
                text.contains("full control of your device")
            } || context.className.contains("ToggleAccessibility", ignoreCase = true) ||
                 context.className.contains("AccessibilityDetails", ignoreCase = true) ||
                 context.className.contains("ToggleFeature", ignoreCase = true) ||
                 context.className.contains("AccessibilityServiceWarning", ignoreCase = true) ||
                 context.viewIds.any { id ->
                     id.contains("main_switch_bar", ignoreCase = true) ||
                     id.contains("switch_widget", ignoreCase = true) ||
                     id.contains("switch_bar", ignoreCase = true) ||
                     id.contains("switch_root", ignoreCase = true)
                 }

            if (isAccessibilityServiceScreen) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "Accessibility service toggle/detail screen tamper intercepted",
                    targetPackage = pkg
                )
            }

            // Device Admin screen deactivation
            val isDeviceAdminScreen = context.className.contains("DeviceAdmin", ignoreCase = true) ||
                    allTexts.any { it.contains("device admin") || it.contains("device administrator") }
            val isDeactivationAttempt = allTexts.any {
                it.contains("deactivate") ||
                it.contains("remove device admin") ||
                it.contains("turn off")
            } || allTokens.any { it in setOf("deactivate", "remove", "disable") }

            if (isDeviceAdminScreen && (isDeactivationAttempt || hasStopOrTurnOffAction)) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "Device Administrator tamper/deactivation attempt intercepted",
                    targetPackage = pkg
                )
            }

            // App Info screen: Force Stop, Clear Data, Uninstall
            val isAppInfoScreen = context.className.contains("InstalledAppDetails", ignoreCase = true) ||
                    context.className.contains("AppInfo", ignoreCase = true) ||
                    context.className.contains("ApplicationsDetailsActivity", ignoreCase = true) ||
                    context.className.contains("AppControl", ignoreCase = true) ||
                    context.className.contains("ManageApp", ignoreCase = true) ||
                    allTexts.any { it.contains("force stop") || it.contains("storage & cache") || it.contains("app info") }

            if (isAppInfoScreen && (hasStopOrTurnOffAction || allTexts.any { it.contains("force stop") || it.contains("clear data") || it.contains("uninstall") })) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "App Info settings tamper/deactivation attempt intercepted",
                    targetPackage = pkg
                )
            }
        }

        // Case 3: Google Play Store uninstall button for Reel Narcotics / Zenith Focus
        if (pkg == "com.android.vending") {
            if (mentionsTargetApp) {
                val hasUninstall = allTexts.any { it.trim().equals("uninstall", ignoreCase = true) } ||
                        allTokens.contains("uninstall")
                if (hasUninstall) {
                    return TamperDetectionResult(
                        isTamperAttempt = true,
                        reason = "Play Store uninstallation attempt intercepted",
                        targetPackage = pkg
                    )
                }
            }
        }

        return TamperDetectionResult(isTamperAttempt = false)
    }
}

