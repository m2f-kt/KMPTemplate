---
phase: 21-group-invitations-profiles
plan: 07
subsystem: api
tags: [ktor, exposed, r2dbc, invitation, email-validation, security]

# Dependency graph
requires:
  - phase: 21-group-invitations-profiles (plans 01-06)
    provides: "InvitationService, InvitationRepository, error types, StringKey infrastructure"
provides:
  - "Email validation in acceptInvitation() — prevents wrong-email users from hijacking invitations"
  - "deleteById() in InvitationRepository — permanent row removal"
  - "Resend deduplication — old invitation deleted instead of revoked"
  - "InvitationEmailMismatch error type with full EN/ES localization chain"
affects: [phase-22-onboarding]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Email validation guard in service layer (compare authenticated user email vs invitation email)"
    - "deleteWhere for permanent record removal in Exposed R2DBC"

key-files:
  created: []
  modified:
    - "server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt"
    - "server/groups/src/main/kotlin/com/m2f/server/groups/repository/InvitationRepository.kt"
    - "server/groups/src/main/kotlin/com/m2f/server/groups/errors/InvitationErrors.kt"
    - "core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt"
    - "core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt"
    - "server/core/config/src/main/kotlin/com/m2f/core/config/server/localization/ServerStrings.kt"

key-decisions:
  - "HTTP 422 (unprocessable) for email mismatch — validation error, not auth/gone"
  - "Resend = delete old + create new (not revoke old + create new) to eliminate duplicate rows"

patterns-established:
  - "Email validation guard pattern: look up authenticated user email, case-insensitive compare against invitation email"

requirements-completed: []

# Metrics
duration: 9min
completed: 2026-03-02
---

# Phase 21 Plan 07: Server Email Validation + Resend Deduplication Summary

**Email validation guard in acceptInvitation() with 422 EmailMismatch error, plus delete-on-resend to eliminate duplicate invitation rows**

## Performance

- **Duration:** 9 min
- **Started:** 2026-03-01T23:39:16Z
- **Completed:** 2026-03-01T23:48:20Z
- **Tasks:** 2
- **Files modified:** 19

## Accomplishments
- Server now rejects invitation acceptance when authenticated user's email doesn't match invitation email (returns 422 INVITATION_EMAIL_MISMATCH)
- Resending an invitation permanently deletes the old row instead of just revoking it, preventing duplicate entries in admin panel
- Full error propagation chain: DomainError → AppError → StringKey → string resources (EN + ES) across all 5 resolver modules

## Task Commits

Each task was committed atomically:

1. **Task 1: Server email validation in acceptInvitation + EmailMismatch error type** - `5682d13` (feat — committed alongside 21-08 docs in previous session)
2. **Task 2: Delete old invitation on resend to prevent duplicate rows** - `7efb1ee` (fix)

## Files Created/Modified
- `server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt` - Email validation in acceptInvitation(), deleteById in resendInvitation()
- `server/groups/src/main/kotlin/com/m2f/server/groups/repository/InvitationRepository.kt` - Added deleteById() using Exposed deleteWhere
- `server/groups/src/main/kotlin/com/m2f/server/groups/errors/InvitationErrors.kt` - Added InvitationEmailMismatch error class (422)
- `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` - Added EmailMismatch variant to Invitation sealed class
- `core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt` - Added INVITATION_EMAIL_MISMATCH entry
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/localization/ServerStrings.kt` - Added EN/ES entries for email mismatch
- `app/auth/.../StringKeyResolver.kt` - Added INVITATION_EMAIL_MISMATCH when branch
- `app/admin/.../StringKeyResolver.kt` - Added INVITATION_EMAIL_MISMATCH when branch
- `app/profile/.../StringKeyResolver.kt` - Added INVITATION_EMAIL_MISMATCH when branch
- `app/documents/.../StringKeyResolver.kt` - Added INVITATION_EMAIL_MISMATCH when branch
- `composeApp/.../StringKeyResolver.kt` - Added INVITATION_EMAIL_MISMATCH when branch
- 8 strings.xml files (EN + ES) - Added error_invitation_email_mismatch string resource

## Decisions Made
- Used HTTP 422 (unprocessable entity) for email mismatch — it's a validation error, not an authentication or resource-state error
- Changed resend from "revoke old + create new" to "delete old + create new" — old invitation data is not needed for audit since a brand new invitation is created with fresh token

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- Task 1 code was committed in a previous session bundled with 21-08 docs commit (5682d13) — code was already in HEAD, so Task 1 commit was a no-op. Task 2 committed cleanly as separate commit.
- Stale Gradle cache caused phantom compilation errors in `:app:auth:compileKotlinJvm` — resolved with `--rerun-tasks` flag.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 21 is now complete (all 8 plans shipped)
- Ready for Phase 22 (Developer Onboarding)
- All invitation flow features fully implemented: invite, accept, revoke, resend, email validation, localization

## Self-Check: PASSED

All files verified present, all commits found, all key patterns confirmed in codebase.

---
*Phase: 21-group-invitations-profiles*
*Completed: 2026-03-02*
