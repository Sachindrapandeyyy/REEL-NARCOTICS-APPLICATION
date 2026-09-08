package com.zenith.focus.core

import com.zenith.focus.core.security.PinHasher
import com.zenith.focus.core.time.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TimeAndSecurityTest {

    @Test
    fun testFormatRemainingHoursMinutesSeconds() {
        assertEquals("01:30:15", DateTimeUtils.formatRemaining(5415000L))
        assertEquals("00:05:00", DateTimeUtils.formatRemaining(300000L))
        assertEquals("00:00:00", DateTimeUtils.formatRemaining(0L))
        assertEquals("00:00:00", DateTimeUtils.formatRemaining(-500L))
    }

    @Test
    fun testFormatRemainingMultiDay() {
        // 2 days, 3 hours, 10 minutes
        val millis = (2 * 24 * 3600 + 3 * 3600 + 10 * 60) * 1000L
        val formatted = DateTimeUtils.formatRemaining(millis)
        assertTrue(formatted.contains("2d"))
        assertTrue(formatted.contains("03h"))
    }

    @Test
    fun testPinHasherHashingAndVerification() {
        val salt = PinHasher.generateSalt()
        assertTrue(salt.isNotBlank())
        assertEquals(32, salt.length) // 16 bytes = 32 hex chars

        val pin = "7492"
        val hash = PinHasher.hashPin(pin, salt)
        assertTrue(hash.isNotBlank())

        // Verification success
        assertTrue(PinHasher.verifyPin("7492", hash, salt))

        // Verification failure
        assertFalse(PinHasher.verifyPin("0000", hash, salt))
        assertFalse(PinHasher.verifyPin("749", hash, salt))
        assertFalse(PinHasher.verifyPin("", hash, salt))
    }

    @Test
    fun testNextDayBoundary() {
        val tz = TimeZone.getTimeZone("UTC")
        val boundary = DateTimeUtils.getNextDayBoundary(tz, targetHour = 4)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = boundary }
        assertEquals(4, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
    }
}
