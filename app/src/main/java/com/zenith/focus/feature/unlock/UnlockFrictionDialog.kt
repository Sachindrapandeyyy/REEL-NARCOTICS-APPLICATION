package com.zenith.focus.feature.unlock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.model.FrictionType
import com.zenith.focus.domain.model.LockState
import com.zenith.focus.domain.model.ProtectionConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun UnlockFrictionDialog(
    lockState: LockState,
    config: ProtectionConfig,
    isNuclearActive: Boolean = false,
    onDismiss: () -> Unit,
    onUnlockConfirmed: () -> Unit,
    onVerifyPin: suspend (String) -> Boolean
) {
    if (isNuclearActive) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "☢️", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NUCLEAR MODE ACTIVE",
                        color = Color(0xFFF43F5E),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Early unlock is permanently disabled. You committed to this session until the timer expires.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "RETURN TO DASHBOARD", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val frictionType = config.frictionType

    // For Math task
    var num1 by remember { mutableIntStateOf(Random.nextInt(25, 65)) }
    var num2 by remember { mutableIntStateOf(Random.nextInt(15, 45)) }
    var mathInput by remember { mutableStateOf("") }
    var mathError by remember { mutableStateOf(false) }

    // For Phrase typing
    var typedPhrase by remember { mutableStateOf("") }
    val targetPhrase = config.unlockPhrase

    // For PIN
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // For Hold 5s
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var holdJob by remember { mutableStateOf<Job?>(null) }

    val animatedHoldProgress by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(100),
        label = "HoldProgress"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "UNLOCK FRICTION",
                    color = Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Confirm Disabling Lock",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                when (frictionType) {
                    FrictionType.HOLD_BUTTON -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Hold the button for 5 seconds to unlock.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                holdJob = coroutineScope.launch {
                                                    val steps = 50
                                                    for (i in 1..steps) {
                                                        delay(100L)
                                                        holdProgress = i.toFloat() / steps.toFloat()
                                                    }
                                                    onUnlockConfirmed()
                                                    onDismiss()
                                                }
                                                tryAwaitRelease()
                                                holdJob?.cancel()
                                                holdProgress = 0f
                                            }
                                        )
                                    }
                            ) {
                                CircularProgressIndicator(
                                    progress = animatedHoldProgress,
                                    modifier = Modifier.size(120.dp),
                                    color = Color(0xFF10B981),
                                    strokeWidth = 6.dp,
                                    trackColor = Color(0xFF334155)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(animatedHoldProgress * 5).toInt()}s",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "HOLD",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    FrictionType.TYPE_PHRASE -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Type the phrase below to confirm:",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(Color(0xFF1E293B), shape = RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = targetPhrase,
                                    color = Color(0xFF06B6D4),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            OutlinedTextField(
                                value = typedPhrase,
                                onValueChange = { typedPhrase = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Type phrase exactly...", color = Color(0xFF64748B)) },
                                singleLine = false,
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (typedPhrase.trim().equals(targetPhrase.trim(), ignoreCase = true)) {
                                        onUnlockConfirmed()
                                        onDismiss()
                                    }
                                },
                                enabled = typedPhrase.trim().equals(targetPhrase.trim(), ignoreCase = true),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("UNLOCK")
                            }
                        }
                    }

                    FrictionType.MATH_TASK -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Solve this arithmetic problem:",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "$num1 + $num2 = ?",
                                color = Color(0xFF10B981),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            OutlinedTextField(
                                value = mathInput,
                                onValueChange = {
                                    mathInput = it
                                    mathError = false
                                },
                                placeholder = { Text("Your answer", color = Color(0xFF64748B)) },
                                singleLine = true,
                                isError = mathError
                            )
                            if (mathError) {
                                Text("Incorrect, please try again", color = Color(0xFFF43F5E), fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (mathInput.trim() == (num1 + num2).toString()) {
                                        onUnlockConfirmed()
                                        onDismiss()
                                    } else {
                                        mathError = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("CONFIRM")
                            }
                        }
                    }

                    FrictionType.PIN_CODE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Enter 4-digit PIN to disable lock:",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = enteredPin,
                                onValueChange = {
                                    if (it.length <= 4) {
                                        enteredPin = it
                                        pinError = false
                                    }
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                isError = pinError,
                                placeholder = { Text("    ", color = Color(0xFF64748B)) },
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                            if (pinError) {
                                Text("Incorrect PIN", color = Color(0xFFF43F5E), fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val valid = onVerifyPin(enteredPin)
                                        if (valid) {
                                            onUnlockConfirmed()
                                            onDismiss()
                                        } else {
                                            pinError = true
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("VERIFY & UNLOCK")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("KEEP LOCK ACTIVE", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}
