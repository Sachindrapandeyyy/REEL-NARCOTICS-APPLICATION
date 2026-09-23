package com.zenith.focus.domain

import com.zenith.focus.accessibility.detector.DetectionResult
import com.zenith.focus.accessibility.policy.NuclearProtectionPolicy
import com.zenith.focus.data.repository.AppLockRepositoryImpl
import com.zenith.focus.domain.model.AppLockConfig
import com.zenith.focus.domain.model.AppLockMode
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.LockedAppRule
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import com.zenith.focus.domain.nuclear.NuclearSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockTest {

    @Test
    fun testAppLockConfigPermanentModeAlwaysLocked() {
        val config = AppLockConfig(
            isAppLockEnabled = true,
            lockedApps = mapOf(
                "com.supercell.clashofclans" to LockedAppRule(
                    packageName = "com.supercell.clashofclans",
                    appName = "Clash of Clans",
                    lockMode = AppLockMode.PERMANENT
                )
            )
        )

        // Permanent lock must block when Nuclear is inactive
        assertTrue(config.isPackageLocked("com.supercell.clashofclans", isNuclearActive = false))

        // Permanent lock must also block when Nuclear is active
        assertTrue(config.isPackageLocked("com.supercell.clashofclans", isNuclearActive = true))

        // Unlocked package must not be blocked
        assertFalse(config.isPackageLocked("com.whatsapp", isNuclearActive = false))
        assertFalse(config.isPackageLocked("com.whatsapp", isNuclearActive = true))
    }

    @Test
    fun testAppLockConfigNuclearOnlyLockedOnlyWhenNuclearActive() {
        val config = AppLockConfig(
            isAppLockEnabled = true,
            lockedApps = mapOf(
                "com.netflix.mediaclient" to LockedAppRule(
                    packageName = "com.netflix.mediaclient",
                    appName = "Netflix",
                    lockMode = AppLockMode.NUCLEAR_ONLY
                )
            )
        )

        // When Nuclear Mode is OFF: App is completely accessible
        assertFalse(config.isPackageLocked("com.netflix.mediaclient", isNuclearActive = false))

        // When Nuclear Mode is ON: App is strictly locked
        assertTrue(config.isPackageLocked("com.netflix.mediaclient", isNuclearActive = true))
    }

    @Test
    fun testAppLockConfigDisabledDisablesAllLocks() {
        val config = AppLockConfig(
            isAppLockEnabled = false,
            lockedApps = mapOf(
                "com.supercell.clashofclans" to LockedAppRule(
                    packageName = "com.supercell.clashofclans",
                    appName = "Clash of Clans",
                    lockMode = AppLockMode.PERMANENT
                ),
                "com.netflix.mediaclient" to LockedAppRule(
                    packageName = "com.netflix.mediaclient",
                    appName = "Netflix",
                    lockMode = AppLockMode.NUCLEAR_ONLY
                )
            )
        )

        assertFalse(config.isPackageLocked("com.supercell.clashofclans", isNuclearActive = false))
        assertFalse(config.isPackageLocked("com.supercell.clashofclans", isNuclearActive = true))
        assertFalse(config.isPackageLocked("com.netflix.mediaclient", isNuclearActive = true))
    }

    @Test
    fun testAppLockConfigCaseInsensitivePackageLookup() {
        val config = AppLockConfig(
            isAppLockEnabled = true,
            lockedApps = mapOf(
                "com.twitter.android" to LockedAppRule(
                    packageName = "com.twitter.android",
                    appName = "X / Twitter",
                    lockMode = AppLockMode.PERMANENT
                )
            )
        )

        assertTrue(config.isPackageLocked("COM.TWITTER.ANDROID", isNuclearActive = false))
        assertTrue(config.isPackageLocked("Com.Twitter.Android", isNuclearActive = false))
    }

    @Test
    fun testRuleSerializationAndDeserialization() {
        val rules = mapOf(
            "com.instagram.android" to LockedAppRule(
                packageName = "com.instagram.android",
                appName = "Instagram",
                lockMode = AppLockMode.NUCLEAR_ONLY,
                addedTimestamp = 123456789L
            ),
            "com.chess" to LockedAppRule(
                packageName = "com.chess",
                appName = "Chess.com",
                lockMode = AppLockMode.PERMANENT,
                addedTimestamp = 987654321L
            )
        )

        val json = AppLockRepositoryImpl.serializeRules(rules)
        assertTrue(json.contains("com.instagram.android"))
        assertTrue(json.contains("NUCLEAR_ONLY"))
        assertTrue(json.contains("com.chess"))
        assertTrue(json.contains("PERMANENT"))

        val deserialized = AppLockRepositoryImpl.deserializeRules(json)
        assertEquals(2, deserialized.size)

        val instaRule = deserialized["com.instagram.android"]
        assertNotNull(instaRule)
        assertEquals("Instagram", instaRule?.appName)
        assertEquals(AppLockMode.NUCLEAR_ONLY, instaRule?.lockMode)

        val chessRule = deserialized["com.chess"]
        assertNotNull(chessRule)
        assertEquals("Chess.com", chessRule?.appName)
        assertEquals(AppLockMode.PERMANENT, chessRule?.lockMode)
    }

    @Test
    fun testAppLockConfigCounts() {
        val config = AppLockConfig(
            isAppLockEnabled = true,
            lockedApps = mapOf(
                "app1" to LockedAppRule("app1", "App 1", AppLockMode.PERMANENT),
                "app2" to LockedAppRule("app2", "App 2", AppLockMode.PERMANENT),
                "app3" to LockedAppRule("app3", "App 3", AppLockMode.NUCLEAR_ONLY),
                "app4" to LockedAppRule("app4", "App 4", AppLockMode.BOTH)
            )
        )

        assertEquals(4, config.totalCount)
        assertEquals(3, config.permanentCount) // app1, app2, app4
        assertEquals(2, config.nuclearCount)   // app3, app4
    }

    @Test
    fun testNuclearProtectionPolicyWithAppLock() {
        val now = 1000000L
        val activeNuclear = NuclearSession(
            startTimeMillis = now - 10000L,
            endTimeMillis = now + 50000L,
            durationMillis = 60000L,
            status = NuclearSessionStatus.ACTIVE
        )

        val detectionResult = DetectionResult(
            isBlocked = true,
            category = ContentCategory.APP_LOCK,
            confidence = 1.0f,
            ruleId = "app_lock_test",
            reason = "Locked application"
        )

        val shouldBlockNuclear = NuclearProtectionPolicy.shouldBlock(
            result = detectionResult,
            nuclearSession = activeNuclear,
            lockState = LockState(),
            config = ProtectionConfig(),
            nowWallClock = now
        )
        assertTrue("APP_LOCK category must be enforced during active nuclear session", shouldBlockNuclear)
    }
}
