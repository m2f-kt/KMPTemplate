package com.m2f.template.designsystem.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * Card variants matching the Pencil terminal design system.
 */
enum class CardVariant { Default, Accent, Info, Highlighted, Compact }

/**
 * A terminal-styled card container composable with 5 variants.
 *
 * All styling tokens (colors, radius, borders, spacing) are read from [TerminalTheme].
 * Built exclusively with Foundation primitives (no Material3 dependencies).
 *
 * @param modifier Modifier applied to the root Column container.
 * @param variant The visual variant controlling background, border colors, and padding.
 * @param content Content slot using [ColumnScope] for flexible vertical composition.
 */
@Composable
fun TerminalCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Default,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders

    val backgroundColor: Color
    val borderColor: Color
    val contentPadding = when (variant) {
        CardVariant.Compact -> spacing.sm
        else -> spacing.lg
    }

    when (variant) {
        CardVariant.Default -> {
            backgroundColor = colors.surface
            borderColor = colors.border
        }

        CardVariant.Accent -> {
            backgroundColor = colors.surface
            borderColor = colors.border
        }

        CardVariant.Info -> {
            backgroundColor = colors.infoBg
            borderColor = colors.info
        }

        CardVariant.Highlighted -> {
            backgroundColor = colors.surface
            borderColor = colors.accentMuted
        }

        CardVariant.Compact -> {
            backgroundColor = colors.surface
            borderColor = colors.border
        }
    }

    val shape = RoundedCornerShape(radius.md)

    // Accent variant draws a thick left edge in the accent color
    val accentColor = colors.accent
    val accentEdgeWidth = 4.dp
    val accentModifier = if (variant == CardVariant.Accent) {
        Modifier.drawBehind {
            drawRect(
                color = accentColor,
                topLeft = Offset.Zero,
                size = Size(accentEdgeWidth.toPx(), size.height),
            )
        }
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .clip(shape)
            .border(borders.thin, borderColor, shape)
            .background(backgroundColor)
            .then(accentModifier)
            .padding(contentPadding),
        content = content,
    )
}
