---
phase: 12-viewmodel-migration
plan: 04
subsystem: profile
tags: [mvi, viewmodel, kotlin, compose, testing, turbine]

# Dependency graph
requires:
  - phase: 12-01
    provides: MVI migration reference pattern (LoginViewModel)
  - phase: 10-mvi-viewmodel-foundation
    provides: MviViewModel base class with take/reduce/sendMutation/sendEvent
  - phase: 11-testing-infrastructure
    provides: MviViewModel.test{} DSL, ViewModelTest, fakeSdk{} builder
provides:
  - ProfileViewModel MVI migration with init { take(LoadProfile) } pattern
  - SharingStarted.Eagerly pattern for ViewModels with init dispatch
  - ProfileViewModelTest demonstrating turbineScope testing for init-dispatching VMs
affects: [12-05-PLAN, 12-06-PLAN]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Init-dispatch ViewModel: SharingStarted.Eagerly + eager model/event access before take() in init block"
    - "turbineScope testing: UnconfinedTestDispatcher + turbineScope { testIn(backgroundScope) } for ViewModels with init dispatch"

key-files:
  created:
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileIntent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileModel.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileMutation.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileEvent.kt
    - app/profile/src/commonTest/kotlin/com/m2f/template/app/profile/ProfileViewModelTest.kt
  modified:
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileViewModel.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/FreeTierContent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/PaidTierContent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/PremiumTierContent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/AdminTierContent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/PowerAdminTierContent.kt
    - app/profile/build.gradle.kts
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt

key-decisions:
  - "SharingStarted.Eagerly for init-dispatching ViewModels: WhileSubscribed loses mutations emitted before first subscriber"
  - "Eager model/event access in init block: forces lazy stateIn/shareIn initialization before dispatching intents"
  - "turbineScope testing pattern: ViewModels with init dispatch cannot use MviViewModel.test{} DSL due to timing; use turbineScope + testIn(backgroundScope) directly"
  - "ProfileScreen parameter stays state: ProfileModel (matching Login/Register convention) to minimize churn in composable body"
  - "Tier content composables updated from ProfileState to ProfileModel (Rule 3: blocking compilation)"

patterns-established:
  - "Init-dispatch pattern: model; event; take(Intent) in init block with SharingStarted.Eagerly"
  - "Testing init-dispatch VMs: UnconfinedTestDispatcher + turbineScope { testIn(backgroundScope) } + advanceUntilIdle()"

requirements-completed: [MVI-05]

# Metrics
duration: 22min
completed: 2026-02-18
---

# Phase 12 Plan 04: Profile ViewModel MVI Migration Summary

**ProfileViewModel migrated to MVI with single Sdk dependency, NavigateToLogin Event replacing logoutTriggered, saveSuccess as Model state, and init-dispatch profile loading via SharingStarted.Eagerly**

## Performance

- **Duration:** 22 min
- **Started:** 2026-02-18T20:06:26Z
- **Completed:** 2026-02-18T20:28:30Z
- **Tasks:** 2
- **Files modified:** 14

## Accomplishments
- ProfileViewModel extends MviViewModel<ProfileIntent, ProfileModel, ProfileMutation, ProfileEvent> with single Sdk constructor dependency
- logoutTriggered boolean replaced by NavigateToLogin Event (one-shot side effect consumed via event.collect in AppNavHost)
- saveSuccess stays as Model field (success alert shown on screen, not navigation)
- Profile load happens via init { take(ProfileIntent.LoadProfile) } through the intent pipeline with SharingStarted.Eagerly
- 4 tests pass using turbineScope + fakeSdk{} across JVM, iOS, and WASM targets
- 5 tier content composables updated from ProfileState to ProfileModel

## Task Commits

Each task was committed atomically:

1. **Task 1: Create MVI types, rewrite ViewModel, update Screen and AppNavHost** - `ce8bd04` (feat) + `a78049c` (fix: SharingStarted.Eagerly for init dispatch)
2. **Task 2: Write ProfileViewModelTest** - `975d534` (test)

## Files Created/Modified
- `app/profile/src/commonMain/.../ProfileIntent.kt` - Sealed interface for profile user actions (LoadProfile, StartEditing, CancelEditing, EditNameChanged, EditEmailChanged, SaveProfileClicked, LogoutClicked)
- `app/profile/src/commonMain/.../ProfileModel.kt` - Data class for profile UI state (saveSuccess as model field, no logoutTriggered)
- `app/profile/src/commonMain/.../ProfileMutation.kt` - Sealed interface for state mutations (SetProfile, SetLoading, StartEdit, CancelEdit, SetEditName, SetEditEmail, SetFieldErrors, SetServerError, SetSaveSuccess)
- `app/profile/src/commonMain/.../ProfileEvent.kt` - Sealed interface with NavigateToLogin event
- `app/profile/src/commonMain/.../ProfileViewModel.kt` - Rewritten to extend MviViewModel with Sdk, init dispatch, SharingStarted.Eagerly
- `app/profile/src/commonMain/.../ProfileScreen.kt` - Parameter changed from ProfileState to ProfileModel
- `app/profile/src/commonMain/.../tier/*.kt` - All 5 tier content composables updated from ProfileState to ProfileModel
- `app/profile/build.gradle.kts` - Added core:mvi and core:testing dependencies
- `composeApp/src/commonMain/.../AppNavHost.kt` - ProfileRoute uses intent dispatch + event.collect navigation
- `app/profile/src/commonTest/.../ProfileViewModelTest.kt` - 4 tests using turbineScope + fakeSdk{}

## Decisions Made
- **SharingStarted.Eagerly for init-dispatching ViewModels:** The default `WhileSubscribed(5_000)` doesn't start collecting mutations until a subscriber is present. ViewModels that dispatch intents in `init {}` need `Eagerly` so the pipeline starts processing mutations immediately when `model` is lazily accessed. Without this, mutations emitted before the first external subscriber are lost.
- **Eager model/event access in init block:** `model; event;` before `take(ProfileIntent.LoadProfile)` forces the lazy `stateIn`/`shareIn` to initialize. Combined with `Eagerly`, this ensures the pipeline is fully wired before mutations are emitted.
- **turbineScope testing pattern:** The standard `MviViewModel.test{}` DSL cannot handle ViewModels with `init { take(...) }` because the DSL creates ViewModel before setting up its test dispatcher. For init-dispatching VMs, use `UnconfinedTestDispatcher` + `turbineScope { testIn(backgroundScope) }` directly.
- **ProfileScreen parameter naming:** Kept `state: ProfileModel` (not renamed to `model`) to avoid changing every `state.xyz` reference inside the composable body, matching the Login/Register convention from Plans 01-02.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Updated 5 tier content composables from ProfileState to ProfileModel**
- **Found during:** Task 1 (ProfileScreen type rename)
- **Issue:** FreeTierContent, PaidTierContent, PremiumTierContent, AdminTierContent, PowerAdminTierContent all reference ProfileState in their function parameters
- **Fix:** Replaced ProfileState with ProfileModel import and parameter type in all 5 files
- **Files modified:** app/profile/src/commonMain/.../tier/{Free,Paid,Premium,Admin,PowerAdmin}TierContent.kt
- **Verification:** `./gradlew :app:profile:compileCommonMainKotlinMetadata` passes
- **Committed in:** ce8bd04

**2. [Rule 1 - Bug] SharingStarted.Eagerly + eager model/event init for init-dispatch pattern**
- **Found during:** Task 2 (ProfileViewModelTest development)
- **Issue:** With default `WhileSubscribed`, mutations from `init { take(LoadProfile) }` are emitted to the pipeline before any subscriber exists, causing them to be lost. Tests could never observe the loaded state.
- **Fix:** Pass `SharingStarted.Eagerly` to MviViewModel base class; access `model` and `event` in init block before dispatching
- **Files modified:** app/profile/src/commonMain/.../ProfileViewModel.kt
- **Verification:** All 4 tests pass on JVM, iOS, and WASM targets
- **Committed in:** a78049c

**3. [Rule 1 - Bug] Corrected AppError.Server.Internal constructor usage in test**
- **Found during:** Task 2 (ProfileViewModelTest)
- **Issue:** `AppError.Server.Internal("Network error")` passes the string as `code` (first parameter), not `message`. The default message is "An unexpected error occurred".
- **Fix:** Used named parameter: `AppError.Server.Internal(message = "Network error")`
- **Files modified:** app/profile/src/commonTest/.../ProfileViewModelTest.kt
- **Verification:** Error message assertion matches
- **Committed in:** 975d534

---

**Total deviations:** 3 auto-fixed (2 bug fixes, 1 blocking)
**Impact on plan:** Tier content composables required updating for compilation. SharingStarted.Eagerly is a meaningful pattern discovery for all future init-dispatching ViewModels. No scope creep.

## Issues Encountered
- **Init-dispatch + lazy StateFlow + WhileSubscribed timing:** The combination of `init { take(...) }`, lazy `model` property, and `SharingStarted.WhileSubscribed` causes mutations to be lost because the pipeline has no subscribers when init fires. Resolved by using `SharingStarted.Eagerly` and forcing eager model/event initialization. This pattern should be applied to all future ViewModels that dispatch intents from init.
- **Test DSL incompatibility:** The `MviViewModel.test{}` DSL creates the ViewModel before setting up the test dispatcher, making it impossible to control the init dispatch timing. Resolved by using raw Turbine `turbineScope` with `UnconfinedTestDispatcher` instead of the DSL.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- ProfileViewModel MVI migration complete with tests
- SharingStarted.Eagerly pattern established for any ViewModel needing init-time intent dispatch
- ProfileState.kt still exists (cleanup is Plan 06) but is no longer imported by ProfileViewModel or AppNavHost
- DashboardViewModel (Plan 05) may need the same Eagerly pattern if it has init dispatch

---
*Phase: 12-viewmodel-migration*
*Completed: 2026-02-18*

## Self-Check: PASSED

- All 5 created files exist (4 MVI types + 1 test)
- ProfileViewModel.kt, ProfileScreen.kt, AppNavHost.kt, build.gradle.kts, 5 tier content files modified
- Commit ce8bd04 (feat: MVI types + ViewModel + Screen + NavHost) verified
- Commit a78049c (fix: SharingStarted.Eagerly) verified
- Commit 975d534 (test: ProfileViewModelTest) verified
- SUMMARY.md created
