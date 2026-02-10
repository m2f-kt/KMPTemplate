---
phase: 01-foundation-module-structure
plan: 03
subsystem: infra
tags: [koin, arrow, context-parameters, di, validation, kmp, compose-multiplatform]

requires:
  - phase: 01-01
    provides: "kmp-library-convention and server-module-convention plugins, version catalog with Koin and Arrow entries"
  - phase: 01-02
    provides: "core:models module with AppError/FieldError types, module stubs for server and app"
provides:
  - "Koin DI wired on all 4 KMP client targets via KoinApplication in App composable"
  - "Koin DI wired on server via Ktor Koin plugin with configurationModule and serverModule"
  - "SharedModule, AppModule, AndroidModule, ServerModule DI definitions"
  - "Arrow validation helpers (validateEmail, validatePassword, validateName, validateRequired) using context parameters"
  - "zipOrAccumulate accumulated validation pattern documented and demonstrated"
  - "-Xcontext-parameters compiler flag propagated to all KMP targets via kmp-library-convention"
affects: [01-04, 02-auth-endpoints, 03-client-sdk, 04-compose-ui, 05-setup-cli, 06-ai-agents]

tech-stack:
  added: [koin-compose-4.1.1, koin-compose-viewmodel-4.1.1, koin-android-4.1.1]
  patterns: [koin-compose-koinapplication, koin-ktor-plugin-install, arrow-context-parameter-validation, zipOrAccumulate-accumulated-errors]

key-files:
  created:
    - shared/src/commonMain/kotlin/com/m2f/template/di/SharedModule.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt
    - composeApp/src/androidMain/kotlin/com/m2f/template/di/AndroidModule.kt
    - server/src/main/kotlin/com/m2f/template/di/ServerModule.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/validation/ValidationSupport.kt
  modified:
    - composeApp/src/commonMain/kotlin/com/m2f/template/App.kt
    - server/src/main/kotlin/com/m2f/template/Application.kt
    - buildSrc/src/main/kotlin/kmp-library-convention.gradle.kts

key-decisions:
  - "KoinApplication composable wraps App for all client targets (single initialization point)"
  - "Server uses install(Koin) Ktor plugin with configurationModule + serverModule"
  - "Validation helpers use context(raise: Raise<FieldError>) pattern with explicit raise.ensure() calls"

patterns-established:
  - "KoinApplication at composition root: all client targets (Android, iOS, JVM, WASM) get DI via single KoinApplication wrapper"
  - "install(Koin) in Ktor Application.module(): server DI initialized via Ktor plugin"
  - "context(raise: Raise<E>) + raise.ensure(): Arrow validation with context parameters for field-level error raising"
  - "zipOrAccumulate composition: multiple validators combined to accumulate all errors instead of fail-fast"

duration: ~17min
completed: 2026-02-10
---

# Plan 01-03: Koin DI Wiring & Arrow Validation Patterns Summary

**Koin DI initialized on all KMP targets via KoinApplication composable and Ktor plugin, with Arrow context-parameter validation helpers using zipOrAccumulate in core:models**

## Performance

- **Duration:** ~17 min
- **Started:** 2026-02-10T20:41:58Z
- **Completed:** 2026-02-10T20:59:40Z
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments
- Koin DI wired and compiling on all 4 client targets (Android, iOS, JVM, WASM) and the server
- SharedModule, AppModule, AndroidModule, and ServerModule provide the DI skeleton for all subsequent feature phases
- Arrow validation helpers (validateEmail, validatePassword, validateName, validateRequired) established in core:models using context parameters
- Context parameters (-Xcontext-parameters) confirmed working on all KMP targets (JVM, WASM, iOS) after adding flag to kmp-library-convention
- zipOrAccumulate accumulated validation pattern documented for future use in auth and form validation

## Task Commits

Each task was committed atomically:

1. **Task 1: Wire Koin DI across all KMP targets and server** - `82a94f0` (feat)
2. **Task 2: Arrow validation helpers with context parameters** - `eb09f73` (feat)

## Files Created/Modified
- `shared/src/commonMain/kotlin/com/m2f/template/di/SharedModule.kt` - Platform-agnostic Koin module definition
- `composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt` - Client app module aggregating shared + client dependencies
- `composeApp/src/androidMain/kotlin/com/m2f/template/di/AndroidModule.kt` - Android-specific Koin module for context-dependent services
- `server/src/main/kotlin/com/m2f/template/di/ServerModule.kt` - Server-specific Koin module for repositories and services
- `core/models/src/commonMain/kotlin/com/m2f/template/models/validation/ValidationSupport.kt` - Arrow validation helpers with context parameter pattern
- `composeApp/src/commonMain/kotlin/com/m2f/template/App.kt` - Wrapped with KoinApplication composable
- `server/src/main/kotlin/com/m2f/template/Application.kt` - Added install(Koin) with configurationModule and serverModule
- `buildSrc/src/main/kotlin/kmp-library-convention.gradle.kts` - Added -Xcontext-parameters flag for all KMP targets

## Decisions Made
- Used KoinApplication composable wrapper (not startKoin) as the single initialization point for all client targets -- simplest cross-platform approach
- Server uses install(Koin) Ktor plugin to register configurationModule and serverModule explicitly
- Validation helpers use `raise.ensure()` explicit call pattern (context parameter named `raise`) rather than implicit Raise resolution

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added -Xcontext-parameters to kmp-library-convention**
- **Found during:** Task 2 (Arrow validation helpers)
- **Issue:** The kmp-library-convention plugin did not have the -Xcontext-parameters compiler flag, which was only present in server-module-convention. Context parameter code would fail to compile on KMP targets.
- **Fix:** Added `kotlin { compilerOptions { freeCompilerArgs.set(listOf("-Xcontext-parameters")) } }` to kmp-library-convention.gradle.kts
- **Files modified:** buildSrc/src/main/kotlin/kmp-library-convention.gradle.kts
- **Verification:** core:models compiles on JVM, WASM, and iOS with context parameters
- **Committed in:** eb09f73 (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Essential fix -- without this, context parameter code would not compile on any KMP target. No scope creep.

## Issues Encountered
- `./gradlew assemble` fails on `composeApp:compileProductionExecutableKotlinWasmJs` (WASM production build internal compiler error) -- this is a pre-existing issue unrelated to this plan's changes. All compilation tasks for individual targets pass successfully.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Koin DI skeleton is ready for feature modules to register their dependencies (Phase 2+)
- Arrow validation pattern established for auth signup validation (Phase 2)
- Context parameters confirmed working on all targets, enabling Raise-based error handling throughout
- Plan 01-04 (logging) can proceed independently

## Self-Check: PASSED

All 5 created files verified on disk. Both task commits (82a94f0, eb09f73) verified in git log.

---
*Plan: 01-03-foundation-module-structure*
*Completed: 2026-02-10*
