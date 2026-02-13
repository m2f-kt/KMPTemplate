---
phase: 05-auth-screens-dashboard-setup-cli
plan: 02
subsystem: auth
tags: [viewmodel, arrow, koin, stateflow, validation, zipOrAccumulate]

# Dependency graph
requires:
  - phase: 01-foundation
    provides: "Arrow Raise patterns, Koin DI setup, ValidationSupport helpers"
  - phase: 03-client-sdk
    provides: "AuthApi with Either-based SDK functions"
provides:
  - "LoginViewModel with email/password validation and AuthApi.login integration"
  - "RegisterViewModel with Arrow zipOrAccumulate accumulated field validation"
  - "ForgotPasswordViewModel with email validation and forgotPassword flow"
  - "Koin DI registration for all 3 auth ViewModels via viewModelOf()"
affects: [05-03-auth-screens, 05-04-dashboard]

# Tech tracking
tech-stack:
  added: [arrow-core (app:auth), lifecycle-viewmodel-compose (app:auth), koin-compose-viewmodel (app:auth)]
  patterns: [ViewModel + StateFlow + MutableStateFlow.update, Arrow zipOrAccumulate for accumulated field validation, viewModelOf Koin registration]

key-files:
  created:
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginState.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterState.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordState.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordViewModel.kt"
  modified:
    - "app/auth/build.gradle.kts"
    - "composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt"
    - "composeApp/build.gradle.kts"
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt"

key-decisions:
  - "withError field name remapping for validateName in zipOrAccumulate (firstName/lastName use same validator)"
  - "Local validation on submit (not on each keystroke), clear individual field errors on change"
  - "RegisterViewModel validates all 6 fields with zipOrAccumulate (accumulated, not fail-fast)"

patterns-established:
  - "ViewModel StateFlow pattern: private MutableStateFlow + public asStateFlow() + update{} for state changes"
  - "Validation-then-API pattern: local validation first, API call in viewModelScope.launch, Either.fold for result"
  - "Field error clearing on change: each onXxxChange clears its own error from state"

# Metrics
duration: 4min
completed: 2026-02-13
---

# Phase 5 Plan 2: Auth ViewModels Summary

**Login/Register/ForgotPassword ViewModels with Arrow zipOrAccumulate validation, Either-based API calls, and Koin DI wiring**

## Performance

- **Duration:** 4 min
- **Started:** 2026-02-13T15:35:21Z
- **Completed:** 2026-02-13T15:39:51Z
- **Tasks:** 2
- **Files modified:** 10

## Accomplishments
- Three auth ViewModels (Login, Register, ForgotPassword) with full state management via StateFlow
- RegisterViewModel uses Arrow zipOrAccumulate for accumulated field validation across 6 fields (firstName, lastName, email, password, confirmPassword, termsAccepted)
- All ViewModels registered in Koin via viewModelOf() for injection with koinViewModel() at navigation destinations
- Added forgotPassword SDK method to AuthApi as prerequisite for ForgotPasswordViewModel

## Task Commits

Each task was committed atomically:

1. **Task 1: Create auth state data classes and ViewModels** - `f6d4c0f` (feat)
2. **Task 2: Wire auth ViewModels into Koin DI** - `5ac5cfe` (feat)

## Files Created/Modified
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginState.kt` - Login form state (email, password, rememberMe, loading, errors)
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt` - Login logic with local validation and AuthApi.login
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterState.kt` - Registration form state (6 fields, fieldErrors map, serverError)
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt` - Registration with Arrow zipOrAccumulate validation
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordState.kt` - Forgot password state (email, emailSent, errors)
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordViewModel.kt` - Forgot password flow with email validation
- `app/auth/build.gradle.kts` - Added arrow-core, lifecycle-viewmodel, koin-compose-viewmodel deps
- `composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt` - Koin viewModelOf() registrations
- `composeApp/build.gradle.kts` - Added app:auth module dependency
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt` - Added forgotPassword SDK method

## Decisions Made
- Used `withError` to remap field names from validateName's hardcoded "name" to "firstName"/"lastName" in zipOrAccumulate slots
- Local validation on submit (not on each keystroke) with individual field error clearing on change
- RegisterViewModel validates all 6 fields with zipOrAccumulate (accumulated errors, not fail-fast) per plan requirement

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added forgotPassword method to AuthApi**
- **Found during:** Task 1 (ForgotPasswordViewModel creation)
- **Issue:** AuthApi did not have forgotPassword method (planned for 05-01 which hadn't run yet), blocking ForgotPasswordViewModel compilation
- **Fix:** Added `forgotPassword(request: ForgotPasswordRequest): Either<AppError, Unit>` to AuthApi with POST to /api/auth/forgot-password
- **Files modified:** core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt
- **Verification:** :app:auth:compileKotlinJvm succeeds
- **Committed in:** f6d4c0f (Task 1 commit)

**2. [Rule 3 - Blocking] Added app:auth dependency to composeApp**
- **Found during:** Task 2 (Koin DI wiring)
- **Issue:** composeApp/build.gradle.kts did not have dependency on projects.app.auth, causing unresolved reference for ViewModel imports in AppModule
- **Fix:** Added `implementation(projects.app.auth)` to composeApp commonMain dependencies
- **Files modified:** composeApp/build.gradle.kts
- **Verification:** :composeApp:compileKotlinJvm succeeds
- **Committed in:** 5ac5cfe (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Both auto-fixes necessary for compilation. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Auth ViewModels are ready for screen composition in 05-03 (auth screens)
- All ViewModels injectable via koinViewModel() at navigation destinations
- State classes provide all fields needed for form rendering

## Self-Check: PASSED

All 6 created files verified on disk. Both task commits (f6d4c0f, 5ac5cfe) verified in git log.

---
*Phase: 05-auth-screens-dashboard-setup-cli*
*Completed: 2026-02-13*
