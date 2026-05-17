package com.devara.splitnow.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.data.SplitRepository
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.SectionHeader
import com.devara.splitnow.ui.components.SetRow
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPaymentMethods: () -> Unit,
    onCurrency: () -> Unit,
    onTheme: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val repo = koinInject<SplitRepository>()
    val payments by repo.observePaymentMethods().collectAsState(initial = emptyList())
    val def = payments.firstOrNull { it.isDefault } ?: payments.firstOrNull()
    val moreCount = (payments.size - 1).coerceAtLeast(0)
    val paymentDetail = when {
        def == null -> "None"
        moreCount == 0 -> def.name
        else -> "${def.name} + $moreCount more"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        FlowNav(title = "Settings", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text("Settings", color = t.ink, fontSize = 44.sp, fontWeight = FontWeight.W700, letterSpacing = (-2).sp)
            Spacer(Modifier.height(12.dp))

            SectionHeader("Splits")
            SetRow(label = "Payment methods", detail = paymentDetail, onClick = onPaymentMethods)
            SetRow(label = "Default currency", detail = "IDR", onClick = onCurrency, last = true)

            SectionHeader("Appearance")
            SetRow(label = "Theme", detail = "System", onClick = onTheme)
            SetRow(label = "Language", detail = "English", onClick = {}, last = true)

            SectionHeader("About")
            SetRow(label = "Privacy", onClick = onPrivacy)
            SetRow(label = "Help & feedback", onClick = onAbout)
            SetRow(label = "Version", detail = "1.0.0", onClick = {}, showChevron = false, last = true)

            Spacer(Modifier.height(32.dp))
        }
    }
}
