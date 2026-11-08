package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class VaultThemePreset(
    val id: String,
    val label: String,
    val isDark: Boolean,
    val bgHex: Long,
    val surfaceHex: Long,
    val surfaceVariantHex: Long
) {
    AURORA("aurora", "Aurora", true, 0xFF0B0E1E, 0xFF151932, 0xFF202648),
    DARK("dark", "Midnight", true, 0xFF0C0E18, 0xFF161A2A, 0xFF1F2438),
    BLACK("black", "Obsidian", true, 0xFF000000, 0xFF0D0E12, 0xFF181A22),
    OCEAN("ocean", "Ocean", true, 0xFF071922, 0xFF0F2936, 0xFF173B4C),
    SUNSET("sunset", "Sunset", true, 0xFF1E0D24, 0xFF2E1536, 0xFF421F4D),
    LIGHT("light", "Daylight", false, 0xFFF2F4FA, 0xFFFFFFFF, 0xFFE6EAF5)
}

enum class VaultAccentPreset(
    val id: String,
    val label: String,
    val primaryDark: Color,
    val primaryLight: Color,
    val secondary: Color
) {
    INDIGO("indigo", "Electric Indigo", Color(0xFF8C7BFF), Color(0xFF5340FF), Color(0xFF27E6FF)),
    EMERALD("emerald", "Emerald Mint", Color(0xFF34D399), Color(0xFF059669), Color(0xFF10B981)),
    CYAN("cyan", "Cyber Cyan", Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF67E8F9)),
    ROSE("rose", "Rose Coral", Color(0xFFFB7185), Color(0xFFE11D48), Color(0xFFF43F5E)),
    AMBER("amber", "Amber Gold", Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFF59E0B))
}

val VaultCardPalette = listOf(
    "#4F46E5" to "Royal Indigo",
    "#0F766E" to "Deep Teal",
    "#1D4ED8" to "Sapphire Blue",
    "#B45309" to "Sovereign Gold",
    "#BE123C" to "Crimson Ruby",
    "#7E22CE" to "Imperial Purple",
    "#15803D" to "Emerald Forest",
    "#1E293B" to "Titanium Slate",
    "#090D16" to "Infinia Obsidian",
    "#C2410C" to "Copper Sunset"
)
