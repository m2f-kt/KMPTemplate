package com.m2f.template.designsystem.components.data

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.AuraPreview
import com.m2f.template.designsystem.theme.AuraTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A swipe-to-reveal wrapper that adds horizontal swipe gesture to any content.
 *
 * Swiping the foreground content to the left reveals action buttons behind it.
 * Swiping past the [revealWidth] threshold snaps the actions fully open; swiping
 * less snaps them closed. Tapping the foreground when actions are revealed closes them.
 *
 * Built entirely with Foundation primitives -- no Material3 dependencies.
 *
 * @param modifier Modifier applied to the outer container.
 * @param revealWidth The width of the action area revealed on swipe.
 * @param swipeActions Composable content for the action buttons, rendered in a [Row].
 * @param content The foreground content that can be swiped.
 */
@Composable
fun AuraSwipeReveal(
    modifier: Modifier = Modifier,
    revealWidth: Dp = 80.dp,
    swipeActions: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val maxRevealPx = with(density) { revealWidth.toPx() }
    val thresholdPx = maxRevealPx / 2f

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clipToBounds(),
    ) {
        // Actions row — behind the foreground, matching item height
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(revealWidth)
                .fillMaxHeight(),
            content = swipeActions,
        )

        // Foreground content — opaque background, slides left to reveal actions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(-offsetX.value.roundToInt(), 0) }
                .background(AuraTheme.colors.surface)
                .pointerInput(Unit) {
                    val touchSlop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        // Wait for initial press
                        do {
                            val pressEvent = awaitPointerEvent()
                        } while (pressEvent.type != PointerEventType.Press)
                        var dragging = false
                        var totalX = 0f

                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.all { !it.pressed }) {
                                // Pointer up — end of gesture
                                if (dragging) {
                                    coroutineScope.launch {
                                        if (offsetX.value > thresholdPx) {
                                            offsetX.animateTo(maxRevealPx, spring())
                                        } else {
                                            offsetX.animateTo(0f, spring())
                                        }
                                    }
                                } else if (offsetX.value > 0f) {
                                    // Tap while revealed — close
                                    coroutineScope.launch {
                                        offsetX.animateTo(0f, spring())
                                    }
                                }
                                break
                            }

                            val change = event.changes.first()
                            val dx = change.positionChange().x
                            totalX += dx

                            if (!dragging && abs(totalX) > touchSlop) {
                                dragging = true
                            }

                            if (dragging) {
                                change.consume()
                                coroutineScope.launch {
                                    val newOffset = (offsetX.value - dx)
                                        .coerceIn(0f, maxRevealPx)
                                    offsetX.snapTo(newOffset)
                                }
                            }
                        }
                    }
                },
        ) {
            content()
        }
    }
}

/**
 * A convenience composable for a delete swipe action.
 *
 * Renders a red delete button intended for use inside [AuraSwipeReveal]'s
 * `swipeActions` slot. Uses [AuraTheme.colors.error] for the background
 * and [AuraTheme.colors.btnDestructiveText] for the label.
 *
 * @param onClick Callback invoked when the delete action is tapped.
 * @param modifier Modifier applied to the action container.
 * @param label Text label displayed on the action button.
 */
@Composable
fun AuraDeleteAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Delete",
) {
    val colors = AuraTheme.colors
    val typography = AuraTheme.typography

    Box(
        modifier = modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(colors.error)
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = typography.xs.copy(
                color = colors.btnDestructiveText,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@AuraPreview
@Composable
private fun AuraSwipeRevealPreview() {
    AuraTheme {
        Column(
            modifier = Modifier
                .background(AuraTheme.colors.bg)
                .padding(16.dp),
        ) {
            AuraList(title = "swipe_actions", count = 3) {
                // Item 1: Default state with delete action
                AuraSwipeReveal(
                    swipeActions = { AuraDeleteAction(onClick = {}) },
                ) {
                    AuraListItem(
                        text = "node_process",
                        subtitle = "PID: 1234",
                        state = ListItemState.Default,
                    )
                }

                // Item 2: Selected state with delete action
                AuraSwipeReveal(
                    swipeActions = { AuraDeleteAction(onClick = {}) },
                ) {
                    AuraListItem(
                        text = "python_script",
                        subtitle = "PID: 5678",
                        state = ListItemState.Selected,
                    )
                }

                // Item 3: Default with custom two-action reveal (delete + archive)
                AuraSwipeReveal(
                    revealWidth = 160.dp,
                    swipeActions = {
                        AuraDeleteAction(onClick = {})
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .fillMaxHeight()
                                .background(AuraTheme.colors.info),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Archive",
                                style = AuraTheme.typography.xs.copy(
                                    color = AuraTheme.colors.surface,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    },
                ) {
                    AuraListItem(
                        text = "docker_container",
                        subtitle = "PID: 9012",
                        state = ListItemState.Default,
                    )
                }
            }
        }
    }
}
