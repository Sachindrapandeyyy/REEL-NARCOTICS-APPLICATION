package com.zenith.focus.accessibility

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.accessibility.detector.BrowserUrlDetector
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BrowserUrlDetectorTest {

    private lateinit var detector: BrowserUrlDetector
    private val config = ProtectionConfig(
        browserProtectionEnabled = true,
        blockYouTubeShorts = true,
        blockInstagramReels = true,
        blockFacebookReels = true,
        blockTikTok = true,
        blockAdultWebsites = true
    )

    @Before
    fun setUp() {
        detector = BrowserUrlDetector()
    }

    @Test
    fun testBlocksShortFormVideoWebUrls() {
        val testCases = listOf(
            "https://m.youtube.com/shorts/abc123" to ContentCategory.YOUTUBE_SHORTS,
            "https://www.youtube.com/shorts/xyz789" to ContentCategory.YOUTUBE_SHORTS,
            "https://www.instagram.com/reels/" to ContentCategory.INSTAGRAM_REELS,
            "https://www.instagram.com/reel/C7xyz123/" to ContentCategory.INSTAGRAM_REELS,
            "https://www.facebook.com/reel/10293847" to ContentCategory.FACEBOOK_REELS,
            "https://m.facebook.com/reel/555444333" to ContentCategory.FACEBOOK_REELS,
            "https://www.tiktok.com/@someuser" to ContentCategory.TIKTOK
        )

        for ((url, expectedCategory) in testCases) {
            val context = ScreenContext(
                packageName = "com.android.chrome",
                visibleTexts = listOf(url),
                nodeTextMap = mapOf("url_bar" to url)
            )
            val result = detector.evaluate(context, config)
            assertTrue("URL $url must be blocked", result.isBlocked)
            assertEquals("Category must match for $url", expectedCategory, result.category)
        }
    }

    @Test
    fun testAllowsEducationalAndLongFormWebUrls() {
        val allowedUrls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/results?search_query=android+development",
            "https://m.youtube.com/results?search_query=jetpack+compose",
            "https://www.instagram.com/direct/inbox/",
            "https://en.wikipedia.org/wiki/Biology",
            "https://www.google.com/search?q=kotlin+coroutines",
            "https://github.com/Sachindrapandeyyy"
        )

        for (url in allowedUrls) {
            val context = ScreenContext(
                packageName = "com.android.chrome",
                visibleTexts = listOf(url),
                nodeTextMap = mapOf("url_bar" to url)
            )
            val result = detector.evaluate(context, config)
            assertFalse("URL $url must be allowed", result.isBlocked)
        }
    }

    @Test
    fun testPathBasedBlocklistRulesMatchExactAndNestedBoardPathsOnly() {
        val pathDetector = BrowserUrlDetector(
            initialBlockedDomains = setOf("4chan.org/b/", "4chan.org/h/")
        )

        val blockedUrls = listOf(
            "https://4chan.org/b/",
            "https://boards.4chan.org/b/thread/999123",
            "4chan.org/h/catalog"
        )
        for (url in blockedUrls) {
            val context = ScreenContext(
                packageName = "com.android.chrome",
                visibleTexts = listOf(url),
                nodeTextMap = mapOf("url_bar" to url)
            )
            val result = pathDetector.evaluate(context, config)
            assertTrue("Path rule must block $url", result.isBlocked)
            assertEquals("ADULT_URL_PATH_MATCH", result.ruleId)
        }

        val allowedUrls = listOf(
            "https://boards.4chan.org/g/",
            "https://4chan.org/bistro/menu"
        )
        for (url in allowedUrls) {
            val context = ScreenContext(
                packageName = "com.android.chrome",
                visibleTexts = listOf(url),
                nodeTextMap = mapOf("url_bar" to url)
            )
            val result = pathDetector.evaluate(context, config)
            assertFalse("Unblocked path $url must remain allowed", result.isBlocked)
        }
    }
}
