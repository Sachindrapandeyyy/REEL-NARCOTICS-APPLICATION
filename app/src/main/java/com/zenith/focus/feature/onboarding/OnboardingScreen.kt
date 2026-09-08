package com.zenith.focus.feature.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.R
import com.zenith.focus.core.designsystem.ZenithEmeraldAccent
import com.zenith.focus.core.designsystem.ZenithNavy
import com.zenith.focus.core.designsystem.ZenithNavyDark
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
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = 9
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenithNavyDark)
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
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until totalPages) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == currentPage) 20.dp else 6.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i == currentPage) ZenithEmeraldAccent else Color(0xFF1E293B))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3D Brand Logo
            Image(
                painter = painterResource(id = R.drawable.ic_reel_narcotics_logo),
                contentDescription = "Reel Narcotics",
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(30.dp))
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Page Content
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
                        description = "Reel Narcotics has NO INTERNET PERMISSION. No cloud database, no account, no external tracking, no ads, and zero telemetry. Everything stays strictly inside your phone's physical hardware."
                    )
                    5 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP 6 OF 9 • SHIELD PERMISSION",
                            color = ZenithEmeraldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Accessibility Shield",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Used strictly to detect when YouTube Shorts & Reels open so Reel Narcotics can immediately close them. 100% offline — your chats, photos, and passwords are NEVER read or collected.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📋 3 Quick Steps in Settings:",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "1. Tap button below -> opens Accessibility\n2. Look for 'Installed apps' or 'Downloaded apps'\n3. Tap 'Reel Narcotics Shield' -> Toggle ON",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isServiceConnected) Color(0xFF064E3B) else ZenithEmeraldAccent
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isServiceConnected) "SHIELD PERMISSION ACTIVE ✓" else "OPEN ACCESSIBILITY SETTINGS",
                                fontWeight = FontWeight.Bold,
                                color = if (isServiceConnected) Color(0xFF6EE7B7) else Color.Black
                            )
                        }
                    }
                    6 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP 7 OF 9 • FOCUS INTEGRITY",
                            color = ZenithEmeraldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Anti-Uninstall Protection",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "During active Nuclear Mode sessions, your subconscious impulse will tempt you to delete the app to resume scrolling. Device Administrator locks the app against uninstallation until the timer ends.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🔒 Unbreakable Focus Commitment:",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• Cannot be uninstalled while Nuclear timer runs\n• Cannot be bypassed via app info or clear data\n• 1-tap direct system prompt (no menu hunting)",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onEnableDeviceAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDeviceAdminActive) Color(0xFF064E3B) else Color(0xFFD97706)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isDeviceAdminActive) "UNINSTALL PROTECTION ACTIVE ✓" else "ACTIVATE UNINSTALL PROTECTION",
                                fontWeight = FontWeight.Bold,
                                color = if (isDeviceAdminActive) Color(0xFF6EE7B7) else Color.White
                            )
                        }
                    }
                    7 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STEP 8 OF 9",
                            color = ZenithEmeraldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose Your Targets",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
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
                    Button(
                        onClick = { currentPage-- },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("BACK", color = Color(0xFF94A3B8))
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
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithEmeraldAccent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (currentPage == totalPages - 1) "GET STARTED" else "NEXT",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(tag: String, title: String, description: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = tag,
            color = ZenithEmeraldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = description,
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun OnboardingToggleRow(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ZenithNavy),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ZenithEmeraldAccent,
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }
    }
}
