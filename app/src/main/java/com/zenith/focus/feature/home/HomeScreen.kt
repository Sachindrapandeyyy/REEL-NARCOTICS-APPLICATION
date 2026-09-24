package com.zenith.focus.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val nuclearProgress = nuclearSession.progressFraction(now)

    val remainingLockMillis = lockState.remainingMillis(now)
    val lockProgress = lockState.progressFraction(now)

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(hour) {
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Peaceful Night"
        }
    }

    val mindfulAdvice = remember(hour, isNuclearActive, isRegularLocked) {
        when {
            isNuclearActive -> "Nuclear Shield is holding. Enjoy the stillness."
            isRegularLocked -> "Focus mode is active. Breathe and do deep work."
            hour in 5..11 -> "We recommend a mindful 25-minute focus session."
            hour in 12..16 -> "Keep your momentum steady without cheap dopamine."
            hour in 17..21 -> "Unwind gracefully. Guard your attention before sleep."
            else -> "Rest peacefully. Digital noise is blocked."
        }
    }

    val scrollState = rememberScrollState()

    // Ambient Pastel Glow Gradient Background
    val ambientGradient = remember(isDarkTheme) {
        if (isDarkTheme) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF13111C),
                    Color(0xFF1A1528),
                    Color(0xFF14111F)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF2F4), // Soft peach blush
                    Color(0xFFFBF8FD), // Serene lavender cream
                    Color(0xFFF3EDFA)  // Soft lilac mist
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ambientGradient)
    ) {
        // Subtle ambient glow orbs in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glowColor1 = if (isDarkTheme) Color(0xFFFF6584).copy(alpha = 0.08f) else Color(0xFFFF8DA3).copy(alpha = 0.15f)
            val glowColor2 = if (isDarkTheme) Color(0xFFA78BFA).copy(alpha = 0.09f) else Color(0xFFDDD6FE).copy(alpha = 0.22f)

            // Top right soft blush orb
            drawCircle(
                color = glowColor1,
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.95f, size.height * 0.12f)
            )
            // Center left soft violet orb
            drawCircle(
                color = glowColor2,
                radius = size.width * 0.65f,
                center = Offset(size.width * 0.05f, size.height * 0.50f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // TOP BAR: Navigation / Brand & Mindful Avatar + Theme Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) earth.surface else Color.White.copy(alpha = 0.9f))
                            .border(1.dp, earth.border, CircleShape)
                            .clickable { onOpenGuide() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Spa,
                            contentDescription = "Mindful Focus",
                            tint = earth.strawberryPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Zenith Focus",
                        color = earth.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Day / Night Toggle Pill
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) earth.surface else Color.White.copy(alpha = 0.9f))
                            .border(1.dp, earth.border, CircleShape)
                            .clickable { onToggleTheme() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDarkTheme) "☀️" else "🌙",
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Mindful Avatar Profile Chip
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(earth.strawberryPink.copy(alpha = 0.15f))
                            .border(1.5.dp, earth.strawberryPink.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (focusStreakDays > 0) "🔥" else "🧘",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MINDFUL EDITORIAL GREETING (Directly matching reference mockup)
            Column {
                Text(
                    text = greeting,
                    color = earth.textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mindfulAdvice,
                    color = earth.textMuted,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HERO CIRCULAR PROGRESS GAUGE CARD (Directly matching "715 / 6000 step" from mockup)
            FrostedCircularGaugeCard(
                todayTotalBlocks = todayTotalBlocks,
                isNuclearActive = isNuclearActive,
                nuclearProgress = nuclearProgress,
                remainingNuclearMillis = remainingNuclearMillis,
                endTimeMillis = nuclearSession.endTimeMillis,
                isRegularLocked = isRegularLocked,
                lockProgress = lockProgress,
                remainingLockMillis = remainingLockMillis,
                focusStreakDays = focusStreakDays,
                onUnlockClicked = onUnlockClicked,
                onStartLockClicked = onStartLockClicked
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2-COLUMN BENTO HABIT CARDS (Directly matching "Drink 8 cups" & "Sleep 8 hours")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Shields & Feeds Blocked (Drink style)
                BentoHabitCard(
                    modifier = Modifier.weight(1f),
                    title = "Shields",
                    icon = Icons.Outlined.Shield,
                    iconTint = earth.strawberryPink,
                    subtitle = "Feeds Blocked",
                    progressFraction = (todayTotalBlocks / 50f).coerceIn(0.05f, 1f),
                    progressColor = earth.strawberryPink,
                    valueText = "$todayTotalBlocks",
                    unitText = "kicks",
                    onClick = onNavigateProtection
                )

                // Card 2: Focus Lock & Sleep/Rest Mode (Sleep style)
                BentoHabitCard(
                    modifier = Modifier.weight(1f),
                    title = "Focus Lock",
                    icon = Icons.Outlined.Bedtime,
                    iconTint = earth.pastelViolet,
                    subtitle = if (isNuclearActive || isRegularLocked) "Enforcing" else "Streak Goal",
                    progressFraction = if (isNuclearActive) nuclearProgress else if (isRegularLocked) lockProgress else (focusStreakDays / 7f).coerceIn(0.1f, 1f),
                    progressColor = earth.pastelViolet,
                    valueText = if (isNuclearActive || isRegularLocked) "Active" else "$focusStreakDays",
                    unitText = if (isNuclearActive || isRegularLocked) "locked" else "days",
                    onClick = {
                        if (isNuclearActive || isRegularLocked) onUnlockClicked() else onStartLockClicked()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // WIDE BENTO CARD: Active Focus Time & Discipline (Matching "Active time 0 / 60 mnt | 1172 kkal")
            WideActiveFocusCard(
                todayTotalBlocks = todayTotalBlocks,
                focusStreakDays = focusStreakDays,
                onClick = onNavigateStats
            )

            Spacer(modifier = Modifier.height(22.dp))

            // MINDFUL PILL ACTION BUTTONS (Matching "login ->" pill button design)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MindfulPillButton(
                    text = if (isNuclearActive) "Nuclear Active ☢️" else if (isRegularLocked) "Unlock Focus 🔓" else "Start Focus →",
                    isPrimary = true,
                    modifier = Modifier.weight(1.3f),
                    onClick = {
                        if (isRegularLocked) onUnlockClicked() else onStartLockClicked()
                    }
                )

                MindfulPillButton(
                    text = if (isNuclearActive) "Extend ☢️" else "Nuclear ☢️",
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    onClick = onArmNuclearClicked
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Action Row: App Lock & Guide Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MindfulPillButton(
                    text = if (lockedAppsCount > 0) "App Lock ($lockedAppsCount) 🛡️" else "App Blocker 🛡️",
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateAppLock
                )

                MindfulPillButton(
                    text = "Mindful Guide 📖",
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenGuide
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // SYSTEM SHIELD & OEM PERMISSION ASSISTANT
            if (!isServiceConnected) {
                OemGuidanceCard(
                    onEnableAccessibility = onEnableAccessibility,
                    context = context
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!isDeviceAdminActive) {
                DeviceAdminWarningCard(
                    onEnableDeviceAdmin = onEnableDeviceAdmin
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // TODAY'S PROTECTION BREAKDOWN CARD
            Text(
                text = "PROTECTED SURFACES",
                color = earth.textMuted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (earth.isDark) earth.surface.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (earth.isDark) earth.border else Color.White.copy(alpha = 0.8f),
                        RoundedCornerShape(26.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val isShortsEnforced = if (isNuclearActive) {
                        nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.YOUTUBE_SHORTS)
                    } else if (isRegularLocked) {
                        config.blockYouTubeShorts && (lockState.enabledCategories.isEmpty() || lockState.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.YOUTUBE_SHORTS))
                    } else config.blockYouTubeShorts

                    val isReelsEnforced = if (isNuclearActive) {
                        nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.INSTAGRAM_REELS)
                    } else if (isRegularLocked) {
                        config.blockInstagramReels && (lockState.enabledCategories.isEmpty() || lockState.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.INSTAGRAM_REELS))
                    } else config.blockInstagramReels

                    val isAdultEnforced = if (isNuclearActive) {
                        nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.ADULT_WEBSITE) ||
                        nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.ADULT_KEYWORD)
                    } else if (isRegularLocked) {
                        (config.blockAdultWebsites || config.blockAdultKeywords) && (lockState.enabledCategories.isEmpty() || lockState.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.ADULT_WEBSITE))
                    } else (config.blockAdultWebsites || config.blockAdultKeywords)

                    PastelProtectionRow(name = "YouTube Shorts", count = todayShortsBlocks, isEnforced = isShortsEnforced)
                    Spacer(modifier = Modifier.height(12.dp))
                    PastelProtectionRow(name = "Instagram Reels", count = todayReelsBlocks, isEnforced = isReelsEnforced)
                    Spacer(modifier = Modifier.height(12.dp))
                    PastelProtectionRow(name = "Facebook Reels", count = 0, isEnforced = config.blockFacebookReels)
                    Spacer(modifier = Modifier.height(12.dp))
                    PastelProtectionRow(name = "Adult & Explicit Content", count = todayAdultBlocks, isEnforced = isAdultEnforced)
                }
            }

            Spacer(modifier = Modifier.height(110.dp)) // Clearance for floating bottom nav
        }
    }
}

/**
 * Hero Circular Progress Gauge matching the "715 / 6000 step" circular ring dial from reference mockup.
 */
@Composable
private fun FrostedCircularGaugeCard(
    todayTotalBlocks: Int,
    isNuclearActive: Boolean,
    nuclearProgress: Float,
    remainingNuclearMillis: Long,
    endTimeMillis: Long,
    isRegularLocked: Boolean,
    lockProgress: Float,
    remainingLockMillis: Long,
    focusStreakDays: Int,
    onUnlockClicked: () -> Unit,
    onStartLockClicked: () -> Unit
) {
    val earth = EarthTheme.colors

    // Calculate target gauge fraction
    val targetFraction = when {
        isNuclearActive -> nuclearProgress.coerceIn(0.02f, 1f)
        isRegularLocked -> lockProgress.coerceIn(0.02f, 1f)
        else -> (todayTotalBlocks / 50f).coerceIn(0.12f, 1f)
    }

    val animatedSweep by animateFloatAsState(
        targetValue = targetFraction * 360f,
        animationSpec = tween(durationMillis = 900),
        label = "GaugeSweep"
    )

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (earth.isDark) earth.surface.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.88f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (earth.isDark) earth.border else Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(32.dp)
            )
            .clickable {
                if (isRegularLocked) onUnlockClicked() else onStartLockClicked()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Ring Dial
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(175.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Background track ring (soft muted lavender/grey)
                    drawArc(
                        color = earth.gaugeTrack,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active progress ring (Strawberry Pink #FF6584)
                    drawArc(
                        color = if (isNuclearActive) earth.strawberryPink else earth.forestGreen,
                        startAngle = -90f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner content of the circular gauge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        isNuclearActive -> {
                            Text(
                                text = DateTimeUtils.formatRemaining(remainingNuclearMillis),
                                color = earth.forestGreen,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            val endFormatted = remember(endTimeMillis) {
                                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(endTimeMillis))
                            }
                            Text(
                                text = "Ends $endFormatted",
                                color = earth.textMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        isRegularLocked -> {
                            Text(
                                text = DateTimeUtils.formatRemaining(remainingLockMillis),
                                color = earth.forestGreen,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Focus Locked",
                                color = earth.textMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        else -> {
                            Text(
                                text = "$todayTotalBlocks",
                                color = earth.textPrimary,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "/50 daily goal",
                                color = earth.textMuted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sub-gauge status tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isNuclearActive) earth.strawberryPink.copy(alpha = 0.12f)
                        else if (isRegularLocked) earth.pastelViolet.copy(alpha = 0.12f)
                        else earth.surfaceSoft
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when {
                        isNuclearActive -> "☢️ Strict Zero-Bypass Shield Active"
                        isRegularLocked -> "🔒 Focus Restraints Engaged"
                        focusStreakDays > 0 -> "🔥 ${focusStreakDays}-Day Mindful Streak"
                        else -> "✨ Shields Active • Tap to Focus"
                    },
                    color = if (isNuclearActive) earth.strawberryPink
                    else if (isRegularLocked) earth.pastelViolet
                    else earth.textMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 2-Column Bento Habit Card matching "Drink 8 cups" & "Sleep 8 hours" from reference mockup.
 */
@Composable
private fun BentoHabitCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    subtitle: String,
    progressFraction: Float,
    progressColor: Color,
    valueText: String,
    unitText: String,
    onClick: () -> Unit
) {
    val earth = EarthTheme.colors

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (earth.isDark) earth.surface.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.88f)
        ),
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (earth.isDark) earth.border else Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Title & Mini Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = earth.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle / Target label
            Text(
                text = subtitle,
                color = earth.textMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Mini progress indicator bar
            LinearProgressIndicator(
                progress = progressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressColor,
                trackColor = earth.gaugeTrack
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Large Value Metric: "$valueText $unitText"
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = valueText,
                    color = earth.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unitText,
                    color = earth.textMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

/**
 * Wide Bento Card matching "Active time 0 / 60 mnt | 1172 kkal" from reference mockup.
 */
@Composable
private fun WideActiveFocusCard(
    todayTotalBlocks: Int,
    focusStreakDays: Int,
    onClick: () -> Unit
) {
    val earth = EarthTheme.colors
    val reclaimedMinutes = (todayTotalBlocks * 2.5).toInt()

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (earth.isDark) earth.surface.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.88f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (earth.isDark) earth.border else Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active time",
                    color = earth.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "Active Time",
                    tint = earth.textMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$reclaimedMinutes",
                        color = earth.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " / 60 mnt",
                        color = earth.textMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥 ${focusStreakDays}d streak",
                        color = earth.strawberryPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${todayTotalBlocks} saved",
                        color = earth.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Mindful Pill Button matching "login ->" style from reference mockup.
 */
@Composable
private fun MindfulPillButton(
    text: String,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val earth = EarthTheme.colors

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (isPrimary) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            earth.strawberryPink,
                            earth.strawberryPink.copy(alpha = 0.9f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            if (earth.isDark) earth.surface else Color.White.copy(alpha = 0.90f),
                            if (earth.isDark) earth.surfaceSoft else Color.White.copy(alpha = 0.85f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isPrimary) Color.Transparent else if (earth.isDark) earth.border else Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = if (isPrimary) Color.White else earth.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PastelProtectionRow(
    name: String,
    count: Int,
    isEnforced: Boolean
) {
    val earth = EarthTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = name,
                color = earth.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (count > 0) {
                Text(
                    text = "$count kicks recorded today",
                    color = earth.textMuted,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isEnforced) earth.strawberryPink.copy(alpha = 0.12f)
                    else earth.surfaceSoft
                )
                .border(
                    1.dp,
                    if (isEnforced) earth.strawberryPink.copy(alpha = 0.35f) else earth.border,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isEnforced) "BLOCKED 🔒" else "READY 🔓",
                color = if (isEnforced) earth.strawberryPink else earth.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun OemGuidanceCard(
    onEnableAccessibility: () -> Unit,
    context: android.content.Context
) {
    val earth = EarthTheme.colors
    val guidance = remember { OemNavigationManager.getGuidance() }

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (earth.isDark) earth.surface.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, earth.strawberryPink.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
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
                    color = earth.strawberryPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Detected ${guidance.brand.displayName}. To activate real-time feed protection, allow restricted settings in App Info, then toggle Accessibility ON.",
                color = earth.textMuted,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onEnableAccessibility,
                colors = ButtonDefaults.buttonColors(containerColor = earth.strawberryPink),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (earth.isDark) earth.surfaceSoft else earth.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
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
}

@Composable
private fun DeviceAdminWarningCard(
    onEnableDeviceAdmin: () -> Unit
) {
    val earth = EarthTheme.colors

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (earth.isDark) earth.surface.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, earth.border, RoundedCornerShape(24.dp))
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
                    text = "Recommended for Nuclear Mode to prevent uninstallation.",
                    color = earth.textMuted,
                    fontSize = 10.5.sp
                )
            }
            OutlinedButton(
                onClick = onEnableDeviceAdmin,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("ENABLE", color = earth.strawberryPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
