package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class InstagramReelsDetector : ContentDetector {
    override val name = "InstagramReelsDetector"
    override val version = "2.0.0-STRICT"

    companion object {
        const val PACKAGE_INSTAGRAM = "com.instagram.android"
    }

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals(PACKAGE_INSTAGRAM, ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockInstagramReels) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Reels blocking disabled")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        // Signal 1: Clips viewer layout or container (Definite Reel)
        if (context.hasAnyViewId("clips_viewer_view_pager", "clips_video_container", "clips_swipe_refresh_layout", "reel_viewer_clips_item")) {
            confidence = 1.0f
            reasons.add("Instagram clips viewer active")
        }

        // Signal 2: Reels bottom navigation tab selected or active
        val isReelsTabSelected = context.hasSelectedDesc("Reels") ||
                context.hasSelectedText("Reels") ||
                context.hasContentDescription("Reels, tab 4 of 5") ||
                context.hasContentDescription("Reels tab") ||
                context.hasContentDescription("Reels, selected") ||
                context.hasContentDescription("Reels tab, selected")
        if (isReelsTabSelected) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Reels tab selected in navigation")
        }

        // Signal 3: Reel action tokens
        if (context.hasContentDescription("Reel by") || context.hasText("Remix this reel") || context.hasText("Use audio") || context.hasText("Original audio") || context.hasText("Watch more reels")) {
            confidence = maxOf(confidence, 0.85f)
            reasons.add("Reel audio/remix action tokens present")
        }

        // Allow DMs only if no clips viewer is active
        if (confidence < 0.40f) {
            if (context.hasAnyViewId("direct_thread_feed", "row_thread_composer", "direct_inbox", "message_composer")) {
                return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Direct messages active")
            }
        }

        val threshold = if (config.strictMode) 0.40f else 0.65f

        return if (confidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = confidence,
                category = ContentCategory.INSTAGRAM_REELS,
                ruleId = "IG_REELS_S_PLUS",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Clean Instagram surface")
        }
    }
}
