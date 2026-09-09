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
    config: ProtectionConfig,
    isServiceConnected: Boolean = true,
    isDeviceAdminActive: Boolean = true,
    todayTotalBlocks: Int,
    todayShortsBlocks: Int,
    todayReelsBlocks: Int,
    todayAdultBlocks: Int,
    focusStreakDays: Int,
    onArmNuclearClicked: () -> Unit,
    onStartLockClicked: () -> Unit,
    onUnlockClicked: () -> Unit,
    onNavigateProtection: () -> Unit,
    onNavigateStats: () -> Unit,
    onEnableAccessibility: () -> Unit = {},
    onEnableDeviceAdmin: () -> Unit = {}
) {
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
            .background(EarthCanvasCream)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // TOP APP BAR: Brand & Time Greeting
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
                        .border(1.dp, EarthBorderLinen, RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "REEL NARCOTICS",
                        color = EarthForestGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Break the scroll.",
                        color = EarthTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Clarity Status Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(EarthSurfaceLinen)
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isNuclearActive) "☢️ NUCLEAR"
                    else if (isRegularLocked) "🔒 FOCUS LOCK"
                    else "STANDBY (OPEN)",
                    color = if (isNuclearActive) EarthCamelOchre
                    else if (isRegularLocked) EarthForestGreen
                    else EarthTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // EDITORIAL SERIF HEADER (Matching reference image)
        Column {
            Text(
                text = "Be present.",
                color = EarthForestGreen,
                fontSize = 36.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reclaim your attention & peace of mind.",
                color = EarthTextMuted,
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
                    .background(EarthAccentRule)
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
                title = "FOCUS LOCK",
                icon = Icons.Outlined.Lock,
                containerColor = EarthForestGreen,
                iconTint = Color.White,
                onClick = onStartLockClicked
            )
            SquircleActionButton(
                title = "NUCLEAR",
                icon = Icons.Outlined.Timer,
                containerColor = EarthCamelOchre,
                iconTint = Color.White,
                onClick = onArmNuclearClicked
            )
            SquircleActionButton(
                title = "SHIELDS",
                icon = Icons.Outlined.Shield,
                containerColor = EarthSageOlive,
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
                containerColor = EarthSandCard,
                iconTint = EarthForestGreen,
                onClick = onNavigateStats
            )
            Spacer(modifier = Modifier.width(28.dp))
            SquircleActionButton(
                title = "GUIDE",
                icon = Icons.Outlined.HelpOutline,
                containerColor = EarthForestGreen,
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
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
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
                                    if (isNuclearActive) EarthCamelOchre
                                    else if (isRegularLocked) EarthForestGreen
                                    else EarthSageOlive
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNuclearActive) "NUCLEAR ENFORCEMENT ACTIVE ☢️"
                                else if (isRegularLocked) "FOCUS RESTRICTIONS ACTIVE 🔒"
                                else "SHIELDS ARMED & READY (STANDBY)",
                                color = if (isNuclearActive) EarthCamelOchre
                                else if (isRegularLocked) EarthForestGreen
                                else EarthForestGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isNuclearActive) "Strict zero-bypass mode active until timer ends"
                                else if (isRegularLocked) "Short-form feeds blocked during session"
                                else "No lock active = No restrictions. Tap any card above to start.",
                                color = EarthTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isNuclearActive || isRegularLocked) "LOCKED" else "READY",
                        color = if (isNuclearActive) EarthCamelOchre else EarthForestGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            val missingCount = (if (!isServiceConnected) 1 else 0) + (if (!isDeviceAdminActive) 1 else 0)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, EarthCamelOchre, RoundedCornerShape(18.dp))
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
                                    .background(EarthCamelOchre)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM SETUP REQUIRED ($missingCount/2 PENDING)",
                                color = EarthCamelOchre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isServiceConnected) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ 1. Accessibility Shield",
                                    color = EarthForestDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Required to detect and immediately close YouTube Shorts & Instagram Reels feeds. 100% offline — zero personal data collected.",
                                    color = EarthTextMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = onEnableAccessibility,
                                    colors = ButtonDefaults.buttonColors(containerColor = EarthForestGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                ) {
                                    Text(
                                        text = "ACTIVATE ACCESSIBILITY SHIELD",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "💡 Tap button -> 'Installed apps' -> 'Reel Narcotics Shield' -> Turn ON",
                                    color = EarthCamelDeep,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    if (!isDeviceAdminActive) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🛡️ 2. Uninstall Protection (Device Admin)",
                                    color = EarthForestDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Prevents deleting or bypassing Reel Narcotics during active Nuclear Mode focus sessions.",
                                    color = EarthTextMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = onEnableDeviceAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = EarthCamelOchre),
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
                                    color = EarthCamelDeep,
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
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, EarthCamelOchre, RoundedCornerShape(20.dp))
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
                            color = EarthCamelOchre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Large Countdown in Serif Font
                    Text(
                        text = DateTimeUtils.formatRemaining(remainingNuclearMillis),
                        color = EarthForestDark,
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
                        color = EarthTextMuted,
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
                        color = EarthCamelOchre,
                        trackColor = EarthSurfaceLinen
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EarthSurfaceLinen)
                            .border(1.dp, EarthBorderLinen, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOCKED UNTIL TIMER EXPIRATION",
                            color = EarthTextMuted,
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
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, EarthForestGreen, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FOCUS LOCK ACTIVE",
                        color = EarthForestGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = DateTimeUtils.formatRemaining(remainingLockMillis),
                        color = EarthForestDark,
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
                        color = EarthForestGreen,
                        trackColor = EarthSurfaceLinen
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onUnlockClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EarthForestGreen),
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
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
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
                            .background(EarthSurfaceLinen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "Standby",
                            tint = EarthForestGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Distraction-Free Mindset",
                            color = EarthForestGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No lock active. Tap any card above or tap '+' to start focusing.",
                            color = EarthTextMuted,
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
            color = EarthTextMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
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
            color = EarthTextMuted,
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
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "KICKS RECORDED",
                        color = EarthTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$todayTotalBlocks",
                        color = EarthForestDark,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Distractions Blocked",
                        color = EarthCamelOchre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Card 2: Protection Protocol
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SHIELD PROTOCOL",
                        color = EarthTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isNuclearActive) "NUCLEAR" else if (isRegularLocked) "STANDARD" else "STANDBY",
                        color = if (isNuclearActive) EarthCamelOchre else if (isRegularLocked) EarthForestGreen else EarthTextDark,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isNuclearActive) "Strict Zero-Bypass" else if (isRegularLocked) "Focus Active" else "No Restrictions",
                        color = EarthTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LANDSCAPE ARTWORK (Mountains + Sun minimalist illustration matching reference)
        OrganicLandscapeArtwork(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        )

        Spacer(modifier = Modifier.height(64.dp)) // Clearance for floating bottom bar
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
            color = EarthTextDark,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = name,
                color = EarthTextDark,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (count > 0) {
                Text(
                    text = "$count kicks recorded today",
                    color = EarthTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isEnforced) EarthForestGreen.copy(alpha = 0.12f) else EarthSurfaceLinen)
                .border(1.dp, if (isEnforced) EarthForestGreen.copy(alpha = 0.35f) else EarthBorderLinen, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = if (isEnforced) "BLOCKED 🔒" else "READY 🔓",
                color = if (isEnforced) EarthForestGreen else EarthTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun OrganicLandscapeArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Soft sun in warm golden camel
        drawCircle(
            color = Color(0xFFC9A97E).copy(alpha = 0.35f),
            radius = height * 0.32f,
            center = androidx.compose.ui.geometry.Offset(width * 0.72f, height * 0.40f)
        )

        // Background gentle hills (sage olive tone)
        val hillPathBack = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height * 0.75f)
            quadraticBezierTo(width * 0.25f, height * 0.50f, width * 0.55f, height * 0.70f)
            quadraticBezierTo(width * 0.80f, height * 0.82f, width, height * 0.60f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(hillPathBack, color = Color(0xFF868D7D).copy(alpha = 0.32f))

        // Foreground layered hills (forest pine tone)
        val hillPathFront = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, height * 0.88f)
            quadraticBezierTo(width * 0.35f, height * 0.68f, width * 0.68f, height * 0.85f)
            quadraticBezierTo(width * 0.88f, height * 0.94f, width, height * 0.80f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(hillPathFront, color = Color(0xFF204844).copy(alpha = 0.22f))
    }
}
