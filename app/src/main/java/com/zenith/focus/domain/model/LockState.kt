package com.zenith.focus.domain.model

import java.util.TimeZone

data class LockState(
    val isActive: Boolean = false,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val timeZoneId: String = TimeZone.getDefault().id,
    val mode: LockMode = LockMode.QUICK,
    val label: String = "Focus Lock",
    val enabledCategories: Set<ContentCategory> = emptySet()
) {
    fun isCurrentlyActive(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return isActive && nowMillis < endTimeMillis
    }

    fun remainingMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        if (!isActive) return 0L
        return (endTimeMillis - nowMillis).coerceAtLeast(0L)
    }

    fun progressFraction(nowMillis: Long = System.currentTimeMillis()): Float {
        if (!isActive || endTimeMillis <= startTimeMillis) return 0f
        val elapsed = (nowMillis - startTimeMillis).coerceAtLeast(0L)
        val total = endTimeMillis - startTimeMillis
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}
