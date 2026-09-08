package com.zenith.focus.domain

import com.zenith.focus.accessibility.detector.DetectionResult
import com.zenith.focus.accessibility.policy.NuclearProtectionPolicy
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import com.zenith.focus.domain.nuclear.NuclearSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NuclearSessionTest {

    @Test
    fun testNuclearSessionActiveWindow() {
        val startWall = 1000000L
        val startElapsed = 50000L
        val duration = 3600000L // 1 hour
        val endWall = startWall + duration

        val session = NuclearSession(
            startTimeMillis = startWall,
            endTimeMillis = endWall,
            startElapsedRealtime = startElapsed,
            durationMillis = duration,
            status = NuclearSessionStatus.ACTIVE
        )

        // Midway (30 minutes in)
        val midWall = startWall + 1800000L
        val midElapsed = startElapsed + 1800000L

        assertTrue(session.isCurrentlyActive(midWall, midElapsed))
        assertEquals(1800000L, session.remainingMillis(midWall, midElapsed))
        assertEquals(0.5f, session.progressFraction(midWall, midElapsed), 0.001f)
    }

    @Test
    fun testNuclearSessionMonotonicClockTamperResistance() {
        val startWall = 1000000L
        val startElapsed = 50000L
        val duration = 3600000L // 1 hour
        val endWall = startWall + duration

        val session = NuclearSession(
            startTimeMillis = startWall,
            endTimeMillis = endWall,
            startElapsedRealtime = startElapsed,
            durationMillis = duration,
            status = NuclearSessionStatus.ACTIVE
        )

        // User tampers with system wall clock: jumps forward by 2 hours
        val tamperedWall = endWall + 3600000L // System clock says tomorrow/later
        // But only 15 minutes actually elapsed on the monotonic clock
        val actualElapsed = startElapsed + (15 * 60 * 1000L)

        // Monotonic reference MUST prevent bypass!
        assertTrue(session.isCurrentlyActive(tamperedWall, actualElapsed))
        assertEquals(45 * 60 * 1000L, session.remainingMillis(tamperedWall, actualElapsed))
    }

    @Test
    fun testNuclearSessionNaturalExpiration() {
        val startWall = 1000000L
        val startElapsed = 50000L
        val duration = 3600000L
        val endWall = startWall + duration

        val session = NuclearSession(
            startTimeMillis = startWall,
            endTimeMillis = endWall,
            startElapsedRealtime = startElapsed,
            durationMillis = duration,
            status = NuclearSessionStatus.ACTIVE
        )

        // After duration has fully elapsed on both clocks
        val pastWall = endWall + 5000L
        val pastElapsed = startElapsed + duration + 5000L

        assertFalse(session.isCurrentlyActive(pastWall, pastElapsed))
        assertEquals(0L, session.remainingMillis(pastWall, pastElapsed))
        assertEquals(1.0f, session.progressFraction(pastWall, pastElapsed), 0.001f)
    }

    @Test
    fun testNuclearProtectionPolicyOverridesPreferences() {
        val now = 1000000L
        val activeSession = NuclearSession(
            startTimeMillis = now - 10000L,
            endTimeMillis = now + 3600000L,
            startElapsedRealtime = 50000L,
            durationMillis = 3600000L,
            status = NuclearSessionStatus.ACTIVE
        )

        val lockState = LockState(isActive = false)
        // User attempted to disable YouTube Shorts and Instagram Reels in normal config
        val userConfigWithFeedsDisabled = ProtectionConfig(
            blockYouTubeShorts = false,
            blockInstagramReels = false
        )

        val shortsResult = DetectionResult(
            isBlocked = true,
            category = ContentCategory.YOUTUBE_SHORTS,
            confidence = 1.0f,
            ruleId = "youtube_shorts",
            reason = "Addictive short form video"
        )

        val reelsResult = DetectionResult(
            isBlocked = true,
            category = ContentCategory.INSTAGRAM_REELS,
            confidence = 1.0f,
            ruleId = "instagram_reels",
            reason = "Addictive reel player"
        )

        // Under Nuclear Mode, both MUST be blocked despite user preferences!
        assertTrue(
            NuclearProtectionPolicy.shouldBlock(
                result = shortsResult,
                nuclearSession = activeSession,
                lockState = lockState,
                config = userConfigWithFeedsDisabled,
                nowWallClock = now,
                nowElapsedRealtime = 60000L
            )
        )

        assertTrue(
            NuclearProtectionPolicy.shouldBlock(
                result = reelsResult,
                nuclearSession = activeSession,
                lockState = lockState,
                config = userConfigWithFeedsDisabled,
                nowWallClock = now,
                nowElapsedRealtime = 60000L
            )
        )
    }

    @Test
    fun testNoLockHasNoRestrictions() {
        val now = 1000000L
        val inactiveNuclear = NuclearSession(status = NuclearSessionStatus.INACTIVE)
        val inactiveLock = LockState(isActive = false)
        val normalConfig = ProtectionConfig()

        val shortsResult = DetectionResult(
            isBlocked = true,
            category = ContentCategory.YOUTUBE_SHORTS,
            confidence = 1.0f,
            ruleId = "youtube_shorts",
            reason = "Addictive short form video"
        )

        // Without any lock active: NO RESTRICTIONS!
        org.junit.Assert.assertFalse(
            NuclearProtectionPolicy.shouldBlock(
                result = shortsResult,
                nuclearSession = inactiveNuclear,
                lockState = inactiveLock,
                config = normalConfig,
                nowWallClock = now,
                nowElapsedRealtime = 60000L
            )
        )
    }

    @Test
    fun testStandardFocusLockEnforcesRestrictions() {
        val now = 1000000L
        val inactiveNuclear = NuclearSession(status = NuclearSessionStatus.INACTIVE)
        val activeStandardLock = LockState(
            isActive = true,
            startTimeMillis = now - 5000L,
            endTimeMillis = now + 1800000L
        )
        val normalConfig = ProtectionConfig()

        val shortsResult = DetectionResult(
            isBlocked = true,
            category = ContentCategory.YOUTUBE_SHORTS,
            confidence = 1.0f,
            ruleId = "youtube_shorts",
            reason = "Addictive short form video"
        )

        // Under Standard Focus Lock: Restrictions MUST be active!
        assertTrue(
            NuclearProtectionPolicy.shouldBlock(
                result = shortsResult,
                nuclearSession = inactiveNuclear,
                lockState = activeStandardLock,
                config = normalConfig,
                nowWallClock = now,
                nowElapsedRealtime = 60000L
            )
        )
    }
}
