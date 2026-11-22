package com.zenith.focus.feature.unlock

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.feature.physical.PhysicalAnchorManager
import kotlinx.coroutines.delay

@Composable
fun UnlockFrictionDialog(
    @Suppress("UNUSED_PARAMETER") lockState: LockState,
    config: ProtectionConfig,
    isNuclearActive: Boolean = false,
    scannedNfcTagHash: String? = null,
    onDismiss: () -> Unit,
    onUnlockConfirmed: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onVerifyPin: suspend (String) -> Boolean = { true }
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current

    if (isNuclearActive) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = earth.surfaceSoft,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, earth.camelOchre, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "☢️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NUCLEAR MODE ACTIVE",
                        color = earth.camelOchre,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Early unlock is permanently disabled. You committed to this session until the countdown timer concludes.",
                        color = earth.textMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "RETURN TO DASHBOARD", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val isPhysicalAnchorRequired = config.enablePhysicalAnchor &&
            (config.nfcAnchorHash.isNotBlank() || config.qrAnchorHash.isNotBlank())

    var qrTokenInput by remember { mutableStateOf("") }
    var qrError by remember { mutableStateOf<String?>(null) }
    var isEmergencyTimerActive by remember { mutableStateOf(false) }
    var emergencySecondsRemaining by remember { mutableIntStateOf(PhysicalAnchorManager.EMERGENCY_OVERRIDE_MINUTES * 60) }

    LaunchedEffect(isEmergencyTimerActive) {
        if (isEmergencyTimerActive) {
            while (emergencySecondsRemaining > 0) {
                delay(1000L)
                emergencySecondsRemaining--
            }
            onUnlockConfirmed()
            onDismiss()
        }
    }

    LaunchedEffect(scannedNfcTagHash) {
        if (scannedNfcTagHash != null && isPhysicalAnchorRequired) {
            if (PhysicalAnchorManager.verifyNfcTag(scannedNfcTagHash, config.nfcAnchorHash)) {
                Toast.makeText(context, "✓ NFC Key Verified! Session Unlocked", Toast.LENGTH_SHORT).show()
                onUnlockConfirmed()
                onDismiss()
            } else {
                Toast.makeText(context, "✗ Wrong NFC Card. Tap the registered card.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = earth.surfaceSoft,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, earth.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isEmergencyTimerActive) {
                    // EMERGENCY TIMER SCREEN
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Timer",
                        tint = earth.camelOchre,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "EMERGENCY OVERRIDE",
                        color = earth.camelOchre,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${emergencySecondsRemaining / 60}m ${emergencySecondsRemaining % 60}s",
                        color = earth.forestDark,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Take a breath. When this 15-minute anti-impulse cooldown ends, your phone will unlock without the physical key.",
                        color = earth.textMuted,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = { isEmergencyTimerActive = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(text = "Cancel Override (Keep Focusing)", fontSize = 12.sp, color = earth.forestDark)
                    }
                } else if (isPhysicalAnchorRequired) {
                    // PHYSICAL ANCHOR REQUIRED SCREEN
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFFFF3E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Nfc,
                            contentDescription = "Physical Anchor",
                            tint = earth.camelOchre,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "PHYSICAL KEY REQUIRED",
                        color = earth.camelOchre,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap Card or Scan QR Key",
                        color = earth.forestDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You locked this session with a physical focus key. Tap your registered NFC card against your phone or enter the QR key.",
                        color = earth.textMuted,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (config.qrAnchorHash.isNotBlank()) {
                        OutlinedTextField(
                            value = qrTokenInput,
                            onValueChange = { input ->
                                qrTokenInput = input
                                if (PhysicalAnchorManager.verifyQrPayload(input, config.qrAnchorHash)) {
                                    qrError = null
                                    Toast.makeText(context, "✓ QR Anchor Key Verified!", Toast.LENGTH_SHORT).show()
                                    onUnlockConfirmed()
                                    onDismiss()
                                } else if (input.isNotBlank() && input.length >= 10) {
                                    qrError = "Invalid QR key token"
                                } else {
                                    qrError = null
                                }
                            },
                            isError = qrError != null,
                            placeholder = { Text("Enter QR payload token...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                Icon(imageVector = Icons.Outlined.QrCodeScanner, contentDescription = "Scan")
                            }
                        )
                        if (qrError != null) {
                            Text(
                                text = "✗ $qrError",
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen)
                    ) {
                        Text(text = "KEEP FOCUSING ✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Emergency 15-minute escape
                    OutlinedButton(
                        onClick = { isEmergencyTimerActive = true },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = earth.camelOchre)
                    ) {
                        Text(text = "Lost Card / QR? (15m Cooldown)", fontSize = 12.sp, color = earth.camelOchre)
                    }
                } else {
                    // STANDARD FOCUS LOCK CONFIRMATION
                    Text(
                        text = "🔒",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FOCUS SESSION ACTIVE",
                        color = earth.forestGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "End Focus Session Early?",
                        color = earth.forestDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Are you sure you want to end this focus session? Distraction feeds will be unblocked.",
                        color = earth.textMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen)
                    ) {
                        Text(
                            text = "KEEP FOCUSING ✓",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            onUnlockConfirmed()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = earth.camelOchre)
                    ) {
                        Text(
                            text = "End Session Now",
                            color = earth.camelOchre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}
