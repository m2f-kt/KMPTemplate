package com.m2f.template.designsystem.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * Button variants matching the Pencil terminal design system.
 */
enum class ButtonVariant { Default, Secondary, Ghost, Destructive }

/**
 * A terminal-styled button composable that reads all styling from [TerminalTheme].
 *
 * Supports 4 text variants via [ButtonVariant] and an optional leading icon slot.
 * Built exclusively with Foundation primitives (no Material3 dependencies).
 *
 * @param text The button label text.
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier Modifier applied to the root container.
 * @param variant The visual variant controlling colors and borders.
 * @param enabled Whether the button is interactive. Disabled buttons apply reduced opacity.
 * @param icon Optional leading icon composable rendered before the text.
 */
@Composable
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius
    val typography = TerminalTheme.typography
    val spacing = TerminalTheme.spacing
    val gap = TerminalTheme.gap
    val borders = TerminalTheme.borders
    val opacity = TerminalTheme.opacity

    val backgroundColor: Color
    val textColor: Color
    val hasBorder: Boolean

    when (variant) {
        ButtonVariant.Default -> {
            backgroundColor = colors.accent
            textColor = colors.surface
            hasBorder = false
        }

        ButtonVariant.Secondary -> {
            backgroundColor = colors.surface
            textColor = colors.text
            hasBorder = true
        }

        ButtonVariant.Ghost -> {
            backgroundColor = Color.Transparent
            textColor = colors.textMuted
            hasBorder = false
        }

        ButtonVariant.Destructive -> {
            backgroundColor = colors.errorBg
            textColor = colors.error
            hasBorder = false
        }
    }

    val shape = RoundedCornerShape(radius.sm)
    val alphaValue = if (enabled) opacity.full else opacity.medium

    Box(
        modifier = modifier
            .alpha(alphaValue)
            .clip(shape)
            .then(
                if (hasBorder) {
                    Modifier.border(borders.thin, colors.border, shape)
                } else {
                    Modifier
                },
            )
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(gap.sm),
        ) {
            if (icon != null) {
                icon()
            }
            BasicText(
                text = text,
                style = typography.sm.copy(color = textColor),
            )
        }
    }
}

/**
 * A square icon-only button styled from [TerminalTheme].
 *
 * Uses surface background with a thin border and rounded corners.
 * Built exclusively with Foundation primitives (no Material3 dependencies).
 *
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier Modifier applied to the root container.
 * @param enabled Whether the button is interactive.
 * @param content The icon composable rendered inside the button.
 */
@Composable
fun TerminalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders
    val opacity = TerminalTheme.opacity

    val shape = RoundedCornerShape(radius.sm)
    val alphaValue = if (enabled) opacity.full else opacity.medium

    Box(
        modifier = modifier
            .alpha(alphaValue)
            .clip(shape)
            .border(borders.thin, colors.border, shape)
            .background(colors.surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
