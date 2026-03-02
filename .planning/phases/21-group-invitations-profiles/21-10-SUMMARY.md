---
phase: 21-group-invitations-profiles
plan: 10
subsystem: api
tags: [exposed, kotlin, invitations, consistency, membership]

# Dependency graph
requires:
  - phase: 21-group-invitations-profiles
    provides: InvitationService, InvitationRepository, MembershipRepository
provides:
  - Membership-consistent invitation listing (stale status override)
  - Resend guard preventing resend to existing members
  - Accept cleanup marking all same-email+group invitations as accepted
affects: [invitation-ui, group-management]

# Tech tracking
tech-stack:
  added: []
  patterns: [membership-cross-reference, bulk-status-correction, guard-pattern]

key-files:
  created: []
  modified:
    - server/groups/src/main/kotlin/com/m2f/server/groups/repository/InvitationRepository.kt
    - server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt

key-decisions:
  - "Cross-reference member emails at query time in listInvitations rather than DB trigger for simplicity"
  - "markAcceptedByGroupAndEmail uses case-insensitive email matching via Exposed lowerCase()"

patterns-established:
  - "Guard pattern: check membership before resend to prevent stale re-invitations"
  - "Bulk status correction: mark all invitations for email+group when any one is accepted"

requirements-completed: [INVITE-03, INVITE-04]

# Metrics
duration: 4min
completed: 2026-03-02
---

# Phase 21 Plan 10: Invitation-Membership Consistency Summary

**Server-side consistency logic: listInvitations cross-references membership to override stale statuses, resendInvitation rejects for existing members, acceptInvitation cleans up duplicate invitations**

## Performance

- **Duration:** 4 min
- **Started:** 2026-03-02T00:32:33Z
- **Completed:** 2026-03-02T00:37:08Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- Added `markAcceptedByGroupAndEmail()` repository method for bulk invitation cleanup
- listInvitations now shows existing-member invitations as accepted regardless of raw DB status
- resendInvitation rejects with MemberAlreadyInGroup when target email is already a group member
- acceptInvitation marks all other invitations for same email+group as accepted (cleanup)

## Task Commits

Each task was committed atomically:

1. **Task 1: Add markAcceptedByGroupAndEmail to InvitationRepository** - `5bb4b3c` (feat)
2. **Task 2: Add membership consistency logic to InvitationService** - `f9ab286` (feat)

## Files Created/Modified
- `server/groups/src/main/kotlin/com/m2f/server/groups/repository/InvitationRepository.kt` - Added markAcceptedByGroupAndEmail() for bulk acceptance cleanup with case-insensitive email matching
- `server/groups/src/main/kotlin/com/m2f/server/groups/service/InvitationService.kt` - Added membership cross-reference in listInvitations, guard in resendInvitation, cleanup in acceptInvitation

## Decisions Made
- Cross-reference member emails at query time in listInvitations (not DB trigger) — simpler, no schema changes needed
- Case-insensitive email matching via Exposed `lowerCase()` in the bulk update query

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added missing Exposed imports for lowerCase and isNull**
- **Found during:** Task 1 (markAcceptedByGroupAndEmail implementation)
- **Issue:** `lowerCase()` and `isNull()` are top-level extensions in `org.jetbrains.exposed.v1.core` and needed explicit imports
- **Fix:** Added `import org.jetbrains.exposed.v1.core.isNull` and `import org.jetbrains.exposed.v1.core.lowerCase`
- **Files modified:** InvitationRepository.kt
- **Verification:** BUILD SUCCESSFUL after adding imports
- **Committed in:** `5bb4b3c` (part of Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Import fix required for compilation. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Plan 10 closes the invitation-membership consistency gap (UAT-2 test #6)
- All 10 plans in Phase 21 complete
- Ready for Phase 22 or next priority

---
*Phase: 21-group-invitations-profiles*
*Completed: 2026-03-02*
