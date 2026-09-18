package com.zenith.focus.domain

import com.zenith.focus.accessibility.detector.DetectionResult
import com.zenith.focus.accessibility.policy.NuclearProtectionPolicy
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.HabitConfig
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HabitConfigTest {

    @Test
    fun testDefaultHabitConfig() {
        val config = HabitConfig()
        assertTrue(config.morningPledgeEnabled)
        assertEquals(8, config.morningPledgeHour)
        assertEquals(0, config.morningPledgeMinute)

        assertTrue(config.eveningSummaryEnabled)
        assertEquals(21, config.eveningSummaryHour)
        assertEquals(0, config.eveningSummaryMinute)

        assertFalse(config.bedtimeShieldEnabled)
        assertEquals(23, config.bedtimeStartHour)
        assertEquals(0, config.bedtimeStartMinute)
        assertEquals(6, config.bedtimeEndHour)
        assertEquals(30, config.bedtimeEndMinute)
    }

    @Test
    fun testBedtimeShieldDisabledReturnsFalse() {
        val config = HabitConfig(bedtimeShieldEnabled = false)
        val midnightMillis = createTimestamp(hour = 0, minute = 30)
        assertFalse(config.isBedtimeActive(midnightMillis))
    }

    @Test
    fun testBedtimeShieldOvernightWindow() {
        val config = HabitConfig(
            bedtimeShieldEnabled = true,
            bedtimeStartHour = 23,
            bedtimeStartMinute = 0,
            bedtimeEndHour = 6,
            bedtimeEndMinute = 30
        )

        // 23:30 (inside start window) -> true
        assertTrue(config.isBedtimeActive(createTimestamp(hour = 23, minute = 30)))

        // 02:00 (midnight hours) -> true
        assertTrue(config.isBedtimeActive(createTimestamp(hour = 2, minute = 0)))

        // 06:29 (just before morning expiration) -> true
        assertTrue(config.isBedtimeActive(createTimestamp(hour = 6, minute = 29)))

        // 06:31 (just after expiration) -> false
        assertFalse(config.isBedtimeActive(createTimestamp(hour = 6, minute = 31)))

        // 12:00 (midday) -> false
        assertFalse(config.isBedtimeActive(createTimestamp(hour = 12, minute = 0)))

        // 22:59 (1 minute before start) -> false
        assertFalse(config.isBedtimeActive(createTimestamp(hour = 22, minute = 59)))
    }

    @Test
    fun testBedtimeShieldSameDayWindow() {
        val config = HabitConfig(
            bedtimeShieldEnabled = true,
            bedtimeStartHour = 14,
            bedtimeStartMinute = 0,
            bedtimeEndHour = 18,
            bedtimeEndMinute = 0
        )

        // 15:00 -> true
        assertTrue(config.isBedtimeActive(createTimestamp(hour = 15, minute = 0)))

        // 13:59 -> false
        assertFalse(config.isBedtimeActive(createTimestamp(hour = 13, minute = 59)))

        // 18:01 -> false
        assertFalse(config.isBedtimeActive(createTimestamp(hour = 18, minute = 1)))
    }

    @Test
    fun testNuclearProtectionPolicyEnforcesBedtimeShield() {
        val midnightMillis = createTimestamp(hour = 1, minute = 0)
        val activeBedtimeHabits = HabitConfig(
            bedtimeShieldEnabled = true,
            bedtimeStartHour = 23,
            bedtimeEndHour = 6
        )

        val reelResult = DetectionResult(
            isBlocked = true,
            confidence = 1.0f,
            category = ContentCategory.INSTAGRAM_REELS,
            ruleId = "rule_test",
            reason = "Test reel match"
        )

        // Neither Nuclear nor Normal Lock is active, but Bedtime Shield IS active
        val shouldBlock = NuclearProtectionPolicy.shouldBlock(
            result = reelResult,
            nuclearSession = NuclearSession(), // inactive
            lockState = LockState(), // inactive
            config = ProtectionConfig(),
            nowWallClock = midnightMillis,
            nowElapsedRealtime = 60000L,
            habitConfig = activeBedtimeHabits
        )
        assertTrue(shouldBlock)

        // Outside bedtime window (e.g. 10:00 AM)
        val morningMillis = createTimestamp(hour = 10, minute = 0)
        val shouldNotBlock = NuclearProtectionPolicy.shouldBlock(
            result = reelResult,
            nuclearSession = NuclearSession(),
            lockState = LockState(),
            config = ProtectionConfig(),
            nowWallClock = morningMillis,
            nowElapsedRealtime = 60000L,
            habitConfig = activeBedtimeHabits
        )
        assertFalse(shouldNotBlock)
    }

    private fun createTimestamp(hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
