package com.zenith.focus.feature.psychology

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

    fun startTimeBox(packageName: String, durationMinutes: Int, purpose: String): TimeBoxSession {
        val now = elapsedRealtimeProvider()
        val durationMs = durationMinutes * 60_000L
        val session = TimeBoxSession(
            packageName = packageName,
            durationMs = durationMs,
            purpose = purpose,
            startTimeRealtime = now,
            endTimeRealtime = now + durationMs
        )
        sessions[packageName] = session
        return session
    }

    fun isCurrentlyActive(packageName: String): Boolean {
        val session = sessions[packageName] ?: return false
        val now = elapsedRealtimeProvider()
        if (session.isExpired(now)) {
            sessions.remove(packageName)
            return false
        }
        return true
    }

    fun getRemainingMillis(packageName: String): Long {
        val session = sessions[packageName] ?: return 0L
        val now = elapsedRealtimeProvider()
        val remaining = session.remainingMs(now)
        if (remaining <= 0L) {
            sessions.remove(packageName)
            return 0L
        }
        return remaining
    }

    fun getProgressFraction(packageName: String): Float {
        val session = sessions[packageName] ?: return 0f
        val now = elapsedRealtimeProvider()
        if (session.isExpired(now)) {
            sessions.remove(packageName)
            return 1.0f
        }
        return session.progressFraction(now)
    }

    fun getActiveSession(packageName: String): TimeBoxSession? {
        val session = sessions[packageName] ?: return null
        val now = elapsedRealtimeProvider()
        if (session.isExpired(now)) {
            sessions.remove(packageName)
            return null
        }
        return session
    }

    fun clearSession(packageName: String) {
        sessions.remove(packageName)
    }

    fun clearAll() {
        sessions.clear()
    }
}
