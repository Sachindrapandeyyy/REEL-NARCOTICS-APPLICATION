package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class FacebookReelsDetector : ContentDetector {
    override val name = "FacebookReelsDetector"
    override val version = "2.1.0-COMPREHENSIVE"

    companion object {
        val FACEBOOK_PACKAGES = setOf(
            "com.facebook.katana",
            "com.facebook.lite",
            "com.facebook.wakizashi"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        val lower = packageName.lowercase(java.util.Locale.US)
        return FACEBOOK_PACKAGES.contains(lower) ||
               (lower.startsWith("com.facebook.") && !lower.contains("orca") && !lower.contains("messenger"))
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockFacebookReels) {
            return DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Facebook Reels blocking disabled")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        val isFacebookLite = context.packageName.equals("com.facebook.lite", ignoreCase = true) ||
                             context.packageName.contains("facebook.lite", ignoreCase = true)

        // Signal 2: Content descriptions & navigation indicators (Accessibility nodes)
        val isReelsTabSelected = context.hasSelectedDesc("Reels") ||
            context.hasSelectedText("Reels") ||
            context.hasContentDescription("Reels, selected") ||
            context.hasContentDescription("Reels tab, selected") ||
            context.hasContentDescription("selected, Reels") ||
            (isFacebookLite && context.visibleTexts.any { it.equals("Reels", ignoreCase = true) } &&
             (context.hasSelectedText("Reels") || context.hasSelectedDesc("Reels") || context.hasContentDescription("Reels, selected")))

        val isReelsNavOrViewer = isReelsTabSelected ||
            context.hasContentDescription("Reels video player")

        // Excluded surface: Facebook Feed (Photos, text posts, groups, stories tray) without active fullscreen player or selected tab
        val isFullscreenReelsPlayer = context.hasAnyViewId("reel_fullscreen_view", "fb_shorts_viewer", "reels_video_player")
        val isFeed = context.hasAnyViewId("newsfeed_recycler", "feed_stream", "story_tray", "story_view", "composer_root") &&
            !isFullscreenReelsPlayer && !isReelsNavOrViewer
        if (isFeed) {
            return DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Clean Facebook feed surface")
        }

        // Facebook Lite specific exclusions: Home feed, Groups, Chats, Profile
        if (isFacebookLite) {
            val isLiteFeed = (context.hasText("What's on your mind?") ||
                              context.hasText("Stories") ||
                              context.visibleTexts.any { it.equals("Facebook", ignoreCase = true) }) &&
                             !context.hasText("Original audio") &&
                             !context.hasText("Remix this reel") &&
                             !isReelsTabSelected

            val isLiteChatOrProfile = context.visibleTexts.any {
                it.equals("Chats", ignoreCase = true) ||
                it.equals("Messages", ignoreCase = true) ||
                it.equals("Edit profile", ignoreCase = true)
            } && !isReelsTabSelected

            if (isLiteFeed || isLiteChatOrProfile) {
                return DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Clean Facebook Lite surface")
            }
        }

        // Signal 1: Fullscreen Reels container or view hierarchy (Standard Facebook)
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
            if (isReelsTabSelected) {
                reasons.add("Facebook Reels tab actively selected")
            } else {
                reasons.add("Facebook Reels navigation/viewer node detected")
            }
        }

        // Facebook Lite Litho detection
        if (isFacebookLite) {
            val hasLiteReelAudioOrRemix = context.hasText("Original audio") ||
                context.hasText("Original Audio") ||
                context.hasText("Remix this reel") ||
                context.hasText("Remix with") ||
                context.hasText("Share reel") ||
                context.contentDescriptions.any { it.contains("Reel by", ignoreCase = true) }

            if (hasLiteReelAudioOrRemix) {
                confidence = maxOf(confidence, 0.95f)
                reasons.add("Facebook Lite Reels audio/remix metadata active")
            } else if (context.hasText("Reels") && (context.hasText("Like") || context.hasText("Comment") || context.hasText("Share")) && !context.hasText("What's on your mind?")) {
                confidence = maxOf(confidence, 0.85f)
                reasons.add("Facebook Lite Reels viewer detected")
            }
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
