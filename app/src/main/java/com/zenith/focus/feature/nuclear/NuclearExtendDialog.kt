package com.zenith.focus.feature.nuclear

import android.os.Build
import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.EarthTheme
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.nuclear.NuclearSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NuclearExtendDialog(
    session: NuclearSession,
    onDismiss: () -> Unit,
    onConfirmExtension: (additionalMillis: Long) -> Unit
) {
    val earth = EarthTheme.colors
    val view = LocalView.current

    val now = System.currentTimeMillis()
    val remainingMillis = session.remainingMillis(now)
    val currentEndTime = session.endTimeMillis

    var additionalDays by remember { mutableIntStateOf(0) }
    var additionalHours by remember { mutableIntStateOf(1) }
    var additionalMinutes by remember { mutableIntStateOf(0) }
    var isCustomPickerOpen by remember { mutableStateOf(false) }

    // Quick selections: 1h, 6h, 1d, 3d, 7d
    var selectedPresetMillis by remember { mutableLongStateOf(1 * 60 * 60 * 1000L) }

    val calculatedAdditionalMillis = if (isCustomPickerOpen) {
        (additionalDays * 24L * 60L * 60L * 1000L) +
        (additionalHours * 60L * 60L * 1000L) +
        (additionalMinutes * 60L * 1000L)
    } else {
        selectedPresetMillis
    }

    val newEndTime = currentEndTime + calculatedAdditionalMillis
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = earth.canvasElevated,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, earth.border, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(earth.camelOchre.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "☢️ NUCLEAR MODE ACTIVE",
                        color = earth.camelOchre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Extend Focus Commitment",
                    color = earth.forestGreen,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You cannot reduce or cancel an active Nuclear lock. However, you can add more time to strengthen your discipline.",
                    color = earth.textMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Lock Status Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, earth.border, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current End Time:",
                                color = earth.textMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = DateTimeUtils.formatRemainingShort(remainingMillis) + " left",
                                color = earth.camelOchre,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = dateFormat.format(Date(currentEndTime)),
                            color = earth.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "SELECT EXTENSION TIME",
                    color = earth.textMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets Grid
                val presets = listOf(
                    1 * 60 * 60 * 1000L to "+1 Hour",
                    6 * 60 * 60 * 1000L to "+6 Hours",
                    24 * 60 * 60 * 1000L to "+1 Day",
                    3 * 24 * 60 * 60 * 1000L to "+3 Days",
                    7 * 24 * 60 * 60 * 1000L to "+7 Days"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    presets.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (millis, label) ->
                                val isSelected = !isCustomPickerOpen && selectedPresetMillis == millis
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) earth.camelOchre else earth.surface
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) earth.camelOchre else earth.border,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            isCustomPickerOpen = false
                                            selectedPresetMillis = millis
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else earth.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                            if (row.size < 3) {
                                for (k in 0 until (3 - row.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Custom Option Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCustomPickerOpen) earth.camelOchre else earth.surface)
                            .border(
                                1.dp,
                                if (isCustomPickerOpen) earth.camelOchre else earth.border,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                isCustomPickerOpen = true
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Custom Duration (Days / Hours / Mins)",
                            color = if (isCustomPickerOpen) Color.White else earth.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isCustomPickerOpen) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                // Custom Steppers if opened
                if (isCustomPickerOpen) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, earth.border, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            StepperRow(
                                label = "Add Days",
                                value = additionalDays,
                                onDecrement = { if (additionalDays > 0) additionalDays-- },
                                onIncrement = { if (additionalDays < 90) additionalDays++ }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            StepperRow(
                                label = "Add Hours",
                                value = additionalHours,
                                onDecrement = { if (additionalHours > 0) additionalHours-- },
                                onIncrement = { if (additionalHours < 23) additionalHours++ }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            StepperRow(
                                label = "Add Minutes",
                                value = additionalMinutes,
                                onDecrement = { if (additionalMinutes >= 5) additionalMinutes -= 5 },
                                onIncrement = { if (additionalMinutes < 55) additionalMinutes += 5 }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // New Projected End Time Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = earth.forestGreen.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, earth.forestGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Projected New Lock End:",
                            color = earth.forestGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormat.format(Date(newEndTime)),
                            color = earth.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "(+${DateTimeUtils.formatRemainingShort(calculatedAdditionalMillis)} added to active focus lock)",
                            color = earth.textMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        if (calculatedAdditionalMillis > 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            } else {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                            onConfirmExtension(calculatedAdditionalMillis)
                        }
                    },
                    enabled = calculatedAdditionalMillis > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = earth.camelOchre,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "CONFIRM EXTENSION (+${DateTimeUtils.formatRemainingShort(calculatedAdditionalMillis)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        color = earth.textMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    val earth = EarthTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = earth.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(earth.surface)
                    .border(1.dp, earth.border, CircleShape)
                    .clickable { onDecrement() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "-", color = earth.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(
                text = "$value",
                color = earth.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(earth.surface)
                    .border(1.dp, earth.border, CircleShape)
                    .clickable { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = earth.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
