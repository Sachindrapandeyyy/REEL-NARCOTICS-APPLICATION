package com.zenith.focus.feature.protection

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MovieFilter
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.domain.model.AppLockConfig
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProtectionScreen(
    nuclearSession: NuclearSession,
    config: ProtectionConfig,
    appLockConfig: AppLockConfig = AppLockConfig(),
    onNavigateAppLock: () -> Unit = {},
    onToggleCategory: (ContentCategory, Boolean) -> Unit,
    onToggleBrowserProtection: (Boolean) -> Unit,
    onToggleStrictMode: (Boolean) -> Unit
) {
    val earth = EarthTheme.colors
    val scrollState = rememberScrollState()
    val isNuclearActive = nuclearSession.isCurrentlyActive()

    val formattedNuclearEnd = remember(nuclearSession.endTimeMillis) {
        SimpleDateFormat("h:mm a (MMM d)", Locale.getDefault()).format(Date(nuclearSession.endTimeMillis))
    }

    // Warm Ivory Linen & Peach Mist Gradient Canvas
    val canvasBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFAF7F2),
                Color(0xFFFBF1E8),
                Color(0xFFF6EFEB)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            // ====================================================================
            // 1. EDITORIAL HEADER: Shields & Interventions
            // ====================================================================
            Text(
                text = "SHIELDS & INTERVENTIONS",
                color = OrbitalCoral,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Feed & Web Shields",
                color = earth.textPrimary,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Instant hardware-level ejection when addictive feeds or explicit loops are opened.",
                color = earth.textMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ====================================================================
            // 2. NUCLEAR MODE STATUS (IF ACTIVE)
            // ====================================================================
            if (isNuclearActive) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, OrbitalCoral.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OrbitalCoralSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "☢️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NUCLEAR LOCK ACTIVE",
                                color = OrbitalCoral,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "All feeds locked ON until $formattedNuclearEnd. Zero bypass allowed.",
                                color = earth.textPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // ====================================================================
            // 3. APP LOCK SHIELD BANNER (FROSTED SQUIRCLE)
            // ====================================================================
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
                    .clickable { onNavigateAppLock() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(OrbitalMintSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Apps,
                                contentDescription = "App Lock",
                                tint = OrbitalMint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "App Lock Shield",
                                    color = earth.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (appLockConfig.isAppLockEnabled) OrbitalMintSoft else earth.surfaceVariant)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (appLockConfig.isAppLockEnabled) "ACTIVE" else "OFF",
                                        color = if (appLockConfig.isAppLockEnabled) OrbitalMint else earth.textMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = if (appLockConfig.totalCount == 0) "Lock distracting apps & games" else "${appLockConfig.totalCount} apps locked",
                                color = earth.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Open App Lock",
                        tint = earth.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 4. GROUP 1: SHORT-FORM ADDICTIVE FEEDS (UNIFIED FROSTED CONTAINER)
            // ====================================================================
            GroupHeader(title = "SHORT-FORM FEEDS", subtitle = "Sub-16ms ejection")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    // Item 1: YouTube Shorts
                    ShieldToggleRow(
                        icon = Icons.Outlined.PlayCircleOutline,
                        iconTint = OrbitalCoral,
                        title = "YouTube Shorts",
                        subtitle = "Ejects upon opening Shorts shelf or reel tab",
                        isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.YOUTUBE_SHORTS) else config.blockYouTubeShorts,
                        enabled = !isNuclearActive,
                        onCheckedChange = { onToggleCategory(ContentCategory.YOUTUBE_SHORTS, it) }
                    )

                    EtherealDivider()

                    // Item 2: Instagram Reels
                    ShieldToggleRow(
                        icon = Icons.Outlined.MovieFilter,
                        iconTint = OrbitalViolet,
                        title = "Instagram Reels",
                        subtitle = "Terminates reel player and infinite clip scroll",
                        isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.INSTAGRAM_REELS) else config.blockInstagramReels,
                        enabled = !isNuclearActive,
                        onCheckedChange = { onToggleCategory(ContentCategory.INSTAGRAM_REELS, it) }
                    )

                    EtherealDivider()

                    // Item 3: Facebook Reels
                    ShieldToggleRow(
                        icon = Icons.Outlined.VideoLibrary,
                        iconTint = OrbitalSky,
                        title = "Facebook Reels",
                        subtitle = "Blocks video watch feed and short clip player",
                        isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.FACEBOOK_REELS) else config.blockFacebookReels,
                        enabled = !isNuclearActive,
                        onCheckedChange = { onToggleCategory(ContentCategory.FACEBOOK_REELS, it) }
                    )

                    EtherealDivider()

                    // Item 4: Snapchat Spotlight
                    ShieldToggleRow(
                        icon = Icons.Outlined.Videocam,
                        iconTint = OrbitalAmber,
                        title = "Snapchat Spotlight",
                        subtitle = "Prevents vertical swipe spotlight trap",
                        isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.SNAPCHAT_SPOTLIGHT) else config.blockSnapchatSpotlight,
                        enabled = !isNuclearActive,
                        onCheckedChange = { onToggleCategory(ContentCategory.SNAPCHAT_SPOTLIGHT, it) }
                    )

                    EtherealDivider()

                    // Item 5: TikTok
                    ShieldToggleRow(
                        icon = Icons.Outlined.MusicNote,
                        iconTint = earth.textPrimary,
                        title = "TikTok",
                        subtitle = "Blocks TikTok and TikTok Lite stream sessions",
                        isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.TIKTOK) else config.blockTikTok,
                        enabled = !isNuclearActive,
                        onCheckedChange = { onToggleCategory(ContentCategory.TIKTOK, it) }
                    )

                    EtherealDivider()

                    // Item 6: Other Short Videos
                    ShieldToggleRow(
                        icon = Icons.Outlined.WarningAmber,
                        iconTint = OrbitalCoral,
                        title = "Other Short Videos",
                        subtitle = "Detects generic vertical swipe video containers",
                        isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.OTHER_SHORT_VIDEO) else config.blockOtherShortVideo,
                        enabled = !isNuclearActive,
                        onCheckedChange = { onToggleCategory(ContentCategory.OTHER_SHORT_VIDEO, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 5. GROUP 2: EXPLICIT CONTENT BARRIER (UNIFIED FROSTED CONTAINER)
            // ====================================================================
            GroupHeader(title = "EXPLICIT CONTENT BARRIER", subtitle = "Zero-tolerance boundary protection")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    // Item 1: Adult Websites
                    ShieldToggleRow(
                        icon = Icons.Outlined.Shield,
                        iconTint = OrbitalCoral,
                        title = "Adult Websites & Domains",
                        subtitle = "Real-time browser URL check against comprehensive adult blacklist",
                        isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.ADULT_WEBSITE) else config.blockAdultWebsites,
                        enabled = !isNuclearActive,
                        onCheckedChange = { onToggleCategory(ContentCategory.ADULT_WEBSITE, it) }
                    )

                    EtherealDivider()

                    // Item 2: Keyword Shield
                    ShieldToggleRow(
                        icon = Icons.Outlined.SearchOff,
                        iconTint = OrbitalViolet,
                        title = "Adult Keyword Shield",
                        subtitle = "Scans search queries and active web pages for explicit terminology",
                        isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.ADULT_KEYWORD) else config.blockAdultKeywords,
                        enabled = !isNuclearActive,
                        onCheckedChange = { onToggleCategory(ContentCategory.ADULT_KEYWORD, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 6. GROUP 3: HARDWARE ENFORCEMENT DEPTH (UNIFIED FROSTED CONTAINER)
            // ====================================================================
            GroupHeader(title = "HARDWARE ENFORCEMENT DEPTH", subtitle = "Low-level anti-tamper heuristics")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    // Item 1: Strict Enforcement
                    ShieldToggleRow(
                        icon = Icons.Outlined.Lock,
                        iconTint = OrbitalCoral,
                        title = "Strict Enforcement Mode",
                        subtitle = "Aggressive foreground ejection to eliminate fast swipe bypasses",
                        isChecked = if (isNuclearActive) true else config.strictMode,
                        enabled = !isNuclearActive,
                        onCheckedChange = onToggleStrictMode
                    )

                    EtherealDivider()

                    // Item 2: Deep URL Inspection
                    ShieldToggleRow(
                        icon = Icons.Outlined.Language,
                        iconTint = OrbitalSky,
                        title = "Browser Deep URL Inspection",
                        subtitle = "Inspects browser address bars to catch instant redirect hops",
                        isChecked = if (isNuclearActive) true else config.browserProtectionEnabled,
                        enabled = !isNuclearActive,
                        onCheckedChange = onToggleBrowserProtection
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun GroupHeader(title: String, subtitle: String) {
    val earth = EarthTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            color = earth.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = subtitle,
            color = earth.textMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ShieldToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val earth = EarthTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column {
                Text(
                    text = title,
                    color = earth.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = earth.textMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }

        EtherealToggle(
            checked = isChecked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            activeColor = OrbitalCoral
        )
    }
}

@Composable
fun EtherealDivider() {
    Divider(
        color = Color(0xFFF0EBE1),
        thickness = 1.dp
    )
}

@Composable
fun EtherealToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    activeColor: Color = OrbitalCoral
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "thumb"
    )
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled && checked -> activeColor.copy(alpha = 0.45f)
            !enabled -> Color(0xFFEBE5DC)
            checked -> activeColor
            else -> Color(0xFFDDD6CC)
        },
        label = "track"
    )

    Box(
        modifier = Modifier
            .width(46.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
