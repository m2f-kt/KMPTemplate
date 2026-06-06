package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AuraRadius(
    val none: Dp,
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val pill: Dp,
    val full: Dp,
)

val LocalAuraRadius = staticCompositionLocalOf {
    AuraRadiusValues
}

// Aura — generous, rounded, friendly corners.
val AuraRadiusValues = AuraRadius(
    none = 0.dp,
    xs = 6.dp,
    sm = 10.dp,
    md = 14.dp,
    lg = 20.dp,
    xl = 28.dp,
    pill = 999.dp,
    full = 9999.dp,
)
