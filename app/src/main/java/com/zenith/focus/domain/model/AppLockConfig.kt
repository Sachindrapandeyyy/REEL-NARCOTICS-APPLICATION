package com.zenith.focus.domain.model

data class AppLockConfig(
    val isAppLockEnabled: Boolean = true,
    val lockedApps: Map<String, LockedAppRule> = emptyMap()
) {
    fun isPackageLocked(
        packageName: String,
        isNuclearActive: Boolean
    ): Boolean {
        val rule = lockedApps[packageName.lowercase(java.util.Locale.US)] ?: return false
        // During nuclear mode, ALL locked apps are strictly enforced with zero bypass
        if (isNuclearActive) {
            return true
        }
        if (!isAppLockEnabled) return false
        return when (rule.lockMode) {
            AppLockMode.PERMANENT -> true
            AppLockMode.NUCLEAR_ONLY -> false
            AppLockMode.BOTH -> true
        }
    }

    fun getRule(packageName: String): LockedAppRule? {
        return lockedApps[packageName.lowercase(java.util.Locale.US)]
    }

    val totalCount: Int get() = lockedApps.size
    val permanentCount: Int get() = lockedApps.values.count { it.lockMode == AppLockMode.PERMANENT || it.lockMode == AppLockMode.BOTH }
    val nuclearCount: Int get() = lockedApps.values.count { it.lockMode == AppLockMode.NUCLEAR_ONLY || it.lockMode == AppLockMode.BOTH }
}
