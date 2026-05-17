package com.devara.splitnow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun SplitNowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tokens = if (darkTheme) SplitNowDarkColors else SplitNowLightColors
    val material = if (darkTheme) {
        darkColorScheme(
            primary = tokens.accent,
            onPrimary = tokens.accentInk,
            background = tokens.bg,
            onBackground = tokens.ink,
            surface = tokens.surface,
            onSurface = tokens.ink,
            surfaceVariant = tokens.surface2,
            onSurfaceVariant = tokens.ink2,
            error = tokens.destructive,
        )
    } else {
        lightColorScheme(
            primary = tokens.accent,
            onPrimary = tokens.accentInk,
            background = tokens.bg,
            onBackground = tokens.ink,
            surface = tokens.surface,
            onSurface = tokens.ink,
            surfaceVariant = tokens.surface2,
            onSurfaceVariant = tokens.ink2,
            error = tokens.destructive,
        )
    }
    CompositionLocalProvider(LocalSplitNowColors provides tokens) {
        MaterialTheme(colorScheme = material, typography = SplitNowType, content = content)
    }
}
