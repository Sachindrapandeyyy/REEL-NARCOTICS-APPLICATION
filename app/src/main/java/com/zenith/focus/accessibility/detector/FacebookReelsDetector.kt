package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class FacebookReelsDetector : ContentDetector {
    override val name = "FacebookReelsDetector"
    override val version = "1.1.0"

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals("com.facebook.katana", ignoreCase = true) ||
               packageName.equals("com.facebook.lite", ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockFacebookReels) {
            return DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Facebook Reels blocking disabled")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        if (context.hasAnyViewId("reel_fullscreen_view", "fb_shorts_container", "reels_tab_container")) {
            confidence += 0.75f
            reasons.add("Facebook reel full-screen container detected")
        }

        if (context.hasText("Reels and short videos") || context.hasText("Create reel") || context.hasContentDescription("Reel by")) {
            confidence += 0.40f
            reasons.add("Facebook Reel tokens found")
        }

        val finalConfidence = confidence.coerceIn(0f, 1f)
        val threshold = if (config.strictMode) 0.55f else 0.70f

        return if (finalConfidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = finalConfidence,
                category = ContentCategory.FACEBOOK_REELS,
                ruleId = "FB_REELS_",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.FACEBOOK_REELS, "Insufficient Facebook Reels signals")
        }
    }
}
