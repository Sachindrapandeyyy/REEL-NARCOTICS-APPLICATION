package com.zenith.focus.domain

import com.zenith.focus.domain.model.LockMode
import com.zenith.focus.domain.model.LockState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockEngineTest {

    @Test
    fun testLockIsActiveWithinWindow() {
        val start = 1000000L
        val end = start + 3600000L // 1 hour later
        val lock = LockState(
            isActive = true,
            startTimeMillis = start,
            endTimeMillis = end,
            mode = LockMode.QUICK
        )

        // Halfway through
        val midway = start + 1800000L
        assertTrue(lock.isCurrentlyActive(midway))
        assertEquals(1800000L, lock.remainingMillis(midway))
        assertEquals(0.5f, lock.progressFraction(midway), 0.001f)
    }

    @Test
    fun testLockExpiresWhenTimePasses() {
        val start = 1000000L
        val end = start + 3600000L
        val lock = LockState(
            isActive = true,
            startTimeMillis = start,
            endTimeMillis = end
        )

        val afterEnd = end + 1000L
        assertFalse(lock.isCurrentlyActive(afterEnd))
        assertEquals(0L, lock.remainingMillis(afterEnd))
        assertEquals(1.0f, lock.progressFraction(afterEnd), 0.001f)
    }

    @Test
    fun testRemainingMillisNeverNegative() {
        val lock = LockState(
            isActive = true,
            startTimeMillis = 1000L,
            endTimeMillis = 2000L
        )

        val past = 5000L
        assertEquals(0L, lock.remainingMillis(past))
    }

    @Test
    fun testInactiveLockReturnsZeroRemaining() {
        val lock = LockState(isActive = false)
        assertEquals(0L, lock.remainingMillis(12345L))
        assertFalse(lock.isCurrentlyActive(12345L))
    }
}
