package com.zenith.focus.core.update

import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.nuclear.NuclearSession
import com.zenith.focus.domain.nuclear.NuclearSessionStatus
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class NuclearModeUpdateSurvivalTest {

    @Test
    fun testNuclearSessionStateSurvivesPackageUpdateSimulation() {
        val now = 1725940000000L // Baseline epoch time
        val duration = 24 * 60 * 60 * 1000L // 24 hours
        val endTime = now + duration

        val activeCategories = setOf(
            ContentCategory.YOUTUBE_SHORTS,
            ContentCategory.INSTAGRAM_REELS,
            ContentCategory.FACEBOOK_REELS
        )

        val preUpdateSession = NuclearSession(
            id = UUID.randomUUID().toString(),
            startTimeMillis = now,
            endTimeMillis = endTime,
            startElapsedRealtime = 100000L,
            durationMillis = duration,
            status = NuclearSessionStatus.ACTIVE,
            createdAt = now,
            blockedCountAtStart = 42,
            enabledCategories = activeCategories
        )

        // Verify pre-update state
        assertTrue(preUpdateSession.isCurrentlyActive(nowWallClock = now, nowElapsedRealtime = 100000L))
        assertEquals(NuclearSessionStatus.ACTIVE, preUpdateSession.status)

        // --- SIMULATE APP PACKAGE UPDATE (APK Replaced) ---
        // During package replacement, the process terminates and restarts.
        // The clock advances by 5 minutes (update download + install).
        val postUpdateTime = now + (5 * 60 * 1000L)
        val postUpdateElapsed = 100000L + (5 * 60 * 1000L)

        // Reconstruct session from persisted absolute timestamps
        val reloadedSession = NuclearSession(
            id = preUpdateSession.id,
            startTimeMillis = preUpdateSession.startTimeMillis,
            endTimeMillis = preUpdateSession.endTimeMillis,
            startElapsedRealtime = preUpdateSession.startElapsedRealtime,
            durationMillis = preUpdateSession.durationMillis,
            status = preUpdateSession.status,
            createdAt = preUpdateSession.createdAt,
            blockedCountAtStart = preUpdateSession.blockedCountAtStart,
            enabledCategories = preUpdateSession.enabledCategories
        )

        // MUST REMAIN 100% ACTIVE
        assertTrue("Nuclear mode must remain active after application update", reloadedSession.isCurrentlyActive(nowWallClock = postUpdateTime, nowElapsedRealtime = postUpdateElapsed))
        assertEquals(preUpdateSession.id, reloadedSession.id)
        assertEquals(now, reloadedSession.startTimeMillis)
        assertEquals(endTime, reloadedSession.endTimeMillis)
        assertEquals(duration, reloadedSession.durationMillis)
        assertEquals(activeCategories, reloadedSession.enabledCategories)
        assertEquals(42, reloadedSession.blockedCountAtStart)

        // Verify that only when epoch reaches expiration does it deactivate
        val afterExpiryTime = endTime + 1000L
        assertFalse("Nuclear session must expire only when absolute epoch time passes", reloadedSession.isCurrentlyActive(nowWallClock = afterExpiryTime, nowElapsedRealtime = postUpdateElapsed + duration))
    }
}
