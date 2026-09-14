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
    override val version = "1.3.1"

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
            "com.opera.browser",
            "com.opera.mini.native",
            "com.opera.gx",
            "com.duckduckgo.mobile.android",
            "com.vivaldi.browser"
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
        return BROWSER_PACKAGES.contains(packageName.lowercase(Locale.US))
    }

    override fun evaluate(context: ScreenContext, config: ProtectionConfig): DetectionResult {
        if (!config.blockAdultWebsites || !config.browserProtectionEnabled) {
            return DetectionResult.allowed(ContentCategory.ADULT_WEBSITE, "Browser adult protection disabled")
        }

        // Find URL text from address bar node or visible texts
        val extractedUrl = findUrlText(context)
        if (extractedUrl.isBlank()) {
            return DetectionResult.allowed(ContentCategory.ADULT_WEBSITE, "No URL detected in browser surface")
        }

        val domain = extractDomain(extractedUrl)
        if (domain.isBlank()) {
            return DetectionResult.allowed(ContentCategory.ADULT_WEBSITE, "Unable to extract domain from: $extractedUrl")
        }

        // 1. Check blocked domain suffixes (.xxx, .porn, etc.)
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

        // 2. Check exact or subdomain match against offline blocklist
        if (isDomainBlocked(domain)) {
            return DetectionResult(
                isBlocked = true,
                confidence = 1.0f,
                category = ContentCategory.ADULT_WEBSITE,
                ruleId = "ADULT_DOMAIN_MATCH",
                reason = "Known adult domain blocked: $domain"
            )
        }

        return DetectionResult.allowed(ContentCategory.ADULT_WEBSITE, "Safe domain: $domain")
    }

    private fun findUrlText(context: ScreenContext): String {
        // Look in visible texts for URL-like patterns
        for (text in context.visibleTexts) {
            val clean = text.trim()
            if (clean.contains(".") && (clean.startsWith("http://") || clean.startsWith("https://") || clean.contains(".com") || clean.contains(".net") || clean.contains(".org") || clean.contains(".xxx") || clean.contains(".porn") || clean.contains(".tv") || clean.contains(".io"))) {
                return clean
            }
        }
        return ""
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
