package com.m2f.template.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Popup
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * A terminal-styled tooltip component that shows floating text on hover.
 *
 * Wraps [content] and displays a small floating label with a surface background,
 * border, and shadow when the user hovers (Desktop/WASM) or long-presses (mobile).
 * Reads all styling exclusively from [TerminalTheme] CompositionLocals.
 *
 * @param text The tooltip text to display.
 * @param modifier Modifier applied to the wrapper container.
 * @param content The composable content that triggers the tooltip.
 */
@Composable
fun TerminalTooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val radius = TerminalTheme.radius
    val spacing = TerminalTheme.spacing
    val borders = TerminalTheme.borders
    val shadows = TerminalTheme.shadows

    var isVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isVisible = true
                            PointerEventType.Exit -> isVisible = false
                        }
                    }
                }
            },
    ) {
        content()

        if (isVisible) {
            Popup {
                val tooltipShape = RoundedCornerShape(radius.sm)
                Box(
                    modifier = Modifier
                        .shadow(shadows.sm.blur, tooltipShape)
                        .clip(tooltipShape)
                        .background(colors.surface)
                        .border(borders.thin, colors.border, tooltipShape)
                        .padding(horizontal = spacing.sm, vertical = spacing.xs),
                ) {
                    BasicText(
                        text = text,
                        style = typography.xs.copy(color = colors.text),
                    )
                }
            }
        }
    }
}
