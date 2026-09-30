package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TamperDetectionTest {

    @Test
    fun testPackageInstallerUninstallationDetected() {
        val context = ScreenContext(
            packageName = "com.google.android.packageinstaller",
            className = "com.android.packageinstaller.UninstallerActivity",
            viewIds = setOf("ok_button", "cancel_button"),
            visibleTexts = listOf("Do you want to uninstall Zenith Focus?", "OK", "Cancel"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("do", "you", "want", "to", "uninstall", "zenith", "focus", "ok", "cancel"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testSettingsAppInfoZenithFocusDetected() {
        val context = ScreenContext(
            packageName = "com.android.settings",
            className = "com.android.settings.applications.InstalledAppDetails",
            viewIds = setOf("left_button", "right_button"),
            visibleTexts = listOf("Zenith Focus", "Uninstall", "Force stop", "Storage & cache"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("zenith", "focus", "uninstall", "force", "stop", "storage", "cache"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testSettingsDeviceAdminDeactivationDetected() {
        val context = ScreenContext(
            packageName = "com.android.settings",
            className = "com.android.settings.DeviceAdminAdd",
            viewIds = setOf("action_button"),
            visibleTexts = listOf("Zenith Focus", "Deactivate this device admin app"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("zenith", "focus", "deactivate", "this", "device", "admin", "app"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testSettingsDeviceAdminActivationAllowed() {
        val context = ScreenContext(
            packageName = "com.android.settings",
            className = "com.android.settings.DeviceAdminAdd",
            viewIds = setOf("action_button"),
            visibleTexts = listOf("Reel Narcotics", "Activate this device admin app", "Cancel"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("reel", "narcotics", "activate", "this", "device", "admin", "app", "cancel"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertFalse(result.isTamperAttempt)
    }

    @Test
    fun testSettingsOtherAppNotDetected() {
        val context = ScreenContext(
            packageName = "com.android.settings",
            className = "com.android.settings.applications.InstalledAppDetails",
            viewIds = setOf("left_button"),
            visibleTexts = listOf("Calculator", "Force stop", "Storage & cache"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("calculator", "force", "stop", "storage", "cache"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertFalse(result.isTamperAttempt)
    }

    @Test
    fun testPlayStoreUninstallZenithFocusDetected() {
        val context = ScreenContext(
            packageName = "com.android.vending",
            className = "com.google.android.finsky.activities.MainActivity",
            viewIds = setOf("uninstall_button"),
            visibleTexts = listOf("Zenith Focus", "Uninstall", "Open"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("zenith", "focus", "uninstall", "open"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testSamsungPackageInstallerUninstallationDetected() {
        val context = ScreenContext(
            packageName = "com.samsung.android.packageinstaller",
            className = "com.android.packageinstaller.UninstallerActivity",
            viewIds = setOf("ok_button", "cancel_button"),
            visibleTexts = listOf("Do you want to uninstall Reel Narcotics?", "OK", "Cancel"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("do", "you", "want", "to", "uninstall", "reel", "narcotics", "ok", "cancel"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testXiaomiSecurityCenterManageAppForceStopDetected() {
        val context = ScreenContext(
            packageName = "com.miui.securitycenter",
            className = "com.miui.appmanager.ApplicationsDetailsActivity",
            viewIds = setOf("am_app_stop", "am_app_uninstall"),
            visibleTexts = listOf("Reel Narcotics", "Force stop", "Uninstall", "Clear data"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("reel", "narcotics", "force", "stop", "uninstall", "clear", "data"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testAccessibilityServiceScreenTamperDetected() {
        val context = ScreenContext(
            packageName = "com.android.settings",
            className = "com.android.settings.SubSettings",
            viewIds = setOf("main_switch_bar", "switch_text"),
            visibleTexts = listOf(
                "Reel Narcotics Shield",
                "Use Reel Narcotics Shield",
                "Reel Narcotics monitors reel playback to protect your focus."
            ),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("reel", "narcotics", "shield", "use", "monitors", "playback", "focus"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testAccessibilityStopConfirmationDialogDetected() {
        val context = ScreenContext(
            packageName = "android",
            className = "android.app.AlertDialog",
            viewIds = setOf("alertTitle", "button1", "button2"),
            visibleTexts = listOf(
                "Stop Reel Narcotics Shield?",
                "Stopping Reel Narcotics Shield? The service will no longer be able to block reels.",
                "Cancel",
                "Stop"
            ),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("stop", "reel", "narcotics", "shield", "cancel"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testTurnOffConfirmationDialogDetected() {
        val context = ScreenContext(
            packageName = "com.android.settings",
            className = "android.app.AlertDialog",
            viewIds = setOf("alertTitle", "button1", "button2"),
            visibleTexts = listOf(
                "Turn off Reel Narcotics Shield?",
                "Cancel",
                "Turn off"
            ),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("turn", "off", "reel", "narcotics", "shield", "cancel"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue(result.isTamperAttempt)
    }

    @Test
    fun testAccessibilityListBrowsingAllowed() {
        val context = ScreenContext(
            packageName = "com.android.settings",
            className = "com.android.settings.accessibility.AccessibilitySettings",
            viewIds = setOf("recycler_view", "title", "summary"),
            visibleTexts = listOf("Accessibility", "Downloaded apps", "TalkBack", "Reel Narcotics Shield", "On"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("accessibility", "downloaded", "apps", "talkback", "reel", "narcotics", "shield", "on"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertFalse(result.isTamperAttempt)
    }

    // --- OEM & THIRD-PARTY FREEZER / DEEP SLEEP TESTS ---

    @Test
    fun testInfinixFreezerTamperIntercepted() {
        val context = ScreenContext(
            packageName = "com.transsion.phonemaster",
            className = "com.transsion.phonemaster.freezer.FreezerActivity",
            viewIds = setOf("freezer_add_btn", "app_title"),
            visibleTexts = listOf("Freezer", "Add to Freezer", "Reel Narcotics", "Freeze", "Cancel"),
            contentDescriptions = listOf("Freezer header"),
            allNormalizedTokens = setOf("freezer", "add", "to", "reel", "narcotics", "freeze", "cancel"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Infinix/Tecno Freezer targeting Reel Narcotics must be intercepted!", result.isTamperAttempt)
    }

    @Test
    fun testInfinixFreezerOtherAppAllowed() {
        val context = ScreenContext(
            packageName = "com.transsion.phonemaster",
            className = "com.transsion.phonemaster.freezer.FreezerActivity",
            viewIds = setOf("freezer_add_btn", "app_title"),
            visibleTexts = listOf("Freezer", "Add to Freezer", "PUBG Mobile", "Freeze", "Cancel"),
            contentDescriptions = listOf("Freezer header"),
            allNormalizedTokens = setOf("freezer", "add", "to", "pubg", "mobile", "freeze", "cancel"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertFalse("Freezing another app in Infinix Freezer must be allowed!", result.isTamperAttempt)
    }

    @Test
    fun testSamsungDeepSleepTamperIntercepted() {
        val context = ScreenContext(
            packageName = "com.samsung.android.lool",
            className = "com.samsung.android.sm.battery.ui.DeepSleepingAppsActivity",
            viewIds = setOf("add_btn", "app_name"),
            visibleTexts = listOf("Deep sleeping apps", "Add apps", "Reel Narcotics", "Add"),
            contentDescriptions = listOf("Deep sleep list"),
            allNormalizedTokens = setOf("deep", "sleeping", "apps", "add", "reel", "narcotics"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Samsung Deep Sleeping apps targeting Reel Narcotics must be intercepted!", result.isTamperAttempt)
    }

    @Test
    fun testXiaomiRestrictBackgroundTamperIntercepted() {
        val context = ScreenContext(
            packageName = "com.miui.powerkeeper",
            className = "com.miui.powerkeeper.ui.AppPowerCenterActivity",
            viewIds = setOf("restrict_background_btn"),
            visibleTexts = listOf("Reel Narcotics", "Battery saver", "Restrict background activity", "Close apps to save power"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("reel", "narcotics", "battery", "saver", "restrict", "background", "activity"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Xiaomi MIUI background restrict targeting Reel Narcotics must be intercepted!", result.isTamperAttempt)
    }

    @Test
    fun testColorOSAppFreezerTamperIntercepted() {
        val context = ScreenContext(
            packageName = "com.oplus.battery",
            className = "com.oplus.battery.freezer.AppFreezerActivity",
            viewIds = setOf("freezer_switch"),
            visibleTexts = listOf("App Quick Freeze", "Auto freeze inactive apps", "Reel Narcotics", "Frozen"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("app", "quick", "freeze", "auto", "inactive", "reel", "narcotics", "frozen"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Oppo/OnePlus ColorOS App Quick Freeze targeting Reel Narcotics must be intercepted!", result.isTamperAttempt)
    }

    @Test
    fun testIceBoxThirdPartyFreezerIntercepted() {
        val context = ScreenContext(
            packageName = "com.catchingnow.icebox",
            className = "com.catchingnow.icebox.MainActivity",
            viewIds = setOf("freeze_action"),
            visibleTexts = listOf("Ice Box", "Freeze apps", "Reel Narcotics", "Freeze now"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("ice", "box", "freeze", "apps", "reel", "narcotics"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Third-party Ice Box freezer targeting Reel Narcotics must be intercepted!", result.isTamperAttempt)
    }

    @Test
    fun testAndroidSettingsBatteryRestrictedIntercepted() {
        val context = ScreenContext(
            packageName = "com.android.settings",
            className = "com.android.settings.fuelgauge.AppBatteryUsageActivity",
            viewIds = setOf("restricted_radio_button"),
            visibleTexts = listOf("App battery usage", "Reel Narcotics", "Restricted", "Restrict battery usage while in background"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("app", "battery", "usage", "reel", "narcotics", "restricted", "restrict", "background"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Android Settings battery restriction targeting Reel Narcotics must be intercepted!", result.isTamperAttempt)
    }

    @Test
    fun testInfinixLauncherDesktopWorkspaceAllowed() {
        // When user is on home screen and both Reel Narcotics icon and Freezer folder icon exist,
        // it must NOT trigger a false positive ejection!
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.Launcher",
            viewIds = setOf("workspace", "icon"),
            visibleTexts = listOf("Freezer", "Reel Narcotics", "Camera", "WhatsApp", "Phone Master"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("freezer", "reel", "narcotics", "camera", "whatsapp", "phone", "master"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertFalse("Normal launcher desktop workspace with Reel Narcotics and Freezer folder must be allowed!", result.isTamperAttempt)
    }

    @Test
    fun testInfinixLauncherLongPressFreezeIntercepted() {
        // When user long-presses Reel Narcotics on Infinix launcher and popup menu has 'Freeze',
        // it must be intercepted!
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.popup.ShortcutView",
            viewIds = setOf("popup_menu", "action_freeze"),
            visibleTexts = listOf("Reel Narcotics", "Freeze", "App info", "Share"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("reel", "narcotics", "freeze", "app", "info", "share"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Launcher context menu with Freeze targeting Reel Narcotics must be intercepted!", result.isTamperAttempt)
    }

    @Test
    fun testInfinixLauncherFreezerFolderAddScreenIntercepted() {
        // When user opens Freezer screen hosted inside XOSLauncher targeting Reel Narcotics
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.xoslauncher.freezer.FreezerActivity",
            viewIds = setOf("freezer_app_list"),
            visibleTexts = listOf("Add to Freezer", "Reel Narcotics", "OK"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("add", "to", "freezer", "reel", "narcotics", "ok"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Launcher FreezerActivity targeting Reel Narcotics must be intercepted!", result.isTamperAttempt)
    }

    @Test
    fun testIsFreezeActionText() {
        assertTrue(TamperDetectionEngine.isFreezeActionText("Freeze"))
        assertTrue(TamperDetectionEngine.isFreezeActionText("To Freezer"))
        assertTrue(TamperDetectionEngine.isFreezeActionText("Send to Freezer"))
        assertTrue(TamperDetectionEngine.isFreezeActionText("Add to Freezer"))
        assertTrue(TamperDetectionEngine.isFreezeActionText("फ्रीज"))
        assertFalse(TamperDetectionEngine.isFreezeActionText("Camera"))
        assertFalse(TamperDetectionEngine.isFreezeActionText("Open"))
    }

    @Test
    fun testMentionsTargetApp() {
        assertTrue(TamperDetectionEngine.mentionsTargetApp("Reel Narcotics"))
        assertTrue(TamperDetectionEngine.mentionsTargetApp("Zenith Focus"))
        assertTrue(TamperDetectionEngine.mentionsTargetApp("reelnarcotics"))
        assertTrue(TamperDetectionEngine.mentionsTargetApp("com.zenith.focus"))
        assertFalse(TamperDetectionEngine.mentionsTargetApp("WhatsApp"))
        assertFalse(TamperDetectionEngine.mentionsTargetApp("Instagram"))
    }

    @Test
    fun testIsFreezerPackage() {
        assertTrue(TamperDetectionEngine.isFreezerPackage("com.transsion.phonemaster"))
        assertTrue(TamperDetectionEngine.isFreezerPackage("com.transsion.xoslauncher"))
        assertTrue(TamperDetectionEngine.isFreezerPackage("com.infinix.freezer"))
        assertTrue(TamperDetectionEngine.isFreezerPackage("com.catchingnow.icebox"))
        assertTrue(TamperDetectionEngine.isFreezerPackage("com.oplus.battery"))
        assertFalse(TamperDetectionEngine.isFreezerPackage("com.google.android.calculator"))
        assertFalse(TamperDetectionEngine.isFreezerPackage("com.whatsapp"))
    }

    @Test
    fun testIsFreezerScreenContextOpenedFolder() {
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.folder.Folder",
            viewIds = setOf("folder_content"),
            visibleTexts = listOf("Freezer", "+", "PUBG Mobile", "Candy Crush"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("freezer", "pubg", "mobile", "candy", "crush"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        assertTrue(TamperDetectionEngine.isFreezerScreenContext(context))
    }

    @Test
    fun testIsFreezerScreenContextDesktopWorkspaceNotFreezer() {
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.Launcher",
            viewIds = setOf("workspace"),
            visibleTexts = listOf("Freezer", "Camera", "Gallery", "Settings", "Phone", "Chrome", "YouTube", "Instagram", "WhatsApp", "Clock", "Maps", "Files", "Reel Narcotics", "Calendar", "Notes", "Calculator", "Contacts", "Weather"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("freezer", "camera", "gallery", "settings", "phone", "chrome", "youtube", "instagram", "whatsapp"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        assertFalse(TamperDetectionEngine.isFreezerScreenContext(context))
    }

    @Test
    fun testIsFreezerScreenContextDedicatedApp() {
        val context = ScreenContext(
            packageName = "com.infinix.freezer",
            className = "com.infinix.freezer.MainActivity",
            viewIds = setOf("root"),
            visibleTexts = listOf("Freezer", "Frozen apps"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("freezer", "frozen", "apps"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        assertTrue(TamperDetectionEngine.isFreezerScreenContext(context))
    }

    @Test
    fun testIsFreezerScreenContextSmallDesktop() {
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.Launcher",
            viewIds = setOf("workspace"),
            visibleTexts = listOf("Freezer", "Reel Narcotics", "Camera", "Phone"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("freezer", "reel", "narcotics", "camera", "phone"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        assertFalse(TamperDetectionEngine.isFreezerScreenContext(context))
    }

    @Test
    fun testEvaluateDesktopFewIconsAllowed() {
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.Launcher",
            viewIds = setOf("workspace"),
            visibleTexts = listOf("Freezer", "Reel Narcotics", "Camera", "Phone"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("freezer", "reel", "narcotics", "camera", "phone"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertFalse("Desktop with only 4 icons including Freezer and Reel Narcotics must be allowed", result.isTamperAttempt)
    }

    @Test
    fun testEvaluateReelNarcoticsInsideFreezerFolderIntercepted() {
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.folder.Folder",
            viewIds = setOf("folder_content"),
            visibleTexts = listOf("Freezer", "Reel Narcotics", "+"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("freezer", "reel", "narcotics"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Reel Narcotics visible inside open Freezer folder must be intercepted", result.isTamperAttempt)
    }

    @Test
    fun testIsFreezerScreenContextOpenFolderWithLauncherRootClass() {
        // Even when root window className is com.transsion.launcher.Launcher,
        // child class in classNames has Folder and title is Freezer
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.Launcher",
            classNames = setOf("com.transsion.launcher.Launcher", "com.transsion.launcher.folder.Folder", "android.widget.TextView"),
            viewIds = setOf("folder_content", "folder_name"),
            visibleTexts = listOf("Freezer", "+", "PUBG Mobile"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("freezer", "pubg", "mobile"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        assertTrue("Open Freezer folder with root Launcher class must be detected via classNames/viewIds", TamperDetectionEngine.isFreezerScreenContext(context))
    }

    @Test
    fun testEvaluateAddToFreezerAppPickerIntercepted() {
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.folder.SelectAppsActivity",
            classNames = setOf("com.transsion.launcher.folder.SelectAppsActivity", "android.widget.CheckBox"),
            viewIds = setOf("select_apps", "checkbox"),
            visibleTexts = listOf("Add to Freezer", "Reel Narcotics", "Camera", "OK"),
            contentDescriptions = emptyList(),
            allNormalizedTokens = setOf("add", "to", "freezer", "reel", "narcotics", "camera", "ok"),
            selectedTexts = emptySet(),
            selectedDescriptions = emptySet()
        )

        val result = TamperDetectionEngine.evaluate(context)
        assertTrue("Add to Freezer app picker targeting Reel Narcotics must be intercepted", result.isTamperAttempt)
    }
}
