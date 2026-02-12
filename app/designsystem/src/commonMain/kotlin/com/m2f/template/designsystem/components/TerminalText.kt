package com.m2f.template.designsystem.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.m2f.template.designsystem.theme.TerminalTheme

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
