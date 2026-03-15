---
phase: quick-fix
plan: "01"
subsystem: app:privacy
tags: [bug-fix, mvi, account-deletion, navigation]
dependency_graph:
  requires: []
  provides: [working-account-deletion-flow]
  affects: [app:privacy:impl, app:privacy:wire]
tech_stack:
  added: []
  patterns: [MVI intent addition, callbacks pattern]
key_files:
  created: []
  modified:
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
    - app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
    - app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt
decisions:
  - Add ProceedToReAuth as a distinct intent that only advances step, keeping ReAuthenticate as the password-bearing intent
  - ConfirmStep cancel navigates back (onBack) since no deletion is scheduled yet at that step
metrics:
  duration: "~10 minutes"
  completed: "2026-03-15T10:14:02Z"
  tasks_completed: 2
  files_modified: 5
---

# Phase quick-fix Plan 01: Fix Delete Account Feature Missing Password Step Summary

**One-liner:** Fixed account deletion flow by adding `ProceedToReAuth` intent to gate the RE_AUTH password step, and changed ConfirmStep cancel to navigate back instead of calling the `cancelDeletion` API.

## What Was Built

Two bugs in the account deletion multi-step flow were fixed:

**Bug 1 (BUG-DELETE-REAUTH):** The `WarningStep` Continue button was calling `onReAuthenticate("")` — passing an empty password and jumping straight to the REASON step, bypassing the password entry screen entirely. This caused `AUTH_INVALID_CREDENTIALS` errors from the server since no password was ever captured.

**Bug 2 (BUG-DELETE-CANCEL):** The `ConfirmStep` Cancel button was calling `onCancelDeletion`, which invokes the `cancelDeletion` SDK/API endpoint. At the CONFIRM step, no deletion has been scheduled yet on the server, so this triggered a 500 error.

## Tasks Completed

| Task | Name | Commit | Key Changes |
|------|------|--------|-------------|
| 1 | Add ProceedToReAuth intent and fix ViewModel | 8ddba7b | AccountDeletionIntent.kt, AccountDeletionViewModel.kt, AccountDeletionViewModelTest.kt |
| 2 | Fix Screen callbacks and Navigation wiring | 622bfc0 | AccountDeletionScreen.kt, PrivacyNavigation.kt |

## Decisions Made

1. **ProceedToReAuth as a distinct intent:** Rather than reusing `ReAuthenticate` with an empty string, a new `ProceedToReAuth` data object was added that only advances the step to RE_AUTH. This preserves the clean semantics of `ReAuthenticate` (which stores the actual password) and makes the flow unambiguous.

2. **ConfirmStep cancel = onBack:** The cancel button at CONFIRM step navigates away (back stack pop) because no server-side deletion has been initiated yet. Only `ScheduledStep` retains `onCancelDeletion` since a pending deletion actually exists at that point.

## Flow After Fix

```
WARNING --(ProceedToReAuth)--> RE_AUTH --(ReAuthenticate(password))--> REASON
--(SetReason)--> CONFIRM --(ConfirmDeletion)--> DeletionScheduled event
                             --(Cancel/onBack)--> navigates away (no API call)

SCHEDULED --(CancelDeletion)--> DeletionCancelled event  (correct: pending deletion exists)
```

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check: PASSED

- [x] AccountDeletionIntent.kt contains `ProceedToReAuth`
- [x] AccountDeletionViewModel.kt contains `handleProceedToReAuth`
- [x] AccountDeletionScreen.kt `WarningStep` uses `onProceedToReAuth`
- [x] AccountDeletionScreen.kt `ConfirmStep` cancel uses `onBack`
- [x] PrivacyNavigation.kt wires `onProceedToReAuth` to `AccountDeletionIntent.ProceedToReAuth`
- [x] All tests pass: `./gradlew :app:privacy:impl:allTests :app:privacy:wire:allTests` BUILD SUCCESSFUL
- [x] Commits exist: 8ddba7b (task 1), 622bfc0 (task 2)
