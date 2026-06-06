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
import com.m2f.template.designsystem.theme.AuraPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * A keyboard shortcut display element styled for Aura aesthetics.
 *
 * Renders a small inline element showing a keyboard shortcut (e.g., "Cmd+K", "Ctrl+S")
 * with bordered, slightly elevated appearance.
 *
 * @param text The keyboard shortcut text to display.
 * @param modifier Modifier for the container.
 */
@Composable
fun AuraKbd(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val borders = AuraTheme.borders
    val radius = AuraTheme.radius
    val spacing = AuraTheme.spacing

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

@AuraPreview
@Composable
private fun AuraKbdPreview() {
    AuraTheme {
        Row(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraKbd("Cmd+K")
            AuraKbd("Ctrl+S")
            AuraKbd("Esc")
        }
    }
}
