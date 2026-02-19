---
phase: 15-localization
plan: 03
subsystem: ui
tags: [localization, StringKey, MVI, ViewModel, KMP, compose-multiplatform]

# Dependency graph
requires:
  - phase: 15-01
    provides: "StringKey enum in core:models, resolveStringKey bridge in composeApp"
provides:
  - "Profile/AdminPanel/RegisterMember ViewModels emit StringKey instead of English strings"
  - "Feature module Models use StringKey? and Map<String, StringKey> for error fields"
  - "Feature screens convert StringKey.code for error display"
  - "All feature ViewModel tests assert on StringKey enum values"
affects: [15-05-compose-integration]

# Tech tracking
tech-stack:
  added: []
  patterns: ["StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR for server error mapping", "StringKey enum values for inline validation errors", "StringKey.code for screen-level error display in feature modules"]

key-files:
  created: []
  modified:
    - "app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileModel.kt"
    - "app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileViewModel.kt"
    - "app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt"
    - "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelModel.kt"
    - "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelViewModel.kt"
    - "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
    - "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/RegisterMemberModel.kt"
    - "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/RegisterMemberViewModel.kt"
    - "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/RegisterMemberScreen.kt"

key-decisions:
  - "Feature modules use StringKey.code for error display (not resolveStringKey) because compose.components.resources inaccessible from feature modules"
  - "Dashboard module unchanged — no error fields in DashboardModel, no hardcoded error strings in DashboardViewModel"
  - "UI text (labels, titles, buttons) left as hardcoded in feature screens — Res.string inaccessible from feature modules without architectural change"

patterns-established:
  - "ViewModel error mapping: StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR"
  - "Inline validation: direct StringKey enum values (e.g., StringKey.VALIDATION_NAME_BLANK)"
  - "Screen error display: state.serverError?.code or state.fieldErrors[key]?.code"
  - "Test assertions: model(XModel(serverError = StringKey.SERVER_INTERNAL_ERROR))"

# Metrics
duration: 11min
completed: 2026-02-19
---

# Phase 15 Plan 03: Feature Module StringKey Migration Summary

**Profile, AdminPanel, and RegisterMember ViewModels migrated from English error strings to StringKey enum values with screen-level .code display and StringKey-based test assertions**

## Performance

- **Duration:** 11 min
- **Started:** 2026-02-19T17:36:12Z
- **Completed:** 2026-02-19T17:47:00Z
- **Tasks:** 2
- **Files modified:** 15

## Accomplishments
- All three feature ViewModels (Profile, AdminPanel, RegisterMember) emit StringKey values instead of English strings for all error fields
- Feature screens convert StringKey to String via `.code` property for TerminalInput/TerminalAlert/TerminalBadge display
- All 3 feature ViewModel test suites updated to assert on StringKey enum values — 10 tests pass
- Zero hardcoded English error strings remain in feature module ViewModels

## Task Commits

Each task was committed atomically:

1. **Task 1: Migrate ViewModels/Models/Mutations to StringKey types** - `e3c675e` (feat)
2. **Task 2: Update screens and tests for StringKey error types** - `8ef4ecf` (feat)

## Files Created/Modified
- `ProfileModel.kt` - fieldErrors: Map<String, StringKey>, serverError: StringKey?
- `ProfileMutation.kt` - SetFieldErrors and SetServerError use StringKey types
- `ProfileViewModel.kt` - Inline validation uses StringKey enum values, server errors mapped via fromCode()
- `ProfileScreen.kt` - Error display uses .code property on StringKey values
- `ProfileViewModelTest.kt` - Assertions use StringKey.SERVER_INTERNAL_ERROR
- `AdminPanelModel.kt` - error: StringKey?
- `AdminPanelMutation.kt` - SetError uses StringKey type
- `AdminPanelViewModel.kt` - Server errors mapped via StringKey.fromCode(error.code)
- `AdminPanelScreen.kt` - Error badge uses error.code
- `AdminPanelViewModelTest.kt` - Assertions use StringKey.GROUP_FORBIDDEN
- `RegisterMemberModel.kt` - fieldErrors: Map<String, StringKey>, serverError: StringKey?
- `RegisterMemberMutation.kt` - SetFieldErrors and SetServerError use StringKey types
- `RegisterMemberViewModel.kt` - Validation errors mapped via StringKey.fromCode(it.message), server errors via fromCode(error.code)
- `RegisterMemberScreen.kt` - Field errors and server error display use .code property
- `RegisterMemberViewModelTest.kt` - Assertions use StringKey.VALIDATION_NAME_BLANK, GROUP_MEMBER_ALREADY_EXISTS, etc.

## Decisions Made
- **StringKey.code for screen error display**: Feature modules cannot access `resolveStringKey()` or `Res.string.*` (those live in composeApp which feature modules don't depend on). Used `StringKey.code` as interim display string. Plan 05 (Compose Integration) can wire up the full resolution when it adds the composeApp-level `resolveStringKey()` bridge.
- **Dashboard module unchanged**: DashboardModel has no error fields and DashboardViewModel has no hardcoded error strings. Dashboard screens only have UI text (labels, titles) which require Res.string access not available in the feature module.
- **UI text left hardcoded in feature screens**: Labels like "< back", "$ user_profile", "edit_profile" are terminal-themed UI constants. Extracting them to Res.string requires adding compose.components.resources to feature module dependencies or restructuring. Deferred to Plan 05.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Feature modules cannot access resolveStringKey() or Res.string.**
- **Found during:** Task 2 (Screen migration)
- **Issue:** Plan specified using `resolveStringKey()` and `stringResource(Res.string.*)` in feature screens, but feature modules (app/profile, app/admin) don't depend on composeApp where these live. `compose.components.resources` is only declared in composeApp and app/designsystem.
- **Fix:** Used `StringKey.code` property for error display instead of `resolveStringKey()`. Left UI text hardcoded (not extractable without architectural change).
- **Files modified:** ProfileScreen.kt, AdminPanelScreen.kt, RegisterMemberScreen.kt
- **Verification:** All screens compile, tests pass
- **Committed in:** 8ef4ecf (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (Rule 3 blocking)
**Impact on plan:** Screens display error codes instead of localized messages. This is expected interim behavior — Plan 05 (Compose Integration) is designed to add the full resolveStringKey bridge at the composeApp level where Res.string is accessible.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- All feature ViewModels are StringKey-native — ready for Plan 05 Compose integration
- Screen composables use `.code` display which Plan 05 can upgrade to `resolveStringKey()` at the composeApp navigation layer
- Test patterns established for StringKey assertions that Plan 05 won't need to change

---
*Phase: 15-localization*
*Completed: 2026-02-19*

## Self-Check: PASSED
- All 10 key files exist on disk
- Both task commits (e3c675e, 8ef4ecf) found in git log
