package com.zenith.focus.feature.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.ZenithBurgundy
import com.zenith.focus.core.designsystem.ZenithBurgundyDeep
import com.zenith.focus.core.designsystem.ZenithCardDark
import com.zenith.focus.core.designsystem.ZenithEmerald
import com.zenith.focus.core.designsystem.ZenithEmeraldAccent
import com.zenith.focus.core.designsystem.ZenithNavy
import com.zenith.focus.core.designsystem.ZenithNavyDark
import com.zenith.focus.core.ui.RestrictedSettingsBanner
import com.zenith.focus.core.ui.RestrictedSettingsGuideDialog
import com.zenith.focus.domain.model.FrictionType
import com.zenith.focus.domain.model.ProtectionConfig
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    config: ProtectionConfig,
    isServiceConnected: Boolean,
    isNuclearActive: Boolean = false,
    currentTheme: String,
    onSelectFrictionType: (FrictionType) -> Unit,
    onSetPin: suspend (String) -> Unit,
    onClearPin: suspend () -> Unit,
    onSetTheme: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenithNavyDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "SECURITY & CONTROLS",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Manage system permissions and anti-impulse unlock barriers.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // NUCLEAR MODE ACTIVE BANNER (IF ACTIVE)
        if (isNuclearActive) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithBurgundyDeep),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ZenithBurgundy, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "☢️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NUCLEAR MODE ACTIVE",
                            color = Color(0xFFFECACA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Security and unlock friction settings are locked until the session expires.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // SYSTEM STATUS
        Text(
            text = "SYSTEM STATUS",
            color = ZenithEmeraldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Accessibility Service Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ZenithNavy),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accessibility Shield Service", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isServiceConnected) "Online: Instant zero-tolerance ejection active" else "Inactive: Required for short-form video blocking",
                            color = if (isServiceConnected) ZenithEmeraldAccent else Color(0xFFF43F5E),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isServiceConnected) ZenithEmeraldAccent else Color(0xFFF43F5E))
                    )
                }

                if (!isServiceConnected) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ACTIVATE ACCESSIBILITY PERMISSION", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    RestrictedSettingsBanner(
                        onOpenDialog = { showRestrictedDialog = true }
                    )
                }
            }
        }

        // Uninstall Protection (Device Administrator) Card
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ZenithNavy),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Uninstall Protection (Device Admin)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isDeviceAdminActive) "Active: OS-level deletion block armed" else "Inactive: Grant Device Admin to prevent uninstallation",
                            color = if (isDeviceAdminActive) ZenithEmeraldAccent else Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isDeviceAdminActive) ZenithEmeraldAccent else Color(0xFFF59E0B))
                    )
                }

                if (!isDeviceAdminActive) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            ZenithDeviceAdminReceiver.openDeviceAdminActivation(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ACTIVATE UNINSTALL PROTECTION", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    RestrictedSettingsBanner(
                        onOpenDialog = { showRestrictedDialog = true }
                    )
                } else if (isNuclearActive) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🔒 Locked by Nuclear Mode. Deactivation is forbidden until session expires.",
                        color = Color(0xFFFECACA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ANTI-IMPULSE UNLOCK FRICTION
        Text(
            text = "ANTI-IMPULSE UNLOCK FRICTION",
            color = Color(0xFF06B6D4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Barriers required if you attempt to cancel a standard focus lock early.",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
        )

        FrictionType.values().forEach { friction ->
            val isSelected = config.frictionType == friction
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF164E63) else ZenithNavy
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        1.dp,
                        if (isSelected) Color(0xFF06B6D4) else Color(0xFF1E293B),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = !isNuclearActive) {
                        onSelectFrictionType(friction)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF06B6D4) else Color(0xFF475569))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = friction.displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = friction.description, color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        }

        if (config.frictionType == FrictionType.PIN_CODE && !isNuclearActive) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showPinDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (config.pinHash.isNotBlank()) "Change PIN" else "Set 4-Digit PIN",
                        color = Color(0xFF06B6D4)
                    )
                }
                if (config.pinHash.isNotBlank()) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onClearPin()
                                Toast.makeText(context, "PIN removed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Remove", color = Color(0xFFF43F5E))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // APPEARANCE
        Text(
            text = "APPEARANCE",
            color = Color(0xFFF59E0B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

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
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Executive Obsidian Theme", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Pure high-contrast palette engineered for maximum focus & battery savings", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ZenithEmerald)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("ACTIVE", color = ZenithEmeraldAccent, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ABOUT & PRIVACY GUARANTEE
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.zenith.focus.R.drawable.ic_reel_narcotics_logo),
                    contentDescription = "Reel Narcotics Logo",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Reel Narcotics v2.0.0", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    Text("Break the scroll. Take back your attention.", color = ZenithEmeraldAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "100% Offline | Zero Telemetry | Zero Accounts\nNo internet permission requested or needed.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showPinDialog && !isNuclearActive) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onSavePin = { pin ->
                coroutineScope.launch {
                    onSetPin(pin)
                    showPinDialog = false
                    Toast.makeText(context, "PIN code saved", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onSavePin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ZenithNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Set Security PIN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "You will be required to enter this 4-digit PIN to cancel any focus lock early.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("Enter PIN (4-6 digits)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFF43F5E),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (pin.length < 4) {
                                errorMessage = "PIN must be at least 4 digits"
                            } else if (pin != confirmPin) {
                                errorMessage = "PINs do not match"
                            } else {
                                onSavePin(pin)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save PIN")
                    }
                }
            }
        }
    }
}
