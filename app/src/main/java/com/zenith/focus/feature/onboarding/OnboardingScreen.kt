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
    val totalPages = 9

    if (showRestrictedDialog) {
        RestrictedSettingsGuideDialog(onDismiss = { showRestrictedDialog = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(earth.canvas)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until totalPages) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == currentPage) 22.dp else 6.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i == currentPage) earth.camelOchre else earth.border)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3D Brand Logo with refined border
            Image(
                painter = painterResource(id = R.drawable.ic_reel_narcotics_logo),
                contentDescription = "Reel Narcotics",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, earth.border, RoundedCornerShape(26.dp))
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Page Content Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (currentPage) {
                    0 -> OnboardingPageContent(
                        tag = "REEL NARCOTICS",
                        title = "Break the Scroll",
                        description = "Short-form video feeds are engineered to exploit your brain's dopamine reward loop, turning a 30-second break into 2 lost hours. Reel Narcotics puts you back in control."
                    )
                    1 -> OnboardingPageContent(
                        tag = "STEP 2 OF 9",
                        title = "Surgical Content Shield",
                        description = "Reel Narcotics doesn't lock you out of essential communication. You can still watch long-form tutorials on YouTube and message friends on Instagram. Only addictive infinite scrolling feeds are intercepted."
                    )
                    2 -> OnboardingPageContent(
                        tag = "STEP 3 OF 9",
                        title = "Offline Adult Protection",
                        description = "Maintain digital purity with a multi-layered offline defense. Explicit domains and keywords are intercepted locally in the browser with zero cloud lookup."
                    )
                    3 -> OnboardingPageContent(
                        tag = "STEP 4 OF 9",
                        title = "Unbreakable Focus Lock",
                        description = "Commit to your goals with immutable focus locks and Nuclear Mode. Locks survive app restarts and phone reboots so you can't cheat your future self."
                    )
                    4 -> OnboardingPageContent(
                        tag = "STEP 5 OF 9",
                        title = "100% Offline • Zero Telemetry",
                        description = "Reel Narcotics operates 100% locally on your device. No cloud database, no account, no external tracking, no ads, and zero telemetry. Everything stays strictly inside your phone's physical hardware."
                    )
                    5 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP 6 OF 9 • SHIELD PERMISSION",
                            color = earth.camelOchre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Accessibility Shield",
                            color = earth.forestGreen,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Used strictly to detect when YouTube Shorts & Reels open so Reel Narcotics can immediately close them. 100% offline — your chats, photos, and passwords are NEVER read or collected.",
                            color = earth.textPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OemPermissionCard(
                            isServiceConnected = isServiceConnected,
                            onOpenGuideDialog = { showRestrictedDialog = true }
                        )
                    }
                    6 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP 7 OF 9 • FOCUS INTEGRITY",
                            color = earth.camelOchre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Anti-Uninstall Protection",
                            color = earth.forestGreen,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "During active Nuclear Mode sessions, your subconscious impulse will tempt you to delete the app to resume scrolling. Device Administrator locks the app against uninstallation until the timer ends.",
                            color = earth.textPrimary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, earth.border, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🔒 Unbreakable Focus Commitment:",
                                    color = earth.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• Cannot be uninstalled while Nuclear timer runs\n• Cannot be bypassed via app info or clear data\n• 1-tap direct system prompt (no menu hunting)",
                                    color = earth.textMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onEnableDeviceAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDeviceAdminActive) earth.forestGreen else earth.camelOchre,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (isDeviceAdminActive) "UNINSTALL PROTECTION ACTIVE ✓" else "ACTIVATE UNINSTALL PROTECTION",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (!isDeviceAdminActive) {
                            val guidance = remember { com.zenith.focus.core.permission.OemNavigationManager.getGuidance() }
                            Text(
                                text = guidance.deviceAdminHint ?: "💡 Tap 'Activate' on the system prompt",
                                color = earth.camelOchre,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            RestrictedSettingsBanner(
                                onOpenDialog = { showRestrictedDialog = true }
                            )
                        }
                    }
                    7 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP 8 OF 9",
                            color = earth.camelOchre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose Your Targets",
                            color = earth.forestGreen,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OnboardingToggleRow("YouTube Shorts", config.blockYouTubeShorts) {
                            onToggleCategory(ContentCategory.YOUTUBE_SHORTS, it)
                        }
                        OnboardingToggleRow("Instagram Reels", config.blockInstagramReels) {
                            onToggleCategory(ContentCategory.INSTAGRAM_REELS, it)
                        }
                        OnboardingToggleRow("Snapchat Spotlight", config.blockSnapchatSpotlight) {
                            onToggleCategory(ContentCategory.SNAPCHAT_SPOTLIGHT, it)
                        }
                        OnboardingToggleRow("Adult Websites", config.blockAdultWebsites) {
                            onToggleCategory(ContentCategory.ADULT_WEBSITE, it)
                        }
                    }
                    8 -> OnboardingPageContent(
                        tag = "READY",
                        title = "Take Back Your Attention",
                        description = "Welcome to your digital sanctuary. Reel Narcotics is armed and ready to break the scroll and protect your focus. Tap GET STARTED to enter."
                    )
                }
            }

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
                        shape = RoundedCornerShape(14.dp)
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
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (currentPage == totalPages - 1) "GET STARTED" else "NEXT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
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
