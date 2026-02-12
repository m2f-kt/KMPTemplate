package com.m2f.template.designsystem.components.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = title,
                    style = typography.sm.copy(
                        color = colors.text,
                        fontWeight = FontWeight.Medium,
                    ),
                )

                if (count != null) {
                    BasicText(
                        text = "[$count items]",
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
 * @param leadingContent Optional composable content shown before the text. Receives the state-appropriate icon color.
 * @param trailingContent Optional composable content shown after the text. Receives the state-appropriate action color.
 */
@Composable
fun TerminalListItem(
    text: String,
    modifier: Modifier = Modifier,
    state: ListItemState = ListItemState.Default,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable (iconColor: Color) -> Unit)? = null,
    trailingContent: (@Composable (actionColor: Color) -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
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

    val subtitleColor = when (state) {
        ListItemState.Default -> colors.textDim
        ListItemState.Hover -> colors.textMuted
        ListItemState.Selected -> colors.textMuted
        ListItemState.Disabled -> colors.textDim
    }

    val iconColor = when (state) {
        ListItemState.Default -> colors.textMuted
        ListItemState.Hover -> colors.text
        ListItemState.Selected -> colors.accent
        ListItemState.Disabled -> colors.textMuted
    }

    val actionColor = when (state) {
        ListItemState.Default -> colors.textMuted
        ListItemState.Hover -> colors.text
        ListItemState.Selected -> colors.accent
        ListItemState.Disabled -> colors.textDim
    }

    val accentColor = colors.accent

    val itemAlpha = if (state == ListItemState.Disabled) opacity.medium else opacity.full

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .background(backgroundColor)
            .then(
                if (state == ListItemState.Selected) {
                    Modifier.drawBehind {
                        drawRect(
                            color = accentColor,
                            topLeft = Offset.Zero,
                            size = size.copy(width = 2.dp.toPx()),
                        )
                    }
                } else {
                    Modifier
                },
            )
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                leadingContent(iconColor)
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = text,
                    style = typography.sm.copy(
                        color = textColor,
                        fontWeight = if (state == ListItemState.Selected) FontWeight.Medium else FontWeight.Normal,
                    ),
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    BasicText(
                        text = subtitle,
                        style = typography.xs.copy(color = subtitleColor),
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailingContent(actionColor)
            }
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
            TerminalList(title = "process_list", count = 4) {
                TerminalListItem(text = "node_process", subtitle = "PID: 1234", state = ListItemState.Default)
                TerminalListItem(text = "python_script", subtitle = "PID: 5678", state = ListItemState.Hover)
                TerminalListItem(text = "docker_container", subtitle = "PID: 9012", state = ListItemState.Selected)
                TerminalListItem(text = "legacy_service", subtitle = "PID: 3456", state = ListItemState.Disabled)
            }
        }
    }
}
