package com.m2f.template.designsystem.components.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.m2f.template.designsystem.theme.TerminalPreview
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * State for a [TerminalListItem].
 */
enum class ListItemState {
    Default,
    Hover,
    Selected,
    Disabled,
}

/**
 * A themed list component that renders a titled list with items.
 *
 * @param modifier Modifier for the outer container.
 * @param title Optional title shown above the list items.
 * @param count Optional count displayed next to the title.
 * @param content Content lambda to add [TerminalListItem] entries.
 */
@Composable
fun TerminalList(
    modifier: Modifier = Modifier,
    title: String? = null,
    count: Int? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders
    val radius = TerminalTheme.radius

    val shape = RoundedCornerShape(radius.md)

    Column(
        modifier = modifier
            .clip(shape)
            .border(borders.thin, colors.border, shape)
            .background(colors.surface),
    ) {
        // Title row
        if (title != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.sm, vertical = spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = title,
                    style = typography.sm.copy(
                        color = colors.text,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )

                if (count != null) {
                    Spacer(modifier = Modifier.width(spacing.xs))
                    BasicText(
                        text = count.toString(),
                        style = typography.xs.copy(color = colors.textMuted),
                    )
                }
            }

            // Title bottom border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(borders.thin)
                    .background(colors.border),
            )
        }

        // List items
        content()
    }
}

/**
 * A single item in a [TerminalList].
 *
 * @param text Primary text label.
 * @param modifier Modifier for the item.
 * @param state The visual state of the item.
 * @param subtitle Optional secondary text below the primary text.
 * @param onClick Click handler; item is clickable when provided and not [ListItemState.Disabled].
 * @param leadingContent Optional composable content shown before the text.
 * @param trailingContent Optional composable content shown after the text.
 * @param showBottomBorder Whether to show a bottom border. Set to false for the last item.
 */
@Composable
fun TerminalListItem(
    text: String,
    modifier: Modifier = Modifier,
    state: ListItemState = ListItemState.Default,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    showBottomBorder: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders
    val opacity = TerminalTheme.opacity

    val backgroundColor = when (state) {
        ListItemState.Default -> colors.surface
        ListItemState.Hover -> colors.inset
        ListItemState.Selected -> colors.accentMuted
        ListItemState.Disabled -> colors.surface
    }

    val textColor = when (state) {
        ListItemState.Default -> colors.text
        ListItemState.Hover -> colors.text
        ListItemState.Selected -> colors.accent
        ListItemState.Disabled -> colors.textDim
    }

    val itemAlpha = if (state == ListItemState.Disabled) opacity.medium else opacity.full

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .background(backgroundColor)
            .then(
                if (onClick != null && state != ListItemState.Disabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(spacing.sm))
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = text,
                    style = typography.sm.copy(color = textColor),
                )
                if (subtitle != null) {
                    BasicText(
                        text = subtitle,
                        style = typography.xs.copy(color = colors.textMuted),
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(spacing.sm))
                trailingContent()
            }
        }

        if (showBottomBorder) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(borders.thin)
                    .background(colors.border),
            )
        }
    }
}

@TerminalPreview
@Composable
private fun TerminalListPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
        ) {
            TerminalList(title = "Team Members", count = 4) {
                TerminalListItem(text = "Alice", subtitle = "Admin", state = ListItemState.Default)
                TerminalListItem(text = "Bob", subtitle = "Editor", state = ListItemState.Hover)
                TerminalListItem(text = "Charlie", subtitle = "Viewer", state = ListItemState.Selected)
                TerminalListItem(text = "Dave", subtitle = "Locked", state = ListItemState.Disabled, showBottomBorder = false)
            }
        }
    }
}
