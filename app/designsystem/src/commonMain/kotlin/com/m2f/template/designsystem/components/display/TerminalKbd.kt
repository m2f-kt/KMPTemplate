package com.m2f.template.designsystem.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A keyboard shortcut display element styled for terminal aesthetics.
 *
 * Renders a small inline element showing a keyboard shortcut (e.g., "Cmd+K", "Ctrl+S")
 * with bordered, slightly elevated appearance.
 *
 * @param text The keyboard shortcut text to display.
 * @param modifier Modifier for the container.
 */
@Composable
fun TerminalKbd(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val borders = TerminalTheme.borders
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing

    val shape = RoundedCornerShape(radius.sm)

    Box(
        modifier = modifier
            .clip(shape)
            .border(borders.thin, colors.border, shape)
            .background(colors.inset)
            .padding(horizontal = spacing.xs, vertical = 2.dp),
    ) {
        BasicText(
            text = text,
            style = typography.xs.copy(color = colors.textMuted),
        )
    }
}

@Preview
@Composable
private fun TerminalKbdPreview() {
    TerminalTheme {
        Row(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalKbd("Cmd+K")
            TerminalKbd("Ctrl+S")
            TerminalKbd("Esc")
        }
    }
}
