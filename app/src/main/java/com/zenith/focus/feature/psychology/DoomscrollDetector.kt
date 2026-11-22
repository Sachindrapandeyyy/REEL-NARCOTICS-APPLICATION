package com.zenith.focus.feature.psychology

import java.util.ArrayDeque

/**
 * Sliding-window velocity detector that detects mindless rapid doomscrolling.
 * Debounces rapid swipes (min 1500ms apart) and triggers when 18+ swipes occur within 60s.
 */
class DoomscrollDetector(
    private val swipeThreshold: Int = 18,
    private val windowDurationMs: Long = 60_000L,
    private val minScrollIntervalMs: Long = 1_500L,
    private val cooldownMs: Long = 120_000L,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {
    private val timestamps = ArrayDeque<Long>()
    private var lastScrollTime = 0L
    private var cooldownUntil = 0L

    @Synchronized
    fun onScroll(): Boolean {
        val now = timeProvider()
        if (now < cooldownUntil) {
            return false
        }
        if (now - lastScrollTime < minScrollIntervalMs) {
            return false
        }
        lastScrollTime = now

        while (timestamps.isNotEmpty() && now - timestamps.first > windowDurationMs) {
            timestamps.removeFirst()
        }

        timestamps.addLast(now)

        if (timestamps.size >= swipeThreshold) {
            resetWithCooldown(cooldownMs)
            return true
        }
        return false
    }

    @Synchronized
    fun resetWithCooldown(cooldown: Long = cooldownMs) {
        timestamps.clear()
        val now = timeProvider()
        cooldownUntil = now + cooldown
        lastScrollTime = 0L
    }

    @Synchronized
    fun reset() {
        timestamps.clear()
        cooldownUntil = 0L
        lastScrollTime = 0L
    }

    @Synchronized
    fun isCoolingDown(): Boolean {
        return timeProvider() < cooldownUntil
    }

    @Synchronized
    fun getCurrentCount(): Int {
        val now = timeProvider()
        while (timestamps.isNotEmpty() && now - timestamps.first > windowDurationMs) {
            timestamps.removeFirst()
        }
        return timestamps.size
    }
}
