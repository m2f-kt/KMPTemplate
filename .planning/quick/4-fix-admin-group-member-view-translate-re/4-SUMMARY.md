---
phase: quick-4
plan: 1
subsystem: admin-panel
tags: [i18n, ui-cleanup, date-formatting]
dependency_graph:
  requires: []
  provides: [spanish-remove-member-translations, formatted-dates]
  affects: [admin-panel-ui]
tech_stack:
  added: []
  patterns: [date-formatting-helper]
key_files:
  created: []
  modified:
    - app/admin/src/commonMain/composeResources/values-es/strings.xml
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
decisions: []
metrics:
  duration: "~1 min"
  completed: "2026-03-02"
---

# Quick Task 4: Fix Admin Group Member View (Translate, Remove CTA, Format Dates)

Spanish Remove Member translations, Register Member CTA removal, and DD/MM/YYYY date formatting for the admin panel member table.

## Changes Made

### Task 1: Add Spanish translations, remove Register Member CTA, format dates

**Commit:** `aa6623a`

1. **Spanish translations for Remove Member** (`values-es/strings.xml`):
   - Added 6 entries: `admin_remove_member_title`, `admin_remove_member_confirm`, `admin_remove_member_cancel`, `admin_remove_member_submit`, `admin_remove_button`, `admin_table_actions`
   - All translations use proper Spanish (e.g., "Eliminar Miembro", "Cancelar")

2. **Removed Register Member CTA** (`AdminPanelScreen.kt`):
   - Removed the `TerminalButton` using `Res.string.admin_register_member_button` from the action buttons row
   - Simplified the Row wrapper to a single Invite Member button
   - Removed unused import `admin_register_member_button`

3. **Formatted joined dates as DD/MM/YYYY** (`AdminPanelScreen.kt`):
   - Added `formatJoinedDate()` private helper function that parses ISO-8601 datetime strings and returns "DD/MM/YYYY" format
   - Updated the member table cell to use `formatJoinedDate(member.joinedAt)` instead of raw `member.joinedAt.ifBlank { "-" }`
   - Gracefully handles blank strings and parse failures (returns "-")
   - Used non-deprecated `day` and `month` properties from kotlinx.datetime

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed deprecated kotlinx.datetime property usage**
- **Found during:** Task 1
- **Issue:** Plan suggested `dayOfMonth` and `monthNumber` which are deprecated in the project's kotlinx-datetime version
- **Fix:** Used `day` and `month` properties instead
- **Files modified:** AdminPanelScreen.kt
- **Commit:** aa6623a

## Verification

- `./gradlew :app:admin:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL (0 warnings)
- Spanish strings.xml contains all 6 Remove Member entries
- AdminPanelScreen.kt has 0 references to `admin_register_member_button`
- `formatJoinedDate` helper defined and used in member table

## Self-Check: PASSED
