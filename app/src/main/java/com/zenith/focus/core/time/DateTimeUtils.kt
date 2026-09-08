package com.zenith.focus.core.time

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {
    fun formatRemaining(remainingMillis: Long): String {
        if (remainingMillis <= 0L) return "00:00:00"
        val totalSeconds = remainingMillis / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return if (hours >= 24) {
            val days = hours / 24
            val remHours = hours % 24
            String.format(Locale.US, "%dd %02dh %02dm", days, remHours, minutes)
        } else {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    fun formatRemainingShort(remainingMillis: Long): String {
        if (remainingMillis <= 0L) return "0s"
        val totalSeconds = remainingMillis / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return when {
            hours >= 24 -> String.format(Locale.US, "%dd %dh", hours / 24, hours % 24)
            hours > 0 -> String.format(Locale.US, "%dh %dm", hours, minutes)
            minutes > 0 -> String.format(Locale.US, "%dm %ds", minutes, seconds)
            else -> String.format(Locale.US, "%ds", seconds)
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatHourMinute(hour: Int, minute: Int): String {
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }

    fun getStartOfToday(timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = Calendar.getInstance(timeZone)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getNextDayBoundary(timeZone: TimeZone = TimeZone.getDefault(), targetHour: Int = 4): Long {
        val cal = Calendar.getInstance(timeZone)
        if (cal.get(Calendar.HOUR_OF_DAY) >= targetHour) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        cal.set(Calendar.HOUR_OF_DAY, targetHour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "GOOD MORNING"
            in 12..16 -> "GOOD AFTERNOON"
            in 17..21 -> "GOOD EVENING"
            else -> "LATE NIGHT FOCUS"
        }
    }
}
