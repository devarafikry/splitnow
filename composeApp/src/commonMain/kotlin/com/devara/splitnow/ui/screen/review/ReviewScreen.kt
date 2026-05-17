package com.devara.splitnow.ui.screen.review

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.domain.BillItem
import com.devara.splitnow.domain.Currency
import com.devara.splitnow.domain.SplitMode
import com.devara.splitnow.domain.calculateShares
import com.devara.splitnow.domain.formatMoney
import com.devara.splitnow.ui.components.EditableRow
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.Segmented
import com.devara.splitnow.ui.components.SectionHeader
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    onShare: () -> Unit,
    onEditItem: (Long) -> Unit,
    onAddItem: (String?) -> Unit,
    onAddShared: () -> Unit,
    onEditCharge: (Long) -> Unit,
    onAddCharge: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val flow = koinInject<SplitFlowState>()
    val currency = flow.currency
    val items = flow.items
    val charges = flow.charges
    val people = flow.people
    var mode by remember { mutableStateOf(flow.splitMode) }

    val shares = remember(items.toList(), charges.toList(), people.toList(), mode) {
        calculateShares(items, charges, mode, people)
    }
    val total = remember(items.toList(), charges.toList()) {
        items.sumOf { it.priceCents } + charges.sumOf { it.valueCents }
    }
    flow.totalCents = total
    flow.splitMode = mode

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "Review", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            // Hero
            Text(
                "${flow.restaurantName.ifBlank { "Receipt" }} · ${people.size} ${if (people.size == 1) "person" else "people"}",
                color = t.ink2,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(8.dp))
            val hero = buildAnnotatedString {
                withStyle(SpanStyle(color = t.ink2, fontSize = 28.sp, fontWeight = FontWeight.W500)) {
                    append("${currency.symbol} ")
                }
                withStyle(SpanStyle(color = t.ink, fontSize = 64.sp, fontWeight = FontWeight.W700, letterSpacing = (-3).sp)) {
                    append(formatMoney(total, currency))
                }
            }
            Text(hero)
            Spacer(Modifier.height(8.dp))

            // Per person section
            SectionHeader("Per person", action = "Add item", onAction = { onAddItem(null) })
            shares.forEach { share ->
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
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(t.ink),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(share.name.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.W700, fontSize = 15.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(share.name, color = t.ink, fontSize = 18.sp, fontWeight = FontWeight.W600, modifier = Modifier.weight(1f))
                        val totalAnn = buildAnnotatedString {
                            withStyle(SpanStyle(color = t.ink2, fontSize = 13.sp, fontWeight = FontWeight.W500)) {
                                append("${currency.symbol} ")
                            }
                            withStyle(SpanStyle(color = t.ink, fontSize = 22.sp, fontWeight = FontWeight.W700)) {
                                append(formatMoney(share.totalCents, currency))
                            }
                        }
                        Text(totalAnn)
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
                    Spacer(Modifier.height(6.dp))
                    val personalItems = items.filter { !it.isShared && share.name in it.people }
                    personalItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditItem(item.id) }
                                .padding(vertical = 8.dp),
                        ) {
                            Text(item.name, color = t.ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text(formatMoney(item.priceCents, currency), color = t.ink, fontWeight = FontWeight.W600, fontSize = 14.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.clickable { onAddItem(share.name) }.padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = t.accent, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add item to ${share.name}", color = t.accent, fontWeight = FontWeight.W600, fontSize = 14.sp)
                    }
                }
            }

            // Shared section
            val sharedItems = items.filter { it.isShared }
            SectionHeader("Shared", action = "Add shared", onAction = onAddShared)
            sharedItems.forEachIndexed { i, item ->
                EditableRow(
                    label = item.name,
                    sub = "split ${people.size} ways",
                    valueText = "${currency.symbol} ${formatMoney(item.priceCents, currency)}",
                    onClick = { onEditItem(item.id) },
                    last = i == sharedItems.lastIndex,
                )
            }
            if (sharedItems.isEmpty()) {
                Row(
                    modifier = Modifier.clickable { onAddShared() }.padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = t.accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add shared item", color = t.accent, fontWeight = FontWeight.W600, fontSize = 15.sp)
                }
            }

            // Tax & charges
            SectionHeader("Tax & charges", action = "Add charge", onAction = onAddCharge)
            charges.forEachIndexed { i, charge ->
                EditableRow(
                    label = charge.label,
                    sub = when (charge.type) {
                        com.devara.splitnow.domain.ChargeType.PERCENT -> "${charge.rate}% of subtotal"
                        com.devara.splitnow.domain.ChargeType.FIXED -> "Fixed"
                    },
                    valueText = if (charge.valueCents >= 0) "${currency.symbol} ${formatMoney(charge.valueCents, currency)}"
                        else "−${currency.symbol} ${formatMoney(-charge.valueCents, currency)}",
                    onClick = { onEditCharge(charge.id) },
                    last = i == charges.lastIndex,
                )
            }
            if (charges.isEmpty()) {
                Row(
                    modifier = Modifier.clickable { onAddCharge() }.padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = t.accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add charge", color = t.accent, fontWeight = FontWeight.W600, fontSize = 15.sp)
                }
            }

            // Split mode
            SectionHeader("Split tax & service")
            Segmented(
                options = listOf("Proportional", "Equal", "Skip"),
                activeIndex = when (mode) {
                    SplitMode.PROPORTIONAL -> 0
                    SplitMode.EQUAL -> 1
                    SplitMode.SKIP -> 2
                },
                onSelect = { i ->
                    mode = when (i) { 0 -> SplitMode.PROPORTIONAL; 1 -> SplitMode.EQUAL; else -> SplitMode.SKIP }
                },
            )
            Text(
                when (mode) {
                    SplitMode.PROPORTIONAL -> "Each person pays charges proportional to their own items."
                    SplitMode.EQUAL -> "Charges are split equally across everyone."
                    SplitMode.SKIP -> "Charges aren't added to anyone's share."
                },
                color = t.ink2,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
            Spacer(Modifier.height(120.dp))
        }

        // Footer share CTA
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            com.devara.splitnow.ui.components.PrimaryButton(
                label = "Share split",
                onClick = onShare,
                background = t.ink,
                height = 64,
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = t.bg) },
            )
        }
    }
}
