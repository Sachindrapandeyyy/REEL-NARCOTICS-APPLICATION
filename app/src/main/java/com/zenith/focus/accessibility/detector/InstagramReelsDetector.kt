package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class InstagramReelsDetector : ContentDetector {
    override val name = "InstagramReelsDetector"
    override val version = "2.2.0-SURGICAL"

    companion object {
        const val PACKAGE_INSTAGRAM = "com.instagram.android"

        // Active full-screen Reels viewer containers (Clips player only; does NOT match 24h Stories or Explore grid)
        val ACTIVE_REELS_VIEWER_IDS = listOf(
            "clips_viewer_view_pager",
            "clips_video_container",
            "clips_swipe_refresh_layout",
            "reel_viewer_clips_item",
            "clips_viewer_container",
            "clips_root",
            "clips_pager",
            "clips_media_component"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals(PACKAGE_INSTAGRAM, ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockInstagramReels) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Reels blocking disabled")
        }

        // 1. Direct Messages / Chats - ALWAYS ALLOWED
        if (context.hasAnyViewId("direct_thread_feed", "row_thread_composer", "direct_inbox", "message_composer")) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Direct messages active")
        }

        val isReelsViewerActive = ACTIVE_REELS_VIEWER_IDS.any { context.hasViewId(it) } ||
            context.viewIds.any { id ->
                id.contains("clips_viewer", ignoreCase = true) ||
                id.contains("clips_video", ignoreCase = true) ||
                id.contains("reel_viewer_clips", ignoreCase = true)
            }

        // Signal 2: Reels bottom navigation tab selected or active
        val isReelsTabSelected = context.hasSelectedDesc("Reels") ||
            context.hasSelectedText("Reels") ||
            context.hasContentDescription("Reels, selected") ||
            context.hasContentDescription("Reels tab, selected") ||
            context.hasContentDescription("selected, Reels") ||
            context.hasContentDescription("Reels, tab 4 of 5, selected") ||
            context.contentDescriptions.any { it.contains("Reels", ignoreCase = true) && it.contains("selected", ignoreCase = true) }

        // 2. Search / Explore / Profile - ALLOWED unless full-screen Reels viewer or Reels tab is opened
        val isSearchOrExplore = context.hasAnyViewId("action_bar_search_edit_text", "search_tab", "explore_tab")
        if (isSearchOrExplore && !isReelsViewerActive && !isReelsTabSelected) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Instagram search/explore active")
        }

        // 3. Normal Feed - ALLOWED unless full-screen viewer or Reels tab is opened
        val isNormalFeed = context.hasAnyViewId("feed_recycler", "main_feed", "sticky_header_list")
        if (isNormalFeed && !isReelsViewerActive && !isReelsTabSelected) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Instagram home feed active")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        // Signal 1: Active Clips/Reel viewer container (Definite Reel opened)
        if (isReelsViewerActive) {
            confidence = 1.0f
            reasons.add("Instagram full-screen clips viewer active")
        }

        if (isReelsTabSelected) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Reels tab actively selected in navigation")
        }

        val threshold = if (config.strictMode) 0.50f else 0.70f

        return if (confidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = confidence,
                category = ContentCategory.INSTAGRAM_REELS,
                ruleId = "IG_REELS_SURGICAL",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Clean Instagram surface")
        }
    }
}
