package com.m2f.template.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AuraGap(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
)

val LocalAuraGap = staticCompositionLocalOf {
    AuraGap(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 24.dp)
}
