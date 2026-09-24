package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class FacebookReelsDetector : ContentDetector {
    override val name = "FacebookReelsDetector"
    override val version = "2.0.0-STRICT"

    companion object {
        val FACEBOOK_PACKAGES = setOf(
            "com.facebook.katana",
            "com.facebook.lite"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        return FACEBOOK_PACKAGES.any { it.equals(packageName, ignoreCase = true) }
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockFacebookReels) {
            return DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Facebook Reels blocking disabled")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        // Signal 2: Content descriptions (Accessibility nodes)
        val isReelsNavOrViewer = context.hasSelectedDesc("Reels") ||
            context.hasSelectedText("Reels") ||
            context.hasContentDescription("Reels, selected") ||
            context.hasContentDescription("Reels tab, selected") ||
            context.hasContentDescription("selected, Reels") ||
            context.hasContentDescription("Reels video player")

        // Excluded surface: Facebook Feed (Photos, text posts, groups, stories tray) without active fullscreen player or selected tab
        val isFullscreenReelsPlayer = context.hasAnyViewId("reel_fullscreen_view", "fb_shorts_viewer", "reels_video_player")
        val isFeed = context.hasAnyViewId("newsfeed_recycler", "feed_stream", "story_tray", "story_view", "composer_root") &&
            !isFullscreenReelsPlayer && !isReelsNavOrViewer
        if (isFeed) {
            return DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Clean Facebook feed surface")
        }

        // Signal 1: Fullscreen Reels container or view hierarchy
        if (context.hasAnyViewId(
                "reel_fullscreen_view",
                "fb_shorts_container",
                "reels_tab_container",
                "reels_video_player",
                "fb_shorts_viewer",
                "reels_page_container",
                "reels_fragment"
            )
        ) {
            confidence = 1.0f
            reasons.add("Facebook Reels container active")
        }

        if (isReelsNavOrViewer) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Facebook Reels navigation/viewer node detected")
        }

        val threshold = if (config.strictMode) 0.50f else 0.70f

        return if (confidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = confidence,
                category = ContentCategory.FACEBOOK_REELS,
                ruleId = "FB_REELS_SURGICAL",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Clean Facebook surface")
        }
    }
}
