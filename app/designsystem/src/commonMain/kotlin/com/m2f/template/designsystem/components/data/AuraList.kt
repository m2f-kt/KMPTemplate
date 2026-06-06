package com.m2f.template.designsystem.components.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.m2f.template.designsystem.theme.AuraPreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m2f.template.designsystem.theme.AuraTheme
import com.m2f.template.designsystem.theme.rememberAuraRipple

/**
 * State for a [AuraListItem].
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
 * @param content Content lambda to add [AuraListItem] entries.
 */
@Composable
fun AuraList(
    modifier: Modifier = Modifier,
    title: String? = null,
    count: Int? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val borders = AuraTheme.borders
    val radius = AuraTheme.radius

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
 * A single item in a [AuraList].
 *
 * @param text Primary text label.
 * @param modifier Modifier for the item.
 * @param state The visual state of the item.
 * @param subtitle Optional secondary text below the primary text.
 * @param onClick Click handler; item is clickable when provided and not [ListItemState.Disabled].
 * @param leadingContent Optional composable content shown before the text. Receives the state-appropriate icon color.
 * @param trailingContent Optional composable content shown after the text. Receives the state-appropriate action color.
 * @param menuItems Optional dropdown menu content. When provided, an ellipsis trigger replaces [trailingContent].
 */
@Composable
fun AuraListItem(
    text: String,
    modifier: Modifier = Modifier,
    state: ListItemState = ListItemState.Default,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable (iconColor: Color) -> Unit)? = null,
    trailingContent: (@Composable (actionColor: Color) -> Unit)? = null,
    menuItems: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography
    val opacity = AuraTheme.opacity

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

    val interactionSource = remember { MutableInteractionSource() }
    val menuInteractionSource = remember { MutableInteractionSource() }
    var menuExpanded by remember { mutableStateOf(false) }

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
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberAuraRipple(),
                        onClick = onClick,
                    )
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

            if (menuItems != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Box {
                    BasicText(
                        text = "\u22EF",
                        modifier = Modifier
                            .clickable(
                                interactionSource = menuInteractionSource,
                                indication = rememberAuraRipple(bounded = false),
                                enabled = state != ListItemState.Disabled,
                            ) {
                                menuExpanded = true
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        style = typography.sm.copy(
                            color = actionColor,
                            letterSpacing = 2.sp,
                        ),
                    )
                    AuraDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        menuItems()
                    }
                }
            } else if (trailingContent != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailingContent(actionColor)
            }
        }
    }
}

@AuraPreview
@Composable
private fun AuraListPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
        ) {
            AuraList(title = "process_list", count = 5) {
                AuraSwipeReveal(
                    swipeActions = { AuraDeleteAction(onClick = {}) },
                ) {
                    AuraListItem(
                        text = "node_process",
                        subtitle = "PID: 1234",
                        state = ListItemState.Default,
                        menuItems = {
                            AuraDropdownMenuItem(text = "View Details", onClick = {})
                            AuraDropdownMenuItem(text = "Terminate", onClick = {})
                            AuraDropdownMenuItem(text = "Copy PID", onClick = {})
                        },
                    )
                }
                AuraSwipeReveal(
                    swipeActions = { AuraDeleteAction(onClick = {}) },
                ) {
                    AuraListItem(text = "python_script", subtitle = "PID: 5678", state = ListItemState.Hover)
                }
                AuraListItem(text = "docker_container", subtitle = "PID: 9012", state = ListItemState.Selected)
                AuraListItem(
                    text = "legacy_service",
                    subtitle = "PID: 3456",
                    state = ListItemState.Disabled,
                    menuItems = {
                        AuraDropdownMenuItem(text = "View Details", onClick = {})
                    },
                )
                AuraListItem(text = "background_worker", subtitle = "PID: 7890", state = ListItemState.Default)
            }
        }
    }
}
