package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class SnapchatSpotlightDetector : ContentDetector {
    override val name = "SnapchatSpotlightDetector"
    override val version = "2.0.0-COMPREHENSIVE"

    companion object {
        const val PACKAGE_SNAPCHAT = "com.snapchat.android"

        val SPOTLIGHT_VIEWER_IDS = listOf(
            "spotlight_fullscreen",
            "spotlight_video_player",
            "spotlight_video",
            "ff_spotlight",
            "neon_spotlight",
            "spotlight_page_view"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals(PACKAGE_SNAPCHAT, ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockSnapchatSpotlight) {
            return DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Spotlight blocking disabled")
        }

        // 1. Check if Spotlight viewer or layout is active (STRICT: Must be an actual video player container, NOT a navigation icon or tab button)
        val isSpotlightViewerActive = SPOTLIGHT_VIEWER_IDS.any { context.hasViewId(it) } ||
            context.viewIds.any { id ->
                val lower = id.lowercase(java.util.Locale.US)
                lower.contains("spotlight") &&
                !lower.contains("icon") &&
                !lower.contains("tab") &&
                !lower.contains("nav") &&
                !lower.contains("button") &&
                !lower.contains("feed_view") &&
                (lower.contains("video") || lower.contains("player") || lower.contains("fullscreen"))
            }

        // 2. Check navigation / tabs (STRICT: Must be explicitly selected)
        val isSpotlightTabActive = context.hasSelectedDesc("Spotlight") ||
            context.hasSelectedText("Spotlight") ||
            context.selectedDescriptions.any { it.contains("Spotlight", ignoreCase = true) } ||
            context.selectedTexts.any { it.contains("Spotlight", ignoreCase = true) } ||
            context.hasContentDescription("Spotlight, selected") ||
            context.hasContentDescription("Spotlight tab, selected") ||
            context.hasContentDescription("selected, Spotlight") ||
            context.contentDescriptions.any { desc ->
                desc.contains("Spotlight", ignoreCase = true) && desc.contains("selected", ignoreCase = true)
            }

        // 3. Check content description / text signals
        val hasSpotlightContent = context.contentDescriptions.any { desc ->
            desc.contains("Spotlight by", ignoreCase = true) ||
            desc.contains("Remix Snap", ignoreCase = true) ||
            desc.contains("Watch Spotlight", ignoreCase = true) ||
            desc.contains("Like this Spotlight", ignoreCase = true) ||
            desc.contains("Trending Sound", ignoreCase = true)
        } || context.visibleTexts.any { text ->
            text.contains("Spotlight", ignoreCase = true) && (text.contains("Sound", ignoreCase = true) || text.contains("Remix", ignoreCase = true) || text.contains("Subscribe", ignoreCase = true))
        }

        // Excluded surfaces: 1-on-1 Chats, Friends list, Camera preview, and Stories/Discover
        val isChatSurface = context.hasAnyViewId("chat_v3_container", "chat_input_text_field", "chat_message_input", "feed_view", "friends_feed")
        val isCameraView = context.hasAnyViewId("camera_view", "camera_layout", "camera_capture_button", "camera_root", "capture_button")
        val isStoryOrDiscover = context.hasAnyViewId("discover_feed", "story_viewer", "opera_page_view") && !isSpotlightViewerActive && !isSpotlightTabActive
        if ((isChatSurface || isCameraView || isStoryOrDiscover) && !isSpotlightViewerActive && !isSpotlightTabActive && !hasSpotlightContent) {
            return DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Snapchat clean camera/chat/stories surface")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        if (isSpotlightViewerActive) {
            confidence = 1.0f
            reasons.add("Snapchat Spotlight/Story/Discover player active")
        }

        if (isSpotlightTabActive) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Snapchat Spotlight tab actively selected")
        }

        if (hasSpotlightContent) {
            confidence = maxOf(confidence, 0.90f)
            reasons.add("Snapchat Spotlight content metadata detected")
        }

        val threshold = if (config.strictMode) 0.50f else 0.70f

        return if (confidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = confidence,
                category = ContentCategory.SNAPCHAT_SPOTLIGHT,
                ruleId = "SNAP_SPOTLIGHT_SURGICAL",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Clean Snapchat camera/chat surface")
        }
    }
}
