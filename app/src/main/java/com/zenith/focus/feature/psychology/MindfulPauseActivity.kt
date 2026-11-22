package com.zenith.focus.feature.psychology

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.ZenithApplication
import com.zenith.focus.core.designsystem.ZenithFocusTheme
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MindfulPauseActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""

        val returnHome = {
            val app = runCatching { ZenithApplication.instance }.getOrNull()
            app?.container?.statisticsRepository?.let { repo ->
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    repo.recordBlockEvent(
                        BlockEvent(
                            timestamp = System.currentTimeMillis(),
                            packageName = targetPackage.ifBlank { "unknown" },
                            category = ContentCategory.IMPULSE_DEFEATED,
                            confidence = 1.0f,
                            ruleId = "mindful_pause_saved"
                        )
                    )
                }
            }

            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(homeIntent)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnHome()
            }
        })

        setContent {
            ZenithFocusTheme {
                MindfulPauseScreen(
                    targetPackage = targetPackage,
                    onCloseApp = returnHome,
                    onProceedWithIntent = { durationMinutes, purpose ->
                        val app = runCatching { ZenithApplication.instance }.getOrNull()
                        app?.container?.intentTimeBoxManager?.startTimeBox(
                            targetPackage,
                            durationMinutes,
                            purpose
                        )
                        app?.container?.mindfulPauseManager?.markPassed(targetPackage)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MindfulPauseScreen(
    targetPackage: String,
    onCloseApp: () -> Unit,
    onProceedWithIntent: (durationMinutes: Int, purpose: String) -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(5) }
    var showTimeBoxPicker by remember { mutableStateOf(false) }

    var selectedDuration by remember { mutableIntStateOf(10) }
    var selectedPurpose by remember { mutableStateOf("Quick Check") }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    val appLabel = remember(targetPackage) {
        when {
            targetPackage.contains("instagram") -> "Instagram"
            targetPackage.contains("youtube") -> "YouTube"
            targetPackage.contains("facebook") -> "Facebook"
            targetPackage.contains("snapchat") -> "Snapchat"
            targetPackage.contains("reddit") -> "Reddit"
            else -> "this app"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Deep calming indigo-slate
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!showTimeBoxPicker) {
            // PHASE 1: 5-Second Mindful Breathing Pause
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🌱 MINDFUL PAUSE",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Take a breath.",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Before entering $appLabel, pause and notice if this is an unconscious impulse.",
                    color = Color(0xFF94A3B8),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Breathing Ring
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(breatheScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF0284C7).copy(alpha = 0.35f),
                                        Color(0xFF0284C7).copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), CircleShape)
                    )

                    Text(
                        text = if (breatheScale > 1.05f) "Inhale..." else "Exhale...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(56.dp))

                // Primary Large Action: Close App (Always enabled)
                Button(
                    onClick = onCloseApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)), // Calming emerald
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Close App (Save Focus)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Subtle Action: Continue with Intent (Unlocked after 5s)
                if (secondsRemaining > 0) {
                    Text(
                        text = "Intent picker unlocks in ${secondsRemaining}s",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                } else {
                    OutlinedButton(
                        onClick = { showTimeBoxPicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE2E8F0)
                        )
                    ) {
                        Text(
                            text = "Continue with Intent →",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // PHASE 2: Intent & Time-Box Picker
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 CHOOSE YOUR INTENT",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Why are you opening $appLabel?",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Purpose Selection
                Text(
                    text = "SELECT PURPOSE",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val purposes = listOf("DMs / Reply", "Study / Research", "Quick Check")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    purposes.forEach { purpose ->
                        val isSelected = selectedPurpose == purpose
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPurpose = purpose }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = purpose,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Duration Selection
                Text(
                    text = "SELECT TIME-BOX DURATION",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val durations = listOf(5, 10, 15)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    durations.forEach { duration ->
                        val isSelected = selectedDuration == duration
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) Color(0xFF059669) else Color(0xFF1E293B),
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF34D399) else Color(0xFF334155),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedDuration = duration }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$duration min",
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Start Intent Session
                Button(
                    onClick = { onProceedWithIntent(selectedDuration, selectedPurpose) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Enter for $selectedDuration min ($selectedPurpose)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onCloseApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF94A3B8)
                    )
                ) {
                    Text(
                        text = "Actually, Never Mind (Save Focus)",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
