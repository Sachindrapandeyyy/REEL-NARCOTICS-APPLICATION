package com.zenith.focus.feature.protection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.domain.model.AppLockConfig
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.domain.nuclear.NuclearSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProtectionScreen(
    nuclearSession: NuclearSession,
    config: ProtectionConfig,
    appLockConfig: AppLockConfig = AppLockConfig(),
    onNavigateAppLock: () -> Unit = {},
    onToggleCategory: (ContentCategory, Boolean) -> Unit,
    onToggleBrowserProtection: (Boolean) -> Unit,
    onToggleStrictMode: (Boolean) -> Unit
) {
    val earth = EarthTheme.colors
    val scrollState = rememberScrollState()
    val isNuclearActive = nuclearSession.isCurrentlyActive()

    val formattedNuclearEnd = remember(nuclearSession.endTimeMillis) {
        SimpleDateFormat("h:mm a (MMM d)", Locale.getDefault()).format(Date(nuclearSession.endTimeMillis))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(earth.canvas)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "⚡ S++ HARD ENFORCEMENT",
            color = earth.forestGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Content Shield",
            color = earth.forestDark,
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "Zero tolerance for cheap dopamine. The moment a blocked surface is detected, you will be instantly ejected to the Home Screen.",
            color = earth.textMuted,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // NUCLEAR MODE STATUS CARD (IMMUTABLE COMMITMENT)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isNuclearActive) 1.5.dp else 1.dp,
                    color = if (isNuclearActive) earth.camelOchre else earth.border,
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isNuclearActive) "☢️ NUCLEAR MODE: ACTIVE" else "☢️ NUCLEAR MODE COMMITMENT",
                        color = if (isNuclearActive) earth.camelOchre else earth.forestGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    if (isNuclearActive) {
                        Box(
                            modifier = Modifier
                                .background(earth.camelOchre, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LOCKED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Text(
                    text = if (isNuclearActive) {
                        "Nuclear Mode is currently active until $formattedNuclearEnd. All protection feeds are locked ON. Settings cannot be disabled."
                    } else {
                        "Nuclear Mode can only be armed from the Home dashboard with a deliberate Hold-To-Activate gesture. Once active, all cheat paths and disable toggles are completely locked."
                    },
                    color = earth.textPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // APP LOCK & BLOCKER SHIELD CARD (OPAL / APPBLOCK STYLE)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, earth.forestGreen.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                .clickable { onNavigateAppLock() }
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🛡️ APP LOCK SHIELD",
                            color = earth.forestGreen,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (appLockConfig.isAppLockEnabled) earth.forestGreen else earth.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (appLockConfig.isAppLockEnabled) "ACTIVE" else "OFF",
                            color = if (appLockConfig.isAppLockEnabled) Color.White else earth.textMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Installed Apps & Games Blocker",
                    color = earth.forestDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )

                Text(
                    text = "Lock specific apps permanently (24/7) or isolate them during active Nuclear Mode sessions.",
                    color = earth.textMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 3.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (appLockConfig.totalCount == 0) {
                            "0 apps locked"
                        } else {
                            "${appLockConfig.totalCount} locked (${appLockConfig.permanentCount} Perm • ${appLockConfig.nuclearCount} Nuclear)"
                        },
                        color = earth.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Manage Apps →",
                        color = earth.forestGreen,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SHORT-FORM VIDEO TARGETS SECTION
        Text(
            text = "SHORT-FORM ADDICTIVE FEEDS",
            color = earth.forestGreen,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "YouTube Shorts",
            subtitle = "Instantly ejects to Home Screen if Shorts button or reel player is opened",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.YOUTUBE_SHORTS) else config.blockYouTubeShorts,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.YOUTUBE_SHORTS, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Instagram Reels",
            subtitle = "Instantly ejects to Home Screen if Reels tab or clips player is opened",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.INSTAGRAM_REELS) else config.blockInstagramReels,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.INSTAGRAM_REELS, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Facebook Reels",
            subtitle = "Instantly ejects if Facebook watch reels or video scroll feed is detected",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.FACEBOOK_REELS) else config.blockFacebookReels,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.FACEBOOK_REELS, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Snapchat Spotlight",
            subtitle = "Instantly ejects if Spotlight vertical swipe feed is opened",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.SNAPCHAT_SPOTLIGHT) else config.blockSnapchatSpotlight,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.SNAPCHAT_SPOTLIGHT, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "TikTok",
            subtitle = "Instantly ejects when TikTok or TikTok Lite feeds are active",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.TIKTOK) else config.blockTikTok,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.TIKTOK, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Other Short Videos",
            subtitle = "Detects generic vertical swipe feeds and audio clips in other apps",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.OTHER_SHORT_VIDEO) else config.blockOtherShortVideo,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.OTHER_SHORT_VIDEO, it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // EXPLICIT CONTENT SECTION
        Text(
            text = "EXPLICIT CONTENT BARRIER",
            color = earth.forestGreen,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Adult Websites & Domains",
            subtitle = "Real-time browser URL check against comprehensive adult blacklist",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.ADULT_WEBSITE) else config.blockAdultWebsites,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.ADULT_WEBSITE, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Adult Keyword Shield",
            subtitle = "Scans search queries and active web pages for explicit terminology",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.ADULT_KEYWORD) else config.blockAdultKeywords,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.ADULT_KEYWORD, it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ADVANCED ENFORCEMENT
        Text(
            text = "SYSTEM ENFORCEMENT DEPTH",
            color = earth.forestGreen,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Strict Enforcement Mode",
            subtitle = "Prevents fast swipe bypasses and aggressively terminates background tasks",
            isChecked = if (isNuclearActive) true else config.strictMode,
            enabled = !isNuclearActive,
            onCheckedChange = onToggleStrictMode
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Browser Deep URL Inspection",
            subtitle = "Reads real-time browser address bars to catch adult domain redirects",
            isChecked = if (isNuclearActive) true else config.browserProtectionEnabled,
            enabled = !isNuclearActive,
            onCheckedChange = onToggleBrowserProtection
        )

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun ProtectionToggleCard(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val earth = EarthTheme.colors
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, earth.border, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = title,
                    color = earth.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = earth.textMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Switch(
                checked = isChecked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = earth.forestGreen,
                    uncheckedThumbColor = earth.textMuted,
                    uncheckedTrackColor = earth.surface,
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = earth.camelOchre
                )
            )
        }
    }
}
