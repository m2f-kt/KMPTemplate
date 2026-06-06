package com.m2f.template.designsystem.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * A keyboard chord rendered as a row of [AuraKbd] chips joined by mono `+` glyphs
 * (e.g. `⌘ + →`). The `+` separator uses the faintest foreground tier (`textFaint`),
 * matching the design's `.kbd-plus` token.
 *
 * @param keys The ordered chord segments (one chip per entry).
 * @param modifier Modifier for the row container.
 */
@Composable
fun AuraKbdCombo(
    keys: List<String>,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val gap = AuraTheme.gap

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keys.forEachIndexed { index, key ->
            if (index > 0) {
                BasicText(
                    text = "+",
                    style = typography.mono.copy(color = colors.textFaint),
                )
            }
            AuraKbd(text = key)
        }
    }
}

@AuraPreview
@Composable
private fun AuraKbdComboPreview() {
    AuraTheme {
        Row(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AuraKbdCombo(keys = listOf("⌘", "→"))
            AuraKbdCombo(keys = listOf("⌥", "→"))
        }
    }
}
