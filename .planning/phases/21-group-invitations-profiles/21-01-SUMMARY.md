---
phase: 21-group-invitations-profiles
plan: 01
subsystem: api
tags: [ktor, exposed, r2dbc, invitation, authorization]

requires:
  - phase: 18.1-group-invitations
    provides: InvitationsTable, InvitationRepository, InvitationService, InvitationRoutes
provides:
  - revokedAt column on invitations table with ALTER TABLE migration
  - findByGroupId, findById, revokeById repository methods
  - listInvitations and revokeInvitation service methods with admin authorization
  - GET and POST routes for listing and revoking invitations
  - InvitationResponse.isRevoked field in shared DTO
  - AppError.Invitation.Revoked and InvitationRevoked domain error
  - ListInvitations and RevokeInvitation type-safe resources
affects: [21-02, 21-03]

tech-stack:
  added: []
  patterns: [ALTER TABLE migration via TransactionManager.current().exec()]

key-files:
  created: []
  modified:
    - server/groups/src/main/kotlin/com/m2f/server/groups/tables/InvitationsTable.kt
    - server/groups/src/main/kotlin/com/m2f/server/groups/repository/InvitationRepository.kt
    - server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt
    - server/groups/src/main/kotlin/com/m2f/server/groups/routes/InvitationRoutes.kt
    - server/groups/src/main/kotlin/com/m2f/server/groups/Groups.kt
    - server/groups/src/main/kotlin/com/m2f/server/groups/errors/InvitationErrors.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/InvitationDtos.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt

key-decisions:
  - "Added CreateInvitationsTableMigration for the InvitationsTable which was previously created without a migration"
  - "Revoke check added before accepted check in acceptInvitation to fail fast on revoked invitations"
  - "revokeInvitation returns Map<String, String> with message rather than empty response"

patterns-established:
  - "ALTER TABLE migration pattern: TransactionManager.current().exec() within migrate()"

requirements-completed: [INVITE-03]

duration: 8min
completed: 2026-02-26
---

# Plan 21-01: Server-Side List & Revoke Summary

**Server-side list and revoke invitation endpoints with revokedAt migration, repository/service/route layers, and admin authorization**

## Performance

- **Duration:** 8 min
- **Tasks:** 3
- **Files modified:** 9

## Accomplishments
- Added revokedAt nullable datetime column to InvitationsTable with proper ALTER TABLE migration
- Implemented findByGroupId, findById, revokeById repository methods
- Built listInvitations and revokeInvitation service methods with admin authorization checks
- Added GET and POST routes inside admin-protected routing block
- Updated InvitationResponse DTO with isRevoked field
- Added revocation guard in acceptInvitation to reject revoked invitations

## Task Commits

Each task was committed atomically:

1. **Task 1: Add revokedAt column migration + update InvitationsTable + InvitationRecord + InvitationResponse** - `0bfe4cb` (feat)
2. **Task 2: Add findByGroupId/findById/revokeById to InvitationRepository + listInvitations/revokeInvitation to InvitationService** - `648eb4f` (feat)
3. **Task 3: Add list/revoke routes to InvitationRoutes** - `3f8f29d` (feat)

## Files Created/Modified
- `server/groups/src/main/kotlin/com/m2f/server/groups/tables/InvitationsTable.kt` - Added revokedAt column
- `server/groups/src/main/kotlin/com/m2f/server/groups/repository/InvitationRepository.kt` - Added revokedAt to record, findByGroupId, findById, revokeById methods
- `server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt` - Added listInvitations, revokeInvitation, revoke guard in accept
- `server/groups/src/main/kotlin/com/m2f/server/groups/routes/InvitationRoutes.kt` - Added list and revoke routes
- `server/groups/src/main/kotlin/com/m2f/server/groups/Groups.kt` - Added CreateInvitationsTableMigration and AddRevokedAtColumnMigration
- `server/groups/src/main/kotlin/com/m2f/server/groups/errors/InvitationErrors.kt` - Added InvitationRevoked error
- `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/InvitationDtos.kt` - Added isRevoked field
- `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt` - Added ListInvitations and RevokeInvitation resources
- `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` - Added Invitation.Revoked error

## Decisions Made
- Added a CreateInvitationsTableMigration for the invitations table which was previously created without a proper migration entry
- Revoke check placed before accepted check in acceptInvitation for fail-fast behavior

## Deviations from Plan
None - plan executed exactly as written

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Backend endpoints ready for SDK integration (Plan 21-02)
- Repository methods ready for integration testing (Plan 21-03)

---
*Phase: 21-group-invitations-profiles*
*Completed: 2026-02-26*
