package com.zenith.focus.domain.model

data class LockedAppRule(
    val packageName: String,
    val appName: String,
    val lockMode: AppLockMode = AppLockMode.PERMANENT,
    val addedTimestamp: Long = System.currentTimeMillis()
)
