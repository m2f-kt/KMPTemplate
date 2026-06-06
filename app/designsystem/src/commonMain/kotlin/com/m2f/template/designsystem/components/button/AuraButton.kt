package com.m2f.template.designsystem.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.m2f.template.designsystem.theme.AuraPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.AuraText
import com.m2f.template.designsystem.modifier.auraBorder
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.theme.rememberAuraRipple

/**
 * Button variants matching the Aura design system. `Aura` is the signature CTA: an elevated
 * fill wrapped in the animated aura border — use it for the single most important action on a surface.
 */
enum class ButtonVariant { Default, Secondary, Ghost, Destructive, Success, Aura }

/**
 * An Aura-styled button composable that reads all styling from [AuraTheme].
 *
 * Supports multiple text variants via [ButtonVariant], an optional leading icon slot,
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
fun AuraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = AuraTheme.colors
    val radius = AuraTheme.radius
    val typography = AuraTheme.typography
    val gap = AuraTheme.gap
    val borders = AuraTheme.borders

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

        ButtonVariant.Aura -> {
            variantBg = colors.surface
            variantText = colors.text
            variantHoverBg = colors.bgElev2
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
    val showAuraBorder = variant == ButtonVariant.Aura && enabled
    val horizontalPadding = if (variant == ButtonVariant.Default) 16.dp else 12.dp

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (showAuraBorder) Modifier.auraBorder(cornerRadius = radius.sm) else Modifier,
            )
            .then(
                if (hasBorder) {
                    Modifier.border(borders.thin, borderColor, shape)
                } else {
                    Modifier
                },
            )
            .background(resolvedBg)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberAuraRipple(),
                enabled = enabled,
                onClick = onClick,
            )
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
 * A square icon-only button styled from [AuraTheme].
 *
 * Supports multiple visual variants via [ButtonVariant] (defaults to Secondary).
 * Built exclusively with Foundation primitives (no Material3 dependencies).
 *
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier Modifier applied to the root container.
 * @param variant The visual variant controlling colors and borders.
 * @param enabled Whether the button is interactive.
 * @param content The icon composable rendered inside the button.
 */
@Composable
fun AuraIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Secondary,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = AuraTheme.colors
    val radius = AuraTheme.radius
    val spacing = AuraTheme.spacing
    val borders = AuraTheme.borders

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Resolve variant colors
    val variantBg: Color
    val variantBorderColor: Color
    val variantHoverBg: Color
    val variantHasBorder: Boolean

    when (variant) {
        ButtonVariant.Default -> {
            variantBg = colors.btnPrimaryBg
            variantBorderColor = Color.Transparent
            variantHoverBg = colors.btnPrimaryHoverBg
            variantHasBorder = false
        }
        ButtonVariant.Secondary -> {
            variantBg = colors.btnSecondaryBg
            variantBorderColor = colors.btnSecondaryBorder
            variantHoverBg = colors.btnSecondaryHoverBg
            variantHasBorder = true
        }
        ButtonVariant.Ghost -> {
            variantBg = Color.Transparent
            variantBorderColor = Color.Transparent
            variantHoverBg = colors.btnGhostHoverBg
            variantHasBorder = false
        }
        ButtonVariant.Destructive -> {
            variantBg = colors.btnDestructiveBg
            variantBorderColor = Color.Transparent
            variantHoverBg = colors.btnDestructiveHoverBg
            variantHasBorder = false
        }
        ButtonVariant.Success -> {
            variantBg = colors.btnSuccessBg
            variantBorderColor = Color.Transparent
            variantHoverBg = colors.btnSuccessHoverBg
            variantHasBorder = false
        }
        ButtonVariant.Aura -> {
            variantBg = colors.surface
            variantBorderColor = Color.Transparent
            variantHoverBg = colors.bgElev2
            variantHasBorder = false
        }
    }

    val backgroundColor: Color
    val borderColor: Color
    val hoverBg: Color
    val hasBorder: Boolean

    if (!enabled) {
        backgroundColor = colors.btnDisabledBg
        borderColor = colors.btnDisabledBorder
        hoverBg = colors.btnDisabledBg
        hasBorder = true
    } else {
        backgroundColor = variantBg
        borderColor = variantBorderColor
        hoverBg = variantHoverBg
        hasBorder = variantHasBorder
    }

    val resolvedBg = if (isHovered && enabled) hoverBg else backgroundColor

    val shape = RoundedCornerShape(radius.sm)

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
            .clickable(
                interactionSource = interactionSource,
                indication = rememberAuraRipple(),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@AuraPreview
@Composable
private fun AuraButtonPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraButton(text = "Default", onClick = {}, variant = ButtonVariant.Default)
            AuraButton(text = "Secondary", onClick = {}, variant = ButtonVariant.Secondary)
            AuraButton(text = "Ghost", onClick = {}, variant = ButtonVariant.Ghost)
            AuraButton(text = "Destructive", onClick = {}, variant = ButtonVariant.Destructive)
            AuraButton(text = "Success", onClick = {}, variant = ButtonVariant.Success)
            AuraButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}

@AuraPreview
@Composable
private fun AuraIconButtonPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraIconButton(onClick = {}, enabled = true) {
                AuraText("X", style = AuraTheme.typography.sm)
            }
            AuraIconButton(onClick = {}, enabled = false) {
                AuraText("X", style = AuraTheme.typography.sm)
            }
        }
    }
}
