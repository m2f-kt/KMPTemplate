---
phase: 11-testing-infrastructure
plan: 01
subsystem: testing
tags: [kotlin, interfaces, koin, delegation, sdk, dependency-injection]

# Dependency graph
requires:
  - phase: 03-client-sdk-storage
    provides: AuthApi and UserApi concrete classes, SdkModule Koin bindings
provides:
  - AuthApi and UserApi as interfaces for fake substitution in tests
  - AuthApiImpl and UserApiImpl as concrete implementations
  - Sdk facade with Kotlin by-delegation combining all API interfaces
  - Koin bindings using interface type qualifiers (single<AuthApi>)
affects: [11-02, 11-03, testing-infrastructure]

# Tech tracking
tech-stack:
  added: []
  patterns: [interface-extraction, kotlin-delegation, koin-interface-bindings]

key-files:
  created:
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApiImpl.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/UserApiImpl.kt
  modified:
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/UserApi.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt

key-decisions:
  - "Interface gets clean name, Impl suffix for concrete class (AuthApi interface, AuthApiImpl class)"
  - "SdkModule Koin bindings updated in Task 1 (Rule 3 deviation) since interface extraction makes constructors unavailable"

patterns-established:
  - "Interface extraction: SDK API classes follow interface+Impl pattern for testability"
  - "Koin type-qualified bindings: single<Interface> { Impl(...) } ensures consumers inject by interface"
  - "Sdk facade: combines all API interfaces via Kotlin by-delegation"

requirements-completed: [TEST-03]

# Metrics
duration: 5min
completed: 2026-02-18
---

# Phase 11 Plan 01: SDK Interface Extraction Summary

**Extracted AuthApi and UserApi interfaces from concrete classes, created Sdk delegation facade, and updated Koin bindings with interface type qualifiers for test fake substitution**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-18T00:48:52Z
- **Completed:** 2026-02-18T00:53:47Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- AuthApi extracted to interface with 6 suspend methods; AuthApiImpl holds all implementation code
- UserApi extracted to interface with 3 suspend methods; UserApiImpl holds all implementation code
- Sdk facade delegates to AuthApi and UserApi via Kotlin `by` keyword
- Koin bindings use `single<AuthApi>` and `single<UserApi>` type qualifiers ensuring correct interface resolution
- All consumer modules (auth, profile, dashboard) compile unchanged with zero modifications

## Task Commits

Each task was committed atomically:

1. **Task 1: Extract SDK interfaces and rename implementations** - `284c9da` (feat)
2. **Task 2: Create Sdk facade and update Koin bindings** - `6bc693b` (feat)

## Files Created/Modified
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt` - Interface with 6 auth method signatures
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApiImpl.kt` - Concrete AuthApi implementation (NEW)
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/UserApi.kt` - Interface with 3 user method signatures
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/UserApiImpl.kt` - Concrete UserApi implementation (NEW)
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt` - Facade delegating to AuthApi and UserApi
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt` - Updated Koin bindings with interface types and Sdk registration

## Decisions Made
- Interface gets the clean name (AuthApi), implementation gets Impl suffix (AuthApiImpl) -- per locked decision from planning
- SdkModule Koin bindings updated alongside interface extraction in Task 1 (deviation, see below) to maintain compilability

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Updated SdkModule Koin bindings during Task 1**
- **Found during:** Task 1 (Extract SDK interfaces)
- **Issue:** After converting AuthApi and UserApi to interfaces, SdkModule.kt referenced `AuthApi(...)` and `UserApi(...)` constructors which no longer exist, causing compilation failure
- **Fix:** Updated `single { AuthApi(...) }` to `single<AuthApi> { AuthApiImpl(...) }` and `single { UserApi(...) }` to `single<UserApi> { UserApiImpl(...) }` in Task 1 instead of Task 2
- **Files modified:** `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt`
- **Verification:** `./gradlew :core:sdk:compileKotlinJvm` passes
- **Committed in:** 284c9da (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Necessary to maintain compilability between tasks. Task 2 still added the Sdk facade and Sdk Koin binding as planned. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Interfaces are ready for fake implementations in Plan 03 (fake builder DSL)
- Plan 02 (test module setup) can proceed independently
- All consumer modules remain unchanged and compile cleanly

## Self-Check: PASSED

All 6 source files verified on disk. Both task commits (284c9da, 6bc693b) verified in git log.

---
*Phase: 11-testing-infrastructure*
*Completed: 2026-02-18*
