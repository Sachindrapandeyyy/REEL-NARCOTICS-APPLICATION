package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class GenericShortFormDetector : ContentDetector {
    override val name = "GenericShortFormDetector"
    override val version = "1.0.0"

    override fun canHandle(packageName: String): Boolean {
        // Fallback detector for any other app when blockOtherShortVideo is enabled
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

        // Heuristic 2: Short video audio/remix action tokens
        if (context.hasText("Original sound") || context.hasText("Use this sound") || context.hasText("Remix")) {
            confidence += 0.40f
            reasons.add("Short video audio tokens found")
        }

        val finalConfidence = confidence.coerceIn(0f, 1f)
        val threshold = if (config.strictMode) 0.60f else 0.75f

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
