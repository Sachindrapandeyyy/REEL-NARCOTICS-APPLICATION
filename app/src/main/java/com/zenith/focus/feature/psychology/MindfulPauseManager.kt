package com.zenith.focus.feature.psychology

import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the mindful pause state to break autopilot muscle-memory app opens.
 * When the user mindfully passes or sets a time box, it remembers this for the duration
 * of continuous foreground use, resetting once the user leaves the app.
 */
class MindfulPauseManager(
    private val monitoredPackages: Set<String> = DEFAULT_MONITORED_PACKAGES
) {
    companion object {
        val DEFAULT_MONITORED_PACKAGES = setOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.snapchat.android",
            "com.facebook.katana",
            "com.reddit.frontpage"
        )
    }

    private val passedPackages = ConcurrentHashMap.newKeySet<String>()
    private var currentForegroundPackage: String? = null

    fun isMonitoredPackage(packageName: String): Boolean {
        val lower = packageName.lowercase(java.util.Locale.US)
        return monitoredPackages.any { lower.contains(it) || it.contains(lower) }
    }

    fun shouldTriggerPause(
        packageName: String,
        isFeatureEnabled: Boolean,
        hasActiveTimeBox: Boolean
    ): Boolean {
        if (!isFeatureEnabled) return false
        if (!isMonitoredPackage(packageName)) return false
        if (hasActiveTimeBox) return false
        val lower = packageName.lowercase(java.util.Locale.US)
        return !passedPackages.contains(lower)
    }

    fun markPassed(packageName: String) {
        passedPackages.add(packageName.lowercase(java.util.Locale.US))
    }

    fun onForegroundPackageChanged(newPackage: String) {
        val lower = newPackage.lowercase(java.util.Locale.US)
        if (lower != currentForegroundPackage) {
            val previous = currentForegroundPackage
            currentForegroundPackage = lower
            if (previous != null && previous != lower && !isMonitoredPackage(lower)) {
                passedPackages.remove(previous)
            }
        }
    }

    fun clearPassed(packageName: String) {
        passedPackages.remove(packageName.lowercase(java.util.Locale.US))
    }

    fun clearAll() {
        passedPackages.clear()
        currentForegroundPackage = null
    }
}
