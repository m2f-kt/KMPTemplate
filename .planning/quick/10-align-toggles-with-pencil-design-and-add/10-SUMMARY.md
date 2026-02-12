---
phase: quick-10
plan: 01
subsystem: ui
tags: [compose, checkbox, switch, table, tri-state, selection, pencil-design]

# Dependency graph
requires:
  - phase: quick-09
    provides: "TerminalTable with table color tokens and TerminalTableCell"
  - phase: 04-04
    provides: "TerminalCheckbox, TerminalSwitch with Canvas icons"
provides:
  - "Tri-state TerminalCheckbox (On/Off/Indeterminate) with Pencil colors"
  - "TerminalSwitch aligned with Pencil (10dp radius, btnPrimaryBg/Text tokens)"
  - "TerminalSelectableTable with checkbox column and row selection"
  - "checkboxBg and tableRowSelectedBg color tokens"
affects: [designsystem, dashboard, auth]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "triStateToggleable for tri-state checkbox accessibility"
    - "Inline table checkbox (16dp) separate from standalone (18dp)"
    - "Aggregate header checkbox state derived from row selection"

key-files:
  created: []
  modified:
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/theme/TerminalColors.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalCheckbox.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/selection/TerminalSwitch.kt"
    - "app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt"

key-decisions:
  - "10.dp literal for switch track radius and label gap (Pencil specifies 10, between radius.md=6 and radius.lg=12)"
  - "btnPrimaryBg for switch on-track (consistent with checkbox checked fill, both #525252 light)"
  - "16dp inline checkbox for table vs 18dp standalone (proportional scaling for table density)"
  - "32dp touch target for table checkboxes (centering 16dp checkbox in 32dp cell)"

patterns-established:
  - "ToggleableState tri-state pattern: On/Off/Indeterminate with triStateToggleable"
  - "Boolean checkbox delegates to tri-state overload for backward compatibility"
  - "Selectable table pattern: header checkbox + per-row checkboxes + selectedRows Set<Int>"

# Metrics
duration: 3min
completed: 2026-02-12
---

# Quick Task 10: Align Toggles with Pencil Design and Add Selection Table Summary

**Tri-state checkbox with On/Off/Indeterminate, Pencil-aligned switch tokens, and TerminalSelectableTable with checkbox column and row highlighting**

## Performance

- **Duration:** 3 min
- **Started:** 2026-02-12T18:11:47Z
- **Completed:** 2026-02-12T18:15:16Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- TerminalCheckbox now supports tri-state (ToggleableState.On, Off, Indeterminate) with correct Pencil colors (checkboxBg for unchecked, btnPrimaryBg for checked/indeterminate)
- TerminalSwitch aligned: 10dp track cornerRadius, accentMuted off track, btnPrimaryBg on track, btnPrimaryText on knob, 10dp label gap
- TerminalSelectableTable with header checkbox (aggregate state) and per-row checkboxes with tableRowSelectedBg selection highlight
- Two new color tokens: checkboxBg (#F5F5F5/#262626) and tableRowSelectedBg (#EDF2EE/#1A231C)

## Task Commits

Each task was committed atomically:

1. **Task 1: Add color tokens and align TerminalCheckbox (tri-state) and TerminalSwitch with Pencil** - `76db398` (feat)
2. **Task 2: Add checkbox selection column to TerminalTable** - `811eb08` (feat)

## Files Created/Modified
- `app/designsystem/.../theme/TerminalColors.kt` - Added checkboxBg and tableRowSelectedBg tokens to data class, defaults, light, and dark palettes
- `app/designsystem/.../selection/TerminalCheckbox.kt` - New tri-state overload with ToggleableState; Boolean overload delegates to it for backward compatibility
- `app/designsystem/.../selection/TerminalSwitch.kt` - 10dp track radius, accentMuted off track, btnPrimaryBg on track, btnPrimaryText on knob, 10dp label gap
- `app/designsystem/.../data/TerminalTable.kt` - Added TerminalTableCheckbox (16dp inline), TerminalSelectableTable, TerminalSelectableTableRow; existing APIs unchanged

## Decisions Made
- Used literal 10.dp for switch track radius (Pencil specifies 10dp, falls between radius.md=6 and radius.lg=12; follows established literal-dp pattern from quick-07/08/09)
- Used btnPrimaryBg for switch on-track instead of accent (in light mode both are #525252; in dark mode btnPrimaryBg=#D4D4D4 is consistent with checkbox checked fill)
- Table checkbox is 16dp in a 32dp touch target cell (proportionally smaller than standalone 18dp checkbox)
- Header row padding adjusted (2dp vertical) to accommodate 32dp checkbox cell without excess height

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- All toggle/selection components aligned with Pencil design spec
- TerminalSelectableTable ready for dashboard row-selection use cases
- Existing TerminalTable/TerminalCheckbox/TerminalSwitch APIs fully backward compatible

## Self-Check: PASSED

All 4 modified files exist on disk. Both commit hashes (76db398, 811eb08) verified in git log. All must-have key_links verified: colors.checkboxBg in TerminalCheckbox (1 match), TerminalTableCheckbox in TerminalTable (3 matches), colors.tableRowSelectedBg in TerminalTable (3 matches).

---
*Quick Task: 10*
*Completed: 2026-02-12*
