package com.example.engine

import androidx.compose.ui.graphics.Color

/**
 * Color Scheme for CodeEditor syntax highlighting and editor chrome.
 */
data class SyntaxColorScheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val background: Color,
    val gutterBackground: Color,
    val gutterText: Color,
    val gutterDivider: Color,
    val textColor: Color,
    val cursorColor: Color,
    val keywordColor: Color,
    val typeColor: Color,
    val stringColor: Color,
    val commentColor: Color,
    val numberColor: Color,
    val functionColor: Color,
    val macroColor: Color,
    val operatorColor: Color
) {
    companion object {
        val CyberDark = SyntaxColorScheme(
            id = "cyber_dark",
            name = "Cyberpunk Dark",
            isDark = true,
            background = Color(0xFF090D16),
            gutterBackground = Color(0xFF0B0F19),
            gutterText = Color(0xFF475569),
            gutterDivider = Color(0xFF1E293B),
            textColor = Color(0xFFE2E8F0),
            cursorColor = Color(0xFF00E5FF),
            keywordColor = Color(0xFFFF5370), // Neon Coral
            typeColor = Color(0xFFFFCB6B),    // Amber Gold
            stringColor = Color(0xFFC3E88D),  // Emerald Lime
            commentColor = Color(0xFF546E7A), // Slate Dimmed
            numberColor = Color(0xFFF78C6C),  // Tangerine Orange
            functionColor = Color(0xFF82AAFF),// Cyber Azure
            macroColor = Color(0xFFC792EA),   // Electric Purple
            operatorColor = Color(0xFF89DDFF) // Bright Teal Cyan
        )

        val SolarLight = SyntaxColorScheme(
            id = "solar_light",
            name = "Solaris Light",
            isDark = false,
            background = Color(0xFFF8FAFC),
            gutterBackground = Color(0xFFF1F5F9),
            gutterText = Color(0xFF94A3B8),
            gutterDivider = Color(0xFFCBD5E1),
            textColor = Color(0xFF0F172A),
            cursorColor = Color(0xFF0284C7),
            keywordColor = Color(0xFFD926AA), // Deep Magenta Rose
            typeColor = Color(0xFFB45309),    // Warm Amber
            stringColor = Color(0xFF15803D),  // Deep Forest Green
            commentColor = Color(0xFF64748B), // Slate Muted
            numberColor = Color(0xFFC2410C),  // Deep Coral Orange
            functionColor = Color(0xFF2563EB),// Royal Cobalt Blue
            macroColor = Color(0xFF7C3AED),   // Deep Violet Purple
            operatorColor = Color(0xFF0284C7) // Sky Cyan Teal
        )
    }
}
