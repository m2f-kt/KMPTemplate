---
phase: 12-viewmodel-migration
plan: 01
subsystem: auth
tags: [mvi, viewmodel, kotlin, compose, testing]

# Dependency graph
requires:
  - phase: 10-mvi-viewmodel-foundation
    provides: MviViewModel base class with take/reduce/sendMutation/sendEvent
  - phase: 11-testing-infrastructure
    provides: MviViewModel.test{} DSL with intent/model/event assertions
  - phase: 11.1-fake-sdk-facade
    provides: fakeSdk{} builder for ViewModel test dependency injection
provides:
  - LoginViewModel MVI migration as canonical reference pattern for Plans 02-05
  - LoginIntent, LoginModel, LoginMutation, LoginEvent type files
  - LoginViewModelTest using MviViewModel.test{} DSL with fakeSdk{}
  - AppNavHost LoginRoute event-based navigation via viewModel.event.collect
affects: [12-02-PLAN, 12-03-PLAN, 12-04-PLAN, 12-05-PLAN, 12-06-PLAN]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "MVI ViewModel migration: extract Intent/Model/Mutation/Event types, rewrite ViewModel, update Screen + NavHost"
    - "Event-based navigation: LaunchedEffect(Unit) { viewModel.event.collect } replaces LaunchedEffect(state.booleanFlag)"
    - "StateFlow conflation in tests: sync fake SDK causes intermediate loading state to be conflated with final error state"

key-files:
  created:
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginIntent.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginMutation.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginEvent.kt
  modified:
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginScreen.kt
    - app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/LoginViewModelTest.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
    - app/auth/build.gradle.kts

key-decisions:
  - "StateFlow conflation: sync fake SDK causes isLoading=true to be conflated with SetServerError in error path -- test expects final state only"
  - "LoginScreen parameter stays state: LoginModel (not renamed to model) to minimize churn in composable body references"

patterns-established:
  - "MVI migration pattern: 4 type files + ViewModel rewrite + Screen type rename + NavHost intent dispatch + event.collect navigation"
  - "Test DSL pattern: fakeSdk{} -> LoginViewModel(sdk) -> viewModel.test { intent/model/event }"

requirements-completed: [MVI-05]

# Metrics
duration: 5min
completed: 2026-02-18
---

# Phase 12 Plan 01: Login ViewModel MVI Migration Summary

**LoginViewModel migrated to MviViewModel with Intent/Model/Mutation/Event types, event-based navigation, and test DSL rewrite as canonical reference for Plans 02-05**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-18T19:48:15Z
- **Completed:** 2026-02-18T19:53:03Z
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments
- LoginViewModel extends MviViewModel<LoginIntent, LoginModel, LoginMutation, LoginEvent> with take()/reduce() pattern
- loginSuccess boolean replaced by NavigateToDashboard event (one-shot side effect, not UI state)
- AppNavHost LoginRoute uses LaunchedEffect(Unit) { viewModel.event.collect } for navigation instead of boolean flag observation
- LoginViewModelTest rewritten with MviViewModel.test{} DSL verifying model states and event emissions across 3 scenarios

## Task Commits

Each task was committed atomically:

1. **Task 1: Create MVI types, rewrite ViewModel, update build config** - `8157baa` (feat)
2. **Task 2: Rewrite LoginViewModelTest using MviViewModel.test{} DSL** - `2a7d2cf` (test)

## Files Created/Modified
- `app/auth/src/commonMain/kotlin/.../LoginIntent.kt` - Sealed interface for login user actions (EmailChanged, PasswordChanged, RememberMeChanged, SubmitLoginClicked)
- `app/auth/src/commonMain/kotlin/.../LoginModel.kt` - Data class for login UI state (no loginSuccess field)
- `app/auth/src/commonMain/kotlin/.../LoginMutation.kt` - Sealed interface for state mutations (SetEmail, SetPassword, SetRememberMe, SetLoading, SetValidationErrors, SetServerError)
- `app/auth/src/commonMain/kotlin/.../LoginEvent.kt` - Sealed interface with NavigateToDashboard event
- `app/auth/src/commonMain/kotlin/.../LoginViewModel.kt` - Rewritten to extend MviViewModel with take()/reduce()
- `app/auth/src/commonMain/kotlin/.../LoginScreen.kt` - Parameter changed from LoginState to LoginModel
- `app/auth/src/commonTest/kotlin/.../LoginViewModelTest.kt` - Rewritten with MviViewModel.test{} DSL
- `composeApp/src/commonMain/kotlin/.../AppNavHost.kt` - LoginRoute uses intent dispatch + event.collect navigation
- `app/auth/build.gradle.kts` - Added core:mvi dependency

## Decisions Made
- **StateFlow conflation in error path:** When the fake SDK returns synchronously, `SetLoading(true)` is immediately followed by `SetServerError(...)`, and `StateFlow` conflates the intermediate `isLoading = true` state. The test expects only the final error state. Success path is unaffected because it emits an Event (not a Mutation) after SetLoading.
- **LoginScreen parameter naming:** Kept `state: LoginModel` (not renamed to `model`) to avoid changing every `state.email`, `state.password` reference inside the composable body. The AppNavHost passes collected model as `state = state`.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Adjusted test expectation for StateFlow conflation in error path**
- **Found during:** Task 2 (LoginViewModelTest rewrite)
- **Issue:** Plan specified `model(isLoading = true)` then `model(serverError = "...")` as two separate assertions, but StateFlow conflates these when fake SDK returns synchronously
- **Fix:** Removed intermediate `isLoading = true` model assertion; test expects final error state directly
- **Files modified:** app/auth/src/commonTest/kotlin/.../LoginViewModelTest.kt
- **Verification:** All 3 tests pass on jvm, ios, wasmJs targets
- **Committed in:** 2a7d2cf (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 bug fix)
**Impact on plan:** Test expectation adjusted to match StateFlow conflation behavior with synchronous fakes. No scope creep.

## Issues Encountered
None - plan executed as written with minor test adjustment for StateFlow conflation.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- LoginViewModel MVI migration complete and serves as canonical reference for Plans 02-05
- RegisterViewModel, ForgotPasswordViewModel, DashboardViewModel, ProfileViewModel can follow identical structural pattern
- LoginState.kt still exists (cleanup is Plan 06) but is no longer imported by LoginViewModel or AppNavHost

---
*Phase: 12-viewmodel-migration*
*Completed: 2026-02-18*

## Self-Check: PASSED

- All 4 MVI type files exist
- LoginViewModel.kt, LoginScreen.kt, AppNavHost.kt, build.gradle.kts modified
- LoginViewModelTest.kt rewritten
- SUMMARY.md created
- Commit 8157baa (feat) verified
- Commit 2a7d2cf (test) verified
