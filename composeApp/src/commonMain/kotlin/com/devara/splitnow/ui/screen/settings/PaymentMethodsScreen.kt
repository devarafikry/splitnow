package com.devara.splitnow.ui.screen.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.data.SplitRepository
import com.devara.splitnow.domain.PaymentKind
import com.devara.splitnow.domain.PaymentMethod
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.Pill
import com.devara.splitnow.ui.components.SectionHeader
import com.devara.splitnow.ui.theme.SplitNowTokens
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val t = SplitNowTokens.colors
    val repo = koinInject<SplitRepository>()
    val methods by repo.observePaymentMethods().collectAsState(initial = emptyList())
    val banks = methods.filter { it.kind == PaymentKind.BANK }
    val wallets = methods.filter { it.kind == PaymentKind.EWALLET }
    val qris = methods.filter { it.kind == PaymentKind.QRIS }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "Payment", onBack = onBack, backLabel = "Settings")
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text("How friends pay you", color = t.ink, fontSize = 32.sp, fontWeight = FontWeight.W700, letterSpacing = (-1.2).sp)
            Spacer(Modifier.height(8.dp))
            Text("Attach a method to every shared bill.", color = t.ink2, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))

            if (banks.isNotEmpty()) {
                SectionHeader("Banks")
                banks.forEachIndexed { i, m -> MethodRow(m, last = i == banks.lastIndex, onClick = { onEdit(m.id) }) }
            }
            if (wallets.isNotEmpty()) {
                SectionHeader("E-wallets")
                wallets.forEachIndexed { i, m -> MethodRow(m, last = i == wallets.lastIndex, onClick = { onEdit(m.id) }) }
            }
            if (qris.isNotEmpty()) {
                SectionHeader("QRIS")
                qris.forEachIndexed { i, m -> MethodRow(m, last = i == qris.lastIndex, onClick = { onEdit(m.id) }) }
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(width = 1.5.dp, color = t.line, shape = RoundedCornerShape(28.dp))
                    .clickable { onAdd() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = t.accent)
                Spacer(Modifier.width(8.dp))
                Text("Add method", color = t.accent, fontWeight = FontWeight.W600, fontSize = 16.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun MethodRow(m: PaymentMethod, last: Boolean, onClick: () -> Unit) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(if (m.kind == PaymentKind.BANK) 12.dp else 22.dp))
                .background(t.surface2),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                m.name.take(if (m.kind == PaymentKind.BANK) 2 else 1).uppercase(),
                color = t.ink,
                fontWeight = FontWeight.W700,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(m.name, color = t.ink, fontSize = 17.sp, fontWeight = FontWeight.W600)
            Text(
                "${m.account}${if (m.holder.isNotBlank()) " · ${m.holder}" else ""}",
                color = t.ink2,
                fontSize = 14.sp,
            )
        }
        if (m.isDefault) Pill("Default", soft = true)
    }
    if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
}
