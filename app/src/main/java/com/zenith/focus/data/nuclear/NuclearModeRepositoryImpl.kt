package com.zenith.focus.data.nuclear

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.zenith.focus.receiver.LockAlarmReceiver
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zenith.focus.domain.nuclear.NuclearModeRepository
import com.zenith.focus.domain.nuclear.NuclearSession
import com.zenith.focus.domain.nuclear.NuclearSessionStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

private val Context.nuclearDataStore: DataStore<Preferences> by preferencesDataStore(name = "zenith_nuclear_preferences")

class NuclearModeRepositoryImpl(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : NuclearModeRepository {

    companion object {
        private val KEY_SESSION_ID = stringPreferencesKey("nuclear_id")
        private val KEY_START_TIME = longPreferencesKey("nuclear_start_time")
        private val KEY_END_TIME = longPreferencesKey("nuclear_end_time")
        private val KEY_START_ELAPSED = longPreferencesKey("nuclear_start_elapsed")
        private val KEY_DURATION = longPreferencesKey("nuclear_duration")
        private val KEY_STATUS = stringPreferencesKey("nuclear_status")
        private val KEY_CREATED_AT = longPreferencesKey("nuclear_created_at")
        private val KEY_BLOCKED_AT_START = intPreferencesKey("nuclear_blocked_at_start")
        private val KEY_ENABLED_CATEGORIES = androidx.datastore.preferences.core.stringSetPreferencesKey("nuclear_enabled_categories")
    }

    private val isInitialized = CompletableDeferred<Unit>()
    private val _session = MutableStateFlow(NuclearSession())
    override val session: StateFlow<NuclearSession> = _session.asStateFlow()

    init {
        externalScope.launch {
            loadInitialState()
        }
    }

    suspend fun ensureInitialized() {
        isInitialized.await()
    }

    private suspend fun loadInitialState() {
        try {
            val prefs = context.nuclearDataStore.data.first()
        val id = prefs[KEY_SESSION_ID] ?: UUID.randomUUID().toString()
        val startTime = prefs[KEY_START_TIME] ?: 0L
        val endTime = prefs[KEY_END_TIME] ?: 0L
        val startElapsed = prefs[KEY_START_ELAPSED] ?: 0L
        val duration = prefs[KEY_DURATION] ?: 0L
        val statusStr = prefs[KEY_STATUS] ?: NuclearSessionStatus.INACTIVE.name
        val createdAt = prefs[KEY_CREATED_AT] ?: System.currentTimeMillis()
        val blockedAtStart = prefs[KEY_BLOCKED_AT_START] ?: 0
        val savedCategories = prefs[KEY_ENABLED_CATEGORIES]?.mapNotNull { name ->
            runCatching { com.zenith.focus.domain.model.ContentCategory.valueOf(name) }.getOrNull()
        }?.toSet() ?: com.zenith.focus.domain.model.ContentCategory.values().toSet()

        val persistedStatus = runCatching {
            NuclearSessionStatus.valueOf(statusStr)
        }.getOrDefault(NuclearSessionStatus.INACTIVE)

        var loadedSession = NuclearSession(
            id = id,
            startTimeMillis = startTime,
            endTimeMillis = endTime,
            startElapsedRealtime = startElapsed,
            durationMillis = duration,
            status = persistedStatus,
            createdAt = createdAt,
            blockedCountAtStart = blockedAtStart,
            enabledCategories = savedCategories
        )

        // Evaluate expiry if ACTIVE
        if (loadedSession.status == NuclearSessionStatus.ACTIVE) {
            val now = System.currentTimeMillis()
            val elapsedRealtime = SystemClock.elapsedRealtime()

            if (!loadedSession.isCurrentlyActive(now, elapsedRealtime)) {
                // Session expired while app was dead
                loadedSession = loadedSession.copy(status = NuclearSessionStatus.EXPIRED)
                persistSession(loadedSession)
            }
        }

        _session.value = loadedSession
        } finally {
            isInitialized.complete(Unit)
        }
    }

    override suspend fun armSession(
        durationMillis: Long,
        currentBlockedCount: Int,
        enabledCategories: Set<com.zenith.focus.domain.model.ContentCategory>
    ) {
        ensureInitialized()
        val current = _session.value
        // If already active, NEVER allow re-arming or mutating
        if (current.status == NuclearSessionStatus.ACTIVE) return

        val armed = NuclearSession(
            id = UUID.randomUUID().toString(),
            durationMillis = durationMillis,
            status = NuclearSessionStatus.ARMING,
            createdAt = System.currentTimeMillis(),
            blockedCountAtStart = currentBlockedCount,
            enabledCategories = enabledCategories
        )
        _session.value = armed
    }

    override suspend fun activateArmedSession(): Boolean {
        ensureInitialized()
        val current = _session.value
        if (current.status != NuclearSessionStatus.ARMING) {
            return false
        }

        val now = System.currentTimeMillis()
        val elapsed = SystemClock.elapsedRealtime()
        val end = now + current.durationMillis

        val activeSession = current.copy(
            startTimeMillis = now,
            endTimeMillis = end,
            startElapsedRealtime = elapsed,
            status = NuclearSessionStatus.ACTIVE
        )

        persistSession(activeSession)
        _session.value = activeSession
        scheduleNuclearAlarm(end)
        return true
    }

    override suspend fun cancelArming(): Boolean {
        ensureInitialized()
        val current = _session.value
        // CRITICAL SECURITY RULE: You cannot cancel an ACTIVE session
        if (current.status == NuclearSessionStatus.ACTIVE) {
            return false
        }

        val inactive = NuclearSession(status = NuclearSessionStatus.INACTIVE)
        persistSession(inactive)
        _session.value = inactive
        return true
    }

    override suspend fun checkAndUpdateExpiration(): Boolean {
        ensureInitialized()
        val current = _session.value
        if (current.status != NuclearSessionStatus.ACTIVE) {
            return false
        }

        val now = System.currentTimeMillis()
        val elapsedRealtime = SystemClock.elapsedRealtime()

        if (!current.isCurrentlyActive(now, elapsedRealtime)) {
            val expired = current.copy(status = NuclearSessionStatus.EXPIRED)
            persistSession(expired)
            _session.value = expired
            return false
        }

        return true
    }

    override suspend fun acknowledgeCompletedSession(): Boolean {
        ensureInitialized()
        val current = _session.value
        if (current.status != NuclearSessionStatus.EXPIRED) {
            return false
        }

        val inactive = NuclearSession(status = NuclearSessionStatus.INACTIVE)
        persistSession(inactive)
        _session.value = inactive
        cancelNuclearAlarm()
        return true
    }

    override suspend fun extendActiveSession(additionalMillis: Long): Boolean {
        ensureInitialized()
        val current = _session.value
        if (current.status != NuclearSessionStatus.ACTIVE || additionalMillis <= 0) {
            return false
        }

        val newEnd = current.endTimeMillis + additionalMillis
        val newDuration = current.durationMillis + additionalMillis
        val extendedSession = current.copy(
            endTimeMillis = newEnd,
            durationMillis = newDuration
        )

        persistSession(extendedSession)
        _session.value = extendedSession
        scheduleNuclearAlarm(newEnd)
        return true
    }

    private fun scheduleNuclearAlarm(endTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nucIntent = Intent(context, LockAlarmReceiver::class.java).apply {
            putExtra("IS_NUCLEAR", true)
        }
        val nucPending = PendingIntent.getBroadcast(
            context,
            2002,
            nucIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.set(AlarmManager.RTC_WAKEUP, endTimeMillis, nucPending)
    }

    private fun cancelNuclearAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nucIntent = Intent(context, LockAlarmReceiver::class.java).apply {
            putExtra("IS_NUCLEAR", true)
        }
        val nucPending = PendingIntent.getBroadcast(
            context,
            2002,
            nucIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.cancel(nucPending)
    }

    override suspend fun onDeviceRebooted() {
        ensureInitialized()
        val current = _session.value
        if (current.status == NuclearSessionStatus.ACTIVE) {
            val now = System.currentTimeMillis()
            if (now >= current.endTimeMillis) {
                val expired = current.copy(status = NuclearSessionStatus.EXPIRED)
                persistSession(expired)
                _session.value = expired
            } else {
                // Device rebooted: monotonic elapsed counter reset to 0.
                // Re-anchor monotonic reference to current elapsedRealtime and remaining duration.
                val remainingWall = current.endTimeMillis - now
                val newElapsed = SystemClock.elapsedRealtime()
                val reanchored = current.copy(
                    startElapsedRealtime = newElapsed,
                    durationMillis = remainingWall
                )
                persistSession(reanchored)
                _session.value = reanchored
            }
        }
    }

    private suspend fun persistSession(session: NuclearSession) {
        context.nuclearDataStore.edit { prefs ->
            prefs[KEY_SESSION_ID] = session.id
            prefs[KEY_START_TIME] = session.startTimeMillis
            prefs[KEY_END_TIME] = session.endTimeMillis
            prefs[KEY_START_ELAPSED] = session.startElapsedRealtime
            prefs[KEY_DURATION] = session.durationMillis
            prefs[KEY_STATUS] = session.status.name
            prefs[KEY_CREATED_AT] = session.createdAt
            prefs[KEY_BLOCKED_AT_START] = session.blockedCountAtStart
            prefs[KEY_ENABLED_CATEGORIES] = session.enabledCategories.map { it.name }.toSet()
        }
    }
}
