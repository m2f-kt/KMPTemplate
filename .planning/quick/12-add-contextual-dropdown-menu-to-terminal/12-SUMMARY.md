---
phase: quick-12
plan: 01
subsystem: ui
tags: [compose, dropdown-menu, popup, foundation, design-system]

# Dependency graph
requires:
  - phase: quick-11
    provides: TerminalListItem with per-state colors and color-passing content lambdas
provides:
  - TerminalDropdownMenu composable for floating popup menus
  - TerminalDropdownMenuItem composable for hoverable menu items
  - TerminalListItem menuItems parameter for contextual action menus
affects: [dashboard, feature-screens]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Popup-based dropdown menu with surface/border/shadow styling"
    - "menuItems lambda parameter for contextual actions on list items"
    - "Ellipsis trigger character for menu activation"

key-files:
  created:
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalDropdownMenu.kt
  modified:
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalList.kt

key-decisions:
  - "Popup onDismissRequest used for dismiss-on-click-outside (Foundation Popup supports it)"
  - "U+22EF midline horizontal ellipsis for three-dots trigger character"
  - "Preview renders menu content directly in Box (Popup not visible in static previews)"

patterns-established:
  - "TerminalDropdownMenu: Popup + Column with shadow/clip/background/border pattern (mirrors TerminalTooltip)"
  - "TerminalDropdownMenuItem: hoverable/clickable Row with InteractionSource (mirrors TerminalButton hover pattern)"

# Metrics
duration: 2min
completed: 2026-02-12
---

# Quick 12: Contextual Dropdown Menu Summary

**TerminalDropdownMenu composable with Popup overlay, hoverable menu items, and ellipsis-triggered integration into TerminalListItem**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-12T18:37:49Z
- **Completed:** 2026-02-12T18:40:05Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- New TerminalDropdownMenu composable renders floating popup with surface bg, border, shadow, and rounded corners
- New TerminalDropdownMenuItem composable renders hoverable clickable rows with optional leading icon and inset hover bg
- TerminalListItem gains menuItems parameter that displays ellipsis trigger and anchors dropdown menu
- menuItems takes precedence over trailingContent; disabled state shows dots but prevents opening

## Task Commits

Each task was committed atomically:

1. **Task 1: Create TerminalDropdownMenu and TerminalDropdownMenuItem composables** - `444a3cd` (feat)
2. **Task 2: Integrate menuItems into TerminalListItem and update preview** - `afdff59` (feat)

## Files Created/Modified
- `app/designsystem/.../components/data/TerminalDropdownMenu.kt` - New file with TerminalDropdownMenu (Popup-based floating container) and TerminalDropdownMenuItem (hoverable row) composables, plus preview
- `app/designsystem/.../components/data/TerminalList.kt` - Added menuItems parameter, menuExpanded state, ellipsis trigger with TerminalDropdownMenu integration, updated preview with contextual menu examples

## Decisions Made
- Used Popup's onDismissRequest parameter for dismiss-on-click-outside behavior (simpler than manual pointer input overlay)
- Used U+22EF (midline horizontal ellipsis) for the trigger character as it renders as three horizontally centered dots
- Preview for TerminalDropdownMenu renders menu content directly in a styled Box rather than inside Popup (Popup content is not visible in static compose previews)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- `compileCommonMainKotlinMetadata` Gradle target fails due to pre-existing `ui-tooling` dependency resolution issue across iOS/WasmJs platforms (not related to this change). Verified compilation via `compileKotlinJvm` which passes cleanly.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- TerminalDropdownMenu available for use in any screen requiring contextual action menus
- TerminalListItem menuItems parameter ready for process list, session list, and other data screens

## Self-Check: PASSED

All artifacts verified:
- TerminalDropdownMenu.kt: FOUND
- TerminalList.kt: FOUND
- 12-SUMMARY.md: FOUND
- Commit 444a3cd: FOUND
- Commit afdff59: FOUND

---
*Phase: quick-12*
*Completed: 2026-02-12*
