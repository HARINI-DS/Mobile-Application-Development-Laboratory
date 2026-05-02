package com.harini.yours.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val YoursColorScheme = darkColorScheme(
    primary          = Crimson,
    onPrimary        = TextOnCrimson,
    primaryContainer = CrimsonDark,
    secondary        = CrimsonBright,
    background       = Black800,
    surface          = Black700,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    outline          = Black500
)

@Composable
fun YoursTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YoursColorScheme,
        typography  = Typography,
        content     = content
    )
}