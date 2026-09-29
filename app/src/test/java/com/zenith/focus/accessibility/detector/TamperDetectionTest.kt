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
}
