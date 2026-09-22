package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class YouTubeShortsDetector : ContentDetector {
    override val name = "YouTubeShortsDetector"
    override val version = "2.2.0-SURGICAL"

    companion object {
        const val PACKAGE_YOUTUBE = "com.google.android.youtube"

        // Full-screen dedicated player containers that only exist when a Short is actively open & playing
        val ACTIVE_SHORTS_PLAYER_IDS = listOf(
            "reel_watch_fragment_root",
            "reel_player_page_container",
            "reel_player_page_content",
            "reel_player_overlay_root",
            "reel_player_overlay_container",
            "reel_watch_player",
            "reel_watch_player_view",
            "reel_time_bar",
            "reel_video_interactions",
            "reel_player_footer_container",
            "reel_watch_refresher",
            "reel_player_page_view",
            "reel_view_pager",
            "shorts_container",
            "shorts_player_fragment",
            "shorts_player",
            "reel_container",
            "reel_player_fragment",
            "reel_layout",
            "reel_pager"
        )

        // Shelf / preview IDs that appear in search results or home feed (MUST NEVER BLOCK)
        val SHELF_PREVIEW_KEYWORDS = listOf(
            "reel_recycler",
            "reel_shelf",
            "reel_shelf_header",
            "reel_shelf_root",
            "reel_carousel"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals(PACKAGE_YOUTUBE, ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockYouTubeShorts) {
            return DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "Shorts blocking disabled")
        }

        // 1. Check if user is actively searching or viewing search results
        val isSearchActive = context.hasAnyViewId(
            "search_results_editor",
            "search_chip_bar",
            "search_edit_text",
            "search_type_selector",
            "search_container",
            "search_box"
        ) || context.className.contains("SearchActivity", ignoreCase = true)

        // 2. Check if a dedicated full-screen Shorts player is mounted
        val matchedPlayerId = ACTIVE_SHORTS_PLAYER_IDS.firstOrNull { context.hasViewId(it) }

        // If search results are showing and NO full-screen player is active, ALLOW immediately
        if (isSearchActive && matchedPlayerId == null) {
            return DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "YouTube search results active (PW / study search immune)")
        }

        // 3. Check if normal long-form video player is active
        val isNormalWatchPlayer = context.hasViewId("watch_while_layout") &&
            (context.hasViewId("player_fragment") || context.hasViewId("time_bar") || context.hasViewId("play_pause_button")) &&
            matchedPlayerId == null
        if (isNormalWatchPlayer) {
            return DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "Normal YouTube long video")
        }

        // 4. Check Shorts tab explicitly selected in navigation bar (independent of specific pivot bar ID)
        val isShortsTabSelected = context.hasSelectedDesc("Shorts") ||
            context.hasSelectedText("Shorts") ||
            context.hasContentDescription("Shorts, selected") ||
            context.hasContentDescription("Shorts tab, selected") ||
            context.hasContentDescription("selected, Shorts") ||
            context.contentDescriptions.any { it.startsWith("Shorts", ignoreCase = true) && it.contains("selected", ignoreCase = true) }

        // 5. Check active Shorts player UI controls (Only present in active full-screen player)
        val hasShortsControls = context.hasContentDescription("Dislike this short") ||
            context.hasContentDescription("Dislike this video") ||
            context.hasContentDescription("Remix this Short") ||
            context.hasContentDescription("Remix") ||
            context.hasContentDescription("Shorts video player") ||
            context.hasContentDescription("Sound used in this short") ||
            context.hasContentDescription("Show sound details") ||
            context.hasContentDescription("Create with this sound") ||
            context.hasContentDescription("Pause short") ||
            context.hasContentDescription("Play short") ||
            context.hasContentDescription("Share this short")

        // 6. Check if this is merely an inline thumbnail shelf/carousel on feed or search
        // Must NEVER exempt if an active player container, Shorts tab, or player controls are present
        val hasShelfOnly = context.viewIds.any { id ->
            SHELF_PREVIEW_KEYWORDS.any { keyword -> id.contains(keyword, ignoreCase = true) }
        } && matchedPlayerId == null && !isShortsTabSelected && !hasShortsControls
        if (hasShelfOnly) {
            return DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "Shorts thumbnail shelf on feed (not opened)")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        // Signal 1: Full-screen Shorts player layout active (Definite Short opened)
        if (matchedPlayerId != null) {
            confidence = 1.0f
            reasons.add("Active Shorts player layout detected ($matchedPlayerId)")
        }

        // Signal 2: Shorts tab explicitly selected in bottom navigation bar
        if (isShortsTabSelected) {
            confidence = 1.0f
            reasons.add("Shorts tab actively selected in navigation bar")
        }

        // Signal 3: Active Shorts player UI controls (Only present in active full-screen player)
        if (hasShortsControls) {
            // Only consider player actions if not on normal watch or search
            if (!isSearchActive && !isNormalWatchPlayer) {
                confidence = maxOf(confidence, 0.95f)
                reasons.add("Active Shorts player overlay controls detected")
            }
        }

        val threshold = if (config.strictMode) 0.50f else 0.70f

        return if (confidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = confidence,
                category = ContentCategory.YOUTUBE_SHORTS,
                ruleId = "YT_SHORTS_SURGICAL",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "Clean YouTube surface")
        }
    }
}
