package com.zenith.focus.accessibility.detector

import com.zenith.focus.domain.model.ContentCategory

data class DetectionResult(
    val isBlocked: Boolean,
    val confidence: Float,
    val category: ContentCategory,
    val ruleId: String,
    val reason: String
) {
    companion object {
        fun allowed(category: ContentCategory, reason: String = "Normal content surface"): DetectionResult {
            return DetectionResult(
                isBlocked = false,
                confidence = 0f,
                category = category,
                ruleId = "ALLOW_SAFE",
                reason = reason
            )
        }
    }
}
