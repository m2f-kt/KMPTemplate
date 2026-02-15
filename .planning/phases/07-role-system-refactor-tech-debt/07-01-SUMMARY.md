---
phase: 07-role-system-refactor-tech-debt
plan: 01
subsystem: models, auth, ai
tags: [kotlin-sealed-class, kotlinx-serialization, custom-kserializer, exposed-r2dbc, user-role]

# Dependency graph
requires:
  - phase: 05-client-features-profile-dashboard-setup
    provides: UserTier sealed class, UserResponse DTO, UserRepository
provides:
  - UserRole sealed class with User/Admin/PowerAdmin variants and flat-string serializer
  - UserResponse.role typed as UserRole (backward-compatible wire format)
  - UserRepository.count() for database row counting
  - getUserCount() returning actual DB count
affects: [07-02-PLAN, server-auth, server-ai]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Custom KSerializer for sealed class flat-string serialization (UserRoleSerializer)"
    - "Exposed R2DBC select().count() for aggregate queries"

key-files:
  created:
    - "core/models/src/commonMain/kotlin/com/m2f/template/models/UserRole.kt"
  modified:
    - "core/models/src/commonMain/kotlin/com/m2f/template/models/dto/UserDtos.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/repository/UserRepository.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/service/UserService.kt"
    - "server/ai/src/main/kotlin/com/m2f/server/ai/tools/UserTools.kt"
    - "server/ai/src/main/kotlin/com/m2f/server/ai/agents/ChatStreamingStrategy.kt"

key-decisions:
  - "Custom KSerializer (UserRoleSerializer) for flat-string wire format instead of @SerialName polymorphic approach"
  - "UserRole.fromString() defaults to User for unrecognized roles (safe fallback)"
  - "Exposed select().count() used for UserRepository.count() (no selectAll in R2DBC API)"

patterns-established:
  - "Custom KSerializer pattern: PrimitiveSerialDescriptor + encodeString/decodeString for sealed class flat serialization"

# Metrics
duration: 8min
completed: 2026-02-15
---

# Phase 7 Plan 01: Core Role Type & Tech Debt Summary

**UserRole sealed class with custom flat-string KSerializer, typed UserResponse.role, actual DB user count, and stale SSE comment fix**

## Performance

- **Duration:** 8 min
- **Started:** 2026-02-15T11:54:31Z
- **Completed:** 2026-02-15T12:02:36Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- Created UserRole sealed class (User/Admin/PowerAdmin) with custom KSerializer for backward-compatible flat-string wire format
- Typed UserResponse.role as UserRole with updated tier extension mapping via role.value
- Added UserRepository.count() and wired getUserCount() to return actual database count
- Fixed stale SSE reference in ChatStreamingStrategy.kt comment to WebSocket

## Task Commits

Each task was committed atomically:

1. **Task 1: Create UserRole sealed class and type UserResponse.role** - `66d3375` (feat)
2. **Task 2: Fix UserTools.getUserCount() and stale SSE comment** - `5a65703` (feat)

**Plan metadata:** pending (docs: complete plan)

## Files Created/Modified
- `core/models/.../UserRole.kt` - UserRole sealed class with User/Admin/PowerAdmin variants and UserRoleSerializer
- `core/models/.../dto/UserDtos.kt` - UserResponse.role typed as UserRole, tier extension updated
- `server/auth/.../repository/UserRepository.kt` - Added count() method using Exposed select().count()
- `server/auth/.../service/UserService.kt` - toUserResponse() maps String role to UserRole.fromString()
- `server/ai/.../tools/UserTools.kt` - getUserCount() calls userRepository.count() for actual DB count
- `server/ai/.../agents/ChatStreamingStrategy.kt` - Comment fixed from SSE to WebSocket delivery

## Decisions Made
- Custom KSerializer (UserRoleSerializer) chosen over @SerialName polymorphic approach to maintain flat string wire format ("ADMIN" not {"type":"ADMIN"})
- UserRole.fromString() defaults to User for unrecognized roles (safe fallback, consistent with UserTier.fromString pattern)
- Used select(UsersTable.columns).count() for count() since selectAll is not available in Exposed R2DBC 1.0.0

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed UserService.toUserResponse() type mismatch**
- **Found during:** Task 2 (server:auth compilation)
- **Issue:** UserService.toUserResponse() passes String role to UserResponse constructor which now expects UserRole after Task 1 changes
- **Fix:** Added UserRole import and changed `role = role` to `role = UserRole.fromString(role)` in the mapper
- **Files modified:** server/auth/src/main/kotlin/com/m2f/server/auth/service/UserService.kt
- **Verification:** ./gradlew :server:auth:compileKotlin passes
- **Committed in:** 5a65703 (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Essential fix for compilation. The plan scoped UserService changes to Plan 02, but the type change in UserResponse.role made this immediately necessary for server:auth to compile.

## Issues Encountered
None - selectAll not available in Exposed R2DBC but select().count() works equivalently.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- UserRole sealed class established in core:models, ready for Plan 02 to thread through server-side code
- Plan 02 can now type UserRecord.role as UserRole, update JWT claims, and update withRole() authorization
- The UserService.toUserResponse() fix done here means Plan 02 only needs to update UserRecord.role typing and AuthService/OAuthService role handling

## Self-Check: PASSED

All 6 files verified present. Both task commits (66d3375, 5a65703) verified in git log.

---
*Phase: 07-role-system-refactor-tech-debt*
*Completed: 2026-02-15*
