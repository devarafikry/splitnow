package com.devara.splitnow.ui.screen.settings

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.devara.splitnow.data.SplitRepository
import com.devara.splitnow.domain.PaymentKind
import com.devara.splitnow.domain.PaymentMethod
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.PrimaryButton
import com.devara.splitnow.ui.components.Segmented
import com.devara.splitnow.ui.components.dismissKeyboardOnTap
import com.devara.splitnow.ui.theme.SplitNowTokens
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun EditPaymentScreen(
    methodId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val repo = koinInject<SplitRepository>()
    val scope = rememberCoroutineScope()
    var existing by remember { mutableStateOf<PaymentMethod?>(null) }
    var kindIdx by rememberSaveable { mutableStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var account by rememberSaveable { mutableStateOf("") }
    var holder by rememberSaveable { mutableStateOf("") }
    var isDefault by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(methodId) {
        if (methodId != null && methodId > 0L) {
            val all = repo.observePaymentMethods()
            // crude pickup — get latest snapshot from one collect.
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bg).dismissKeyboardOnTap()) {
        FlowNav(title = if (methodId == null) "Add method" else "Edit method", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            FieldLabel("Type")
            Segmented(
                options = listOf("Bank", "E-wallet", "QRIS"),
                activeIndex = kindIdx,
                onSelect = { kindIdx = it },
            )
            Spacer(Modifier.height(20.dp))

            FieldLabel(if (kindIdx == 2) "Provider name" else if (kindIdx == 1) "Wallet" else "Bank")
            InputBox(name, { name = it }, hint = if (kindIdx == 0) "e.g. BCA" else if (kindIdx == 1) "e.g. GoPay" else "e.g. QRIS")

            Spacer(Modifier.height(16.dp))
            FieldLabel(if (kindIdx == 2) "Merchant ID" else "Account number")
            InputBox(account, { account = it }, hint = if (kindIdx == 1) "0812-XXX" else "1234-5678")

            Spacer(Modifier.height(16.dp))
            FieldLabel("Holder name")
            InputBox(holder, { holder = it }, hint = "Your name")

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Set as default", color = t.ink, fontSize = 15.sp, fontWeight = FontWeight.W500, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isDefault) t.accent else t.surface2)
                        .clickable { isDefault = !isDefault }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(if (isDefault) "Yes" else "No", color = if (isDefault) Color.White else t.ink2, fontWeight = FontWeight.W600, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            PrimaryButton(
                label = "Save",
                onClick = {
                    scope.launch {
                        repo.savePaymentMethod(
                            PaymentMethod(
                                id = methodId ?: 0L,
                                kind = listOf(PaymentKind.BANK, PaymentKind.EWALLET, PaymentKind.QRIS)[kindIdx],
                                name = name.trim().ifBlank { "Method" },
                                account = account.trim(),
                                holder = holder.trim(),
                                isDefault = isDefault,
                            )
                        )
                        onSaved()
                    }
                },
                enabled = name.isNotBlank() && account.isNotBlank(),
                background = t.ink,
            )
        }
    }
}

@Composable
private fun FieldLabel(label: String) {
    val t = SplitNowTokens.colors
    Text(
        label.uppercase(),
        color = t.ink2,
        fontSize = 12.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun InputBox(value: String, onChange: (String) -> Unit, hint: String = "") {
    val t = SplitNowTokens.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(t.surface2)
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
