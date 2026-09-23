package com.zenith.focus.domain.repository

import com.zenith.focus.domain.model.AppLockConfig
import com.zenith.focus.domain.model.AppLockMode
import com.zenith.focus.domain.model.InstalledAppInfo
import com.zenith.focus.domain.model.LockedAppRule
import kotlinx.coroutines.flow.StateFlow

interface AppLockRepository {
    val appLockConfig: StateFlow<AppLockConfig>

    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun lockApp(packageName: String, appName: String, mode: AppLockMode)
    suspend fun lockApps(apps: List<Pair<String, String>>, mode: AppLockMode)
    suspend fun updateAppLockMode(packageName: String, mode: AppLockMode)
    suspend fun unlockApp(packageName: String): Boolean
    suspend fun getInstalledApps(): List<InstalledAppInfo>
}
