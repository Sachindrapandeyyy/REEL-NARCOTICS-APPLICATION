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
}
