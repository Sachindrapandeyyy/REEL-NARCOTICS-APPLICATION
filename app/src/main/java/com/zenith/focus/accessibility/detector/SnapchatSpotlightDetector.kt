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
            "spotlight_container",
            "spotlight_fullscreen",
            "spotlight_video_player",
            "full_screen_player",
            "spotlight_carousel",
            "spotlight_feed",
            "spotlight_video",
            "spotlight_tab",
            "ff_spotlight",
            "neon_spotlight",
            "action_spotlight",
            "discover_feed"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals(PACKAGE_SNAPCHAT, ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockSnapchatSpotlight) {
            return DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Spotlight blocking disabled")
        }

        // 1. Check if Spotlight viewer or layout is active
        val isSpotlightViewerActive = SPOTLIGHT_VIEWER_IDS.any { context.hasViewId(it) } ||
            context.viewIds.any { id ->
                id.contains("spotlight", ignoreCase = true) ||
                id.contains("discover_feed", ignoreCase = true)
            }

        // 2. Check navigation / tabs
        val isSpotlightTabActive = context.hasSelectedDesc("Spotlight") ||
            context.hasSelectedText("Spotlight") ||
            context.selectedDescriptions.any { it.contains("Spotlight", ignoreCase = true) } ||
            context.selectedTexts.any { it.contains("Spotlight", ignoreCase = true) } ||
            context.contentDescriptions.any { desc ->
                desc.contains("Spotlight", ignoreCase = true) &&
                (desc.contains("tab", ignoreCase = true) || desc.contains("selected", ignoreCase = true) || desc.contains("button", ignoreCase = true) || desc.trim().equals("Spotlight", ignoreCase = true))
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

        // Excluded surfaces: 1-on-1 Chats, Friends list, and Camera preview (without active spotlight viewer or tab)
        val isChatSurface = context.hasAnyViewId("chat_v3_container", "chat_input_text_field", "chat_message_input", "feed_view", "friends_feed")
        val isCameraView = context.hasAnyViewId("camera_view", "camera_layout", "camera_capture_button") && !isSpotlightViewerActive && !isSpotlightTabActive
        if ((isChatSurface || isCameraView) && !isSpotlightViewerActive && !isSpotlightTabActive && !hasSpotlightContent) {
            return DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Snapchat chat/camera active")
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
