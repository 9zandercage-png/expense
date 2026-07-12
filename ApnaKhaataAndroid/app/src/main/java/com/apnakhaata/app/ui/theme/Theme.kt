package com.apnakhaata.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Passbook / ledger palette (same as web app)
val Paper = Color(0xFFF4EFE2)
val Card = Color(0xFFFAF6EE)
val Ink = Color(0xFF1B2A4A)
val InkSoft = Color(0xFF5B6472)
val RedLedger = Color(0xFFA33B20)
val GreenLedger = Color(0xFF3F6B4F)
val Gold = Color(0xFFC08A2E)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDE7D8),
    onSurfaceVariant = InkSoft,
    error = RedLedger
)

@Composable
fun ApnaKhaataTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}

/** Deterministic color per category (matches web app idea). */
fun categoryColor(category: String): Color {
    val palette = listOf(
        Color(0xFF3F6B4F), Color(0xFFA33B20), Color(0xFFC08A2E),
        Color(0xFF2B6CA3), Color(0xFF7B4FA3), Color(0xFF3E8E8C),
        Color(0xFFAA5A44), Color(0xFF5B6472), Color(0xFF8A6D3B),
        Color(0xFF4A7A8C)
    )
    if (category == "Uncategorised") return Color(0xFF9B9485)
    val idx = Math.abs(category.hashCode()) % palette.size
    return palette[idx]
}
