package com.devara.splitnow.ui.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.theme.SplitNowTokens
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun HistoryListScreen(
    onBack: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val t = SplitNowTokens.colors
    val repo = koinInject<SplitRepository>()
    val splits by repo.observeSplits().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "History", onBack = onBack)
        if (splits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No splits yet", color = t.ink, fontWeight = FontWeight.W700, fontSize = 22.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Your past splits will show up here.", color = t.ink2, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 4.dp, bottom = 24.dp),
            ) {
                items(splits, key = { it.id }) { split ->
                    HistoryRow(split = split, onClick = { onOpen(split.id) })
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(split: Split, onClick: () -> Unit) {
    val t = SplitNowTokens.colors
    val currency = Currency.byCode(split.currencyCode)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(t.surface2)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(t.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                split.restaurantName.firstOrNull()?.uppercase() ?: "?",
                color = t.ink,
                fontWeight = FontWeight.W700,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.size(width = 12.dp, height = 0.dp).let { it })
        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(split.restaurantName, color = t.ink, fontWeight = FontWeight.W600, fontSize = 15.sp)
            Text(humanDate(split.dateMs), color = t.ink2, fontSize = 12.sp)
        }
        val ann = buildAnnotatedString {
            withStyle(SpanStyle(color = t.ink2, fontSize = 12.sp, fontWeight = FontWeight.W500)) {
                append("${currency.symbol} ")
            }
            withStyle(SpanStyle(color = t.ink, fontSize = 16.sp, fontWeight = FontWeight.W700)) {
                append(formatMoney(split.totalCents, currency))
            }
        }
        Text(ann)
    }
}

private fun humanDate(ms: Long): String {
    val instant = kotlinx.datetime.Clock.System.now()
    val now = instant.toEpochMilliseconds()
    val diff = now - ms
    val mins = diff / 60_000
    val hours = mins / 60
    val days = hours / 24
    return when {
        mins < 1 -> "just now"
        mins < 60 -> "${mins}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val ldt = kotlinx.datetime.Instant.fromEpochMilliseconds(ms).toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            "${ldt.dayOfMonth} ${months[ldt.monthNumber - 1]}"
        }
    }
}
