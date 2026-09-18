package com.zenith.focus.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zenith.focus.core.notification.HabitNotificationScheduler
import com.zenith.focus.core.security.PinHasher
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.FrictionType
import com.zenith.focus.domain.model.HabitConfig
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.model.ScheduleConfig
import com.zenith.focus.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "zenith_settings_preferences")

class SettingsRepositoryImpl(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SettingsRepository {

    companion object {
        private val KEY_BLOCK_SHORTS = booleanPreferencesKey("block_shorts")
        private val KEY_BLOCK_REELS = booleanPreferencesKey("block_reels")
        private val KEY_BLOCK_SPOTLIGHT = booleanPreferencesKey("block_spotlight")
        private val KEY_BLOCK_FB_REELS = booleanPreferencesKey("block_fb_reels")
        private val KEY_BLOCK_TIKTOK = booleanPreferencesKey("block_tiktok")
        private val KEY_BLOCK_OTHER = booleanPreferencesKey("block_other")
        private val KEY_BLOCK_ADULT_SITES = booleanPreferencesKey("block_adult_sites")
        private val KEY_BLOCK_ADULT_KEYWORDS = booleanPreferencesKey("block_adult_keywords")
        private val KEY_BROWSER_PROTECTION = booleanPreferencesKey("browser_protection")
        private val KEY_STRICT_MODE = booleanPreferencesKey("strict_mode")
        private val KEY_NUCLEAR_MODE = booleanPreferencesKey("nuclear_mode")
        private val KEY_FRICTION_TYPE = stringPreferencesKey("friction_type")
        private val KEY_UNLOCK_PHRASE = stringPreferencesKey("unlock_phrase")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
        private val KEY_DAILY_REELS_LIMIT = intPreferencesKey("daily_reels_limit")
        private val KEY_DAILY_SHORTS_LIMIT = intPreferencesKey("daily_shorts_limit")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_APP_THEME = stringPreferencesKey("app_theme")

        private val KEY_MORNING_PLEDGE_ENABLED = booleanPreferencesKey("morning_pledge_enabled")
        private val KEY_MORNING_PLEDGE_HOUR = intPreferencesKey("morning_pledge_hour")
        private val KEY_MORNING_PLEDGE_MIN = intPreferencesKey("morning_pledge_min")
        private val KEY_EVENING_SUMMARY_ENABLED = booleanPreferencesKey("evening_summary_enabled")
        private val KEY_EVENING_SUMMARY_HOUR = intPreferencesKey("evening_summary_hour")
        private val KEY_EVENING_SUMMARY_MIN = intPreferencesKey("evening_summary_min")
        private val KEY_BEDTIME_SHIELD_ENABLED = booleanPreferencesKey("bedtime_shield_enabled")
        private val KEY_BEDTIME_START_HOUR = intPreferencesKey("bedtime_start_hour")
        private val KEY_BEDTIME_START_MIN = intPreferencesKey("bedtime_start_min")
        private val KEY_BEDTIME_END_HOUR = intPreferencesKey("bedtime_end_hour")
        private val KEY_BEDTIME_END_MIN = intPreferencesKey("bedtime_end_min")
    }

    private val _protectionConfig = MutableStateFlow(ProtectionConfig())
    override val protectionConfig: StateFlow<ProtectionConfig> = _protectionConfig.asStateFlow()

    private val _schedules = MutableStateFlow<List<ScheduleConfig>>(emptyList())
    override val schedules: StateFlow<List<ScheduleConfig>> = _schedules.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    override val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _appTheme = MutableStateFlow("SYSTEM")
    override val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _habitConfig = MutableStateFlow(HabitConfig())
    override val habitConfig: StateFlow<HabitConfig> = _habitConfig.asStateFlow()

    init {
        externalScope.launch {
            loadSettings()
        }
    }

    private suspend fun loadSettings() {
        val prefs = context.settingsDataStore.data.first()
        val friction = runCatching {
            FrictionType.valueOf(prefs[KEY_FRICTION_TYPE] ?: FrictionType.HOLD_BUTTON.name)
        }.getOrDefault(FrictionType.HOLD_BUTTON)

        val config = ProtectionConfig(
            blockYouTubeShorts = prefs[KEY_BLOCK_SHORTS] ?: true,
            blockInstagramReels = prefs[KEY_BLOCK_REELS] ?: true,
            blockSnapchatSpotlight = prefs[KEY_BLOCK_SPOTLIGHT] ?: true,
            blockFacebookReels = prefs[KEY_BLOCK_FB_REELS] ?: true,
            blockTikTok = prefs[KEY_BLOCK_TIKTOK] ?: true,
            blockOtherShortVideo = prefs[KEY_BLOCK_OTHER] ?: true,
            blockAdultWebsites = prefs[KEY_BLOCK_ADULT_SITES] ?: true,
            blockAdultKeywords = prefs[KEY_BLOCK_ADULT_KEYWORDS] ?: true,
            browserProtectionEnabled = prefs[KEY_BROWSER_PROTECTION] ?: true,
            strictMode = prefs[KEY_STRICT_MODE] ?: true,
            nuclearMode = prefs[KEY_NUCLEAR_MODE] ?: false,
            frictionType = friction,
            unlockPhrase = prefs[KEY_UNLOCK_PHRASE] ?: "I choose my long term goals over cheap dopamine",
            pinHash = prefs[KEY_PIN_HASH] ?: "",
            pinSalt = prefs[KEY_PIN_SALT] ?: ""
        )
        _protectionConfig.value = config
        _isOnboardingCompleted.value = prefs[KEY_ONBOARDING_DONE] ?: false
        _appTheme.value = prefs[KEY_APP_THEME] ?: "SYSTEM"

        val habits = HabitConfig(
            morningPledgeEnabled = prefs[KEY_MORNING_PLEDGE_ENABLED] ?: true,
            morningPledgeHour = prefs[KEY_MORNING_PLEDGE_HOUR] ?: 8,
            morningPledgeMinute = prefs[KEY_MORNING_PLEDGE_MIN] ?: 0,
            eveningSummaryEnabled = prefs[KEY_EVENING_SUMMARY_ENABLED] ?: true,
            eveningSummaryHour = prefs[KEY_EVENING_SUMMARY_HOUR] ?: 21,
            eveningSummaryMinute = prefs[KEY_EVENING_SUMMARY_MIN] ?: 0,
            bedtimeShieldEnabled = prefs[KEY_BEDTIME_SHIELD_ENABLED] ?: false,
            bedtimeStartHour = prefs[KEY_BEDTIME_START_HOUR] ?: 23,
            bedtimeStartMinute = prefs[KEY_BEDTIME_START_MIN] ?: 0,
            bedtimeEndHour = prefs[KEY_BEDTIME_END_HOUR] ?: 6,
            bedtimeEndMinute = prefs[KEY_BEDTIME_END_MIN] ?: 30
        )
        _habitConfig.value = habits
        HabitNotificationScheduler.reschedule(context, habits)
    }

    override suspend fun updateCategory(category: ContentCategory, isBlocked: Boolean) {
        val current = _protectionConfig.value
        val updated = when (category) {
            ContentCategory.YOUTUBE_SHORTS -> current.copy(blockYouTubeShorts = isBlocked)
            ContentCategory.INSTAGRAM_REELS -> current.copy(blockInstagramReels = isBlocked)
            ContentCategory.SNAPCHAT_SPOTLIGHT -> current.copy(blockSnapchatSpotlight = isBlocked)
            ContentCategory.FACEBOOK_REELS -> current.copy(blockFacebookReels = isBlocked)
            ContentCategory.TIKTOK -> current.copy(blockTikTok = isBlocked)
            ContentCategory.OTHER_SHORT_VIDEO -> current.copy(blockOtherShortVideo = isBlocked)
            ContentCategory.ADULT_WEBSITE -> current.copy(blockAdultWebsites = isBlocked)
            ContentCategory.ADULT_KEYWORD -> current.copy(blockAdultKeywords = isBlocked)
            ContentCategory.SYSTEM_TAMPER -> current
        }
        updateProtectionConfig(updated)
    }

    override suspend fun updateProtectionConfig(config: ProtectionConfig) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_BLOCK_SHORTS] = config.blockYouTubeShorts
            prefs[KEY_BLOCK_REELS] = config.blockInstagramReels
            prefs[KEY_BLOCK_SPOTLIGHT] = config.blockSnapchatSpotlight
            prefs[KEY_BLOCK_FB_REELS] = config.blockFacebookReels
            prefs[KEY_BLOCK_TIKTOK] = config.blockTikTok
            prefs[KEY_BLOCK_OTHER] = config.blockOtherShortVideo
            prefs[KEY_BLOCK_ADULT_SITES] = config.blockAdultWebsites
            prefs[KEY_BLOCK_ADULT_KEYWORDS] = config.blockAdultKeywords
            prefs[KEY_BROWSER_PROTECTION] = config.browserProtectionEnabled
            prefs[KEY_STRICT_MODE] = config.strictMode
            prefs[KEY_NUCLEAR_MODE] = config.nuclearMode
            prefs[KEY_FRICTION_TYPE] = config.frictionType.name
            prefs[KEY_UNLOCK_PHRASE] = config.unlockPhrase
            prefs[KEY_PIN_HASH] = config.pinHash
            prefs[KEY_PIN_SALT] = config.pinSalt
        }
        _protectionConfig.value = config
    }

    override suspend fun setStrictMode(enabled: Boolean) {
        val current = _protectionConfig.value.copy(strictMode = enabled)
        updateProtectionConfig(current)
    }

    override suspend fun setFrictionType(frictionType: FrictionType) {
        val current = _protectionConfig.value.copy(frictionType = frictionType)
        updateProtectionConfig(current)
    }

    override suspend fun setPin(pin: String) {
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin(pin, salt)
        val current = _protectionConfig.value.copy(pinHash = hash, pinSalt = salt)
        updateProtectionConfig(current)
    }

    override suspend fun clearPin() {
        val current = _protectionConfig.value.copy(pinHash = "", pinSalt = "")
        updateProtectionConfig(current)
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val current = _protectionConfig.value
        return PinHasher.verifyPin(pin, current.pinHash, current.pinSalt)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = completed
        }
        _isOnboardingCompleted.value = completed
    }

    override suspend fun setAppTheme(theme: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_APP_THEME] = theme
        }
        _appTheme.value = theme
    }

    override suspend fun saveSchedule(schedule: ScheduleConfig) {
        val list = _schedules.value.toMutableList()
        val idx = list.indexOfFirst { it.id == schedule.id }
        if (idx >= 0) {
            list[idx] = schedule
        } else {
            list.add(schedule)
        }
        _schedules.value = list
    }

    override suspend fun deleteSchedule(id: String) {
        _schedules.value = _schedules.value.filterNot { it.id == id }
    }

    override suspend fun updateHabitConfig(config: HabitConfig) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_MORNING_PLEDGE_ENABLED] = config.morningPledgeEnabled
            prefs[KEY_MORNING_PLEDGE_HOUR] = config.morningPledgeHour
            prefs[KEY_MORNING_PLEDGE_MIN] = config.morningPledgeMinute
            prefs[KEY_EVENING_SUMMARY_ENABLED] = config.eveningSummaryEnabled
            prefs[KEY_EVENING_SUMMARY_HOUR] = config.eveningSummaryHour
            prefs[KEY_EVENING_SUMMARY_MIN] = config.eveningSummaryMinute
            prefs[KEY_BEDTIME_SHIELD_ENABLED] = config.bedtimeShieldEnabled
            prefs[KEY_BEDTIME_START_HOUR] = config.bedtimeStartHour
            prefs[KEY_BEDTIME_START_MIN] = config.bedtimeStartMinute
            prefs[KEY_BEDTIME_END_HOUR] = config.bedtimeEndHour
            prefs[KEY_BEDTIME_END_MIN] = config.bedtimeEndMinute
        }
        _habitConfig.value = config
        HabitNotificationScheduler.reschedule(context, config)
    }
}
