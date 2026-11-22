package com.zenith.focus.feature.stats

/**
 * Metric models for daily focus score and attention reclaimed.
 */
data class DailyFocusMetrics(
    val score: Int,
    val timeReclaimedMinutes: Int,
    val impulsesDefeated: Int,
    val doomscrollsBroken: Int,
    val shortFormBlocks: Int,
    val tamperAttempts: Int,
    val streakDays: Int,
    val isNuclearActive: Boolean
)

/**
 * Pure-Kotlin offline calculator transforming SQLite block events into motivating Opal-style metrics.
 */
object FocusScoreCalculator {

    const val MINUTES_PER_IMPULSE = 4
    const val MINUTES_PER_DOOMSCROLL = 8
    const val MINUTES_PER_BLOCK = 2

    const val BASE_SCORE = 70
    const val MIN_SCORE = 10
    const val MAX_SCORE = 100

    fun calculateTimeReclaimedMinutes(
        impulsesDefeated: Int,
        doomscrollsBroken: Int,
        shortFormBlocks: Int
    ): Int {
        return (impulsesDefeated.coerceAtLeast(0) * MINUTES_PER_IMPULSE) +
                (doomscrollsBroken.coerceAtLeast(0) * MINUTES_PER_DOOMSCROLL) +
                (shortFormBlocks.coerceAtLeast(0) * MINUTES_PER_BLOCK)
    }

    fun calculateDailyScore(
        impulsesDefeated: Int,
        doomscrollsBroken: Int,
        shortFormBlocks: Int,
        tamperAttempts: Int,
        streakDays: Int = 0,
        isNuclearActive: Boolean = false
    ): Int {
        var score = BASE_SCORE
        score += impulsesDefeated.coerceAtLeast(0) * 3
        score += doomscrollsBroken.coerceAtLeast(0) * 5
        score += shortFormBlocks.coerceAtLeast(0) * 1
        score += streakDays.coerceIn(0, 7) * 2
        if (isNuclearActive) {
            score += 10
        }
        score -= tamperAttempts.coerceAtLeast(0) * 10
        return score.coerceIn(MIN_SCORE, MAX_SCORE)
    }

    fun calculateMetrics(
        impulsesDefeated: Int,
        doomscrollsBroken: Int,
        shortFormBlocks: Int,
        tamperAttempts: Int,
        streakDays: Int = 0,
        isNuclearActive: Boolean = false
    ): DailyFocusMetrics {
        val timeReclaimed = calculateTimeReclaimedMinutes(
            impulsesDefeated = impulsesDefeated,
            doomscrollsBroken = doomscrollsBroken,
            shortFormBlocks = shortFormBlocks
        )
        val score = calculateDailyScore(
            impulsesDefeated = impulsesDefeated,
            doomscrollsBroken = doomscrollsBroken,
            shortFormBlocks = shortFormBlocks,
            tamperAttempts = tamperAttempts,
            streakDays = streakDays,
            isNuclearActive = isNuclearActive
        )
        return DailyFocusMetrics(
            score = score,
            timeReclaimedMinutes = timeReclaimed,
            impulsesDefeated = impulsesDefeated,
            doomscrollsBroken = doomscrollsBroken,
            shortFormBlocks = shortFormBlocks,
            tamperAttempts = tamperAttempts,
            streakDays = streakDays,
            isNuclearActive = isNuclearActive
        )
    }
}
