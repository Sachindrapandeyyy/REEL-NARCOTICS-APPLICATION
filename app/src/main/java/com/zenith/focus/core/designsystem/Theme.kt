package com.zenith.focus.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ZenithPrimary,
    onPrimary = ZenithOnPrimary,
    primaryContainer = ZenithPrimaryContainer,
    secondary = ZenithSecondary,
    onSecondary = ZenithOnSecondary,
    secondaryContainer = ZenithSecondaryContainer,
    background = ZenithDarkBg,
    surface = ZenithDarkSurface,
    surfaceVariant = ZenithDarkSurfaceVariant,
    onBackground = ZenithTextPrimaryDark,
    onSurface = ZenithTextPrimaryDark,
    outline = ZenithDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ZenithPrimaryDark,
    onPrimary = ZenithOnPrimary,
    primaryContainer = ZenithPrimary,
    secondary = ZenithSecondary,
    onSecondary = ZenithOnSecondary,
    secondaryContainer = ZenithSecondaryContainer,
    background = ZenithLightBg,
    surface = ZenithLightSurface,
    surfaceVariant = ZenithLightSurfaceVariant,
    onBackground = ZenithTextPrimaryLight,
    onSurface = ZenithTextPrimaryLight,
    outline = ZenithLightBorder
)

@Composable
fun ZenithFocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
