package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TerminalColors(
    val bg: Color,
    val surface: Color,
    val accent: Color,
    val accentMuted: Color,
    val text: Color,
    val textMuted: Color,
    val textDim: Color,
    val border: Color,
    val inset: Color,
    val success: Color,
    val successBg: Color,
    val warning: Color,
    val warningBg: Color,
    val error: Color,
    val errorBg: Color,
    val info: Color,
    val infoBg: Color,
)

val LocalTerminalColors = staticCompositionLocalOf {
    TerminalColors(
        bg = Color.Unspecified,
        surface = Color.Unspecified,
        accent = Color.Unspecified,
        accentMuted = Color.Unspecified,
        text = Color.Unspecified,
        textMuted = Color.Unspecified,
        textDim = Color.Unspecified,
        border = Color.Unspecified,
        inset = Color.Unspecified,
        success = Color.Unspecified,
        successBg = Color.Unspecified,
        warning = Color.Unspecified,
        warningBg = Color.Unspecified,
        error = Color.Unspecified,
        errorBg = Color.Unspecified,
        info = Color.Unspecified,
        infoBg = Color.Unspecified,
    )
}

// Light theme -- extracted from terminal_design_system.pen variables
val TerminalLightColors = TerminalColors(
    bg = Color(0xFFE8E8E8),
    surface = Color(0xFFF5F5F5),
    accent = Color(0xFF4A9B6E),
    accentMuted = Color(0xFFD8E8DE),
    text = Color(0xFF1F1F1F),
    textMuted = Color(0xFF5A5A5A),
    textDim = Color(0xFF787878),
    border = Color(0xFFD0D0D0),
    inset = Color(0xFFEFEFEF),
    success = Color(0xFF4A9B6E),
    successBg = Color(0xFFD8E8DE),
    warning = Color(0xFFA08840),
    warningBg = Color(0xFFEDE8D8),
    error = Color(0xFFB05A5A),
    errorBg = Color(0xFFEDDCDC),
    info = Color(0xFF4A7EB0),
    infoBg = Color(0xFFDCE6EF),
)

// Dark theme -- extracted from terminal_design_system.pen variables
val TerminalDarkColors = TerminalColors(
    bg = Color(0xFF101012),
    surface = Color(0xFF1A1A1C),
    accent = Color(0xFF6BAF8A),
    accentMuted = Color(0xFF1F3028),
    text = Color(0xFFD4D4D4),
    textMuted = Color(0xFF8A8A8A),
    textDim = Color(0xFF5A5A5A),
    border = Color(0xFF2A2A2E),
    inset = Color(0xFF212124),
    success = Color(0xFF6BAF8A),
    successBg = Color(0xFF1A2820),
    warning = Color(0xFFC4A860),
    warningBg = Color(0xFF28241A),
    error = Color(0xFFCA7A7A),
    errorBg = Color(0xFF2A1A1A),
    info = Color(0xFF7AA4CA),
    infoBg = Color(0xFF1A2530),
)
