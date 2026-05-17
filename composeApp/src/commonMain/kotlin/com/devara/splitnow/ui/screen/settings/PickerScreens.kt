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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.devara.splitnow.data.PREF_DEFAULT_CURRENCY
import com.devara.splitnow.data.PREF_LOCALE
import com.devara.splitnow.data.PREF_THEME
import com.devara.splitnow.data.SettingsStore
import com.devara.splitnow.domain.Currency
import com.devara.splitnow.l10n.Locales
import com.devara.splitnow.l10n.ThemeChoice
import com.devara.splitnow.l10n.Translator
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun CurrencyPickerScreen(onBack: () -> Unit) {
    val t = SplitNowTokens.colors
    val settings = koinInject<SettingsStore>()
    var selected by remember { mutableStateOf(settings.getString(PREF_DEFAULT_CURRENCY, "IDR") ?: "IDR") }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "Default currency", onBack = onBack, backLabel = "Settings")
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(Currency.all, key = { it.code }) { c ->
                PickerRow(
                    label = c.code,
                    sub = "${c.symbol} · ${currencyName(c.code)}",
                    selected = selected == c.code,
                    onClick = {
                        selected = c.code
                        settings.putString(PREF_DEFAULT_CURRENCY, c.code)
                    },
                )
            }
        }
    }
}

private fun currencyName(code: String): String = when (code) {
    "IDR" -> "Indonesian Rupiah"
    "USD" -> "US Dollar"
    "EUR" -> "Euro"
    "SGD" -> "Singapore Dollar"
    "MYR" -> "Malaysian Ringgit"
    "JPY" -> "Japanese Yen"
    "GBP" -> "British Pound"
    else -> code
}

@Composable
fun ThemePickerScreen(onBack: () -> Unit) {
    val t = SplitNowTokens.colors
    val settings = koinInject<SettingsStore>()
    var selected by remember { mutableStateOf(settings.getString(PREF_THEME, "system") ?: "system") }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "Theme", onBack = onBack, backLabel = "Settings")
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            ThemeChoice.entries.forEach { choice ->
                PickerRow(
                    label = choice.label,
                    sub = choice.description,
                    selected = selected == choice.id,
                    onClick = {
                        selected = choice.id
                        settings.putString(PREF_THEME, choice.id)
                    },
                )
            }
        }
    }
}

@Composable
fun LanguagePickerScreen(onBack: () -> Unit) {
    val t = SplitNowTokens.colors
    val settings = koinInject<SettingsStore>()
    val translator = koinInject<Translator>()
    var selected by remember { mutableStateOf(settings.getString(PREF_LOCALE, "en") ?: "en") }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        FlowNav(title = "Language", onBack = onBack, backLabel = "Settings")
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        ) {
            items(Locales.all, key = { it.code }) { l ->
                PickerRow(
                    label = l.label,
                    sub = l.englishName,
                    selected = selected == l.code,
                    onClick = {
                        selected = l.code
                        settings.putString(PREF_LOCALE, l.code)
                        translator.setLocale(l.code)
                    },
                )
            }
        }
    }
}

@Composable
private fun PickerRow(
    label: String,
    sub: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = t.ink, fontSize = 17.sp, fontWeight = FontWeight.W500)
            if (sub != null) {
                Text(sub, color = t.ink2, fontSize = 13.sp)
            }
        }
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = t.accent)
        } else {
            Spacer(Modifier.size(20.dp))
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(t.line))
}
