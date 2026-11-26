package com.zenith.focus.feature.psychology

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the mindful pause state to break autopilot muscle-memory app opens.
 * When the user mindfully passes or sets a time box, it remembers this for the duration
 * of continuous foreground use, resetting once the user leaves the app.
 */
class MindfulPauseManager(
    private val monitoredPackages: Set<String> = DEFAULT_MONITORED_PACKAGES,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {
    companion object {
        private const val TRIGGER_COOLDOWN_MS = 2_500L

        val DEFAULT_MONITORED_PACKAGES = setOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.snapchat.android",
            "com.facebook.katana",
            "com.reddit.frontpage"
        )

        private fun isTransientSystemOrImePackage(lower: String): Boolean {
            return lower.isBlank() ||
                lower == "com.zenith.focus" ||
                lower == "android" ||
                lower.contains("systemui") ||
                lower.contains("inputmethod") ||
                lower.contains("keyboard") ||
                lower.contains("facemoji") ||
                lower.contains("gboard") ||
                lower.contains("swiftkey") ||
                lower.contains("honeyboard") ||
                lower.contains("permissioncontroller") ||
                lower.contains("packageinstaller") ||
                lower.contains("intentresolver") ||
                lower.contains("smartpanel") ||
                lower.contains("screenshot") ||
                lower.contains("accessibility")
        }
    }

    private val passedPackages = ConcurrentHashMap.newKeySet<String>()
    private val lastTriggerTimes = ConcurrentHashMap<String, Long>()
    private val closedCooldownUntil = ConcurrentHashMap<String, Long>()

    @Volatile
    private var currentForegroundPackage: String? = null

    fun isMonitoredPackage(packageName: String): Boolean {
        val lower = packageName.lowercase(Locale.US).trim()
        if (lower.isBlank()) return false
        return monitoredPackages.any { monitored ->
            lower == monitored || lower.startsWith("$monitored.")
        }
    }

    fun shouldTriggerPause(
        packageName: String,
        isFeatureEnabled: Boolean,
        hasActiveTimeBox: Boolean
    ): Boolean {
        if (!isFeatureEnabled) return false
        if (!isMonitoredPackage(packageName)) return false
        if (hasActiveTimeBox) return false
        val lower = packageName.lowercase(Locale.US)
        if (passedPackages.contains(lower)) return false

        val now = timeProvider()
        val closedUntil = closedCooldownUntil[lower] ?: 0L
        if (now < closedUntil) return false

        val lastTrigger = lastTriggerTimes[lower] ?: 0L
        if (lastTrigger > 0L && now - lastTrigger < TRIGGER_COOLDOWN_MS) return false

        return true
    }

    fun tryLockTrigger(packageName: String): Boolean {
        val lower = packageName.lowercase(Locale.US)
        val now = timeProvider()
        val prev = lastTriggerTimes[lower] ?: 0L
        if (prev > 0L && now - prev < TRIGGER_COOLDOWN_MS) {
            return false
        }
        lastTriggerTimes[lower] = now
        return true
    }

    fun markClosedRecently(packageName: String) {
        val lower = packageName.lowercase(Locale.US)
        passedPackages.remove(lower)
        closedCooldownUntil[lower] = timeProvider() + TRIGGER_COOLDOWN_MS
    }

    fun markPassed(packageName: String) {
        passedPackages.add(packageName.lowercase(Locale.US))
    }

    fun onForegroundPackageChanged(newPackage: String) {
        val lower = newPackage.lowercase(Locale.US)
        if (isTransientSystemOrImePackage(lower)) {
            return
        }
        if (lower != currentForegroundPackage) {
            val previous = currentForegroundPackage
            currentForegroundPackage = lower
            if (previous != null && previous != lower && !isMonitoredPackage(lower)) {
                passedPackages.remove(previous)
            }
        }
    }

    fun clearPassed(packageName: String) {
        passedPackages.remove(packageName.lowercase(Locale.US))
    }

    fun clearAll() {
        passedPackages.clear()
        lastTriggerTimes.clear()
        closedCooldownUntil.clear()
        currentForegroundPackage = null
    }
}
