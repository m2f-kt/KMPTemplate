---
phase: quick-8
plan: 1
subsystem: designsystem
tags: [ui, table, alignment, spacing]
dependency_graph:
  requires: []
  provides: [vertically-centered-table-rows, column-spacing]
  affects: [admin-panel-invitations, admin-panel-members]
tech_stack:
  added: []
  patterns: [compose-alignment, compose-padding]
key_files:
  modified:
    - app/designsystem/src/commonMain/kotlin/com/m2f/template/designsystem/components/data/TerminalTable.kt
decisions: []
metrics:
  duration: 27s
  completed: "2026-03-02T12:57:01Z"
---

# Quick Task 8: Fix Invitation Table Row Vertical Alignment Summary

Vertically center table row cells and add 8dp column spacing in TerminalTable components.

## Changes

### Task 1: Add vertical centering to TerminalTableRow and column spacing to TerminalTableCell
- **Commit:** 536aea2
- Added `verticalAlignment = Alignment.CenterVertically` to the inner `Row` in `TerminalTableRow` composable content overload — matches existing `TerminalSelectableTableRow` behavior
- Added `padding(end = 8.dp)` to `TerminalTableCell` `BasicText` modifier chain — creates consistent spacing between columns
- The `List<String>` convenience overload of `TerminalTableRow` inherits the fix since it delegates to the composable overload

## Verification

- `./gradlew :app:designsystem:compileKotlinWasmJs` — BUILD SUCCESSFUL

## Deviations from Plan

None — plan executed exactly as written.
