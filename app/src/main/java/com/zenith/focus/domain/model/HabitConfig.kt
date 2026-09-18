package com.zenith.focus.domain.model

import java.util.Calendar

data class HabitConfig(
    val morningPledgeEnabled: Boolean = true,
    val morningPledgeHour: Int = 8,
    val morningPledgeMinute: Int = 0,
    val eveningSummaryEnabled: Boolean = true,
    val eveningSummaryHour: Int = 21,
    val eveningSummaryMinute: Int = 0,
    val bedtimeShieldEnabled: Boolean = false,
    val bedtimeStartHour: Int = 23,
    val bedtimeStartMinute: Int = 0,
    val bedtimeEndHour: Int = 6,
    val bedtimeEndMinute: Int = 30
) {
    /**
     * Evaluates whether a given wall-clock timestamp falls within the Bedtime Sleep Shield window.
     * Accurately handles overnight windows (e.g. 23:00 to 06:30) and same-day windows.
     */
    fun isBedtimeActive(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!bedtimeShieldEnabled) return false

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMinutes = bedtimeStartHour * 60 + bedtimeStartMinute
        val endMinutes = bedtimeEndHour * 60 + bedtimeEndMinute

        return if (startMinutes < endMinutes) {
            // Same-day interval (e.g. 13:00 to 17:00)
            currentMinutes in startMinutes until endMinutes
        } else if (startMinutes > endMinutes) {
            // Overnight interval (e.g. 23:00 to 06:30)
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        } else {
            // Start equals end: treated as active throughout
            true
        }
    }
}
