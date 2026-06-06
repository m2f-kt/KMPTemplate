package com.m2f.template.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) AuraDarkColors else AuraLightColors
    val typography = auraTypography()

    CompositionLocalProvider(
        LocalAuraColors provides colors,
        LocalAuraTypography provides typography,
        LocalAuraSpacing provides AuraSpacing(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 20.dp),
        LocalAuraGap provides AuraGap(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 24.dp),
        LocalAuraShadows provides AuraShadowValues,
        LocalAuraOpacity provides AuraOpacityValues,
        LocalAuraRadius provides AuraRadiusValues,
        LocalAuraBorders provides AuraBorderValues,
        LocalAuraGlows provides AuraGlowValues,
        LocalAuraMotion provides AuraMotionValues,
        content = content,
    )
}

object AuraTheme {
    val colors: AuraColors
        @Composable get() = LocalAuraColors.current

    val typography: AuraTypography
        @Composable get() = LocalAuraTypography.current

    val spacing: AuraSpacing
        @Composable get() = LocalAuraSpacing.current

    val gap: AuraGap
        @Composable get() = LocalAuraGap.current

    val shadows: AuraShadows
        @Composable get() = LocalAuraShadows.current

    val opacity: AuraOpacity
        @Composable get() = LocalAuraOpacity.current

    val radius: AuraRadius
        @Composable get() = LocalAuraRadius.current

    val borders: AuraBorders
        @Composable get() = LocalAuraBorders.current

    val glows: AuraGlows
        @Composable get() = LocalAuraGlows.current

    val motion: AuraMotion
        @Composable get() = LocalAuraMotion.current
}
