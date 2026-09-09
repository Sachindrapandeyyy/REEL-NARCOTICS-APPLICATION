package com.zenith.focus.feature.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.SpiderBlue
import com.zenith.focus.core.designsystem.SpiderBlueAccent
import com.zenith.focus.core.designsystem.SpiderBorderDark
import com.zenith.focus.core.designsystem.SpiderCardDark
import com.zenith.focus.core.designsystem.SpiderRed
import com.zenith.focus.core.designsystem.SpiderRedAccent
import com.zenith.focus.core.designsystem.SpiderSurfaceDark
import com.zenith.focus.domain.model.LockMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PresetDuration(
    val label: String,
    val durationMillis: Long,
    val mode: LockMode = LockMode.QUICK
)

@Composable
fun LockSetupDialog(
    onDismiss: () -> Unit,
    onStartLock: (durationMillis: Long, mode: LockMode, label: String) -> Unit,
    onStartUntilTomorrow: () -> Unit
) {
    val presets = remember {
        listOf(
            PresetDuration("5 MIN", 5 * 60 * 1000L),
            PresetDuration("15 MIN", 15 * 60 * 1000L),
            PresetDuration("25 MIN", 25 * 60 * 1000L),
            PresetDuration("45 MIN", 45 * 60 * 1000L),
            PresetDuration("1 HOUR", 60 * 60 * 1000L),
            PresetDuration("2 HOURS", 2 * 60 * 60 * 1000L),
            PresetDuration("4 HOURS", 4 * 60 * 60 * 1000L),
            PresetDuration("1 DAY", 24 * 60 * 60 * 1000L),
            PresetDuration("7 DAYS", 7 * 24 * 60 * 60 * 1000L),
            PresetDuration("30 DAYS (1 MO)", 30 * 24 * 60 * 60 * 1000L),
            PresetDuration("90 DAYS (3 MO)", 90 * 24 * 60 * 60 * 1000L)
        )
    }

    var selectedPreset by remember { mutableStateOf<PresetDuration?>(presets[4]) } // 1 Hour default
    var isCustomMode by remember { mutableStateOf(false) }

    // Granular Custom Timer state: Days, Hours, Minutes (Supports up to 90 Days / 3 Months)
    var customDays by remember { mutableIntStateOf(0) }
    var customHours by remember { mutableIntStateOf(0) }
    var customMinutes by remember { mutableIntStateOf(25) }

    val calculatedCustomMillis = (customDays * 86400000L) + (customHours * 3600000L) + (customMinutes * 60000L)
    // BUG FIX: Strictly allow 1-minute and 5-minute custom timers, no hard 15m coercion!
    val effectiveDurationMillis = if (isCustomMode) {
        calculatedCustomMillis.coerceAtLeast(1 * 60 * 1000L)
    } else {
        selectedPreset?.durationMillis ?: (60 * 60 * 1000L)
    }

    val targetTime = System.currentTimeMillis() + effectiveDurationMillis
    val targetTimeFormatted = remember(effectiveDurationMillis) {
        val sdf = SimpleDateFormat("h:mm a (MMM d, yyyy)", Locale.getDefault())
        sdf.format(Date(targetTime))
    }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SpiderSurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(22.dp)
            ) {
                Text(
                    text = "🔒 ARM FOCUS LOCK",
                    color = SpiderBlueAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Lock In Your Focus",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Shorts & Reels will be instantly ejected. You retain full access to essential calls, messages & tools.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // MODE TOGGLE: Presets vs Custom Timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SpiderCardDark)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isCustomMode) SpiderBlue else Color.Transparent)
                            .clickable { isCustomMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PRESETS",
                            color = if (!isCustomMode) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCustomMode) SpiderBlue else Color.Transparent)
                            .clickable { isCustomMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CUSTOM TIMER ⏱️",
                            color = if (isCustomMode) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isCustomMode) {
                    // PRESETS GRID
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(presets) { preset ->
                            val isSelected = selectedPreset == preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SpiderBlue else SpiderCardDark)
                                    .border(1.dp, if (isSelected) SpiderBlueAccent else SpiderBorderDark, RoundedCornerShape(12.dp))
                                    .clickable { selectedPreset = preset }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.label,
                                    color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } else {
                    // EXTENDED CUSTOM TIMER (DAYS + HOURS + MINUTES, UP TO 3 MONTHS)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(SpiderCardDark)
                            .border(1.dp, SpiderBorderDark, RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // DAYS STEPPER (0 to 90 Days / 3 Months)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DAYS", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(SpiderSurfaceDark)
                                            .clickable { customDays = (customDays - 1).coerceAtLeast(0) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }

                                    Text(
                                        text = "${customDays}d",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(SpiderSurfaceDark)
                                            .clickable { customDays = (customDays + 1).coerceAtMost(90) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // HOURS STEPPER (0 to 23 Hours)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HOURS", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(SpiderSurfaceDark)
                                            .clickable { customHours = (customHours - 1).coerceAtLeast(0) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }

                                    Text(
                                        text = "${customHours}h",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(SpiderSurfaceDark)
                                            .clickable { customHours = (customHours + 1).coerceAtMost(23) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // MINUTES STEPPER (0 to 55 Minutes)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MINUTES", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(SpiderSurfaceDark)
                                            .clickable {
                                                customMinutes = if (customMinutes <= 0) 55 else customMinutes - 5
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }

                                    Text(
                                        text = "${customMinutes}m",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(SpiderSurfaceDark)
                                            .clickable {
                                                customMinutes = (customMinutes + 5) % 60
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Minute Chips: Includes +5 min for instant 5-minute setup!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(5, 15, 30, 45).forEach { mins ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SpiderSurfaceDark)
                                        .border(1.dp, SpiderBorderDark, RoundedCornerShape(8.dp))
                                        .clickable {
                                            customDays = 0
                                            customHours = 0
                                            customMinutes = mins
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${mins}m", color = SpiderBlueAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TARGET EXPIRY PREVIEW PILL
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpiderCardDark)
                        .border(1.dp, SpiderBorderDark, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏳ Target End: $targetTimeFormatted",
                        color = SpiderBlueAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SLEEP/OVERNIGHT BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpiderCardDark)
                        .border(1.dp, SpiderBorderDark, RoundedCornerShape(12.dp))
                        .clickable {
                            onStartUntilTomorrow()
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌙 LOCK UNTIL 4:00 AM (TOMORROW)",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpiderCardDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (isCustomMode) {
                                val label = when {
                                    customDays > 0 -> "${customDays}d ${customHours}h Lock"
                                    customHours > 0 && customMinutes > 0 -> "${customHours}h ${customMinutes}m Lock"
                                    customHours > 0 -> "${customHours}h Lock"
                                    else -> "${customMinutes}m Lock"
                                }
                                onStartLock(effectiveDurationMillis, LockMode.CUSTOM, label)
                            } else {
                                selectedPreset?.let {
                                    onStartLock(it.durationMillis, it.mode, it.label)
                                }
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpiderBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ARM LOCK 🔒", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}