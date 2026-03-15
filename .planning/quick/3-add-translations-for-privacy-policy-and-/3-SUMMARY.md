---
phase: quick-3
plan: 01
subsystem: privacy,profile,i18n
tags: [i18n, string-resources, compose-resources, privacy, spanish]
dependency_graph:
  requires: []
  provides: [privacy-module-string-resources, profile-es-complete-translations]
  affects: [app:privacy:impl, app:profile:impl]
tech_stack:
  added: []
  patterns: [Compose Resources stringResource(), @Composable consentTypeLabel()]
key_files:
  created:
    - app/privacy/impl/src/commonMain/composeResources/values/strings.xml
    - app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
  modified:
    - app/profile/impl/src/commonMain/composeResources/values-es/strings.xml
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateScreen.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/LegalDocumentScreen.kt
decisions:
  - consentTypeLabel() converted from plain fun to @Composable fun to allow stringResource() usage; all call sites were already in @Composable contexts so no cascading changes needed
metrics:
  duration: 4 minutes
  completed: 2026-03-15T11:00:03Z
  tasks_completed: 2
  files_modified: 7
---

# Quick Task 3: Add translations for privacy module and profile ES gaps — Summary

**One-liner:** 59-entry EN+ES string resource files for all 4 privacy screens, with consentTypeLabel() made @Composable, plus 7 missing profile Spanish entries added.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Create privacy module string resources (EN + ES) and add missing profile ES translations | ad8760f | values/strings.xml (created), values-es/strings.xml (created), profile/values-es/strings.xml (modified) |
| 2 | Refactor privacy screens to use stringResource() instead of hardcoded strings | b2f647d | PrivacySettingsScreen.kt, AccountDeletionScreen.kt, ConsentGateScreen.kt, LegalDocumentScreen.kt |

## What Was Built

### Task 1: String Resource XML Files

Created `app/privacy/impl/src/commonMain/composeResources/values/strings.xml` with 59 English string entries covering all 4 privacy screens:
- `privacy_settings_*` (17 entries) — PrivacySettingsScreen header, loading, error, sections, export, delete
- `privacy_deletion_*` (20 entries) — AccountDeletionScreen all 5 steps: warning, reauth, reason, confirm, scheduled
- `privacy_gate_*` (7 entries) — ConsentGateScreen title, subtitle, loading, error, buttons
- `privacy_consent_*` (6 entries) — Shared consent type labels + required badge + view + granted prefix
- `privacy_document_*` (4 entries) — LegalDocumentScreen back, loading, error, published prefix

Created matching `values-es/strings.xml` with 59 Spanish translations using proper UTF-8 accented characters (á, é, ó, ú, ñ, ¿, ¡).

Added 6 missing avatar/crop entries and `sidebar_nav_privacy` to `app/profile/impl/src/commonMain/composeResources/values-es/strings.xml`.

### Task 2: Screen Refactoring

All 4 privacy screens now use `stringResource(Res.string.xxx)` with no remaining hardcoded English UI strings. Format strings (`%1$s`) used for dynamic content (grantedAt date, publishedAt date, scheduledAt date).

`consentTypeLabel()` in ConsentGateScreen.kt promoted from `internal fun` to `@Composable internal fun` — all three call sites (ConsentRow, ConsentStatusRow, LegalDocumentContent) were already inside `@Composable` functions so no further changes were required.

## Verification

- XML validity: all 3 modified/created XML files parsed successfully with no errors
- Key parity: EN and ES privacy module files both have exactly 59 keys, all matching
- Compile: `./gradlew :app:privacy:impl:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL
- Hardcoded string grep: zero matches for quoted English UI strings in privacy screen Kotlin files

## Deviations from Plan

None — plan executed exactly as written.
