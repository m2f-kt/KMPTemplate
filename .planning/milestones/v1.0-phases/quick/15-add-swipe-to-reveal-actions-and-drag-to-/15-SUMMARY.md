---
phase: quick-15
plan: 01
subsystem: ui
tags: [compose, gestures, swipe-to-reveal, drag-to-reorder, foundation, kmp]

# Dependency graph
requires:
  - phase: 04-05
    provides: "TerminalList, TerminalListItem, ListItemState composables"
  - phase: quick-11
    provides: "Pencil-aligned TerminalListItem with state-driven styling"
provides:
  - "TerminalSwipeReveal -- swipe-to-reveal wrapper composable"
  - "TerminalDeleteAction -- red delete convenience action"
  - "TerminalReorderableList -- LazyColumn with long-press drag-to-reorder"
  - "ReorderState + rememberReorderState -- drag state management"
affects: [dashboard, process-list, any-list-based-ui]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Animatable + detectHorizontalDragGestures for swipe gesture"
    - "detectDragGesturesAfterLongPress + graphicsLayer for drag-to-reorder"
    - "ReorderState with mutableIntStateOf/mutableFloatStateOf for performant drag tracking"

key-files:
  created:
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalSwipeReveal.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalReorderableList.kt"
  modified:
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt"

key-decisions:
  - "Animatable<Float> for swipe offset (allows imperative snapTo + spring animateTo)"
  - "Fixed revealWidth parameter (default 80dp) instead of SubcomposeLayout measurement"
  - "Separate tap detector for close-on-tap-when-revealed (pointerInput chaining)"
  - "mutableIntStateOf/mutableFloatStateOf for ReorderState (Compose primitive state performance)"
  - "graphicsLayer for drag elevation (translationY + scaleX/Y + shadowElevation)"

patterns-established:
  - "Swipe-to-reveal: Animatable + detectHorizontalDragGestures with threshold snap"
  - "Drag-to-reorder: detectDragGesturesAfterLongPress + LazyListState.layoutInfo for target detection"

# Metrics
duration: 2min
completed: 2026-02-12
---

# Quick Task 15: Swipe-to-Reveal Actions and Drag-to-Reorder Summary

**Swipe-to-reveal actions wrapper and long-press drag-to-reorder list using Foundation gestures and Animatable**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-12T22:07:48Z
- **Completed:** 2026-02-12T22:10:08Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- TerminalSwipeReveal wraps any content with left-swipe gesture revealing action buttons (threshold snap at 80dp, tap-to-close)
- TerminalDeleteAction provides a red delete convenience composable for swipe actions
- TerminalReorderableList renders typed items in LazyColumn with long-press drag-to-reorder and elevation/scale visual feedback
- ReorderState + rememberReorderState provide hoisted drag state management
- Existing TerminalList remains completely unchanged (non-breaking addition)

## Task Commits

Each task was committed atomically:

1. **Task 1: Create TerminalSwipeReveal wrapper composable with TerminalDeleteAction** - `f4919bf` (feat)
2. **Task 2: Create TerminalReorderableList with drag-to-reorder gesture** - `32de96a` (feat)

## Files Created/Modified
- `app/designsystem/.../components/data/TerminalSwipeReveal.kt` - Swipe-to-reveal wrapper + TerminalDeleteAction + preview
- `app/designsystem/.../components/data/TerminalReorderableList.kt` - Reorderable LazyColumn list + ReorderState + preview
- `app/designsystem/.../components/data/TerminalList.kt` - Preview updated to demonstrate TerminalSwipeReveal integration

## Decisions Made
- Animatable<Float> for swipe offset tracking -- allows imperative snapTo during drag and spring animateTo on release
- Fixed revealWidth Dp parameter (default 80dp) instead of SubcomposeLayout measurement -- simpler and sufficient for standard action buttons
- Separate pointerInput blocks for tap and drag detection on the foreground content -- chaining avoids gesture conflicts
- mutableIntStateOf and mutableFloatStateOf for ReorderState properties -- Compose primitive state avoids boxing overhead
- graphicsLayer for drag visual feedback (translationY, scaleX/Y 1.02, shadowElevation 8f) -- GPU-accelerated, no recomposition

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Swipe-to-reveal and drag-to-reorder gestures ready for integration into any list-based UI
- Both composables are Foundation-only and cross-platform (KMP)
- Can be composed together (e.g., swipeable items in a reorderable list)

## Self-Check: PASSED

- TerminalSwipeReveal.kt: FOUND
- TerminalReorderableList.kt: FOUND
- Commit f4919bf: FOUND (Task 1)
- Commit 32de96a: FOUND (Task 2)
- Compilation: PASSED (no errors)

---
*Quick task: quick-15*
*Completed: 2026-02-12*
