package com.m2f.template.designsystem.components.selection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A terminal-styled radio button component with accessibility support.
 *
 * Uses Foundation [selectable] with [Role.RadioButton] for proper accessibility semantics.
 * Renders an outer circle with a filled inner dot when selected. The outer ring uses
 * the accent color when selected and the border color when unselected.
 * Reads all styling from [TerminalTheme].
 *
 * @param selected Whether the radio button is currently selected.
 * @param onClick Callback invoked when the radio button is clicked.
 * @param modifier Modifier applied to the root row layout.
 * @param label Optional text label displayed to the right of the radio button.
 * @param enabled Whether the radio button is interactive.
 */
@Composable
fun TerminalRadio(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val borders = TerminalTheme.borders
    val gap = TerminalTheme.gap
    val opacity = TerminalTheme.opacity

    val outerSize = 18.dp
    val innerDotSize = 10.dp
    val contentAlpha = if (enabled) opacity.full else opacity.medium

    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .alpha(contentAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(outerSize)) {
            val strokeWidth = borders.default.toPx()
            val outerRadius = size.minDimension / 2f

            if (selected) {
                // Selected: accent border ring
                drawCircle(
                    color = colors.accent,
                    radius = outerRadius - strokeWidth / 2f,
                    style = Stroke(width = strokeWidth),
                )
                // Inner filled dot
                val innerRadius = innerDotSize.toPx() / 2f
                drawCircle(
                    color = colors.accent,
                    radius = innerRadius,
                )
            } else {
                // Unselected: surface fill with border
                drawCircle(
                    color = colors.surface,
                    radius = outerRadius - strokeWidth / 2f,
                )
                drawCircle(
                    color = colors.border,
                    radius = outerRadius - strokeWidth / 2f,
                    style = Stroke(width = strokeWidth),
                )
            }
        }

        if (label != null) {
            Spacer(modifier = Modifier.width(gap.sm))
            BasicText(
                text = label,
                style = typography.sm.copy(color = colors.text),
            )
        }
    }
}

@Preview
@Composable
private fun TerminalRadioPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalRadio(selected = false, onClick = {})
            TerminalRadio(selected = true, onClick = {})
            TerminalRadio(selected = true, onClick = {}, label = "Option A")
            TerminalRadio(selected = false, onClick = {}, label = "Disabled", enabled = false)
        }
    }
}
