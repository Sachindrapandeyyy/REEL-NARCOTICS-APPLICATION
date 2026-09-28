package com.zenith.focus.accessibility.detector

import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

class AdultContentDetector : ContentDetector {
    override val name = "AdultContentDetector"
    override val version = "1.4.0"

    companion object {
        // Tier 1: Definite, unambiguous pornography / adult tokens (strictly zero false-positive tolerance)
        val TIER_1_EXPLICIT_TOKENS = setOf(
            "porn", "porno", "pornography", "xxx", "hentai", "redtube",
            "xvideos", "xnxx", "youporn", "xhamster", "brazzers", "chaturbate",
            "stripchat", "onlyfans", "camgirl", "sexvideo",
            "hardcoresex", "milfporn", "blowjob", "deepthroat", "cumshot",
            "creampie", "dildo", "faphouse", "jerkmate",
            "pornhub", "eporner", "spankbang", "beeg", "rule34", "nhentai",
            "doujins", "hanime", "cam4", "bongacams", "myfreecams", "camsoda",
            "nsfw", "cybersex", "femdom", "jav", "incest", "boobjob", "handjob",
            "cuckold", "pornvideo", "pornstar", "freecam", "livesex", "sexchat",
            "chudai", "bhabhisex", "desiporn", "desi_porn", "xxxvideo", "xvideo"
        )

        // Tier 2: Suspicious tokens requiring multi-signal confirmation (pruned of ambiguous words)
        val TIER_2_SUSPICIOUS_TOKENS = setOf(
            "fetish", "erotica", "stripper", "boobs", "horny", "slut",
            "masturbate", "bdsm", "threesome", "orgy", "fap", "shemale",
            "gangbang", "erotic"
        )

        // Benign tokens to protect legitimate educational/medical contexts for Tier 2 ambiguous words
        val SAFE_CONTEXT_TOKENS = setOf(
            "documentary", "biology", "medical", "anatomy", "health", "education",
            "therapy", "sussex", "scunthorpe", "psychology", "clinic",
            "wikipedia", "study", "research", "science", "academic"
        )
    }

    override fun canHandle(packageName: String): Boolean {
        // Strictly inspect supported web browsers and OEM browsers only; never inspect non-browser apps
        return BrowserUrlDetector.isBrowserPackage(packageName)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockAdultKeywords) {
            return DetectionResult.allowed(ContentCategory.ADULT_KEYWORD, "Adult keyword blocking disabled")
        }

        val tokens = context.allNormalizedTokens
        if (tokens.isEmpty()) {
            return DetectionResult.allowed(ContentCategory.ADULT_KEYWORD, "No text tokens on screen")
        }

        // 1. Scan Tier 1 unambiguous explicit tokens (Unconditional: cannot be bypassed by safe words or web footers)
        val tier1Matches = tokens.filter { TIER_1_EXPLICIT_TOKENS.contains(it) }
        if (tier1Matches.isNotEmpty()) {
            return DetectionResult(
                isBlocked = true,
                confidence = 0.98f,
                category = ContentCategory.ADULT_KEYWORD,
                ruleId = "ADULT_TIER1_MATCH",
                reason = "Explicit adult tokens detected: ${tier1Matches.joinToString(", ")}"
            )
        }

        // 2. Guard against harmless educational / medical contexts for Tier 2 ambiguous tokens
        val hasSafeContext = tokens.any { SAFE_CONTEXT_TOKENS.contains(it) }

        // 3. Scan Tier 2 suspicious tokens (strictly require at least 2 distinct tokens AND no safe context)
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
