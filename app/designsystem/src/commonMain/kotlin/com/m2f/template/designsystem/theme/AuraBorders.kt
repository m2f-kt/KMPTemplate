package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AuraBorders(
    val thin: Dp,
    val default: Dp,
    val thick: Dp,
)

val LocalAuraBorders = staticCompositionLocalOf {
    AuraBorderValues
}

val AuraBorderValues = AuraBorders(
    thin = 1.dp,
    default = 2.dp,
    thick = 3.dp,
)
