package com.zenith.focus.accessibility.overlay

import java.util.Locale

/**
 * Provides theme-aware colors for stealth button cover overlays on short-form tabs.
 * Perfectly masks the bright icons (Shorts/Reels) with matching background colors.
 */
object ShortFormButtonShieldTheme {
    const val COLOR_YOUTUBE_DARK = 0xFF0F0F0F.toInt()
    const val COLOR_YOUTUBE_LIGHT = 0xFFFFFFFF.toInt()
    const val COLOR_SOCIAL_DARK = 0xFF000000.toInt()
    const val COLOR_SOCIAL_LIGHT = 0xFFFFFFFF.toInt()

    const val COLOR_DOT_DARK = 0x40FFFFFF.toInt() // 25% alpha white
    const val COLOR_DOT_LIGHT = 0x40000000.toInt() // 25% alpha black

    fun getBackgroundColor(packageName: String, isDarkMode: Boolean): Int {
        val lower = packageName.lowercase(Locale.US)
        return if (lower.contains("youtube")) {
            if (isDarkMode) COLOR_YOUTUBE_DARK else COLOR_YOUTUBE_LIGHT
        } else {
            if (isDarkMode) COLOR_SOCIAL_DARK else COLOR_SOCIAL_LIGHT
        }
    }

    fun getDotColor(isDarkMode: Boolean): Int {
        return if (isDarkMode) COLOR_DOT_DARK else COLOR_DOT_LIGHT
    }
}
