package com.zenith.focus.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.ZenithAlabaster
import com.zenith.focus.core.designsystem.ZenithBurgundy
import com.zenith.focus.core.designsystem.ZenithBurgundyDeep
import com.zenith.focus.core.designsystem.ZenithCardDark
import com.zenith.focus.core.designsystem.ZenithEmerald
import com.zenith.focus.core.designsystem.ZenithEmeraldAccent
import com.zenith.focus.core.designsystem.ZenithNavy
import com.zenith.focus.core.designsystem.ZenithNavyDark
import com.zenith.focus.core.designsystem.ZenithNavyLight
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
            .background(ZenithNavyDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
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
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "REEL NARCOTICS",
                        color = ZenithEmeraldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Break the scroll.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Clarity Status Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(ZenithNavyLight)
                    .border(
                        1.dp,
                        if (isNuclearActive) ZenithBurgundy
                        else if (isRegularLocked) ZenithEmeraldAccent.copy(alpha = 0.5f)
                        else Color(0xFF334155),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isNuclearActive) "☢️ NUCLEAR (STRICT)"
                    else if (isRegularLocked) "🔒 STANDARD LOCK"
                    else "🟢 STANDBY (NO LOCK)",
                    color = if (isNuclearActive) Color(0xFFF43F5E)
                    else if (isRegularLocked) ZenithEmeraldAccent
                    else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Column {
            Text(
                text = greeting,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Take back your attention.",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // SYSTEM SHIELD STATUS & ACTIVATION ASSISTANT
        val allShieldsOperational = isServiceConnected && isDeviceAdminActive
        Spacer(modifier = Modifier.height(14.dp))

        if (allShieldsOperational) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                .background(if (isNuclearActive) Color(0xFFF43F5E) else ZenithEmeraldAccent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNuclearActive) "NUCLEAR RESTRICTIONS STRICTLY ENFORCED ☢️"
                                else if (isRegularLocked) "STANDARD FOCUS RESTRICTIONS ACTIVE 🔒"
                                else "SHIELDS ARMED & READY (STANDBY) 🛡️",
                                color = if (isNuclearActive) Color(0xFFF43F5E) else ZenithEmeraldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isNuclearActive) "Nuclear Lock: Strict zero-bypass mode active until timer ends"
                                else if (isRegularLocked) "Standard Focus: Short-form feeds blocked during session"
                                else "No lock active = No restrictions. Start a lock to block shorts.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isNuclearActive || isRegularLocked) "LOCKED" else "READY",
                        color = if (isNuclearActive) Color(0xFFF43F5E) else ZenithEmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        } else {
            val missingCount = (if (!isServiceConnected) 1 else 0) + (if (!isDeviceAdminActive) 1 else 0)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1018)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFE11D48), RoundedCornerShape(16.dp))
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
                                    .background(Color(0xFFF43F5E))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM SETUP REQUIRED ($missingCount/2 PENDING)",
                                color = Color(0xFFFECACA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isServiceConnected) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1622)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ 1. Accessibility Shield",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Required to detect and immediately close YouTube Shorts & Instagram Reels feeds. 100% offline — zero personal data collected.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = onEnableAccessibility,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
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
                                    color = Color(0xFFFDA4AF),
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    if (!isDeviceAdminActive) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1C14)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🛡️ 2. Uninstall Protection (Device Admin)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Prevents deleting or bypassing Reel Narcotics during active Nuclear Mode focus sessions.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = onEnableDeviceAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
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
                                    color = Color(0xFFFDE68A),
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithBurgundyDeep.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, ZenithBurgundy, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☢️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NUCLEAR MODE ACTIVE",
                            color = Color(0xFFFECACA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Large Countdown
                    Text(
                        text = DateTimeUtils.formatRemaining(remainingNuclearMillis),
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    val nuclearEndTime = remember(nuclearSession.endTimeMillis) {
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(nuclearSession.endTimeMillis))
                    }

                    Text(
                        text = "Ends at $nuclearEndTime",
                        color = Color(0xFFE2E8F0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = nuclearProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFF43F5E),
                        trackColor = ZenithNavyDark
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Calm Status Indicator: LOCKED UNTIL TIMER EXPIRATION
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZenithNavyDark.copy(alpha = 0.8f))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOCKED UNTIL TIMER EXPIRATION",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        } else if (isRegularLocked) {
            // REGULAR FOCUS LOCK ACTIVE CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithCardDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, ZenithEmeraldAccent, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FOCUS LOCK ACTIVE",
                        color = ZenithEmeraldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = DateTimeUtils.formatRemaining(remainingLockMillis),
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = lockProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ZenithEmeraldAccent,
                        trackColor = ZenithNavyDark
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onUnlockClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithNavyLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "UNLOCK CHALLENGE", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // INACTIVE STATE: NUCLEAR COMMITMENT HERO + QUICK LOCK
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "IRREVERSIBLE DISCIPLINE",
                                color = Color(0xFFF43F5E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Nuclear Mode",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ZenithBurgundyDeep),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "☢️", fontSize = 18.sp)
                        }
                    }

                    Text(
                        text = "Locks your device into pure productivity. Zero pause, zero cancel, zero bypass until the timer expires.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
                    )

                    // NUCLEAR MODE CALL TO ACTION
                    Button(
                        onClick = onArmNuclearClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithBurgundy),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "ARM NUCLEAR MODE ☢️",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onStartLockClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "STANDARD FOCUS LOCK 🔒",
                            color = ZenithEmeraldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: TODAY'S PROTECTION (With Muted Deep Burgundy Badges)
        Text(
            text = "TODAY'S PROTECTION",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ZenithNavy),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val isEnforced = isNuclearActive || isRegularLocked
                ProtectionItemRow(name = "YouTube Shorts", count = todayShortsBlocks, isEnforced = isEnforced)
                Spacer(modifier = Modifier.height(12.dp))
                ProtectionItemRow(name = "Instagram Reels", count = todayReelsBlocks, isEnforced = isEnforced)
                Spacer(modifier = Modifier.height(12.dp))
                ProtectionItemRow(name = "Facebook Reels", count = 0, isEnforced = isEnforced)
                Spacer(modifier = Modifier.height(12.dp))
                ProtectionItemRow(name = "Adult & Explicit Websites", count = todayAdultBlocks, isEnforced = isEnforced)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: FOCUS OVERVIEW
        Text(
            text = "FOCUS OVERVIEW",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Distractions Kicked Today
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithNavy),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "KICKS RECORDED",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$todayTotalBlocks",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Distractions Terminated",
                        color = ZenithEmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Card 2: Protection Protocol
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithNavy),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SHIELD PROTOCOL",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isNuclearActive) "NUCLEAR" else if (isRegularLocked) "STANDARD" else "STANDBY",
                        color = if (isNuclearActive) Color(0xFFF43F5E) else if (isRegularLocked) ZenithEmeraldAccent else Color(0xFF94A3B8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isNuclearActive) "Strict Restriction" else if (isRegularLocked) "Focus Active" else "No Restrictions",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
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
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (count > 0) {
                Text(
                    text = "$count kicks recorded today",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isEnforced) ZenithBurgundyDeep else Color(0xFF1E293B))
                .border(1.dp, if (isEnforced) ZenithBurgundy else Color(0xFF334155), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isEnforced) "BLOCKED" else "READY",
                color = if (isEnforced) Color(0xFFFECACA) else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}
