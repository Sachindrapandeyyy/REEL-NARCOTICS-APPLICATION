package com.zenith.focus.feature.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.core.permission.OemNavigationManager
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(
    nuclearSession: NuclearSession,
    lockState: LockState,
    config: ProtectionConfig,
    isServiceConnected: Boolean = true,
    isDeviceAdminActive: Boolean = true,
    todayTotalBlocks: Int,
    todayShortsBlocks: Int,
    todayReelsBlocks: Int,
    todayAdultBlocks: Int,
    focusStreakDays: Int,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onArmNuclearClicked: () -> Unit,
    onStartLockClicked: () -> Unit,
    onUnlockClicked: () -> Unit,
    onNavigateProtection: () -> Unit,
    onNavigateStats: () -> Unit,
    onEnableAccessibility: () -> Unit = {},
    onEnableDeviceAdmin: () -> Unit = {},
    onOpenGuide: () -> Unit = {},
    onNavigateAppLock: () -> Unit = {},
    lockedAppsCount: Int = 0
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = System.currentTimeMillis()
        }
    }

    val isNuclearActive = nuclearSession.isCurrentlyActive(now)
    val isRegularLocked = lockState.isCurrentlyActive(now)
    val remainingNuclearMillis = nuclearSession.remainingMillis(now)
    val remainingLockMillis = lockState.remainingMillis(now)

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(hour) {
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Peaceful night"
        }
    }

    val insightSubtitle = remember(todayTotalBlocks, isNuclearActive, isRegularLocked) {
        when {
            isNuclearActive -> "Zero-bypass nuclear shield active..."
            isRegularLocked -> "Focus restrictions active. Staying present..."
            todayTotalBlocks > 0 -> "Analysing your insights • $todayTotalBlocks kicks today..."
            else -> "Analysing your insights..."
        }
    }

    // Infinite breathing pulse for the Ethereal Aurora Focus Core
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraBreathing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val scrollState = rememberScrollState()

    // Warm Ivory Linen & Peach Mist Canvas Gradient (Exact match to reference mockup)
    val etherealCanvasBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFAF7F2), // Warm ivory linen
                Color(0xFFFBF1E8), // Soft peach blush
                Color(0xFFF6EFEB)  // Ethereal warm canvas
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(etherealCanvasBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ====================================================================
            // 1. TOP NAVIGATION BAR: [Calendar] History  Overview [Infinity]
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Frosted squircle button with Calendar/Stats icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, earth.border, RoundedCornerShape(16.dp))
                        .clickable { onNavigateStats() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "History",
                        tint = earth.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center: History & Overview Tabs
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "History",
                        color = earth.textMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateStats() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Overview",
                        color = earth.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Right: Mindful Circular Zen/Infinity Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, earth.border, CircleShape)
                        .clickable { onOpenGuide() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AllInclusive,
                        contentDescription = "Zenith Guide",
                        tint = earth.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ====================================================================
            // 2. HERO: ETHEREAL AURORA FOCUS CORE (Soft Diffused Violet Gradient Orb)
            // ====================================================================
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clickable {
                        if (isRegularLocked) onUnlockClicked() else onStartLockClicked()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Diffused Radial Aurora Glow Canvas
                Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(pulseScale)
                ) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Multi-stop ethereal violet & blush diffusion
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFC084FC).copy(alpha = pulseAlpha * 0.90f), // Soft lavender core
                                Color(0xFFDDD6FE).copy(alpha = pulseAlpha * 0.55f),
                                Color(0xFFFDE8E8).copy(alpha = pulseAlpha * 0.25f), // Soft peach edge
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }

                // Real-time Countdown or Serene Core Glyph
                if (isNuclearActive) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = DateTimeUtils.formatRemaining(remainingNuclearMillis),
                            color = earth.textPrimary,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NUCLEAR LOCKED",
                            color = earth.textPrimary.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                } else if (isRegularLocked) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = DateTimeUtils.formatRemaining(remainingLockMillis),
                            color = earth.textPrimary,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FOCUS ACTIVE",
                            color = earth.textPrimary.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ====================================================================
            // 3. EDITORIAL GREETING: "Good evening, Olivia!" Style
            // ====================================================================
            Text(
                text = "$greeting, Olivia!",
                color = earth.textPrimary,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = insightSubtitle,
                color = earth.textMuted,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ====================================================================
            // 4. HORIZONTAL ORBITAL STATUS PILL STRIP (Exact Match to Reference Mockup)
            // ====================================================================
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(28.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip 1: Amber Dot (YouTube Shorts)
                    OrbitalSquircleChip(
                        glowColor = OrbitalAmber,
                        type = OrbitalChipType.AMBER_DOT,
                        onClick = onNavigateProtection
                    )

                    // Chip 2: Sky Blue Dot (Instagram Reels)
                    OrbitalSquircleChip(
                        glowColor = OrbitalSky,
                        type = OrbitalChipType.SKY_DOT,
                        onClick = onNavigateProtection
                    )

                    // Chip 3: Mint Green Dot (App Lock)
                    OrbitalSquircleChip(
                        glowColor = OrbitalMint,
                        type = OrbitalChipType.MINT_DOT,
                        onClick = onNavigateAppLock
                    )

                    // Chip 4: Coral Red Ring (Nuclear Mode)
                    OrbitalSquircleChip(
                        glowColor = OrbitalCoral,
                        type = OrbitalChipType.CORAL_RING,
                        onClick = onArmNuclearClicked
                    )

                    // Chip 5: Violet Orbital Double Ring (Focus Streak)
                    OrbitalSquircleChip(
                        glowColor = OrbitalViolet,
                        type = OrbitalChipType.VIOLET_ORBITAL,
                        onClick = onNavigateStats
                    )

                    // Arrow Chevron '>'
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateStats() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "View Details",
                            tint = earth.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 5. BENTO FEATURE GRID (3-Column Layout from Reference Mockup)
            // ====================================================================
            // Row 1: Medications / Reminders / Symptoms style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento 1: Reel Shields (Medications style)
                BentoCardExact(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Medication,
                    title = "Shields",
                    subtitle = if (todayTotalBlocks > 0) "$todayTotalBlocks kicks" else "Active",
                    onClick = onNavigateProtection
                )

                // Bento 2: Focus Lock (Reminders style)
                BentoCardExact(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Checklist,
                    title = "Focus Lock",
                    subtitle = if (isRegularLocked) "Enforcing" else "Start 25m",
                    onClick = {
                        if (isRegularLocked) onUnlockClicked() else onStartLockClicked()
                    }
                )

                // Bento 3: Nuclear Mode (Symptoms & Conditions style)
                BentoCardExact(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Emergency,
                    title = "Nuclear",
                    subtitle = if (isNuclearActive) "Armed ☢️" else "Zero-Bypass",
                    onClick = onArmNuclearClicked
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Medical / Document / Guide style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento 4: App Lock (Medical style)
                BentoCardExact(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Apps,
                    title = "App Lock",
                    subtitle = if (lockedAppsCount > 0) "$lockedAppsCount apps" else "0 apps",
                    onClick = onNavigateAppLock
                )

                // Bento 5: Insights & Stats (Report document style)
                BentoCardExact(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Description,
                    title = "Insights",
                    subtitle = "Analytics",
                    onClick = onNavigateStats
                )

                // Bento 6: Guide & Philosophy (Help style)
                BentoCardExact(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.HelpOutline,
                    title = "Guide",
                    subtitle = "Philosophy",
                    onClick = onOpenGuide
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 6. OEM ASSISTANT (ONLY IF PERMISSIONS MISSING)
            // ====================================================================
            if (!isServiceConnected) {
                val guidance = remember { OemNavigationManager.getGuidance() }
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrbitalCoral.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ Accessibility Shield Required",
                                color = earth.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "📱 ${guidance.brand.osSkin}",
                                color = OrbitalCoral,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Detected ${guidance.brand.displayName}. To activate feed blocking, allow restricted settings, then toggle Accessibility ON.",
                            color = earth.textMuted,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onEnableAccessibility,
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitalCoral),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Text(
                                text = "1. OPEN ACCESSIBILITY SETTINGS ➔",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { OemNavigationManager.openAppInfo(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = earth.surfaceVariant),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Text(
                                text = "2. ALLOW RESTRICTED SETTINGS ➔",
                                color = earth.textPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!isDeviceAdminActive) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, earth.border, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "🛡️ Anti-Uninstall Armor",
                                color = earth.textPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Prevents deleting Reel Narcotics during active Nuclear sessions.",
                                color = earth.textMuted,
                                fontSize = 10.5.sp
                            )
                        }
                        OutlinedButton(
                            onClick = onEnableDeviceAdmin,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("ENABLE", color = OrbitalCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ====================================================================
            // 7. BOTTOM QUICK FOCUS ACTION PILL (Exact Match to Mockup)
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 4-Dots Grid Menu Circular Button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, earth.border, CircleShape)
                        .clickable { onNavigateAppLock() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = "App Lock Menu",
                        tint = earth.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Wide Floating Prompt Action Pill: "Ask me about your diet..." Style
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, earth.border, RoundedCornerShape(25.dp))
                        .clickable {
                            if (isRegularLocked) onUnlockClicked() else onStartLockClicked()
                        }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                isNuclearActive -> "☢️ Nuclear Lock Active..."
                                isRegularLocked -> "🔒 Focus Lock Active • Tap to unlock..."
                                else -> "Start a 25m Focus Lock session..."
                            },
                            color = earth.textMuted,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Icon(
                            imageVector = Icons.Outlined.ArrowForward,
                            contentDescription = "Start",
                            tint = earth.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp)) // Clearance for floating bottom nav
        }
    }
}

enum class OrbitalChipType {
    AMBER_DOT,
    SKY_DOT,
    MINT_DOT,
    CORAL_RING,
    VIOLET_ORBITAL
}

/**
 * Individual frosted squircle chip matching each orbital item in the reference mockup.
 */
@Composable
private fun OrbitalSquircleChip(
    glowColor: Color,
    type: OrbitalChipType,
    onClick: () -> Unit
) {
    val earth = EarthTheme.colors

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.75f))
            .border(1.dp, earth.border, RoundedCornerShape(15.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(32.dp)) {
            val center = Offset(size.width / 2, size.height / 2)

            when (type) {
                OrbitalChipType.AMBER_DOT -> {
                    // Amber glowing circle + top mini accent arc
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, glowColor.copy(alpha = 0.3f), Color.Transparent),
                            center = center,
                            radius = 14.dp.toPx()
                        ),
                        radius = 14.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = glowColor,
                        radius = 5.5.dp.toPx(),
                        center = center
                    )
                    drawArc(
                        color = glowColor.copy(alpha = 0.7f),
                        startAngle = 210f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(center.x - 10.dp.toPx(), center.y - 13.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 20.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                OrbitalChipType.SKY_DOT -> {
                    // Sky blue glowing dot + tiny coral accent dot at top right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, glowColor.copy(alpha = 0.25f), Color.Transparent),
                            center = center,
                            radius = 14.dp.toPx()
                        ),
                        radius = 14.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = glowColor,
                        radius = 5.5.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = OrbitalCoral,
                        radius = 2.5.dp.toPx(),
                        center = Offset(center.x + 8.dp.toPx(), center.y - 8.dp.toPx())
                    )
                }
                OrbitalChipType.MINT_DOT -> {
                    // Mint green glowing dot + subtle top arc
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, glowColor.copy(alpha = 0.25f), Color.Transparent),
                            center = center,
                            radius = 14.dp.toPx()
                        ),
                        radius = 14.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = glowColor,
                        radius = 5.5.dp.toPx(),
                        center = center
                    )
                    drawArc(
                        color = glowColor.copy(alpha = 0.6f),
                        startAngle = 230f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(center.x - 10.dp.toPx(), center.y - 12.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 20.dp.toPx()),
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                OrbitalChipType.CORAL_RING -> {
                    // Coral red circular ring
                    drawCircle(
                        color = glowColor.copy(alpha = 0.15f),
                        radius = 12.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = glowColor,
                        radius = 10.5.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                OrbitalChipType.VIOLET_ORBITAL -> {
                    // Violet orbital double ring with inner core
                    drawCircle(
                        color = glowColor.copy(alpha = 0.20f),
                        radius = 12.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = glowColor,
                        radius = 11.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = glowColor,
                        radius = 4.dp.toPx(),
                        center = center
                    )
                }
            }
        }
    }
}

/**
 * Bento card matching the exact cards from reference mockup (Medications, Reminders, Symptoms).
 */
@Composable
private fun BentoCardExact(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val earth = EarthTheme.colors

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF6F0).copy(alpha = 0.90f)),
        modifier = modifier
            .height(110.dp)
            .border(1.dp, earth.border, RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top-left line icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = earth.textPrimary,
                modifier = Modifier.size(22.dp)
            )

            // Bottom-left title and subtitle
            Column {
                Text(
                    text = title,
                    color = earth.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 15.sp
                )
                Text(
                    text = subtitle,
                    color = earth.textMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
