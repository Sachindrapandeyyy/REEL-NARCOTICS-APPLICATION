package com.zenith.focus.accessibility.policy

import com.zenith.focus.accessibility.detector.DetectionResult
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import com.zenith.focus.domain.nuclear.NuclearSessionStatus

object NuclearProtectionPolicy {

    /**
     * Resolves whether content should be blocked according to the strict priority chain:
     * 1. Nuclear Mode (Absolute highest: all addictive short-form & adult feeds blocked, immutable)
     * 2. Focus Lock Mode (Active countdown window)
     * 3. Global Protection / Continuous Shield
     * 4. App Restrictions & Normal Preferences
     */
    fun shouldBlock(
        result: DetectionResult,
        nuclearSession: NuclearSession,
        lockState: LockState,
        config: ProtectionConfig,
        nowWallClock: Long = System.currentTimeMillis(),
        nowElapsedRealtime: Long = android.os.SystemClock.elapsedRealtime()
    ): Boolean {
        if (!result.isBlocked) return false

        // PRIORITY 1: NUCLEAR MODE
        if (nuclearSession.isCurrentlyActive(nowWallClock, nowElapsedRealtime)) {
            // Under Nuclear Mode, ALL addictive & adult content is 100% blocked, zero exceptions
            return isAddictiveOrAdultCategory(result.category)
        }

        // PRIORITY 2: FOCUS LOCK
        if (lockState.isCurrentlyActive(nowWallClock)) {
            if (lockState.enabledCategories.contains(result.category)) {
                return true
            }
        }

        // PRIORITY 3: CONTINUOUS SHIELD / GLOBAL PREFERENCES
        return config.isCategoryBlocked(result.category)
    }

    private fun isAddictiveOrAdultCategory(category: ContentCategory): Boolean {
        return when (category) {
            ContentCategory.YOUTUBE_SHORTS,
            ContentCategory.INSTAGRAM_REELS,
            ContentCategory.TIKTOK,
            ContentCategory.SNAPCHAT_SPOTLIGHT,
            ContentCategory.FACEBOOK_REELS,
            ContentCategory.OTHER_SHORT_VIDEO,
            ContentCategory.ADULT_WEBSITE,
            ContentCategory.ADULT_KEYWORD,
            ContentCategory.SYSTEM_TAMPER -> true
        }
    }
}
