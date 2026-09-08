package com.zenith.focus.accessibility.analyzer

import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

object TextNormalizer {
    private val DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    private val SEPARATOR_PATTERN = Pattern.compile("[\\s_\\-\\.\\*\\+~#]+")

    fun normalize(rawText: String): String {
        if (rawText.isBlank()) return ""

        // 1. Unicode decomposition
        val decomposed = Normalizer.normalize(rawText, Normalizer.Form.NFD)
        val strippedDiacritics = DIACRITICS_PATTERN.matcher(decomposed).replaceAll("")

        // 2. Lowercase
        var text = strippedDiacritics.lowercase(Locale.US)

        // 3. Remove zero-width characters
        text = text.replace("[\u200B-\u200D\uFEFF]".toRegex(), "")

        // 4. Decode common leetspeak substitutions
        val sb = StringBuilder(text.length)
        for (c in text) {
            val mapped = when (c) {
                '0' -> 'o'
                '1', '!', '|' -> 'i'
                '3' -> 'e'
                '4', '@' -> 'a'
                '5', '$' -> 's'
                '7', '+' -> 't'
                '8' -> 'b'
                else -> c
            }
            sb.append(mapped)
        }

        return sb.toString()
    }

    fun extractTokens(rawOrNormalizedText: String): Set<String> {
        val normalized = normalize(rawOrNormalizedText)
        if (normalized.isBlank()) return emptySet()
        val tokens = mutableSetOf<String>()

        // 1. Standard word extraction
        val words = normalized.split(SEPARATOR_PATTERN)
        for (w in words) {
            val clean = w.trim()
            if (clean.length >= 3) {
                tokens.add(clean)
            }
        }

        // 2. Look for spaced or dotted single-letter obfuscation like 'p.o.r.n' or 'p o r n'
        val dottedMatch = Regex("([a-z][\\s_\\.\\*\\-~#]){2,}[a-z]").findAll(normalized)
        for (match in dottedMatch) {
            val stripped = match.value.replace("[^a-z0-9]".toRegex(), "")
            if (stripped.length >= 3) {
                tokens.add(stripped)
            }
        }

        // 3. Compacted full string
        val compacted = normalized.replace("[^a-z0-9]".toRegex(), "")
        if (compacted.length >= 3) {
            tokens.add(compacted)
        }

        return tokens
    }
}
