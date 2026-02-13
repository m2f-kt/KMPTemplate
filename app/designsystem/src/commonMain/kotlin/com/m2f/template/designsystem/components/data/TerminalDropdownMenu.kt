package com.m2f.template.designsystem.components.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.m2f.template.designsystem.theme.TerminalPreview
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.theme.rememberTerminalRipple

/**
 * A terminal-styled floating dropdown menu that renders as a popup overlay.
 *
 * Displays menu content in a floating container with surface background, border,
 * shadow, and rounded corners matching the terminal design system. Built exclusively
 * with Foundation primitives and [Popup] from `androidx.compose.ui.window`.
 *
 * @param expanded Whether the menu is currently visible.
 * @param onDismissRequest Callback invoked when the menu should be dismissed.
 * @param modifier Modifier applied to the popup content container.
 * @param content Content lambda to add [TerminalDropdownMenuItem] entries.
 */
@Composable
fun TerminalDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return

    val colors = TerminalTheme.colors
    val borders = TerminalTheme.borders
    val radius = TerminalTheme.radius
    val shape = RoundedCornerShape(radius.md)

    Popup(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = modifier
                .width(160.dp)
                .clip(shape)
                .background(colors.surface)
                .border(borders.thin, colors.border, shape)
                .padding(vertical = 4.dp),
            content = content,
        )
    }
}

/**
 * A single item in a [TerminalDropdownMenu].
 *
 * Renders a hoverable, clickable row with text and an optional leading icon.
 * Hover state changes the background to the inset color. Styling reads from
 * [TerminalTheme] CompositionLocals.
 *
 * @param text The menu item label.
 * @param onClick Callback invoked when the item is clicked.
 * @param modifier Modifier applied to the item row.
 * @param leadingIcon Optional composable rendered before the text.
 */
@Composable
fun TerminalDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = if (isHovered) colors.inset else colors.surface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberTerminalRipple(),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        BasicText(
            text = text,
            style = typography.sm.copy(color = colors.text),
        )
    }
}

@TerminalPreview
@Composable
private fun TerminalDropdownMenuPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
        ) {
            // Show the menu content directly (not in a Popup) so it renders in preview
            val colors = TerminalTheme.colors
            val borders = TerminalTheme.borders
            val radius = TerminalTheme.radius
            val shape = RoundedCornerShape(radius.md)

            Box(
                modifier = Modifier
                    .width(160.dp)
                    .clip(shape)
                    .background(colors.surface)
                    .border(borders.thin, colors.border, shape)
                    .padding(vertical = 4.dp),
            ) {
                Column {
                    TerminalDropdownMenuItem(text = "View Details", onClick = {})
                    TerminalDropdownMenuItem(
                        text = "Terminate",
                        onClick = {},
                        leadingIcon = {
                            BasicText(
                                text = "X",
                                style = TerminalTheme.typography.sm.copy(
                                    color = TerminalTheme.colors.error,
                                ),
                            )
                        },
                    )
                    TerminalDropdownMenuItem(text = "Copy PID", onClick = {})
                }
            }
        }
    }
}
