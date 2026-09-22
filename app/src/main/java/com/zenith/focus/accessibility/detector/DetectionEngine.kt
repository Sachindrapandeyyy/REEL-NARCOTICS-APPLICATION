package com.zenith.focus.accessibility.detector

import android.content.Context
import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class DetectionEngine(context: Context) {
    val version: String = "1.0.0"

    private val youtubeShortsDetector = YouTubeShortsDetector()
    private val instagramReelsDetector = InstagramReelsDetector()
    private val snapchatSpotlightDetector = SnapchatSpotlightDetector()
    private val facebookReelsDetector = FacebookReelsDetector()
    private val tiktokDetector = TikTokDetector()
    private val browserUrlDetector = BrowserUrlDetector(context)
    private val adultContentDetector = AdultContentDetector()
    private val genericShortFormDetector = GenericShortFormDetector()

    private val detectors: List<ContentDetector> = listOf(
        youtubeShortsDetector,
        instagramReelsDetector,
        snapchatSpotlightDetector,
        facebookReelsDetector,
        tiktokDetector,
        browserUrlDetector,
        adultContentDetector,
        genericShortFormDetector
    )

    fun evaluate(screenContext: ScreenContext, config: ProtectionConfig): DetectionResult {
        val pkg = screenContext.packageName

        // Check app-specific specialized detectors first (Specialized Authority Model)
        for (detector in detectors) {
            if (detector !is GenericShortFormDetector && detector.canHandle(pkg)) {
                return detector.evaluate(screenContext, config)
            }
        }

        // Only fallback to generic detector if no specialized detector handled this app
        if (genericShortFormDetector.canHandle(pkg)) {
            return genericShortFormDetector.evaluate(screenContext, config)
        }

        return DetectionResult.allowed(ContentCategory.OTHER_SHORT_VIDEO, "Screen verified clean")
    }
}
