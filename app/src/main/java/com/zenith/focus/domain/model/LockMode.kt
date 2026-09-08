package com.zenith.focus.domain.model

enum class LockMode(val displayName: String) {
    QUICK("Quick Lock"),
    CUSTOM("Custom Lock"),
    UNTIL_TOMORROW("Until Tomorrow"),
    SCHEDULED("Scheduled Focus"),
    DAILY_LIMIT("Daily Limit")
}
