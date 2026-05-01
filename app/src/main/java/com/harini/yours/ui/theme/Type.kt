package com.harini.yours.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system sans-serif with tight tracking for a premium feel.
// If you add custom fonts (e.g. "Syne" or "Outfit") to res/font, replace below.
val YoursFont = FontFamily.Default

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = YoursFont,
        fontWeight = FontWeight.Black,
        fontSize   = 32.sp,
        letterSpacing = (-1).sp
    ),
    titleLarge = TextStyle(
        fontFamily = YoursFont,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = YoursFont,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = YoursFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight  = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = YoursFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight  = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = YoursFont,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        letterSpacing = 0.8.sp
    )
)