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
            PresetDuration("15 MIN", 15 * 60 * 1000L),
            PresetDuration("25 MIN", 25 * 60 * 1000L),
            PresetDuration("45 MIN", 45 * 60 * 1000L),
            PresetDuration("1 HOUR", 60 * 60 * 1000L),
            PresetDuration("2 HOURS", 2 * 60 * 60 * 1000L),
            PresetDuration("4 HOURS", 4 * 60 * 60 * 1000L),
            PresetDuration("8 HOURS", 8 * 60 * 60 * 1000L),
            PresetDuration("1 DAY", 24 * 60 * 60 * 1000L),
            PresetDuration("3 DAYS", 3 * 24 * 60 * 60 * 1000L),
            PresetDuration("7 DAYS", 7 * 24 * 60 * 60 * 1000L)
        )
    }

    var selectedPreset by remember { mutableStateOf<PresetDuration?>(presets[3]) } // 1 Hour default
    var isCustomMode by remember { mutableStateOf(false) }

    // Granular Custom Timer state
    var customHours by remember { mutableIntStateOf(1) }
    var customMinutes by remember { mutableIntStateOf(30) }

    val calculatedCustomMillis = (customHours * 3600000L) + (customMinutes * 60000L)
    val effectiveDurationMillis = if (isCustomMode) {
        calculatedCustomMillis.coerceAtLeast(15 * 60 * 1000L)
    } else {
        selectedPreset?.durationMillis ?: (60 * 60 * 1000L)
    }

    val targetTime = System.currentTimeMillis() + effectiveDurationMillis
    val targetTimeFormatted = remember(effectiveDurationMillis) {
        val sdf = SimpleDateFormat("h:mm a (MMM d)", Locale.getDefault())
        sdf.format(Date(targetTime))
    }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF131B2E),
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
                    color = Color(0xFF10B981),
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
                        .background(Color(0xFF0F172A))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isCustomMode) Color(0xFF10B981) else Color.Transparent)
                            .clickable { isCustomMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PRESET DURATIONS",
                            color = if (!isCustomMode) Color.White else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCustomMode) Color(0xFF10B981) else Color.Transparent)
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
                        modifier = Modifier.height(180.dp)
                    ) {
                        items(presets) { preset ->
                            val isSelected = selectedPreset == preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B))
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
                    // GRANULAR CUSTOM TIMER (HOURS + MINUTES)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // HOURS STEPPER
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HOURS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B))
                                            .clickable { customHours = (customHours - 1).coerceAtLeast(0) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    }

                                    Text(
                                        text = "${customHours}h",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B))
                                            .clickable { customHours = (customHours + 1).coerceAtMost(72) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // MINUTES STEPPER
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MINUTES", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B))
                                            .clickable {
                                                customMinutes = if (customMinutes <= 0) 55 else customMinutes - 5
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    }

                                    Text(
                                        text = "${customMinutes}m",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B))
                                            .clickable {
                                                customMinutes = (customMinutes + 5) % 60
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Minute Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(15, 30, 45).forEach { mins ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E293B))
                                        .clickable {
                                            customMinutes = mins
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+$mins min", color = Color(0xFF06B6D4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF164E63).copy(alpha = 0.4f))
                        .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏳ Target End: $targetTimeFormatted",
                        color = Color(0xFF06B6D4),
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
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
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

                Spacer(modifier = Modifier.height(18.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("CANCEL", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (isCustomMode) {
                                val label = when {
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("ARM LOCK 🔒", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}