package com.zenith.focus.core.designsystem

import androidx.compose.ui.graphics.Color

// ============================================================================
// REEL NARCOTICS - SPIDER-MAN EXECUTIVE PALETTE
// 1. Spider Crimson Red: Iconic Hero Accent (#DC2626, #EF4444, #991B1B)
// 2. Spider Heroic Blue: Dynamic Secondary & Badges (#2563EB, #1D4ED8, #3B82F6)
// 3. Stealth Suit Black: Dark Slate/Obsidian Backgrounds (#080C14, #0F172A)
// 4. Web Suit Surface: Deep Tech Card Surfaces (#101726, #131D31)
// 5. Web Borders & Silver: Clean Grid Contrasts (#25334D, #1E293B, #F8FAFC)
// ============================================================================

// SPIDER-MAN CORE PALETTE
val SpiderRed = Color(0xFFDC2626)
val SpiderRedAccent = Color(0xFFEF4444)
val SpiderRedDark = Color(0xFF991B1B)
val SpiderRedDeep = Color(0xFF450A0A)

val SpiderBlue = Color(0xFF2563EB)
val SpiderBlueAccent = Color(0xFF3B82F6)
val SpiderBlueDark = Color(0xFF1D4ED8)
val SpiderBlueDeep = Color(0xFF172554)

val SpiderStealthBg = Color(0xFF080C14)
val SpiderSurfaceDark = Color(0xFF101726)
val SpiderCardDark = Color(0xFF131D31)
val SpiderBorderDark = Color(0xFF25334D)
val SpiderBorderSubtle = Color(0xFF1E293B)

val SpiderWebWhite = Color(0xFFF8FAFC)
val SpiderTextSecondary = Color(0xFF94A3B8)
val SpiderGold = Color(0xFFF59E0B)

// BACKWARD COMPATIBLE & SYSTEM BINDINGS
val ZenithNavy = SpiderSurfaceDark
val ZenithNavyLight = Color(0xFF1A2438)
val ZenithNavyDark = SpiderStealthBg

val ZenithEmerald = SpiderBlueDark
val ZenithEmeraldAccent = SpiderBlueAccent
val ZenithSage = SpiderBlue

val ZenithAlabaster = Color(0xFFF8FAFC)
val ZenithSurfaceWhite = Color(0xFFFFFFFF)
val ZenithBorderLight = Color(0xFFE2E8F0)
val ZenithTextMutedLight = Color(0xFF64748B)

val ZenithBurgundy = SpiderRedDark
val ZenithBurgundyDeep = SpiderRedDeep
val ZenithBurgundyAccent = SpiderRedAccent

val ZenithDarkBg = SpiderStealthBg
val ZenithDarkSurface = SpiderSurfaceDark
val ZenithCardDark = SpiderCardDark
val ZenithDarkSurfaceVariant = Color(0xFF1E293B)
val ZenithDarkBorder = SpiderBorderDark

val ZenithLightBg = ZenithAlabaster
val ZenithLightSurface = ZenithSurfaceWhite
val ZenithLightSurfaceVariant = Color(0xFFF1F5F9)
val ZenithLightBorder = ZenithBorderLight

val ZenithPrimary = SpiderRed
val ZenithPrimaryDark = SpiderRedDark
val ZenithPrimaryContainer = SpiderRedDeep
val ZenithOnPrimary = Color(0xFFFFFFFF)

val ZenithSecondary = SpiderBlue
val ZenithSecondaryContainer = SpiderBlueDeep
val ZenithOnSecondary = Color(0xFFFFFFFF)

val ZenithCoral = SpiderRedAccent
val ZenithCoralContainer = SpiderRedDeep
val ZenithAmber = SpiderGold
val ZenithAmberContainer = Color(0xFF78350F)
val ZenithPurple = Color(0xFF8B5CF6)

val ZenithTextPrimaryDark = Color(0xFFF8FAFC)
val ZenithTextSecondaryDark = Color(0xFF94A3B8)
val ZenithTextPrimaryLight = Color(0xFF0F172A)
val ZenithTextSecondaryLight = ZenithTextMutedLight
