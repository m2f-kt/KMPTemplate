package com.m2f.template.designsystem.components.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.modifier.auraGlow
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.theme.rememberAuraRipple

/**
 * A pill-shaped segmented control for single-select among 2+ short options (the design's
 * `ModeToggle`). Each segment shows a status dot + label; the active segment lifts to the
 * `inset` surface with a strong-hairline border and recolors its dot to neon cyan with a glow.
 *
 * Uses Foundation [selectableGroup] + per-segment [selectable] with [Role.RadioButton] so it is
 * keyboard-reachable and announced as a single-choice group — a11y parity with [AuraRadio],
 * which it visually replaces. Reads all styling from [AuraTheme]; introduces no new tokens.
 *
 * @param options Ordered segment labels.
 * @param selectedIndex Index of the active segment.
 * @param onSelect Invoked with the tapped segment's index.
 * @param modifier Modifier for the container.
 */
@Composable
fun AuraSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AuraTheme.colors
    val borders = AuraTheme.borders
    val radius = AuraTheme.radius

    val containerPadding = 3.dp
    val shape = RoundedCornerShape(radius.pill)

    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.bgElev2)
            .border(borders.thin, colors.border, shape)
            .selectableGroup()
            .padding(containerPadding),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, label ->
            SegmentOption(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun SegmentOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val borders = AuraTheme.borders
    val radius = AuraTheme.radius
    val gap = AuraTheme.gap

    val dotSize = 6.dp
    val horizontalPadding = 14.dp
    val verticalPadding = 6.dp
    val shape = RoundedCornerShape(radius.pill)
    val interactionSource = remember { MutableInteractionSource() }

    val activeSurface = if (selected) {
        Modifier
            .background(colors.inset)
            .border(borders.thin, colors.borderStrong, shape)
    } else {
        Modifier
    }
    val dotGlow = if (selected) Modifier.auraGlow(AuraTheme.glows.cyan, CircleShape) else Modifier

    Row(
        modifier = Modifier
            .clip(shape)
            .then(activeSurface)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = rememberAuraRipple(bounded = true),
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.spacedBy(gap.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .then(dotGlow)
                .clip(CircleShape)
                .background(if (selected) colors.neonCyan else colors.textFaint),
        )
        BasicText(
            text = label,
            style = typography.sm.copy(
                color = if (selected) colors.text else colors.textDim,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@AuraPreview
@Composable
private fun AuraSegmentedControlPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraSegmentedControl(
                options = listOf("Hold to talk", "Tap to toggle"),
                selectedIndex = 0,
                onSelect = {},
            )
            AuraSegmentedControl(
                options = listOf("Hold to talk", "Tap to toggle"),
                selectedIndex = 1,
                onSelect = {},
            )
        }
    }
}
