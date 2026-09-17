package com.zenith.focus.feature.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.R
import com.zenith.focus.core.designsystem.EarthTheme
import com.zenith.focus.core.ui.OemPermissionCard
import com.zenith.focus.core.ui.RestrictedSettingsBanner
import com.zenith.focus.core.ui.RestrictedSettingsGuideDialog
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun OnboardingScreen(
    config: ProtectionConfig,
    isServiceConnected: Boolean,
    isDeviceAdminActive: Boolean = false,
    onToggleCategory: (ContentCategory, Boolean) -> Unit,
    onEnableDeviceAdmin: () -> Unit = {},
    onCompleteOnboarding: () -> Unit
) {
    val earth = EarthTheme.colors
    var currentPage by remember { mutableIntStateOf(0) }
    var showRestrictedDialog by remember { mutableStateOf(false) }
    val totalPages = 3
    val scrollState = rememberScrollState()

    if (showRestrictedDialog) {
        RestrictedSettingsGuideDialog(onDismiss = { showRestrictedDialog = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(earth.canvas)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until totalPages) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == currentPage) 26.dp else 8.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i == currentPage) earth.camelOchre else earth.border)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3D Brand Logo with refined border
            Image(
                painter = painterResource(id = R.drawable.ic_reel_narcotics_logo),
                contentDescription = "Reel Narcotics",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Page Content Container
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (currentPage) {
                    // STEP 1: VALUE PROPOSITION HERO
                    0 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "REEL NARCOTICS • 100% OFFLINE",
                            color = earth.camelOchre,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Break the Scroll.\nTake Back Attention.",
                            color = earth.forestGreen,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Surgical distraction blocker engineered to destroy endless dopamine loops. Keeps long tutorials and chats open, but terminates infinite feeds the instant they appear.",
                            color = earth.textPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Value Pillars
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, earth.border, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                ValuePillarRow("🛡️", "Surgical Precision", "Only kills Shorts & Reels. YouTube tutorials & DMs stay accessible.")
                                Spacer(modifier = Modifier.height(10.dp))
                                ValuePillarRow("🔒", "100% Offline & Private", "Zero tracking, zero cloud accounts, zero ads, and zero telemetry.")
                                Spacer(modifier = Modifier.height(10.dp))
                                ValuePillarRow("⚡", "Sub-Millisecond Response", "Terminates addictive feeds instantly before your dopamine spike triggers.")
                            }
                        }
                    }

                    // STEP 2: SMART SHIELD SETUP
                    1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP 2 OF 3 • SMART PERMISSION",
                            color = earth.camelOchre,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Activate Reel Shield",
                            color = earth.forestGreen,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Used strictly to detect when short-form video players appear so Reel Narcotics can close them. Your chats, photos, and passwords are NEVER collected.",
                            color = earth.textPrimary,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OemPermissionCard(
                            isServiceConnected = isServiceConnected,
                            onOpenGuideDialog = { showRestrictedDialog = true }
                        )
                    }

                    // STEP 3: CUSTOMIZE TARGETS & READY
                    2 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP 3 OF 3 • CUSTOMIZE",
                            color = earth.camelOchre,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose Your Targets",
                            color = earth.forestGreen,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select which addictive feeds Reel Narcotics should intercept:",
                            color = earth.textMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OnboardingToggleRow("YouTube Shorts", config.blockYouTubeShorts) {
                            onToggleCategory(ContentCategory.YOUTUBE_SHORTS, it)
                        }
                        OnboardingToggleRow("Instagram Reels", config.blockInstagramReels) {
                            onToggleCategory(ContentCategory.INSTAGRAM_REELS, it)
                        }
                        OnboardingToggleRow("Snapchat Spotlight", config.blockSnapchatSpotlight) {
                            onToggleCategory(ContentCategory.SNAPCHAT_SPOTLIGHT, it)
                        }
                        OnboardingToggleRow("Adult Websites (Browser)", config.blockAdultWebsites) {
                            onToggleCategory(ContentCategory.ADULT_WEBSITE, it)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        border = BorderStroke(1.dp, earth.border),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("BACK", color = earth.textMuted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }

                Button(
                    onClick = {
                        if (currentPage < totalPages - 1) {
                            currentPage++
                        } else {
                            onCompleteOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPage == totalPages - 1) earth.camelOchre else earth.forestGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = if (currentPage == totalPages - 1) "ENTER FOCUS MODE 🚀" else "NEXT ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ValuePillarRow(icon: String, title: String, description: String) {
    val earth = EarthTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = earth.forestDark,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = earth.textMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun OnboardingPageContent(tag: String, title: String, description: String) {
    val earth = EarthTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = tag,
            color = earth.camelOchre,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = earth.forestGreen,
            fontSize = 26.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = description,
            color = earth.textPrimary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun OnboardingToggleRow(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val earth = EarthTheme.colors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, earth.border, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = earth.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = earth.forestGreen,
                    uncheckedThumbColor = earth.textMuted,
                    uncheckedTrackColor = earth.surface
                )
            )
        }
    }
}
