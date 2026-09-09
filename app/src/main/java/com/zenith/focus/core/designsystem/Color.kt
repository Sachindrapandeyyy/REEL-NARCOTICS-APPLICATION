package com.zenith.focus.core.designsystem

import androidx.compose.ui.graphics.Color

// ============================================================================
// REEL NARCOTICS - ORGANIC EARTH & LINEN MINIMALIST PALETTE
// Extracted directly from reference image:
// 1. EarthCanvasCream: Warm Oatmeal / Linen Background (#F3EBE5)
// 2. EarthSurfaceLinen: Soft Off-White / Sand Surface (#FAF7F2, #EDE8E2)
// 3. EarthForestGreen: Deep Botanical Pine Green (#204844, #173330)
// 4. EarthCamelOchre: Warm Golden Camel Accent & FAB (#BC9259, #C9A97E)
// 5. EarthSageOlive: Muted Botanical Sage (#868D7D, #A3AB9B)
// 6. EarthSandCard: Soft Sand Oatmeal (#DED5C7, #EAE3D9)
// 7. EarthTextDark: High-Contrast Deep Charcoal/Pine (#1E2B28)
// 8. EarthTextMuted: Calming Sage Charcoal (#66706B)
// 9. EarthAccentRule: Warm Ochre Divider (#BC9B6D)
// ============================================================================

// ORGANIC EARTH PALETTE TOKENS (LIGHT)
val EarthCanvasCream = Color(0xFFF3EBE5)
val EarthCanvasCreamLight = Color(0xFFFAF7F2)
val EarthSurfaceLinen = Color(0xFFEDE8E2)
val EarthSurfaceLinenSoft = Color(0xFFFAF8F5)
val EarthSandCard = Color(0xFFDED5C7)
val EarthSandCardLight = Color(0xFFEBE4D8)

val EarthForestGreen = Color(0xFF204844)
val EarthForestDark = Color(0xFF173330)
val EarthForestDeep = Color(0xFF102422)
val EarthForestLight = Color(0xFF2E635D)

val EarthCamelOchre = Color(0xFFBC9259)
val EarthCamelLight = Color(0xFFDFCAAB)
val EarthCamelDeep = Color(0xFF916A37)
val EarthAccentRule = Color(0xFFBC9B6D)

val EarthSageOlive = Color(0xFF868D7D)
val EarthSageLight = Color(0xFFBCC2B4)
val EarthSageDeep = Color(0xFF5E6556)

val EarthTextDark = Color(0xFF1E2B28)
val EarthTextPrimary = Color(0xFF1E2B28)
val EarthTextMuted = Color(0xFF66706B)
val EarthTextSubtle = Color(0xFF8C9590)

val EarthBorderLinen = Color(0xFFDDD4C6)
val EarthBorderSubtle = Color(0xFFE8E1D5)

// SCANDINAVIAN MIDNIGHT PALETTE TOKENS (NIGHT / DARK)
val EarthNightBg = Color(0xFF101715)
val EarthNightBgElevated = Color(0xFF15201D)
val EarthNightSurface = Color(0xFF182421)
val EarthNightSurfaceSoft = Color(0xFF1F2E2A)
val EarthNightSurfaceVariant = Color(0xFF253732)
val EarthNightBorder = Color(0xFF2C3E38)
val EarthNightBorderSubtle = Color(0xFF384D46)

val EarthNightText = Color(0xFFF2ECE6)
val EarthNightTextMuted = Color(0xFF9BA8A1)
val EarthNightTextSubtle = Color(0xFF7A8780)

val EarthNightGreen = Color(0xFF357A6F)
val EarthNightGreenDark = Color(0xFF23554D)
val EarthNightCamel = Color(0xFFD6A76E)
val EarthNightCamelLight = Color(0xFFE5BF8F)
val EarthNightSage = Color(0xFF9AA593)

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
    val isDark: Boolean
)

val LightEarthColors = EarthColors(
    canvas = EarthCanvasCream,
    canvasElevated = EarthCanvasCreamLight,
    surface = EarthSurfaceLinen,
    surfaceSoft = EarthSurfaceLinenSoft,
    surfaceVariant = EarthSandCard,
    border = EarthBorderLinen,
    borderSubtle = EarthBorderSubtle,
    textPrimary = EarthTextDark,
    textMuted = EarthTextMuted,
    textSubtle = EarthTextSubtle,
    forestGreen = EarthForestGreen,
    forestDark = EarthForestDark,
    camelOchre = EarthCamelOchre,
    camelLight = EarthCamelLight,
    sageOlive = EarthSageOlive,
    error = Color(0xFFB83A3A),
    isDark = false
)

val DarkEarthColors = EarthColors(
    canvas = EarthNightBg,
    canvasElevated = EarthNightBgElevated,
    surface = EarthNightSurface,
    surfaceSoft = EarthNightSurfaceSoft,
    surfaceVariant = EarthNightSurfaceVariant,
    border = EarthNightBorder,
    borderSubtle = EarthNightBorderSubtle,
    textPrimary = EarthNightText,
    textMuted = EarthNightTextMuted,
    textSubtle = EarthNightTextSubtle,
    forestGreen = EarthNightGreen,
    forestDark = EarthNightGreenDark,
    camelOchre = EarthNightCamel,
    camelLight = EarthNightCamelLight,
    sageOlive = EarthNightSage,
    error = Color(0xFFE57373),
    isDark = true
)

val LocalEarthColors = androidx.compose.runtime.staticCompositionLocalOf { LightEarthColors }

object EarthTheme {
    val colors: EarthColors
        @androidx.compose.runtime.Composable
        get() = LocalEarthColors.current
}

// SPIDER-MAN BACKWARD COMPATIBLE BINDINGS (Redirected to Organic Earth)
val SpiderRed = EarthForestGreen
val SpiderRedAccent = EarthCamelOchre
val SpiderRedDark = EarthForestDark
val SpiderRedDeep = EarthForestDeep

val SpiderBlue = EarthForestGreen
val SpiderBlueAccent = EarthCamelOchre
val SpiderBlueDark = EarthForestDark
val SpiderBlueDeep = EarthForestDeep

val SpiderStealthBg = EarthCanvasCream
val SpiderSurfaceDark = EarthSurfaceLinenSoft
val SpiderCardDark = EarthSurfaceLinen
val SpiderBorderDark = EarthBorderLinen
val SpiderBorderSubtle = EarthBorderSubtle

val SpiderWebWhite = EarthTextDark
val SpiderTextSecondary = EarthTextMuted
val SpiderGold = EarthCamelOchre

// ZENITH SYSTEM BINDINGS
val ZenithNavy = EarthSurfaceLinenSoft
val ZenithNavyLight = EarthSurfaceLinen
val ZenithNavyDark = EarthCanvasCream

val ZenithEmerald = EarthForestGreen
val ZenithEmeraldAccent = EarthCamelOchre
val ZenithSage = EarthSageOlive

val ZenithAlabaster = EarthCanvasCream
val ZenithSurfaceWhite = EarthSurfaceLinenSoft
val ZenithBorderLight = EarthBorderLinen
val ZenithTextMutedLight = EarthTextMuted

val ZenithBurgundy = EarthForestGreen
val ZenithBurgundyDeep = EarthForestDark
val ZenithBurgundyAccent = EarthCamelOchre

val ZenithDarkBg = EarthCanvasCream
val ZenithDarkSurface = EarthSurfaceLinenSoft
val ZenithCardDark = EarthSurfaceLinen
val ZenithDarkSurfaceVariant = EarthSandCard
val ZenithDarkBorder = EarthBorderLinen

val ZenithLightBg = EarthCanvasCream
val ZenithLightSurface = EarthSurfaceLinenSoft
val ZenithLightSurfaceVariant = EarthSandCard
val ZenithLightBorder = EarthBorderLinen

val ZenithPrimary = EarthForestGreen
val ZenithPrimaryDark = EarthForestDark
val ZenithPrimaryContainer = EarthSurfaceLinen
val ZenithOnPrimary = Color(0xFFFFFFFF)

val ZenithSecondary = EarthCamelOchre
val ZenithSecondaryContainer = EarthCamelLight
val ZenithOnSecondary = Color(0xFFFFFFFF)

val ZenithCoral = EarthCamelOchre
val ZenithCoralContainer = EarthCamelLight
val ZenithAmber = EarthCamelOchre
val ZenithAmberContainer = EarthCamelLight
val ZenithPurple = EarthSageOlive

val ZenithTextPrimaryDark = EarthTextDark
val ZenithTextSecondaryDark = EarthTextMuted
val ZenithTextPrimaryLight = EarthTextDark
val ZenithTextSecondaryLight = EarthTextMuted
