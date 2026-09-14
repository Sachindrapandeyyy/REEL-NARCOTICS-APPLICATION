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

        // 2. Scoring for Spotlight
        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        if (context.hasAnyViewId("spotlight_container", "spotlight_fullscreen", "spotlight_video_player")) {
            confidence += 0.70f
            reasons.add("Spotlight player layout active")
        }

        if (context.hasContentDescription("Spotlight tab") || context.hasExactText("Spotlight")) {
            confidence += 0.50f
            reasons.add("Spotlight tab selected")
        }

        if (context.hasText("Remix Snap") || context.hasContentDescription("Spotlight replies")) {
            confidence += 0.35f
            reasons.add("Spotlight action tokens found")
        }

        val finalConfidence = confidence.coerceIn(0f, 1f)
        val threshold = if (config.strictMode) 0.55f else 0.70f

        return if (finalConfidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = finalConfidence,
                category = ContentCategory.SNAPCHAT_SPOTLIGHT,
                ruleId = "SNAP_SPOTLIGHT_MATCH",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.SNAPCHAT_SPOTLIGHT, "Insufficient Spotlight signals")
        }
    }
}
