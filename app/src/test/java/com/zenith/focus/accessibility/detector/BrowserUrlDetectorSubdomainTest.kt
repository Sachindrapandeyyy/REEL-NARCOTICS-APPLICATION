package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig
import org.junit.Assert.*
import org.junit.Test

class BrowserUrlDetectorSubdomainTest {

    private val blockedList = setOf("badsite.com", "pornhub.com", "explicitmedia.org")
    private val detector = BrowserUrlDetector(initialBlockedDomains = blockedList)
    private val config = ProtectionConfig(blockAdultWebsites = true, browserProtectionEnabled = true)

    @Test
    fun testExtractDomainStripsProtocolWwwAndMobile() {
        assertEquals("badsite.com", detector.extractDomain("https://www.badsite.com/video/123"))
        assertEquals("badsite.com", detector.extractDomain("http://m.badsite.com/feed"))
        assertEquals("badsite.com", detector.extractDomain("badsite.com/search?q=test"))
        assertEquals("sub.badsite.com", detector.extractDomain("https://sub.badsite.com/watch"))
    }

    @Test
    fun testExactDomainMatchIsBlocked() {
        val context = ScreenContext(
            packageName = "com.android.chrome",
            visibleTexts = listOf("https://badsite.com/feed")
        )
        val result = detector.evaluate(context, config)
        assertTrue("Exact domain match must be blocked", result.isBlocked)
        assertEquals(ContentCategory.ADULT_WEBSITE, result.category)
        assertEquals("ADULT_DOMAIN_MATCH", result.ruleId)
    }

    @Test
    fun testSubdomainMatchIsBlocked() {
        val context = ScreenContext(
            packageName = "com.android.chrome",
            visibleTexts = listOf("https://video.pornhub.com/view_video.php")
        )
        val result = detector.evaluate(context, config)
        assertTrue("Subdomain of blocked adult domain must be blocked", result.isBlocked)
        assertEquals(ContentCategory.ADULT_WEBSITE, result.category)
        assertEquals("ADULT_DOMAIN_MATCH", result.ruleId)
    }

    @Test
    fun testDeepSubdomainMatchIsBlocked() {
        val context = ScreenContext(
            packageName = "org.mozilla.firefox",
            visibleTexts = listOf("https://cdn.edge.explicitmedia.org/stream")
        )
        val result = detector.evaluate(context, config)
        assertTrue("Deep nested subdomain must be blocked", result.isBlocked)
        assertEquals(ContentCategory.ADULT_WEBSITE, result.category)
    }

    @Test
    fun testUnrelatedSimilarDomainIsNotBlocked() {
        // e.g. notbadsite.com should NOT match badsite.com
        val context = ScreenContext(
            packageName = "com.android.chrome",
            visibleTexts = listOf("https://notbadsite.com/article")
        )
        val result = detector.evaluate(context, config)
        assertFalse("Unrelated domain with suffix substring must not be falsely blocked", result.isBlocked)
    }

    @Test
    fun testSafeEducationalDomainAllowed() {
        val context = ScreenContext(
            packageName = "com.android.chrome",
            visibleTexts = listOf("https://en.wikipedia.org/wiki/Mathematics")
        )
        val result = detector.evaluate(context, config)
        assertFalse("Clean educational domain must be allowed", result.isBlocked)
    }
}
