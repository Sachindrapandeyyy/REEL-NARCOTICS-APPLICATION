package com.zenith.focus.accessibility

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.accessibility.detector.UniversalShortFormAppDetector
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UniversalShortFormAppDetectorTest {

    private lateinit var detector: UniversalShortFormAppDetector
    private val defaultConfig = ProtectionConfig()

    @Before
    fun setUp() {
        detector = UniversalShortFormAppDetector()
    }

    @Test
    fun testBlocksDedicatedShortFormAppsWhenLockActive() {
        val config = ProtectionConfig(blockOtherShortVideo = true, blockTikTok = true)
        val shortFormApps = listOf(
            "com.zhiliaoapp.musically",      // TikTok Global
            "com.ss.android.ugc.trill",      // TikTok Asian / Lite
            "com.ss.android.ugc.aweme",      // Douyin
            "in.mohalla.video",              // Moj
            "com.eterno.shortvideos",        // Josh
            "in.mohalla.sharechat",          // ShareChat
            "io.chingari.app",               // Chingari
            "com.zee5.hipi",                 // Hipi
            "com.roposo.android",            // Roposo
            "com.next.innovation.takatak"    // MX TakaTak
        )

        for (pkg in shortFormApps) {
            assertTrue("Detector must handle short-form app: $pkg", detector.canHandle(pkg))

            val context = ScreenContext(packageName = pkg)
            val result = detector.evaluate(context, config)

            assertTrue("App $pkg must be blocked when lock is active", result.isBlocked)
            assertEquals("Confidence must be 1.0", 1.0f, result.confidence, 0.001f)
            assertTrue("Category must be TikTok or OTHER_SHORT_VIDEO",
                result.category == ContentCategory.TIKTOK || result.category == ContentCategory.OTHER_SHORT_VIDEO)
            assertEquals("UNIVERSAL_SHORT_FORM_APP", result.ruleId)
        }
    }

    @Test
    fun testAllowsDedicatedShortFormAppsWhenLockInactive() {
        val pkg = "in.mohalla.video" // Moj
        val context = ScreenContext(packageName = pkg)
        val config = ProtectionConfig(blockOtherShortVideo = false, blockTikTok = false)
        val result = detector.evaluate(context, config)

        assertFalse("App must not be blocked when config is disabled", result.isBlocked)
    }

    @Test
    fun testDoesNotHandleProductiveOrSystemApps() {
        val nonShortFormApps = listOf(
            "com.google.android.gm",
            "com.slack",
            "com.android.calculator2",
            "com.android.chrome",
            "com.whatsapp",
            "com.spotify.music"
        )

        for (pkg in nonShortFormApps) {
            assertFalse("Detector must not handle non-short-form app: $pkg", detector.canHandle(pkg))
        }
    }
}
