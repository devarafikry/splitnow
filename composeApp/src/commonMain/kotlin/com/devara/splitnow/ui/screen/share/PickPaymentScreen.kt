package com.devara.splitnow.ui.screen.share

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.data.SplitRepository
import com.devara.splitnow.domain.PaymentKind
import com.devara.splitnow.domain.PaymentMethod
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.PrimaryButton
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun PickPaymentScreen(
    onBack: () -> Unit,
    onContinue: (paymentId: Long?) -> Unit,
    onAddNew: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val repo = koinInject<SplitRepository>()
    val flow = koinInject<SplitFlowState>()
    val methods by repo.observePaymentMethods().collectAsState(initial = emptyList())

    // Pre-select existing default or previously chosen.
    var selectedId by remember(methods.size) {
        mutableStateOf(
            flow.let { f ->
                f.run {
                    methods.firstOrNull { m -> m.isDefault }?.id
                        ?: methods.firstOrNull()?.id
                }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "Payment for share", onBack = onBack, backLabel = "Review")
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                "Pick a payment method",
                color = t.ink,
                fontSize = 28.sp,
                fontWeight = FontWeight.W700,
                letterSpacing = (-0.8).sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Friends use this to pay you back. It's embedded on the share image.",
                color = t.ink2,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(20.dp))

            if (methods.isEmpty()) {
                EmptyState(onAddNew = onAddNew)
            } else {
                methods.forEachIndexed { i, m ->
                    PaymentChoice(
                        method = m,
                        selected = selectedId == m.id,
                        onClick = { selectedId = m.id },
                        last = i == methods.lastIndex,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.5.dp, t.line, RoundedCornerShape(26.dp))
                        .clickable { onAddNew() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = t.accent)
                    Spacer(Modifier.width(6.dp))
                    Text("Add new method", color = t.accent, fontWeight = FontWeight.W600, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(120.dp))
        }
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            PrimaryButton(
                label = if (methods.isEmpty()) "Skip — share without payment" else "Continue",
                onClick = { onContinue(selectedId) },
                background = t.ink,
                height = 60,
            )
        }
    }
}

@Composable
private fun EmptyState(onAddNew: () -> Unit) {
    val t = SplitNowTokens.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(t.surface2)
            .padding(24.dp),
    ) {
        Text("No payment methods yet", color = t.ink, fontSize = 18.sp, fontWeight = FontWeight.W700)
        Spacer(Modifier.height(6.dp))
        Text(
            "Add a bank account, e-wallet, or QRIS so friends can pay you back when you share the split.",
            color = t.ink2,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(t.accent)
                .clickable { onAddNew() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = t.accentInk)
            Spacer(Modifier.width(6.dp))
            Text("Add payment method", color = t.accentInk, fontWeight = FontWeight.W700, fontSize = 15.sp)
        }
    }
}

@Composable
private fun PaymentChoice(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
    last: Boolean,
) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (last) 0.dp else 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) t.accentSoft else t.surface2)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(if (method.kind == PaymentKind.BANK) 12.dp else 22.dp))
                .background(t.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                method.name.take(if (method.kind == PaymentKind.BANK) 2 else 1).uppercase(),
                color = t.ink,
                fontWeight = FontWeight.W700,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(method.name, color = t.ink, fontSize = 16.sp, fontWeight = FontWeight.W600)
            Text(
                "${method.account}${if (method.holder.isNotBlank()) " · ${method.holder}" else ""}",
                color = t.ink2,
                fontSize = 13.sp,
            )
        }
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = t.accent)
        }
    }
}
