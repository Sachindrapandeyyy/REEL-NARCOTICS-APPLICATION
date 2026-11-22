package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig
import java.util.Locale

/**
 * Universal Short-Form Video Applications Detector.
 * Surgically blocks dedicated short-form video applications on launch during active locks.
 */
class UniversalShortFormAppDetector : ContentDetector {
    override val name = "UniversalShortFormAppDetector"
    override val version = "1.0.0"

    companion object {
        val DEDICATED_SHORT_FORM_PACKAGES = setOf(
            // TikTok & Douyin ecosystem
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.zhiliaoapp.musically.go",
            "com.ss.android.ugc.tiktok.lite",
            "com.ss.android.ugc.aweme",
            "com.ss.android.ugc.aweme.lite",
            // Moj & Moj Lite
            "in.mohalla.video",
            "in.mohalla.video.lite",
            // Josh
            "com.eterno.shortvideos",
            "com.eterno.shortvideos.lite",
            // ShareChat (Short video feed)
            "in.mohalla.sharechat",
            // Chingari
            "io.chingari.app",
            // Hipi
            "com.zee5.hipi",
            // Roposo
            "com.roposo.android",
            // MX TakaTak
            "com.next.innovation.takatak"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val lower = packageName.lowercase(Locale.US)
        return DEDICATED_SHORT_FORM_PACKAGES.contains(lower) ||
                lower == "in.mohalla.video" ||
                lower == "com.eterno.shortvideos" ||
                lower == "io.chingari.app" ||
                lower == "com.zee5.hipi" ||
                lower == "com.roposo.android" ||
                lower == "com.next.innovation.takatak"
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        val pkg = context.packageName.lowercase(Locale.US)
        val isBlockedByConfig = config.blockOtherShortVideo || config.blockTikTok

        if (!isBlockedByConfig) {
            return DetectionResult.allowed(ContentCategory.OTHER_SHORT_VIDEO, "Short-form video apps permitted by settings")
        }

        val appName = when {
            pkg.contains("mohalla.video") -> "Moj"
            pkg.contains("eterno") -> "Josh"
            pkg.contains("sharechat") -> "ShareChat"
            pkg.contains("chingari") -> "Chingari"
            pkg.contains("hipi") -> "Hipi"
            pkg.contains("roposo") -> "Roposo"
            pkg.contains("takatak") -> "MX TakaTak"
            pkg.contains("musically") || pkg.contains("tiktok") || pkg.contains("aweme") || pkg.contains("trill") -> "TikTok"
            else -> "Short-form Video App"
        }

        return DetectionResult(
            isBlocked = true,
            confidence = 1.0f,
            category = if (appName == "TikTok") ContentCategory.TIKTOK else ContentCategory.OTHER_SHORT_VIDEO,
            ruleId = "UNIVERSAL_SHORT_FORM_APP",
            reason = "$appName is a dedicated short-form video app and is blocked during active focus."
        )
    }
}
