package com.zenith.focus.feature.psychology

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.ZenithApplication
import com.zenith.focus.core.designsystem.ZenithFocusTheme
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoomscrollWarningActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }

    private var targetPackageState by mutableStateOf("")

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        targetPackageState = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        targetPackageState = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""

        val returnHome = {
            val pkg = targetPackageState
            val app = runCatching { ZenithApplication.instance }.getOrNull()
            app?.container?.statisticsRepository?.let { repo ->
                CoroutineScope(Dispatchers.IO).launch {
                    repo.recordBlockEvent(
                        BlockEvent(
                            timestamp = System.currentTimeMillis(),
                            packageName = pkg.ifBlank { "unknown" },
                            category = ContentCategory.DOOMSCROLL_BROKEN,
                            confidence = 1.0f,
                            ruleId = "doomscroll_interrupted"
                        )
                    )
                }
            }

            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            runCatching { startActivity(homeIntent) }
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnHome()
            }
        })

        setContent {
            ZenithFocusTheme {
                DoomscrollWarningScreen(
                    targetPackage = targetPackageState,
                    onCloseApp = returnHome,
                    onContinue = {
                        val app = runCatching { ZenithApplication.instance }.getOrNull()
                        app?.container?.doomscrollDetector?.resetWithCooldown(120_000L)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun DoomscrollWarningScreen(
    targetPackage: String,
    onCloseApp: () -> Unit,
    onContinue: () -> Unit
) {
    val appLabel = when {
        targetPackage.contains("instagram") -> "Instagram"
        targetPackage.contains("youtube") -> "YouTube"
        targetPackage.contains("facebook") -> "Facebook"
        targetPackage.contains("snapchat") -> "Snapchat"
        targetPackage.contains("reddit") -> "Reddit"
        targetPackage.contains("tiktok") -> "TikTok"
        else -> "this feed"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF334155), CircleShape)
                    .border(2.dp, Color(0xFFF59E0B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛑",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DOOMSCROLL DETECTED",
                color = Color(0xFFF59E0B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "You're scrolling on autopilot.",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "You've swiped rapidly across $appLabel without pause. Is this really how you want to spend your attention right now?",
                color = Color(0xFF94A3B8),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Primary Button: Close App
            Button(
                onClick = onCloseApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Close App & Save Focus",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Button: Continue
            OutlinedButton(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF94A3B8)
                )
            ) {
                Text(
                    text = "I'm Conscious, Resume Session",
                    fontSize = 14.sp
                )
            }
        }
    }
}
