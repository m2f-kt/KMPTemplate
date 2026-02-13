package com.m2f.template.designsystem.components.selection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import com.m2f.template.designsystem.theme.TerminalPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.theme.rememberTerminalRipple

/**
 * A terminal-styled tri-state checkbox component with accessibility support.
 *
 * Uses Foundation [triStateToggleable] with [Role.Checkbox] for proper accessibility semantics.
 * Renders a square box with rounded corners; when checked or indeterminate, shows
 * a primary background with a checkmark or dash drawn via Canvas.
 * Reads all styling from [TerminalTheme].
 *
 * @param state The tri-state value: [ToggleableState.On], [ToggleableState.Off], or [ToggleableState.Indeterminate].
 * @param onClick Callback invoked when the checkbox is clicked.
 * @param modifier Modifier applied to the root row layout.
 * @param label Optional text label displayed to the right of the checkbox.
 * @param enabled Whether the checkbox is interactive.
 */
@Composable
fun TerminalCheckbox(
    state: ToggleableState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius
    val borders = TerminalTheme.borders
    val opacity = TerminalTheme.opacity

    val boxSize = 18.dp
    val shape = RoundedCornerShape(radius.sm)
    val contentAlpha = if (enabled) opacity.full else opacity.medium
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .triStateToggleable(
                state = state,
                interactionSource = interactionSource,
                indication = rememberTerminalRipple(bounded = false),
                enabled = enabled,
                role = Role.Checkbox,
                onClick = onClick,
            )
            .alpha(contentAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(boxSize)
                .clip(shape)
                .then(
                    when (state) {
                        ToggleableState.Off -> Modifier
                            .background(colors.checkboxBg)
                            .border(borders.default, colors.border, shape)
                        ToggleableState.On,
                        ToggleableState.Indeterminate -> Modifier
                            .background(colors.btnPrimaryBg)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                ToggleableState.On -> {
                    val checkColor = colors.btnPrimaryText
                    Canvas(modifier = Modifier.size(12.dp)) {
                        val strokeWidth = 2.dp.toPx()
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
                ToggleableState.Indeterminate -> {
                    val dashColor = colors.btnPrimaryText
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawLine(
                            color = dashColor,
                            start = Offset(size.width * 0.2f, size.height * 0.5f),
                            end = Offset(size.width * 0.8f, size.height * 0.5f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                ToggleableState.Off -> { /* empty */ }
            }
        }

        if (label != null) {
            Spacer(modifier = Modifier.width(10.dp))
            BasicText(
                text = label,
                style = typography.sm.copy(color = colors.text),
            )
        }
    }
}

/**
 * A terminal-styled checkbox component with accessibility support.
 *
 * This is a convenience overload that delegates to the tri-state [TerminalCheckbox].
 * It maps [checked] to [ToggleableState.On] or [ToggleableState.Off].
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
    TerminalCheckbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        label = label,
        enabled = enabled,
    )
}

@TerminalPreview
@Composable
private fun TerminalCheckboxPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalCheckbox(checked = false, onCheckedChange = {})
            TerminalCheckbox(checked = true, onCheckedChange = {})
            TerminalCheckbox(
                state = ToggleableState.Indeterminate,
                onClick = {},
                label = "--partial",
            )
            TerminalCheckbox(checked = true, onCheckedChange = {}, label = "Accept terms")
            TerminalCheckbox(checked = false, onCheckedChange = {}, label = "Disabled option", enabled = false)
        }
    }
}
