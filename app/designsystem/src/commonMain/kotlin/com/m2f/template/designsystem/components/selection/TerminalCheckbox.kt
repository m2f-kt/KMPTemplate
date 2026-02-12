package com.m2f.template.designsystem.components.selection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A terminal-styled checkbox component with accessibility support.
 *
 * Uses Foundation [toggleable] with [Role.Checkbox] for proper accessibility semantics.
 * Renders a square box with rounded corners; when checked, shows an accent background
 * with a checkmark drawn via Canvas. Reads all styling from [TerminalTheme].
 *
 * @param checked Whether the checkbox is currently checked.
 * @param onCheckedChange Callback invoked when the checkbox is toggled.
 * @param modifier Modifier applied to the root row layout.
 * @param label Optional text label displayed to the right of the checkbox.
 * @param enabled Whether the checkbox is interactive.
 */
@Composable
fun TerminalCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius
    val borders = TerminalTheme.borders
    val gap = TerminalTheme.gap
    val opacity = TerminalTheme.opacity

    val boxSize = 18.dp
    val shape = RoundedCornerShape(radius.sm)
    val contentAlpha = if (enabled) opacity.full else opacity.medium

    Row(
        modifier = modifier
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
            .alpha(contentAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(boxSize)
                .clip(shape)
                .then(
                    if (checked) {
                        Modifier.background(colors.accent)
                    } else {
                        Modifier
                            .background(colors.surface)
                            .border(borders.default, colors.border, shape)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                val checkColor = colors.surface
                Canvas(modifier = Modifier.size(12.dp)) {
                    val strokeWidth = 2.dp.toPx()
                    // Draw checkmark: two line segments
                    drawLine(
                        color = checkColor,
                        start = Offset(size.width * 0.2f, size.height * 0.5f),
                        end = Offset(size.width * 0.4f, size.height * 0.75f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = checkColor,
                        start = Offset(size.width * 0.4f, size.height * 0.75f),
                        end = Offset(size.width * 0.8f, size.height * 0.25f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
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
