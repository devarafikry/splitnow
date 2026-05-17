package com.devara.splitnow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * SplitNow typography. SF Pro on iOS / Roboto on Android via system default,
 * Material3 picks the platform font automatically. JetBrains Mono is reserved
 * for receipt rendering — declared as Monospace and substituted on each
 * platform's monospace face.
 */
private val System = FontFamily.Default
val Mono: FontFamily = FontFamily.Monospace

val SplitNowType = Typography(
    displayLarge = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W800,
        fontSize = 76.sp,
        lineHeight = 70.sp,
        letterSpacing = (-0.045).em,
    ),
    displayMedium = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W700,
        fontSize = 64.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.04).em,
    ),
    displaySmall = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W700,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.04).em,
    ),
    headlineLarge = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W700,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.035).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.03).em,
    ),
    headlineSmall = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W700,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.02).em,
    ),
    titleLarge = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W600,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.02).em,
    ),
    titleMedium = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W600,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.015).em,
    ),
    titleSmall = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W600,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.01).em,
    ),
    bodyLarge = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W400,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.015).em,
    ),
    bodyMedium = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W400,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.01).em,
    ),
    bodySmall = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W400,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.005).em,
    ),
    labelLarge = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W600,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.01).em,
    ),
    labelMedium = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W700,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.12.em,
    ),
    labelSmall = TextStyle(
        fontFamily = System,
        fontWeight = FontWeight.W700,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.12.em,
    ),
)
