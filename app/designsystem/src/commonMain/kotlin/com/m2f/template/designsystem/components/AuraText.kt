package com.m2f.template.designsystem.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.modifier.auraBrush
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun AuraText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AuraTheme.typography.base,
    color: Color = AuraTheme.colors.text,
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

/**
 * Branded headline ink — text painted with the aura gradient (cyan → violet → magenta).
 * Use for the one emphasized fragment in a headline, not whole paragraphs.
 */
@Composable
fun AuraGradientText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AuraTheme.typography.h1,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(brush = auraBrush()),
        maxLines = maxLines,
        overflow = overflow,
    )
}

@AuraPreview
@Composable
private fun AuraTextPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraText(
                text = "Medium text",
                style = AuraTheme.typography.md,
                color = AuraTheme.colors.text,
            )
            AuraText(
                text = "Base text",
                style = AuraTheme.typography.base,
                color = AuraTheme.colors.textMuted,
            )
            AuraText(
                text = "Small text",
                style = AuraTheme.typography.sm,
                color = AuraTheme.colors.textDim,
            )
            AuraText(
                text = "Extra-small accent",
                style = AuraTheme.typography.xs,
                color = AuraTheme.colors.accent,
            )
        }
    }
}
