package com.devara.splitnow.ui.screen.history

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.devara.splitnow.domain.BillItem
import com.devara.splitnow.domain.Charge
import com.devara.splitnow.domain.Currency
import com.devara.splitnow.domain.PersonShare
import com.devara.splitnow.domain.Split
import com.devara.splitnow.domain.SplitMode
import com.devara.splitnow.domain.calculateShares
import com.devara.splitnow.domain.formatMoney
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.PrimaryButton
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun HistoryDetailScreen(
    splitId: Long,
    onBack: () -> Unit,
    onShare: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val repo = koinInject<SplitRepository>()
    val flow = koinInject<SplitFlowState>()
    var data by remember { mutableStateOf<Triple<Split, List<BillItem>, List<Charge>>?>(null) }
    LaunchedEffect(splitId) { data = repo.loadSplitDetail(splitId) }

    val detail = data
    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "Detail", onBack = onBack)
        if (detail == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = t.ink2)
            }
            return@Column
        }
        val (split, items, charges) = detail
        val currency = Currency.byCode(split.currencyCode)
        val people = items.flatMap { it.people }.distinct().ifEmpty { listOf("You") }
        val shares = calculateShares(items, charges, split.splitMode, people)
        val total = split.totalCents

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        ) {
            Text(
                "${split.restaurantName} · ${people.size} ${if (people.size == 1) "person" else "people"}",
                color = t.ink2,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = t.ink2, fontSize = 28.sp, fontWeight = FontWeight.W500)) { append("${currency.symbol} ") }
                    withStyle(SpanStyle(color = t.ink, fontSize = 56.sp, fontWeight = FontWeight.W700, letterSpacing = (-2.4).sp)) {
                        append(formatMoney(total, currency))
                    }
                },
            )
            Spacer(Modifier.height(20.dp))
            shares.forEach { share ->
                PersonCard(
                    share = share,
                    items = items,
                    charges = charges,
                    people = people,
                    mode = split.splitMode,
                    currency = currency,
                )
            }
            Spacer(Modifier.height(100.dp))
        }

        Box(modifier = Modifier.padding(24.dp)) {
            PrimaryButton(
                label = "Share split",
                onClick = {
                    flow.reset()
                    flow.restaurantName = split.restaurantName
                    flow.currency = currency
                    flow.splitMode = split.splitMode
                    flow.items.addAll(items)
                    flow.charges.addAll(charges)
                    flow.people.addAll(people)
                    flow.totalCents = total
                    flow.persistedSplitId = split.id
                    onShare()
                },
                background = t.ink,
                height = 64,
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = t.bg) },
            )
        }
    }
}

@Composable
private fun PersonCard(
    share: PersonShare,
    items: List<BillItem>,
    charges: List<Charge>,
    people: List<String>,
    mode: SplitMode,
    currency: Currency,
) {
    val t = SplitNowTokens.colors
    val canonical = people.associateBy { it.lowercase() }
    val personalItems = items.filter { item ->
        !item.isShared &&
            item.people.any { it.equals(share.name, ignoreCase = true) } &&
            item.people.mapNotNull { canonical[it.lowercase()] }.isNotEmpty()
    }
    val sharedItems = items.filter { item ->
        item.isShared || item.people.mapNotNull { canonical[it.lowercase()] }.isEmpty()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(t.surface2)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(t.ink),
                contentAlignment = Alignment.Center,
            ) {
                Text(share.name.firstOrNull()?.uppercase() ?: "?", color = t.bg, fontWeight = FontWeight.W700, fontSize = 15.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(share.name, color = t.ink, fontSize = 18.sp, fontWeight = FontWeight.W600, modifier = Modifier.weight(1f))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = t.ink2, fontSize = 13.sp, fontWeight = FontWeight.W500)) { append("${currency.symbol} ") }
                    withStyle(SpanStyle(color = t.ink, fontSize = 22.sp, fontWeight = FontWeight.W700)) {
                        append(formatMoney(share.totalCents, currency))
                    }
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
        Spacer(Modifier.height(6.dp))

        // Personal items
        personalItems.forEach { item ->
            val splitAmong = item.people.size.coerceAtLeast(1)
            val portion = item.priceCents / splitAmong
            DetailRow(
                label = item.name,
                sub = if (splitAmong > 1) "split $splitAmong ways · ${currency.symbol} ${formatMoney(item.priceCents, currency)} total" else null,
                amount = portion,
                currency = currency,
            )
        }
        // Shared item portions
        sharedItems.forEach { item ->
            val portion = item.priceCents / people.size.coerceAtLeast(1)
            DetailRow(
                label = item.name,
                sub = "shared · split ${people.size} ways",
                amount = portion,
                currency = currency,
            )
        }
        // Charge portions (only when EQUAL — SKIP means host absorbed them)
        if (mode != SplitMode.SKIP) {
            charges.forEach { charge ->
                val excluded = charge.excluded.any { it.equals(share.name, ignoreCase = true) }
                if (excluded) return@forEach
                val payers = people.filterNot { p -> charge.excluded.any { it.equals(p, ignoreCase = true) } }
                if (payers.isEmpty()) return@forEach
                val portion = charge.valueCents / payers.size
                DetailRow(
                    label = charge.label,
                    sub = "shared · split ${payers.size} ways",
                    amount = portion,
                    currency = currency,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, sub: String?, amount: Long, currency: Currency) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = t.ink, fontSize = 14.sp)
            if (sub != null) {
                Text(sub, color = t.ink2, fontSize = 11.sp)
            }
        }
        Text(formatMoney(amount, currency), color = t.ink, fontWeight = FontWeight.W600, fontSize = 14.sp)
    }
}
