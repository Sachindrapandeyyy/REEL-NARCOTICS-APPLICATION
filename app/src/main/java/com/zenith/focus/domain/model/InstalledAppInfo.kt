package com.zenith.focus.domain.model

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    val isLocked: Boolean = false,
    val lockMode: AppLockMode? = null
)
