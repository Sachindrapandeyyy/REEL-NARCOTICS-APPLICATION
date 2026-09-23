package com.zenith.focus.domain.model

enum class AppLockMode(val displayName: String, val description: String) {
    NUCLEAR_ONLY("Nuclear Mode", "Locked strictly during active Nuclear sessions"),
    PERMANENT("Permanent 24/7", "Locked continuously around the clock"),
    BOTH("Both (Nuclear & 24/7)", "Permanently locked across all modes")
}
