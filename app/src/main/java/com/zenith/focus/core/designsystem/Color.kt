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

// ORGANIC EARTH PALETTE TOKENS
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
