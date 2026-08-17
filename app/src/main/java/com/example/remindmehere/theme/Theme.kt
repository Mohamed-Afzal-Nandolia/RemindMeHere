package com.example.remindmehere.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary          = VioletPrimary,
    onPrimary        = OnPrimary,
    primaryContainer = VioletDark,
    onPrimaryContainer = VioletLight,
    secondary        = CyanAccent,
    onSecondary      = DeepNavy,
    secondaryContainer = Color(0xFF0E4F5C),
    onSecondaryContainer = CyanLight,
    tertiary         = VioletLight,
    background       = DeepNavy,
    onBackground     = OnSurface,
    surface          = NavySurface,
    onSurface        = OnSurface,
    surfaceVariant   = NavyContainer,
    onSurfaceVariant = OnSurfaceMuted,
    outline          = CardBorder,
    error            = ErrorColor,
)

@Composable
fun RemindMeHereTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content
    )
}
