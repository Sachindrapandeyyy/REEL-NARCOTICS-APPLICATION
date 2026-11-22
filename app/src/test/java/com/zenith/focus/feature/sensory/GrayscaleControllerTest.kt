package com.zenith.focus.feature.sensory

import org.junit.Assert.*
import org.junit.Test

class GrayscaleControllerTest {

    @Test
    fun `constants match Android Secure Settings specifications`() {
        assertEquals("accessibility_display_daltonizer_enabled", GrayscaleController.DISPLAY_DALTONIZER_ENABLED)
        assertEquals("accessibility_display_daltonizer", GrayscaleController.DISPLAY_DALTONIZER)
        assertEquals(0, GrayscaleController.MONOCHROMACY_MODE)
    }

    @Test
    fun `adb grant command specifies correct permission and package`() {
        val command = GrayscaleController.ADB_GRANT_COMMAND
        assertTrue(command.contains("adb shell pm grant"))
        assertTrue(command.contains("com.zenith.focus"))
        assertTrue(command.contains("android.permission.WRITE_SECURE_SETTINGS"))
    }
}
