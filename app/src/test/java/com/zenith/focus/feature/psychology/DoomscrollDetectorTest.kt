package com.zenith.focus.feature.psychology

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DoomscrollDetectorTest {

    private var simulatedTime = 100_000L
    private lateinit var detector: DoomscrollDetector

    @Before
    fun setUp() {
        simulatedTime = 100_000L
        detector = DoomscrollDetector(
            swipeThreshold = 18,
            windowDurationMs = 60_000L,
            minScrollIntervalMs = 1_500L,
            cooldownMs = 120_000L,
            timeProvider = { simulatedTime }
        )
    }

    @Test
    fun `test debounce rejects rapid scrolls under 1500ms`() {
        // First scroll
        assertFalse(detector.onScroll())
        assertEquals(1, detector.getCurrentCount())

        // Immediate scroll 500ms later -> debounced
        simulatedTime += 500L
        assertFalse(detector.onScroll())
        assertEquals(1, detector.getCurrentCount())

        // Scroll after 1500ms -> accepted
        simulatedTime += 1500L
        assertFalse(detector.onScroll())
        assertEquals(2, detector.getCurrentCount())
    }

    @Test
    fun `test threshold reached triggers doomscroll and resets with cooldown`() {
        // Deliver 17 valid scrolls
        for (i in 1..17) {
            simulatedTime += 1600L
            val triggered = detector.onScroll()
            assertFalse("Scroll $i should not trigger yet", triggered)
        }
        assertEquals(17, detector.getCurrentCount())

        // 18th scroll within window -> triggers!
        simulatedTime += 1600L
        val triggered = detector.onScroll()
        assertTrue("18th scroll should trigger doomscroll detection", triggered)
        assertTrue(detector.isCoolingDown())
        assertEquals(0, detector.getCurrentCount())

        // During cooldown, subsequent scrolls return false even if 2 seconds apart
        simulatedTime += 2000L
        assertFalse(detector.onScroll())
    }

    @Test
    fun `test swipes outside sliding window expire`() {
        // 5 scrolls early on
        for (i in 1..5) {
            simulatedTime += 1600L
            detector.onScroll()
        }
        assertEquals(5, detector.getCurrentCount())

        // Advance time by 65 seconds (window is 60s)
        simulatedTime += 65_000L

        // Next scroll
        detector.onScroll()
        // Only the latest scroll remains, old 5 expired
        assertEquals(1, detector.getCurrentCount())
    }

    @Test
    fun `test cooldown expires after configured duration`() {
        // Trigger cooldown directly
        detector.resetWithCooldown(120_000L)
        assertTrue(detector.isCoolingDown())

        // 100 seconds later -> still in cooldown
        simulatedTime += 100_000L
        assertTrue(detector.isCoolingDown())
        assertFalse(detector.onScroll())

        // 25 seconds later (total 125s) -> cooldown expired
        simulatedTime += 25_000L
        assertFalse(detector.isCoolingDown())

        // New scroll accepted
        assertFalse(detector.onScroll())
        assertEquals(1, detector.getCurrentCount())
    }

    @Test
    fun `test manual reset clears all state`() {
        detector.onScroll()
        simulatedTime += 2000L
        detector.onScroll()
        assertEquals(2, detector.getCurrentCount())

        detector.reset()
        assertEquals(0, detector.getCurrentCount())
        assertFalse(detector.isCoolingDown())
    }
}
