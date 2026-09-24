package com.zenith.focus.feature.settings

import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SystemSecurityUpdateGood
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.core.permission.OemNavigationManager
import com.zenith.focus.core.ui.RestrictedSettingsGuideDialog
import com.zenith.focus.core.update.UpdateManager
import com.zenith.focus.core.update.UpdateState
import com.zenith.focus.domain.model.FrictionType
import com.zenith.focus.domain.model.HabitConfig
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.feature.protection.EtherealToggle
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver

@Composable
fun SettingsScreen(
    @Suppress("UNUSED_PARAMETER") config: ProtectionConfig,
    isServiceConnected: Boolean,
    isNuclearActive: Boolean = false,
    @Suppress("UNUSED_PARAMETER") currentTheme: String = "",
    @Suppress("UNUSED_PARAMETER") updateManager: UpdateManager? = null,
    habitConfig: HabitConfig = HabitConfig(),
    onUpdateHabitConfig: (HabitConfig) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") todayTotalBlocks: Int = 0,
    @Suppress("UNUSED_PARAMETER") focusStreakDays: Int = 1,
    onShowUpdateDialog: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSelectFrictionType: (FrictionType) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSetPin: suspend (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onClearPin: suspend () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onSetTheme: (String) -> Unit = {}
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    var showRestrictedDialog by remember { mutableStateOf(false) }
    var isDeviceAdminActive by remember {
        mutableStateOf(ZenithDeviceAdminReceiver.isAdminActive(context))
    }

    if (showRestrictedDialog) {
        RestrictedSettingsGuideDialog(onDismiss = { showRestrictedDialog = false })
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDeviceAdminActive = ZenithDeviceAdminReceiver.isAdminActive(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Warm Ivory Linen & Peach Mist Gradient Canvas
    val canvasBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFAF7F2),
                Color(0xFFFBF1E8),
                Color(0xFFF6EFEB)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            // ====================================================================
            // 1. EDITORIAL HEADER
            // ====================================================================
            Text(
                text = "SYSTEM & PREFERENCES",
                color = OrbitalCoral,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Settings & Guard",
                color = earth.textPrimary,
                fontSize = 28.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Configure hardware defenses, mindful routines, and device armor.",
                color = earth.textMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ====================================================================
            // 2. NUCLEAR MODE STATUS (IF ACTIVE)
            // ====================================================================
            if (isNuclearActive) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, OrbitalCoral.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "☢️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "NUCLEAR LOCK ACTIVE",
                                color = OrbitalCoral,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Preferences & hardware uninstallation barriers are locked until session ends.",
                                color = earth.textPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ====================================================================
            // 3. GROUP 1: SYSTEM HARDWARE ENFORCERS (UNIFIED FROSTED CONTAINER)
            // ====================================================================
            GroupHeader(title = "HARDWARE SHIELDS", subtitle = "Core system bindings")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    // Item 1: Accessibility Shield
                    SettingsActionRow(
                        icon = Icons.Outlined.Shield,
                        iconTint = if (isServiceConnected) OrbitalMint else OrbitalCoral,
                        title = "Accessibility Shield",
                        subtitle = if (isServiceConnected) "Active & protecting dopamine baseline" else "Disabled • Tap to grant permission",
                        badge = if (isServiceConnected) "ACTIVE" else "REQUIRED",
                        badgeColor = if (isServiceConnected) OrbitalMint else OrbitalCoral,
                        onClick = {
                            OemNavigationManager.openAccessibilitySettings(context)
                        }
                    )

                    EtherealDivider()

                    // Item 2: Anti-Uninstall Device Admin
                    SettingsActionRow(
                        icon = Icons.Outlined.AdminPanelSettings,
                        iconTint = if (isDeviceAdminActive) OrbitalMint else OrbitalAmber,
                        title = "Anti-Uninstall Armor",
                        subtitle = if (isDeviceAdminActive) "Active • Prevents bypass uninstallation" else "Device Admin inactive",
                        badge = if (isDeviceAdminActive) "PROTECTED" else "ENABLE",
                        badgeColor = if (isDeviceAdminActive) OrbitalMint else OrbitalAmber,
                        onClick = {
                            ZenithDeviceAdminReceiver.openDeviceAdminActivation(context)
                        }
                    )

                    EtherealDivider()

                    // Item 3: Restricted Settings & Battery Guidance
                    SettingsActionRow(
                        icon = Icons.Outlined.VerifiedUser,
                        iconTint = OrbitalSky,
                        title = "OEM Background Guard",
                        subtitle = "Prevent Android OS from putting protection service to sleep",
                        badge = "GUIDE",
                        badgeColor = OrbitalSky,
                        onClick = {
                            showRestrictedDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 4. GROUP 2: MINDFUL HABIT SCHEDULES (UNIFIED FROSTED CONTAINER)
            // ====================================================================
            GroupHeader(title = "ATTENTION ROUTINES", subtitle = "Automated habit prompts")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    // Item 1: Morning Focus Pledge
                    SettingsToggleRow(
                        icon = Icons.Outlined.LightMode,
                        iconTint = OrbitalAmber,
                        title = "Morning Focus Pledge",
                        subtitle = "8:00 AM daily intention setting before scrolling starts",
                        isChecked = habitConfig.morningPledgeEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateHabitConfig(habitConfig.copy(morningPledgeEnabled = enabled))
                        }
                    )

                    EtherealDivider()

                    // Item 2: Evening Victory Digest
                    SettingsToggleRow(
                        icon = Icons.Outlined.NotificationsActive,
                        iconTint = OrbitalViolet,
                        title = "Evening Summary Digest",
                        subtitle = "9:00 PM summary of kicks deflected and focus streak",
                        isChecked = habitConfig.eveningSummaryEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateHabitConfig(habitConfig.copy(eveningSummaryEnabled = enabled))
                        }
                    )

                    EtherealDivider()

                    // Item 3: Bedtime Sleep Shield
                    SettingsToggleRow(
                        icon = Icons.Outlined.Bedtime,
                        iconTint = OrbitalCoral,
                        title = "Bedtime Sleep Shield",
                        subtitle = "11:00 PM to 6:30 AM unconditional feed lockdown",
                        isChecked = habitConfig.bedtimeShieldEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateHabitConfig(habitConfig.copy(bedtimeShieldEnabled = enabled))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 5. GROUP 3: APPLICATION UPDATES & ENGINE (UNIFIED FROSTED CONTAINER)
            // ====================================================================
            GroupHeader(title = "ENGINE & UPDATES", subtitle = "Release channel")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Reel Narcotics v2.5.0",
                                color = earth.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ethereal Aurora Edition",
                                color = earth.textMuted,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onShowUpdateDialog,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = earth.surfaceVariant),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = "Check for updates",
                                tint = earth.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Update",
                                color = earth.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 6. GROUP 4: PRIVACY & DATA GUARANTEE
            // ====================================================================
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.70f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OrbitalMintSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Safe",
                            tint = OrbitalMint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "100% Offline & Private",
                            color = earth.textPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Zero remote tracking. All app usage, focus sessions, and statistics remain exclusively in your local on-device SQLite database.",
                            color = earth.textMuted,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun GroupHeader(title: String, subtitle: String) {
    val earth = EarthTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Text(
            text = title,
            color = earth.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = earth.textMuted,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    val earth = EarthTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column {
                Text(
                    text = title,
                    color = earth.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = earth.textMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = badge,
                color = badgeColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val earth = EarthTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column {
                Text(
                    text = title,
                    color = earth.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = earth.textMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }

        EtherealToggle(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            activeColor = OrbitalViolet
        )
    }
}

@Composable
private fun EtherealDivider() {
    Divider(
        color = Color(0xFFF0EBE1),
        thickness = 1.dp
    )
}
