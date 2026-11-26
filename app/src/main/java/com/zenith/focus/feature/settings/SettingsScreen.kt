package com.zenith.focus.feature.settings

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SystemSecurityUpdateGood
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    config: ProtectionConfig,
    isServiceConnected: Boolean,
    isNuclearActive: Boolean = false,
    isLockActive: Boolean = false,
    updateManager: UpdateManager? = null,
    habitConfig: HabitConfig = HabitConfig(),
    onUpdateHabitConfig: (HabitConfig) -> Unit = {},
    onUpdateProtectionConfig: (ProtectionConfig) -> Unit = {},
    onOpenPhysicalAnchorDialog: () -> Unit = {},
    todayTotalBlocks: Int = 0,
    focusStreakDays: Int = 1,
    onShowUpdateDialog: () -> Unit = {},
    onSelectFrictionType: (FrictionType) -> Unit = {},
    onSetPin: suspend (String) -> Unit = {},
    onClearPin: suspend () -> Unit = {},
    onEnableAccessibility: (() -> Unit)? = null
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val handleOpenAccessibility: () -> Unit = onEnableAccessibility ?: {
        OemNavigationManager.openAccessibilitySettings(context)
        Unit
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()
    val isSessionLocked = isNuclearActive || isLockActive
    val updateState = updateManager?.state?.collectAsState()?.value

    var showRestrictedDialog by remember { mutableStateOf(false) }
    var showFrictionPickerDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showPhraseEditorDialog by remember { mutableStateOf(false) }
    var showRoutineScheduleDialog by remember { mutableStateOf(false) }
    var isDeviceAdminActive by remember {
        mutableStateOf(ZenithDeviceAdminReceiver.isAdminActive(context))
    }
    var isSecureSettingsGranted by remember {
        mutableStateOf(com.zenith.focus.feature.sensory.GrayscaleController.isPermissionGranted(context))
    }
    var isDeviceOwner by remember {
        mutableStateOf(com.zenith.focus.core.security.DeviceOwnerManager.isDeviceOwner(context))
    }

    if (showRestrictedDialog) {
        RestrictedSettingsGuideDialog(onDismiss = { showRestrictedDialog = false })
    }

    DisposableEffect(lifecycleOwner, isNuclearActive) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDeviceAdminActive = ZenithDeviceAdminReceiver.isAdminActive(context)
                isSecureSettingsGranted = com.zenith.focus.feature.sensory.GrayscaleController.isPermissionGranted(context)
                isDeviceOwner = com.zenith.focus.core.security.DeviceOwnerManager.isDeviceOwner(context)
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
                        onClick = handleOpenAccessibility
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
                    val morningStr = formatRoutineClock(habitConfig.morningPledgeHour, habitConfig.morningPledgeMinute)
                    SettingsToggleRow(
                        icon = Icons.Outlined.LightMode,
                        iconTint = OrbitalAmber,
                        title = "Morning Focus Pledge",
                        subtitle = "$morningStr daily intention setting before scrolling starts",
                        isChecked = habitConfig.morningPledgeEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateHabitConfig(habitConfig.copy(morningPledgeEnabled = enabled))
                        }
                    )

                    EtherealDivider()

                    // Item 2: Evening Victory Digest
                    val eveningStr = formatRoutineClock(habitConfig.eveningSummaryHour, habitConfig.eveningSummaryMinute)
                    SettingsToggleRow(
                        icon = Icons.Outlined.NotificationsActive,
                        iconTint = OrbitalViolet,
                        title = "Evening Summary Digest",
                        subtitle = "$eveningStr summary of kicks deflected and focus streak",
                        isChecked = habitConfig.eveningSummaryEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateHabitConfig(habitConfig.copy(eveningSummaryEnabled = enabled))
                        }
                    )

                    EtherealDivider()

                    // Item 3: Bedtime Sleep Shield
                    val bedtimeStartStr = formatRoutineClock(habitConfig.bedtimeStartHour, habitConfig.bedtimeStartMinute)
                    val bedtimeEndStr = formatRoutineClock(habitConfig.bedtimeEndHour, habitConfig.bedtimeEndMinute)
                    SettingsToggleRow(
                        icon = Icons.Outlined.Bedtime,
                        iconTint = OrbitalCoral,
                        title = "Bedtime Sleep Shield",
                        subtitle = "$bedtimeStartStr to $bedtimeEndStr unconditional feed lockdown",
                        isChecked = habitConfig.bedtimeShieldEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled && isNuclearActive) {
                                Toast.makeText(context, "Locked while Nuclear Mode is active.", Toast.LENGTH_SHORT).show()
                            } else {
                                onUpdateHabitConfig(habitConfig.copy(bedtimeShieldEnabled = enabled))
                            }
                        }
                    )

                    EtherealDivider()

                    SettingsActionRow(
                        icon = Icons.Outlined.Edit,
                        iconTint = OrbitalSky,
                        title = "Customize Routine Times",
                        subtitle = "$morningStr • $eveningStr • $bedtimeStartStr–$bedtimeEndStr",
                        badge = if (isNuclearActive) "LOCKED" else "EDIT",
                        badgeColor = if (isNuclearActive) OrbitalAmber else OrbitalSky,
                        onClick = {
                            if (isNuclearActive) {
                                Toast.makeText(context, "Routine schedules locked during active Nuclear Mode.", Toast.LENGTH_SHORT).show()
                            } else {
                                showRoutineScheduleDialog = true
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // PSYCHOLOGICAL ARMOR (ANTI-IMPULSE ENGINES)
            // ====================================================================
            GroupHeader(title = "PSYCHOLOGICAL ARMOR", subtitle = "Impulse, time blindness & doomscroll guards")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    // Item 1: 5-Second Mindful Breathing Pause
                    SettingsToggleRow(
                        icon = Icons.Outlined.SelfImprovement,
                        iconTint = OrbitalSky,
                        title = "Mindful Breathing Pause",
                        subtitle = "5-second breathing pause to break muscle-memory app opening",
                        isChecked = config.enableMindfulPause,
                        onCheckedChange = { enabled ->
                            onUpdateProtectionConfig(config.copy(enableMindfulPause = enabled))
                        }
                    )

                    EtherealDivider()

                    // Item 2: Stealth Visual-Hide Cover
                    SettingsToggleRow(
                        icon = Icons.Outlined.VisibilityOff,
                        iconTint = OrbitalViolet,
                        title = "Stealth Button Cover",
                        subtitle = "Masks bright Shorts/Reels tabs with app-matching color & subtle dot",
                        isChecked = config.stealthButtonCover,
                        onCheckedChange = { enabled ->
                            onUpdateProtectionConfig(config.copy(stealthButtonCover = enabled))
                        }
                    )

                    EtherealDivider()

                    // Item 3: Doomscroll Velocity Guard
                    SettingsToggleRow(
                        icon = Icons.Outlined.Speed,
                        iconTint = OrbitalCoral,
                        title = "Doomscroll Velocity Guard",
                        subtitle = "Interception alert when 18+ rapid scrolls are detected in 60s",
                        isChecked = config.enableDoomscrollGuard,
                        onCheckedChange = { enabled ->
                            onUpdateProtectionConfig(config.copy(enableDoomscrollGuard = enabled))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // PHYSICAL & SENSORY ARMOR (NFC / QR ANCHOR & AUTO-GRAYSCALE)
            // ====================================================================
            GroupHeader(title = "PHYSICAL & SENSORY ARMOR", subtitle = "Hardware NFC/QR keys & dopamine-free display")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    // Item 1: Auto-Grayscale Monochrome Mode
                    SettingsToggleRow(
                        icon = Icons.Outlined.ColorLens,
                        iconTint = if (isSecureSettingsGranted) OrbitalMint else OrbitalAmber,
                        title = "Auto-Grayscale (Monochrome)",
                        subtitle = if (isSecureSettingsGranted)
                            "Monochrome screen during Nuclear Mode to kill color dopamine"
                        else
                            "Requires 1-time ADB permission • Tap command below to copy",
                        isChecked = config.enableAutoGrayscale,
                        onCheckedChange = { enabled ->
                            if (!enabled && isNuclearActive) {
                                Toast.makeText(context, "Auto-Grayscale cannot be disabled during active Nuclear Mode.", Toast.LENGTH_SHORT).show()
                            } else {
                                if (enabled && !isSecureSettingsGranted) {
                                    Toast.makeText(context, "Grant WRITE_SECURE_SETTINGS via ADB first!", Toast.LENGTH_LONG).show()
                                }
                                if (enabled && isNuclearActive && isSecureSettingsGranted) {
                                    com.zenith.focus.feature.sensory.GrayscaleController.applyGrayscaleIfEnabled(context, true)
                                }
                                onUpdateProtectionConfig(config.copy(enableAutoGrayscale = enabled))
                            }
                        }
                    )

                    if (!isSecureSettingsGranted) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF3E0))
                                .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(10.dp))
                                .clickable {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(com.zenith.focus.feature.sensory.GrayscaleController.ADB_GRANT_COMMAND))
                                    android.widget.Toast.makeText(context, "ADB grant command copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⚠️ ADB SETUP REQUIRED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "(Tap to Copy)", fontSize = 10.sp, color = Color(0xFFE65100))
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = com.zenith.focus.feature.sensory.GrayscaleController.ADB_GRANT_COMMAND,
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = Color(0xFFBF360C)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    EtherealDivider()

                    // Item 2: Physical Anchor Lock (NFC / QR)
                    val hasAnchorLinked = config.nfcAnchorHash.isNotBlank() || config.qrAnchorHash.isNotBlank()
                    SettingsActionRow(
                        icon = Icons.Outlined.Nfc,
                        iconTint = if (config.enablePhysicalAnchor && hasAnchorLinked) OrbitalMint else OrbitalSky,
                        title = "Physical Anchor Key (NFC / QR)",
                        subtitle = if (hasAnchorLinked)
                            "Active • Tap card or scan QR to unlock early"
                        else
                            "Free DIY Brick alternative • Tap to link card or QR",
                        badge = if (config.enablePhysicalAnchor && hasAnchorLinked) "ACTIVE" else "CONFIGURE",
                        badgeColor = if (config.enablePhysicalAnchor && hasAnchorLinked) OrbitalMint else OrbitalSky,
                        onClick = onOpenPhysicalAnchorDialog
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // UNLOCK FRICTION & PIN CHALLENGE CONFIGURATION
            // ====================================================================
            GroupHeader(title = "UNLOCK FRICTION & PIN", subtitle = "Challenge required before unlocking Focus Lock early")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                    val frictionTitle = when (config.frictionType) {
                        FrictionType.HOLD_BUTTON -> "5-Second Conscious Hold"
                        FrictionType.TYPE_PHRASE -> "Type Pledge Phrase"
                        FrictionType.MATH_TASK -> "Mental Math Challenge"
                        FrictionType.PIN_CODE -> "Secret 4–8 Digit PIN"
                    }
                    SettingsActionRow(
                        icon = Icons.Outlined.Lock,
                        iconTint = OrbitalViolet,
                        title = "Unlock Method",
                        subtitle = frictionTitle,
                        badge = if (isSessionLocked) "LOCKED" else "CHANGE",
                        badgeColor = if (isSessionLocked) OrbitalAmber else OrbitalViolet,
                        onClick = {
                            if (isSessionLocked) {
                                Toast.makeText(context, "Unlock friction cannot be changed while a session is active.", Toast.LENGTH_SHORT).show()
                            } else {
                                showFrictionPickerDialog = true
                            }
                        }
                    )

                    EtherealDivider()

                    val hasPin = !config.pinHash.isNullOrBlank()
                    SettingsActionRow(
                        icon = Icons.Outlined.Security,
                        iconTint = if (hasPin) OrbitalMint else OrbitalSky,
                        title = "Focus PIN Code",
                        subtitle = if (hasPin) "4–8 digit PIN active" else "Optional PIN required for PIN_CODE mode",
                        badge = if (hasPin) "MANAGE" else "SET PIN",
                        badgeColor = if (hasPin) OrbitalMint else OrbitalSky,
                        onClick = { showPinSetupDialog = true }
                    )

                    EtherealDivider()

                    SettingsActionRow(
                        icon = Icons.Outlined.Edit,
                        iconTint = OrbitalCoral,
                        title = "Pledge Unlock Phrase",
                        subtitle = "\"${config.unlockPhrase}\"",
                        badge = if (isSessionLocked) "LOCKED" else "EDIT",
                        badgeColor = if (isSessionLocked) OrbitalAmber else OrbitalCoral,
                        onClick = {
                            if (isSessionLocked) {
                                Toast.makeText(context, "Unlock phrase cannot be edited while locked.", Toast.LENGTH_SHORT).show()
                            } else {
                                showPhraseEditorDialog = true
                            }
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
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Reel Narcotics v${com.zenith.focus.BuildConfig.VERSION_NAME}",
                                color = earth.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (updateState) {
                                    is UpdateState.Available -> "Update v${updateState.manifest.versionName} ready • ${focusStreakDays}d streak"
                                    else -> "Ethereal Aurora • $todayTotalBlocks kicks today • ${focusStreakDays}d streak"
                                },
                                color = if (updateState is UpdateState.Available) OrbitalCoral else earth.textMuted,
                                fontSize = 12.sp
                            )
                        }

                        if (com.zenith.focus.BuildConfig.ENABLE_SELF_UPDATER) {
                            Button(
                                onClick = onShowUpdateDialog,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (updateState is UpdateState.Available) OrbitalCoral else earth.surfaceVariant
                                ),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Sync,
                                    contentDescription = "Check for updates",
                                    tint = if (updateState is UpdateState.Available) Color.White else earth.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (updateState is UpdateState.Available) "Install" else "Update",
                                    color = if (updateState is UpdateState.Available) Color.White else earth.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // GOD MODE (ADB DEVICE OWNER) CARD
            // ====================================================================
            GroupHeader(title = "GOD MODE (ADB)", subtitle = "Ultimate tamper-proofing")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isDeviceOwner) earth.forestGreen else earth.border,
                        RoundedCornerShape(22.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isDeviceOwner) "⚡" else "🛡️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Owner Status",
                                color = earth.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDeviceOwner) earth.forestGreen.copy(alpha = 0.15f) else earth.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDeviceOwner) "Active (God Mode Armed)" else "Not Enabled",
                                color = if (isDeviceOwner) earth.forestGreen else earth.textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isDeviceOwner) {
                            "Device Owner privileges active. Uninstallation, application controls, and safe boot are natively blocked during Nuclear Sessions."
                        } else {
                            "Connect your phone to a PC and run the ADB command below to grant Device Owner privileges for zero-bypass focus."
                        },
                        color = earth.textMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ADB Command Box with Copy button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF2EEE9))
                            .clickable {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(com.zenith.focus.core.security.DeviceOwnerManager.ADB_PROVISION_COMMAND))
                                android.widget.Toast.makeText(context, "Copied ADB command to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = com.zenith.focus.core.security.DeviceOwnerManager.ADB_PROVISION_COMMAND,
                                color = Color(0xFF333333),
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = "COPY",
                                color = OrbitalCoral,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isDeviceOwner) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                if (isNuclearActive) {
                                    android.widget.Toast.makeText(context, "Cannot deactivate God Mode during an active lock!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    val success = com.zenith.focus.core.security.DeviceOwnerManager.deactivateDeviceOwner(context)
                                    if (success) {
                                        isDeviceOwner = false
                                        android.widget.Toast.makeText(context, "Device Owner deactivated", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isNuclearActive,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text(
                                text = if (isNuclearActive) "LOCKED DURING NUCLEAR MODE" else "DEACTIVATE GOD MODE",
                                fontSize = 11.sp,
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

    if (showFrictionPickerDialog) {
        FrictionMethodPickerDialog(
            currentType = config.frictionType,
            hasPinConfigured = !config.pinHash.isNullOrBlank(),
            onDismiss = { showFrictionPickerDialog = false },
            onSelect = { selected ->
                showFrictionPickerDialog = false
                if (selected == FrictionType.PIN_CODE && config.pinHash.isNullOrBlank()) {
                    Toast.makeText(context, "Create a 4–8 digit PIN to enable PIN Lock mode.", Toast.LENGTH_SHORT).show()
                    showPinSetupDialog = true
                } else {
                    onSelectFrictionType(selected)
                }
            }
        )
    }

    if (showPinSetupDialog) {
        PinSetupModalDialog(
            hasExistingPin = !config.pinHash.isNullOrBlank(),
            canClearPin = !isSessionLocked,
            onDismiss = { showPinSetupDialog = false },
            onSavePin = { pin ->
                coroutineScope.launch {
                    onSetPin(pin)
                    if (!isSessionLocked) {
                        onSelectFrictionType(FrictionType.PIN_CODE)
                    }
                    showPinSetupDialog = false
                    Toast.makeText(context, "Focus PIN saved & active.", Toast.LENGTH_SHORT).show()
                }
            },
            onRemovePin = {
                coroutineScope.launch {
                    onClearPin()
                    if (config.frictionType == FrictionType.PIN_CODE) {
                        onSelectFrictionType(FrictionType.HOLD_BUTTON)
                    }
                    showPinSetupDialog = false
                    Toast.makeText(context, "Focus PIN removed.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showPhraseEditorDialog) {
        UnlockPhraseEditorDialog(
            currentPhrase = config.unlockPhrase,
            onDismiss = { showPhraseEditorDialog = false },
            onSavePhrase = { newPhrase ->
                onUpdateProtectionConfig(config.copy(unlockPhrase = newPhrase))
                showPhraseEditorDialog = false
                Toast.makeText(context, "Unlock phrase updated.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showRoutineScheduleDialog) {
        RoutineScheduleEditorDialog(
            habitConfig = habitConfig,
            onDismiss = { showRoutineScheduleDialog = false },
            onSave = { updated ->
                onUpdateHabitConfig(updated)
                showRoutineScheduleDialog = false
                Toast.makeText(context, "Routine schedule updated.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

private fun formatRoutineClock(hour24: Int, minute: Int): String {
    val h = ((hour24 % 24) + 24) % 24
    val m = ((minute % 60) + 60) % 60
    val period = if (h >= 12) "PM" else "AM"
    val hour12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return String.format(Locale.US, "%d:%02d %s", hour12, m, period)
}

@Composable
private fun FrictionMethodPickerDialog(
    currentType: FrictionType,
    hasPinConfigured: Boolean,
    onDismiss: () -> Unit,
    onSelect: (FrictionType) -> Unit
) {
    val earth = EarthTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = earth.surfaceSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("UNLOCK CHALLENGE", color = OrbitalViolet, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select Early-Unlock Friction", color = earth.textPrimary, fontSize = 18.sp, fontFamily = FontFamily.Serif)
                Spacer(modifier = Modifier.height(14.dp))

                val options = listOf(
                    Triple(FrictionType.HOLD_BUTTON, "5-Second Conscious Hold", "Press and hold to reflect before ending Lock"),
                    Triple(FrictionType.TYPE_PHRASE, "Type Pledge Phrase", "Must type your commitment phrase character-by-character"),
                    Triple(FrictionType.MATH_TASK, "Mental Math Challenge", "Solve two arithmetic problems to exit Lock early"),
                    Triple(FrictionType.PIN_CODE, "Secret 4–8 Digit PIN", if (hasPinConfigured) "Verify with your saved numeric PIN" else "Prompts PIN creation first")
                )
                options.forEach { (type, title, desc) ->
                    val isSelected = type == currentType
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) OrbitalVioletSoft else Color.White)
                            .border(
                                1.dp,
                                if (isSelected) OrbitalViolet else earth.border,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelect(type) }
                            .padding(12.dp)
                    ) {
                        Text(title, color = if (isSelected) OrbitalViolet else earth.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(desc, color = earth.textMuted, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("CLOSE", color = earth.textMuted, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PinSetupModalDialog(
    hasExistingPin: Boolean,
    canClearPin: Boolean,
    onDismiss: () -> Unit,
    onSavePin: (String) -> Unit,
    onRemovePin: () -> Unit
) {
    val earth = EarthTheme.colors
    var pinInput by remember { mutableStateOf("") }
    var confirmInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = earth.surfaceSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("FOCUS PIN", color = OrbitalViolet, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(if (hasExistingPin) "Update Focus PIN" else "Set Focus PIN", color = earth.textPrimary, fontSize = 18.sp, fontFamily = FontFamily.Serif)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) { pinInput = it; errorMsg = null } },
                    label = { Text("New PIN (4–8 digits)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmInput,
                    onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) { confirmInput = it; errorMsg = null } },
                    label = { Text("Confirm PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(msg, color = OrbitalCoral, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL", color = earth.textMuted, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            when {
                                pinInput.length < 4 -> errorMsg = "PIN must be at least 4 digits."
                                pinInput != confirmInput -> errorMsg = "PIN entries do not match."
                                else -> onSavePin(pinInput)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitalViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE PIN", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (hasExistingPin && canClearPin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRemovePin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("REMOVE SAVED PIN", color = OrbitalCoral, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnlockPhraseEditorDialog(
    currentPhrase: String,
    onDismiss: () -> Unit,
    onSavePhrase: (String) -> Unit
) {
    val earth = EarthTheme.colors
    var phraseInput by remember { mutableStateOf(currentPhrase) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = earth.surfaceSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("UNLOCK PLEDGE", color = OrbitalCoral, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Custom Commitment Phrase", color = earth.textPrimary, fontSize = 18.sp, fontFamily = FontFamily.Serif)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phraseInput,
                    onValueChange = { if (it.length <= 100) { phraseInput = it; errorMsg = null } },
                    label = { Text("Commitment phrase (8+ chars)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(msg, color = OrbitalCoral, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL", color = earth.textMuted, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val clean = phraseInput.trim()
                            if (clean.length < 8) {
                                errorMsg = "Enter at least 8 characters."
                            } else {
                                onSavePhrase(clean)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitalCoral),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineScheduleEditorDialog(
    habitConfig: HabitConfig,
    onDismiss: () -> Unit,
    onSave: (HabitConfig) -> Unit
) {
    val earth = EarthTheme.colors
    var morningMin by remember { mutableIntStateOf(habitConfig.morningPledgeHour * 60 + habitConfig.morningPledgeMinute) }
    var eveningMin by remember { mutableIntStateOf(habitConfig.eveningSummaryHour * 60 + habitConfig.eveningSummaryMinute) }
    var bedStartMin by remember { mutableIntStateOf(habitConfig.bedtimeStartHour * 60 + habitConfig.bedtimeStartMinute) }
    var bedEndMin by remember { mutableIntStateOf(habitConfig.bedtimeEndHour * 60 + habitConfig.bedtimeEndMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = earth.surfaceSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("ATTENTION ROUTINES", color = OrbitalSky, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Customize Daily Times", color = earth.textPrimary, fontSize = 18.sp, fontFamily = FontFamily.Serif)
                Spacer(modifier = Modifier.height(12.dp))

                TimeStepperRow("Morning Pledge", morningMin) { delta -> morningMin = ((morningMin + delta) % 1440 + 1440) % 1440 }
                Spacer(modifier = Modifier.height(8.dp))
                TimeStepperRow("Evening Summary", eveningMin) { delta -> eveningMin = ((eveningMin + delta) % 1440 + 1440) % 1440 }
                Spacer(modifier = Modifier.height(8.dp))
                TimeStepperRow("Bedtime Start", bedStartMin) { delta -> bedStartMin = ((bedStartMin + delta) % 1440 + 1440) % 1440 }
                Spacer(modifier = Modifier.height(8.dp))
                TimeStepperRow("Bedtime End", bedEndMin) { delta -> bedEndMin = ((bedEndMin + delta) % 1440 + 1440) % 1440 }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL", color = earth.textMuted, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onSave(
                                habitConfig.copy(
                                    morningPledgeHour = morningMin / 60,
                                    morningPledgeMinute = morningMin % 60,
                                    eveningSummaryHour = eveningMin / 60,
                                    eveningSummaryMinute = eveningMin % 60,
                                    bedtimeStartHour = bedStartMin / 60,
                                    bedtimeStartMinute = bedStartMin % 60,
                                    bedtimeEndHour = bedEndMin / 60,
                                    bedtimeEndMinute = bedEndMin % 60
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitalViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE TIMES", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeStepperRow(
    label: String,
    totalMinutes: Int,
    onStepMinutes: (Int) -> Unit
) {
    val earth = EarthTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, earth.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = earth.textMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(formatRoutineClock(totalMinutes / 60, totalMinutes % 60), color = earth.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(earth.surfaceVariant)
                    .clickable { onStepMinutes(-30) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("-30m", color = earth.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrbitalVioletSoft)
                    .clickable { onStepMinutes(30) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("+30m", color = OrbitalViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
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
