package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class TikTokDetector : ContentDetector {
    override val name = "TikTokDetector"
    override val version = "1.0.0"

    override fun canHandle(packageName: String): Boolean {
        return packageName.equals("com.zhiliaoapp.musically", ignoreCase = true) ||
               packageName.equals("com.ss.android.ugc.trill", ignoreCase = true) ||
               packageName.equals("com.zhiliaoapp.musically.go", ignoreCase = true)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockTikTok) {
            return DetectionResult.allowed(ContentCategory.TIKTOK, "TikTok blocking disabled")
        }

        // TikTok is by definition a 100% short-video application
        return DetectionResult(
            isBlocked = true,
            confidence = 1.0f,
            category = ContentCategory.TIKTOK,
            ruleId = "TIKTOK_APP_ACTIVE",
            reason = "TikTok application active during focus lock"
        )
    }
}
