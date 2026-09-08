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
            "com.facebook.lite",
            "com.facebook.orca"
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

        // Signal 1: Fullscreen Reels container or view hierarchy
        if (context.hasAnyViewId(
                "reel_fullscreen_view",
                "fb_shorts_container",
                "reels_tab_container",
                "reels_video_player",
                "reel_viewer",
                "fb_shorts_viewer",
                "reels_tray",
                "reels_page_container",
                "reels_fragment"
            )
        ) {
            confidence = 1.0f
            reasons.add("Facebook Reels container active")
        }

        // Signal 2: Content descriptions (Accessibility nodes)
        if (context.hasContentDescription("Reels, tab") ||
            context.hasContentDescription("Reels tab") ||
            context.hasContentDescription("Watch Reels") ||
            context.hasContentDescription("Shorts and reels") ||
            context.hasContentDescription("Reels video player") ||
            context.hasContentDescription("Facebook Reels") ||
            context.hasContentDescription("Reels")
        ) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Facebook Reels navigation/viewer node detected")
        }

        // Signal 3: Reel Creator / Audio tokens
        if (context.hasContentDescription("Reel by") ||
            context.hasContentDescription("Reel of") ||
            context.hasText("Remix reel") ||
            context.hasText("Remix this reel") ||
            context.hasText("Use audio") ||
            context.hasText("Original audio") ||
            context.hasText("Watch more reels") ||
            context.hasText("Create reel") ||
            context.hasText("Reels and short videos") ||
            context.hasText("Reels & short videos")
        ) {
            confidence = maxOf(confidence, 0.90f)
            reasons.add("Facebook Reel audio/creator tokens detected")
        }

        val threshold = if (config.strictMode) 0.40f else 0.60f

        return if (confidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = confidence,
                category = ContentCategory.FACEBOOK_REELS,
                ruleId = "FB_REELS_STRICT_V2",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Clean Facebook surface")
        }
    }
}
