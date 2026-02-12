package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class TerminalOpacity(
    val full: Float,
    val high: Float,
    val medium: Float,
    val low: Float,
)

val LocalTerminalOpacity = staticCompositionLocalOf {
    TerminalOpacityValues
}

val TerminalOpacityValues = TerminalOpacity(
    full = 1.0f,
    high = 0.75f,
    medium = 0.50f,
    low = 0.25f,
)
