package com.zenith.focus.accessibility

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.accessibility.detector.TamperDetectionEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TamperDetectionEngineTest {

    @Test
    fun testRuleAInfinixFreezerAddScreenTriggersTamper() {
        // Infinix Freezer Add screen: mentions Reel Narcotics and Freeze/Add action
        val context = ScreenContext(
            packageName = "com.transsion.xoslauncher",
            className = "com.transsion.launcher.popup.ShortcutView",
            viewIds = setOf("popup_menu", "action_freeze"),
            visibleTexts = listOf("Reel Narcotics", "Freeze", "App info", "Share"),
            allNormalizedTokens = setOf("reel", "narcotics", "freeze", "app", "info", "share")
        )

        val result = TamperDetectionEngine.evaluate(context, isNuclearActive = true)
        assertTrue("Infinix Freezer popup menu targeting Reel Narcotics must trigger tamper", result.isTamperAttempt)
    }

    @Test
    fun testRuleASamsungDeepSleepingAppsTriggersTamper() {
        // Samsung Device Care: Deep sleeping apps screen targeting Reel Narcotics
        val context = ScreenContext(
            packageName = "com.samsung.android.lool",
            className = "com.samsung.android.sm.battery.ui.DeepSleepingAppsActivity",
            viewIds = setOf("app_title", "deep_sleep_button"),
            visibleTexts = listOf("Deep sleeping apps", "Reel Narcotics", "Put to sleep"),
            allNormalizedTokens = setOf("deep", "sleeping", "apps", "reel", "narcotics", "put", "sleep")
        )

        val result = TamperDetectionEngine.evaluate(context, isNuclearActive = true)
        assertTrue("Samsung Deep Sleep targeting Reel Narcotics must trigger tamper", result.isTamperAttempt)
    }

    @Test
    fun testRuleBSettingsDateTimeScreenBlocksOnlyDuringNuclearMode() {
        val dateTimeContext = ScreenContext(
            packageName = "com.android.settings",
            className = "com.android.settings.DateTimeSettings",
            viewIds = setOf("date_time_settings"),
            visibleTexts = listOf("Date & time", "Set time automatically", "Set date"),
            allNormalizedTokens = setOf("date", "time", "set", "automatically")
        )

        // 1. Inactive Nuclear Mode: User CAN access Date & Time settings
        val inactiveResult = TamperDetectionEngine.evaluate(dateTimeContext, isNuclearActive = false)
        assertFalse("Date & time settings must NOT be blocked when Nuclear Mode is inactive", inactiveResult.isTamperAttempt)

        // 2. Active Nuclear Mode: User CANNOT access Date & Time to manipulate clock
        val activeResult = TamperDetectionEngine.evaluate(dateTimeContext, isNuclearActive = true)
        assertTrue("Date & time settings MUST be blocked when Nuclear Mode is active", activeResult.isTamperAttempt)
    }

    @Test
    fun testThirdPartyFreezersAndClonersBlockedDuringNuclearMode() {
        val clonerAndFreezerPackages = listOf(
            "com.catchingnow.icebox",
            "com.aistra.hail",
            "com.oasisfeng.greenify",
            "me.piebridge.brevent",
            "com.tafayor.hibernator",
            "com.hy.freezer",
            "exa.free.f",
            "com.oasisfeng.island",
            "net.typeblog.shelter",
            "com.lbe.parallel.intl",
            "com.excelliance.dualaid",
            "com.ludashi.dualspace",
            "com.clone.android.dual.space",
            "com.vmos.pro",
            "com.pspace.vandroid",
            "com.applisto.appcloner"
        )

        for (pkg in clonerAndFreezerPackages) {
            val context = ScreenContext(packageName = pkg)

            // When Nuclear Mode is active: must be blocked immediately
            val nuclearResult = TamperDetectionEngine.evaluate(context, isNuclearActive = true)
            assertTrue("Cloner/Freezer $pkg must be blocked during Nuclear Mode", nuclearResult.isTamperAttempt)

            // When Nuclear Mode is inactive: not blocked
            val inactiveResult = TamperDetectionEngine.evaluate(context, isNuclearActive = false)
            assertFalse("Cloner/Freezer $pkg should not be blocked when Nuclear Mode is inactive", inactiveResult.isTamperAttempt)
        }
    }

    @Test
    fun testNormalSettingsScreensNeverBlocked() {
        val normalSettingsScreens = listOf(
            ScreenContext(
                packageName = "com.android.settings",
                className = "com.android.settings.wifi.WifiSettings",
                visibleTexts = listOf("Wi-Fi", "Available networks", "Saved networks"),
                allNormalizedTokens = setOf("wifi", "available", "networks")
            ),
            ScreenContext(
                packageName = "com.android.settings",
                className = "com.android.settings.bluetooth.BluetoothSettings",
                visibleTexts = listOf("Bluetooth", "Pair new device"),
                allNormalizedTokens = setOf("bluetooth", "pair", "device")
            ),
            ScreenContext(
                packageName = "com.android.settings",
                className = "com.android.settings.DisplaySettings",
                visibleTexts = listOf("Display", "Brightness level", "Dark theme"),
                allNormalizedTokens = setOf("display", "brightness", "dark", "theme")
            ),
            ScreenContext(
                packageName = "com.android.settings",
                className = "com.android.settings.SoundSettings",
                visibleTexts = listOf("Sound & vibration", "Media volume", "Ring volume"),
                allNormalizedTokens = setOf("sound", "vibration", "volume")
            )
        )

        for (screen in normalSettingsScreens) {
            val resultNuclear = TamperDetectionEngine.evaluate(screen, isNuclearActive = true)
            assertFalse("Normal Settings screen ${screen.className} must never be blocked during Nuclear Mode", resultNuclear.isTamperAttempt)

            val resultNormal = TamperDetectionEngine.evaluate(screen, isNuclearActive = false)
            assertFalse("Normal Settings screen ${screen.className} must never be blocked normally", resultNormal.isTamperAttempt)
        }
    }
}
