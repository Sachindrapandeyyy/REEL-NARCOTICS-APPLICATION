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
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
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
import androidx.compose.ui.platform.LocalContext
import com.zenith.focus.core.permission.OemNavigationManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import com.zenith.focus.domain.nuclear.NuclearSessionStatus
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    nuclearSession: NuclearSession,
    lockState: LockState,
    @Suppress("UNUSED_PARAMETER") config: ProtectionConfig,
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
    onEnableDeviceAdmin: () -> Unit = {}
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

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning."
            in 12..16 -> "Good afternoon."
            in 17..21 -> "Good evening."
            else -> "Stay disciplined."
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(earth.canvas)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // TOP APP BAR: Brand, Day/Night Toggle & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.zenith.focus.R.drawable.ic_reel_narcotics_logo),
                    contentDescription = "Reel Narcotics",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, earth.border, RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "REEL NARCOTICS",
                        color = earth.forestGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "$greeting Break the scroll.",
                        color = earth.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Clarity Status Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(earth.surfaceSoft)
                        .border(1.dp, earth.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isNuclearActive) "☢️ NUCLEAR"
                        else if (isRegularLocked) "🔒 FOCUS LOCK"
                        else if (focusStreakDays > 0) "🔥 ${focusStreakDays}D STREAK"
                        else "STANDBY (OPEN)",
                        color = if (isNuclearActive) earth.camelOchre
                        else if (isRegularLocked) earth.forestGreen
                        else earth.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quick Day / Night Mode Toggle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(earth.surfaceSoft)
                        .border(1.dp, earth.border, CircleShape)
                        .clickable { onToggleTheme() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDarkTheme) "☀️" else "🌙",
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // EDITORIAL SERIF HEADER (Matching reference image)
        Column {
            Text(
                text = "Be present.",
                color = earth.forestGreen,
                fontSize = 36.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reclaim your attention & peace of mind.",
                color = earth.textMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(14.dp))
            // Camel Accent Line matching reference
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(earth.camelOchre)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5-SQUIRCLE ACTION GRID (Directly matching reference image layout)
        // Row 1: 3 squircle buttons (Focus Lock, Nuclear, Shields)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquircleActionButton(
                title = if (isNuclearActive) "LOCKED 🔒" else "FOCUS LOCK",
                icon = Icons.Outlined.Lock,
                containerColor = if (isNuclearActive) earth.surfaceSoft else earth.forestGreen,
                iconTint = if (isNuclearActive) earth.textMuted else Color.White,
                onClick = onStartLockClicked
            )
            SquircleActionButton(
                title = if (isNuclearActive) "EXTEND ☢️" else "NUCLEAR",
                icon = Icons.Outlined.Timer,
                containerColor = earth.camelOchre,
                iconTint = Color.White,
                onClick = onArmNuclearClicked
            )
            SquircleActionButton(
                title = "SHIELDS",
                icon = Icons.Outlined.Shield,
                containerColor = earth.sageOlive,
                iconTint = Color.White,
                onClick = onNavigateProtection
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 2: 2 centered squircle buttons (Insights, Guide)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquircleActionButton(
                title = "INSIGHTS",
                icon = Icons.Outlined.AutoStories,
                containerColor = earth.surfaceVariant,
                iconTint = earth.forestGreen,
                onClick = onNavigateStats
            )
            Spacer(modifier = Modifier.width(28.dp))
            SquircleActionButton(
                title = "GUIDE",
                icon = Icons.Outlined.HelpOutline,
                containerColor = earth.forestGreen,
                iconTint = Color.White,
                onClick = onEnableAccessibility
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // SYSTEM SHIELD STATUS & ACTIVATION ASSISTANT
        val allShieldsOperational = isServiceConnected && isDeviceAdminActive

        if (allShieldsOperational) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isNuclearActive) earth.camelOchre
                                    else if (isRegularLocked) earth.forestGreen
                                    else earth.sageOlive
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNuclearActive) "NUCLEAR ENFORCEMENT ACTIVE ☢️"
                                else if (isRegularLocked) "FOCUS RESTRICTIONS ACTIVE 🔒"
                                else "SHIELDS ARMED & READY (STANDBY)",
                                color = if (isNuclearActive) earth.camelOchre
                                else if (isRegularLocked) earth.forestGreen
                                else earth.forestGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isNuclearActive) "Strict zero-bypass mode active until timer ends"
                                else if (isRegularLocked) "Short-form feeds blocked during session"
                                else "No lock active = No restrictions. Tap any card above to start.",
                                color = earth.textMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isNuclearActive || isRegularLocked) "LOCKED" else "READY",
                        color = if (isNuclearActive) earth.camelOchre else earth.forestGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            val missingCount = (if (!isServiceConnected) 1 else 0) + (if (!isDeviceAdminActive) 1 else 0)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, earth.camelOchre, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(earth.camelOchre)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM SETUP REQUIRED ($missingCount/2 PENDING)",
                                color = earth.camelOchre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isServiceConnected) {
                        val guidance = remember { OemNavigationManager.getGuidance() }
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = earth.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { OemNavigationManager.openAppInfo(context) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ 1. Accessibility Shield",
                                        color = earth.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "📱 ${guidance.brand.osSkin}",
                                        color = earth.camelOchre,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Detected ${guidance.brand.displayName}. To activate, open App Info first to allow restricted settings, then enable Accessibility.",
                                    color = earth.textMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Button(
                                    onClick = { OemNavigationManager.openAppInfo(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = earth.forestDark),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Text(
                                        text = "1. TOUCH TO OPEN APP INFO ➔",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = onEnableAccessibility,
                                    colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Text(
                                        text = "2. OPEN ACCESSIBILITY SETTINGS ➔",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (guidance.step3ButtonLabel != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { OemNavigationManager.openOemAutostart(context) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                    ) {
                                        Text(
                                            text = guidance.step3ButtonLabel,
                                            color = earth.camelOchre,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!isDeviceAdminActive) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = earth.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🛡️ 2. Uninstall Protection (Device Admin)",
                                    color = earth.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Prevents deleting or bypassing Reel Narcotics during active Nuclear Mode focus sessions.",
                                    color = earth.textMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = onEnableDeviceAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = earth.camelOchre),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Text(
                                        text = "ACTIVATE UNINSTALL PROTECTION",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "💡 Pops up Android system prompt directly — simply tap 'Activate'",
                                    color = earth.camelOchre,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION: NUCLEAR MODE / FOCUS HERO CARD
        if (isNuclearActive) {
            // NUCLEAR MODE ACTIVE CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, earth.camelOchre, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☢️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NUCLEAR MODE ACTIVE",
                            color = earth.camelOchre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Large Countdown in Serif Font
                    Text(
                        text = DateTimeUtils.formatRemaining(remainingNuclearMillis),
                        color = earth.forestGreen,
                        fontSize = 40.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp
                    )

                    val nuclearEndTime = remember(nuclearSession.endTimeMillis) {
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(nuclearSession.endTimeMillis))
                    }

                    Text(
                        text = "Ends at $nuclearEndTime",
                        color = earth.textMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = nuclearProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = earth.camelOchre,
                        trackColor = earth.surface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(earth.surface)
                            .border(1.dp, earth.border, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOCKED UNTIL TIMER EXPIRATION",
                            color = earth.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        } else if (isRegularLocked) {
            // REGULAR FOCUS LOCK ACTIVE CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, earth.forestGreen, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FOCUS LOCK ACTIVE",
                        color = earth.forestGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = DateTimeUtils.formatRemaining(remainingLockMillis),
                        color = earth.forestGreen,
                        fontSize = 38.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = lockProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = earth.forestGreen,
                        trackColor = earth.surface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onUnlockClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "UNLOCK CHALLENGE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // INACTIVE STATE: TRANQUIL MINIMALIST STATUS
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(earth.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "Standby",
                            tint = earth.forestGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Distraction-Free Mindset",
                            color = earth.forestGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No lock active. Tap any card above or tap '+' to start focusing.",
                            color = earth.textMuted,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: TODAY'S PROTECTION
        Text(
            text = "TODAY'S PROTECTION",
            color = earth.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, earth.border, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                val isShortsEnforced = if (isNuclearActive) {
                    nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.YOUTUBE_SHORTS)
                } else if (isRegularLocked) {
                    lockState.enabledCategories.isEmpty() || lockState.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.YOUTUBE_SHORTS)
                } else false

                val isReelsEnforced = if (isNuclearActive) {
                    nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.INSTAGRAM_REELS)
                } else if (isRegularLocked) {
                    lockState.enabledCategories.isEmpty() || lockState.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.INSTAGRAM_REELS)
                } else false

                val isFacebookEnforced = if (isNuclearActive) {
                    nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.FACEBOOK_REELS)
                } else if (isRegularLocked) {
                    lockState.enabledCategories.isEmpty() || lockState.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.FACEBOOK_REELS)
                } else false

                val isAdultEnforced = if (isNuclearActive) {
                    nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.ADULT_WEBSITE) ||
                    nuclearSession.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.ADULT_KEYWORD)
                } else if (isRegularLocked) {
                    lockState.enabledCategories.isEmpty() || lockState.enabledCategories.contains(com.zenith.focus.domain.model.ContentCategory.ADULT_WEBSITE)
                } else false

                ProtectionItemRow(name = "YouTube Shorts", count = todayShortsBlocks, isEnforced = isShortsEnforced)
                Spacer(modifier = Modifier.height(12.dp))
                ProtectionItemRow(name = "Instagram Reels", count = todayReelsBlocks, isEnforced = isReelsEnforced)
                Spacer(modifier = Modifier.height(12.dp))
                ProtectionItemRow(name = "Facebook Reels", count = 0, isEnforced = isFacebookEnforced)
                Spacer(modifier = Modifier.height(12.dp))
                ProtectionItemRow(name = "Adult & Explicit Websites", count = todayAdultBlocks, isEnforced = isAdultEnforced)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: FOCUS OVERVIEW
        Text(
            text = "FOCUS OVERVIEW",
            color = earth.textMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Distractions Kicked Today
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, earth.border, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "KICKS RECORDED",
                        color = earth.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$todayTotalBlocks",
                        color = earth.forestGreen,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Distractions Blocked",
                        color = earth.camelOchre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Card 2: Protection Protocol
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, earth.border, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SHIELD PROTOCOL",
                        color = earth.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isNuclearActive) "NUCLEAR" else if (isRegularLocked) "STANDARD" else "STANDBY",
                        color = if (isNuclearActive) earth.camelOchre else if (isRegularLocked) earth.forestGreen else earth.textPrimary,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isNuclearActive) "Strict Zero-Bypass" else if (isRegularLocked) "Focus Active" else "No Restrictions",
                        color = earth.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SCANDINAVIAN LANDSCAPE ARTWORK CARD (DAY / NIGHT ADAPTIVE)
        OrganicLandscapeCard(
            isDark = isDarkTheme,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(110.dp)) // Complete clearance for floating bottom nav bar
    }
}

@Composable
fun OrganicLandscapeCard(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val drawableRes = if (isDark) {
        com.zenith.focus.R.drawable.bg_earth_landscape_night
    } else {
        com.zenith.focus.R.drawable.bg_earth_landscape_day
    }
    val earth = EarthTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, earth.border, RoundedCornerShape(22.dp))
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = drawableRes),
            contentDescription = "Scandinavian Landscape Artwork",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for harmonious blend with canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            earth.canvas.copy(alpha = 0.65f)
                        ),
                        startY = 60f
                    )
                )
        )

        // Inspirational zen quote overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = if (isDark) "Peace under the stars." else "Still waters run deep.",
                color = if (isDark) EarthNightText else EarthForestDark,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isDark) "Night protection is active. Rest your mind." else "Protected clarity for your mindful workflow.",
                color = if (isDark) EarthNightTextMuted else EarthTextMuted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun SquircleActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    val earth = EarthTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(containerColor)
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = earth.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
fun ProtectionItemRow(
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
                .clip(RoundedCornerShape(10.dp))
                .background(if (isEnforced) earth.forestGreen.copy(alpha = 0.12f) else earth.surface)
                .border(1.dp, if (isEnforced) earth.forestGreen.copy(alpha = 0.35f) else earth.border, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = if (isEnforced) "BLOCKED 🔒" else "READY 🔓",
                color = if (isEnforced) earth.forestGreen else earth.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
