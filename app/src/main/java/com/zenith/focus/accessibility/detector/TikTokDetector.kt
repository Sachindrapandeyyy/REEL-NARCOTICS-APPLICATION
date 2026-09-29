package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class TikTokDetector : ContentDetector {
    override val name = "TikTokDetector"
    override val version = "1.1.0"

    companion object {
        val TIKTOK_PACKAGES = setOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.zhiliaoapp.musically.go",
            "com.ss.android.ugc.tiktok.lite",
            "com.ss.android.ugc.aweme",
            "com.ss.android.ugc.aweme.lite"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        val lower = packageName.lowercase(java.util.Locale.US)
        return TIKTOK_PACKAGES.contains(lower) ||
               lower.contains("musically") ||
               lower.contains("tiktok")
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
