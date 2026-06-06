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
import com.m2f.template.designsystem.theme.AuraPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.theme.rememberAuraRipple

/**
 * An Aura-styled tri-state checkbox component with accessibility support.
 *
 * Uses Foundation [triStateToggleable] with [Role.Checkbox] for proper accessibility semantics.
 * Renders a square box with rounded corners; when checked or indeterminate, shows
 * a primary background with a checkmark or dash drawn via Canvas.
 * Reads all styling from [AuraTheme].
 *
 * @param state The tri-state value: [ToggleableState.On], [ToggleableState.Off], or [ToggleableState.Indeterminate].
 * @param onClick Callback invoked when the checkbox is clicked.
 * @param modifier Modifier applied to the root row layout.
 * @param label Optional text label displayed to the right of the checkbox.
 * @param enabled Whether the checkbox is interactive.
 */
@Composable
fun AuraCheckbox(
    state: ToggleableState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val radius = AuraTheme.radius
    val borders = AuraTheme.borders
    val opacity = AuraTheme.opacity

    val boxSize = 18.dp
    val shape = RoundedCornerShape(radius.sm)
    val contentAlpha = if (enabled) opacity.full else opacity.medium
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .triStateToggleable(
                state = state,
                interactionSource = interactionSource,
                indication = rememberAuraRipple(bounded = true),
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
            Spacer(modifier = Modifier.width(AuraTheme.spacing.sm))
            BasicText(
                text = label,
                style = typography.sm.copy(color = colors.text),
            )
        }
    }
}

/**
 * An Aura-styled checkbox component with accessibility support.
 *
 * This is a convenience overload that delegates to the tri-state [AuraCheckbox].
 * It maps [checked] to [ToggleableState.On] or [ToggleableState.Off].
 *
 * @param checked Whether the checkbox is currently checked.
 * @param onCheckedChange Callback invoked when the checkbox is toggled.
 * @param modifier Modifier applied to the root row layout.
 * @param label Optional text label displayed to the right of the checkbox.
 * @param enabled Whether the checkbox is interactive.
 */
@Composable
fun AuraCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    AuraCheckbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        label = label,
        enabled = enabled,
    )
}

@AuraPreview
@Composable
private fun AuraCheckboxPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraCheckbox(checked = false, onCheckedChange = {})
            AuraCheckbox(checked = true, onCheckedChange = {})
            AuraCheckbox(
                state = ToggleableState.Indeterminate,
                onClick = {},
                label = "--partial",
            )
            AuraCheckbox(checked = true, onCheckedChange = {}, label = "Accept terms")
            AuraCheckbox(checked = false, onCheckedChange = {}, label = "Disabled option", enabled = false)
        }
    }
}
