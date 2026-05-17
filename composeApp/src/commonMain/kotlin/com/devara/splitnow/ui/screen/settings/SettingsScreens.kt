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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.data.PREF_DEFAULT_CURRENCY
import com.devara.splitnow.data.PREF_LOCALE
import com.devara.splitnow.data.PREF_THEME
import com.devara.splitnow.data.SettingsStore
import com.devara.splitnow.data.SplitRepository
import com.devara.splitnow.domain.Currency
import com.devara.splitnow.l10n.Locales
import com.devara.splitnow.ui.components.SectionHeader
import com.devara.splitnow.ui.components.SetRow
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPaymentMethods: () -> Unit,
    onCurrency: () -> Unit,
    onLanguage: () -> Unit,
    onTheme: () -> Unit,
    onPrivacy: () -> Unit,
    onHelp: () -> Unit,
) {
    val t = SplitNowTokens.colors
    val settings = koinInject<SettingsStore>()
    val repo = koinInject<SplitRepository>()
    val payments by repo.observePaymentMethods().collectAsState(initial = emptyList())
    val def = payments.firstOrNull { it.isDefault } ?: payments.firstOrNull()
    val moreCount = (payments.size - 1).coerceAtLeast(0)
    val paymentDetail = when {
        def == null -> "None"
        moreCount == 0 -> def.name
        else -> "${def.name} + $moreCount more"
    }
    val currentCurrency = remember { settings.getString(PREF_DEFAULT_CURRENCY, "IDR") ?: "IDR" }
    val currentTheme = remember { (settings.getString(PREF_THEME, "system") ?: "system").replaceFirstChar { it.uppercase() } }
    val currentLocale = remember { settings.getString(PREF_LOCALE, "en") ?: "en" }
    val currentLanguageLabel = remember(currentLocale) {
        Locales.all.firstOrNull { it.code == currentLocale }?.label ?: "English"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Inline back button + large title — no duplicate header.
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onBack() }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = t.accent)
                Spacer(Modifier.size(2.dp))
                Text("Back", color = t.accent, fontWeight = FontWeight.W500, fontSize = 17.sp)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text("Settings", color = t.ink, fontSize = 44.sp, fontWeight = FontWeight.W700, letterSpacing = (-2).sp)

            SectionHeader("Splits")
            SetRow(label = "Payment methods", detail = paymentDetail, onClick = onPaymentMethods)
            SetRow(label = "Default currency", detail = currentCurrency, onClick = onCurrency, last = true)

            SectionHeader("Appearance")
            SetRow(label = "Theme", detail = currentTheme, onClick = onTheme)
            SetRow(label = "Language", detail = currentLanguageLabel, onClick = onLanguage, last = true)

            SectionHeader("About")
            SetRow(label = "Privacy", onClick = onPrivacy)
            SetRow(label = "Help & feedback", onClick = onHelp)
            SetRow(label = "Version", detail = "1.0.0", onClick = {}, showChevron = false, last = true)

            Spacer(Modifier.height(32.dp))
        }
    }
}
