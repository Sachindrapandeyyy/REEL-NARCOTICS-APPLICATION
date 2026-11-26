package com.zenith.focus.feature.psychology

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class TimeBoxSession(
    val packageName: String,
    val durationMs: Long,
    val purpose: String,
    val startTimeRealtime: Long,
    val endTimeRealtime: Long
) {
    fun remainingMs(currentRealtime: Long): Long {
        return (endTimeRealtime - currentRealtime).coerceAtLeast(0L)
    }

    fun isExpired(currentRealtime: Long): Boolean {
        return currentRealtime >= endTimeRealtime
    }

    fun progressFraction(currentRealtime: Long): Float {
        if (durationMs <= 0L) return 1.0f
        val elapsed = (currentRealtime - startTimeRealtime).coerceIn(0L, durationMs)
        return (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0.0f, 1.0f)
    }
}

/**
 * Manages intentional time-boxed browsing sessions to cure time blindness.
 * Uses monotonic clock (elapsedRealtime) to prevent system clock tampering.
 */
class IntentTimeBoxManager(
    private val elapsedRealtimeProvider: () -> Long = { android.os.SystemClock.elapsedRealtime() }
) {
    private val sessions = ConcurrentHashMap<String, TimeBoxSession>()

    private fun keyOf(packageName: String): String = packageName.lowercase(Locale.US)

    fun startTimeBox(packageName: String, durationMinutes: Int, purpose: String): TimeBoxSession {
        val key = keyOf(packageName)
        val now = elapsedRealtimeProvider()
        val durationMs = durationMinutes * 60_000L
        val session = TimeBoxSession(
            packageName = key,
            durationMs = durationMs,
            purpose = purpose,
            startTimeRealtime = now,
            endTimeRealtime = now + durationMs
        )
        sessions[key] = session
        return session
    }

    fun isCurrentlyActive(packageName: String): Boolean {
        return getActiveSession(packageName) != null
    }

    fun getRemainingMillis(packageName: String): Long {
        val session = getActiveSession(packageName) ?: return 0L
        return session.remainingMs(elapsedRealtimeProvider())
    }

    fun getProgressFraction(packageName: String): Float {
        val session = getActiveSession(packageName) ?: return 0f
        return session.progressFraction(elapsedRealtimeProvider())
    }

    fun getActiveSession(packageName: String): TimeBoxSession? {
        val key = keyOf(packageName)
        val session = sessions[key] ?: return null
        val now = elapsedRealtimeProvider()
        // Do not remove expired session here so consumeRecentlyExpiredSession can handle foreground ejection once.
        return if (session.isExpired(now)) null else session
    }

    /**
     * Atomically consumes an expired session if one exists.
     * Returns the session only if it expired within [maxStaleMs] so foreground active timers
     * notify and eject immediately, while hours-old stale sessions are silently cleared.
     */
    fun consumeRecentlyExpiredSession(
        packageName: String,
        maxStaleMs: Long = 120_000L
    ): TimeBoxSession? {
        val key = keyOf(packageName)
        val session = sessions[key] ?: return null
        val now = elapsedRealtimeProvider()
        if (!session.isExpired(now)) return null
        sessions.remove(key, session)
        val overdueMs = now - session.endTimeRealtime
        return if (overdueMs in 0L..maxStaleMs) session else null
    }

    fun clearSession(packageName: String) {
        sessions.remove(keyOf(packageName))
    }

    fun clearAll() {
        sessions.clear()
    }
}
