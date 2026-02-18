---
phase: 11-testing-infrastructure
plan: 03
subsystem: testing
tags: [fakes, dsl, turbine, kotest, viewmodel, mvi, kmp]

# Dependency graph
requires:
  - phase: 11-01-sdk-interface-extraction
    provides: "AuthApi and UserApi interfaces for fake implementation"
  - phase: 11-02-core-testing-module
    provides: "core:testing module with Turbine, Kotest, ViewModelTest base, MviViewModel.test{} DSL, @FakeSDKDsl annotation"
provides:
  - "fakeAuthApi {} DSL builder returning configurable AuthApi test doubles"
  - "fakeUserApi {} DSL builder returning configurable UserApi test doubles"
  - "Reference LoginViewModelTest demonstrating fakes + Turbine + Kotest + ViewModelTest"
  - "MviViewModelTestDslTest proving MviViewModel.test {} DSL works end-to-end"
affects: [12-feature-viewmodel-tests, 13-integration-tests]

# Tech tracking
tech-stack:
  added: []
  patterns: [fake-builder-dsl, reference-test-pattern, advanceUntilIdle-after-viewModelScope-launch]

key-files:
  created:
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeAuthApiBuilder.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeUserApiBuilder.kt
    - app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/LoginViewModelTest.kt
    - core/testing/src/commonTest/kotlin/com/m2f/template/core/testing/MviViewModelTestDslTest.kt
  modified:
    - core/testing/build.gradle.kts
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/MviViewModelTestDsl.kt
    - app/auth/build.gradle.kts

key-decisions:
  - "MviViewModel.test{} DSL sets Dispatchers.Main to UnconfinedTestDispatcher sharing runTest scheduler internally"
  - "advanceUntilIdle() called after each intent dispatch in DSL to drain viewModelScope coroutines"
  - "DSL manages its own Dispatchers.Main setup; ViewModelTest @AfterTest handles resetMain"

patterns-established:
  - "Fake SDK builder pattern: fakeAuthApi { login { _, _ -> ... } } creates configurable test doubles"
  - "Reference test pattern: extend ViewModelTest, use fakeAuthApi DSL, test StateFlow with Turbine, assert with shouldBe"
  - "advanceUntilIdle() after viewModel action calls that use viewModelScope.launch"

requirements-completed: [TEST-04, MVI-06]

# Metrics
duration: 6min
completed: 2026-02-18
---

# Phase 11 Plan 03: Fake SDK Builders and Reference ViewModel Tests Summary

**Fake AuthApi/UserApi builder DSL with @FakeSDKDsl, reference LoginViewModel test proving Turbine+Kotest+ViewModelTest pattern, and MviViewModel.test{} DSL end-to-end validation**

## Performance

- **Duration:** 6 min
- **Started:** 2026-02-18T01:05:51Z
- **Completed:** 2026-02-18T01:12:18Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments
- FakeAuthApiBuilder with 6 configurable methods and fakeAuthApi {} top-level DSL function
- FakeUserApiBuilder with 3 configurable methods and fakeUserApi {} top-level DSL function
- 3 passing LoginViewModelTest tests demonstrating login success, validation error, and server error flows
- 3 passing MviViewModelTestDslTest tests proving MviViewModel.test {} DSL works with model, event, and mixed assertions
- Fixed MviViewModel.test {} DSL to use UnconfinedTestDispatcher sharing runTest scheduler for reliable viewModelScope coroutine drainage

## Task Commits

Each task was committed atomically:

1. **Task 1: Create fake SDK builder DSL (FakeAuthApiBuilder, FakeUserApiBuilder)** - `6525513` (feat)
2. **Task 2: Write reference LoginViewModel test using DSL and fakes** - `0bdaf9d` (feat)
3. **Task 3: Exercise MviViewModel.test {} DSL with minimal inline ViewModel** - `4058fa6` (feat)

## Files Created/Modified
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeAuthApiBuilder.kt` - FakeAuthApiBuilder with 6 configurable lambdas and fakeAuthApi {} DSL
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeUserApiBuilder.kt` - FakeUserApiBuilder with 3 configurable lambdas and fakeUserApi {} DSL
- `app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/LoginViewModelTest.kt` - Reference test with 3 test cases using fakeAuthApi + Turbine + Kotest
- `core/testing/src/commonTest/kotlin/com/m2f/template/core/testing/MviViewModelTestDslTest.kt` - 3 tests exercising MviViewModel.test {} DSL end-to-end
- `core/testing/build.gradle.kts` - Added core:sdk dependency for fake builder interface access
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/MviViewModelTestDsl.kt` - Fixed dispatcher wiring with UnconfinedTestDispatcher + advanceUntilIdle
- `app/auth/build.gradle.kts` - Added testImplementation(projects.core.testing)

## Decisions Made
- MviViewModel.test {} DSL sets Dispatchers.Main to UnconfinedTestDispatcher(testScheduler) internally so viewModelScope.launch coroutines share the same scheduler as runTest -- this ensures advanceUntilIdle() properly drains pending coroutines
- DSL calls advanceUntilIdle() after each intent dispatch to guarantee mutations/events are emitted before Turbine assertions
- ViewModelTest @AfterTest handles Dispatchers.resetMain() lifecycle; DSL does not call resetMain to avoid race conditions during backgroundScope cleanup

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Added missing expiresIn parameter to AuthResponse in test**
- **Found during:** Task 2 (LoginViewModelTest)
- **Issue:** Plan's sample code created `AuthResponse(accessToken, refreshToken)` but `AuthResponse` data class requires `expiresIn: Long`
- **Fix:** Added `expiresIn = 3600` to the test's AuthResponse constructor call
- **Files modified:** LoginViewModelTest.kt
- **Verification:** `./gradlew :app:auth:jvmTest` passes
- **Committed in:** 0bdaf9d (Task 2 commit)

**2. [Rule 1 - Bug] Added @OptIn(ExperimentalCoroutinesApi::class) for advanceUntilIdle**
- **Found during:** Task 2 (LoginViewModelTest)
- **Issue:** `advanceUntilIdle()` requires ExperimentalCoroutinesApi opt-in; compiler warned
- **Fix:** Added `@OptIn(ExperimentalCoroutinesApi::class)` annotation to LoginViewModelTest class
- **Files modified:** LoginViewModelTest.kt
- **Verification:** `./gradlew :app:auth:jvmTest` passes with no warnings
- **Committed in:** 0bdaf9d (Task 2 commit)

**3. [Rule 1 - Bug] Fixed MviViewModel.test {} DSL dispatcher scheduler mismatch**
- **Found during:** Task 3 (MviViewModelTestDslTest)
- **Issue:** DSL's `runTest` used its own scheduler while `Dispatchers.Main` (set by ViewModelTest) used a separate `StandardTestDispatcher` -- `advanceUntilIdle()` only advanced runTest's scheduler, leaving viewModelScope coroutines pending, causing Turbine timeout
- **Fix:** DSL now sets `Dispatchers.Main` to `UnconfinedTestDispatcher(testScheduler)` inside `runTest` so both share the same scheduler; advanceUntilIdle() called after each intent dispatch
- **Files modified:** MviViewModelTestDsl.kt
- **Verification:** `./gradlew :core:testing:jvmTest` passes all 3 MviViewModelTestDslTest tests
- **Committed in:** 4058fa6 (Task 3 commit)

---

**Total deviations:** 3 auto-fixed (3 bugs)
**Impact on plan:** All fixes necessary for compilation and test correctness. The DSL dispatcher fix is an improvement to Plan 02's MviViewModelTestDsl.kt that was only discoverable once real tests exercised it. No scope creep.

## Issues Encountered
None beyond the auto-fixed deviations above.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Full testing toolkit is complete: fakes, DSL, base class, assertions all verified end-to-end
- Phase 12 can copy LoginViewModelTest as reference for additional ViewModel tests
- Phase 12 MVI migration can use MviViewModel.test {} DSL pattern from MviViewModelTestDslTest
- All tests run on JVM via `./gradlew :app:auth:jvmTest` and `./gradlew :core:testing:jvmTest`

## Self-Check: PASSED

All 5 created/modified source files verified on disk. All 3 task commits (6525513, 0bdaf9d, 4058fa6) verified in git log.

---
*Phase: 11-testing-infrastructure*
*Completed: 2026-02-18*
