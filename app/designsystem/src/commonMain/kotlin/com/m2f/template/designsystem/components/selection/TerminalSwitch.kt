package com.m2f.template.designsystem.components.selection

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.m2f.template.designsystem.theme.TerminalPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.theme.rememberTerminalRipple

/**
 * A terminal-styled toggle switch component with accessibility support.
 *
 * Uses Foundation [toggleable] with [Role.Switch] for proper accessibility semantics.
 * Renders a rounded track with an animated sliding knob. The knob slides smoothly
 * between the off and on positions. Reads all styling from [TerminalTheme].
 *
 * @param checked Whether the switch is currently on.
 * @param onCheckedChange Callback invoked when the switch is toggled.
 * @param modifier Modifier applied to the root row layout.
 * @param label Optional text label displayed to the right of the switch.
 * @param enabled Whether the switch is interactive.
 */
@Composable
fun TerminalSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val opacity = TerminalTheme.opacity

    val trackWidth = 40.dp
    val trackHeight = 22.dp
    val knobSize = 16.dp
    val knobPadding = 3.dp

    val trackColor = if (checked) colors.btnPrimaryBg else colors.accentMuted
    val knobColor = if (checked) colors.btnPrimaryText else colors.textDim
    val contentAlpha = if (enabled) opacity.full else opacity.medium

    // Animate knob position
    val knobOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - knobSize - knobPadding else knobPadding,
        animationSpec = tween(durationMillis = 150),
    )

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                indication = rememberTerminalRipple(bounded = true),
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .alpha(contentAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(trackHeight)
                .clip(CircleShape)
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .offset(x = knobOffset)
                    .size(knobSize)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(knobColor),
            )
        }

        if (label != null) {
            Spacer(modifier = Modifier.width(TerminalTheme.spacing.sm))
            BasicText(
                text = label,
                style = typography.sm.copy(color = colors.text),
            )
        }
    }
}

@TerminalPreview
@Composable
private fun TerminalSwitchPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalSwitch(checked = false, onCheckedChange = {})
            TerminalSwitch(checked = true, onCheckedChange = {})
            TerminalSwitch(checked = true, onCheckedChange = {}, label = "Dark mode")
            TerminalSwitch(checked = false, onCheckedChange = {}, label = "Disabled", enabled = false)
        }
    }
}
