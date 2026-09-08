package com.zenith.focus.core.mascot

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zenith.focus.domain.model.MascotState

@Composable
fun ZenithMascot(
    state: MascotState,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenithMascotMotion")
    
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ZenithBreath"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ZenithAura"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = (w * 0.36f) * breathScale

        // Draw Ambient Aura
        val (auraColor1, auraColor2) = when (state) {
            MascotState.HEALTHY, MascotState.FOCUSED -> Color(0xFF10B981) to Color(0xFF06B6D4)
            MascotState.ALERT -> Color(0xFFF59E0B) to Color(0xFFEF4444)
            MascotState.PROTECTED -> Color(0xFF06B6D4) to Color(0xFF3B82F6)
            MascotState.BLOCKING -> Color(0xFFF43F5E) to Color(0xFF881337)
            MascotState.STREAK -> Color(0xFFF59E0B) to Color(0xFF8B5CF6)
            MascotState.RECOVERY -> Color(0xFF8B5CF6) to Color(0xFF3B82F6)
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(auraColor1.copy(alpha = auraAlpha), auraColor2.copy(alpha = 0f)),
                center = Offset(cx, cy),
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f,
            center = Offset(cx, cy)
        )

        // Draw Forcefield Shell if PROTECTED or BLOCKING
        if (state == MascotState.PROTECTED || state == MascotState.BLOCKING) {
            val shieldColor = if (state == MascotState.BLOCKING) Color(0xFFF43F5E) else Color(0xFF06B6D4)
            drawCircle(
                color = shieldColor.copy(alpha = 0.35f),
                radius = radius * 1.25f,
                center = Offset(cx, cy),
                style = Stroke(width = 4.dp.toPx())
            )
            drawCircle(
                color = shieldColor.copy(alpha = 0.15f),
                radius = radius * 1.35f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Draw Ears/Antennae
        val earOffset = radius * 0.72f
        val earSize = radius * 0.32f
        drawCircle(
            color = auraColor1,
            radius = earSize,
            center = Offset(cx - earOffset, cy - earOffset * 0.85f)
        )
        drawCircle(
            color = auraColor1,
            radius = earSize,
            center = Offset(cx + earOffset, cy - earOffset * 0.85f)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = earSize * 0.55f,
            center = Offset(cx - earOffset, cy - earOffset * 0.85f)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = earSize * 0.55f,
            center = Offset(cx + earOffset, cy - earOffset * 0.85f)
        )

        // Draw Body Shape (Smooth organic rounded pebble)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
            ),
            topLeft = Offset(cx - radius, cy - radius * 0.9f),
            size = Size(radius * 2f, radius * 1.9f),
            cornerRadius = CornerRadius(radius * 0.75f, radius * 0.75f)
        )

        // Draw Golden Crown for STREAK
        if (state == MascotState.STREAK) {
            drawCrown(cx, cy - radius * 0.95f, radius * 0.5f)
        }

        // Draw Face Expression based on MascotState
        when (state) {
            MascotState.HEALTHY -> drawHealthyFace(cx, cy, radius)
            MascotState.FOCUSED -> drawFocusedFace(cx, cy, radius)
            MascotState.ALERT -> drawAlertFace(cx, cy, radius)
            MascotState.PROTECTED -> drawProtectedFace(cx, cy, radius)
            MascotState.BLOCKING -> drawBlockingFace(cx, cy, radius)
            MascotState.STREAK -> drawStreakFace(cx, cy, radius)
            MascotState.RECOVERY -> drawRecoveryFace(cx, cy, radius)
        }
    }
}

private fun DrawScope.drawHealthyFace(cx: Float, cy: Float, r: Float) {
    val eyeY = cy - r * 0.1f
    val eyeSpacing = r * 0.38f
    val eyeR = r * 0.14f

    // Big happy sparkling eyes
    drawCircle(Color(0xFF10B981), radius = eyeR, center = Offset(cx - eyeSpacing, eyeY))
    drawCircle(Color(0xFF10B981), radius = eyeR, center = Offset(cx + eyeSpacing, eyeY))
    drawCircle(Color.White, radius = eyeR * 0.4f, center = Offset(cx - eyeSpacing + 2f, eyeY - 2f))
    drawCircle(Color.White, radius = eyeR * 0.4f, center = Offset(cx + eyeSpacing + 2f, eyeY - 2f))

    // Rosy Cheeks
    drawCircle(Color(0xFFF43F5E).copy(alpha = 0.35f), radius = eyeR * 0.7f, center = Offset(cx - eyeSpacing * 1.3f, eyeY + eyeR * 1.2f))
    drawCircle(Color(0xFFF43F5E).copy(alpha = 0.35f), radius = eyeR * 0.7f, center = Offset(cx + eyeSpacing * 1.3f, eyeY + eyeR * 1.2f))

    // Friendly smile
    val smilePath = Path().apply {
        moveTo(cx - eyeSpacing * 0.6f, eyeY + r * 0.28f)
        quadraticBezierTo(cx, eyeY + r * 0.46f, cx + eyeSpacing * 0.6f, eyeY + r * 0.28f)
    }
    drawPath(smilePath, color = Color.White, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawFocusedFace(cx: Float, cy: Float, r: Float) {
    val eyeY = cy - r * 0.08f
    val eyeSpacing = r * 0.38f
    val strokeWidth = 3.5.dp.toPx()

    // Zen arched meditation eyes (^_^)
    val leftEye = Path().apply {
        moveTo(cx - eyeSpacing - r * 0.12f, eyeY + r * 0.05f)
        quadraticBezierTo(cx - eyeSpacing, eyeY - r * 0.12f, cx - eyeSpacing + r * 0.12f, eyeY + r * 0.05f)
    }
    val rightEye = Path().apply {
        moveTo(cx + eyeSpacing - r * 0.12f, eyeY + r * 0.05f)
        quadraticBezierTo(cx + eyeSpacing, eyeY - r * 0.12f, cx + eyeSpacing + r * 0.12f, eyeY + r * 0.05f)
    }
    drawPath(leftEye, color = Color(0xFF06B6D4), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    drawPath(rightEye, color = Color(0xFF06B6D4), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

    // Third eye / forehead crystal
    drawCircle(Color(0xFF10B981), radius = r * 0.08f, center = Offset(cx, cy - r * 0.45f))

    // Peaceful calm mouth
    val smilePath = Path().apply {
        moveTo(cx - eyeSpacing * 0.4f, eyeY + r * 0.3f)
        quadraticBezierTo(cx, eyeY + r * 0.4f, cx + eyeSpacing * 0.4f, eyeY + r * 0.3f)
    }
    drawPath(smilePath, color = Color.White.copy(alpha = 0.9f), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawAlertFace(cx: Float, cy: Float, r: Float) {
    val eyeY = cy - r * 0.1f
    val eyeSpacing = r * 0.38f
    val eyeR = r * 0.17f

    // Wide alert eyes
    drawCircle(Color(0xFFF59E0B), radius = eyeR, center = Offset(cx - eyeSpacing, eyeY))
    drawCircle(Color(0xFFF59E0B), radius = eyeR, center = Offset(cx + eyeSpacing, eyeY))
    drawCircle(Color(0xFF0F172A), radius = eyeR * 0.45f, center = Offset(cx - eyeSpacing, eyeY))
    drawCircle(Color(0xFF0F172A), radius = eyeR * 0.45f, center = Offset(cx + eyeSpacing, eyeY))
    drawCircle(Color.White, radius = eyeR * 0.2f, center = Offset(cx - eyeSpacing + 2f, eyeY - 2f))
    drawCircle(Color.White, radius = eyeR * 0.2f, center = Offset(cx + eyeSpacing + 2f, eyeY - 2f))

    // Curious alert 'o' mouth
    drawCircle(Color.White, radius = r * 0.07f, center = Offset(cx, eyeY + r * 0.35f), style = Stroke(width = 2.5.dp.toPx()))
}

private fun DrawScope.drawProtectedFace(cx: Float, cy: Float, r: Float) {
    val eyeY = cy - r * 0.1f
    val eyeSpacing = r * 0.38f
    val eyeR = r * 0.13f

    // Steady determined eyes
    drawCircle(Color(0xFF06B6D4), radius = eyeR, center = Offset(cx - eyeSpacing, eyeY))
    drawCircle(Color(0xFF06B6D4), radius = eyeR, center = Offset(cx + eyeSpacing, eyeY))
    drawCircle(Color.White, radius = eyeR * 0.35f, center = Offset(cx - eyeSpacing, eyeY - 1f))
    drawCircle(Color.White, radius = eyeR * 0.35f, center = Offset(cx + eyeSpacing, eyeY - 1f))

    // Confident smile
    val smilePath = Path().apply {
        moveTo(cx - eyeSpacing * 0.5f, eyeY + r * 0.28f)
        quadraticBezierTo(cx, eyeY + r * 0.42f, cx + eyeSpacing * 0.5f, eyeY + r * 0.28f)
    }
    drawPath(smilePath, color = Color.White, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawBlockingFace(cx: Float, cy: Float, r: Float) {
    val eyeY = cy - r * 0.1f
    val eyeSpacing = r * 0.38f
    val strokeWidth = 3.5.dp.toPx()

    // Heroic determined slant brows/eyes
    drawLine(
        color = Color(0xFFF43F5E),
        start = Offset(cx - eyeSpacing - r * 0.14f, eyeY + r * 0.04f),
        end = Offset(cx - eyeSpacing + r * 0.12f, eyeY - r * 0.06f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFF43F5E),
        start = Offset(cx + eyeSpacing + r * 0.14f, eyeY + r * 0.04f),
        end = Offset(cx + eyeSpacing - r * 0.12f, eyeY - r * 0.06f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    // Steadfast resolute mouth
    drawLine(
        color = Color.White,
        start = Offset(cx - eyeSpacing * 0.35f, eyeY + r * 0.32f),
        end = Offset(cx + eyeSpacing * 0.35f, eyeY + r * 0.32f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawStreakFace(cx: Float, cy: Float, r: Float) {
    val eyeY = cy - r * 0.1f
    val eyeSpacing = r * 0.38f

    // Cheerful squinting celebratory eyes (><)
    val leftEye = Path().apply {
        moveTo(cx - eyeSpacing - r * 0.12f, eyeY - r * 0.08f)
        lineTo(cx - eyeSpacing + r * 0.06f, eyeY)
        lineTo(cx - eyeSpacing - r * 0.12f, eyeY + r * 0.08f)
    }
    val rightEye = Path().apply {
        moveTo(cx + eyeSpacing + r * 0.12f, eyeY - r * 0.08f)
        lineTo(cx + eyeSpacing - r * 0.06f, eyeY)
        lineTo(cx + eyeSpacing + r * 0.12f, eyeY + r * 0.08f)
    }
    drawPath(leftEye, color = Color(0xFFF59E0B), style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
    drawPath(rightEye, color = Color(0xFFF59E0B), style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))

    // Broad joyful mouth
    val smilePath = Path().apply {
        moveTo(cx - eyeSpacing * 0.6f, eyeY + r * 0.24f)
        quadraticBezierTo(cx, eyeY + r * 0.48f, cx + eyeSpacing * 0.6f, eyeY + r * 0.24f)
    }
    drawPath(smilePath, color = Color.White, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawRecoveryFace(cx: Float, cy: Float, r: Float) {
    val eyeY = cy - r * 0.06f
    val eyeSpacing = r * 0.38f

    // Soft sleeping eyes (— —)
    drawLine(
        color = Color(0xFF8B5CF6),
        start = Offset(cx - eyeSpacing - r * 0.1f, eyeY),
        end = Offset(cx - eyeSpacing + r * 0.1f, eyeY),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF8B5CF6),
        start = Offset(cx + eyeSpacing - r * 0.1f, eyeY),
        end = Offset(cx + eyeSpacing + r * 0.1f, eyeY),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Gentle calm mouth
    drawLine(
        color = Color.White.copy(alpha = 0.8f),
        start = Offset(cx - eyeSpacing * 0.25f, eyeY + r * 0.28f),
        end = Offset(cx + eyeSpacing * 0.25f, eyeY + r * 0.28f),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawCrown(cx: Float, topY: Float, width: Float) {
    val crownPath = Path().apply {
        moveTo(cx - width / 2f, topY + width * 0.4f)
        lineTo(cx - width / 2f, topY)
        lineTo(cx - width / 4f, topY + width * 0.2f)
        lineTo(cx, topY - width * 0.1f)
        lineTo(cx + width / 4f, topY + width * 0.2f)
        lineTo(cx + width / 2f, topY)
        lineTo(cx + width / 2f, topY + width * 0.4f)
        close()
    }
    drawPath(crownPath, color = Color(0xFFF59E0B))
    // Jewels
    drawCircle(Color(0xFFF43F5E), radius = width * 0.06f, center = Offset(cx, topY - width * 0.05f))
    drawCircle(Color(0xFF06B6D4), radius = width * 0.05f, center = Offset(cx - width * 0.4f, topY + width * 0.08f))
    drawCircle(Color(0xFF06B6D4), radius = width * 0.05f, center = Offset(cx + width * 0.4f, topY + width * 0.08f))
}
