---
phase: quick-3
plan: 01
subsystem: admin-panel
tags: [admin, mvi, remove-member, dialog, tdd]
dependency_graph:
  requires: [sdk.removeMember, FakeGroupApiBuilder.removeMember]
  provides: [AdminPanel remove member UI flow]
  affects: [AdminPanelScreen, AdminPanelViewModel, AppNavHost]
tech_stack:
  added: []
  patterns: [MVI remove-member flow following revoke-invitation pattern]
key_files:
  created: []
  modified:
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelIntent.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelModel.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelMutation.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelViewModel.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
    - app/admin/src/commonMain/composeResources/values/strings.xml
    - app/admin/src/commonTest/kotlin/com/m2f/template/app/admin/AdminPanelViewModelTest.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
decisions:
  - Followed exact revoke-invitation pattern for consistency
  - Owner role hidden via client-side UX guard (server also enforces)
metrics:
  duration: ~6 min
  completed: "2026-03-02T10:53:34Z"
---

# Quick Task 3: Remove Member from Admin Panel Summary

**Remove member MVI flow with confirmation dialog, TDD tests, and AppNavHost wiring — following the revoke-invitation pattern exactly**

## What Was Done

### Task 1: Add MVI types + ViewModel handler + tests (TDD)

- **RED:** Wrote 3 failing tests first: success removes from list + decrements memberCount, failure closes dialog + sets error, cancel hides dialog
- **GREEN:** Added ConfirmRemoveMember/CancelRemoveMember/ExecuteRemoveMember intents, showRemoveMemberDialog/removeMemberTarget/isRemovingMember model fields, ShowRemoveMemberDialog/HideRemoveMemberDialog/SetRemovingMember/RemoveMemberFromList mutations, and handleRemoveMember handler calling `sdk.removeMember(groupId, userId)`
- **Commit:** `44d9ab9`

### Task 2: Add Remove button to members table + confirmation dialog + wiring

- Added ACTIONS column (5th column) to members table with Remove button per row
- Remove button hidden for Owner role (UX guard matching server enforcement)
- Added RemoveMemberDialog composable following RevokeDialog pattern exactly
- Wired 3 new callbacks in AppNavHost: onConfirmRemoveMember, onCancelRemoveMember, onExecuteRemoveMember
- Added 7 string resources for the remove member dialog
- **Commit:** `4ff65aa`

## Verification Results

- `./gradlew :app:admin:allTests` — All 7 tests pass (4 existing + 3 new)
- `./gradlew :app:admin:compileCommonMainKotlinMetadata :composeApp:compileCommonMainKotlinMetadata` — Both modules compile successfully

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check: PASSED
