---
phase: 21-group-invitations-profiles
plan: 05
subsystem: sdk, models, auth, admin, profile, documents, composeApp
tags: [error-handling, mvi, localization, invitation-flow, string-resources]

# Dependency graph
requires:
  - phase: 21-group-invitations-profiles
    plan: 01
    provides: "InvitationResponse DTO with isRevoked field"
provides:
  - "HTTP 410 Gone mapping to ServerMapped error in ErrorMapper"
  - "4 invitation error StringKey entries with exhaustive resolver coverage"
  - "Revoked invitation UI state in InviteAccept MVI chain"
  - "Revoked-state UI block in InviteAcceptScreen"
affects: [error-handling, invitation-acceptance, localization]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "HTTP status code to AppError mapping for domain-specific errors (410 → ServerMapped)"
    - "Exhaustive StringKey enum with synchronized resolver branches across 5 modules"

key-files:
  created: []
  modified:
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt"
    - "core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/InviteAcceptModel.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/InviteAcceptMutation.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/InviteAcceptViewModel.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/InviteAcceptScreen.kt"
    - "app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/StringKeyResolver.kt"
    - "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/StringKeyResolver.kt"
    - "app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/StringKeyResolver.kt"
    - "app/documents/src/commonMain/kotlin/com/m2f/template/app/documents/StringKeyResolver.kt"
    - "composeApp/src/commonMain/kotlin/com/m2f/template/localization/StringKeyResolver.kt"

key-decisions:
  - "Map HTTP 410 to ServerMapped (same as 422) to preserve server error code for StringKey resolution"
  - "Add all 4 invitation error StringKey entries at once for complete coverage"
  - "Use isRevoked field from existing InvitationResponse DTO (already provided by plan 21-01)"

patterns-established:
  - "When adding StringKey entries, all 5 StringKeyResolver files must be updated simultaneously"
  - "String resources must be added to all modules with a StringKeyResolver (not just the consuming module)"

requirements-completed: []

# Metrics
duration: 15min
completed: 2026-03-01
---

# Phase 21 Plan 05: Revoked Invitation Error Propagation Summary

**Full error propagation chain for revoked invitations: HTTP 410 mapping, StringKey entries, MVI state threading, and revoked-state UI**

## Performance

- **Duration:** 15 min
- **Started:** 2026-03-01T18:00:00Z
- **Completed:** 2026-03-01T18:15:00Z
- **Tasks:** 2
- **Files modified:** 20

## Accomplishments
- Mapped HTTP 410 Gone to `AppError.Client.ServerMapped` in ErrorMapper, preserving server error code
- Added 4 StringKey entries: `INVITATION_REVOKED`, `INVITATION_EXPIRED`, `INVITATION_ALREADY_ACCEPTED`, `INVITATION_NOT_FOUND`
- Added EN/ES invitation error string resources across all 5 modules with StringKeyResolvers (9 strings.xml files)
- Updated all 5 StringKeyResolver files with exhaustive `when` branches for new entries
- Threaded `isRevoked` through InviteAccept MVI chain (Model → Mutation → ViewModel → Screen)
- Added revoked-state UI block with error alert, hint text, and request-new-invitation button
- Guarded action buttons behind `!state.isRevoked` condition

## Task Commits

Each task was committed atomically:

1. **Task 1: ErrorMapper 410 mapping + StringKey entries + string resources + resolvers** - `6c27873` (feat)
2. **Task 2: isRevoked MVI chain + revoked-state UI** - `ad948c3` (feat)

## Files Created/Modified
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt` - HTTP 410 → ServerMapped mapping
- `core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt` - 4 invitation error entries
- `app/auth/.../InviteAcceptModel.kt` - `isRevoked: Boolean = false` field
- `app/auth/.../InviteAcceptMutation.kt` - `isRevoked` in SetInvitationDetails
- `app/auth/.../InviteAcceptViewModel.kt` - Wire isRevoked from DTO through mutation and reduce
- `app/auth/.../InviteAcceptScreen.kt` - Revoked-state UI block + action button guard
- 5x `StringKeyResolver.kt` - 4 invitation imports + when branches each
- 5x EN `strings.xml` - 4 invitation error strings each
- 4x ES `strings.xml` - 4 invitation error strings each (documents has no ES file)

## Decisions Made
- HTTP 410 mapped to `ServerMapped` (same pattern as 422) to preserve server's error code for StringKey resolution
- All 4 invitation error entries added at once for complete coverage, even though only INVITATION_REVOKED was the immediate UAT gap
- Used existing `isRevoked` field from `InvitationResponse` DTO (plan 21-01 already added it)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- XML files lost closing `</resources>` tags during initial edits (edit pattern consumed the closing tag). Fixed before committing.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- UAT gap 3 (revoked invitation error message) is now closed
- Full error propagation chain: Server 410 → ErrorMapper → ServerMapped → StringKey → UI
- InviteAcceptScreen correctly shows revoked/expired/already-accepted states with appropriate UI

---
*Phase: 21-group-invitations-profiles*
*Completed: 2026-03-01*
