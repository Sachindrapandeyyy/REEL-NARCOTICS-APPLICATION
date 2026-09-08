package com.zenith.focus.domain.repository

import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.FrictionType
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.model.ScheduleConfig
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val protectionConfig: StateFlow<ProtectionConfig>
    val schedules: StateFlow<List<ScheduleConfig>>
    val isOnboardingCompleted: StateFlow<Boolean>
    val appTheme: StateFlow<String> // "SYSTEM", "LIGHT", "DARK"

    suspend fun updateCategory(category: ContentCategory, isBlocked: Boolean)
    suspend fun updateProtectionConfig(config: ProtectionConfig)
    suspend fun setStrictMode(enabled: Boolean)
    suspend fun setFrictionType(frictionType: FrictionType)
    suspend fun setPin(pin: String)
    suspend fun clearPin()
    suspend fun verifyPin(pin: String): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setAppTheme(theme: String)
    suspend fun saveSchedule(schedule: ScheduleConfig)
    suspend fun deleteSchedule(id: String)
}
