---
phase: 16-tech-debt-cleanup
plan: 01
subsystem: ui
tags: [wasm, localization, localStorage, compose-resources]

requires:
  - phase: 15-localization
    provides: "locale switching infrastructure with multiplatform-settings StorageSettings"
provides:
  - "WASM locale persistence across page reloads via pre-boot localStorage read"
  - "getLocalStorageLocale() JS interop fallback in getAppLocale() chain"
affects: [localization, wasm]

tech-stack:
  added: []
  patterns:
    - "Pre-WASM localStorage read pattern for restoring state before Compose boots"

key-files:
  created: []
  modified:
    - "composeApp/src/wasmJsMain/resources/index.html"
    - "composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt"

key-decisions:
  - "Read localStorage key com.russhwolf.settings.pref_language before WASM loads to close timing gap"
  - "getLocalStorageLocale() as separate top-level function (Kotlin/WASM js() constraint)"

patterns-established:
  - "Pre-WASM state restoration: read localStorage in index.html script before composeApp.js loads"

requirements-completed: [DEBT-04]

duration: 3min
completed: 2026-02-21
---

# Plan 16-01: WASM Locale Persistence Summary

**Pre-WASM localStorage read restores user's locale selection before Compose Resources initializes, closing the timing gap that reset language on page refresh**

## Performance

- **Duration:** 3 min
- **Started:** 2026-02-21
- **Completed:** 2026-02-21
- **Tasks:** 1
- **Files modified:** 2

## Accomplishments
- Added pre-boot localStorage read in index.html that sets window.__customLocale from persisted preference
- Added getLocalStorageLocale() JS interop function in AppLocale.wasmJs.kt
- Updated getAppLocale() fallback chain: overrideLocale -> localStorage -> browserLanguage()

## Task Commits

Each task was committed atomically:

1. **Task 1: Add pre-WASM localStorage locale read and getAppLocale() fallback** - `2435255` (fix)

## Files Created/Modified
- `composeApp/src/wasmJsMain/resources/index.html` - Pre-WASM localStorage read setting window.__customLocale before Compose Resources initializes
- `composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt` - getLocalStorageLocale() fallback and updated getAppLocale() chain

## Decisions Made
- Read the exact key `com.russhwolf.settings.pref_language` that multiplatform-settings StorageSettings writes
- getLocalStorageLocale() implemented as separate top-level function per Kotlin/WASM js() constraint (established pattern from Phase 15)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- WASM locale persistence is complete; no blockers for subsequent phases
- Pattern established for pre-WASM state restoration if future features need it

---
*Phase: 16-tech-debt-cleanup*
*Completed: 2026-02-21*
