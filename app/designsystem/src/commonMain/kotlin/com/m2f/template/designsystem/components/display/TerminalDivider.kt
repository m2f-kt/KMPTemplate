package com.m2f.template.designsystem.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
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

@Preview
@Composable
private fun TerminalDividerPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
        ) {
            TerminalText("Above")
            TerminalDivider(modifier = Modifier.padding(vertical = 8.dp))
            TerminalText("Below")
        }
    }
}
