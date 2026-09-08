package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ProtectionConfig

interface ContentDetector {
    val name: String
    val version: String
    fun canHandle(packageName: String): Boolean
    fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult
}
