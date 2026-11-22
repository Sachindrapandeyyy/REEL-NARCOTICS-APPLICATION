package com.zenith.focus.feature.stats

import org.junit.Assert.*
import org.junit.Test

class FocusScoreCalculatorTest {

    @Test
    fun `calculateTimeReclaimedMinutes correctly weighs impulses doomscrolls and blocks`() {
        // 5 impulses * 4m = 20m
        // 3 doomscrolls * 8m = 24m
        // 10 short-form blocks * 2m = 20m
        // Total = 64 minutes
        val timeReclaimed = FocusScoreCalculator.calculateTimeReclaimedMinutes(
            impulsesDefeated = 5,
            doomscrollsBroken = 3,
            shortFormBlocks = 10
        )
        assertEquals(64, timeReclaimed)
    }

    @Test
    fun `calculateTimeReclaimedMinutes returns zero for non-positive inputs`() {
        val zero = FocusScoreCalculator.calculateTimeReclaimedMinutes(0, 0, 0)
        assertEquals(0, zero)

        val negativeHandling = FocusScoreCalculator.calculateTimeReclaimedMinutes(-5, -2, -1)
        assertEquals(0, negativeHandling)
    }

    @Test
    fun `calculateDailyScore defaults to 70 base with no activity`() {
        val score = FocusScoreCalculator.calculateDailyScore(
            impulsesDefeated = 0,
            doomscrollsBroken = 0,
            shortFormBlocks = 0,
            tamperAttempts = 0,
            streakDays = 0,
            isNuclearActive = false
        )
        assertEquals(70, score)
    }

    @Test
    fun `calculateDailyScore adds bonuses correctly`() {
        // base 70 + (2 impulses * 3 = 6) + (1 doomscroll * 5 = 5) + (10 blocks * 1 = 10) + streak (min(5,7)*2 = 10) + nuclear 10
        // 70 + 6 + 5 + 10 + 10 + 10 = 111 -> clamped to 100
        val score = FocusScoreCalculator.calculateDailyScore(
            impulsesDefeated = 2,
            doomscrollsBroken = 1,
            shortFormBlocks = 10,
            tamperAttempts = 0,
            streakDays = 5,
            isNuclearActive = true
        )
        assertEquals(100, score)
    }

    @Test
    fun `calculateDailyScore penalizes tamper attempts`() {
        // base 70 - (3 tamper * 10 = 30) = 40
        val score = FocusScoreCalculator.calculateDailyScore(
            impulsesDefeated = 0,
            doomscrollsBroken = 0,
            shortFormBlocks = 0,
            tamperAttempts = 3,
            streakDays = 0,
            isNuclearActive = false
        )
        assertEquals(40, score)
    }

    @Test
    fun `calculateDailyScore clamps between 10 and 100`() {
        // Extreme negative: 70 - 100 = -30 -> clamped to 10
        val minScore = FocusScoreCalculator.calculateDailyScore(
            impulsesDefeated = 0,
            doomscrollsBroken = 0,
            shortFormBlocks = 0,
            tamperAttempts = 10,
            streakDays = 0,
            isNuclearActive = false
        )
        assertEquals(10, minScore)

        // Extreme positive: 70 + 300 = 370 -> clamped to 100
        val maxScore = FocusScoreCalculator.calculateDailyScore(
            impulsesDefeated = 100,
            doomscrollsBroken = 100,
            shortFormBlocks = 500,
            tamperAttempts = 0,
            streakDays = 30,
            isNuclearActive = true
        )
        assertEquals(100, maxScore)
    }

    @Test
    fun `calculateMetrics returns complete DailyFocusMetrics`() {
        val metrics = FocusScoreCalculator.calculateMetrics(
            impulsesDefeated = 4,
            doomscrollsBroken = 2,
            shortFormBlocks = 8,
            tamperAttempts = 0,
            streakDays = 3,
            isNuclearActive = false
        )

        // Time: 4*4 (16) + 2*8 (16) + 8*2 (16) = 48 minutes
        assertEquals(48, metrics.timeReclaimedMinutes)
        // Score: 70 + 4*3 (12) + 2*5 (10) + 8*1 (8) + (3*2 = 6) = 106 -> clamped to 100
        assertEquals(100, metrics.score)
        assertEquals(4, metrics.impulsesDefeated)
        assertEquals(2, metrics.doomscrollsBroken)
        assertEquals(8, metrics.shortFormBlocks)
    }
}
