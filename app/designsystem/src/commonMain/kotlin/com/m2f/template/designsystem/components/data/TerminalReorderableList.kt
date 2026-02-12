package com.m2f.template.designsystem.components.data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.m2f.template.designsystem.theme.TerminalPreview
import com.m2f.template.designsystem.theme.TerminalTheme

/**
 * State holder for tracking drag-to-reorder gesture state.
 *
 * Tracks the currently dragged item index and vertical drag offset.
 * Use [rememberReorderState] to create and remember an instance.
 */
class ReorderState {
    /**
     * Index of the item currently being dragged, or -1 when not dragging.
     */
    var draggedIndex by mutableIntStateOf(-1)
        internal set

    /**
     * Vertical pixel offset of the dragged item from its original position.
     */
    var dragOffset by mutableFloatStateOf(0f)
        internal set

    /**
     * Whether a drag operation is currently in progress.
     */
    val isDragging: Boolean get() = draggedIndex >= 0
}

/**
 * Creates and remembers a [ReorderState] instance.
 */
@Composable
fun rememberReorderState(): ReorderState = remember { ReorderState() }

/**
 * A typed, reorderable list composable with long-press drag-to-reorder gesture.
 *
 * Long-pressing an item initiates a drag operation. Dragging vertically reorders
 * items and calls the [onMove] callback. The dragged item receives visual elevation
 * feedback (scale and shadow). Built with Foundation [LazyColumn] and pointer input
 * gestures -- no Material3 dependencies.
 *
 * The visual container matches [TerminalList] styling: rounded corners, border,
 * surface background, and optional title row.
 *
 * @param items The list of items to display.
 * @param onMove Callback invoked when an item is reordered, with source and target indices.
 * @param modifier Modifier applied to the outer container.
 * @param key Optional key factory for stable item identity during reordering.
 * @param title Optional title shown above the list items.
 * @param count Optional count displayed next to the title.
 * @param state The [ReorderState] tracking drag gesture progress.
 * @param itemContent Content lambda for each item, receiving index, item, and whether it is being dragged.
 */
@Composable
fun <T> TerminalReorderableList(
    items: List<T>,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    title: String? = null,
    count: Int? = null,
    state: ReorderState = rememberReorderState(),
    itemContent: @Composable (index: Int, item: T, isDragged: Boolean) -> Unit,
) {
    val colors = TerminalTheme.colors
    val typography = TerminalTheme.typography
    val borders = TerminalTheme.borders
    val radius = TerminalTheme.radius

    val shape = RoundedCornerShape(radius.md)
    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier
            .clip(shape)
            .border(borders.thin, colors.border, shape)
            .background(colors.surface),
    ) {
        // Title row (matches TerminalList pattern)
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

        LazyColumn(
            state = lazyListState,
        ) {
            itemsIndexed(
                items = items,
                key = if (key != null) { index, item -> key(item) } else null,
            ) { index, item ->
                val isDragged = index == state.draggedIndex

                Box(
                    modifier = Modifier
                        .then(
                            if (isDragged) {
                                Modifier.zIndex(1f)
                            } else {
                                Modifier
                            },
                        )
                        .then(
                            if (isDragged) {
                                Modifier.graphicsLayer {
                                    translationY = state.dragOffset
                                    scaleX = 1.02f
                                    scaleY = 1.02f
                                    shadowElevation = 8f
                                }
                            } else {
                                Modifier
                            },
                        )
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    state.draggedIndex = index
                                    state.dragOffset = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    state.dragOffset += dragAmount.y

                                    val currentItemInfo = lazyListState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.index == state.draggedIndex }
                                    if (currentItemInfo != null) {
                                        val draggedCenter =
                                            currentItemInfo.offset + currentItemInfo.size / 2 + state.dragOffset.toInt()
                                        val targetItem = lazyListState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { info ->
                                                draggedCenter in info.offset..(info.offset + info.size)
                                            }
                                        if (targetItem != null && targetItem.index != state.draggedIndex) {
                                            onMove(state.draggedIndex, targetItem.index)
                                            state.dragOffset -= (targetItem.offset - currentItemInfo.offset).toFloat()
                                            state.draggedIndex = targetItem.index
                                        }
                                    }
                                },
                                onDragEnd = {
                                    state.draggedIndex = -1
                                    state.dragOffset = 0f
                                },
                                onDragCancel = {
                                    state.draggedIndex = -1
                                    state.dragOffset = 0f
                                },
                            )
                        },
                ) {
                    itemContent(index, item, isDragged)
                }
            }
        }
    }
}

@TerminalPreview
@Composable
private fun TerminalReorderableListPreview() {
    TerminalTheme {
        Column(
            modifier = Modifier
                .background(TerminalTheme.colors.bg)
                .padding(16.dp),
        ) {
            val items = remember {
                mutableStateListOf(
                    "node_process" to "PID: 1234",
                    "python_script" to "PID: 5678",
                    "docker_container" to "PID: 9012",
                    "background_worker" to "PID: 3456",
                    "cron_job" to "PID: 7777",
                )
            }
            TerminalReorderableList(
                items = items,
                onMove = { from, to -> items.add(to, items.removeAt(from)) },
                key = { it.first },
                title = "reorderable_processes",
                count = items.size,
            ) { _, item, isDragged ->
                TerminalListItem(
                    text = item.first,
                    subtitle = item.second,
                    state = if (isDragged) ListItemState.Selected else ListItemState.Default,
                )
            }
        }
    }
}
