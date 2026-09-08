package com.zenith.focus.accessibility

import com.zenith.focus.accessibility.analyzer.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun testLeetspeakDecoding() {
        assertEquals("porn", TextNormalizer.normalize("p0rn"))
        assertEquals("sex", TextNormalizer.normalize("s3x"))
        assertEquals("adult", TextNormalizer.normalize("@dult"))
        assertEquals("video", TextNormalizer.normalize("v1de0"))
    }

    @Test
    fun testUnicodeAndDiacriticsRemoval() {
        assertEquals("resume", TextNormalizer.normalize("résumé"))
        assertEquals("naive", TextNormalizer.normalize("naïve"))
    }

    @Test
    fun testTokenExtraction() {
        val tokens = TextNormalizer.extractTokens("Check this p.o.r.n video out!")
        println("EXTRACTED TOKENS: $tokens")
        assertTrue("Tokens was: $tokens", tokens.contains("video"))
    }
}
