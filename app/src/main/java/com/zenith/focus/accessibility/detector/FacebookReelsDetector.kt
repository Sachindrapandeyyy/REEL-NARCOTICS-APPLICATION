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

        // Messenger / Direct Chats immunity - ALWAYS ALLOWED
        val isMessenger = context.packageName.equals("com.facebook.orca", ignoreCase = true)
        if (isMessenger && context.hasAnyViewId(
                "thread_view",
                "message_list",
                "messages_list",
                "composer",
                "composer_text_view",
                "text_input_bar",
                "thread_title",
                "orca_chat_thread_view_root",
                "direct_inbox",
                "thread_list"
            )
        ) {
            return DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Messenger active chat or inbox immune")
        }

        // Signal 1: Fullscreen Reels container or view hierarchy
        if (context.hasAnyViewId(
                "reel_fullscreen_view",
                "fb_shorts_container",
                "reels_tab_container",
                "reels_video_player",
                "reel_viewer",
                "fb_shorts_viewer",
                "reels_page_container",
                "reels_fragment"
            )
        ) {
            confidence = 1.0f
            reasons.add("Facebook Reels container active")
        }

        // Signal 2: Content descriptions (Accessibility nodes)
        val isReelsNavOrViewer = context.hasSelectedDesc("Reels") ||
            context.hasSelectedText("Reels") ||
            context.hasContentDescription("Reels, selected") ||
            context.hasContentDescription("Reels tab, selected") ||
            context.hasContentDescription("selected, Reels") ||
            context.hasContentDescription("Reels video player")
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
