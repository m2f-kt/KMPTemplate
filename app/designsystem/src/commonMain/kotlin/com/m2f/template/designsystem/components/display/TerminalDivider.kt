package com.m2f.template.designsystem.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A simple horizontal divider line using theme border color.
 *
 * @param modifier Modifier for the divider.
 */
@Composable
fun TerminalDivider(
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val borders = TerminalTheme.borders

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(borders.thin)
            .background(colors.border),
    )
}
