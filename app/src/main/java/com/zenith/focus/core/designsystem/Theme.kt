package com.zenith.focus.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val EtherealColorScheme = lightColorScheme(
    primary = OrbitalCoral,
    onPrimary = Color.White,
    primaryContainer = EtherealSurfaceVariant,
    secondary = OrbitalViolet,
    onSecondary = Color.White,
    secondaryContainer = OrbitalVioletSoft,
    background = EtherealCanvas,
    surface = EtherealSurface,
    surfaceVariant = EtherealSurfaceVariant,
    onBackground = EtherealTextPrimary,
    onSurface = EtherealTextPrimary,
    outline = EtherealBorder
)

@Composable
fun ZenithFocusTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalEarthColors provides SingleEtherealColors) {
        MaterialTheme(
            colorScheme = EtherealColorScheme,
            content = content
        )
    }
}

