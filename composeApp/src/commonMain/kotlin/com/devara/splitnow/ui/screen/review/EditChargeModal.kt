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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.domain.Charge
import com.devara.splitnow.domain.ChargeType
import com.devara.splitnow.domain.parseMoneyToCents
import com.devara.splitnow.ui.components.Segmented
import com.devara.splitnow.ui.components.dismissKeyboardOnTap
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun EditChargeModal(
    chargeId: Long?,
    onCancel: () -> Unit,
    onSaved: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val flow = koinInject<SplitFlowState>()
    val isNew = chargeId == null
    val original = if (!isNew) flow.charges.firstOrNull { it.id == chargeId } else null
    var label by rememberSaveable { mutableStateOf(original?.label ?: "Tax") }
    var typeIdx by rememberSaveable { mutableStateOf(if (original?.type == ChargeType.FIXED) 1 else 0) }
    var rateText by rememberSaveable { mutableStateOf(original?.rate?.toString() ?: "10") }
    var valueText by rememberSaveable {
        mutableStateOf(original?.let { com.devara.splitnow.domain.formatMoney(it.valueCents, flow.currency) } ?: "")
    }

    val subtotal = flow.items.sumOf { it.priceCents }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding()
            .dismissKeyboardOnTap(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Cancel", color = t.ink2, fontWeight = FontWeight.W500, fontSize = 16.sp, modifier = Modifier.clickable { onCancel() })
            Spacer(Modifier.weight(1f))
            Text(if (isNew) "Add charge" else "Edit charge", color = t.ink, fontWeight = FontWeight.W700, fontSize = 17.sp)
            Spacer(Modifier.weight(1f))
            Text(
                if (isNew) "Add" else "Save",
                color = t.accent,
                fontWeight = FontWeight.W700,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    val type = if (typeIdx == 0) ChargeType.PERCENT else ChargeType.FIXED
                    val rate = rateText.toDoubleOrNull() ?: 0.0
                    val v = if (type == ChargeType.PERCENT) ((subtotal * rate) / 100.0).toLong() else parseMoneyToCents(valueText, flow.currency)
                    val newCharge = Charge(
                        id = original?.id ?: ((flow.charges.maxOfOrNull { it.id } ?: 0L) + 1L),
                        label = label.trim().ifBlank { "Charge" },
                        type = type,
                        rate = rate,
                        valueCents = v,
                    )
                    if (isNew) flow.charges.add(newCharge)
                    else if (original != null) {
                        val idx = flow.charges.indexOf(original)
                        if (idx >= 0) flow.charges[idx] = newCharge
                    }
                    onSaved()
                },
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column {
                FieldLabel("Label")
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(t.surface2).padding(horizontal = 18.dp, vertical = 14.dp),
                ) {
                    BasicTextField(
                        value = label,
                        onValueChange = { label = it },
                        textStyle = TextStyle(color = t.ink, fontSize = 17.sp, fontWeight = FontWeight.W500),
                        cursorBrush = SolidColor(t.accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Column {
                FieldLabel("Type")
                Segmented(options = listOf("Percent", "Fixed amount"), activeIndex = typeIdx, onSelect = { typeIdx = it })
            }
            if (typeIdx == 0) {
                Column {
                    FieldLabel("Rate")
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(t.surface2).padding(horizontal = 18.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("%", color = t.ink2, fontSize = 24.sp, fontWeight = FontWeight.W500)
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = rateText,
                            onValueChange = { rateText = it },
                            textStyle = TextStyle(color = t.ink, fontSize = 28.sp, fontWeight = FontWeight.W700),
                            cursorBrush = SolidColor(t.accent),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Applied proportionally to each person's subtotal.", color = t.ink2, fontSize = 13.sp)
                }
            } else {
                Column {
                    FieldLabel("Amount")
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(t.surface2).padding(horizontal = 18.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(flow.currency.symbol, color = t.ink2, fontSize = 24.sp, fontWeight = FontWeight.W500)
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = valueText,
                            onValueChange = { valueText = it },
                            textStyle = TextStyle(color = t.ink, fontSize = 28.sp, fontWeight = FontWeight.W700),
                            cursorBrush = SolidColor(t.accent),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            if (!isNew && original != null) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable {
                            flow.charges.remove(original)
                            onSaved()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Remove charge", color = t.destructive, fontWeight = FontWeight.W600, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(label: String) {
    val t = SplitNowTokens.colors
    Text(label.uppercase(), color = t.ink2, fontSize = 12.sp, fontWeight = FontWeight.W700, letterSpacing = 1.2.sp, modifier = Modifier.padding(bottom = 10.dp))
}
