---
phase: 12-viewmodel-migration
plan: 03
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
  - phase: 12-01
    provides: LoginViewModel MVI migration as canonical reference pattern
provides:
  - ForgotPasswordViewModel MVI migration with empty ForgotPasswordEvent
  - Pattern demonstration that emailSent stays as Model state (not Event) for screens where user remains
  - ForgotPasswordViewModelTest using MviViewModel.test{} DSL
affects: [12-06-PLAN]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Empty Event sealed interface for screens without ViewModel-driven navigation"
    - "emailSent as Model field (not Event) when user stays on screen to see result"
    - "StateFlow conflation in success path: isLoading=true conflated with emailSent=true when fake SDK returns synchronously"

key-files:
  created:
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordIntent.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordMutation.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordEvent.kt
    - app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/ForgotPasswordViewModelTest.kt
  modified:
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordViewModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordScreen.kt

key-decisions:
  - "emailSent stays as Model field (not Event) because user remains on screen to see success message"
  - "ForgotPasswordEvent is empty sealed interface -- no ViewModel-driven navigation needed"
  - "StateFlow conflation in success path: isLoading=true is conflated with emailSent=true because both are Mutations and fake SDK returns synchronously"

patterns-established:
  - "Empty Event pattern: sealed interface with no members for screens without ViewModel-driven navigation"
  - "Model-state success indicator: emailSent as Model field when user stays on screen, vs Event for navigation triggers"

requirements-completed: [MVI-05]

# Metrics
duration: 5min
completed: 2026-02-18
---

# Phase 12 Plan 03: ForgotPassword ViewModel MVI Migration Summary

**ForgotPasswordViewModel migrated to MviViewModel with empty Event interface, emailSent as Model state (not Event), and 2 tests using MviViewModel.test{} DSL**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-18T19:56:35Z
- **Completed:** 2026-02-18T20:02:14Z
- **Tasks:** 2
- **Files modified:** 7

## Accomplishments
- ForgotPasswordViewModel extends MviViewModel<ForgotPasswordIntent, ForgotPasswordModel, ForgotPasswordMutation, ForgotPasswordEvent> with Sdk dependency
- emailSent kept as Model field (not Event) since user stays on screen to see success message -- demonstrates MVI flexibility
- ForgotPasswordEvent is empty sealed interface (no ViewModel-driven navigation; "back to login" is Composable-level)
- ForgotPasswordViewModelTest verifies success path (emailSent in Model) and validation error using MviViewModel.test{} DSL

## Task Commits

Each task was committed atomically:

1. **Task 1: Create MVI types, rewrite ViewModel, update Screen** - `66776ef` (feat)
2. **Task 2: Write ForgotPasswordViewModelTest using MviViewModel.test{} DSL** - `7c8a3fb` (test)

## Files Created/Modified
- `app/auth/src/commonMain/kotlin/.../ForgotPasswordIntent.kt` - Sealed interface with EmailChanged, SubmitForgotPasswordClicked
- `app/auth/src/commonMain/kotlin/.../ForgotPasswordModel.kt` - Data class with email, isLoading, emailSent, emailError, serverError
- `app/auth/src/commonMain/kotlin/.../ForgotPasswordMutation.kt` - Sealed interface with SetEmail, SetLoading, SetEmailSent, SetEmailError, SetServerError
- `app/auth/src/commonMain/kotlin/.../ForgotPasswordEvent.kt` - Empty sealed interface (no ViewModel-driven navigation)
- `app/auth/src/commonMain/kotlin/.../ForgotPasswordViewModel.kt` - Rewritten to extend MviViewModel with Sdk dependency
- `app/auth/src/commonMain/kotlin/.../ForgotPasswordScreen.kt` - Parameter changed from ForgotPasswordState to ForgotPasswordModel
- `app/auth/src/commonTest/kotlin/.../ForgotPasswordViewModelTest.kt` - 2 tests using MviViewModel.test{} DSL

## Decisions Made
- **emailSent as Model field:** Kept emailSent in ForgotPasswordModel (not as an Event) because the user stays on the screen to see the success message. This is the correct pattern for non-navigation success states.
- **Empty Event interface:** ForgotPasswordEvent has no members because "back to login" navigation is handled at the Composable level (navController.popBackStack), not by the ViewModel.
- **StateFlow conflation in success path:** Unlike LoginViewModel where success emits an Event (breaking the Mutation chain), ForgotPasswordViewModel emits SetEmailSent as a Mutation after SetLoading(true). With synchronous fake SDK, StateFlow conflates the intermediate loading state. Test expects only the final emailSent state.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Adjusted test expectation for StateFlow conflation in success path**
- **Found during:** Task 2 (ForgotPasswordViewModelTest)
- **Issue:** Plan specified `model(isLoading = true)` then `model(emailSent = true)` as two separate assertions, but StateFlow conflates these when fake SDK returns synchronously because both are Mutations (unlike Login where success emits an Event)
- **Fix:** Removed intermediate `isLoading = true` model assertion; test expects final emailSent state directly
- **Files modified:** app/auth/src/commonTest/kotlin/.../ForgotPasswordViewModelTest.kt
- **Verification:** All 8 auth tests pass on jvm, ios, wasmJs targets
- **Committed in:** 7c8a3fb (Task 2 commit)

**2. [Rule 3 - Blocking] Fixed AppNavHost RegisterRoute block to match already-migrated RegisterViewModel**
- **Found during:** Task 1 (composeApp compilation verification)
- **Issue:** RegisterViewModel was already migrated to MVI (Plan 02), but AppNavHost RegisterRoute still used old-style API (viewModel.state, viewModel::onFirstNameChange). A linter had partially applied changes but left the file in an inconsistent state.
- **Fix:** Updated RegisterRoute block to use viewModel.model, intent dispatch, and event.collect navigation pattern matching the migrated RegisterViewModel
- **Files modified:** composeApp/src/commonMain/kotlin/.../AppNavHost.kt
- **Verification:** composeApp compiles successfully
- **Committed in:** Part of AppNavHost already being in correct state after linter reconciliation

---

**Total deviations:** 2 auto-fixed (1 bug fix, 1 blocking)
**Impact on plan:** Test expectation adjusted for StateFlow conflation (known pattern from Plan 01). RegisterRoute fix was necessary to unblock compilation. No scope creep.

## Issues Encountered
None beyond the documented deviations.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- ForgotPasswordViewModel MVI migration complete
- ForgotPasswordState.kt still exists (cleanup is Plan 06)
- DashboardViewModel and ProfileViewModel migrations (Plans 04, 05) can proceed following same pattern
- Empty Event sealed interface pattern established for screens without ViewModel-driven navigation

---
*Phase: 12-viewmodel-migration*
*Completed: 2026-02-18*

## Self-Check: PASSED

- All 4 MVI type files exist (Intent, Model, Mutation, Event)
- ForgotPasswordViewModel.kt rewritten
- ForgotPasswordScreen.kt updated
- ForgotPasswordViewModelTest.kt created with 2 tests
- SUMMARY.md created
- Commit 66776ef (feat) verified
- Commit 7c8a3fb (test) verified
