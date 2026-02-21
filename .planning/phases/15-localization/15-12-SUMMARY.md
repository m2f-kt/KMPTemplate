---
phase: 15-localization
plan: 12
subsystem: localization
tags: [kotlin-wasm, js-interop, compile-fix, wasmjs]

# Dependency graph
requires:
  - phase: 15-localization
    provides: WASM locale implementation (Plan 06)
provides:
  - Clean WASM compilation of AppLocale.wasmJs.kt
affects: [composeApp wasmJs target, UAT verification]

# Tech tracking
tech-stack:
  added: []
  patterns: [js() call isolation in dedicated top-level function for Kotlin/Wasm 2.3.10]

key-files:
  modified:
    - composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt

key-decisions:
  - "Split js() call into separate navigatorLanguage() function returning JsString, with browserLanguage() doing .toString().take(2)"
  - "Added @OptIn(ExperimentalWasmJsInterop::class) annotations to suppress experimental API warnings"

patterns-established:
  - "js() isolation: Kotlin/Wasm js('...') must be sole expression in a top-level function body — no chaining allowed"

# Metrics
duration: 1min
completed: 2026-02-21
---

# Phase 15 Plan 12: Fix WASM js() Compile Error Summary

**Isolated js("navigator.language") into its own top-level function returning JsString, fixing Kotlin/Wasm 2.3.10 "sole expression" compilation error**

## Performance

- **Duration:** 1 min
- **Started:** 2026-02-21T10:11:35Z
- **Completed:** 2026-02-21T10:12:43Z
- **Tasks:** 1
- **Files modified:** 1

## Accomplishments
- Fixed WASM compilation error by splitting `browserLanguage()` into two functions: `navigatorLanguage()` (js-only) and `browserLanguage()` (string processing)
- Added `@OptIn(ExperimentalWasmJsInterop::class)` annotations for clean warning-free compilation
- Verified `compileKotlinWasmJs` passes with zero errors and zero warnings

## Task Commits

Each task was committed atomically:

1. **Task 1: Fix js() call isolation** - `5dd7b08` (fix)

## Files Created/Modified
- `composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt` - Split browserLanguage() into navigatorLanguage() (js-only, returns JsString) + browserLanguage() (toString/take chain), added @OptIn annotations

## Decisions Made
- Split into `navigatorLanguage(): JsString = js("navigator.language")` (sole expression) and `browserLanguage(): String = navigatorLanguage().toString().take(2)` (processing)
- Added `@OptIn(ExperimentalWasmJsInterop::class)` to both functions to suppress experimental API warnings (Rule 1 deviation — see below)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Added @OptIn(ExperimentalWasmJsInterop::class) annotations**
- **Found during:** Task 1 (compilation verification)
- **Issue:** After splitting the js() call, compilation succeeded but emitted 4 warnings about ExperimentalWasmJsInterop opt-in requirement on `js()` and `JsString`
- **Fix:** Added `import kotlin.js.ExperimentalWasmJsInterop` and `@OptIn(ExperimentalWasmJsInterop::class)` annotations on both functions
- **Files modified:** composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt
- **Verification:** `./gradlew composeApp:compileKotlinWasmJs` — BUILD SUCCESSFUL with zero warnings
- **Committed in:** `5dd7b08` (part of task commit)

---

**Total deviations:** 1 auto-fixed (Rule 1 — missing opt-in annotation)
**Impact on plan:** Minor addition for clean compilation. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- WASM compilation is now clean — unblocks UAT verification for WASM target
- Plan 15-13 (admin panel visibility fix) is next remaining gap closure item

---
*Phase: 15-localization*
*Completed: 2026-02-21*
