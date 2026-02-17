---
phase: quick-15
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalSwipeReveal.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalReorderableList.kt
  - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
autonomous: true
must_haves:
  truths:
    - "Swiping a list item left reveals action buttons behind it"
    - "Tapping a revealed item or elsewhere snaps it closed"
    - "TerminalDeleteAction renders a red delete button composable"
    - "Long-pressing an item in TerminalReorderableList initiates drag"
    - "Dragging vertically reorders items and calls onMove callback"
    - "Dragged item shows visual elevation feedback"
    - "Existing TerminalList remains unchanged (non-breaking)"
  artifacts:
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalSwipeReveal.kt"
      provides: "Swipe-to-reveal wrapper composable and TerminalDeleteAction"
      exports: ["TerminalSwipeReveal", "TerminalDeleteAction"]
    - path: "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalReorderableList.kt"
      provides: "Reorderable list with drag-to-reorder and state holder"
      exports: ["TerminalReorderableList", "rememberReorderState"]
  key_links:
    - from: "TerminalSwipeReveal.kt"
      to: "TerminalTheme"
      via: "color tokens for action background"
      pattern: "TerminalTheme\\.colors"
    - from: "TerminalSwipeReveal.kt"
      to: "TerminalListItem"
      via: "wraps any content including TerminalListItem"
      pattern: "content\\(\\)"
    - from: "TerminalReorderableList.kt"
      to: "LazyColumn"
      via: "uses LazyColumn for indexed item rendering"
      pattern: "LazyColumn"
    - from: "TerminalReorderableList.kt"
      to: "onMove callback"
      via: "hoisted state reorder callback"
      pattern: "onMove"
---

<objective>
Add swipe-to-reveal actions and drag-to-reorder gestures to the TerminalList family.

Purpose: Enable interactive list behaviors -- swiping items to reveal contextual actions (delete, edit) and long-press dragging to reorder items -- completing the data component gesture repertoire.

Output: Two new composable files (TerminalSwipeReveal.kt, TerminalReorderableList.kt) plus an updated preview in TerminalList.kt demonstrating swipe-to-reveal usage.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalTheme.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/feedback/TerminalTooltip.kt
@app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create TerminalSwipeReveal wrapper composable with TerminalDeleteAction</name>
  <files>
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalSwipeReveal.kt
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt
  </files>
  <action>
Create `TerminalSwipeReveal.kt` in the `components/data` package with these composables:

**TerminalSwipeReveal** -- a generic wrapper that adds swipe-to-reveal behavior to any content:

```kotlin
@Composable
fun TerminalSwipeReveal(
    modifier: Modifier = Modifier,
    swipeActions: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit,
)
```

Implementation details:
- Use `Animatable<Float, AnimationVector1D>` (from `androidx.compose.animation.core`) initialized to `0f` to track the horizontal offset of the foreground content. The offset represents how far left the content has been swiped (positive values = swiped further left = more actions revealed).
- Use `pointerInput(Unit)` with `detectHorizontalDragGestures` (from `androidx.compose.foundation.gestures`) for the swipe gesture. On each drag delta: `snapTo(offsetX - delta)` (negative delta = swipe left = increase offset). Clamp offset to `0f..maxRevealPx` range.
- `onDragEnd`: if the current offset > threshold (80.dp converted to px via `with(LocalDensity.current)`), `animateTo(maxRevealPx)` to snap open. Otherwise `animateTo(0f)` to snap closed. Use `spring()` spec for snappy feel.
- `onDragCancel`: same as drag end -- animate based on threshold.
- `maxRevealPx`: measure the actions row width. Use a `SubcomposeLayout` or simpler approach: set a fixed max reveal width of 80.dp (single action) or make it configurable via a `revealWidth: Dp = 80.dp` parameter. Use the simpler fixed approach: `revealWidth` parameter defaulting to `80.dp`, convert to px for animation bounds.
- Layout structure (use `Box` with `clipToBounds()`):
  1. Actions row: `Row` positioned at the end (right side), `Modifier.align(Alignment.CenterEnd).width(revealWidth)`, with `colors.errorBg` background. Contains the `swipeActions` content.
  2. Foreground: `Box` with `Modifier.offset { IntOffset(-offsetX.value.roundToInt(), 0) }` wrapping the `content()`. Also add a `pointerInput` tap detector: if the offset > 0 (actions are visible), intercept the tap and `animateTo(0f)` to close.
- For the tap-to-close on the foreground when revealed: inside the foreground Box, add `.pointerInput(Unit) { detectTapGestures { if (offsetX.value > 0f) { coroutineScope.launch { offsetX.animateTo(0f) } } } }`. Import `detectTapGestures` from `androidx.compose.foundation.gestures`.
- Use `rememberCoroutineScope()` for launching animations from gesture callbacks.
- All imports from Foundation/Compose only -- NO Material3.

**TerminalDeleteAction** -- a convenience composable for the common "delete" swipe action:

```kotlin
@Composable
fun TerminalDeleteAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Delete",
)
```

Implementation:
- `Box` filling max height with `colors.error` background (using `TerminalTheme.colors.error`), clickable with `onClick`.
- Center a `BasicText` with the label in `typography.xs.copy(color = colors.btnDestructiveText, fontWeight = FontWeight.Medium)`.
- Minimum width matching the default `revealWidth` (80.dp), centered content.

**Preview** -- Add a `@TerminalPreview` function `TerminalSwipeRevealPreview` in the same file showing:
- A `TerminalList` with 3 items, each wrapped in `TerminalSwipeReveal`:
  - Item 1: Default state with delete action
  - Item 2: Selected state with delete action
  - Item 3: Default with custom two-action reveal (delete + archive)
- Follow the existing preview pattern: `private fun`, `TerminalTheme { Column(Modifier.background(colors.bg).padding(16.dp)) { ... } }`.

**Update TerminalList.kt preview** -- In the existing `TerminalListPreview`, wrap 1-2 of the list items in `TerminalSwipeReveal` to demonstrate integration. Keep the existing items structure; just wrap the first item:
```kotlin
TerminalSwipeReveal(
    swipeActions = { TerminalDeleteAction(onClick = {}) }
) {
    TerminalListItem(text = "node_process", ...)
}
```

Key patterns to follow:
- `pointerInput` pattern from TerminalTooltip.kt (awaitPointerEventScope)
- `animateDpAsState` / animation pattern from TerminalSwitch.kt
- Foundation-only: `BasicText`, `Box`, `Row`, `Modifier.offset`, `Modifier.clipToBounds`
- Theme access: `TerminalTheme.colors`, `TerminalTheme.typography`
- KDoc on all public composables matching the codebase style
  </action>
  <verify>
Build the designsystem module:
```bash
./gradlew :app:designsystem:compileKotlinDesktop
```
Verify no compilation errors. Check that TerminalSwipeReveal and TerminalDeleteAction are public and importable.
  </verify>
  <done>
TerminalSwipeReveal wraps content with horizontal swipe gesture that reveals action buttons. Swipe past 80dp threshold snaps open, below snaps closed. Tap-to-close when revealed. TerminalDeleteAction renders a red delete button. Preview shows swipe-to-reveal on list items. TerminalList preview updated to demonstrate integration.
  </done>
</task>

<task type="auto">
  <name>Task 2: Create TerminalReorderableList with drag-to-reorder gesture</name>
  <files>
    app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalReorderableList.kt
  </files>
  <action>
Create `TerminalReorderableList.kt` in the `components/data` package with these composables:

**ReorderState** -- internal state holder for drag tracking:

```kotlin
class ReorderState {
    var draggedIndex by mutableIntStateOf(-1)
        internal set
    var dragOffset by mutableFloatStateOf(0f)
        internal set
    val isDragging: Boolean get() = draggedIndex >= 0
}

@Composable
fun rememberReorderState(): ReorderState = remember { ReorderState() }
```

**TerminalReorderableList** -- a typed, reorderable list composable:

```kotlin
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
)
```

Implementation details:
- Outer container: same `Column` + `clip(shape)` + `border` + `background(colors.surface)` structure as `TerminalList` for visual consistency. Include the optional title row (copy the pattern from TerminalList.kt).
- Inside, use `LazyColumn` with `rememberLazyListState()` for the items.
- For each item in `items.forEachIndexed` (via `LazyColumn` `itemsIndexed`):
  - Use `key` parameter if provided: `items(count = items.size, key = if (key != null) { index -> key(items[index]) } else null)`
  - Each item gets a `pointerInput` modifier with `detectDragGesturesAfterLongPress`:
    - `onDragStart`: set `state.draggedIndex = thisIndex`, `state.dragOffset = 0f`
    - `onDrag(_, dragAmount)`: accumulate `state.dragOffset += dragAmount.y`. Calculate target index based on offset and item heights. Use `lazyListState.layoutInfo.visibleItemsInfo` to find which item index the dragged item has moved over. If target differs from current position, call `onMove(state.draggedIndex, targetIndex)` and update `state.draggedIndex = targetIndex`, reset `state.dragOffset` partially.
    - `onDragEnd`: reset `state.draggedIndex = -1`, `state.dragOffset = 0f`
    - `onDragCancel`: same as `onDragEnd`
  - Visual feedback for the dragged item (`isDragged = index == state.draggedIndex`):
    - Apply `Modifier.graphicsLayer { translationY = state.dragOffset; scaleX = 1.02f; scaleY = 1.02f; shadowElevation = 8f }` when `isDragged` is true.
    - Apply `Modifier.zIndex(1f)` to the dragged item so it renders above others.
  - The `itemContent` lambda receives `isDragged` so the caller can further customize (e.g., change background).

- Reorder logic detail: When calculating the target index during drag:
  ```kotlin
  val currentItemInfo = lazyListState.layoutInfo.visibleItemsInfo
      .firstOrNull { it.index == state.draggedIndex }
  if (currentItemInfo != null) {
      val draggedCenter = currentItemInfo.offset + currentItemInfo.size / 2 + state.dragOffset.toInt()
      val targetItem = lazyListState.layoutInfo.visibleItemsInfo
          .firstOrNull { draggedCenter in it.offset..(it.offset + it.size) }
      if (targetItem != null && targetItem.index != state.draggedIndex) {
          onMove(state.draggedIndex, targetItem.index)
          state.draggedIndex = targetItem.index
          // Partially reset offset to account for the swap
          state.dragOffset -= (targetItem.offset - currentItemInfo.offset).toFloat()
      }
  }
  ```

- IMPORTANT: `state.draggedIndex` and `state.dragOffset` use `mutableIntStateOf` and `mutableFloatStateOf` respectively (Compose primitive state for performance).

**Preview** -- Add `@TerminalPreview` function `TerminalReorderableListPreview`:

```kotlin
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
            ) { index, item, isDragged ->
                TerminalListItem(
                    text = item.first,
                    subtitle = item.second,
                    state = if (isDragged) ListItemState.Selected else ListItemState.Default,
                )
            }
        }
    }
}
```

Key patterns:
- Foundation-only: `LazyColumn`, `graphicsLayer`, `pointerInput`, `detectDragGesturesAfterLongPress`
- No Material3 (no `Surface`, no `ElevatedCard`, etc.)
- Title row matches TerminalList.kt pattern exactly (copy-paste the title section)
- Theme access via `TerminalTheme.colors`, `TerminalTheme.typography`, etc.
- KDoc on all public composables
  </action>
  <verify>
Build the designsystem module:
```bash
./gradlew :app:designsystem:compileKotlinDesktop
```
Verify no compilation errors. Check that TerminalReorderableList, rememberReorderState, and ReorderState are public and importable.
  </verify>
  <done>
TerminalReorderableList renders typed items in a LazyColumn with long-press drag-to-reorder. Dragged item shows elevation and scale feedback. onMove callback fires on reorder for hoisted state management. ReorderState tracks drag position. Existing TerminalList remains completely unchanged. Preview demonstrates reorderable list with mutableStateListOf.
  </done>
</task>

</tasks>

<verification>
After both tasks complete:
1. `./gradlew :app:designsystem:compileKotlinDesktop` -- compiles without errors
2. TerminalSwipeReveal, TerminalDeleteAction, TerminalReorderableList, rememberReorderState are all public exports
3. TerminalList.kt is unchanged except for the preview function update
4. No Material3 imports anywhere in the new files
5. All new composables have KDoc comments
6. Preview functions follow the established pattern (private, TerminalTheme wrapper, bg background)
</verification>

<success_criteria>
- TerminalSwipeReveal wraps any content with left-swipe gesture revealing action buttons
- TerminalDeleteAction provides a red delete convenience action
- Snap-open/snap-closed threshold behavior at 80dp
- Tap-to-close when actions are revealed
- TerminalReorderableList renders indexed items in LazyColumn
- Long-press initiates drag with visual elevation/scale feedback
- onMove callback enables hoisted state reordering
- Both composables are Foundation-only, cross-platform (KMP)
- Existing TerminalList untouched (non-breaking addition)
- Desktop compilation passes
</success_criteria>

<output>
After completion, create `.planning/quick/15-add-swipe-to-reveal-actions-and-drag-to-/15-SUMMARY.md`
</output>
