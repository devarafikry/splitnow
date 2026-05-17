package com.devara.splitnow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devara.splitnow.ui.theme.SplitNowTokens

/**
 * The SplitNow brand mark: a stylized `%` — two dots + diagonal slash, tilted +35°.
 *
 * Renders inside a 100×100 logical viewport (matching the design SVG), then
 * scales to whatever size you pass. Single color (defaults to brand coral).
 */
@Composable
fun SplitNowMark(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = SplitNowTokens.colors.accent,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val scale = s / 100f
        fun x(v: Float) = v * scale
        fun y(v: Float) = v * scale
        // Top-left circle
        drawCircle(color = color, radius = 9f * scale, center = Offset(x(28f), y(32f)))
        // Diagonal slash
        drawLine(
            color = color,
            start = Offset(x(22f), y(78f)),
            end = Offset(x(78f), y(22f)),
            strokeWidth = 22f * scale,
            cap = StrokeCap.Round,
        )
        // Bottom-right circle
        drawCircle(color = color, radius = 9f * scale, center = Offset(x(72f), y(68f)))
    }
}
