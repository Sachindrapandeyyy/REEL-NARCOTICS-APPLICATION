package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext

data class TamperDetectionResult(
    val isTamperAttempt: Boolean,
    val reason: String = "",
    val targetPackage: String = ""
)

object TamperDetectionEngine {

    private val UNINSTALL_PACKAGES = setOf(
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.android.settings",
        "com.android.vending"
    )

    private val TARGET_APP_IDENTIFIERS = setOf(
        "zenith",
        "reel narcotics",
        "narcotics",
        "com.zenith.focus"
    )

    fun evaluate(context: ScreenContext): TamperDetectionResult {
        val pkg = context.packageName.lowercase()
        if (pkg !in UNINSTALL_PACKAGES) {
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

        // Case 2: Settings - App Info, Device Admin deactivation, or Accessibility disabling
        if (pkg == "com.android.settings") {
            val isDeviceAdminScreen = context.className.contains("DeviceAdmin", ignoreCase = true) ||
                    allTexts.any { it.contains("device admin") || it.contains("device administrator") }

            val hasDestructiveAction = allTexts.any { text ->
                text.contains("uninstall") ||
                text.contains("force stop") ||
                text.contains("deactivate") ||
                text.contains("clear data") ||
                text.contains("clear storage") ||
                text.contains("disable") ||
                text.contains("stop app") ||
                text.contains("turn off") ||
                text.contains("remove device admin")
            } || allTokens.any { token ->
                token in setOf("uninstall", "deactivate", "forcestop", "disable")
            }

            // In Device Admin settings, if Reel Narcotics is listed/opened/toggled:
            if (isDeviceAdminScreen && mentionsTargetApp) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "Device Administrator tamper/deactivation attempt intercepted",
                    targetPackage = pkg
                )
            }

            val isAppInfoOrAdminScreen = context.className.contains("InstalledAppDetails", ignoreCase = true) ||
                    context.className.contains("AccessibilitySettings", ignoreCase = true) ||
                    hasDestructiveAction

            if (isAppInfoOrAdminScreen && mentionsTargetApp) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "Settings tamper/deactivation attempt intercepted",
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

