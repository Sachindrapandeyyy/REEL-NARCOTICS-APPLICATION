package com.zenith.focus.core.designsystem

import androidx.compose.ui.graphics.Color

// ============================================================================
// REEL NARCOTICS - ETHEREAL AURORA & WARM IVORY MINDFUL PALETTE
// Single unified aesthetic matching user's reference mockup:
// - Warm ivory / peach blush canvas (#FAF7F2, #FBF1E8)
// - Ethereal diffused violet aurora sphere (#C084FC, #A855F7)
// - Frosted translucent white squircle cards
// - Multi-color orbital status chips: Amber, Sky, Mint, Coral, Violet
// - Editorial high-contrast charcoal typography (#1E1A22)
// ============================================================================

val EtherealCanvas = Color(0xFFFAF7F2)
val EtherealCanvasBlush = Color(0xFFFBF1E8)
val EtherealCanvasWarm = Color(0xFFF5EFEB)
val EtherealCanvasElevated = Color(0xFFFCF9F5)

val EtherealSurface = Color(0xFFFFFFFF)
val EtherealSurfaceSoft = Color(0xFFFAF6F2)
val EtherealSurfaceVariant = Color(0xFFF4EDE4)
val EtherealBorder = Color(0xFFEFE8DE)
val EtherealBorderSubtle = Color(0xFFF7F2EB)

val EtherealTextPrimary = Color(0xFF1E1A22)
val EtherealTextMuted = Color(0xFF8E889B)
val EtherealTextSubtle = Color(0xFFAFA9B8)

// Orbital Status & Feature Highlights
val OrbitalAmber = Color(0xFFF59E0B)
val OrbitalAmberSoft = Color(0xFFFEF3C7)
val OrbitalSky = Color(0xFF0EA5E9)
val OrbitalSkySoft = Color(0xFFE0F2FE)
val OrbitalMint = Color(0xFF10B981)
val OrbitalMintSoft = Color(0xFFD1FAE5)
val OrbitalCoral = Color(0xFFF43F5E)
val OrbitalCoralSoft = Color(0xFFFFE4E6)
val OrbitalViolet = Color(0xFF8B5CF6)
val OrbitalVioletSoft = Color(0xFFEDE9FE)
val OrbitalAuroraCenter = Color(0xFFC084FC)

// BACKWARD-COMPATIBLE BINDINGS
val PastelCanvas = EtherealCanvas
val PastelCanvasBlush = EtherealCanvasBlush
val PastelCanvasLavender = EtherealCanvasWarm
val PastelCanvasElevated = EtherealCanvasElevated
val PastelSurface = EtherealSurface
val PastelSurfaceSoft = EtherealSurfaceSoft
val PastelSurfaceVariant = EtherealSurfaceVariant
val PastelBorder = EtherealBorder
val PastelBorderSubtle = EtherealBorderSubtle

val PastelStrawberryPink = OrbitalCoral
val PastelStrawberryLight = OrbitalCoralSoft
val PastelStrawberryDeep = Color(0xFFBE123C)

val PastelViolet = OrbitalViolet
val PastelVioletLight = OrbitalVioletSoft
val PastelVioletDeep = Color(0xFF6D28D9)

val PastelPeach = Color(0xFFFF9E7D)
val PastelPeachLight = Color(0xFFFFD4C7)
val PastelPeachDeep = Color(0xFFE27450)

val PastelTextPrimary = EtherealTextPrimary
val PastelTextMuted = EtherealTextMuted
val PastelTextSubtle = EtherealTextSubtle
val PastelTrack = EtherealBorder

val EarthCanvasCream = EtherealCanvas
val EarthCanvasCreamLight = EtherealCanvasElevated
val EarthSurfaceLinen = EtherealSurface
val EarthSurfaceLinenSoft = EtherealSurfaceSoft
val EarthSandCard = EtherealSurfaceVariant
val EarthSandCardLight = EtherealBorderSubtle

val EarthForestGreen = OrbitalCoral
val EarthForestDark = EtherealTextPrimary
val EarthForestDeep = Color(0xFF14101F)
val EarthForestLight = OrbitalCoral

val EarthCamelOchre = OrbitalViolet
val EarthCamelLight = OrbitalVioletSoft
val EarthCamelDeep = Color(0xFF6D28D9)
val EarthAccentRule = OrbitalViolet

val EarthSageOlive = OrbitalMint
val EarthSageLight = OrbitalMintSoft
val EarthSageDeep = Color(0xFF047857)

val EarthTextDark = EtherealTextPrimary
val EarthTextPrimary = EtherealTextPrimary
val EarthTextMuted = EtherealTextMuted
val EarthTextSubtle = EtherealTextSubtle

val EarthBorderLinen = EtherealBorder
val EarthBorderSubtle = EtherealBorderSubtle

val EarthNightBg = EtherealCanvas
val EarthNightBgElevated = EtherealCanvasElevated
val EarthNightSurface = EtherealSurface
val EarthNightSurfaceSoft = EtherealSurfaceSoft
val EarthNightSurfaceVariant = EtherealSurfaceVariant
val EarthNightBorder = EtherealBorder
val EarthNightBorderSubtle = EtherealBorderSubtle

val EarthNightText = EtherealTextPrimary
val EarthNightTextMuted = EtherealTextMuted
val EarthNightTextSubtle = EtherealTextSubtle

val EarthNightGreen = OrbitalCoral
val EarthNightGreenDark = OrbitalCoral
val EarthNightCamel = OrbitalViolet
val EarthNightCamelLight = OrbitalVioletSoft
val EarthNightSage = OrbitalMint

// SEMANTIC EARTH COLORS (SINGLE UNIFIED DESIGN FOR ALL MODES)
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
    val strawberryPink: Color = OrbitalCoral,
    val pastelViolet: Color = OrbitalViolet,
    val softPeach: Color = PastelPeach,
    val gaugeTrack: Color = EtherealBorder,
    val orbitalAmber: Color = OrbitalAmber,
    val orbitalSky: Color = OrbitalSky,
    val orbitalMint: Color = OrbitalMint,
    val orbitalCoral: Color = OrbitalCoral,
    val orbitalViolet: Color = OrbitalViolet,
    val auroraCenter: Color = OrbitalAuroraCenter
)

// Single unified aesthetic
val SingleEtherealColors = EarthColors(
    canvas = EtherealCanvas,
    canvasElevated = EtherealCanvasElevated,
    surface = EtherealSurface,
    surfaceSoft = EtherealSurfaceSoft,
    surfaceVariant = EtherealSurfaceVariant,
    border = EtherealBorder,
    borderSubtle = EtherealBorderSubtle,
    textPrimary = EtherealTextPrimary,
    textMuted = EtherealTextMuted,
    textSubtle = EtherealTextSubtle,
    forestGreen = OrbitalCoral,
    forestDark = EtherealTextPrimary,
    camelOchre = OrbitalViolet,
    camelLight = OrbitalVioletSoft,
    sageOlive = OrbitalMint,
    error = Color(0xFFE54D66),
    isDark = false,
    strawberryPink = OrbitalCoral,
    pastelViolet = OrbitalViolet,
    softPeach = PastelPeach,
    gaugeTrack = EtherealBorder,
    orbitalAmber = OrbitalAmber,
    orbitalSky = OrbitalSky,
    orbitalMint = OrbitalMint,
    orbitalCoral = OrbitalCoral,
    orbitalViolet = OrbitalViolet,
    auroraCenter = OrbitalAuroraCenter
)

val LightEarthColors = SingleEtherealColors
val DarkEarthColors = SingleEtherealColors

val LocalEarthColors = androidx.compose.runtime.staticCompositionLocalOf { SingleEtherealColors }

object EarthTheme {
    val colors: EarthColors
        @androidx.compose.runtime.Composable
        get() = LocalEarthColors.current
}

// SPIDER-MAN BACKWARD COMPATIBLE BINDINGS
val SpiderRed = OrbitalCoral
val SpiderRedAccent = OrbitalViolet
val SpiderRedDark = Color(0xFFBE123C)
val SpiderRedDeep = Color(0xFF14101F)

val SpiderBlue = OrbitalSky
val SpiderBlueAccent = OrbitalViolet
val SpiderBlueDark = Color(0xFF0369A1)
val SpiderBlueDeep = Color(0xFF14101F)

val SpiderStealthBg = EtherealCanvas
val SpiderSurfaceDark = EtherealSurfaceSoft
val SpiderCardDark = EtherealSurface
val SpiderBorderDark = EtherealBorder
val SpiderBorderSubtle = EtherealBorderSubtle

val SpiderWebWhite = EtherealTextPrimary
val SpiderTextSecondary = EtherealTextMuted
val SpiderGold = OrbitalAmber

// ZENITH SYSTEM BINDINGS
val ZenithNavy = EtherealSurfaceSoft
val ZenithNavyLight = EtherealSurface
val ZenithNavyDark = EtherealCanvas

val ZenithEmerald = OrbitalCoral
val ZenithEmeraldAccent = OrbitalViolet
val ZenithSage = OrbitalMint

val ZenithAlabaster = EtherealCanvas
val ZenithSurfaceWhite = EtherealSurfaceSoft
val ZenithBorderLight = EtherealBorder
val ZenithTextMutedLight = EtherealTextMuted

val ZenithBurgundy = OrbitalCoral
val ZenithBurgundyDeep = Color(0xFFBE123C)
val ZenithBurgundyAccent = OrbitalViolet

val ZenithDarkBg = EtherealCanvas
val ZenithDarkSurface = EtherealSurfaceSoft
val ZenithCardDark = EtherealSurface
val ZenithDarkSurfaceVariant = EtherealSurfaceVariant
val ZenithDarkBorder = EtherealBorder

val ZenithLightBg = EtherealCanvas
val ZenithLightSurface = EtherealSurfaceSoft
val ZenithLightSurfaceVariant = EtherealSurfaceVariant
val ZenithLightBorder = EtherealBorder

val ZenithPrimary = OrbitalCoral
val ZenithPrimaryDark = Color(0xFFBE123C)
val ZenithPrimaryContainer = EtherealSurfaceVariant
val ZenithOnPrimary = Color(0xFFFFFFFF)

val ZenithSecondary = OrbitalViolet
val ZenithSecondaryContainer = OrbitalVioletSoft
val ZenithOnSecondary = Color(0xFFFFFFFF)

val ZenithCoral = PastelPeach
val ZenithCoralContainer = OrbitalCoralSoft
val ZenithAmber = OrbitalAmber
val ZenithAmberContainer = OrbitalAmberSoft
val ZenithPurple = OrbitalViolet

val ZenithTextPrimaryDark = EtherealTextPrimary
val ZenithTextSecondaryDark = EtherealTextMuted
val ZenithTextPrimaryLight = EtherealTextPrimary
val ZenithTextSecondaryLight = EtherealTextMuted
