# Quick Task 16: Fix TerminalSwipeReveal — actions visible, wrong layering, swipe broken

## Result: COMPLETE

## Bugs Fixed

| Bug | Root Cause | Fix |
|-----|-----------|-----|
| Actions visible by default (fill entire screen height) | `fillMaxHeight()` on actions Row inside Column fills remaining column space | Added `height(IntrinsicSize.Min)` on parent Box — constrains height to content |
| Actions below item instead of behind | Foreground Box had transparent background | Added `background(colors.surface)` to foreground Box — opaque overlay |
| Swipe gesture not working | Two separate `pointerInput` blocks — `detectTapGestures` consumed down events before `detectHorizontalDragGestures` could process them | Replaced with single `awaitEachGesture` block — manual touch slop detection handles both drag and tap in one scope |

## Changes

| File | Change |
|------|--------|
| `TerminalSwipeReveal.kt` | Rewrote gesture handler, added IntrinsicSize.Min, added surface background |

## Commits

| Hash | Message |
|------|---------|
| `a0f1688` | fix(quick-16): fix TerminalSwipeReveal — height, layering, and gesture handling |
