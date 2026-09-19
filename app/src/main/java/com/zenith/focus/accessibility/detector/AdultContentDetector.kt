package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class AdultContentDetector : ContentDetector {
    override val name = "AdultContentDetector"
    override val version = "1.3.0"

    companion object {
        // Tier 1: Definite, unambiguous pornography / adult tokens
        private val TIER_1_EXPLICIT_TOKENS = setOf(
            "porn", "porno", "pornography", "xxx", "hentai", "redtube",
            "xvideos", "xnxx", "youporn", "xhamster", "brazzers", "chaturbate",
            "stripchat", "onlyfans", "erotic", "gangbang", "camgirl", "sexvideo",
            "hardcoresex", "milfporn", "blowjob", "deepthroat", "cumshot",
            "creampie", "dildo", "faphouse", "jerkmate"
        )

        // Tier 2: Suspicious tokens requiring multi-signal confirmation (pruned of ambiguous words like escort/naked/sensual)
        private val TIER_2_SUSPICIOUS_TOKENS = setOf(
            "fetish", "erotica", "stripper", "boobs", "horny", "slut",
            "masturbate", "bdsm", "threesome", "orgy", "fap", "shemale"
        )

        // Benign tokens to protect harmless educational/governmental/news contexts (Scunthorpe guard)
        private val SAFE_CONTEXT_TOKENS = setOf(
            "documentary", "biology", "medical", "anatomy", "health", "education",
            "therapy", "sussex", "scunthorpe", "psychology", "clinic",
            "news", "article", "report", "police", "security", "government",
            "officer", "ias", "ips", "upsc", "convoy", "law", "court",
            "wikipedia", "study", "research", "exam", "holiday", "leave",
            "service", "official", "rules", "travel", "protocol", "minister",
            "president", "army", "military", "vehicle", "cosmetics", "makeup",
            "beauty", "palette", "recipe", "history", "science", "academic"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        val pkg = packageName.lowercase(java.util.Locale.US)
        // Strictly inspect supported web browsers only; never inspect non-browser apps or search engines
        return BrowserUrlDetector.BROWSER_PACKAGES.contains(pkg)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockAdultKeywords) {
            return DetectionResult.allowed(ContentCategory.ADULT_KEYWORD, "Adult keyword blocking disabled")
        }

        val tokens = context.allNormalizedTokens
        if (tokens.isEmpty()) {
            return DetectionResult.allowed(ContentCategory.ADULT_KEYWORD, "No text tokens on screen")
        }

        // 1. Guard against harmless educational / medical / governmental / news contexts
        val hasSafeContext = tokens.any { SAFE_CONTEXT_TOKENS.contains(it) }

        // 2. Scan Tier 1 unambiguous explicit tokens
        val tier1Matches = tokens.filter { TIER_1_EXPLICIT_TOKENS.contains(it) }
        if (tier1Matches.isNotEmpty() && !hasSafeContext) {
            return DetectionResult(
                isBlocked = true,
                confidence = 0.95f,
                category = ContentCategory.ADULT_KEYWORD,
                ruleId = "ADULT_TIER1_MATCH",
                reason = "Explicit tokens detected: ${tier1Matches.joinToString(", ")}"
            )
        }

        // 3. Scan Tier 2 suspicious tokens (strictly require at least 2 distinct tokens, never 1)
        val tier2Matches = tokens.filter { TIER_2_SUSPICIOUS_TOKENS.contains(it) }
        val threshold = 2
        if (tier2Matches.size >= threshold && !hasSafeContext) {
            return DetectionResult(
                isBlocked = true,
                confidence = 0.75f,
                category = ContentCategory.ADULT_KEYWORD,
                ruleId = "ADULT_TIER2_MATCH",
                reason = "Multiple adult indicator tokens detected: ${tier2Matches.joinToString(", ")}"
            )
        }

        return DetectionResult.allowed(ContentCategory.ADULT_KEYWORD, "No explicit adult indicators found")
    }
}
