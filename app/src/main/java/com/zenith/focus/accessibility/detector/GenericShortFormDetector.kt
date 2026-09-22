package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class GenericShortFormDetector : ContentDetector {
    override val name = "GenericShortFormDetector"
    override val version = "1.1.0"

    companion object {
        private val SPECIALIZED_PACKAGES = setOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.facebook.katana",
            "com.facebook.lite",
            "com.facebook.orca",
            "com.snapchat.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.zhiliaoapp.musically.go"
        )

        private val EXCLUDED_PACKAGE_PREFIXES = listOf(
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.messaging",
            "com.google.android.gm",
            "com.google.android.apps.photos",
            "com.google.android.apps.docs",
            "com.google.android.keep",
            "com.google.android.calculator",
            "com.google.android.deskclock",
            "com.google.android.apps.maps",
            "com.android.vending",
            "com.android.settings",
            "com.android.phone",
            "com.google.android.dialer",
            "com.android.incallui",
            "com.android.mms",
            "com.whatsapp",
            "org.telegram.messenger"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        val pkg = packageName.lowercase(java.util.Locale.US)

        // Never inspect packages that have dedicated specialized detectors
        if (SPECIALIZED_PACKAGES.contains(pkg)) {
            return false
        }
        
        // Never inspect our own app, launchers, or system UI
        if (pkg.contains("zenith") || pkg.contains("launcher") || pkg.contains("systemui")) {
            return false
        }

        // Never inspect web browsers (handled by BrowserUrlDetector and AdultContentDetector)
        if (BrowserUrlDetector.BROWSER_PACKAGES.contains(pkg)) {
            return false
        }

        // Never inspect core utilities, communication, camera, gallery, settings, or educational platforms
        if (EXCLUDED_PACKAGE_PREFIXES.any { pkg.startsWith(it) }) {
            return false
        }

        if (pkg.contains("camera") || pkg.contains("gallery") || pkg.contains("dialer") ||
            pkg.contains("phone") || pkg.contains("contact") || pkg.contains("calculator") ||
            pkg.contains("clock") || pkg.contains("calendar") || pkg.contains("keyboard") ||
            pkg.contains("inputmethod") || pkg.contains("settings") || pkg.contains("installer") ||
            pkg.contains("penpencil") || pkg.contains("physicswalla") || pkg.contains("unacademy") ||
            pkg.contains("khanacademy") || pkg.contains("coursera") || pkg.contains("udemy") ||
            pkg.contains("byjus") || pkg.contains("vedantu") || pkg.contains("doubtnut") ||
            pkg.contains("allen") || pkg.contains("testbook")
        ) {
            return false
        }

        return true
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockOtherShortVideo) {
            return DetectionResult.allowed(ContentCategory.OTHER_SHORT_VIDEO, "Other short video blocking disabled")
        }

        var confidence = 0.0f
        val reasons = mutableListOf<String>()

        // Heuristic 1: Vertical ViewPager2 taking full screen
        if (context.hasViewId("view_pager") || context.className.contains("ViewPager2")) {
            confidence += 0.35f
            reasons.add("Vertical pager layout identified")
        }

        // Heuristic 2: Definite short video audio signature
        if (context.hasText("Original sound") || context.hasText("Use this sound") || context.hasText("Original Audio")) {
            confidence += 0.40f
            reasons.add("Short video audio signature found")
        }

        // Heuristic 3: Vertical short-video action bar or explicit short hashtags
        val hasShortHashtag = context.visibleTexts.any { 
            val lower = it.lowercase(java.util.Locale.US)
            lower.contains("#shorts") || lower.contains("#reels") || lower.contains("#shortvideo")
        }
        if (hasShortHashtag) {
            confidence += 0.35f
            reasons.add("Short-form hashtag detected")
        }

        val hasActionOverlay = context.hasAnyViewId(
            "action_like", "action_share", "like_button", "share_button",
            "comment_button", "action_comment", "reel_action"
        )
        if (hasActionOverlay) {
            confidence += 0.25f
            reasons.add("Short-form vertical action buttons detected")
        }

        val finalConfidence = confidence.coerceIn(0f, 1f)
        val threshold = if (config.strictMode) 0.75f else 0.85f

        return if (finalConfidence >= threshold) {
            DetectionResult(
                isBlocked = true,
                confidence = finalConfidence,
                category = ContentCategory.OTHER_SHORT_VIDEO,
                ruleId = "GENERIC_SHORT_",
                reason = reasons.joinToString("; ")
            )
        } else {
            DetectionResult.allowed(ContentCategory.OTHER_SHORT_VIDEO, "Insufficient generic short video signals")
        }
    }
}
