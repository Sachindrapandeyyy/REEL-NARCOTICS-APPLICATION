package com.zenith.focus.feature.nuclear

import android.view.HapticFeedbackConstants
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.ZenithAlabaster
import com.zenith.focus.core.designsystem.ZenithBurgundy
import com.zenith.focus.core.designsystem.ZenithBurgundyDeep
import com.zenith.focus.core.designsystem.ZenithCardDark
import com.zenith.focus.core.designsystem.ZenithEmerald
import com.zenith.focus.core.designsystem.ZenithEmeraldAccent
import com.zenith.focus.core.designsystem.ZenithNavy
import com.zenith.focus.core.designsystem.ZenithNavyDark
import com.zenith.focus.core.time.DateTimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NuclearArmingDialog(
    onDismiss: () -> Unit,
    onArmSession: (durationMillis: Long) -> Unit,
    onConfirmActivation: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) } // 0: Warning & Duration, 1: Ready & Hold to Activate
    var selectedDurationMillis by remember { mutableStateOf(2 * 60 * 60 * 1000L) } // default 2 hours

    // Custom Hours & Minutes
    var customHours by remember { mutableIntStateOf(2) }
    var customMinutes by remember { mutableIntStateOf(0) }
    var isCustomPickerOpen by remember { mutableStateOf(false) }

    val view = LocalView.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val durations = remember {
        listOf(
            "15 MIN" to (15 * 60 * 1000L),
            "30 MIN" to (30 * 60 * 1000L),
            "1 HOUR" to (60 * 60 * 1000L),
            "2 HOURS" to (2 * 60 * 60 * 1000L),
            "6 HOURS" to (6 * 60 * 60 * 1000L),
            "12 HOURS" to (12 * 60 * 60 * 1000L),
            "24 HOURS" to (24 * 60 * 60 * 1000L)
        )
    }

    val targetTimeFormatted = remember(selectedDurationMillis) {
        val target = System.currentTimeMillis() + selectedDurationMillis
        SimpleDateFormat("h:mm a (MMM d)", Locale.getDefault()).format(Date(target))
    }

    Dialog(onDismissRequest = {
        if (step == 0) onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ZenithNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                if (step == 0) {
                    // STEP 1: WARNING & DURATION SELECTION
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ZenithBurgundyDeep),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "☢️", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NUCLEAR MODE",
                            color = Color(0xFFF43F5E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Irreversible Focus Commitment",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    // EXPLANATION CARD
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ZenithBurgundyDeep.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .border(1.dp, ZenithBurgundy, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "YOU ARE COMMITTING TO THIS SESSION:",
                                color = Color(0xFFFECACA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• You CANNOT turn Nuclear Mode off once started.\n" +
                                       "• You CANNOT shorten or pause the timer.\n" +
                                       "• All addictive feeds will remain 100% blocked.\n" +
                                       "• Force closing or reopening will NOT cancel it.\n" +
                                       "• Deleting or tampering with the app is blocked.\n" +
                                       "• Reboots will NOT bypass it.\n" +
                                       "• Ends automatically when the timer expires.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    val isAdminActive = remember { ZenithDeviceAdminReceiver.isAdminActive(context) }
                    if (!isAdminActive) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = ZenithDeviceAdminReceiver.createAddAdminIntent(context)
                                    context.startActivity(intent)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "UNINSTALL PROTECTION RECOMMENDED",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Tap to grant Device Admin so the app cannot be uninstalled during this session.",
                                        color = Color(0xFFFEF3C7),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, ZenithEmeraldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🛡️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Uninstall Protection Armed (Device Admin active)",
                                    color = ZenithEmeraldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "SELECT COMMITMENT DURATION",
                        color = ZenithAlabaster.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // DURATION CHIPS (Rows of 2)
                    for (i in durations.indices step 2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val item1 = durations[i]
                            val isSel1 = !isCustomPickerOpen && selectedDurationMillis == item1.second
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel1) ZenithEmerald else ZenithCardDark)
                                    .border(
                                        1.dp,
                                        if (isSel1) ZenithEmeraldAccent else Color(0xFF334155),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        isCustomPickerOpen = false
                                        selectedDurationMillis = item1.second
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item1.first,
                                    color = if (isSel1) Color.White else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (i + 1 < durations.size) {
                                val item2 = durations[i + 1]
                                val isSel2 = !isCustomPickerOpen && selectedDurationMillis == item2.second
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel2) ZenithEmerald else ZenithCardDark)
                                    .border(
                                        1.dp,
                                        if (isSel2) ZenithEmeraldAccent else Color(0xFF334155),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        isCustomPickerOpen = false
                                        selectedDurationMillis = item2.second
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item2.first,
                                        color = if (isSel2) Color.White else Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // CUSTOM DURATION ACCORDION
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCustomPickerOpen) ZenithEmerald else ZenithCardDark)
                            .border(
                                1.dp,
                                if (isCustomPickerOpen) ZenithEmeraldAccent else Color(0xFF334155),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                isCustomPickerOpen = !isCustomPickerOpen
                                if (isCustomPickerOpen) {
                                    selectedDurationMillis = (customHours * 3600000L) + (customMinutes * 60000L)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCustomPickerOpen) "CUSTOM: ${customHours}h ${customMinutes}m" else "CUSTOM DURATION ⏱️",
                            color = if (isCustomPickerOpen) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isCustomPickerOpen) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hours Stepper
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ZenithNavyDark)
                                        .clickable {
                                            if (customHours > 0) {
                                                customHours--
                                                selectedDurationMillis = (customHours * 3600000L) + (customMinutes * 60000L).coerceAtLeast(15 * 60000L)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Text(
                                    text = "${customHours}h",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ZenithNavyDark)
                                        .clickable {
                                            if (customHours < 72) {
                                                customHours++
                                                selectedDurationMillis = (customHours * 3600000L) + (customMinutes * 60000L)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }

                            // Minutes Stepper
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ZenithNavyDark)
                                        .clickable {
                                            if (customMinutes >= 5) {
                                                customMinutes -= 5
                                                val total = (customHours * 3600000L) + (customMinutes * 60000L)
                                                selectedDurationMillis = total.coerceAtLeast(15 * 60000L)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Text(
                                    text = "${customMinutes}m",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ZenithNavyDark)
                                        .clickable {
                                            if (customMinutes < 55) {
                                                customMinutes += 5
                                                selectedDurationMillis = (customHours * 3600000L) + (customMinutes * 60000L)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TARGET EXPIRY PREVIEW
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZenithCardDark)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏳ Target End: $targetTimeFormatted",
                            color = ZenithEmeraldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = "CANCEL", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onArmSession(selectedDurationMillis)
                                step = 1
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ZenithEmerald),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = "PROCEED ➔", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }

                } else {
                    // STEP 2: FINAL CONFIRMATION + HOLD TO ACTIVATE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ZenithEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🔒", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FINAL CONFIRMATION",
                            color = ZenithEmeraldAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "NUCLEAR MODE READY",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ZenithNavyDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Duration:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                Text(
                                    text = DateTimeUtils.formatRemaining(selectedDurationMillis),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Ends at:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                Text(
                                    text = targetTimeFormatted,
                                    color = ZenithEmeraldAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "⚠️ Once activated, you CANNOT cancel, edit, or unlock until the timer hits zero. Hold below to confirm your discipline.",
                        color = Color(0xFFFECACA),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // HOLD TO ACTIVATE BUTTON (3 SECONDS)
                    HoldToActivateButton(
                        onConfirmed = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onConfirmActivation()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { step = 0 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "BACK TO DURATION", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HoldToActivateButton(
    onConfirmed: () -> Unit
) {
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }
    val view = LocalView.current

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val totalSteps = 30
            val stepDelay = 100L // 3000ms total
            for (i in 1..totalSteps) {
                if (!isHolding) break
                delay(stepDelay)
                holdProgress = i.toFloat() / totalSteps.toFloat()
                if (i % 10 == 0) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            if (isHolding && holdProgress >= 1f) {
                onConfirmed()
            }
        } else {
            holdProgress = 0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(100),
        label = "HoldProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ZenithBurgundyDeep)
            .border(2.dp, if (isHolding) Color(0xFFF43F5E) else ZenithBurgundy, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isHolding = true
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    isHolding = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Fill Progress Background
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(64.dp)
                .background(Color(0xFFE11D48).copy(alpha = 0.8f))
                .align(Alignment.CenterStart)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isHolding) "KEEP HOLDING... (${(animatedProgress * 100).toInt()}%)" else "HOLD TO ACTIVATE ☢️",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}
