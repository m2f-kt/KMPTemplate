---
phase: 15-localization
plan: 01
subsystem: localization
tags: [compose-resources, string-keys, i18n, kmp, serialization]

# Dependency graph
requires:
  - phase: 14-group-admin-ui
    provides: "All 8 screens with hardcoded English strings to extract"
provides:
  - "StringKey enum in core:models bridging error codes to localization keys"
  - "English strings.xml with ~150 string entries for all screens"
  - "resolveStringKey() @Composable bridge function mapping StringKey to Res.string.*"
affects: [15-localization, viewmodel-error-handling, screen-composables]

# Tech tracking
tech-stack:
  added: [compose.components.resources (strings.xml)]
  patterns: [StringKey enum for ViewModel error messages, resolveStringKey bridge pattern, error_ prefix convention for string resource names]

key-files:
  created:
    - core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt
    - composeApp/src/commonMain/composeResources/values/strings.xml
    - composeApp/src/commonMain/kotlin/com/m2f/template/localization/StringKeyResolver.kt
  modified: []

key-decisions:
  - "resolveStringKey function name chosen over stringResource to avoid collision with org.jetbrains.compose.resources.stringResource"
  - "StringKey enum entries use identical code strings (e.g. AUTH_INVALID_CREDENTIALS) for direct AppError.code mapping via fromCode()"
  - "Error string resources use error_ prefix + lowercased code (e.g. error_auth_invalid_credentials) as naming convention"
  - "StringKey is @Serializable for potential wire usage (server-sent error keys)"

patterns-established:
  - "StringKey enum: shared ViewModel-safe localization keys in core:models (no Compose dependency)"
  - "error_ prefix convention: all error/validation string resources prefixed with error_ followed by lowercased StringKey code"
  - "resolveStringKey bridge: exhaustive when-mapping ensures compile-time safety when adding new StringKey entries"
  - "Screen string organization: strings.xml sections separated by XML comments per screen"

# Metrics
duration: 4min
completed: 2026-02-19
---

# Phase 15 Plan 01: Localization Foundation Summary

**StringKey enum with 32 entries, English strings.xml with ~150 strings for all 8 screens, and resolveStringKey @Composable bridge with exhaustive when-mapping**

## Performance

- **Duration:** 4 min
- **Started:** 2026-02-19T17:29:42Z
- **Completed:** 2026-02-19T17:34:14Z
- **Tasks:** 3
- **Files created:** 3

## Accomplishments
- StringKey enum in core:models with 32 entries (24 AppError codes + 7 validation messages + GENERIC_ERROR) and fromCode() companion lookup
- English strings.xml with ~150 string entries organized by screen section covering all 8 app screens plus error/validation messages
- resolveStringKey() @Composable bridge function with exhaustive when expression mapping every StringKey to its Res.string.* accessor

## Task Commits

Each task was committed atomically:

1. **Task 1: Create StringKey enum in core:models** - `e2387bb` (feat)
2. **Task 2: Create English strings.xml with all app strings** - `30c285c` (feat)
3. **Task 3: Create StringKeyResolver bridge function** - `6d9088e` (feat)

**Plan metadata:** (pending final commit)

## Files Created/Modified
- `core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt` - @Serializable enum with 32 entries and fromCode() lookup
- `composeApp/src/commonMain/composeResources/values/strings.xml` - English locale strings for all screens and error messages
- `composeApp/src/commonMain/kotlin/com/m2f/template/localization/StringKeyResolver.kt` - @Composable bridge function with exhaustive StringKey→Res.string mapping

## Decisions Made
- **resolveStringKey name:** Chose `resolveStringKey` over `stringResource` to avoid collision with `org.jetbrains.compose.resources.stringResource`
- **StringKey code convention:** Enum entry names match AppError.code values exactly (e.g. `AUTH_INVALID_CREDENTIALS`) for direct `fromCode()` lookup
- **error_ prefix:** String resource names use `error_` + lowercased code (e.g. `error_auth_invalid_credentials`) for clear namespace separation from UI strings
- **@Serializable on StringKey:** Added for potential wire usage where server sends StringKey codes directly

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- StringKey enum ready for ViewModel error handling migration (Plan 02)
- strings.xml ready for screen composable migration (Plans 03-04)
- resolveStringKey bridge ready for use in screen composables
- Adding new StringKey entries will cause compile error in resolveStringKey() until mapping is added (compile-time safety)

---
*Phase: 15-localization*
*Completed: 2026-02-19*
