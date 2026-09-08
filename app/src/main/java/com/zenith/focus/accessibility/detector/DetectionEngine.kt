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

        // Check app-specific detectors first
        for (detector in detectors) {
            if (detector.canHandle(pkg)) {
                val result = detector.evaluate(screenContext, config)
                if (result.isBlocked) {
                    return result
                }
            }
        }

        return DetectionResult.allowed(ContentCategory.OTHER_SHORT_VIDEO, "Screen verified clean")
    }
}
