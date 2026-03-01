---
phase: 21-group-invitations-profiles
plan: 06
subsystem: api, ui
tags: [ktor, invitation, resend, compose, mvi, admin-panel]

# Dependency graph
requires:
  - phase: 21-group-invitations-profiles (plans 01-03)
    provides: InvitationService, InvitationRoutes, InvitationApi, AdminPanelScreen with InvitationsSection
provides:
  - Resend invitation server endpoint (POST /api/groups/{groupId}/invitations/{invitationId}/resend)
  - SDK resendInvitation method
  - Groups.ResendInvitation route class
  - FakeInvitationApiBuilder resendInvitation stub
  - Admin panel Resend button for expired/revoked invitations
affects: [admin-panel, invitation-flow, e2e-tests]

# Tech tracking
tech-stack:
  added: []
  patterns: [resend-as-revoke-plus-create pattern for invitation lifecycle]

key-files:
  created: []
  modified:
    - server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt
    - server/groups/src/main/kotlin/com/m2f/server/groups/routes/InvitationRoutes.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/InvitationApi.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/InvitationApiImpl.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeInvitationApiBuilder.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelIntent.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelMutation.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelModel.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelViewModel.kt
    - app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt
    - app/admin/src/commonMain/composeResources/values/strings.xml
    - app/admin/src/commonMain/composeResources/values-es/strings.xml
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt

key-decisions:
  - "Resend = revoke old + create new invitation (not just extend expiry) for clean audit trail"
  - "Resend only allowed for expired/revoked invitations (active ones should be revoked first)"

patterns-established:
  - "Resend-as-revoke-plus-create: resending creates fresh invitation with new token/expiry, old one marked revoked"

requirements-completed: []

# Metrics
duration: 6min
completed: 2026-03-01
---

# Phase 21 Plan 06: Resend Invitation Summary

**Full-stack resend invitation flow: server endpoint revokes old + creates new invitation, SDK method, admin panel Resend button for expired/revoked invitations with EN/ES strings**

## Performance

- **Duration:** 6 min
- **Started:** 2026-03-01T17:41:07Z
- **Completed:** 2026-03-01T17:47:17Z
- **Tasks:** 2
- **Files modified:** 14

## Accomplishments
- Server `resendInvitation` endpoint that revokes old invitation and creates new one for the same email with fresh 7-day expiry and sends email
- SDK `resendInvitation` method wired through InvitationApi interface, impl, and fake builder
- Admin panel shows "Resend" button (Secondary variant) for expired/revoked invitation rows
- MVI chain complete: ResendInvitation intent -> SetResending mutation -> handleResendInvitation handler -> list refresh

## Task Commits

Each task was committed atomically:

1. **Task 1: Server resend endpoint + SDK method + route class + fake builder** - `c2f5cc4` (feat)
2. **Task 2: Admin panel Resend button for expired/revoked invitations** - `db55256` (feat)

## Files Created/Modified
- `InvitationService.kt` - Added `resendInvitation()` method (revoke old + create new + send email)
- `InvitationRoutes.kt` - Added POST resend route with admin auth
- `ApiRoutes.kt` - Added `Groups.ResendInvitation` resource class
- `InvitationApi.kt` - Added `resendInvitation` interface method
- `InvitationApiImpl.kt` - Added implementation calling POST endpoint
- `FakeInvitationApiBuilder.kt` - Added fake stub for testing
- `AdminPanelIntent.kt` - Added `ResendInvitation` intent
- `AdminPanelMutation.kt` - Added `SetResending` mutation
- `AdminPanelModel.kt` - Added `isResending` state field
- `AdminPanelViewModel.kt` - Added `handleResendInvitation` handler + reduce case
- `AdminPanelScreen.kt` - Added `onResend` param, Resend button for expired/revoked rows
- `values/strings.xml` - Added `admin_resend_button` ("Resend")
- `values-es/strings.xml` - Added `admin_resend_button` ("Reenviar")
- `AppNavHost.kt` - Wired `onResend` callback at call site

## Decisions Made
- Resend = revoke old + create new invitation (not just extend expiry) — clean audit trail, fresh token
- Resend only allowed for expired/revoked invitations — active ones should be revoked explicitly first

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added onResend parameter to AppNavHost.kt call site**
- **Found during:** Task 2 (AdminPanelScreen composable)
- **Issue:** Plan only specified changes to `AdminPanelScreen.kt` and `InvitationsSection`, but adding `onResend` param to `AdminPanelScreen` composable also requires updating the call site in `AppNavHost.kt` — otherwise compilation fails
- **Fix:** Added `onResend = { viewModel.take(AdminPanelIntent.ResendInvitation(it)) }` to the `AdminPanelScreen()` call in `AppNavHost.kt`
- **Files modified:** `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt`
- **Verification:** `./gradlew :composeApp:compileKotlinJvm` passes
- **Committed in:** db55256 (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Essential for compilation. No scope creep.

## Issues Encountered
- Pre-existing test failures in `server:groups:test` (3 InvitationRoutesTest failures due to shared Testcontainers database state / `PostgresqlDataIntegrityViolationException`) — not caused by our changes. Compilation verification passes for all modules.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Resend invitation flow is complete end-to-end
- All Phase 21 gap closure plans (04, 05, 06) can be verified together in UAT

---
*Phase: 21-group-invitations-profiles*
*Completed: 2026-03-01*

## Self-Check: PASSED

All 14 modified files verified on disk. Both commit hashes (c2f5cc4, db55256) confirmed in git log. Key content patterns (resendInvitation, ResendInvitation, admin_resend_button) verified in target files.
