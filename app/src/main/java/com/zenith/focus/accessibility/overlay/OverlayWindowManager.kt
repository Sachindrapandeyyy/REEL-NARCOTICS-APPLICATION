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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import com.zenith.focus.core.designsystem.*
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

    companion object {
        @Volatile
        var isOverlayShowing = false
            private set
        @Volatile
        var lastDismissTime = 0L
            private set

        fun notifyDismissed() {
            isOverlayShowing = false
            lastDismissTime = System.currentTimeMillis()
        }
    }

    fun isCoolingDown(): Boolean {
        return System.currentTimeMillis() - lastDismissTime < 800L
    }

    fun showOverlay(category: ContentCategory, remainingMillis: Long, reason: String) {
        if (isOverlayShowing || isCoolingDown()) return

        mainHandler.post {
            if (isOverlayShowing) return@post
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(service)) {
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
                    ZenithFocusTheme {
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
            notifyDismissed()
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
    val isAppLock = category == ContentCategory.APP_LOCK

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
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient soft lavender aura glow
        Canvas(modifier = Modifier.size(300.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFC084FC).copy(alpha = 0.35f),
                        Color(0xFFDDD6FE).copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension / 2
                ),
                radius = size.minDimension / 2,
                center = center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_reel_narcotics_logo),
                contentDescription = "Reel Narcotics Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.5.dp, Color(0xFFEFE8DE), RoundedCornerShape(26.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sub-category pill
            Text(
                text = when {
                    isAppLock -> "APPLICATION LOCKED"
                    isAdult -> "MINDFUL BOUNDARY"
                    else -> "FOCUS DEFENDED"
                },
                color = OrbitalCoral,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Editorial Heading
            Text(
                text = when {
                    isAppLock -> "Step Away."
                    isAdult -> "Boundary Held."
                    else -> "Pause & Breathe."
                },
                color = Color(0xFF1E1A22),
                fontSize = 32.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Context message
            Text(
                text = if (reason.isNotBlank()) {
                    reason
                } else if (isAppLock) {
                    "This app is locked to protect your focus and dopamine baseline. Ejecting to Home Screen."
                } else if (isAdult) {
                    "This destination is blocked by your Adult Protection shield. Your mind deserves peace."
                } else {
                    "Break the scroll. Take back your attention.\nAddictive vertical feeds are terminated to preserve your dopamine baseline."
                },
                color = Color(0xFF8E889B),
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Timer / Enforcement Pill
            if (remaining > 0L) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, Color(0xFFEFE8DE), RoundedCornerShape(16.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(OrbitalViolet)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${com.zenith.focus.core.time.DateTimeUtils.formatRemaining(remaining)} remaining",
                            color = Color(0xFF1E1A22),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (isAppLock) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, Color(0xFFEFE8DE), RoundedCornerShape(16.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(OrbitalAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Permanent 24/7 Shield",
                            color = Color(0xFF1E1A22),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Calm, Confident Return Button
            Button(
                onClick = onGoBack,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrbitalCoral),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "RETURN TO PRESENT",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}


