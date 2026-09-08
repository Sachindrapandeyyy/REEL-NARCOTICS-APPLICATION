package com.zenith.focus.domain.model

data class ScheduleConfig(
    val id: String,
    val name: String,
    val isEnabled: Boolean,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7) // 1=Mon .. 7=Sun
)
