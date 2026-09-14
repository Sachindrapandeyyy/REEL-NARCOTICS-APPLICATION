package com.zenith.focus.core.ui

import android.os.Build
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.core.permission.OemNavigationManager

/**
 * Android 13+ & Multi-OEM Restricted Settings Helper Banner & Dialog.
 * Helps users bypass "App was denied access" / "Restricted setting" in 3 easy steps.
 */
@Composable
fun RestrictedSettingsBanner(
    modifier: Modifier = Modifier,
    onOpenDialog: () -> Unit
) {
    val earth = EarthTheme.colors
    val guidance = OemNavigationManager.getGuidance()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = earth.surface),
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, earth.camelOchre, RoundedCornerShape(14.dp))
                .clickable { onOpenDialog() }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Phone Setup: ${guidance.brand.displayName}",
                        color = earth.camelOchre,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seeing 'Restricted setting' or denied access? Tap for 5-sec fix ➔",
                        color = earth.textPrimary,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RestrictedSettingsGuideDialog(
    onDismiss: () -> Unit
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current
    val guidance = OemNavigationManager.getGuidance()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = earth.surfaceSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Bypass Restricted Setting",
                            color = earth.forestDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "📱 Detected: ${guidance.brand.displayName} (${guidance.brand.osSkin})",
                            color = earth.camelOchre,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Android 13+ and ${guidance.brand.osSkin} block sideloaded app permissions by default. Follow these steps to unlock:",
                    color = earth.textMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Steps Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = earth.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, earth.border, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StepRow(
                            number = "1",
                            title = guidance.step1Title,
                            subtitle = guidance.step1Desc
                        )
                        StepRow(
                            number = "2",
                            title = guidance.step2Title,
                            subtitle = guidance.step2Desc
                        )
                        if (guidance.step3Title != null && guidance.step3Desc != null) {
                            StepRow(
                                number = "3",
                                title = guidance.step3Title,
                                subtitle = guidance.step3Desc
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onDismiss()
                        OemNavigationManager.openAccessibilitySettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "1. OPEN ACCESSIBILITY SETTINGS ➔",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onDismiss()
                        OemNavigationManager.openAppInfo(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = earth.forestDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "2. TOUCH TO OPEN APP INFO (3-DOTS) ➔",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                if (guidance.step3ButtonLabel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            OemNavigationManager.openOemAutostart(context)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(
                            text = guidance.step3ButtonLabel,
                            color = earth.camelOchre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text("CLOSE", color = earth.textMuted, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StepRow(number: String, title: String, subtitle: String) {
    val earth = EarthTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(earth.forestGreen, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = earth.textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = subtitle, color = earth.textMuted, fontSize = 10.5.sp, lineHeight = 14.sp)
        }
    }
}
