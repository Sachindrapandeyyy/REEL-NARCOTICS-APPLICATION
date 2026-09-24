package com.zenith.focus.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-precision 5x7 Dot-Matrix LED / LCD display renderer.
 * Renders characters out of a grid of circular dots with authentic faint inactive matrix texture.
 */
object DotMatrixFont {
    // 5 columns x 7 rows. Each integer represents a row (5 bits, bit 4 is leftmost).
    val GLYPHS: Map<Char, IntArray> = mapOf(
        '0' to intArrayOf(
            0b01110,
            0b10001,
            0b10011,
            0b10101,
            0b11001,
            0b10001,
            0b01110
        ),
        '1' to intArrayOf(
            0b00100,
            0b01100,
            0b00100,
            0b00100,
            0b00100,
            0b00100,
            0b01110
        ),
        '2' to intArrayOf(
            0b01110,
            0b10001,
            0b00001,
            0b00010,
            0b00100,
            0b01000,
            0b11111
        ),
        '3' to intArrayOf(
            0b11110,
            0b00001,
            0b00001,
            0b01110,
            0b00001,
            0b00001,
            0b11110
        ),
        '4' to intArrayOf(
            0b00010,
            0b00110,
            0b01010,
            0b10010,
            0b11111,
            0b00010,
            0b00010
        ),
        '5' to intArrayOf(
            0b11111,
            0b10000,
            0b11110,
            0b00001,
            0b00001,
            0b10001,
            0b01110
        ),
        '6' to intArrayOf(
            0b01110,
            0b10000,
            0b10000,
            0b11110,
            0b10001,
            0b10001,
            0b01110
        ),
        '7' to intArrayOf(
            0b11111,
            0b00001,
            0b00010,
            0b00100,
            0b01000,
            0b01000,
            0b01000
        ),
        '8' to intArrayOf(
            0b01110,
            0b10001,
            0b10001,
            0b01110,
            0b10001,
            0b10001,
            0b01110
        ),
        '9' to intArrayOf(
            0b01110,
            0b10001,
            0b10001,
            0b01111,
            0b00001,
            0b00001,
            0b01110
        ),
        '.' to intArrayOf(
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00110,
            0b00110
        ),
        ',' to intArrayOf(
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00110,
            0b00110,
            0b00100
        ),
        '%' to intArrayOf(
            0b11001,
            0b11010,
            0b00100,
            0b01000,
            0b01011,
            0b10011,
            0b00000
        ),
        '+' to intArrayOf(
            0b00000,
            0b00100,
            0b00100,
            0b11111,
            0b00100,
            0b00100,
            0b00000
        ),
        '-' to intArrayOf(
            0b00000,
            0b00000,
            0b00000,
            0b11111,
            0b00000,
            0b00000,
            0b00000
        ),
        'm' to intArrayOf(
            0b00000,
            0b00000,
            0b11011,
            0b10101,
            0b10101,
            0b10001,
            0b10001
        ),
        'g' to intArrayOf(
            0b00000,
            0b00000,
            0b01111,
            0b10001,
            0b01111,
            0b00001,
            0b01110
        ),
        'o' to intArrayOf(
            0b00000,
            0b00000,
            0b01110,
            0b10001,
            0b10001,
            0b10001,
            0b01110
        ),
        'z' to intArrayOf(
            0b00000,
            0b00000,
            0b11111,
            0b00010,
            0b00100,
            0b01000,
            0b11111
        ),
        'k' to intArrayOf(
            0b10000,
            0b10010,
            0b10100,
            0b11000,
            0b10100,
            0b10010,
            0b10001
        ),
        'h' to intArrayOf(
            0b10000,
            0b10000,
            0b10110,
            0b11001,
            0b10001,
            0b10001,
            0b10001
        ),
        ' ' to intArrayOf(
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00000
        )
    )
}

@Composable
fun DotMatrixNumber(
    text: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 2.6.dp,
    dotSpacing: Dp = 1.0.dp,
    charSpacing: Dp = 2.5.dp,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.08f),
    showInactiveGrid: Boolean = true
) {
    val density = LocalDensity.current
    val dotSizePx = with(density) { dotSize.toPx() }
    val dotSpacingPx = with(density) { dotSpacing.toPx() }
    val charSpacingPx = with(density) { charSpacing.toPx() }
    val dotRadius = dotSizePx / 2f
    val step = dotSizePx + dotSpacingPx

    val colsPerChar = 5
    val rows = 7

    val charWidthPx = (colsPerChar - 1) * step + dotSizePx
    val charHeightPx = (rows - 1) * step + dotSizePx
    val totalWidthPx = text.length * charWidthPx + (text.length - 1).coerceAtLeast(0) * charSpacingPx

    val totalWidthDp = with(density) { totalWidthPx.toDp() }
    val totalHeightDp = with(density) { charHeightPx.toDp() }

    Canvas(
        modifier = modifier
            .width(totalWidthDp)
            .height(totalHeightDp)
    ) {
        var startX = 0f

        text.forEach { ch ->
            val lower = ch.lowercaseChar()
            val pattern = DotMatrixFont.GLYPHS[lower] ?: DotMatrixFont.GLYPHS['0']!!

            for (r in 0 until rows) {
                val rowBits = pattern[r]
                val cy = r * step + dotRadius

                for (c in 0 until colsPerChar) {
                    val cx = startX + c * step + dotRadius
                    val isBitSet = (rowBits and (1 shl (colsPerChar - 1 - c))) != 0

                    if (isBitSet) {
                        drawCircle(
                            color = activeColor,
                            radius = dotRadius,
                            center = Offset(cx, cy)
                        )
                    } else if (showInactiveGrid) {
                        drawCircle(
                            color = inactiveColor,
                            radius = dotRadius * 0.85f,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }

            startX += charWidthPx + charSpacingPx
        }
    }
}
