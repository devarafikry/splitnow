package com.devara.splitnow.ui.screen.share

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.data.SplitRepository
import com.devara.splitnow.domain.BillItem
import com.devara.splitnow.domain.Charge
import com.devara.splitnow.domain.Currency
import com.devara.splitnow.domain.PersonShare
import com.devara.splitnow.domain.SplitMode
import com.devara.splitnow.domain.calculateShares
import com.devara.splitnow.domain.formatMoney
import com.devara.splitnow.share.SharePngLauncher
import com.devara.splitnow.ui.components.BrandPill
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.theme.SplitNowTokens
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ShareScreen(
    paymentMethodId: Long? = null,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val flow = koinInject<SplitFlowState>()
    val repo = koinInject<SplitRepository>()
    val shareLauncher = koinInject<SharePngLauncher>()
    val currency = flow.currency
    // Snapshot the input lists so the calculator is stable while sharing.
    val items = remember { flow.items.toList() }
    val charges = remember { flow.charges.toList() }
    val people = remember { flow.people.toList() }
    val shares = remember(items, charges, people, flow.splitMode) {
        calculateShares(items, charges, flow.splitMode, people)
    }
    val total = remember(items, charges) {
        items.sumOf { it.priceCents } + charges.sumOf { it.valueCents }
    }
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val paymentMethods by repo.observePaymentMethods().collectAsState(initial = emptyList())
    val defaultMethod = paymentMethodId?.let { id -> paymentMethods.firstOrNull { it.id == id } }
        ?: paymentMethods.firstOrNull { it.isDefault }
        ?: paymentMethods.firstOrNull()

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(
            title = "Share",
            onBack = onBack,
            trailing = {
                Text(
                    "Done",
                    color = t.accent,
                    fontWeight = FontWeight.W700,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onDone() },
                )
            },
        )
        // On-screen preview: scrollable, so a tall card (many people, many items)
        // remains usable. The capture wrapping the card uses `wrapContentHeight`
        // with `unbounded = true` so the GraphicsLayer records the full natural
        // height of the card — not just what fits in the viewport.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .padding(22.dp)
                    .wrapContentHeight(unbounded = true)
                    .drawWithContent {
                        // Record at the inner Box's measured size (which IS the
                        // full card height because of wrapContentHeight). Even
                        // if the user only sees the top of the card, the layer
                        // holds the entire bitmap.
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    },
            ) {
                ReceiptCard(
                    restaurantName = flow.restaurantName.ifBlank { "Receipt" },
                    shares = shares,
                    items = items,
                    charges = charges,
                    people = people,
                    mode = flow.splitMode,
                    totalCents = total,
                    currency = currency,
                    bankName = defaultMethod?.name,
                    bankAccount = defaultMethod?.account,
                    bankHolder = defaultMethod?.holder,
                )
            }
        }

        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            com.devara.splitnow.ui.components.PrimaryButton(
                label = "Share image",
                onClick = {
                    scope.launch {
                        val image = graphicsLayer.toImageBitmap()
                        val bytes = encodeBitmapToPng(image)
                        // Persist the split (update existing row if it was already
                        // saved on Done/AI-parse). Tracked by persistedSplitId so
                        // re-shares don't duplicate.
                        if (flow.items.isNotEmpty()) {
                            val split = com.devara.splitnow.domain.Split(
                                id = flow.persistedSplitId ?: 0L,
                                restaurantName = flow.restaurantName.ifBlank { "Receipt" },
                                dateMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                                currencyCode = currency.code,
                                totalCents = total,
                                splitMode = flow.splitMode,
                                paymentMethodId = defaultMethod?.id,
                            )
                            flow.persistedSplitId = repo.saveSplit(split, flow.items.toList(), flow.charges.toList())
                        }
                        shareLauncher.shareImage(bytes)
                    }
                },
                background = t.ink,
                height = 64,
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = t.bg) },
            )
        }
    }
}

@Composable
private fun ReceiptCard(
    restaurantName: String,
    shares: List<PersonShare>,
    items: List<BillItem>,
    charges: List<Charge>,
    people: List<String>,
    mode: SplitMode,
    totalCents: Long,
    currency: Currency,
    bankName: String?,
    bankAccount: String?,
    bankHolder: String?,
) {
    val t = SplitNowTokens.colors
    val canonical = people.associateBy { it.lowercase() }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (t.isDark) Color(0xFF1A1A1A) else Color(0xFFFBFAF6))
            .padding(22.dp)
            .width(290.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BrandPill("SplitNow")
        }
        Spacer(Modifier.height(10.dp))
        Text(
            restaurantName,
            color = t.ink,
            fontWeight = FontWeight.W700,
            fontSize = 17.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(2.dp))
        val nowStr = remember { humanDateNow() }
        Text(
            nowStr,
            color = t.ink2,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
        Spacer(Modifier.height(8.dp))

        // Per-person breakdown — header row + every item / shared portion / charge portion.
        shares.forEachIndexed { i, s ->
            PersonReceiptBlock(
                share = s,
                items = items,
                charges = charges,
                people = people,
                canonical = canonical,
                mode = mode,
                currency = currency,
            )
            if (i < shares.lastIndex) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(t.line.copy(alpha = 0.6f)))
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Total", color = t.ink, fontWeight = FontWeight.W700, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            Text(
                "${currency.symbol} ${formatMoney(totalCents, currency)}",
                color = t.ink,
                fontWeight = FontWeight.W700,
                fontSize = 13.sp,
            )
        }
        if (bankName != null && bankAccount != null) {
            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (t.isDark) Color(0x14FFFFFF) else Color(0xFFF4F2EE))
                    .padding(14.dp),
            ) {
                Text("TRANSFER TO", color = t.ink2, fontSize = 10.sp, fontWeight = FontWeight.W700, letterSpacing = 0.6.sp)
                Spacer(Modifier.height(4.dp))
                Text("$bankName · $bankAccount", color = t.ink, fontWeight = FontWeight.W700, fontSize = 14.sp)
                if (bankHolder != null) {
                    Spacer(Modifier.height(1.dp))
                    Text("a.n. $bankHolder", color = t.ink2, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "splitnow.devalab.app",
            color = t.ink3,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.3.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun PersonReceiptBlock(
    share: PersonShare,
    items: List<BillItem>,
    charges: List<Charge>,
    people: List<String>,
    canonical: Map<String, String>,
    mode: SplitMode,
    currency: Currency,
) {
    val t = SplitNowTokens.colors
    val personalItems = items.filter { item ->
        !item.isShared &&
            item.people.any { it.equals(share.name, ignoreCase = true) } &&
            item.people.mapNotNull { canonical[it.lowercase()] }.isNotEmpty()
    }
    val sharedItems = items.filter { item ->
        item.isShared || item.people.mapNotNull { canonical[it.lowercase()] }.isEmpty()
    }
    // Header
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(t.surface2),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                share.name.firstOrNull()?.uppercase() ?: "?",
                color = t.ink,
                fontWeight = FontWeight.W700,
                fontSize = 11.sp,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(share.name, color = t.ink, fontWeight = FontWeight.W700, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(
            "${currency.symbol} ${formatMoney(share.totalCents, currency)}",
            color = t.ink,
            fontWeight = FontWeight.W700,
            fontSize = 14.sp,
        )
    }
    // Items + portions
    val rows = buildList {
        personalItems.forEach { item ->
            val splitAmong = item.people.size.coerceAtLeast(1)
            val portion = item.priceCents / splitAmong
            add(Triple(item.name, if (splitAmong > 1) "÷$splitAmong" else null, portion))
        }
        sharedItems.forEach { item ->
            val portion = item.priceCents / people.size.coerceAtLeast(1)
            add(Triple(item.name, "shared", portion))
        }
        if (mode != SplitMode.SKIP) {
            charges.forEach { charge ->
                val excluded = charge.excluded.any { it.equals(share.name, ignoreCase = true) }
                if (excluded) return@forEach
                val payers = people.filterNot { p -> charge.excluded.any { it.equals(p, ignoreCase = true) } }
                if (payers.isEmpty()) return@forEach
                add(Triple(charge.label, "shared", charge.valueCents / payers.size))
            }
        }
    }
    if (rows.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
    }
    rows.forEach { (label, tag, amount) ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Indent slightly under the person.
            Spacer(Modifier.width(30.dp))
            Text(label, color = t.ink2, fontSize = 12.sp, modifier = Modifier.weight(1f))
            if (tag != null) {
                Text(
                    tag,
                    color = t.ink3,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 6.dp),
                )
            }
            Text(
                formatMoney(amount, currency),
                color = t.ink2,
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
            )
        }
    }
}

private fun humanDateNow(): String {
    val instant = kotlinx.datetime.Clock.System.now()
    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
    val now: kotlinx.datetime.LocalDateTime = instant.toLocalDateTime(tz)
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val hh = now.hour.toString().padStart(2, '0')
    val mm = now.minute.toString().padStart(2, '0')
    return "${now.dayOfMonth} ${months[now.monthNumber-1]} ${now.year} · $hh:$mm"
}

/** Encode a Compose ImageBitmap to PNG bytes. Platform-specific. */
expect suspend fun encodeBitmapToPng(image: androidx.compose.ui.graphics.ImageBitmap): ByteArray
