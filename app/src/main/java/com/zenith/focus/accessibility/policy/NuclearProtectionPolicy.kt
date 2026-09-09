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

        // PRIORITY 1: NUCLEAR LOCK (Strict Restriction: Selective user platforms & adult content, zero bypass)
        if (nuclearSession.isCurrentlyActive(nowWallClock, nowElapsedRealtime)) {
            if (result.category == ContentCategory.SYSTEM_TAMPER) {
                return true
            }
            return if (nuclearSession.enabledCategories.isNotEmpty()) {
                nuclearSession.enabledCategories.contains(result.category)
            } else {
                isAddictiveOrAdultCategory(result.category)
            }
        }

        // PRIORITY 2: STANDARD FOCUS LOCK (Standard Restriction: Active focus countdown window)
        if (lockState.isCurrentlyActive(nowWallClock)) {
            return if (lockState.enabledCategories.isNotEmpty()) {
                lockState.enabledCategories.contains(result.category)
            } else {
                isAddictiveOrAdultCategory(result.category) || config.isCategoryBlocked(result.category)
            }
        }

        // NO LOCK ACTIVE: NO RESTRICTIONS
        // When neither Nuclear Mode nor Standard Focus Lock is active, NO content is restricted.
        return false
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
