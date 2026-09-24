package com.zenith.focus.core.designsystem

import androidx.compose.ui.graphics.Color

// ============================================================================
// REEL NARCOTICS - MINDFUL PASTEL NEUMORPHIC & FROSTED GLASS PALETTE
// Extracted directly from user reference design:
// 1. PastelCanvas: Soft blush cream & lavender mist (#FBF8FD, #FFF3F2)
// 2. PastelSurface: Translucent Frosted White (#FFFFFF)
// 3. PastelSurfaceSoft: Gentle Lilac Mist (#FAF7FD)
// 4. PastelSurfaceVariant: Soft Violet Tint (#F3EDF9)
// 5. PastelStrawberryPink: Vibrant Strawberry Rose Accent (#FF6584)
// 6. PastelViolet: Mindful Pastel Violet (#8B5CF6)
// 7. PastelPeach: Sunrise Coral (#FF9E7D)
// 8. PastelTextPrimary: Deep Nocturnal Slate (#1E192B)
// 9. PastelTextMuted: Mindful Lavender Slate (#7E7792)
// 10. PastelTrack: Soft Ring Track Lavender (#EDE7F5)
// ============================================================================

// MINDFUL PASTEL TOKENS (LIGHT)
val PastelCanvas = Color(0xFFFBF8FD)
val PastelCanvasBlush = Color(0xFFFFF2F4)
val PastelCanvasLavender = Color(0xFFF4EDFC)
val PastelCanvasElevated = Color(0xFFFFF6F7)
val PastelSurface = Color(0xFFFFFFFF)
val PastelSurfaceSoft = Color(0xFFFAF7FD)
val PastelSurfaceVariant = Color(0xFFF3EDF9)
val PastelBorder = Color(0xFFEDE4F4)
val PastelBorderSubtle = Color(0xFFF6EFFB)

val PastelStrawberryPink = Color(0xFFFF6584)
val PastelStrawberryLight = Color(0xFFFF94A8)
val PastelStrawberryDeep = Color(0xFFE04566)

val PastelViolet = Color(0xFF8B5CF6)
val PastelVioletLight = Color(0xFFDDD6FE)
val PastelVioletDeep = Color(0xFF6D28D9)

val PastelPeach = Color(0xFFFF9E7D)
val PastelPeachLight = Color(0xFFFFD4C7)
val PastelPeachDeep = Color(0xFFE27450)

val PastelTextPrimary = Color(0xFF1E192B)
val PastelTextMuted = Color(0xFF7E7792)
val PastelTextSubtle = Color(0xFFA59EBA)
val PastelTrack = Color(0xFFEDE7F5)

// NOCTURNAL AMETHYST TOKENS (DARK)
val NightCanvas = Color(0xFF13111C)
val NightCanvasElevated = Color(0xFF1A1626)
val NightSurface = Color(0xFF1E1A2C)
val NightSurfaceSoft = Color(0xFF262137)
val NightSurfaceVariant = Color(0xFF2E2843)
val NightBorder = Color(0xFF383152)
val NightBorderSubtle = Color(0xFF453D63)

val NightTextPrimary = Color(0xFFFAF7FF)
val NightTextMuted = Color(0xFFAAA3BF)
val NightTextSubtle = Color(0xFF7D7791)

val NightStrawberryPink = Color(0xFFFF6584)
val NightStrawberryLight = Color(0xFFFF8DA3)
val NightViolet = Color(0xFFA78BFA)
val NightVioletLight = Color(0xFFC4B5FD)
val NightPeach = Color(0xFFFFB099)
val NightTrack = Color(0xFF2D2742)

// BACKWARD-COMPATIBLE EARTH MAPPINGS (MAPPING DIRECTLY TO PASTEL DESIGN)
val EarthCanvasCream = PastelCanvas
val EarthCanvasCreamLight = PastelCanvasElevated
val EarthSurfaceLinen = PastelSurface
val EarthSurfaceLinenSoft = PastelSurfaceSoft
val EarthSandCard = PastelSurfaceVariant
val EarthSandCardLight = PastelBorderSubtle

val EarthForestGreen = PastelStrawberryPink
val EarthForestDark = PastelTextPrimary
val EarthForestDeep = Color(0xFF14101F)
val EarthForestLight = PastelStrawberryLight

val EarthCamelOchre = PastelViolet
val EarthCamelLight = PastelVioletLight
val EarthCamelDeep = PastelVioletDeep
val EarthAccentRule = PastelViolet

val EarthSageOlive = PastelPeach
val EarthSageLight = PastelPeachLight
val EarthSageDeep = PastelPeachDeep

val EarthTextDark = PastelTextPrimary
val EarthTextPrimary = PastelTextPrimary
val EarthTextMuted = PastelTextMuted
val EarthTextSubtle = PastelTextSubtle

val EarthBorderLinen = PastelBorder
val EarthBorderSubtle = PastelBorderSubtle

// NIGHT MAPPINGS
val EarthNightBg = NightCanvas
val EarthNightBgElevated = NightCanvasElevated
val EarthNightSurface = NightSurface
val EarthNightSurfaceSoft = NightSurfaceSoft
val EarthNightSurfaceVariant = NightSurfaceVariant
val EarthNightBorder = NightBorder
val EarthNightBorderSubtle = NightBorderSubtle

val EarthNightText = NightTextPrimary
val EarthNightTextMuted = NightTextMuted
val EarthNightTextSubtle = NightTextSubtle

val EarthNightGreen = NightStrawberryPink
val EarthNightGreenDark = NightStrawberryPink
val EarthNightCamel = NightViolet
val EarthNightCamelLight = NightVioletLight
val EarthNightSage = NightPeach

// SEMANTIC EARTH COLORS FOR DAY & NIGHT
data class EarthColors(
    val canvas: Color,
    val canvasElevated: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val surfaceVariant: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val forestGreen: Color,
    val forestDark: Color,
    val camelOchre: Color,
    val camelLight: Color,
    val sageOlive: Color,
    val error: Color,
    val isDark: Boolean,
    val strawberryPink: Color = PastelStrawberryPink,
    val pastelViolet: Color = PastelViolet,
    val softPeach: Color = PastelPeach,
    val gaugeTrack: Color = PastelTrack
)

val LightEarthColors = EarthColors(
    canvas = PastelCanvas,
    canvasElevated = PastelCanvasElevated,
    surface = PastelSurface,
    surfaceSoft = PastelSurfaceSoft,
    surfaceVariant = PastelSurfaceVariant,
    border = PastelBorder,
    borderSubtle = PastelBorderSubtle,
    textPrimary = PastelTextPrimary,
    textMuted = PastelTextMuted,
    textSubtle = PastelTextSubtle,
    forestGreen = PastelStrawberryPink,
    forestDark = PastelTextPrimary,
    camelOchre = PastelViolet,
    camelLight = PastelVioletLight,
    sageOlive = PastelPeach,
    error = Color(0xFFE54D66),
    isDark = false,
    strawberryPink = PastelStrawberryPink,
    pastelViolet = PastelViolet,
    softPeach = PastelPeach,
    gaugeTrack = PastelTrack
)

val DarkEarthColors = EarthColors(
    canvas = NightCanvas,
    canvasElevated = NightCanvasElevated,
    surface = NightSurface,
    surfaceSoft = NightSurfaceSoft,
    surfaceVariant = NightSurfaceVariant,
    border = NightBorder,
    borderSubtle = NightBorderSubtle,
    textPrimary = NightTextPrimary,
    textMuted = NightTextMuted,
    textSubtle = NightTextSubtle,
    forestGreen = NightStrawberryPink,
    forestDark = NightTextPrimary,
    camelOchre = NightViolet,
    camelLight = NightVioletLight,
    sageOlive = NightPeach,
    error = Color(0xFFFF6B81),
    isDark = true,
    strawberryPink = NightStrawberryPink,
    pastelViolet = NightViolet,
    softPeach = NightPeach,
    gaugeTrack = NightTrack
)

val LocalEarthColors = androidx.compose.runtime.staticCompositionLocalOf { LightEarthColors }

object EarthTheme {
    val colors: EarthColors
        @androidx.compose.runtime.Composable
        get() = LocalEarthColors.current
}

// SPIDER-MAN BACKWARD COMPATIBLE BINDINGS (Redirected to Mindful Pastel)
val SpiderRed = PastelStrawberryPink
val SpiderRedAccent = PastelViolet
val SpiderRedDark = PastelStrawberryDeep
val SpiderRedDeep = Color(0xFF14101F)

val SpiderBlue = PastelViolet
val SpiderBlueAccent = PastelPeach
val SpiderBlueDark = PastelVioletDeep
val SpiderBlueDeep = Color(0xFF14101F)

val SpiderStealthBg = PastelCanvas
val SpiderSurfaceDark = PastelSurfaceSoft
val SpiderCardDark = PastelSurface
val SpiderBorderDark = PastelBorder
val SpiderBorderSubtle = PastelBorderSubtle

val SpiderWebWhite = PastelTextPrimary
val SpiderTextSecondary = PastelTextMuted
val SpiderGold = PastelViolet

// ZENITH SYSTEM BINDINGS
val ZenithNavy = PastelSurfaceSoft
val ZenithNavyLight = PastelSurface
val ZenithNavyDark = PastelCanvas

val ZenithEmerald = PastelStrawberryPink
val ZenithEmeraldAccent = PastelViolet
val ZenithSage = PastelPeach

val ZenithAlabaster = PastelCanvas
val ZenithSurfaceWhite = PastelSurfaceSoft
val ZenithBorderLight = PastelBorder
val ZenithTextMutedLight = PastelTextMuted

val ZenithBurgundy = PastelStrawberryPink
val ZenithBurgundyDeep = PastelStrawberryDeep
val ZenithBurgundyAccent = PastelViolet

val ZenithDarkBg = PastelCanvas
val ZenithDarkSurface = PastelSurfaceSoft
val ZenithCardDark = PastelSurface
val ZenithDarkSurfaceVariant = PastelSurfaceVariant
val ZenithDarkBorder = PastelBorder

val ZenithLightBg = PastelCanvas
val ZenithLightSurface = PastelSurfaceSoft
val ZenithLightSurfaceVariant = PastelSurfaceVariant
val ZenithLightBorder = PastelBorder

val ZenithPrimary = PastelStrawberryPink
val ZenithPrimaryDark = PastelStrawberryDeep
val ZenithPrimaryContainer = PastelSurfaceVariant
val ZenithOnPrimary = Color(0xFFFFFFFF)

val ZenithSecondary = PastelViolet
val ZenithSecondaryContainer = PastelVioletLight
val ZenithOnSecondary = Color(0xFFFFFFFF)

val ZenithCoral = PastelPeach
val ZenithCoralContainer = PastelPeachLight
val ZenithAmber = PastelPeach
val ZenithAmberContainer = PastelPeachLight
val ZenithPurple = PastelViolet

val ZenithTextPrimaryDark = PastelTextPrimary
val ZenithTextSecondaryDark = PastelTextMuted
val ZenithTextPrimaryLight = PastelTextPrimary
val ZenithTextSecondaryLight = PastelTextMuted
