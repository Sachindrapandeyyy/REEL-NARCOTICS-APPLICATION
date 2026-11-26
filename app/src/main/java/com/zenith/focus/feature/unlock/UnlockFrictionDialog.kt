package com.zenith.focus.feature.unlock

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.domain.model.FrictionType
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.feature.physical.PhysicalAnchorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun UnlockFrictionDialog(
    @Suppress("UNUSED_PARAMETER") lockState: LockState,
    config: ProtectionConfig,
    isNuclearActive: Boolean = false,
    scannedNfcTagHash: String? = null,
    onClearScannedNfcTag: () -> Unit = {},
    onDismiss: () -> Unit,
    onUnlockConfirmed: () -> Unit,
    onVerifyPin: suspend (String) -> Boolean = { true }
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
        if (!scannedNfcTagHash.isNullOrBlank() && isPhysicalAnchorRequired) {
            val isMatch = PhysicalAnchorManager.verifyNfcTag(scannedNfcTagHash, config.nfcAnchorHash)
            onClearScannedNfcTag()
            if (isMatch) {
                Toast.makeText(context, "✓ NFC Key Verified! Session Unlocked", Toast.LENGTH_SHORT).show()
                onUnlockConfirmed()
                onDismiss()
            } else {
                Toast.makeText(context, "✗ Wrong NFC Card. Tap the registered card.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val scope = rememberCoroutineScope()
    val view = LocalView.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = earth.surfaceSoft,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, earth.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
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
                    // STANDARD FOCUS LOCK FRICTION CHALLENGE
                    val effectiveFriction = if (config.frictionType == FrictionType.PIN_CODE && config.pinHash.isBlank()) {
                        FrictionType.HOLD_BUTTON
                    } else {
                        config.frictionType
                    }

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
                        text = effectiveFriction.description,
                        color = earth.textMuted,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    when (effectiveFriction) {
                        FrictionType.HOLD_BUTTON -> {
                            var holdProgress by remember { mutableFloatStateOf(0f) }
                            var isHolding by remember { mutableStateOf(false) }

                            LaunchedEffect(isHolding) {
                                if (isHolding) {
                                    val steps = 50
                                    for (i in 1..steps) {
                                        if (!isHolding) break
                                        delay(100L)
                                        holdProgress = i.toFloat() / steps.toFloat()
                                        if (i % 10 == 0) {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        }
                                    }
                                    if (isHolding && holdProgress >= 1f) {
                                        onUnlockConfirmed()
                                        onDismiss()
                                    }
                                } else {
                                    holdProgress = 0f
                                }
                            }

                            val animatedProgress by animateFloatAsState(
                                targetValue = holdProgress,
                                animationSpec = tween(100),
                                label = "UnlockHold"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(earth.camelOchre.copy(alpha = 0.16f))
                                    .border(1.5.dp, if (isHolding) earth.camelOchre else earth.border, RoundedCornerShape(14.dp))
                                    .pointerInput(Unit) {
                                        awaitEachGesture {
                                            awaitFirstDown(requireUnconsumed = false)
                                            isHolding = true
                                            do {
                                                val ev = awaitPointerEvent()
                                            } while (ev.changes.any { it.pressed })
                                            isHolding = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedProgress)
                                        .height(50.dp)
                                        .background(earth.camelOchre)
                                        .align(Alignment.CenterStart)
                                )
                                Text(
                                    text = if (isHolding) "HOLDING... (${(animatedProgress * 100).toInt()}%)" else "HOLD 5s TO END SESSION",
                                    color = if (animatedProgress > 0.45f) Color.White else earth.camelOchre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                        FrictionType.TYPE_PHRASE -> {
                            val targetPhrase = config.unlockPhrase.ifBlank { "I choose focus over dopamine" }
                            var typedInput by remember { mutableStateOf("") }
                            val matchesPhrase = typedInput.trim().equals(targetPhrase.trim(), ignoreCase = false)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, earth.border, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = targetPhrase,
                                    color = earth.forestDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = typedInput,
                                onValueChange = { typedInput = it },
                                placeholder = { Text("Type phrase exactly...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    if (matchesPhrase) {
                                        onUnlockConfirmed()
                                        onDismiss()
                                    }
                                },
                                enabled = matchesPhrase,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "End Session Now",
                                    color = if (matchesPhrase) earth.camelOchre else earth.textMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                        FrictionType.MATH_TASK -> {
                            val a = remember { Random.nextInt(13, 28) }
                            val b = remember { Random.nextInt(6, 13) }
                            val c = remember { Random.nextInt(12, 39) }
                            val expectedAnswer = remember { a * b + c }
                            var mathInput by remember { mutableStateOf("") }
                            var mathError by remember { mutableStateOf(false) }

                            Text(
                                text = "What is ($a × $b) + $c ?",
                                color = earth.forestDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = mathInput,
                                onValueChange = {
                                    mathInput = it.filter { ch -> ch.isDigit() }
                                    mathError = false
                                },
                                isError = mathError,
                                placeholder = { Text("Enter answer...", fontSize = 12.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (mathError) {
                                Text("✗ Incorrect answer", color = Color(0xFFD32F2F), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    if (mathInput.trim().toIntOrNull() == expectedAnswer) {
                                        onUnlockConfirmed()
                                        onDismiss()
                                    } else {
                                        mathError = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Verify & End Session", color = earth.camelOchre, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                        FrictionType.PIN_CODE -> {
                            var pinInput by remember { mutableStateOf("") }
                            var pinError by remember { mutableStateOf(false) }

                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = {
                                    pinInput = it.filter { ch -> ch.isDigit() }.take(8)
                                    pinError = false
                                },
                                isError = pinError,
                                placeholder = { Text("Enter Focus PIN...", fontSize = 12.sp) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (pinError) {
                                Text("✗ Incorrect PIN", color = Color(0xFFD32F2F), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        if (onVerifyPin(pinInput)) {
                                            onUnlockConfirmed()
                                            onDismiss()
                                        } else {
                                            pinError = true
                                        }
                                    }
                                },
                                enabled = pinInput.length >= 4,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Verify PIN & Unlock", color = earth.camelOchre, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                }
            }
        }
    }
}
