---
phase: 15-localization
plan: 02
subsystem: auth, ui, localization
tags: [compose-resources, stringResource, StringKey, i18n, MVI, localization]

# Dependency graph
requires:
  - phase: 15-01
    provides: StringKey enum, strings.xml, resolveStringKey bridge in composeApp
provides:
  - Auth ViewModels emit StringKey values instead of hardcoded English strings
  - Auth screens use stringResource(Res.string.*) for all UI text
  - Auth screens use resolveStringKey() for error field display
  - Auth ViewModel tests assert on StringKey enum values
  - Auth module has its own strings.xml and local resolveStringKey bridge
  - ValidationSupport produces StringKey-coded FieldError messages
affects: [15-03, 15-05, auth-module]

# Tech tracking
tech-stack:
  added: [compose.components.resources in app/auth]
  patterns: [per-module strings.xml with local resolveStringKey bridge, StringKey? error types in MVI Models]

key-files:
  created:
    - app/auth/src/commonMain/composeResources/values/strings.xml
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/StringKeyResolver.kt
  modified:
    - core/models/src/commonMain/kotlin/com/m2f/template/models/validation/ValidationSupport.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginMutation.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginScreen.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterMutation.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterScreen.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordViewModel.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordMutation.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordScreen.kt
    - app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/LoginViewModelTest.kt
    - app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/RegisterViewModelTest.kt
    - app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/ForgotPasswordViewModelTest.kt
    - composeApp/src/commonMain/composeResources/values/strings.xml
    - composeApp/src/commonMain/kotlin/com/m2f/template/localization/StringKeyResolver.kt

key-decisions:
  - "Auth module gets its own compose.components.resources dependency and local resolveStringKey bridge (Rule 3 deviation — module can't depend on composeApp)"
  - "Auth module strings.xml duplicates error strings from composeApp — each module self-contained for resource resolution"
  - "Added VALIDATION_PASSWORDS_MISMATCH and VALIDATION_TERMS_NOT_ACCEPTED StringKey entries (missing from Plan 01)"

patterns-established:
  - "Per-module strings.xml pattern: feature modules needing stringResource() add compose.components.resources and maintain their own strings.xml"
  - "Per-module resolveStringKey bridge: each module with UI error display creates its own local resolveStringKey composable"
  - "StringKey? in MVI Models: error fields use StringKey? instead of String?, screens resolve at display time"

# Metrics
duration: 8min
completed: 2026-02-19
---

# Phase 15 Plan 02: Auth Module Localization Summary

**Migrated all auth ViewModels/screens to StringKey enum errors and stringResource() UI text with per-module resource infrastructure**

## Performance

- **Duration:** 8 min
- **Started:** 2026-02-19T21:12:00Z
- **Completed:** 2026-02-19T21:20:00Z
- **Tasks:** 2
- **Files modified:** 22

## Accomplishments
- All three auth ViewModels (Login, Register, ForgotPassword) emit StringKey values for errors instead of hardcoded English
- All three auth screens use stringResource(Res.string.*) for every UI text element
- Error display fields resolve StringKey at display time via resolveStringKey() bridge
- All auth ViewModel tests updated to assert on StringKey enum values
- Auth module has self-contained string resources (strings.xml + local StringKeyResolver)

## Task Commits

Each task was committed atomically:

1. **Task 1: Migrate ValidationSupport + auth ViewModels/Models/Mutations to StringKey** - `1aa77f7` (feat)
2. **Task 2: Migrate auth screens to stringResource and update tests** - `390d895` (feat)

## Files Created/Modified

### Created
- `app/auth/src/commonMain/composeResources/values/strings.xml` - Auth module string resources (UI text + error strings)
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/StringKeyResolver.kt` - Local resolveStringKey composable bridge

### Modified — ViewModel/Model/Mutation Layer
- `core/models/src/commonMain/kotlin/com/m2f/template/models/validation/ValidationSupport.kt` - FieldError messages now use StringKey.*.code
- `core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt` - Added VALIDATION_PASSWORDS_MISMATCH, VALIDATION_TERMS_NOT_ACCEPTED
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/Login{Model,Mutation,ViewModel}.kt` - StringKey? error types
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/Register{Model,Mutation,ViewModel}.kt` - StringKey? error types
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPassword{Model,Mutation,ViewModel}.kt` - StringKey? error types

### Modified — Screen Layer
- `app/auth/build.gradle.kts` - Added compose.components.resources dependency
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginScreen.kt` - stringResource() + resolveStringKey()
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterScreen.kt` - stringResource() + resolveStringKey()
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/ForgotPasswordScreen.kt` - stringResource() + resolveStringKey()

### Modified — Tests
- `app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/LoginViewModelTest.kt` - Assert StringKey values
- `app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/RegisterViewModelTest.kt` - Assert StringKey values
- `app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/ForgotPasswordViewModelTest.kt` - Assert StringKey values

### Modified — composeApp (supporting changes)
- `composeApp/src/commonMain/composeResources/values/strings.xml` - Added 2 new error strings
- `composeApp/src/commonMain/kotlin/com/m2f/template/localization/StringKeyResolver.kt` - Added 2 new mappings

## Decisions Made
- **Auth module gets own compose resources:** The app/auth module cannot depend on composeApp (dependency direction is reversed), so it needs its own compose.components.resources dependency and local strings.xml + resolveStringKey bridge (Rule 3 deviation)
- **Error string duplication across modules:** Auth module strings.xml contains all error strings (duplicated from composeApp). Each module is self-contained for resource resolution. Future DRY optimization possible via shared resource module.
- **Two missing StringKey entries added:** VALIDATION_PASSWORDS_MISMATCH and VALIDATION_TERMS_NOT_ACCEPTED were not in Plan 01's StringKey enum but required for RegisterViewModel validation (Rule 2 deviation)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Created per-module resource infrastructure for app/auth**
- **Found during:** Task 2 (screen migration)
- **Issue:** Auth screens need stringResource() and resolveStringKey() but app/auth cannot depend on composeApp (reverse dependency). Plan assumed resolveStringKey() from composeApp would be available.
- **Fix:** Added compose.components.resources to app/auth build.gradle.kts, created auth module strings.xml with all UI + error strings, created local resolveStringKey bridge function.
- **Files modified:** app/auth/build.gradle.kts, app/auth/src/commonMain/composeResources/values/strings.xml, app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/StringKeyResolver.kt
- **Verification:** ./gradlew :app:auth:allTests passes, ./gradlew :composeApp:compileKotlinMetadata passes
- **Committed in:** 390d895 (Task 2 commit)

**2. [Rule 2 - Missing Critical] Added VALIDATION_PASSWORDS_MISMATCH and VALIDATION_TERMS_NOT_ACCEPTED StringKey entries**
- **Found during:** Task 1 (RegisterViewModel migration)
- **Issue:** RegisterViewModel validation for password mismatch and terms acceptance had no corresponding StringKey entries
- **Fix:** Added both entries to StringKey enum, composeApp strings.xml, composeApp StringKeyResolver, and auth module strings.xml + StringKeyResolver
- **Files modified:** StringKey.kt, both strings.xml files, both StringKeyResolver files
- **Verification:** All validation paths in RegisterViewModel compile and test correctly
- **Committed in:** 1aa77f7 (Task 1 commit) + 390d895 (Task 2 commit for auth module resources)

---

**Total deviations:** 2 auto-fixed (1 blocking, 1 missing critical)
**Impact on plan:** Both deviations necessary for correct operation. No scope creep — per-module resources is the correct architectural pattern for KMP modules.

## Issues Encountered
None — all changes compiled and tested cleanly after applying deviations.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Auth module fully localized, ready for Plan 03 (feature module screens migration)
- Per-module resource pattern established and verified — can be replicated in app/dashboard, app/profile, app/admin
- resolveStringKey bridge pattern documented for future module migrations

---
*Phase: 15-localization*
*Completed: 2026-02-19*
