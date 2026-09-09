package com.zenith.focus.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EarthNightGreen,
    onPrimary = Color.White,
    primaryContainer = EarthNightSurfaceSoft,
    secondary = EarthNightCamel,
    onSecondary = Color.White,
    secondaryContainer = EarthNightCamelLight,
    background = EarthNightBg,
    surface = EarthNightSurface,
    surfaceVariant = EarthNightSurfaceVariant,
    onBackground = EarthNightText,
    onSurface = EarthNightText,
    outline = EarthNightBorder
)

private val LightColorScheme = lightColorScheme(
    primary = EarthForestGreen,
    onPrimary = Color.White,
    primaryContainer = EarthSurfaceLinenSoft,
    secondary = EarthCamelOchre,
    onSecondary = Color.White,
    secondaryContainer = EarthCamelLight,
    background = EarthCanvasCream,
    surface = EarthSurfaceLinenSoft,
    surfaceVariant = EarthSandCard,
    onBackground = EarthTextDark,
    onSurface = EarthTextDark,
    outline = EarthBorderLinen
)

@Composable
fun ZenithFocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val earthColors = if (darkTheme) DarkEarthColors else LightEarthColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalEarthColors provides earthColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
