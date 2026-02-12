package com.m2f.template.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.theme.TerminalPreview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp

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
 * A terminal-styled badge component for displaying short inline labels.
 *
 * Renders a small pill-shaped label with variant-colored background and text.
 * Badges are display-only and have no click handler.
 * Reads all styling exclusively from [TerminalTheme] CompositionLocals.
 *
 * @param text The label text displayed inside the badge.
 * @param modifier Modifier applied to the root layout.
 * @param variant Display variant controlling color scheme.
 */
@Composable
fun TerminalBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Default,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders

    val shape = RoundedCornerShape(radius.pill)

    val (bgColor, fgColor, borderColor) = when (variant) {
        BadgeVariant.Default -> Triple(colors.surface, colors.text, colors.border)
        BadgeVariant.Accent -> Triple(colors.accentMuted, colors.accent, colors.accentMuted)
        BadgeVariant.Success -> Triple(colors.successBg, colors.success, colors.successBg)
        BadgeVariant.Warning -> Triple(colors.warningBg, colors.warning, colors.warningBg)
        BadgeVariant.Error -> Triple(colors.errorBg, colors.error, colors.errorBg)
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
        .padding(horizontal = spacing.sm, vertical = spacing.xs)

    BasicText(
        text = text,
        modifier = badgeModifier,
        style = typography.xs.copy(color = fgColor),
    )
}

@TerminalPreview
@Composable
private fun TerminalBadgePreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeVariant.entries.forEach { variant ->
                    TerminalBadge(text = variant.name, variant = variant)
                }
            }
        }
    }
}
