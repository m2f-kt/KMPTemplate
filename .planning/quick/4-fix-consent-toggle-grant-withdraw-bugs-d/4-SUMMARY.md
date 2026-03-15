---
phase: quick-4
plan: 01
subsystem: app:privacy
tags: [bug-fix, consent, localization, mvi]
dependency_graph:
  requires: []
  provides: [working-consent-toggle, translated-export-status]
  affects: [app:privacy:impl, app:privacy:wire]
tech_stack:
  added: []
  patterns: [ToggleConsent intent with ConsentStatus payload, exportStatusLabel composable helper]
key_files:
  created: []
  modified:
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsIntent.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
    - app/privacy/impl/src/commonMain/composeResources/values/strings.xml
    - app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
    - app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
    - app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModelTest.kt
decisions:
  - Pass full ConsentStatus (not just ConsentType) in ToggleConsent intent so ViewModel can branch on granted state without extra lookup
  - Fetch latest document version via getLegalDocument before calling grantConsent to ensure version accuracy
  - Extract reloadConsents() helper to eliminate code duplication in handleToggleConsent
metrics:
  duration: ~20min
  completed: 2026-03-15T11:34:10Z
  tasks_completed: 2
  files_modified: 7
---

# Phase quick-4 Plan 01: Fix Consent Toggle Grant/Withdraw Bugs Summary

**One-liner:** Fixed inverted consent toggle logic (always withdrew) by routing through ToggleConsent intent that branches on granted state, and replaced raw ExportStatus.name enum output with translated stringResource labels.

## Tasks Completed

| # | Task | Commit | Files |
|---|------|--------|-------|
| 1 | Fix consent toggle to grant or withdraw based on current state | 9d524eb | Intent.kt, ViewModel.kt, Screen.kt, Navigation.kt, Test.kt |
| 2 | Translate export status badge and add string resources | bd8fc7f | Screen.kt, strings.xml (EN+ES) |

## What Was Built

### Task 1: Consent Toggle Fix (TDD)

**Root cause:** `WithdrawConsent(ConsentType)` intent always called `sdk.withdrawConsent()` regardless of whether the consent was already granted or not. The screen passed `consent.type` without the current `granted` state.

**Fix:**
- Replaced `WithdrawConsent(val type: ConsentType)` with `ToggleConsent(val consent: ConsentStatus)` — passes full object so ViewModel has `granted` state.
- `handleToggleConsent()` branches: if `consent.granted=true` → `withdrawConsent`; if `consent.granted=false` → fetch doc version via `getLegalDocument`, then `grantConsent`.
- Extracted `reloadConsents()` helper used by both branches.
- Updated `PrivacySettingsScreen`, `PrivacySettingsContent`, and `ConsentStatusRow` to use `onToggleConsent: (ConsentStatus) -> Unit`.
- Updated `PrivacyNavigation.kt` wire to dispatch `ToggleConsent(consent)`.

**Tests added/updated:**
- `toggle consent withdraws when already granted` (updated from old withdraw test)
- `toggle consent grants when not yet granted` (new)
- `toggle consent grant failure shows error in model` (new)

### Task 2: Export Status Badge Translation

**Root cause:** `TerminalBadge(text = state.exportStatus.status.name)` output raw enum names like "PENDING".

**Fix:**
- Added `exportStatusLabel(status: ExportStatus): String` `@Composable` helper that maps each value to `stringResource(Res.string.privacy_export_status_xxx)`.
- Added 5 EN strings: Pending, Processing, Completed, Failed, Expired.
- Added 5 ES strings: Pendiente, Procesando, Completado, Fallido, Expirado.

## Deviations from Plan

None — plan executed exactly as written.

## Verification

- `./gradlew :app:privacy:impl:allTests` — PASSED (all platforms)
- `./gradlew :app:privacy:impl:compileCommonMainKotlinMetadata` — PASSED
- No `WithdrawConsent` references in privacy module
- No `status.name` used for badge text in PrivacySettingsScreen

## Self-Check: PASSED

- `9d524eb` — feat(quick-4): fix consent toggle to grant or withdraw based on current state
- `bd8fc7f` — feat(quick-4): translate export status badge and add string resources
- All modified files exist and compile
- All tests pass
