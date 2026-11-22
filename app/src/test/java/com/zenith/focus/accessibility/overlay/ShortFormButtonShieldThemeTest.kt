package com.zenith.focus.accessibility.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class ShortFormButtonShieldThemeTest {

    @Test
    fun `test YouTube background colors for dark and light themes`() {
        val darkColor = ShortFormButtonShieldTheme.getBackgroundColor("com.google.android.youtube", isDarkMode = true)
        assertEquals(0xFF0F0F0F.toInt(), darkColor)

        val lightColor = ShortFormButtonShieldTheme.getBackgroundColor("com.google.android.youtube", isDarkMode = false)
        assertEquals(0xFFFFFFFF.toInt(), lightColor)
    }

    @Test
    fun `test Social background colors for dark and light themes`() {
        val igDark = ShortFormButtonShieldTheme.getBackgroundColor("com.instagram.android", isDarkMode = true)
        assertEquals(0xFF000000.toInt(), igDark)

        val igLight = ShortFormButtonShieldTheme.getBackgroundColor("com.instagram.android", isDarkMode = false)
        assertEquals(0xFFFFFFFF.toInt(), igLight)

        val fbDark = ShortFormButtonShieldTheme.getBackgroundColor("com.facebook.katana", isDarkMode = true)
        assertEquals(0xFF000000.toInt(), fbDark)

        val snapLight = ShortFormButtonShieldTheme.getBackgroundColor("com.snapchat.android", isDarkMode = false)
        assertEquals(0xFFFFFFFF.toInt(), snapLight)
    }

    @Test
    fun `test Dot indicator colors for dark and light themes`() {
        val darkDot = ShortFormButtonShieldTheme.getDotColor(isDarkMode = true)
        assertEquals(0x40FFFFFF.toInt(), darkDot)

        val lightDot = ShortFormButtonShieldTheme.getDotColor(isDarkMode = false)
        assertEquals(0x40000000.toInt(), lightDot)
    }
}
