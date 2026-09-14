package com.zenith.focus.core.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.core.permission.DeviceBrand
import com.zenith.focus.core.permission.OemNavigationManager

/**
 * Universal Multi-OEM Smart Permission & App Info Routing Card.
 *
 * Provides clear, phone-tailored steps to bypass Android 13/14/15 Restricted Settings
 * and configure Xiaomi, Samsung, OnePlus, Vivo, and other OEM background permissions.
 */
@Composable
fun OemPermissionCard(
    isServiceConnected: Boolean,
    modifier: Modifier = Modifier,
    onOpenGuideDialog: (() -> Unit)? = null
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current
    val guidance = OemNavigationManager.getGuidance()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isServiceConnected) earth.forestGreen else earth.camelOchre,
                RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Phone Detection Badge
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
                            .background(if (isServiceConnected) earth.forestGreen else earth.camelOchre)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isServiceConnected) "SHIELD ACTIVE & ARMED ✓" else "ONE-TOUCH PERMISSION SETUP",
                        color = if (isServiceConnected) earth.forestGreen else earth.camelOchre,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // OEM Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(earth.surface)
                        .border(1.dp, earth.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "📱 ${guidance.brand.osSkin}",
                        color = earth.textPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detected device details
            Text(
                text = "Detected: ${guidance.brand.displayName}",
                color = earth.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isServiceConnected) {
                // Step 1: Open App Info (Primary Touch Navigation)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = earth.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, earth.camelOchre.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { OemNavigationManager.openAppInfo(context) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("1️⃣", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = guidance.step1Title,
                                color = earth.forestDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = guidance.step1Desc,
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
                                text = "TOUCH HERE TO OPEN APP INFO ➔",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Step 2: Open Accessibility Settings
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = earth.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, earth.border, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("2️⃣", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = guidance.step2Title,
                                color = earth.forestDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = guidance.step2Desc,
                            color = earth.textMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Button(
                            onClick = { OemNavigationManager.openAccessibilitySettings(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text(
                                text = "OPEN ACCESSIBILITY SETTINGS ➔",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Step 3 (Optional OEM background optimization if applicable)
                if (guidance.step3Title != null && guidance.step3ButtonLabel != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = earth.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, earth.border, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (guidance.brand == DeviceBrand.XIAOMI) "⚡" else "🔋", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = guidance.step3Title,
                                    color = earth.forestDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (guidance.step3Desc != null) {
                                Text(
                                    text = guidance.step3Desc,
                                    color = earth.textMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            OutlinedButton(
                                onClick = { OemNavigationManager.openOemAutostart(context) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = guidance.step3ButtonLabel,
                                    color = earth.camelOchre,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (onOpenGuideDialog != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenGuideDialog() },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Need more help? View Step-by-Step Visual Guide ➔",
                            color = earth.camelOchre,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Text(
                    text = "✓ Reel Narcotics Shield is actively running and inspecting short feeds offline.",
                    color = earth.forestGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
