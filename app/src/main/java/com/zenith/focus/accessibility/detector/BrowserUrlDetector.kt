package com.zenith.focus.accessibility.detector

import android.content.Context
import com.zenith.focus.accessibility.analyzer.ScreenContext
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.util.Locale

class BrowserUrlDetector(
    context: Context? = null,
    initialBlockedDomains: Set<String> = emptySet()
) : ContentDetector {
    override val name = "BrowserUrlDetector"
    override val version = "1.4.0"

    private val blockedDomains = mutableSetOf<String>()
    private val blockedSuffixes = setOf(".porn", ".xxx", ".adult", ".cam", ".sex")

    companion object {
        val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "org.mozilla.firefox",
            "org.mozilla.fenix",
            "org.mozilla.focus",
            "com.microsoft.emmx",
            "com.brave.browser",
            "com.sec.android.app.sbrowser",
            "com.sec.android.app.sbrowser.beta",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.opera.gx",
            "com.opera.touch",
            "com.duckduckgo.mobile.android",
            "com.vivaldi.browser",
            // OEM and Global popular browsers
            "com.transsion.phoenix", // Phoenix Browser
            "com.shalltry.browser",
            "com.UCMobile.intl", // UC Browser
            "com.uc.browser.en",
            "com.uc.browser.hd",
            "com.mi.globalbrowser", // Mi Browser
            "com.mi.globalbrowser.mini", // Mint Browser
            "com.coloros.browser", // Oppo Browser
            "com.heytap.browser", // Realme / HeyTap Browser
            "com.vivo.browser", // Vivo Browser
            "com.huawei.browser", // Huawei Browser
            "com.kiwibrowser.browser", // Kiwi Browser
            "com.jio.web", // JioPages
            "mark.via.gp", // Via Browser
            "org.torproject.torbrowser", // Tor Browser
            "com.yandex.browser", // Yandex Browser
            "com.aloha.browser" // Aloha Browser
        )

        private val URL_BAR_VIEW_IDS = arrayOf(
            "url_bar",
            "search_box_text",
            "toolbar",
            "mozac_browser_toolbar_url_view",
            "search_box",
            "location_bar_edit_text",
            "addressbar",
            "omnibox"
        )

        val KNOWN_ADULT_ROOTS = setOf(
            "pornhub", "xvideos", "xnxx", "redtube", "youporn", "xhamster",
            "brazzers", "chaturbate", "stripchat", "eporner", "beeg", "spankbang",
            "rule34", "nhentai", "doujins", "hanime", "cam4", "bongacams",
            "myfreecams", "camsoda", "faphouse", "jerkmate", "txxx", "tube8",
            "spankwire", "drtuber", "bangbros", "naughtyamerica", "realitykings",
            "twistys", "mofos", "blacked", "tushy", "deeper", "adultwork",
            "manyvids", "shemalez", "livejasmin", "flirt4free", "streamate",
            "imlive", "fansly", "missav", "jable", "thumbzilla", "motherless",
            "heavy-r", "hardcoresex", "milfporn", "deepthroat", "cumshot",
            "creampie", "desiporn", "bhabhisex", "chudai"
        )

        private val EXPLICIT_WORD_REGEX = Regex("\\b(porn|porno|pornography|xxx|hentai|blowjob|dildo)\\b", RegexOption.IGNORE_CASE)

        fun isBrowserPackage(packageName: String): Boolean {
            if (packageName.isBlank()) return false
            val pkg = packageName.lowercase(Locale.US)
            if (BROWSER_PACKAGES.contains(pkg)) return true
            return pkg.contains(".browser") ||
                   pkg.endsWith(".browser") ||
                   pkg.contains("phoenix") ||
                   pkg.contains("ucmobile")
        }

        fun hasExplicitAdultContent(text: String): Boolean {
            if (text.isBlank()) return false
            val lower = text.lowercase(Locale.US)
            for (root in KNOWN_ADULT_ROOTS) {
                if (lower.contains(root)) return true
            }
            if (EXPLICIT_WORD_REGEX.containsMatchIn(lower)) return true
            for (suffix in setOf(".porn", ".xxx", ".adult", ".cam", ".sex")) {
                if (lower.contains(suffix)) return true
            }
            return false
        }
    }

    init {
        blockedDomains.addAll(initialBlockedDomains)
        context?.let { loadBlocklist(it) }
    }

    private fun loadBlocklist(context: Context) {
        runCatching {
            context.assets.open("adult_blocklist.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                    lines.forEach { line ->
                        val clean = line.trim().lowercase(Locale.US)
                        if (clean.isNotBlank() && !clean.startsWith("#")) {
                            blockedDomains.add(clean)
                        }
                    }
                }
            }
        }
    }

    override fun canHandle(packageName: String): Boolean {
        return isBrowserPackage(packageName)
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockAdultWebsites || !config.browserProtectionEnabled) {
            return DetectionResult.allowed(ContentCategory.ADULT_WEBSITE, "Browser adult protection disabled")
        }

        val addressBarText = findAddressBarText(context)

        // 1. If address bar contains raw search terms (not a full URL), inspect for adult keywords/roots immediately
        if (addressBarText.isNotBlank() && !isUrlLike(addressBarText) && hasExplicitAdultContent(addressBarText)) {
            return DetectionResult(
                isBlocked = true,
                confidence = 1.0f,
                category = ContentCategory.ADULT_WEBSITE,
                ruleId = "ADULT_ADDRESS_BAR_MATCH",
                reason = "Explicit adult query detected in address bar: $addressBarText"
            )
        }

        // 2. Find URL text from address bar node or visible texts
        val extractedUrl = if (addressBarText.isNotBlank() && isUrlLike(addressBarText)) {
            addressBarText
        } else {
            findUrlText(context)
        }

        if (extractedUrl.isBlank()) {
            return DetectionResult.allowed(ContentCategory.ADULT_WEBSITE, "No URL detected in browser surface")
        }

        val domain = extractDomain(extractedUrl)
        if (domain.isBlank()) {
            return DetectionResult.allowed(ContentCategory.ADULT_WEBSITE, "Unable to extract domain from: $extractedUrl")
        }

        // 3. Check blocked domain suffixes (.xxx, .porn, etc.)
        for (suffix in blockedSuffixes) {
            if (domain.endsWith(suffix)) {
                return DetectionResult(
                    isBlocked = true,
                    confidence = 1.0f,
                    category = ContentCategory.ADULT_WEBSITE,
                    ruleId = "ADULT_TLD_MATCH",
                    reason = "Adult top level domain ($suffix) detected: $domain"
                )
            }
        }

        // 4. Check exact or subdomain match against offline blocklist
        if (isDomainBlocked(domain)) {
            return DetectionResult(
                isBlocked = true,
                confidence = 1.0f,
                category = ContentCategory.ADULT_WEBSITE,
                ruleId = "ADULT_DOMAIN_MATCH",
                reason = "Known adult domain blocked: $domain"
            )
        }

        // 5. Inspect full URL (query parameters, search paths) for adult keywords
        if (hasExplicitAdultContent(extractedUrl)) {
            return DetectionResult(
                isBlocked = true,
                confidence = 1.0f,
                category = ContentCategory.ADULT_WEBSITE,
                ruleId = "ADULT_URL_CONTENT_MATCH",
                reason = "Explicit adult query or path detected in URL: $extractedUrl"
            )
        }

        return DetectionResult.allowed(ContentCategory.ADULT_WEBSITE, "Safe domain: $domain")
    }

    private fun findAddressBarText(context: ScreenContext): String {
        for (urlId in URL_BAR_VIEW_IDS) {
            val addressText = context.nodeTextMap.entries.firstOrNull { (id, text) ->
                id.contains(urlId, ignoreCase = true) && text.isNotBlank()
            }?.value
            if (!addressText.isNullOrBlank()) {
                return addressText.trim()
            }
        }
        return ""
    }

    private fun findUrlText(context: ScreenContext): String {
        // 1. Prioritize text from known browser address bar view IDs
        for (urlId in URL_BAR_VIEW_IDS) {
            val addressText = context.nodeTextMap.entries.firstOrNull { (id, text) ->
                id.contains(urlId, ignoreCase = true) && isUrlLike(text)
            }?.value
            if (!addressText.isNullOrBlank()) {
                return addressText.trim()
            }
        }

        // 2. Fallback: inspect visibleTexts with strict URL structure checks (never match phrases with spaces)
        for (text in context.visibleTexts) {
            val clean = text.trim()
            if (isUrlLike(clean)) {
                return clean
            }
        }
        return ""
    }

    private fun isUrlLike(text: String): Boolean {
        val clean = text.trim()
        if (clean.contains(" ") || clean.contains("\n") || clean.length < 4 || clean.length > 256) return false
        if (clean.startsWith("http://", ignoreCase = true) || clean.startsWith("https://", ignoreCase = true)) return true
        if (!clean.contains(".")) return false
        if (blockedSuffixes.any { clean.endsWith(it, ignoreCase = true) || clean.contains("$it/", ignoreCase = true) }) return true
        val dotIdx = clean.lastIndexOf('.')
        if (dotIdx in 1 until clean.length - 2) {
            val tld = clean.substring(dotIdx + 1).takeWhile { it.isLetter() }
            return tld.length >= 2
        }
        return false
    }

    fun extractDomain(rawUrl: String): String {
        val withProtocol = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://"
        } else {
            rawUrl
        }

        val host = runCatching {
            URI(withProtocol).host
        }.getOrNull() ?: rawUrl.split("/").firstOrNull() ?: rawUrl

        var cleaned = host.lowercase(Locale.US).trim()
        if (cleaned.startsWith("www.")) cleaned = cleaned.substring(4)
        if (cleaned.startsWith("m.")) cleaned = cleaned.substring(2)
        return cleaned
    }

    private fun isDomainBlocked(domain: String): Boolean {
        if (blockedDomains.contains(domain)) return true

        // Subdomain matching (e.g. video.pornhub.com -> pornhub.com)
        for (blocked in blockedDomains) {
            if (domain.endsWith(".$blocked")) {
                return true
            }
        }
        return false
    }

    fun addCustomBlockedDomain(domain: String) {
        blockedDomains.add(domain.lowercase(Locale.US).trim())
    }
}
