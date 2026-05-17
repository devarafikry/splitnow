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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.domain.BillItem
import com.devara.splitnow.domain.Currency
import com.devara.splitnow.domain.parseMoneyToCents
import com.devara.splitnow.ui.components.ChipPicker
import com.devara.splitnow.ui.components.dismissKeyboardOnTap
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun EditItemModal(
    itemId: Long?,
    presetAssignedTo: String?,
    onCancel: () -> Unit,
    onSaved: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val flow = koinInject<SplitFlowState>()
    val isNew = itemId == null
    val original = if (!isNew) flow.items.firstOrNull { it.id == itemId } else null

    var name by rememberSaveable { mutableStateOf(original?.name ?: "") }
    var priceText by rememberSaveable {
        mutableStateOf(
            original?.let {
                com.devara.splitnow.domain.formatMoney(it.priceCents, flow.currency)
            } ?: "",
        )
    }
    val people = flow.people.toList() + listOf("Shared")
    val initialIndices = remember {
        if (original != null) {
            if (original.isShared) setOf(people.lastIndex)
            else original.people.mapNotNull { p ->
                val i = people.indexOf(p); if (i >= 0) i else null
            }.toSet()
        } else if (presetAssignedTo != null) {
            val i = people.indexOf(presetAssignedTo); if (i >= 0) setOf(i) else setOf(people.lastIndex)
        } else setOf(people.lastIndex)
    }
    var activeIndices by remember { mutableStateOf(initialIndices.toMutableSet()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding()
            .dismissKeyboardOnTap(),
    ) {
        // Top
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Cancel", color = t.ink2, fontWeight = FontWeight.W500, fontSize = 16.sp, modifier = Modifier.clickable { onCancel() })
            Spacer(Modifier.weight(1f))
            Text(if (isNew) "Add item" else "Edit item", color = t.ink, fontWeight = FontWeight.W700, fontSize = 17.sp)
            Spacer(Modifier.weight(1f))
            Text(
                if (isNew) "Add" else "Save",
                color = t.accent,
                fontWeight = FontWeight.W700,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    val price = parseMoneyToCents(priceText, flow.currency)
                    val assigned = if (activeIndices.contains(people.lastIndex)) BillItem.SHARED
                    else activeIndices.mapNotNull { people.getOrNull(it) }.joinToString(",")
                    if (isNew) {
                        flow.items.add(
                            BillItem(
                                id = (flow.items.maxOfOrNull { it.id } ?: 0L) + 1L,
                                name = name.trim().ifBlank { "Item" },
                                priceCents = price,
                                assignedTo = assigned.ifBlank { BillItem.SHARED },
                            )
                        )
                    } else if (original != null) {
                        val idx = flow.items.indexOf(original)
                        if (idx >= 0) flow.items[idx] = original.copy(
                            name = name.trim().ifBlank { "Item" },
                            priceCents = price,
                            assignedTo = assigned.ifBlank { BillItem.SHARED },
                        )
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
            FormField(t = t, label = "Item") {
                InputBox(name, { name = it }, hint = "e.g. Nasi goreng pedas")
            }
            FormField(t = t, label = "Price") {
                BigPriceBox(priceText, { priceText = it }, prefix = flow.currency.symbol)
            }
            FormField(t = t, label = "Who ordered", hint = "Tap a name to assign. Multiple = split equally.") {
                ChipPicker(
                    options = people,
                    activeIndices = activeIndices,
                    onToggle = { i ->
                        val newSet = activeIndices.toMutableSet()
                        val sharedIdx = people.lastIndex
                        if (i == sharedIdx) {
                            // Toggle SHARED = clear all and set
                            newSet.clear()
                            newSet.add(sharedIdx)
                        } else {
                            newSet.remove(sharedIdx)
                            if (newSet.contains(i)) newSet.remove(i) else newSet.add(i)
                            if (newSet.isEmpty()) newSet.add(sharedIdx)
                        }
                        activeIndices = newSet
                    },
                )
            }
            if (!isNew && original != null) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable {
                            flow.items.remove(original)
                            onSaved()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Delete item", color = t.destructive, fontWeight = FontWeight.W600, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun FormField(
    t: com.devara.splitnow.ui.theme.SplitNowColors,
    label: String,
    hint: String? = null,
    content: @Composable () -> Unit,
) {
    Column {
        Text(label.uppercase(), color = t.ink3, fontSize = 12.sp, fontWeight = FontWeight.W700, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(10.dp))
        content()
        if (hint != null) {
            Spacer(Modifier.height(8.dp))
            Text(hint, color = t.ink2, fontSize = 13.sp)
        }
    }
}

@Composable
private fun InputBox(value: String, onChange: (String) -> Unit, hint: String) {
    val t = SplitNowTokens.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface2)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) Text(hint, color = t.ink3, fontSize = 17.sp)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = t.ink, fontSize = 17.sp, fontWeight = FontWeight.W500),
            cursorBrush = SolidColor(t.accent),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BigPriceBox(value: String, onChange: (String) -> Unit, prefix: String) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface2)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(prefix, color = t.ink2, fontSize = 24.sp, fontWeight = FontWeight.W500)
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) Text("0", color = t.ink3, fontSize = 28.sp, fontWeight = FontWeight.W700)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = TextStyle(color = t.ink, fontSize = 28.sp, fontWeight = FontWeight.W700, letterSpacing = (-0.8).sp),
                cursorBrush = SolidColor(t.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
