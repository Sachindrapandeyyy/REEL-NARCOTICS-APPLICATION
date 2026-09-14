package com.zenith.focus.accessibility.overlay

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.zenith.focus.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.ZenithDarkBg
import com.zenith.focus.core.designsystem.ZenithPrimary
import com.zenith.focus.core.mascot.ZenithMascot
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.MascotState
import com.zenith.focus.feature.blocked.BlockScreenActivity
import kotlinx.coroutines.delay

class OverlayWindowManager(
    private val service: AccessibilityService,
    private val onGoBackRequested: () -> Unit
) {
    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private var isOverlayShowing = false
    private var lastDismissTime = 0L

    fun isCoolingDown(): Boolean {
        return System.currentTimeMillis() - lastDismissTime < 800L
    }

    fun showOverlay(category: ContentCategory, remainingMillis: Long, reason: String) {
        if (isOverlayShowing || isCoolingDown()) return

        mainHandler.post {
            if (isOverlayShowing) return@post

            if (Settings.canDrawOverlays(service)) {
                createAndAttachWindowOverlay(category, remainingMillis, reason)
            } else {
                launchFallbackActivity(category, remainingMillis, reason)
            }
        }
    }

    private fun createAndAttachWindowOverlay(category: ContentCategory, remainingMillis: Long, reason: String) {
        runCatching {
            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val composeView = ComposeView(service).apply {
                setContent {
                    MaterialTheme {
                        BlockOverlayContent(
                            category = category,
                            initialRemainingMillis = remainingMillis,
                            reason = reason,
                            onGoBack = {
                                dismissOverlay()
                                onGoBackRequested()
                            }
                        )
                    }
                }
            }

            windowManager.addView(composeView, layoutParams)
            overlayView = composeView
            isOverlayShowing = true
        }.onFailure {
            launchFallbackActivity(category, remainingMillis, reason)
        }
    }

    private fun launchFallbackActivity(category: ContentCategory, remainingMillis: Long, reason: String) {
        val intent = Intent(service, BlockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra(BlockScreenActivity.EXTRA_CATEGORY, category.name)
            putExtra(BlockScreenActivity.EXTRA_REMAINING_MILLIS, remainingMillis)
            putExtra(BlockScreenActivity.EXTRA_REASON, reason)
        }
        service.startActivity(intent)
        isOverlayShowing = true
    }

    fun dismissOverlay() {
        mainHandler.post {
            overlayView?.let { view ->
                runCatching {
                    windowManager.removeView(view)
                }
                overlayView = null
            }
            isOverlayShowing = false
            lastDismissTime = System.currentTimeMillis()
        }
    }
}

@Composable
fun BlockOverlayContent(
    category: ContentCategory,
    initialRemainingMillis: Long,
    reason: String = "",
    onGoBack: () -> Unit
) {
    var remaining by remember { mutableLongStateOf(initialRemainingMillis) }

    LaunchedEffect(Unit) {
        while (remaining > 0L) {
            delay(1000L)
            remaining = (remaining - 1000L).coerceAtLeast(0L)
        }
    }

    val isAdult = category == ContentCategory.ADULT_WEBSITE || category == ContentCategory.ADULT_KEYWORD

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_reel_narcotics_logo),
                contentDescription = "Reel Narcotics Logo",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Heading
            Text(
                text = if (isAdult) "EXPLICIT CONTENT INTERCEPTED" else "REEL NARCOTICS",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Motto & Explanation
            Text(
                text = if (reason.isNotBlank()) {
                    reason
                } else if (isAdult) {
                    "This destination is blocked by your Adult Protection shield. Your mind deserves peace."
                } else {
                    "Break the scroll. Take back your attention.\nAddictive vertical feeds are terminated to preserve your dopamine baseline."
                },
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Timer Pill
            Box(
                modifier = Modifier
                    .background(Color(0xFF1E293B), shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${com.zenith.focus.core.time.DateTimeUtils.formatRemaining(remaining)} remaining",
                    color = Color(0xFF10B981),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Big Go Back Button
            Button(
                onClick = onGoBack,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "GO BACK",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

