package com.m2f.template.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

@Composable
fun TerminalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) TerminalDarkColors else TerminalLightColors
    val typography = terminalTypography()

    CompositionLocalProvider(
        LocalTerminalColors provides colors,
        LocalTerminalTypography provides typography,
        LocalTerminalSpacing provides TerminalSpacing(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 20.dp),
        LocalTerminalGap provides TerminalGap(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 24.dp),
        LocalTerminalShadows provides TerminalShadowValues,
        LocalTerminalOpacity provides TerminalOpacityValues,
        LocalTerminalRadius provides TerminalRadiusValues,
        LocalTerminalBorders provides TerminalBorderValues,
        content = content,
    )
}

object TerminalTheme {
    val colors: TerminalColors
        @Composable get() = LocalTerminalColors.current

    val typography: TerminalTypography
        @Composable get() = LocalTerminalTypography.current

    val spacing: TerminalSpacing
        @Composable get() = LocalTerminalSpacing.current

    val gap: TerminalGap
        @Composable get() = LocalTerminalGap.current

    val shadows: TerminalShadows
        @Composable get() = LocalTerminalShadows.current

    val opacity: TerminalOpacity
        @Composable get() = LocalTerminalOpacity.current

    val radius: TerminalRadius
        @Composable get() = LocalTerminalRadius.current

    val borders: TerminalBorders
        @Composable get() = LocalTerminalBorders.current
}
