package com.zenith.focus.accessibility.analyzer

data class ScreenContext(
    val packageName: String,
    val className: String = "",
    val classNames: Set<String> = emptySet(),
    val viewIds: Set<String> = emptySet(),
    val visibleTexts: List<String> = emptyList(),
    val contentDescriptions: List<String> = emptyList(),
    val allNormalizedTokens: Set<String> = emptySet(),
    val selectedTexts: Set<String> = emptySet(),
    val selectedDescriptions: Set<String> = emptySet(),
    val nodeTextMap: Map<String, String> = emptyMap(),
    val shortFormTabBounds: android.graphics.Rect? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun hasClassName(partial: String, ignoreCase: Boolean = true): Boolean {
        return className.contains(partial, ignoreCase = ignoreCase) ||
                classNames.any { it.contains(partial, ignoreCase = ignoreCase) }
    }

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

    /**
     * Checks whether a bottom navigation tab (e.g., "Shorts", "Reels", "Spotlight") is actively selected
     * without false-matching checked toggles like "Share to Reels".
     */
    fun isNavigationTabSelected(tabName: String): Boolean {
        val tab = tabName.trim().lowercase(java.util.Locale.US)
        fun matchesTabLabel(raw: String): Boolean {
            val l = raw.trim().lowercase(java.util.Locale.US)
            return l == tab ||
                l.startsWith("$tab,") ||
                l.startsWith("$tab tab") ||
                l.startsWith("selected, $tab") ||
                l.endsWith(", $tab") ||
                (l.contains(tab) && l.contains("tab"))
        }

        val inSelectedNodes = selectedDescriptions.any { matchesTabLabel(it) } ||
            selectedTexts.any { matchesTabLabel(it) }
        if (inSelectedNodes) return true

        return contentDescriptions.any { raw ->
            val l = raw.trim().lowercase(java.util.Locale.US)
            l == "$tab, selected" ||
                l == "$tab tab, selected" ||
                l == "selected, $tab" ||
                (l.startsWith(tab) && l.contains("selected"))
        }
    }

    fun containsAnyToken(tokens: Collection<String>): Boolean {
        return tokens.any { allNormalizedTokens.contains(it) }
    }

    fun hasAnyViewId(vararg ids: String): Boolean {
        return ids.any { hasViewId(it) }
    }
}
