package com.m2f.template.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme

/**
 * Variants of [AuraModeBadge].
 *
 * - [Primary] — info-themed pill.
 * - [Secondary] — success-themed pill, shows a status lamp by default.
 */
enum class ModeBadgeVariant {
    Primary,
    Secondary,
}

/**
 * An Aura-styled two-variant mode badge.
 *
 * Renders a pill announcing a mode with an optional leading icon, a stacked code+label column,
 * and an optional trailing status lamp.
 *
 * @param code The short uppercase code shown above the label (e.g. `"MODE.CLD"`, `"MODE.DEV"`).
 * @param label The human-readable label shown below the code (e.g. `"Online"`, `"Offline"`).
 * @param modifier Modifier applied to the root pill.
 * @param mode Variant controlling color theming.
 * @param showLamp Whether to render the trailing status lamp dot.
 * @param icon Optional leading icon slot (e.g. a `cloud` or `shield-check` glyph). When `null`,
 *   the icon column collapses.
 */
@Composable
fun AuraModeBadge(
    code: String,
    label: String,
    modifier: Modifier = Modifier,
    mode: ModeBadgeVariant = ModeBadgeVariant.Primary,
    showLamp: Boolean = mode == ModeBadgeVariant.Secondary,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val borders = AuraTheme.borders
    val gap = AuraTheme.gap

    val (bgColor, accentColor) = when (mode) {
        ModeBadgeVariant.Primary -> colors.infoBg to colors.info
        ModeBadgeVariant.Secondary -> colors.successBg to colors.success
    }

    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(borders.thin, accentColor, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(gap.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                text = code,
                style = typography.chartAxis.copy(
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                ),
            )
            BasicText(
                text = label,
                style = typography.md.copy(
                    color = colors.text,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
        if (showLamp) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor),
            )
        }
    }
}

@AuraPreview
@Composable
private fun AuraModeBadgePreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AuraModeBadge(
                code = "MODE.CLD",
                label = "Online",
                mode = ModeBadgeVariant.Primary,
                icon = { GlyphDot(color = AuraTheme.colors.info) },
            )
            AuraModeBadge(
                code = "MODE.DEV",
                label = "Offline",
                mode = ModeBadgeVariant.Secondary,
                icon = { GlyphDot(color = AuraTheme.colors.success) },
            )
        }
    }
}

@Composable
private fun GlyphDot(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color),
    )
}
