package com.devara.splitnow.ui.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.ui.components.PrimaryButton
import com.devara.splitnow.ui.theme.SplitNowTokens

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val t = SplitNowTokens.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(t.brandBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 28.dp, end = 28.dp, top = 18.dp, bottom = 24.dp),
        ) {
            // Corner mark — wordmark + dots
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "SplitNow",
                    color = t.brandInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W800,
                    letterSpacing = (-0.4).sp,
                )
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf(0, 1, 2).forEach { i ->
                        Box(
                            modifier = Modifier
                                .width(if (i == 1) 18.dp else 5.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(t.brandInk.copy(alpha = if (i == 1) 1f else 0.35f)),
                        )
                        if (i < 2) Spacer(Modifier.width(5.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Hero typographic moment
            val hero = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.W800)) { append("Split\nthe bill,\n") }
                withStyle(SpanStyle(fontWeight = FontWeight.W500, fontStyle = FontStyle.Italic)) { append("keep it\nsimple.") }
            }
            Text(
                hero,
                color = t.brandInk,
                fontSize = 72.sp,
                lineHeight = 70.sp,
                letterSpacing = (-3).sp,
            )

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                label = "Get started",
                onClick = onGetStarted,
                background = Color(0xFF0F0E0C),
                foreground = t.brandInk,
            )
        }
    }
}
