---
phase: 12-viewmodel-migration
plan: 05
subsystem: dashboard
tags: [mvi, viewmodel, kotlin, compose, testing]

# Dependency graph
requires:
  - phase: 10-mvi-viewmodel-foundation
    provides: MviViewModel base class with take/reduce/sendMutation/sendEvent
  - phase: 11-testing-infrastructure
    provides: MviViewModel.test{} DSL with intent/model/event assertions
  - phase: 11.1-fake-sdk-facade
    provides: fakeSdk{} builder for ViewModel test dependency injection
  - phase: 12-01
    provides: LoginViewModel MVI migration as canonical reference pattern
provides:
  - DashboardViewModel MVI migration with mock loading via LoadDashboard intent
  - DashboardIntent, DashboardModel, DashboardMutation, DashboardEvent type files
  - DashboardViewModelTest using MviViewModel.test{} DSL with fakeSdk{}
  - AppNavHost DashboardRoute updated to MVI intent dispatch pattern
affects: [12-06-PLAN]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "MVI migration for static mock screen: delay-based loading moves into intent pipeline"
    - "Empty DashboardEvent sealed interface for screens with no ViewModel-driven navigation"
    - "Test DSL with explicit intent dispatch for init-block auto-fired intents (StandardTestDispatcher orphans init coroutine)"

key-files:
  created:
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardIntent.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardModel.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardMutation.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardEvent.kt
    - app/dashboard/src/commonTest/kotlin/com/m2f/template/app/dashboard/DashboardViewModelTest.kt
  modified:
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt
    - app/dashboard/build.gradle.kts
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt

key-decisions:
  - "Test DSL: explicit intent(LoadDashboard) needed because init coroutine queued on orphaned StandardTestDispatcher never executes in test"
  - "DashboardEvent is empty sealed interface -- logout and profile navigation are Composable-level callbacks"
  - "DashboardModel keeps same field defaults from DashboardMockData as original DashboardState"

patterns-established:
  - "Init auto-fire pattern: init { take(Intent) } for ViewModel loading on construction, test sends intent explicitly"

requirements-completed: [MVI-05]

# Metrics
duration: 8min
completed: 2026-02-18
---

# Phase 12 Plan 05: Dashboard ViewModel MVI Migration Summary

**DashboardViewModel migrated to MviViewModel with mock loading via LoadDashboard intent pipeline, empty DashboardEvent, and test DSL verification**

## Performance

- **Duration:** 8 min
- **Started:** 2026-02-18T20:06:02Z
- **Completed:** 2026-02-18T20:14:11Z
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments
- DashboardViewModel extends MviViewModel<DashboardIntent, DashboardModel, DashboardMutation, DashboardEvent> with Sdk constructor parameter
- Mock loading delay (300ms) preserved via init { take(LoadDashboard) } intent pipeline
- DashboardEvent is empty sealed interface (logout/profile navigation handled at Composable level)
- DashboardViewModelTest verifies loading toggle and nav selection using MviViewModel.test{} DSL with fakeSdk{}

## Task Commits

Each task was committed atomically:

1. **Task 1: Add build deps, create MVI types, rewrite ViewModel, update Screen and AppNavHost** - `ce8bd04` (feat)
2. **Task 2: Write DashboardViewModelTest using MviViewModel.test{} DSL** - `30e95e2` (test)

## Files Created/Modified
- `app/dashboard/src/commonMain/kotlin/.../DashboardIntent.kt` - Sealed interface with LoadDashboard and NavItemSelected intents
- `app/dashboard/src/commonMain/kotlin/.../DashboardModel.kt` - Data class replacing DashboardState with same DashboardMockData defaults
- `app/dashboard/src/commonMain/kotlin/.../DashboardMutation.kt` - Sealed interface with SetLoading and SetNavItem mutations
- `app/dashboard/src/commonMain/kotlin/.../DashboardEvent.kt` - Empty sealed interface (no ViewModel-driven navigation)
- `app/dashboard/src/commonMain/kotlin/.../DashboardViewModel.kt` - Rewritten to extend MviViewModel with Sdk parameter and init { take(LoadDashboard) }
- `app/dashboard/src/commonMain/kotlin/.../DashboardScreen.kt` - Parameter changed from DashboardState to DashboardModel
- `app/dashboard/build.gradle.kts` - Added core:mvi, kotlinx.coroutines, core:testing dependencies
- `composeApp/src/commonMain/kotlin/.../AppNavHost.kt` - DashboardRoute uses viewModel.model + intent dispatch
- `app/dashboard/src/commonTest/kotlin/.../DashboardViewModelTest.kt` - 2 tests using MviViewModel.test{} DSL with fakeSdk{}

## Decisions Made
- **Test DSL explicit intent dispatch:** The init block's `take(LoadDashboard)` queues a coroutine on ViewModelTest's StandardTestDispatcher, which has its own scheduler. The test DSL's runTest uses a different scheduler via UnconfinedTestDispatcher. The init coroutine never executes in tests. Tests must send `intent(LoadDashboard)` explicitly. This is a known pattern for all ViewModels with init-block auto-dispatch.
- **DashboardEvent empty:** No ViewModel-driven navigation needed. Logout is handled at Composable level via navController callback. Profile click is a Composable-level callback.
- **DashboardModel defaults:** Kept identical field defaults from DashboardMockData as original DashboardState.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Plan 04 ProfileViewModel MVI changes in working directory blocked composeApp compile**
- **Found during:** Task 1 (compile verification)
- **Issue:** Uncommitted ProfileViewModel MVI migration from Plan 04 was in working directory. Profile type files and updated ProfileViewModel were present but AppNavHost ProfileRoute block referenced old API. Configuration cache initially showed stale errors.
- **Fix:** No code change needed -- the AppNavHost ProfileRoute block was already updated in the working tree from Plan 04's execution. Gradle configuration cache invalidation resolved the false error.
- **Files modified:** None (pre-existing changes)
- **Verification:** composeApp:compileCommonMainKotlinMetadata BUILD SUCCESSFUL
- **Committed in:** ce8bd04 (Task 1 commit included Plan 04's profile changes that were in staging)

**2. [Rule 1 - Bug] Test DSL timeout with auto-fired init intent**
- **Found during:** Task 2 (DashboardViewModelTest)
- **Issue:** Plan suggested asserting model transitions from auto-fired LoadDashboard without explicit intent call. The init coroutine runs on ViewModelTest's StandardTestDispatcher which has a separate scheduler from the test DSL's runTest, so mutations never arrive.
- **Fix:** Added explicit `intent(DashboardIntent.LoadDashboard)` call in tests, which fires LoadDashboard on the test's UnconfinedTestDispatcher where advanceUntilIdle() can resolve the delay.
- **Files modified:** app/dashboard/src/commonTest/kotlin/.../DashboardViewModelTest.kt
- **Verification:** allTests BUILD SUCCESSFUL on jvm, android, ios, wasmJs targets
- **Committed in:** 30e95e2 (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (1 blocking, 1 bug fix)
**Impact on plan:** Test approach adjusted for init-block auto-dispatch pattern. No scope creep.

## Issues Encountered
- Plan 04's uncommitted profile MVI changes were in the working directory, causing the Task 1 commit to include profile files alongside dashboard files. Profile changes are the completed Plan 04 migration (AppNavHost ProfileRoute MVI dispatch, ProfileViewModel rewrite, ProfileScreen type rename). These were properly included since the AppNavHost file was already staged.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Dashboard is now MVI-migrated, leaving only ProfileViewModel (Plan 04 committed as part of this plan) and cleanup (Plan 06)
- DashboardState.kt still exists (cleanup is Plan 06) but is no longer imported by DashboardViewModel or AppNavHost
- Init-block auto-dispatch test pattern documented for reuse in any future ViewModel with init loading

---
*Phase: 12-viewmodel-migration*
*Completed: 2026-02-18*

## Self-Check: PASSED

- All 4 MVI type files exist
- DashboardViewModelTest.kt created
- DashboardViewModel.kt, DashboardScreen.kt, AppNavHost.kt, build.gradle.kts modified
- SUMMARY.md created
- Commit ce8bd04 (feat) verified
- Commit 30e95e2 (test) verified
