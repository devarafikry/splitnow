package com.devara.splitnow.ui.screen.ocr

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.PrimaryButton
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

/**
 * Quick OCR review — show raw recognized lines so user can confirm before
 * describing. The full per-item edit happens on the Review screen post-AI.
 */
@Composable
fun OCRReviewScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val flow = koinInject<SplitFlowState>()
    val lines = flow.ocrText.split('\n').map { it.trim() }.filter { it.length >= 2 }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "Scanned", onBack = onBack, backLabel = "Retake")
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                "Detected ${lines.size} lines from the receipt. Add who-got-what next.",
                color = t.ink2,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(24.dp))

            // Hero — line count as the big number
            val hero = buildAnnotatedString {
                withStyle(SpanStyle(color = t.ink, fontSize = 64.sp, fontWeight = FontWeight.W700, letterSpacing = (-3).sp)) {
                    append(lines.size.toString())
                }
                append(" ")
                withStyle(SpanStyle(color = t.ink2, fontSize = 22.sp, fontWeight = FontWeight.W500)) {
                    append("lines")
                }
            }
            Text(hero)

            Spacer(Modifier.height(20.dp))

            // Raw lines preview
            lines.forEach { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(line, color = t.ink, fontSize = 15.sp, modifier = Modifier.weight(1f))
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
            }
            Spacer(Modifier.height(20.dp))
        }

        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            PrimaryButton(label = "Continue", onClick = onContinue, background = t.ink)
        }
    }
}
