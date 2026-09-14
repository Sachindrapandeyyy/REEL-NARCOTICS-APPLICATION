package com.zenith.focus.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.zenith.focus.receiver.LockAlarmReceiver
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zenith.focus.domain.model.LockMode
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.repository.LockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.TimeZone

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zenith_lock_preferences")

class LockRepositoryImpl(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : LockRepository {

    companion object {
        private val KEY_IS_ACTIVE = booleanPreferencesKey("lock_is_active")
        private val KEY_START_TIME = longPreferencesKey("lock_start_time")
        private val KEY_END_TIME = longPreferencesKey("lock_end_time")
        private val KEY_TIMEZONE = stringPreferencesKey("lock_timezone")
        private val KEY_MODE = stringPreferencesKey("lock_mode")
        private val KEY_LABEL = stringPreferencesKey("lock_label")
    }

    private val _lockState = MutableStateFlow(LockState())
    override val lockState: StateFlow<LockState> = _lockState.asStateFlow()

    init {
        externalScope.launch {
            loadInitialState()
        }
    }

    private suspend fun loadInitialState() {
        val prefs = context.dataStore.data.first()
        val isActive = prefs[KEY_IS_ACTIVE] ?: false
        val startTime = prefs[KEY_START_TIME] ?: 0L
        val endTime = prefs[KEY_END_TIME] ?: 0L
        val timeZoneId = prefs[KEY_TIMEZONE] ?: TimeZone.getDefault().id
        val modeStr = prefs[KEY_MODE] ?: LockMode.QUICK.name
        val label = prefs[KEY_LABEL] ?: "Focus Lock"

        val mode = runCatching { LockMode.valueOf(modeStr) }.getOrDefault(LockMode.QUICK)
        val now = System.currentTimeMillis()

        // Auto-expire if time has passed
        val actualActive = isActive && now < endTime
        val state = LockState(
            isActive = actualActive,
            startTimeMillis = startTime,
            endTimeMillis = endTime,
            timeZoneId = timeZoneId,
            mode = mode,
            label = label
        )
        _lockState.value = state

        if (isActive && !actualActive) {
            // Persist expired state
            context.dataStore.edit { p ->
                p[KEY_IS_ACTIVE] = false
            }
        }
    }

    override suspend fun startLock(durationMillis: Long, mode: LockMode, label: String) {
        val now = System.currentTimeMillis()
        val end = now + durationMillis
        startLockUntil(end, mode, label)
    }

    override suspend fun startLockUntil(targetTimestampMillis: Long, mode: LockMode, label: String) {
        val now = System.currentTimeMillis()
        val tzId = TimeZone.getDefault().id

        context.dataStore.edit { prefs ->
            prefs[KEY_IS_ACTIVE] = true
            prefs[KEY_START_TIME] = now
            prefs[KEY_END_TIME] = targetTimestampMillis
            prefs[KEY_TIMEZONE] = tzId
            prefs[KEY_MODE] = mode.name
            prefs[KEY_LABEL] = label
        }

        _lockState.value = LockState(
            isActive = true,
            startTimeMillis = now,
            endTimeMillis = targetTimestampMillis,
            timeZoneId = tzId,
            mode = mode,
            label = label
        )

        // Schedule system wakeup alarm for lock expiration
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, LockAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.set(AlarmManager.RTC_WAKEUP, targetTimestampMillis, pendingIntent)
    }

    override suspend fun endLock() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_ACTIVE] = false
        }
        _lockState.value = _lockState.value.copy(isActive = false)

        // Cancel scheduled alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, LockAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.cancel(pendingIntent)
    }

    override suspend fun refreshLockState() {
        val current = _lockState.value
        val now = System.currentTimeMillis()
        if (current.isActive && now >= current.endTimeMillis) {
            endLock()
        }
    }
}
