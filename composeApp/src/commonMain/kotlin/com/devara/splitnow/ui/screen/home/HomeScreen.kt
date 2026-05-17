package com.devara.splitnow.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.data.SplitRepository
import com.devara.splitnow.domain.Currency
import com.devara.splitnow.domain.Split
import com.devara.splitnow.domain.formatMoney
import com.devara.splitnow.ui.components.BrandPill
import com.devara.splitnow.ui.theme.SplitNowTokens
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    onSettings: () -> Unit,
    onNewSplit: () -> Unit,
    onOpenSplit: (Long) -> Unit,
) {
    val t = SplitNowTokens.colors
    val repo = koinInject<SplitRepository>()
    val splits by repo.observeSplits().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var monthCount by remember { mutableStateOf(0) }
    var monthTotal by remember { mutableStateOf(0L) }
    val latest = splits.firstOrNull()
    val currency = Currency.byCode(latest?.currencyCode ?: "IDR")

    LaunchedEffect(splits.size) {
        // Sum splits from the start of this month — naive: take last 30 days.
        val since = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - 30L * 24 * 60 * 60 * 1000
        monthCount = repo.countSplitsSince(since)
        monthTotal = repo.sumSplitsSince(since)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        // Top: SplitNow + settings gear
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("SplitNow", color = t.ink, fontWeight = FontWeight.W800, fontSize = 19.sp, letterSpacing = (-0.5).sp)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(t.surface2)
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = t.ink)
            }
        }

        // Latest split card
        if (latest != null) {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(t.surface2)
                    .clickable { onOpenSplit(latest.id) }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("LATEST", color = t.ink3, fontSize = 10.sp, fontWeight = FontWeight.W700, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(latest.restaurantName, color = t.ink, fontWeight = FontWeight.W600, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    val txt = buildAnnotatedString {
                        withStyle(SpanStyle(color = t.ink2, fontSize = 11.sp, fontWeight = FontWeight.W500)) {
                            append("${currency.symbol} ")
                        }
                        withStyle(SpanStyle(color = t.ink, fontSize = 16.sp, fontWeight = FontWeight.W700)) {
                            append(formatMoney(latest.totalCents, currency))
                        }
                    }
                    Text(txt)
                    Text(relativeDate(latest.dateMs), color = t.ink2, fontSize = 11.sp)
                }
            }
        }

        // Receipt stack illustration
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            ReceiptStack()
        }

        // Stat strip
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 8.dp, bottom = 22.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            val left = buildAnnotatedString {
                withStyle(SpanStyle(color = t.ink, fontSize = 22.sp, fontWeight = FontWeight.W700, letterSpacing = (-0.6).sp)) {
                    append(monthCount.toString())
                }
                append(" ")
                withStyle(SpanStyle(color = t.ink2, fontSize = 13.sp)) { append("splits this month") }
            }
            Text(left)
            Spacer(Modifier.weight(1f))
            val right = buildAnnotatedString {
                withStyle(SpanStyle(color = t.ink2, fontSize = 12.sp, fontWeight = FontWeight.W500)) {
                    append("${currency.symbol} ")
                }
                withStyle(SpanStyle(color = t.ink, fontSize = 22.sp, fontWeight = FontWeight.W700, letterSpacing = (-0.6).sp)) {
                    append(formatMoney(monthTotal, currency))
                }
            }
            Text(right)
        }

        // New split CTA — coral pill in thumb-reach zone
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(t.accent)
                .clickable { onNewSplit() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Text("New split", color = Color.White, fontWeight = FontWeight.W700, fontSize = 18.sp)
        }
    }
}

@Composable
private fun ReceiptStack() {
    val t = SplitNowTokens.colors
    Box(modifier = Modifier.size(280.dp, 320.dp), contentAlignment = Alignment.Center) {
        // Three tilted receipt cards stacked
        FakeReceipt(rotation = -10f, offsetX = (-64).dp, offsetY = (-28).dp, scale = 0.78f, brand = false, lines = 3)
        FakeReceipt(rotation = 6f, offsetX = 30.dp, offsetY = 4.dp, scale = 0.88f, brand = false, lines = 4)
        FakeReceipt(rotation = -3f, offsetX = (-12).dp, offsetY = 38.dp, scale = 1.0f, brand = true, lines = 5)
    }
}

@Composable
private fun FakeReceipt(
    rotation: Float,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
    scale: Float,
    brand: Boolean,
    lines: Int,
) {
    val t = SplitNowTokens.colors
    val widthDp = (180 * scale).dp
    val heightDp = (240 * scale).dp
    Box(
        modifier = Modifier
            .size(widthDp, heightDp)
            .offset(x = offsetX, y = offsetY)
            .rotate(rotation)
            .clip(RoundedCornerShape(14.dp))
            .background(if (brand) t.surface else t.surface2)
            .then(if (brand) Modifier.border(1.dp, t.line, RoundedCornerShape(14.dp)) else Modifier)
            .padding(16.dp),
    ) {
        Column {
            if (brand) {
                BrandPill("SplitNow")
                Spacer(Modifier.height(8.dp))
            } else {
                Box(
                    modifier = Modifier.width(40.dp).height(6.dp).clip(RoundedCornerShape(3.dp))
                        .background(t.ink.copy(alpha = 0.4f)),
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(if (brand) 0.85f else 0.7f).height(8.dp)
                    .clip(RoundedCornerShape(2.dp)).background(t.ink.copy(alpha = 0.85f)),
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.fillMaxWidth(0.45f).height(5.dp)
                    .clip(RoundedCornerShape(2.dp)).background(t.ink.copy(alpha = 0.35f)),
            )
            Spacer(Modifier.height(10.dp))
            repeat(lines) { idx ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    val widths = listOf(0.55f, 0.7f, 0.48f, 0.62f, 0.5f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(widths.getOrElse(idx) { 0.6f })
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(t.ink.copy(alpha = 0.55f)),
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(t.ink.copy(alpha = 0.6f)),
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

private fun relativeDate(ms: Long): String {
    val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val diff = now - ms
    val mins = diff / 60_000
    val hours = mins / 60
    val days = hours / 24
    return when {
        mins < 1 -> "just now"
        mins < 60 -> "${mins}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "earlier"
    }
}

