package com.m2f.template.designsystem.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.m2f.template.designsystem.theme.TerminalTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun TerminalText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TerminalTheme.typography.base,
    color: Color = TerminalTheme.colors.text,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color),
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Preview
@Composable
private fun TerminalTextPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalText(
                text = "Medium text",
                style = TerminalTheme.typography.md,
                color = TerminalTheme.colors.text,
            )
            TerminalText(
                text = "Base text",
                style = TerminalTheme.typography.base,
                color = TerminalTheme.colors.textMuted,
            )
            TerminalText(
                text = "Small text",
                style = TerminalTheme.typography.sm,
                color = TerminalTheme.colors.textDim,
            )
            TerminalText(
                text = "Extra-small accent",
                style = TerminalTheme.typography.xs,
                color = TerminalTheme.colors.accent,
            )
        }
    }
}
