package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Represents a shadow specification for terminal components.
 * Used with Modifier.shadow(elevation, shape) or custom drawing.
 */
@Immutable
data class TerminalShadow(
    val blur: Dp,
    val offsetX: Dp,
    val offsetY: Dp,
    val spread: Dp,
    val color: Color,
)

@Immutable
data class TerminalShadows(
    val none: TerminalShadow?,
    val sm: TerminalShadow,
    val md: TerminalShadow,
    val lg: TerminalShadow,
)

val LocalTerminalShadows = staticCompositionLocalOf {
    TerminalShadowValues
}

val TerminalShadowValues = TerminalShadows(
    none = null,
    sm = TerminalShadow(blur = 4.dp, offsetX = 0.dp, offsetY = 2.dp, spread = 0.dp, color = Color(0x20000000)),
    md = TerminalShadow(blur = 8.dp, offsetX = 0.dp, offsetY = 4.dp, spread = 0.dp, color = Color(0x30000000)),
    lg = TerminalShadow(blur = 16.dp, offsetX = 0.dp, offsetY = 8.dp, spread = 0.dp, color = Color(0x40000000)),
)
