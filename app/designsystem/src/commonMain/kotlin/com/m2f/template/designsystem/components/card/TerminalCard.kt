package com.m2f.template.designsystem.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.m2f.template.designsystem.theme.TerminalPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.components.TerminalText
import com.m2f.template.designsystem.components.button.ButtonVariant
import com.m2f.template.designsystem.components.button.TerminalButton
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * Card variants matching the Pencil terminal design system.
 */
enum class CardVariant { Default, Accent, Info, Highlighted, Compact }

/**
 * A terminal-styled card with header/content/footer structure.
 *
 * Supports 4 visual variants via [CardVariant] (Default, Accent, Info, Highlighted).
 * For the horizontal Compact layout, use [TerminalCompactCard] instead.
 *
 * All styling tokens (colors, radius, borders, spacing, typography) are read from [TerminalTheme].
 * Built exclusively with Foundation primitives (no Material3 dependencies).
 *
 * @param title The card header title text.
 * @param modifier Modifier applied to the root container.
 * @param description Optional subtitle text shown below the title.
 * @param variant The visual variant controlling colors, borders, and structure.
 * @param icon Optional icon composable rendered on the right side of the header.
 * @param footer Optional footer composable (only rendered for [CardVariant.Default]).
 * @param content Content slot rendered between header and footer.
 */
@Composable
fun TerminalCard(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    variant: CardVariant = CardVariant.Default,
    icon: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing
    val gap = TerminalTheme.gap
    val borders = TerminalTheme.borders

    val shape = RoundedCornerShape(radius.sm)

    // Per-variant styling
    val containerBg: Color
    val borderWidth = when (variant) {
        CardVariant.Highlighted -> borders.`default`
        else -> borders.thin
    }
    val borderColor: Color
    val hasBorder: Boolean
    val headerBg: Color
    val titleColor: Color
    val titleWeight: FontWeight
    val descriptionColor: Color
    val iconTint: Color
    val hasHeaderBottomBorder: Boolean

    when (variant) {
        CardVariant.Default -> {
            containerBg = colors.surface
            borderColor = colors.border
            hasBorder = true
            headerBg = Color.Transparent
            titleColor = colors.text
            titleWeight = FontWeight.Medium
            descriptionColor = colors.textMuted
            iconTint = colors.textMuted
            hasHeaderBottomBorder = true
        }

        CardVariant.Accent -> {
            containerBg = colors.bg
            borderColor = Color.Transparent
            hasBorder = false
            headerBg = colors.textMuted
            titleColor = colors.surface
            titleWeight = FontWeight.Medium
            descriptionColor = colors.surface.copy(alpha = 0.7f)
            iconTint = colors.surface
            hasHeaderBottomBorder = false
        }

        CardVariant.Info -> {
            containerBg = colors.surface
            borderColor = colors.info
            hasBorder = true
            headerBg = colors.infoBg
            titleColor = colors.info
            titleWeight = FontWeight.Medium
            descriptionColor = colors.textMuted
            iconTint = colors.info
            hasHeaderBottomBorder = false
        }

        CardVariant.Highlighted -> {
            containerBg = colors.accentMuted
            borderColor = colors.accent
            hasBorder = true
            headerBg = Color.Transparent
            titleColor = colors.accent
            titleWeight = FontWeight.SemiBold
            descriptionColor = colors.textMuted
            iconTint = colors.accent
            hasHeaderBottomBorder = false
        }

        CardVariant.Compact -> {
            // Compact uses TerminalCompactCard; provide defaults to satisfy exhaustive when
            containerBg = colors.surface
            borderColor = colors.border
            hasBorder = true
            headerBg = Color.Transparent
            titleColor = colors.text
            titleWeight = FontWeight.Medium
            descriptionColor = colors.textMuted
            iconTint = colors.textMuted
            hasHeaderBottomBorder = false
        }
    }

    val borderModifier = if (hasBorder) {
        Modifier.border(borderWidth, borderColor, shape)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .clip(shape)
            .then(borderModifier)
            .background(containerBg),
    ) {
        // Header section
        CardHeader(
            title = title,
            description = description,
            icon = icon,
            backgroundColor = headerBg,
            titleColor = titleColor,
            titleWeight = titleWeight,
            descriptionColor = descriptionColor,
            iconTint = iconTint,
            hasBottomBorder = hasHeaderBottomBorder,
            bottomBorderColor = colors.border,
        )

        // Content slot
        CardContent(content = content)

        // Footer (Default only, when non-null)
        if (variant == CardVariant.Default && footer != null) {
            CardFooter(
                borderColor = colors.border,
                footer = footer,
            )
        }
    }
}

@Composable
private fun CardHeader(
    title: String,
    description: String?,
    icon: (@Composable () -> Unit)?,
    backgroundColor: Color,
    titleColor: Color,
    titleWeight: FontWeight,
    descriptionColor: Color,
    iconTint: Color,
    hasBottomBorder: Boolean,
    bottomBorderColor: Color,
) {
    val spacing = TerminalTheme.spacing
    val gap = TerminalTheme.gap
    val typography = TerminalTheme.typography

    val bottomBorderModifier = if (hasBottomBorder) {
        Modifier.drawBehind {
            drawLine(
                color = bottomBorderColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx(),
            )
        }
    } else {
        Modifier
    }

    val bgModifier = if (backgroundColor != Color.Transparent) {
        Modifier.background(backgroundColor)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(bgModifier)
            .then(bottomBorderModifier)
            .padding(vertical = spacing.lg, horizontal = spacing.xl),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(gap.xs),
        ) {
            TerminalText(
                text = title,
                style = typography.md.copy(fontWeight = titleWeight),
                color = titleColor,
            )
            if (description != null) {
                TerminalText(
                    text = description,
                    style = typography.xs,
                    color = descriptionColor,
                )
            }
        }

        if (icon != null) {
            icon()
        }
    }
}

@Composable
private fun CardContent(
    content: @Composable () -> Unit,
) {
    val spacing = TerminalTheme.spacing

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.xl),
    ) {
        content()
    }
}

@Composable
private fun CardFooter(
    borderColor: Color,
    footer: @Composable () -> Unit,
) {
    val spacing = TerminalTheme.spacing
    val gap = TerminalTheme.gap

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(vertical = spacing.md, horizontal = spacing.xl),
        horizontalArrangement = Arrangement.spacedBy(gap.md, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        footer()
    }
}

/**
 * A compact horizontal card for list-like items.
 *
 * Renders as a single Row with optional leading/trailing icons and a title+details column.
 * For the vertical header/content/footer layout, use [TerminalCard] instead.
 *
 * @param title The primary text label.
 * @param modifier Modifier applied to the root container.
 * @param details Optional secondary text shown below the title.
 * @param leadingIcon Optional composable rendered before the text column.
 * @param trailingIcon Optional composable rendered after the text column.
 * @param onClick Optional click handler. When non-null, the card becomes clickable.
 */
@Composable
fun TerminalCompactCard(
    title: String,
    modifier: Modifier = Modifier,
    details: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing
    val gap = TerminalTheme.gap
    val borders = TerminalTheme.borders
    val typography = TerminalTheme.typography

    val shape = RoundedCornerShape(radius.sm)

    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .clip(shape)
            .border(borders.thin, colors.border, shape)
            .background(colors.surface)
            .then(clickModifier)
            .padding(spacing.md),
        horizontalArrangement = Arrangement.spacedBy(gap.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(gap.xs),
        ) {
            TerminalText(
                text = title,
                style = typography.base.copy(fontWeight = FontWeight.Medium),
                color = colors.text,
            )
            if (details != null) {
                TerminalText(
                    text = details,
                    style = typography.xs,
                    color = colors.textDim,
                )
            }
        }

        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}

@TerminalPreview
@Composable
private fun TerminalCardPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(TerminalTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(TerminalTheme.gap.lg),
        ) {
            // 1. Default Card -- with footer
            TerminalCard(
                title = "Terminal Session",
                description = "Active connection",
                variant = CardVariant.Default,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    TerminalText(
                        text = ">_",
                        style = TerminalTheme.typography.sm,
                        color = TerminalTheme.colors.textMuted,
                    )
                },
                footer = {
                    TerminalButton(
                        text = "Cancel",
                        onClick = {},
                        variant = ButtonVariant.Ghost,
                    )
                    TerminalButton(
                        text = "Connect",
                        onClick = {},
                        variant = ButtonVariant.Default,
                    )
                },
            ) {
                TerminalText("Session content goes here")
            }

            // 2. Accent Card -- dark header, no footer
            TerminalCard(
                title = "Network Status",
                description = "All systems operational",
                variant = CardVariant.Accent,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    TerminalText(
                        text = "~",
                        style = TerminalTheme.typography.sm,
                        color = TerminalTheme.colors.surface,
                    )
                },
            ) {
                TerminalText("Monitoring dashboard content")
            }

            // 3. Info Card
            TerminalCard(
                title = "System Notice",
                description = "Scheduled maintenance",
                variant = CardVariant.Info,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    TerminalText(
                        text = "i",
                        style = TerminalTheme.typography.sm,
                        color = TerminalTheme.colors.info,
                    )
                },
            ) {
                TerminalText("The server will be restarted at 02:00 UTC.")
            }

            // 4. Highlighted Card
            TerminalCard(
                title = "Featured Project",
                description = "Pinned repository",
                variant = CardVariant.Highlighted,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    TerminalText(
                        text = "*",
                        style = TerminalTheme.typography.sm,
                        color = TerminalTheme.colors.accent,
                    )
                },
            ) {
                TerminalText("Project details and stats")
            }

            // 5. Compact Card
            TerminalCompactCard(
                title = "config.yaml",
                details = "Modified 2 hours ago",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    TerminalText(
                        text = "#",
                        style = TerminalTheme.typography.xs,
                        color = TerminalTheme.colors.textMuted,
                    )
                },
                trailingIcon = {
                    TerminalText(
                        text = ">",
                        style = TerminalTheme.typography.xs,
                        color = TerminalTheme.colors.textMuted,
                    )
                },
            )
        }
    }
}
