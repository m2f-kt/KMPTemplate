package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TerminalRadius(
    val none: Dp,
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val pill: Dp,
    val full: Dp,
)

val LocalTerminalRadius = staticCompositionLocalOf {
    TerminalRadiusValues
}

val TerminalRadiusValues = TerminalRadius(
    none = 0.dp,
    xs = 2.dp,
    sm = 4.dp,
    md = 6.dp,
    lg = 12.dp,
    pill = 24.dp,
    full = 9999.dp,
)
