---
phase: 11-testing-infrastructure
plan: 02
subsystem: testing
tags: [turbine, kotest, coroutine-test, kmp, mvi, viewmodel, dsl]

# Dependency graph
requires:
  - phase: 10-mvi-viewmodel-foundation
    provides: "MviViewModel base class with model/event flows and take() intent dispatch"
provides:
  - "core:testing KMP module re-exporting Turbine, Kotest, Arrow assertions, coroutine-test"
  - "MviViewModel.test{} and .scopedTest{} Turbine-based DSL extensions"
  - "ViewModelTestContext with intent(), model(), event() statement queuing"
  - "ViewModelTest abstract base class with Dispatchers.setMain/resetMain lifecycle"
  - "@ViewModelTestDsl and @FakeSDKDsl marker annotations"
affects: [11-03-fake-sdks, 12-feature-viewmodel-tests, 13-integration-tests]

# Tech tracking
tech-stack:
  added: [turbine-1.2.1, kotlinx-coroutines-test-1.10.2]
  patterns: [test-dsl-extension-functions, statement-queuing-pattern, dsl-marker-annotations]

key-files:
  created:
    - core/testing/build.gradle.kts
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/Annotations.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/Statement.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/ViewModelTestContext.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/MviViewModelTestDsl.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/ViewModelTest.kt
  modified:
    - gradle/libs.versions.toml
    - settings.gradle.kts

key-decisions:
  - "Added kotlin-test-junit as JVM-specific api() dependency to resolve @BeforeTest/@AfterTest annotations in commonMain library"
  - "DSL uses statement queuing pattern: intent/model/event calls build a list, test runner processes sequentially"
  - "Initial StateFlow emission auto-consumed inside DSL so test authors never see it"

patterns-established:
  - "Test DSL pattern: MviViewModel.test {} queues statements then runs Turbine assertions"
  - "Test library in commonMain: consumed via testImplementation, re-exports test deps via api()"
  - "ViewModelTest base class: extend to get Dispatchers.setMain automatically"

requirements-completed: [TEST-01, TEST-05, TEST-06, MVI-06]

# Metrics
duration: 13min
completed: 2026-02-18
---

# Phase 11 Plan 02: Core Testing Module Summary

**Turbine-based MviViewModel test DSL with statement queuing, Kotest/Arrow assertion re-exports, and ViewModelTest base class for KMP**

## Performance

- **Duration:** 13 min
- **Started:** 2026-02-18T00:48:53Z
- **Completed:** 2026-02-18T01:02:26Z
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments
- Created core:testing KMP module compiling on all 5 targets (JVM, Android, iOS, wasmJs)
- Implemented MviViewModel.test{} and .scopedTest{} extension functions using Turbine for Flow assertion
- Built ViewModelTestContext DSL with intent(), model(), event() methods using sealed Statement hierarchy
- Re-exported Turbine, Kotest assertions, Arrow assertions, coroutine-test, and kotlin-test via api() dependencies
- Added ViewModelTest abstract base class handling Dispatchers.setMain/resetMain lifecycle

## Task Commits

Each task was committed atomically:

1. **Task 1: Create core:testing module with version catalog and Gradle setup** - `f3e2576` (feat)
2. **Task 2: Implement ViewModel test DSL and ViewModelTest base class** - `53324b5` (feat)

## Files Created/Modified
- `gradle/libs.versions.toml` - Added turbine and kotlinx-coroutines-test version catalog entries
- `settings.gradle.kts` - Registered core:testing module
- `core/testing/build.gradle.kts` - KMP module with api() re-exports of test libraries
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/Annotations.kt` - @ViewModelTestDsl and @FakeSDKDsl DslMarker annotations
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/Statement.kt` - Sealed hierarchy for IntentStatement, ModelStatement, EventStatement
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/ViewModelTestContext.kt` - DSL receiver collecting statement list
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/MviViewModelTestDsl.kt` - MviViewModel.test{} and .scopedTest{} extensions
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/ViewModelTest.kt` - Abstract base class with Dispatchers.setMain/resetMain

## Decisions Made
- Added `kotlin-test-junit` as JVM-specific `api()` dependency because `@BeforeTest`/`@AfterTest` annotations from `kotlin.test` resolve to JUnit `@Before`/`@After` on JVM, and the base `kotlin-test` JVM jar doesn't include them
- Statement queuing pattern processes intent dispatches and assertions sequentially, matching the Airalo ViewModelTestContext2 reference pattern
- Initial StateFlow emission is auto-consumed inside the DSL (`modelTurbine.awaitItem()`) so test authors only see state changes from their intents

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added kotlin-test-junit JVM dependency for @BeforeTest/@AfterTest resolution**
- **Found during:** Task 2 (ViewModelTest base class implementation)
- **Issue:** `@BeforeTest` and `@AfterTest` from `kotlin.test` are expect declarations that resolve to JUnit annotations on JVM, but the base `kotlin-test` JVM artifact does not include them -- only the JUnit adapter does
- **Fix:** Added `jvmMain.dependencies { api(libs.kotlin.testJunit) }` to `core/testing/build.gradle.kts`
- **Files modified:** core/testing/build.gradle.kts
- **Verification:** `./gradlew :core:testing:compileKotlinJvm` succeeds with all source files
- **Committed in:** 53324b5 (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Auto-fix was necessary for JVM compilation. No scope creep. The JUnit adapter is the standard KMP approach for JVM test annotation resolution.

## Issues Encountered
None beyond the auto-fixed deviation above.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- core:testing module is ready for consumers to add `testImplementation(projects.core.testing)` and immediately get Turbine, Kotest, Arrow assertions, and the ViewModel test DSL
- Plan 03 (fake SDK builders) can now use `@FakeSDKDsl` annotation and build on the core:testing foundation
- wasmJs compilation verified successful -- Turbine 1.2.1 + Kotlin 2.3.10 compatibility confirmed (blocker from STATE.md resolved)

## Self-Check: PASSED

All 7 created files verified on disk. Both task commits (f3e2576, 53324b5) verified in git log.

---
*Phase: 11-testing-infrastructure*
*Completed: 2026-02-18*
