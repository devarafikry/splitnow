package com.devara.splitnow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * SplitNow design tokens. Coral is *the* brand color, used sparingly:
 * onboarding hero, primary CTAs, loading pulse, brand pill, "you" identity.
 * Everywhere else: ink + white + surface2. People differentiated by name,
 * not color.
 */
@Immutable
data class SplitNowColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val line: Color,
    val accent: Color,
    val accentInk: Color,
    val accentSoft: Color,
    val brandBg: Color,
    val brandInk: Color,
    val destructive: Color,
    val isDark: Boolean,
)

val SplitNowLightColors = SplitNowColors(
    bg = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF4F2EE),
    ink = Color(0xFF0F0E0C),
    ink2 = Color(0xFF6E6A62),
    ink3 = Color(0xFFA8A49A),
    line = Color(0x140F0E0C),
    accent = Color(0xFFF25A2B),
    accentInk = Color(0xFFFFFFFF),
    accentSoft = Color(0xFFFCE4D8),
    brandBg = Color(0xFFF25A2B),
    brandInk = Color(0xFFFFFFFF),
    destructive = Color(0xFFC7300F),
    isDark = false,
)

val SplitNowDarkColors = SplitNowColors(
    bg = Color(0xFF0B0B0C),
    surface = Color(0xFF161618),
    surface2 = Color(0xFF1F1F22),
    ink = Color(0xFFFAFAF7),
    ink2 = Color(0x9EEBEBF5),
    ink3 = Color(0x52EBEBF5),
    line = Color(0x14FFFFFF),
    accent = Color(0xFFFF6F4A),
    accentInk = Color(0xFF1A0F0A),
    accentSoft = Color(0x29FF6F4A),
    brandBg = Color(0xFFF25A2B),
    brandInk = Color(0xFFFFFFFF),
    destructive = Color(0xFFFF6A4D),
    isDark = true,
)

val LocalSplitNowColors = compositionLocalOf { SplitNowLightColors }

object SplitNowTokens {
    val colors: SplitNowColors
        @Composable @ReadOnlyComposable get() = LocalSplitNowColors.current
}
