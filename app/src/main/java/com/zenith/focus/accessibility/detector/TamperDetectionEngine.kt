package com.zenith.focus.accessibility.detector

import android.view.accessibility.AccessibilityEvent
import com.zenith.focus.accessibility.analyzer.ScreenContext
import java.util.Locale

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

    private val OEM_FREEZER_AND_POWER_PACKAGES = setOf(
        // Transsion (Infinix, Tecno, itel)
        "com.transsion.phonemaster",
        "com.transsion.xoslauncher",
        "com.transsion.hilauncher",
        "com.transsion.neofreezer",
        "com.transsion.freezer",
        "com.infinix.freezer",
        "com.tecno.freezer",
        "com.transsion.magicholder",
        "com.transsion.appfreeze",
        "com.transsion.desktop",
        // Samsung
        "com.samsung.android.lool",
        "com.samsung.android.sm",
        "com.samsung.android.sm_cn",
        "com.sec.android.app.launcher",
        // Xiaomi / POCO / Redmi
        "com.miui.powerkeeper",
        "com.miui.securitycenter",
        "com.miui.cleanmaster",
        "com.miui.home",
        // Oppo / OnePlus / Realme
        "com.oplus.battery",
        "com.coloros.safecenter",
        "com.oplus.safecenter",
        "com.coloros.oppoguardelf",
        "com.oplus.appfreezer",
        "com.coloros.battery",
        "com.oplus.deepthinker",
        // Vivo / iQOO
        "com.iqoo.secure",
        "com.vivo.permissionmanager",
        "com.vivo.abe",
        "com.vivo.hybrid",
        "com.bbk.launcher2",
        // Huawei / Honor
        "com.huawei.systemmanager",
        "com.hihonor.systemmanager",
        // Third-party app freezers & isolation utilities
        "com.catchingnow.icebox",
        "com.aistra.hail",
        "com.real.clearprocesses",
        "com.sunnychung.applicationfreezer",
        "catch_.me_.if_.you_.can_",
        "com.iamnotnd.freeze",
        "moe.shizuku.privileged.api",
        "com.rosan.dhizuku",
        "com.draco.island"
    )

    private val FREEZE_SUSPEND_KEYWORDS = setOf(
        "freezer",
        "freeze",
        "frozen",
        "deep sleep",
        "deep sleeping apps",
        "sleeping apps",
        "put to sleep",
        "never sleeping apps",
        "auto freeze",
        "quick freeze",
        "hibernate",
        "restrict background",
        "restrict background activity",
        "restricted",
        "pause app activity",
        "pause app activity if unused",
        "background usage limits",
        "background restriction",
        "app clone",
        "private space",
        "second space",
        "ice box",
        "freeze apps",
        "to freezer",
        "send to freezer",
        "add to freezer",
        "फ्रीजर",
        "फ्रीज"
    )

    private val TARGET_APP_IDENTIFIERS = setOf(
        "zenith",
        "reel narcotics",
        "narcotics",
        "reelnarcotics",
        "com.zenith.focus"
    )

    fun isFreezerPackage(pkg: String): Boolean {
        val lower = pkg.lowercase(Locale.US)
        return lower in OEM_FREEZER_AND_POWER_PACKAGES ||
                lower.contains("freezer") ||
                lower.contains("icebox") ||
                lower.contains("appfreezer") ||
                lower.contains("powerkeeper") ||
                lower.contains("hail") ||
                lower.contains("phonemaster")
    }

    fun hasFreezerKeyword(text: String): Boolean {
        val lower = text.lowercase(Locale.US)
        return FREEZE_SUSPEND_KEYWORDS.any { lower.contains(it) }
    }

    fun hasFreezerKeyword(texts: List<String>): Boolean {
        return texts.any { hasFreezerKeyword(it) }
    }

    fun isFreezeActionText(text: String): Boolean {
        val lower = text.lowercase(Locale.US).trim()
        return lower == "freeze" || lower == "to freezer" ||
                lower == "send to freezer" || lower == "add to freezer" || lower == "freeze apps" ||
                lower == "deep sleep" || lower == "put to sleep" || lower == "quick freeze" ||
                lower == "hibernate" || lower == "restrict" ||
                (lower.contains("फ्रीज") && !lower.contains("फ्रीजर"))
    }

    fun isUninstallActionText(text: String): Boolean {
        val lower = text.lowercase(Locale.US).trim()
        return lower == "uninstall" || lower == "delete" || lower == "remove" ||
                lower == "disable" || lower == "force stop" || lower == "clear data"
    }

    fun mentionsTargetApp(text: String): Boolean {
        val lower = text.lowercase(Locale.US)
        return TARGET_APP_IDENTIFIERS.any { id -> lower.contains(id) }
    }

    fun isDirectFreezerTamper(
        lowerPkg: String,
        event: AccessibilityEvent,
        eventText: String,
        lastTargetAppLongPressTime: Long,
        now: Long
    ): Boolean {
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_CLICKED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_LONG_CLICKED
        ) {
            return false
        }

        val isTargetMentioned = mentionsTargetApp(eventText)
        val isFreezerPkg = isFreezerPackage(lowerPkg)

        // 1. User clicked directly on Reel Narcotics inside a Freezer / PhoneMaster / IceBox picker/list
        // (Never trigger on launcher desktop where clicking Reel Narcotics launches the app)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED &&
            isFreezerPkg && !lowerPkg.contains("launcher") && isTargetMentioned
        ) {
            return true
        }

        // 2. User clicked "Freeze", "To Freezer", "Send to Freezer", or "Add to Freezer"
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED &&
            isFreezeActionText(eventText)
        ) {
            // Either the click text itself mentions Reel Narcotics, OR Reel Narcotics was recently long-pressed
            if (isTargetMentioned || (now - lastTargetAppLongPressTime < 10000L)) {
                return true
            }
        }

        return false
    }

    fun isFreezerActionOrFolder(lowerPkg: String, event: AccessibilityEvent, eventText: String): Boolean {
        val className = event.className?.toString()?.lowercase(Locale.US) ?: ""

        // 1. Tapped "Freezer" icon/folder on launcher or PhoneMaster
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val trimmed = eventText.trim().lowercase(Locale.US)
            val isFreezerClick = trimmed == "freezer" || trimmed == "फ्रीजर" ||
                    trimmed == "neofreezer" || trimmed == "icebox" || trimmed == "hail" ||
                    trimmed == "to freezer" || trimmed == "send to freezer" ||
                    trimmed == "add to freezer" || trimmed == "freeze apps"
            if (isFreezerClick && (lowerPkg.contains("launcher") || isFreezerPackage(lowerPkg))) {
                return true
            }
        }

        // 2. Opened a dedicated Freezer activity / window or Add Apps picker
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val isFreezerActivity = className.contains("freezer") ||
                    className.contains("neofreezer") ||
                    className.contains("deepsleep") ||
                    className.contains("appfreeze")
            if (isFreezerActivity && !className.contains("workspace") && !className.contains("launcherrootview")) {
                return true
            }

            val isFolderOrPicker = (className.contains("folder") && !className.contains("icon")) ||
                    className.contains("selectapps") || className.contains("addapps") ||
                    className.contains("freezeapps")
            val isFreezerFolderWindow = isFolderOrPicker &&
                    (eventText.contains("freezer") || eventText.contains("फ्रीजर") || eventText.contains("freeze"))
            if (isFreezerFolderWindow) {
                return true
            }

            // Dedicated standalone freezer packages
            if (isFreezerPackage(lowerPkg) && !lowerPkg.contains("launcher") && !lowerPkg.contains("phonemaster")) {
                return true
            }
        }

        // 3. User inside PhoneMaster viewing or interacting with Freezer
        if (lowerPkg.contains("phonemaster") && (eventText.contains("freezer") || className.contains("freezer"))) {
            return true
        }

        // 4. Clicked inside an open Freezer folder or Freezer activity
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED &&
            (className.contains("freezer") || className.contains("neofreezer") ||
             ((className.contains("folder") && !className.contains("icon")) && (eventText.contains("freezer") || eventText.contains("फ्रीजर"))) ||
             eventText.contains("add to freezer") || eventText.contains("freeze apps"))
        ) {
            return true
        }

        return false
    }

    fun isFreezerScreenContext(context: ScreenContext): Boolean {
        val pkg = context.packageName.lowercase(Locale.US)
        val className = context.className.lowercase(Locale.US)
        val allTexts = context.visibleTexts.map { it.lowercase(Locale.US) }

        // Dedicated third-party or OEM freezer packages (IceBox, Hail, NeoFreezer, etc.)
        if (pkg.contains("icebox") || pkg.contains("hail") || pkg.contains("neofreezer") ||
            pkg == "com.transsion.freezer" || pkg == "com.infinix.freezer" || pkg == "com.tecno.freezer"
        ) {
            return true
        }

        // Dedicated freezer activities (excluding launcher desktop activities)
        if (!pkg.contains("launcher") && (
            context.hasClassName("freezer") || context.hasClassName("neofreezer") ||
            context.hasClassName("deepsleep") || context.hasClassName("appfreezer")
        )) {
            return true
        }

        // Phone Master Freezer section
        if (pkg.contains("phonemaster") && (hasFreezerKeyword(allTexts) || context.hasClassName("freezer"))) {
            return true
        }

        // Inside launcher: An opened Freezer folder, Freezer drawer, or Add to Freezer picker
        if (pkg.contains("launcher")) {
            val hasFreezerHeader = allTexts.any { it.trim().equals("freezer", ignoreCase = true) || it.trim().equals("फ्रीजर", ignoreCase = true) }
            val hasExplicitFreezeAction = allTexts.any {
                val t = it.trim().lowercase(Locale.US)
                t == "to freezer" || t == "send to freezer" || t == "add to freezer" || t == "freeze apps" || t == "frozen apps"
            }
            // Open folder container (not desktop icon FolderIcon / folder_icon)
            val isFolder = (context.hasClassName("folder") && !context.hasClassName("foldericon")) ||
                    (className.contains("folder") && !className.contains("icon")) ||
                    context.classNames.any {
                        val l = it.lowercase(Locale.US)
                        l.contains("folder") && !l.contains("icon")
                    } ||
                    context.hasAnyViewId("folder_content", "folder_paged_view", "folder_grid")

            val hasAddButton = allTexts.any {
                val t = it.trim().lowercase(Locale.US)
                t == "+" || t == "add" || t == "add apps" || t == "जोड़ें"
            }

            // Add to Freezer app picker inside launcher
            val isAppPicker = allTexts.any {
                val t = it.trim().lowercase(Locale.US)
                t == "add to freezer" || t == "freeze apps" ||
                (hasFreezerHeader && (t == "select apps" || t == "choose apps" || t == "add apps"))
            } || context.hasClassName("selectapps") || context.hasClassName("addapps") ||
                 className.contains("selectapps") || className.contains("addapps") ||
                 context.hasAnyViewId("select_apps", "freezer_app_list")

            if (isAppPicker) {
                return true
            }

            // Exclude normal desktop workspace from being classified as a freezer folder
            val isDesktopWorkspace = className.contains("workspace") || className.contains("celllayout") ||
                    context.hasClassName("workspace")

            // Desktop workspace without both open folder and add button is NEVER a freezer screen
            if (isDesktopWorkspace && (!isFolder || !hasAddButton)) {
                return false
            }

            // An open Freezer folder has folder in class/viewIds + "Freezer" header/title + Add button
            if (isFolder && hasFreezerHeader && hasAddButton) {
                return true
            }

            // Or explicit freeze action buttons inside launcher (not regular desktop)
            if (hasExplicitFreezeAction && !isDesktopWorkspace) {
                return true
            }

            // Standalone Freezer activity inside launcher
            if ((className.contains("freezeractivity") || className.contains("neofreezeractivity")) &&
                !className.contains("workspace")) {
                return true
            }

            return false
        }

        // Samsung Deep Sleeping apps screen
        if ((pkg.contains("samsung") || pkg.contains("lool") || pkg.contains("sm")) &&
            allTexts.any { it.contains("deep sleep") || it.contains("sleeping apps") }
        ) {
            return true
        }

        // Xiaomi MIUI Background Restrict screen
        if ((pkg.contains("powerkeeper") || pkg.contains("miui")) &&
            allTexts.any { it.contains("restrict background activity") || it.contains("restrict background") }
        ) {
            return true
        }

        return false
    }

    private fun isPotentialTamperPackage(pkg: String): Boolean {
        return pkg in BASE_TAMPER_PACKAGES ||
                pkg in OEM_FREEZER_AND_POWER_PACKAGES ||
                pkg.contains("packageinstaller") ||
                pkg.contains("securitycenter") ||
                pkg.contains("safecenter") ||
                pkg.contains("permission") ||
                pkg.contains("phonemaster") ||
                pkg.contains("iqoo.secure") ||
                pkg.contains("cleanmaster") ||
                pkg.contains("freezer") ||
                pkg.contains("powerkeeper") ||
                pkg.contains("icebox") ||
                pkg.contains("appfreezer") ||
                pkg.contains("settings") ||
                pkg.contains("accessibility") ||
                pkg.contains("launcher") ||
                pkg == "com.android.vending" ||
                pkg == "android"
    }

    fun evaluate(context: ScreenContext): TamperDetectionResult {
        val pkg = context.packageName.lowercase(Locale.US)
        if (!isPotentialTamperPackage(pkg)) {
            return TamperDetectionResult(isTamperAttempt = false)
        }

        val allTexts = context.visibleTexts.map { it.lowercase(Locale.US) }
        val allTokens = context.allNormalizedTokens

        val mentionsTargetApp = TARGET_APP_IDENTIFIERS.any { id ->
            allTexts.any { it.contains(id) } ||
            allTokens.contains(id) ||
            context.viewIds.any { it.contains(id, ignoreCase = true) } ||
            context.contentDescriptions.any { it.contains(id, ignoreCase = true) }
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

        // Case 4: OEM & Third-Party App Freezer, Deep Sleep, or Background Restrict Interception
        val isFreezerOrPowerPackage = isFreezerPackage(pkg) || pkg.contains("launcher")

        val hasFreezerKeyword = hasFreezerKeyword(allTexts) ||
                allTokens.any { it in setOf("freezer", "freeze", "frozen", "deepsleep", "hibernate") } ||
                context.contentDescriptions.any { desc -> hasFreezerKeyword(desc.lowercase(Locale.US)) }

        if (mentionsTargetApp && (isFreezerOrPowerPackage || hasFreezerKeyword)) {
            val isFreezerScreen = isFreezerScreenContext(context) || hasFreezerKeyword ||
                    context.className.contains("Freezer", ignoreCase = true) ||
                    context.className.contains("Sleep", ignoreCase = true) ||
                    context.className.contains("PowerSaver", ignoreCase = true) ||
                    context.className.contains("Restrict", ignoreCase = true) ||
                    context.className.contains("Hibernate", ignoreCase = true) ||
                    pkg.contains("freezer") ||
                    pkg.contains("icebox") ||
                    pkg.contains("phonemaster")

            if (isFreezerScreen) {
                // If in launcher, distinguish between regular desktop workspace and actual freezer screen/dialog/popup
                if (pkg.contains("launcher")) {
                    val isPopupOrMenu = context.hasClassName("shortcut") ||
                            context.hasClassName("popup") ||
                            context.hasClassName("menu") ||
                            context.hasClassName("bubble") ||
                            context.hasAnyViewId("popup", "shortcut", "menu", "bubble")

                    val hasExplicitFreezeOption = allTexts.any {
                        val t = it.trim().lowercase(Locale.US)
                        t == "freeze" || t == "to freezer" || t == "send to freezer" ||
                        t == "add to freezer" || t == "freeze apps" || (t.contains("फ्रीज") && !t.contains("फ्रीजर"))
                    }

                    // If popup/shortcut menu on Reel Narcotics contains "Freeze"
                    if (isPopupOrMenu && hasExplicitFreezeOption) {
                        return TamperDetectionResult(
                            isTamperAttempt = true,
                            reason = "Launcher Freezer shortcut menu tamper attempt intercepted ($pkg)",
                            targetPackage = pkg
                        )
                    }

                    // If screen is "Add to Freezer" app picker targeting Reel Narcotics
                    val isAddPicker = allTexts.any {
                        val t = it.trim().lowercase(Locale.US)
                        t == "add to freezer" || t == "freeze apps" || t == "to freezer" ||
                        t == "select apps" || t == "choose apps" || t == "add apps"
                    } || context.hasClassName("selectapps") || context.hasClassName("addapps") ||
                         context.hasAnyViewId("select_apps", "app_list", "freezer_app_list")

                    if (isAddPicker) {
                        return TamperDetectionResult(
                            isTamperAttempt = true,
                            reason = "Launcher Add to Freezer app picker targeting Reel Narcotics intercepted ($pkg)",
                            targetPackage = pkg
                        )
                    }

                    // If class name explicitly identifies a Freezer activity or screen inside launcher
                    val isFreezerActivity = (context.className.contains("freezeractivity", ignoreCase = true) ||
                            context.className.contains("neofreezeractivity", ignoreCase = true)) &&
                            !context.className.contains("workspace", ignoreCase = true)

                    if (isFreezerActivity) {
                        return TamperDetectionResult(
                            isTamperAttempt = true,
                            reason = "Launcher Freezer screen tamper attempt intercepted ($pkg)",
                            targetPackage = pkg
                        )
                    }

                    // If Reel Narcotics is inside an open Freezer folder or drawer
                    if (isFreezerScreenContext(context)) {
                        return TamperDetectionResult(
                            isTamperAttempt = true,
                            reason = "Launcher Freezer folder tamper attempt intercepted ($pkg)",
                            targetPackage = pkg
                        )
                    }

                    // Otherwise, launcher desktop workspace is completely normal and allowed
                    return TamperDetectionResult(isTamperAttempt = false)
                } else {
                    return TamperDetectionResult(
                        isTamperAttempt = true,
                        reason = "OEM/System App Freezer or Deep Sleep tamper attempt intercepted ($pkg)",
                        targetPackage = pkg
                    )
                }
            }
        }

        // Case 5: Android Settings Battery Optimization / "Restricted" Background Interception
        if (pkg.contains("settings") && mentionsTargetApp) {
            val isBatteryScreen = allTexts.any {
                it.contains("app battery usage") ||
                it.contains("battery optimization") ||
                it.contains("background restriction") ||
                it.contains("manage battery usage") ||
                it.contains("optimize battery usage")
            } || context.className.contains("Battery", ignoreCase = true)

            val hasRestrictAction = allTexts.any {
                it.contains("restricted") ||
                it.contains("restrict background") ||
                it.contains("pause app activity if unused")
            } || allTokens.contains("restricted")

            if (isBatteryScreen && hasRestrictAction) {
                return TamperDetectionResult(
                    isTamperAttempt = true,
                    reason = "Settings background battery restriction tamper attempt intercepted",
                    targetPackage = pkg
                )
            }
        }

        return TamperDetectionResult(isTamperAttempt = false)
    }
}
