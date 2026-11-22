package com.zenith.focus.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith.focus.core.designsystem.EarthTheme
import com.zenith.focus.core.designsystem.OrbitalCoral

/**
 * Prominent In-App Accessibility Disclosure Modal.
 *
 * Complies with Google Play & Android OS requirements by providing clear,
 * non-skippable explanation of how and why AccessibilityService is used,
 * ensuring 100% offline privacy and zero telemetry before routing the user to system settings.
 */
@Composable
fun AccessibilityDisclosureDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val earth = EarthTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F2)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.5.dp, earth.border, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔒 PROMINENT ACCESSIBILITY DISCLOSURE",
                    color = OrbitalCoral,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Why Reel Narcotics Needs Accessibility",
                    color = earth.textPrimary,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))

                DisclosureBullet(
                    icon = "🎯",
                    title = "Surgical Short-Form Video Interception",
                    desc = "Reel Narcotics monitors window events to detect when vertical player surfaces (YouTube Shorts, Instagram Reels, Snapchat Spotlight, Facebook Reels) or explicit adult URLs are actively visible, and surgically presses Back to dismiss them."
                )
                Spacer(modifier = Modifier.height(12.dp))

                DisclosureBullet(
                    icon = "🛡️",
                    title = "Anti-Tamper Session Protection",
                    desc = "During active Focus or Nuclear sessions, Accessibility is used to prevent addictive impulse overrides, such as unauthorized force-stopping or putting Reel Narcotics into freezer apps."
                )
                Spacer(modifier = Modifier.height(12.dp))

                DisclosureBullet(
                    icon = "🚫",
                    title = "100% Offline • Zero Data Collection",
                    desc = "Reel Narcotics operates entirely on-device with zero telemetry. It NEVER reads, logs, stores, or transmits your personal messages, photos, passwords, banking info, or keystrokes to any remote server."
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("NOT NOW", color = earth.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrbitalCoral,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                    ) {
                        Text("AGREE & CONTINUE ➔", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisclosureBullet(icon: String, title: String, desc: String) {
    val earth = EarthTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = earth.textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = desc,
                color = earth.textMuted,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
