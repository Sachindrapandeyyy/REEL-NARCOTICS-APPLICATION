package com.zenith.focus.feature.psychology

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IntentTimeBoxManagerTest {

    private var simulatedRealtime = 50_000L
    private lateinit var manager: IntentTimeBoxManager

    @Before
    fun setUp() {
        simulatedRealtime = 50_000L
        manager = IntentTimeBoxManager(
            elapsedRealtimeProvider = { simulatedRealtime }
        )
    }

    @Test
    fun `test startTimeBox creates active session`() {
        val session = manager.startTimeBox("com.instagram.android", 10, "DMs / Reply")
        assertEquals("com.instagram.android", session.packageName)
        assertEquals(600_000L, session.durationMs)
        assertEquals("DMs / Reply", session.purpose)
        assertEquals(50_000L, session.startTimeRealtime)
        assertEquals(650_000L, session.endTimeRealtime)

        assertTrue(manager.isCurrentlyActive("com.instagram.android"))
        assertNotNull(manager.getActiveSession("com.instagram.android"))
        assertEquals(600_000L, manager.getRemainingMillis("com.instagram.android"))
        assertEquals(0.0f, manager.getProgressFraction("com.instagram.android"), 0.01f)
    }

    @Test
    fun `test progressFraction and remainingMillis progress linearly`() {
        manager.startTimeBox("com.google.android.youtube", 10, "Study / Research")

        // Advance 5 minutes (300,000ms out of 600,000ms)
        simulatedRealtime += 300_000L

        assertTrue(manager.isCurrentlyActive("com.google.android.youtube"))
        assertEquals(300_000L, manager.getRemainingMillis("com.google.android.youtube"))
        assertEquals(0.5f, manager.getProgressFraction("com.google.android.youtube"), 0.01f)

        // Advance 4 more minutes (total 9 min = 540,000ms)
        simulatedRealtime += 240_000L
        assertEquals(60_000L, manager.getRemainingMillis("com.google.android.youtube"))
        assertEquals(0.9f, manager.getProgressFraction("com.google.android.youtube"), 0.01f)
    }

    @Test
    fun `test session expiration clears session and marks inactive`() {
        manager.startTimeBox("com.facebook.katana", 5, "Quick Check")
        assertTrue(manager.isCurrentlyActive("com.facebook.katana"))

        // Advance beyond 5 minutes
        simulatedRealtime += 301_000L

        assertFalse(manager.isCurrentlyActive("com.facebook.katana"))
        assertNull(manager.getActiveSession("com.facebook.katana"))
        assertEquals(0L, manager.getRemainingMillis("com.facebook.katana"))
        assertEquals(0f, manager.getProgressFraction("com.facebook.katana"), 0.01f)
    }

    @Test
    fun `test clearSession and clearAll manually terminates sessions`() {
        manager.startTimeBox("com.snapchat.android", 10, "DMs")
        manager.startTimeBox("com.reddit.frontpage", 15, "Research")

        assertTrue(manager.isCurrentlyActive("com.snapchat.android"))
        assertTrue(manager.isCurrentlyActive("com.reddit.frontpage"))

        manager.clearSession("com.snapchat.android")
        assertFalse(manager.isCurrentlyActive("com.snapchat.android"))
        assertTrue(manager.isCurrentlyActive("com.reddit.frontpage"))

        manager.clearAll()
        assertFalse(manager.isCurrentlyActive("com.reddit.frontpage"))
    }
}
