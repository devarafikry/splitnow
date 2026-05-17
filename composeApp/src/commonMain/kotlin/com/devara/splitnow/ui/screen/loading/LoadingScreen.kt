package com.devara.splitnow.ui.screen.loading

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.ui.components.SplitNowMark
import com.devara.splitnow.ui.theme.SplitNowTokens

@Composable
fun LoadingScreen() {
    val t = SplitNowTokens.colors
    Box(modifier = Modifier.fillMaxSize().background(t.bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val transition = rememberInfiniteTransition()
            val pulse by transition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1600),
                    repeatMode = RepeatMode.Restart,
                ),
            )
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(t.accent.copy(alpha = 0.3f)),
                )
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(t.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    SplitNowMark(size = 56.dp, color = t.accentInk)
                }
            }
            Spacer(Modifier.height(44.dp))
            Text(
                "Splitting\nyour bill",
                color = t.ink,
                fontSize = 34.sp,
                fontWeight = FontWeight.W700,
                lineHeight = 36.sp,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            Text("Usually takes 2–3 seconds.", color = t.ink2, fontSize = 16.sp)
        }
    }
}
