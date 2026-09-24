package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class InstagramReelsDetector : ContentDetector {
    override val name = "InstagramReelsDetector"
    override val version = "2.3.0-COMPREHENSIVE"

    companion object {
        const val PACKAGE_INSTAGRAM = "com.instagram.android"
        const val PACKAGE_INSTAGRAM_LITE = "com.instagram.lite"
        const val PACKAGE_INSTAGRAM_THREADS = "com.instagram.barcelona"

        // Active full-screen Reels viewer containers (Dedicated fullscreen viewer only)
        val ACTIVE_REELS_VIEWER_IDS = listOf(
            "clips_viewer_view_pager",
            "clips_viewer_container",
            "clips_swipe_refresh_layout",
            "reel_viewer_clips_item",
            "clips_video_container",
            "clips_media_item",
            "clips_item",
            "clips_view_pager",
            "clips_root_container",
            "swipe_refresh_clips",
            "reel_player_view",
            "reels_page_view",
            "reels_video_player"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        val lower = packageName.lowercase(java.util.Locale.US)
        return lower == PACKAGE_INSTAGRAM ||
               lower == PACKAGE_INSTAGRAM_LITE ||
               lower == PACKAGE_INSTAGRAM_THREADS ||
               lower.startsWith("com.instagram.")
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockInstagramReels) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Reels blocking disabled")
        }

        // 1. Direct Messages / Chats - ALWAYS ALLOWED unless a full-screen Reel player is actively mounted
        val isDirectChat = context.hasAnyViewId("direct_thread_feed", "row_thread_composer", "direct_inbox", "message_composer", "direct_recipient_picker")

        val isDedicatedClipsViewer = context.hasAnyViewId("clips_viewer_view_pager", "clips_viewer_container", "reels_page_view", "reels_video_player")

        val isReelsViewerActive = ACTIVE_REELS_VIEWER_IDS.any { context.hasViewId(it) } ||
            context.viewIds.any { id ->
                id.contains("clips_viewer_view_pager", ignoreCase = true) ||
                id.contains("clips_viewer_container", ignoreCase = true) ||
                id.contains("reel_viewer_clips_item", ignoreCase = true) ||
                id.contains("clips_video_container", ignoreCase = true) ||
                id.contains("clips_view_pager", ignoreCase = true) ||
                id.contains("reels_page_view", ignoreCase = true) ||
                id.contains("reels_video_player", ignoreCase = true)
            }

        // Signal 2: Reels bottom navigation tab selected or active
        val isReelsTabSelected = context.hasSelectedDesc("Reels") ||
            context.hasSelectedText("Reels") ||
            context.selectedDescriptions.any { it.contains("Reels", ignoreCase = true) } ||
            context.selectedTexts.any { it.contains("Reels", ignoreCase = true) } ||
            context.contentDescriptions.any { desc ->
                desc.contains("Reels", ignoreCase = true) &&
                (desc.contains("selected", ignoreCase = true) || desc.contains("tab", ignoreCase = true) || desc.contains("4 of 5", ignoreCase = true) || desc.trim().equals("Reels", ignoreCase = true))
            }

        // Signal 3: Specific Reels interactive metadata
        val hasReelsMetadata = context.contentDescriptions.any { desc ->
            desc.contains("Reel by", ignoreCase = true) ||
            desc.contains("Like reel", ignoreCase = true) ||
            desc.contains("Comment on reel", ignoreCase = true) ||
            desc.contains("Share reel", ignoreCase = true) ||
            desc.contains("Watch Reels", ignoreCase = true) ||
            desc.contains("Watch more Reels", ignoreCase = true) ||
            desc.contains("Remix this reel", ignoreCase = true)
        } || context.visibleTexts.any { text ->
            (text.contains("Trending", ignoreCase = true) && text.contains("Audio", ignoreCase = true)) ||
            (text.contains("Remix with", ignoreCase = true)) ||
            (text.contains("Original audio", ignoreCase = true))
        }

        // Signal 4: Instagram Lite dedicated clips surface
        val isInstagramLite = context.packageName.equals(PACKAGE_INSTAGRAM_LITE, ignoreCase = true)
        val isLiteClipsViewer = isInstagramLite && (
            isReelsTabSelected ||
            context.hasViewId("reels_tab") ||
            context.hasViewId("reels_tab_container") ||
            context.viewIds.any { id ->
                id.contains("reels_tab", ignoreCase = true) ||
                id.contains("clips_viewer", ignoreCase = true) ||
                id.contains("reels_page", ignoreCase = true) ||
                id.contains("reel_player", ignoreCase = true)
            } ||
            (hasReelsMetadata && (context.hasViewId("video_player") || context.hasViewId("video_container") || context.hasViewId("clips_container")))
        )

        // Exclusions 1: Direct Messages (Always immune when not inside dedicated fullscreen viewer)
        if (isDirectChat && !isDedicatedClipsViewer && !isReelsTabSelected) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Direct messages active")
        }

        // Exclusions 2: 24h stories (reel_viewer) without dedicated clips viewer must NOT be blocked
        val is24hStory = (context.hasViewId("reel_viewer") || context.hasViewId("reel_viewer_root") || context.hasViewId("reel_viewer_framelayout")) &&
            !isDedicatedClipsViewer &&
            !isReelsTabSelected && !isLiteClipsViewer
        if (is24hStory) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Instagram 24h Story active")
        }

        // Exclusions 3: User Profiles & Account settings
        val isUserProfile = (context.hasAnyViewId("profile_tab", "profile_pager", "user_profile_header", "profile_header")) &&
            !isDedicatedClipsViewer && !isReelsTabSelected
        if (isUserProfile) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Instagram user profile active")
        }

        // Exclusions 4: Explore Grid thumbnails (explore_tab) without dedicated full-screen viewer must be allowed
        val isExploreGrid = (context.hasViewId("explore_tab") || context.hasViewId("search_tab")) &&
            !isDedicatedClipsViewer && !isReelsTabSelected && !isLiteClipsViewer
        if (isExploreGrid) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Instagram explore grid active")
        }

        // Exclusions 5: Normal Home Feed (Photos, normal carousel/feed video posts) without dedicated full-screen viewer
        val isNormalFeed = (context.hasAnyViewId("feed_recycler", "main_feed", "sticky_header_list")) &&
            !isDedicatedClipsViewer && !isReelsTabSelected && !isLiteClipsViewer
        if (isNormalFeed) {
            return DetectionResult.allowed(ContentCategory.INSTAGRAM_REELS, "Instagram home feed active")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        if (isReelsViewerActive) {
            confidence = 1.0f
            reasons.add("Instagram full-screen clips viewer active")
        }

        if (isReelsTabSelected) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Reels tab actively selected in navigation")
        }

        if (hasReelsMetadata && (isDedicatedClipsViewer || isReelsViewerActive)) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Reels content metadata detected")
        }

        if (isLiteClipsViewer) {
            confidence = 1.0f
            reasons.add("Instagram Lite Reels surface active")
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
