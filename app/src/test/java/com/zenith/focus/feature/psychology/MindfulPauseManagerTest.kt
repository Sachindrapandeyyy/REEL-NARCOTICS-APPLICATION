package com.zenith.focus.feature.psychology

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MindfulPauseManagerTest {

    private lateinit var manager: MindfulPauseManager

    @Before
    fun setUp() {
        manager = MindfulPauseManager()
    }

    @Test
    fun `test monitored packages are recognized case-insensitively`() {
        assertTrue(manager.isMonitoredPackage("com.instagram.android"))
        assertTrue(manager.isMonitoredPackage("COM.GOOGLE.ANDROID.YOUTUBE"))
        assertTrue(manager.isMonitoredPackage("com.facebook.katana"))
        assertTrue(manager.isMonitoredPackage("com.snapchat.android"))
        assertTrue(manager.isMonitoredPackage("com.reddit.frontpage"))

        // Unmonitored utility apps and substring-overlapping framework names like "android"
        assertFalse(manager.isMonitoredPackage("com.google.android.calculator"))
        assertFalse(manager.isMonitoredPackage("com.whatsapp"))
        assertFalse(manager.isMonitoredPackage("android"))
        assertFalse(manager.isMonitoredPackage("com"))
    }

    @Test
    fun `test shouldTriggerPause respects feature toggle and active timebox`() {
        // Feature disabled
        assertFalse(
            manager.shouldTriggerPause(
                packageName = "com.instagram.android",
                isFeatureEnabled = false,
                hasActiveTimeBox = false
            )
        )

        // Active TimeBox running -> should NOT trigger pause
        assertFalse(
            manager.shouldTriggerPause(
                packageName = "com.instagram.android",
                isFeatureEnabled = true,
                hasActiveTimeBox = true
            )
        )

        // Monitored app, enabled, no time box -> triggers pause!
        assertTrue(
            manager.shouldTriggerPause(
                packageName = "com.instagram.android",
                isFeatureEnabled = true,
                hasActiveTimeBox = false
            )
        )
    }

    @Test
    fun `test markPassed suppresses pause during continuous session`() {
        val pkg = "com.instagram.android"
        assertTrue(manager.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))

        manager.markPassed(pkg)
        assertFalse(manager.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))
    }

    @Test
    fun `test onForegroundPackageChanged clears passed state when switching to launcher`() {
        val pkg = "com.instagram.android"
        manager.onForegroundPackageChanged(pkg)
        manager.markPassed(pkg)

        // User stays in Instagram (sub-activities or dialogs)
        manager.onForegroundPackageChanged(pkg)
        assertFalse(manager.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))

        // User presses Home and goes to launcher
        manager.onForegroundPackageChanged("com.android.launcher3")

        // User re-opens Instagram -> pause should trigger again!
        assertTrue(manager.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))
    }

    @Test
    fun `test transient keyboards and closing cooldown prevent pause double-launch`() {
        var clock = 10_000L
        val timedMgr = MindfulPauseManager(timeProvider = { clock })
        val pkg = "com.instagram.android"

        timedMgr.onForegroundPackageChanged(pkg)
        timedMgr.markPassed(pkg)

        // Third-party Transsion Facemoji / Gboard IME appears over Instagram -> passed state preserved
        timedMgr.onForegroundPackageChanged("com.facemoji.lite.transsion")
        assertFalse(timedMgr.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))

        // After tryLockTrigger locks, rapid duplicate window events within 2500ms must not re-trigger
        timedMgr.clearPassed(pkg)
        assertTrue(timedMgr.tryLockTrigger(pkg))
        assertFalse(timedMgr.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))

        clock += 2600L
        assertTrue(timedMgr.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))

        // Closing app suppresses trailing events for 2500ms
        timedMgr.markClosedRecently(pkg)
        assertFalse(timedMgr.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))
        clock += 2600L
        assertTrue(timedMgr.shouldTriggerPause(pkg, isFeatureEnabled = true, hasActiveTimeBox = false))
    }
}
