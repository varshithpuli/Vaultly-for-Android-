package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun VaultlyTheme(
    themePreset: VaultThemePreset = VaultThemePreset.AURORA,
    accentPreset: VaultAccentPreset = VaultAccentPreset.INDIGO,
    content: @Composable () -> Unit
) {
    val isDark = themePreset.isDark
    val primary = if (isDark) accentPreset.primaryDark else accentPreset.primaryLight
    val background = Color(themePreset.bgHex)
    val surface = Color(themePreset.surfaceHex)
    val surfaceVariant = Color(themePreset.surfaceVariantHex)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color(0xFF0A0D1C),
            primaryContainer = surfaceVariant,
            onPrimaryContainer = Color(0xFFEEF2FF),
            secondary = accentPreset.secondary,
            onSecondary = Color(0xFF071426),
            secondaryContainer = surfaceVariant,
            onSecondaryContainer = Color(0xFFE0F2FE),
            tertiary = Color(0xFFFBBF24),
            background = background,
            onBackground = Color(0xFFF1F5F9),
            surface = surface,
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = Color(0xFF94A3B8),
            error = Color(0xFFFF6B72),
            outline = Color(0xFF334155)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0E7FF),
            onPrimaryContainer = Color(0xFF1E1B4B),
            secondary = accentPreset.secondary,
            onSecondary = Color(0xFF082F49),
            secondaryContainer = Color(0xFFE0F2FE),
            onSecondaryContainer = Color(0xFF0C4A6E),
            tertiary = Color(0xFFD97706),
            background = background,
            onBackground = Color(0xFF0F172A),
            surface = surface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = Color(0xFF475569),
            error = Color(0xFFE11D48),
            outline = Color(0xFFCBD5E1)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    VaultlyTheme(content = content)
}
