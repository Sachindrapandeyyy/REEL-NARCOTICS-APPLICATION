package com.zenith.focus.accessibility.analyzer

data class ScreenContext(
    val packageName: String,
    val className: String = "",
    val viewIds: Set<String> = emptySet(),
    val visibleTexts: List<String> = emptyList(),
    val contentDescriptions: List<String> = emptyList(),
    val allNormalizedTokens: Set<String> = emptySet(),
    val selectedTexts: Set<String> = emptySet(),
    val selectedDescriptions: Set<String> = emptySet(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun hasViewId(partialOrFull: String): Boolean {
        return viewIds.any { it.contains(partialOrFull, ignoreCase = true) }
    }

    fun hasText(text: String, ignoreCase: Boolean = true): Boolean {
        return visibleTexts.any { it.contains(text, ignoreCase = ignoreCase) }
    }

    fun hasExactText(text: String, ignoreCase: Boolean = true): Boolean {
        return visibleTexts.any { it.equals(text, ignoreCase = ignoreCase) }
    }

    fun hasContentDescription(desc: String, ignoreCase: Boolean = true): Boolean {
        return contentDescriptions.any { it.contains(desc, ignoreCase = ignoreCase) }
    }

    fun hasSelectedText(text: String, ignoreCase: Boolean = true): Boolean {
        return selectedTexts.any { it.contains(text, ignoreCase = ignoreCase) }
    }

    fun hasSelectedDesc(desc: String, ignoreCase: Boolean = true): Boolean {
        return selectedDescriptions.any { it.contains(desc, ignoreCase = ignoreCase) }
    }

    fun containsAnyToken(tokens: Collection<String>): Boolean {
        return tokens.any { allNormalizedTokens.contains(it) }
    }

    fun hasAnyViewId(vararg ids: String): Boolean {
        return ids.any { hasViewId(it) }
    }
}
