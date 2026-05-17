package com.devara.splitnow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.ui.theme.SplitNowTokens

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
    background: Color? = null,
    foreground: Color? = null,
    height: Int = 60,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val t = SplitNowTokens.colors
    val bg = (background ?: t.ink).let { if (enabled) it else it.copy(alpha = 0.6f) }
    // Default foreground = the inverted ink so the text contrasts the ink-bg
    // in both light and dark mode (light: white-on-dark; dark: dark-on-near-white).
    val fg = foreground ?: t.bg
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(height.dp)
            .clip(RoundedCornerShape(height.dp / 2))
            .background(bg)
            .clickable(enabled = enabled, interactionSource = source, indication = null) { onClick() }
            .padding(horizontal = 26.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = label,
            color = fg,
            fontWeight = FontWeight.W600,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun GhostButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = true,
) {
    val t = SplitNowTokens.colors
    Box(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = t.line, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = t.ink, fontWeight = FontWeight.W600, fontSize = 17.sp)
    }
}

@Composable
fun AccentLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    val t = SplitNowTokens.colors
    Row(
        modifier = modifier.clickable { onClick() }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(4.dp))
        }
        Text(label, color = t.accent, fontWeight = FontWeight.W600, fontSize = 14.sp)
    }
}
