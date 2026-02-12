package com.m2f.template.designsystem.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * Button variants matching the Pencil terminal design system.
 */
enum class ButtonVariant { Default, Secondary, Ghost, Destructive, Success }

/**
 * A terminal-styled button composable that reads all styling from [TerminalTheme].
 *
 * Supports 5 text variants via [ButtonVariant], an optional leading icon slot,
 * explicit disabled styling (not alpha-based), and hover states via InteractionSource.
 * Built exclusively with Foundation primitives (no Material3 dependencies).
 *
 * @param text The button label text.
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier Modifier applied to the root container.
 * @param variant The visual variant controlling colors and borders.
 * @param enabled Whether the button is interactive. Disabled buttons use specific disabled colors.
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
    val gap = TerminalTheme.gap
    val borders = TerminalTheme.borders

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Resolve variant colors
    val variantBg: Color
    val variantText: Color
    val variantHoverBg: Color
    val variantHasBorder: Boolean
    val variantBorderColor: Color

    when (variant) {
        ButtonVariant.Default -> {
            variantBg = colors.btnPrimaryBg
            variantText = colors.btnPrimaryText
            variantHoverBg = colors.btnPrimaryHoverBg
            variantHasBorder = false
            variantBorderColor = Color.Transparent
        }

        ButtonVariant.Secondary -> {
            variantBg = colors.btnSecondaryBg
            variantText = colors.btnSecondaryText
            variantHoverBg = colors.btnSecondaryHoverBg
            variantHasBorder = true
            variantBorderColor = colors.btnSecondaryBorder
        }

        ButtonVariant.Ghost -> {
            variantBg = Color.Transparent
            variantText = colors.btnGhostText
            variantHoverBg = colors.btnGhostHoverBg
            variantHasBorder = false
            variantBorderColor = Color.Transparent
        }

        ButtonVariant.Destructive -> {
            variantBg = colors.btnDestructiveBg
            variantText = colors.btnDestructiveText
            variantHoverBg = colors.btnDestructiveHoverBg
            variantHasBorder = false
            variantBorderColor = Color.Transparent
        }

        ButtonVariant.Success -> {
            variantBg = colors.btnSuccessBg
            variantText = colors.btnSuccessText
            variantHoverBg = colors.btnSuccessHoverBg
            variantHasBorder = false
            variantBorderColor = Color.Transparent
        }
    }

    // Override for disabled state
    val backgroundColor: Color
    val textColor: Color
    val hasBorder: Boolean
    val borderColor: Color
    val hoverBg: Color

    if (!enabled) {
        backgroundColor = colors.btnDisabledBg
        textColor = colors.btnDisabledText
        hasBorder = true
        borderColor = colors.btnDisabledBorder
        hoverBg = colors.btnDisabledBg
    } else {
        backgroundColor = variantBg
        textColor = variantText
        hasBorder = variantHasBorder
        borderColor = variantBorderColor
        hoverBg = variantHoverBg
    }

    val resolvedBg = if (isHovered && enabled) hoverBg else backgroundColor

    val shape = RoundedCornerShape(radius.sm)
    val horizontalPadding = if (variant == ButtonVariant.Default) 16.dp else 12.dp

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (hasBorder) {
                    Modifier.border(borders.thin, borderColor, shape)
                } else {
                    Modifier
                },
            )
            .background(resolvedBg)
            .hoverable(interactionSource)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
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
                style = typography.sm.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

/**
 * A square icon-only button styled from [TerminalTheme].
 *
 * Uses Secondary styling (surface background with border) and hover states.
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

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor: Color
    val borderColor: Color
    val hoverBg: Color

    if (!enabled) {
        backgroundColor = colors.btnDisabledBg
        borderColor = colors.btnDisabledBorder
        hoverBg = colors.btnDisabledBg
    } else {
        backgroundColor = colors.btnSecondaryBg
        borderColor = colors.btnSecondaryBorder
        hoverBg = colors.btnSecondaryHoverBg
    }

    val resolvedBg = if (isHovered && enabled) hoverBg else backgroundColor

    val shape = RoundedCornerShape(radius.sm)

    Box(
        modifier = modifier
            .clip(shape)
            .border(borders.thin, borderColor, shape)
            .background(resolvedBg)
            .hoverable(interactionSource)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview
@Composable
private fun TerminalButtonPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalButton(text = "Default", onClick = {}, variant = ButtonVariant.Default)
            TerminalButton(text = "Secondary", onClick = {}, variant = ButtonVariant.Secondary)
            TerminalButton(text = "Ghost", onClick = {}, variant = ButtonVariant.Ghost)
            TerminalButton(text = "Destructive", onClick = {}, variant = ButtonVariant.Destructive)
            TerminalButton(text = "Success", onClick = {}, variant = ButtonVariant.Success)
            TerminalButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}

@Preview
@Composable
private fun TerminalIconButtonPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TerminalIconButton(onClick = {}, enabled = true) {
                TerminalText("X", style = TerminalTheme.typography.sm)
            }
            TerminalIconButton(onClick = {}, enabled = false) {
                TerminalText("X", style = TerminalTheme.typography.sm)
            }
        }
    }
}
