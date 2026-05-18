package com.devara.splitnow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
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
import com.devara.splitnow.ui.theme.SplitNowTokens

@Composable
fun FlowNav(
    title: String,
    onBack: (() -> Unit)? = null,
    backLabel: String = "Back",
    trailing: (@Composable () -> Unit)? = null,
) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 56.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onBack() }
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = t.accent)
                Spacer(Modifier.width(2.dp))
                Text(backLabel, color = t.accent, fontWeight = FontWeight.W500, fontSize = 17.sp)
            }
        } else {
            Spacer(Modifier.width(40.dp))
        }
        Spacer(Modifier.weight(1f))
        Text(title, fontWeight = FontWeight.W600, fontSize = 17.sp, color = t.ink)
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.CenterEnd) {
            trailing?.invoke()
        }
    }
}

@Composable
fun Pill(text: String, soft: Boolean = true) {
    val t = SplitNowTokens.colors
    val bg = if (soft) t.accentSoft else Color.Transparent
    val fg = if (soft) t.accent else t.ink2
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .then(if (soft) Modifier.background(bg) else Modifier.border(1.dp, t.line, RoundedCornerShape(99.dp)))
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.W600, letterSpacing = 0.4.sp)
    }
}

@Composable
fun BrandPill(text: String) {
    val t = SplitNowTokens.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(t.accent)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text, color = t.accentInk, fontSize = 11.sp, fontWeight = FontWeight.W800, letterSpacing = 0.2.sp)
    }
}

@Composable
fun Money(cents: Long, currencyCode: String, big: Boolean = false) {
    val t = SplitNowTokens.colors
    val currency = com.devara.splitnow.domain.Currency.byCode(currencyCode)
    val formatted = com.devara.splitnow.domain.formatMoney(cents, currency)
    val ann = buildAnnotatedString {
        withStyle(SpanStyle(color = t.ink2, fontSize = (if (big) 13 else 11).sp, fontWeight = FontWeight.W500)) {
            append(currency.symbol)
        }
        append(" ")
        withStyle(SpanStyle(color = t.ink, fontSize = (if (big) 22 else 16).sp, fontWeight = FontWeight.W700)) {
            append(formatted)
        }
    }
    Text(ann, fontFamily = null)
}

@Composable
fun SectionHeader(label: String, action: String? = null, onAction: (() -> Unit)? = null) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            label.uppercase(),
            color = t.ink3,
            fontWeight = FontWeight.W700,
            fontSize = 13.sp,
            letterSpacing = 1.2.sp,
        )
        Spacer(Modifier.weight(1f))
        if (action != null) {
            Row(
                modifier = Modifier.clickable { onAction?.invoke() }.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = t.accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(action, color = t.accent, fontWeight = FontWeight.W600, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun EditableRow(
    label: String,
    valueText: String,
    sub: String? = null,
    onClick: (() -> Unit)? = null,
    last: Boolean = false,
) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = t.ink, fontSize = 15.sp, fontWeight = FontWeight.W500)
            if (sub != null) {
                Text(sub, color = t.ink2, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Text(valueText, color = t.ink, fontSize = 15.sp, fontWeight = FontWeight.W600)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = t.ink3)
    }
    if (!last) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.line))
    }
}

@Composable
fun SetRow(
    label: String,
    detail: String? = null,
    onClick: (() -> Unit)? = null,
    last: Boolean = false,
    showChevron: Boolean = true,
) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 18.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = t.ink, fontSize = 17.sp, fontWeight = FontWeight.W500, modifier = Modifier.weight(1f))
        if (detail != null) {
            Text(detail, color = t.ink2, fontSize = 15.sp, modifier = Modifier.padding(end = 8.dp))
        }
        if (showChevron) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = t.ink3)
        }
    }
    if (!last) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(t.line))
    }
}

@Composable
fun Segmented(
    options: List<String>,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val t = SplitNowTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(t.surface2)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == activeIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) t.surface else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = if (active) t.ink else t.ink2,
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun ChipPicker(
    options: List<String>,
    activeIndices: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    val t = SplitNowTokens.colors
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i in activeIndices
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (active) t.ink else t.surface2)
                    .clickable { onToggle(i) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (active) {
                    // Active chip sits on a t.ink background — the indicator
                    // dot + label must use t.bg (which inverts with the theme)
                    // or they vanish in dark mode (ink IS near-white there).
                    Box(
                        modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(t.bg),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    label,
                    color = if (active) t.bg else t.ink,
                    fontWeight = FontWeight.W600,
                    fontSize = 15.sp,
                )
            }
        }
    }
}
