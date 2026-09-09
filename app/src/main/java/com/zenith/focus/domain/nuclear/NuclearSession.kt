package com.zenith.focus.domain.nuclear

import java.util.UUID

data class NuclearSession(
    val id: String = UUID.randomUUID().toString(),
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val startElapsedRealtime: Long = 0L,
    val durationMillis: Long = 0L,
    val status: NuclearSessionStatus = NuclearSessionStatus.INACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val blockedCountAtStart: Int = 0,
    val enabledCategories: Set<com.zenith.focus.domain.model.ContentCategory> = com.zenith.focus.domain.model.ContentCategory.values().toSet()
) {
    /**
     * Determines whether Nuclear Mode is currently actively enforced.
     * Uses both monotonic elapsedRealtime and wall-clock time to prevent clock tampering.
     */
    fun isCurrentlyActive(
        nowWallClock: Long = System.currentTimeMillis(),
        nowElapsedRealtime: Long = android.os.SystemClock.elapsedRealtime()
    ): Boolean {
        if (status != NuclearSessionStatus.ACTIVE) return false

        // Check monotonic elapsed time if device has not rebooted since session start
        if (startElapsedRealtime > 0L && nowElapsedRealtime >= startElapsedRealtime) {
            val elapsedFromBoot = nowElapsedRealtime - startElapsedRealtime
            // If user jumped wall-clock forward, monotonic clock prevents early exit
            if (elapsedFromBoot < durationMillis) {
                return true
            }
        }

        // Fallback or post-reboot: check wall-clock
        return nowWallClock < endTimeMillis
    }

    /**
     * Calculates the true remaining milliseconds.
     */
    fun remainingMillis(
        nowWallClock: Long = System.currentTimeMillis(),
        nowElapsedRealtime: Long = android.os.SystemClock.elapsedRealtime()
    ): Long {
        if (status != NuclearSessionStatus.ACTIVE) return 0L

        if (startElapsedRealtime > 0L && nowElapsedRealtime >= startElapsedRealtime) {
            val elapsed = nowElapsedRealtime - startElapsedRealtime
            val monotonicRemaining = (durationMillis - elapsed).coerceAtLeast(0L)
            // If wall clock was manipulated forward, respect monotonic remaining
            if (monotonicRemaining > 0L) {
                return monotonicRemaining
            }
        }

        return (endTimeMillis - nowWallClock).coerceAtLeast(0L)
    }

    /**
     * Progress of the active session from 0.0 to 1.0.
     */
    fun progressFraction(
        nowWallClock: Long = System.currentTimeMillis(),
        nowElapsedRealtime: Long = android.os.SystemClock.elapsedRealtime()
    ): Float {
        if (status != NuclearSessionStatus.ACTIVE || durationMillis <= 0L) return 0f
        val remaining = remainingMillis(nowWallClock, nowElapsedRealtime)
        val elapsed = (durationMillis - remaining).coerceAtLeast(0L)
        return (elapsed.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)
    }
}
