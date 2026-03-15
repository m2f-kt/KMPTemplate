---
phase: quick
plan: 2
subsystem: app:privacy
tags: [bug-fix, account-deletion, logout, mvi]
key-files:
  modified:
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
    - app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt
decisions:
  - "Call sdk.logout() before emitting NavigateToLogin so tokens are always cleared on account deletion confirmation"
  - "Reset both pendingDeletion and step state before emitting DeletionCancelled so UI reflects clean WARNING state"
metrics:
  completed: "2026-03-15T10:39:04Z"
  tasks: 2
  files: 2
---

# Quick Task 2: Fix Account Deletion Logout + Cancel State Reset

**One-liner:** Fixed AccountDeletionViewModel to call sdk.logout() and emit NavigateToLogin on confirm, and reset UI state (step=WARNING, pendingDeletion=null) before emitting DeletionCancelled on cancel.

## Summary

Two bugs were present in `AccountDeletionViewModel`:

1. **Confirm deletion did not log out:** After a successful deletion request, the ViewModel emitted `DeletionScheduled` without calling `sdk.logout()`, leaving tokens active and failing to redirect the user to login.

2. **Cancel deletion left stale UI state:** After cancelling a scheduled deletion, the ViewModel emitted `DeletionCancelled` without resetting `pendingDeletion` or `step`, so the UI would still show the scheduled deletion data on next render.

Both were fixed with minimal targeted changes.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Update tests (RED phase) | 6a973a1 | AccountDeletionViewModelTest.kt |
| 2 | Fix ViewModel (GREEN phase) | 57313a7 | AccountDeletionViewModel.kt |

## Changes Made

### AccountDeletionViewModel.kt

**handleConfirmDeletion** `ifRight` branch:
- Before: `sendMutation(SetLoading(false))` + `sendEvent(DeletionScheduled)`
- After: `sendMutation(SetLoading(false))` + `sdk.logout()` + `sendEvent(NavigateToLogin)`

**handleCancelDeletion** `ifRight` branch:
- Before: `sendMutation(SetLoading(false))` + `sendEvent(DeletionCancelled)`
- After: `sendMutation(SetLoading(false))` + `sendMutation(SetPendingDeletion(null))` + `sendMutation(SetStep(WARNING))` + `sendEvent(DeletionCancelled)`

### AccountDeletionViewModelTest.kt

- Renamed `confirmDeletion calls SDK and emits DeletionScheduled` to `confirmDeletion calls SDK logs out and navigates to login`
- Added `auth { logout { Either.Right(Unit) } }` to fakeSdk blocks in both confirm deletion tests
- Changed `event(AccountDeletionEvent.DeletionScheduled)` to `event(AccountDeletionEvent.NavigateToLogin)` in both confirm deletion tests
- Added `model(AccountDeletionModel())` assertion in cancelDeletion test before the `event(DeletionCancelled)` assertion

## Verification

- `./gradlew :app:privacy:impl:allTests` — BUILD SUCCESSFUL, all 22 tests pass
- `./gradlew :app:privacy:wire:allTests` — BUILD SUCCESSFUL, no regressions

## Deviations from Plan

None - plan executed exactly as written. One minor deviation: test name contained a comma (illegal character in Kotlin backtick identifiers on some targets), fixed to remove comma before running.

## Self-Check: PASSED

- `/Users/marc/IdeaProjects/Template/app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt` — FOUND
- `/Users/marc/IdeaProjects/Template/app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt` — FOUND
- Commit 6a973a1 — FOUND
- Commit 57313a7 — FOUND
