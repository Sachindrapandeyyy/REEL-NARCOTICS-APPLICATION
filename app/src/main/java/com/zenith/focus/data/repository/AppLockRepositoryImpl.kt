package com.zenith.focus.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zenith.focus.accessibility.service.ZenithAccessibilityService
import com.zenith.focus.domain.model.AppLockConfig
import com.zenith.focus.domain.model.AppLockMode
import com.zenith.focus.domain.model.InstalledAppInfo
import com.zenith.focus.domain.model.LockedAppRule
import com.zenith.focus.domain.repository.AppLockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

private val Context.appLockDataStore: DataStore<Preferences> by preferencesDataStore(name = "zenith_app_lock_preferences")

class AppLockRepositoryImpl(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AppLockRepository {

    companion object {
        private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val KEY_LOCKED_APPS_JSON = stringPreferencesKey("locked_apps_json")

        fun serializeRules(rules: Map<String, LockedAppRule>): String {
            val jsonArray = JSONArray()
            rules.values.forEach { rule ->
                val obj = JSONObject().apply {
                    put("pkg", rule.packageName)
                    put("name", rule.appName)
                    put("mode", rule.lockMode.name)
                    put("time", rule.addedTimestamp)
                }
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }

        fun deserializeRules(json: String?): Map<String, LockedAppRule> {
            if (json.isNullOrBlank()) return emptyMap()
            val result = mutableMapOf<String, LockedAppRule>()
            runCatching {
                val jsonArray = JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val pkg = obj.optString("pkg")
                    val name = obj.optString("name", pkg)
                    val modeStr = obj.optString("mode", AppLockMode.PERMANENT.name)
                    val mode = runCatching { AppLockMode.valueOf(modeStr) }.getOrDefault(AppLockMode.PERMANENT)
                    val time = obj.optLong("time", System.currentTimeMillis())
                    if (pkg.isNotBlank()) {
                        result[pkg.lowercase(Locale.US)] = LockedAppRule(
                            packageName = pkg,
                            appName = name,
                            lockMode = mode,
                            addedTimestamp = time
                        )
                    }
                }
            }
            return result
        }
    }

    private val _appLockConfig = MutableStateFlow(AppLockConfig())
    override val appLockConfig: StateFlow<AppLockConfig> = _appLockConfig.asStateFlow()

    init {
        externalScope.launch {
            context.appLockDataStore.data
                .catch { emit(emptyPreferences()) }
                .collect { prefs ->
                    val isEnabled = prefs[KEY_APP_LOCK_ENABLED] ?: true
                    val rulesJson = prefs[KEY_LOCKED_APPS_JSON]
                    val rules = deserializeRules(rulesJson)
                    _appLockConfig.value = AppLockConfig(
                        isAppLockEnabled = isEnabled,
                        lockedApps = rules
                    )
                }
        }
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        _appLockConfig.value = _appLockConfig.value.copy(isAppLockEnabled = enabled)
        context.appLockDataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_ENABLED] = enabled
        }
    }

    override suspend fun lockApp(packageName: String, appName: String, mode: AppLockMode) {
        val normalizedPkg = packageName.trim().lowercase(Locale.US)
        val updatedMap = _appLockConfig.value.lockedApps.toMutableMap()
        updatedMap[normalizedPkg] = LockedAppRule(
            packageName = packageName.trim(),
            appName = appName.trim(),
            lockMode = mode,
            addedTimestamp = System.currentTimeMillis()
        )
        _appLockConfig.value = _appLockConfig.value.copy(lockedApps = updatedMap)
        saveRulesToDataStore(updatedMap)
    }

    override suspend fun lockApps(apps: List<Pair<String, String>>, mode: AppLockMode) {
        val updatedMap = _appLockConfig.value.lockedApps.toMutableMap()
        val now = System.currentTimeMillis()
        for ((pkg, name) in apps) {
            val normalizedPkg = pkg.trim().lowercase(Locale.US)
            updatedMap[normalizedPkg] = LockedAppRule(
                packageName = pkg.trim(),
                appName = name.trim(),
                lockMode = mode,
                addedTimestamp = now
            )
        }
        _appLockConfig.value = _appLockConfig.value.copy(lockedApps = updatedMap)
        saveRulesToDataStore(updatedMap)
    }

    override suspend fun updateAppLockMode(packageName: String, mode: AppLockMode) {
        val normalizedPkg = packageName.trim().lowercase(Locale.US)
        val existing = _appLockConfig.value.lockedApps[normalizedPkg] ?: return
        val updatedMap = _appLockConfig.value.lockedApps.toMutableMap()
        updatedMap[normalizedPkg] = existing.copy(lockMode = mode)
        _appLockConfig.value = _appLockConfig.value.copy(lockedApps = updatedMap)
        saveRulesToDataStore(updatedMap)
    }

    override suspend fun unlockApp(packageName: String): Boolean {
        val normalizedPkg = packageName.trim().lowercase(Locale.US)
        if (!_appLockConfig.value.lockedApps.containsKey(normalizedPkg)) {
            return false
        }
        val updatedMap = _appLockConfig.value.lockedApps.toMutableMap()
        updatedMap.remove(normalizedPkg)
        _appLockConfig.value = _appLockConfig.value.copy(lockedApps = updatedMap)
        saveRulesToDataStore(updatedMap)
        return true
    }

    private suspend fun saveRulesToDataStore(rules: Map<String, LockedAppRule>) {
        val json = serializeRules(rules)
        context.appLockDataStore.edit { prefs ->
            prefs[KEY_LOCKED_APPS_JSON] = json
        }
    }

    override suspend fun getInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(mainIntent, 0)

        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val homePackages = pm.queryIntentActivities(homeIntent, 0)
            .mapNotNull { it.activityInfo?.packageName?.lowercase(Locale.US) }
            .toSet()

        val selfPackage = context.packageName.lowercase(Locale.US)
        val currentLocked = _appLockConfig.value.lockedApps

        val resultList = mutableListOf<InstalledAppInfo>()
        val seenPackages = mutableSetOf<String>()

        for (resolveInfo in resolveInfoList) {
            val pkg = resolveInfo.activityInfo?.packageName ?: continue
            val lowerPkg = pkg.lowercase(Locale.US)

            // Never include self, home launchers, SystemUI, or essential utilities in the picker
            if (lowerPkg == selfPackage ||
                homePackages.contains(lowerPkg) ||
                lowerPkg.contains("systemui") ||
                lowerPkg == "com.android.settings" ||
                ZenithAccessibilityService.isEssentialUtility(lowerPkg) ||
                seenPackages.contains(lowerPkg)
            ) {
                continue
            }

            seenPackages.add(lowerPkg)
            val appLabel = resolveInfo.loadLabel(pm)?.toString() ?: pkg
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val lockedRule = currentLocked[lowerPkg]

            resultList.add(
                InstalledAppInfo(
                    packageName = pkg,
                    appName = appLabel,
                    isSystemApp = isSystem,
                    isLocked = lockedRule != null,
                    lockMode = lockedRule?.lockMode
                )
            )
        }

        resultList.sortedWith(compareBy({ !it.isLocked }, { it.appName.lowercase(Locale.US) }))
    }
}
