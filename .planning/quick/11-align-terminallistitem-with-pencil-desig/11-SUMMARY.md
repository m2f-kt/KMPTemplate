---
phase: quick-11
plan: 01
subsystem: ui
tags: [compose, list, pencil, design-system, drawBehind]

# Dependency graph
requires:
  - phase: 04-05
    provides: "TerminalList and TerminalListItem with ListItemState enum"
provides:
  - "Pencil-aligned TerminalList header with SpaceBetween layout and [N items] format"
  - "Pencil-aligned TerminalListItem with per-state subtitle/icon/action colors"
  - "Selected state 2dp left accent border via drawBehind"
  - "Color-passing leading/trailing content lambdas"
affects: [design-system, list-components]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "drawBehind for selected state left accent border (consistent with TerminalAlert pattern)"
    - "Color-passing content lambdas for state-aware icon/action rendering"

key-files:
  created: []
  modified:
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt"

key-decisions:
  - "Leading/trailing content lambdas changed from () -> Unit to (Color) -> Unit for state-aware icon coloring"
  - "showBottomBorder removed entirely from TerminalListItem (Pencil has no individual item borders)"
  - "drawBehind pattern from TerminalAlert reused for selected state left accent border"

patterns-established:
  - "State-aware color lambdas: content slots receive computed colors based on component state"

# Metrics
duration: 1min
completed: 2026-02-12
---

# Quick Task 11: TerminalListItem Pencil Alignment Summary

**Pencil-aligned TerminalList/TerminalListItem with per-state colors, drawBehind selected border, and color-passing content lambdas**

## Performance

- **Duration:** 1 min
- **Started:** 2026-02-12T18:25:48Z
- **Completed:** 2026-02-12T18:27:05Z
- **Tasks:** 1
- **Files modified:** 1

## Accomplishments
- TerminalList header aligned: 12dp/16dp padding, FontWeight.Medium, "[N items]" format, SpaceBetween layout
- TerminalListItem aligned: 12dp/16dp padding, 12dp gaps, 2dp title-subtitle gap, per-state colors for subtitle/icon/action
- Selected state renders 2dp left accent border via drawBehind (consistent with TerminalAlert pattern)
- Leading/trailing content lambdas now receive state-appropriate Color parameter
- Removed showBottomBorder parameter entirely
- Preview updated to process_list content with terminal-like item names

## Task Commits

Each task was committed atomically:

1. **Task 1: Align TerminalList header and TerminalListItem with Pencil design** - `6ce7515` (feat)

## Files Created/Modified
- `app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt` - Pencil-aligned TerminalList and TerminalListItem with all design token changes

## Decisions Made
- Leading/trailing content lambdas changed from `(@Composable () -> Unit)?` to `(@Composable (Color) -> Unit)?` so callers receive state-appropriate icon/action colors
- showBottomBorder removed entirely (Pencil design has no individual item borders)
- drawBehind pattern reused from TerminalAlert for selected state left accent border
- Literal dp values (16dp/12dp/2dp) used since Pencil tokens don't map to existing spacing tokens

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None.

## User Setup Required
None - no external service configuration required.

## Self-Check: PASSED

- [x] TerminalList.kt exists and compiles
- [x] Commit 6ce7515 exists in git history
- [x] showBottomBorder has 0 occurrences in TerminalList.kt
- [x] drawBehind used for selected state border
- [x] Per-state subtitle/icon/action colors implemented

---
*Quick task: 11-align-terminallistitem-with-pencil-desig*
*Completed: 2026-02-12*
