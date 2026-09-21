package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class SnapchatSpotlightDetector : ContentDetector {
    override val name = "SnapchatSpotlightDetector"
    override val version = "1.1.0"

    companion object {
        const val PACKAGE_SNAPCHAT = "com.snapchat.android"
    }

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals(PACKAGE_SNAPCHAT, ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockSnapchatSpotlight) {
            return DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Spotlight blocking disabled")
        }

        // 1. Excluded surfaces (Chat, Camera)
        if (context.hasAnyViewId("chat_v3_container", "camera_view", "chat_input_text_field")) {
            return DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Snapchat chat/camera active")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        if (context.hasAnyViewId("spotlight_container", "spotlight_fullscreen", "spotlight_video_player")) {
            confidence = 1.0f
            reasons.add("Spotlight player layout active")
        }

        val isSpotlightSelected = context.hasSelectedDesc("Spotlight") || context.hasSelectedText("Spotlight")
        if (isSpotlightSelected) {
            confidence = maxOf(confidence, 0.95f)
            reasons.add("Spotlight tab actively selected")
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
            DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Insufficient Spotlight signals")
        }
    }
}
