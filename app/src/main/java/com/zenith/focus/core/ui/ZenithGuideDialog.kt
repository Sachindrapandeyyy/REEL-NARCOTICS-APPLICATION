package com.zenith.focus.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith.focus.core.designsystem.EarthTheme

@Composable
fun ZenithGuideDialog(
    onDismiss: () -> Unit
) {
    val earth = EarthTheme.colors
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, earth.border, RoundedCornerShape(24.dp)),
            color = earth.canvas
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "USER GUIDE & REFERENCE",
                            color = earth.forestGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "How Reel Narcotics Works",
                            color = earth.textPrimary,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(earth.surfaceSoft)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = earth.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GuideItemCard(
                        icon = "🛡️",
                        title = "1. Content Shields",
                        tag = "Surgical Blocking",
                        description = "Blocks YouTube Shorts, Instagram Reels, Facebook Reels, and Snapchat Spotlight directly at the accessibility layer. You can still watch long educational videos, reply to DMs, and take phone calls without interruption."
                    )

                    GuideItemCard(
                        icon = "⚡",
                        title = "2. App Lock Shield",
                        tag = "Permanent & Nuclear",
                        description = "Lock distracting whole applications (games, shopping, social apps):\n• 🔒 Permanent (24/7): Continuous protection around the clock.\n• ☢️ Nuclear Only: Locked only when a Nuclear session is active, leaving them accessible during normal work."
                    )

                    GuideItemCard(
                        icon = "☢️",
                        title = "3. Nuclear Mode",
                        tag = "Zero-Cheat Commitment",
                        description = "Armed with a deliberate 3-second hold gesture. Once active, all shield settings, app locks, and deactivation toggles are strictly locked until the timer expires. No early unlocks, no cheat codes."
                    )

                    GuideItemCard(
                        icon = "🌙",
                        title = "4. Bedtime Sleep Shield",
                        tag = "Late-Night Protection",
                        description = "Automatically locks feeds between 11:00 PM and 6:30 AM to prevent late-night doomscrolling in bed. Can be toggled on or off from Settings."
                    )

                    GuideItemCard(
                        icon = "🔐",
                        title = "5. Anti-Uninstall (Device Admin)",
                        tag = "System Guard",
                        description = "Prevents impulsive uninstallation of Reel Narcotics from Android launcher or Play Store. Once activated, Android requires Device Admin authorization before the app can ever be removed."
                    )


                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Understood — Let's Focus",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideItemCard(
    icon: String,
    title: String,
    tag: String,
    description: String
) {
    val earth = EarthTheme.colors
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = earth.textPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = earth.surface,
                    modifier = Modifier.border(1.dp, earth.border, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = tag,
                        color = earth.camelOchre,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = earth.textPrimary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
