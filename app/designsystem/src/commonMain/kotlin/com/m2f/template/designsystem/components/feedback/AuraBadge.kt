package com.m2f.template.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * Badge display variants mapping to theme color pairs.
 */
enum class BadgeVariant {
    Default,
    Accent,
    Success,
    Warning,
    Error,
}

/**
 * An Aura-styled badge component for displaying short inline labels.
 *
 * Renders a small rounded (not pill) label with variant-colored background and text.
 * Badges are display-only and have no click handler.
 * Reads all styling exclusively from [AuraTheme] CompositionLocals.
 *
 * @param text The label text displayed inside the badge.
 * @param modifier Modifier applied to the root layout.
 * @param icon Optional leading icon text displayed before the label (e.g., checkmark, warning symbol).
 * @param variant Display variant controlling color scheme.
 */
@Composable
fun AuraBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    variant: BadgeVariant = BadgeVariant.Default,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val radius = AuraTheme.radius
    val borders = AuraTheme.borders

    val shape = RoundedCornerShape(radius.sm)

    val (bgColor, fgColor, borderColor) = when (variant) {
        BadgeVariant.Default -> Triple(colors.surface, colors.text, colors.border)
        BadgeVariant.Accent -> Triple(colors.btnPrimaryBg, colors.btnPrimaryText, colors.btnPrimaryBg)
        BadgeVariant.Success -> Triple(colors.successBg, colors.success, colors.successBg)
        BadgeVariant.Warning -> Triple(colors.warningBg, colors.warning, colors.warningBg)
        BadgeVariant.Error -> Triple(colors.errorBg, colors.error, colors.errorBg)
    }

    val fontWeight = when (variant) {
        BadgeVariant.Default -> FontWeight.Medium
        else -> FontWeight.SemiBold
    }

    val badgeModifier = modifier
        .clip(shape)
        .background(bgColor)
        .then(
            if (variant == BadgeVariant.Default) {
                Modifier.border(borders.thin, borderColor, shape)
            } else {
                Modifier
            },
        )
        .padding(horizontal = 10.dp, vertical = 4.dp)

    Row(
        modifier = badgeModifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            BasicText(
                text = icon,
                style = typography.xxs.copy(
                    color = fgColor,
                    fontWeight = fontWeight,
                ),
            )
        }
        BasicText(
            text = text,
            style = typography.xxs.copy(
                color = fgColor,
                fontWeight = fontWeight,
            ),
        )
    }
}

@AuraPreview
@Composable
private fun AuraBadgePreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AuraBadge(text = "v1.0.0", variant = BadgeVariant.Default)
                AuraBadge(text = "RUNNING", variant = BadgeVariant.Accent)
                AuraBadge(text = "PASSED", icon = "\u2713", variant = BadgeVariant.Success)
                AuraBadge(text = "PENDING", icon = "\u25D0", variant = BadgeVariant.Warning)
                AuraBadge(text = "FAILED", icon = "\u2715", variant = BadgeVariant.Error)
            }
        }
    }
}
