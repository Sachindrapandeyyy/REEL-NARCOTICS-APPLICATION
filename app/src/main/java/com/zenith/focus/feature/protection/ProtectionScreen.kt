package com.zenith.focus.feature.protection

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.ZenithBurgundy
import com.zenith.focus.core.designsystem.ZenithBurgundyDeep
import com.zenith.focus.core.designsystem.ZenithEmeraldAccent
import com.zenith.focus.core.designsystem.ZenithNavy
import com.zenith.focus.core.designsystem.ZenithNavyDark
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
    onToggleCategory: (ContentCategory, Boolean) -> Unit,
    onToggleBrowserProtection: (Boolean) -> Unit,
    onToggleStrictMode: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    val isNuclearActive = nuclearSession.isCurrentlyActive()

    val formattedNuclearEnd = remember(nuclearSession.endTimeMillis) {
        SimpleDateFormat("h:mm a (MMM d)", Locale.getDefault()).format(Date(nuclearSession.endTimeMillis))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenithNavyDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "⚡ S++ HARD ENFORCEMENT",
            color = Color(0xFFF43F5E),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Content Shield",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Zero tolerance for cheap dopamine. The moment a blocked surface is detected, you will be instantly ejected to the Home Screen.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // NUCLEAR MODE STATUS CARD (IMMUTABLE COMMITMENT)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNuclearActive) ZenithBurgundyDeep else ZenithNavy
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isNuclearActive) 2.dp else 1.dp,
                    color = if (isNuclearActive) ZenithBurgundy else Color(0xFF334155),
                    shape = RoundedCornerShape(16.dp)
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
                        color = if (isNuclearActive) Color(0xFFFECACA) else Color(0xFFF43F5E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    if (isNuclearActive) {
                        Box(
                            modifier = Modifier
                                .background(ZenithBurgundy, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
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
                    color = if (isNuclearActive) Color(0xFFE2E8F0) else Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SHORT-FORM VIDEO TARGETS SECTION
        Text(
            text = "SHORT-FORM ADDICTIVE FEEDS",
            color = ZenithEmeraldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

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
            title = "Snapchat Spotlight",
            subtitle = "Instantly ejects if Spotlight vertical swipe feed is opened",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.SNAPCHAT_SPOTLIGHT) else config.blockSnapchatSpotlight,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.SNAPCHAT_SPOTLIGHT, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Facebook Reels",
            subtitle = "Instantly ejects if Facebook Reels player or story reel is opened",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.FACEBOOK_REELS) else config.blockFacebookReels,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.FACEBOOK_REELS, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "TikTok App",
            subtitle = "Completely blocks TikTok app launch & swipe feed",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.TIKTOK) else config.blockTikTok,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.TIKTOK, it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ADULT & EXPLICIT PROTECTION
        Text(
            text = "ADULT & EXPLICIT CONTENT",
            color = com.zenith.focus.core.designsystem.SpiderRedAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        ProtectionToggleCard(
            title = "Adult Web Domains",
            subtitle = "Blocks adult websites across Chrome, Firefox, Brave, Samsung Internet & Edge",
            isChecked = if (isNuclearActive) nuclearSession.enabledCategories.contains(ContentCategory.ADULT_WEBSITE) else config.blockAdultWebsites,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.ADULT_WEBSITE, it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ProtectionToggleCard(
            title = "Explicit Keywords",
            subtitle = "Detects and blocks explicit search queries & provocative tags",
            isChecked = if (isNuclearActive) true else config.blockAdultKeywords,
            enabled = !isNuclearActive,
            onCheckedChange = { onToggleCategory(ContentCategory.ADULT_KEYWORD, it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ADVANCED STRICT SYSTEM PROTECTION
        Text(
            text = "ADVANCED HARDENING",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenithNavy),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
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
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
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
                    checkedTrackColor = ZenithEmeraldAccent,
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFF1E293B),
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = ZenithBurgundy
                )
            )
        }
    }
}
