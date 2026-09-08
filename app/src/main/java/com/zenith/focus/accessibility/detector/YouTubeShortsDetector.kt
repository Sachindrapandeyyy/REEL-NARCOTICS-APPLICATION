package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class YouTubeShortsDetector : ContentDetector {
    override val name = "YouTubeShortsDetector"
    override val version = "2.0.0-STRICT"

    companion object {
        const val PACKAGE_YOUTUBE = "com.google.android.youtube"
    }

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals(PACKAGE_YOUTUBE, ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockYouTubeShorts) {
            return DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "Shorts blocking disabled")
        }

        // S++ STRICT ZERO-TOLERANCE: Any Shorts indicator triggers instant ejection
        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        // Signal 1: Reel container / recycler / player presence (Definite Shorts)
        val reelIds = listOf(
            "reel_watch_fragment_root",
            "reel_recycler",
            "reel_player_page_container",
            "reel_player_page_content",
            "reel_player_overlay_root",
            "reel_player_overlay_container",
            "reel_watch_player",
            "reel_time_bar",
            "reel_scrim_shorts_while_bottom_gradient",
            "reel_scrim_shorts_while_top",
            "reel_video_interactions",
            "reel_player_footer_container",
            "reel_watch_refresher",
            "reel_player_page_view",
            "reel_view_pager",
            "reel_watch_player_view"
        )
        val matchedReelId = reelIds.firstOrNull { context.hasViewId(it) }
            ?: context.viewIds.firstOrNull { it.contains("reel_", ignoreCase = true) }

        if (matchedReelId != null) {
            confidence = 1.0f
            reasons.add("Shorts reel layout detected ($matchedReelId)")
        }

        // Signal 2: Shorts Pivot Bar item selected
        if (context.hasViewId("pivot_bar")) {
            if (context.hasSelectedDesc("Shorts") || context.hasSelectedText("Shorts")) {
                confidence = 1.0f
                reasons.add("Shorts tab actively selected in navigation bar")
            }
        }

        // Signal 3: Shorts action tokens & hashtags
        if (context.hasContentDescription("Remix this Short") ||
            context.hasContentDescription("Dislike this short") ||
            context.hasContentDescription("Shorts video player") ||
            context.hasContentDescription("Shorts camera") ||
            context.hasContentDescription("#shorts") ||
            context.hasText("#shorts")
        ) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Shorts UI action/hashtag detected")
        }

        // Safe surface check only if NO shorts signals exist at all
        if (confidence < 0.30f) {
            val isNormalWatchPlayer = context.hasViewId("watch_while_layout") &&
                (context.hasViewId("player_fragment") || context.hasViewId("time_bar") || context.hasViewId("play_pause_button")) &&
                !context.hasAnyViewId("reel_recycler", "reel_watch_fragment_root", "reel_watch_player")
            if (isNormalWatchPlayer) {
                return DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "Normal YouTube long video")
            }

            val isSearch = context.hasViewId("search_results_editor") || context.hasViewId("search_chip_bar")
            if (isSearch) {
                return DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "YouTube search active")
            }
        }

        val threshold = if (config.strictMode) 0.30f else 0.50f

        return if (confidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = confidence,
                category = ContentCategory.YOUTUBE_SHORTS,
                ruleId = "YT_SHORTS_S_PLUS",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.YOUTUBE_SHORTS, "Clean YouTube surface")
        }
    }
}
