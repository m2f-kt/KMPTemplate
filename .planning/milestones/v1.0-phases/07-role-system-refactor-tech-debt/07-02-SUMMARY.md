---
phase: 07-role-system-refactor-tech-debt
plan: 02
subsystem: auth, database
tags: [exposed-table, foreign-key, migration, role-authorization, typed-role, jwt-claims]

# Dependency graph
requires:
  - phase: 07-role-system-refactor-tech-debt
    plan: 01
    provides: UserRole sealed class with User/Admin/PowerAdmin variants and flat-string serializer
provides:
  - RolesTable with seeded User/Admin/PowerAdmin rows and id/name/level columns
  - UsersTable.roleId FK referencing RolesTable.id (replaces role varchar)
  - Database migration for roles table creation, seeding, and users FK addition
  - Typed UserRole throughout server: repository, JWT, services, RBAC, routes
affects: [server-auth, server-ai, client-features]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Hardcoded roleId mapping (when expression) for seeded FK values instead of join queries"
    - "Old varchar column left in DB (ignored by Exposed) to avoid risky ALTER TABLE DROP over R2DBC"

key-files:
  created:
    - "server/auth/src/main/kotlin/com/m2f/server/auth/tables/RolesTable.kt"
  modified:
    - "server/auth/src/main/kotlin/com/m2f/server/auth/tables/UsersTable.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/Auth.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/repository/UserRepository.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/service/AuthService.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/service/OAuthService.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/service/UserService.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/security/JwtTokenProvider.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/authorization/RoleAuthorization.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/routes/UserRoutes.kt"

key-decisions:
  - "Hardcoded roleId-to-UserRole when() mapping instead of join with RolesTable (simpler for template project)"
  - "Old role varchar left in DB to avoid risky ALTER TABLE DROP COLUMN over R2DBC"
  - "Suppressed createMissingTablesAndColumns deprecation warning (template project, not production migration)"

patterns-established:
  - "FK roleId mapping: when(roleId) { 2 -> Admin; 3 -> PowerAdmin; else -> User } for seeded role IDs"

# Metrics
duration: 6min
completed: 2026-02-15
---

# Phase 7 Plan 02: Server Role System Refactor Summary

**RolesTable with seeded FK rows, UsersTable.roleId replacing varchar, and typed UserRole across repository/JWT/services/RBAC/routes**

## Performance

- **Duration:** 6 min
- **Started:** 2026-02-15T12:06:37Z
- **Completed:** 2026-02-15T12:13:30Z
- **Tasks:** 2
- **Files modified:** 10

## Accomplishments
- Created RolesTable with id/name/level columns and seeded User (id=1), Admin (id=2), PowerAdmin (id=3) rows
- Replaced UsersTable.role varchar with roleId FK referencing RolesTable.id with default 1
- Refactored all server code to use typed UserRole: repository insert/read, JWT claims, AuthService, OAuthService, RoleAuthorization, UserRoutes
- Eliminated all string-based role handling from server application code

## Task Commits

Each task was committed atomically:

1. **Task 1: Create RolesTable, update UsersTable with FK, add migration** - `2d62e19` (feat)
2. **Task 2: Refactor server code to use typed UserRole everywhere** - `44b155e` (feat)

**Plan metadata:** pending (docs: complete plan)

## Files Created/Modified
- `server/auth/.../tables/RolesTable.kt` - New Exposed Table for roles with id/name/level columns and unique name index
- `server/auth/.../tables/UsersTable.kt` - roleId FK replacing role varchar, default 1 (User)
- `server/auth/.../Auth.kt` - CreateRolesTableAndMigrateUsersMigration: creates roles table, seeds 3 rows, adds role_id to users
- `server/auth/.../repository/UserRepository.kt` - UserRecord.role typed as UserRole, insert accepts UserRole, toUserRecord maps roleId
- `server/auth/.../security/JwtTokenProvider.kt` - generateAccessToken/generateTokenPair accept UserRole, encode role.value
- `server/auth/.../service/AuthService.kt` - Uses UserRole.User for registration, typed role from UserRecord for login/refresh
- `server/auth/.../service/OAuthService.kt` - Uses UserRole.User for new users, typed role from UserRecord for existing
- `server/auth/.../service/UserService.kt` - toUserResponse passes typed role directly (no fromString)
- `server/auth/.../authorization/RoleAuthorization.kt` - RoleConfig.roles typed as Set<UserRole>, withRole accepts UserRole variants
- `server/auth/.../routes/UserRoutes.kt` - withRole(UserRole.Admin) instead of withRole("ADMIN")

## Decisions Made
- Hardcoded roleId-to-UserRole `when()` mapping chosen over join with RolesTable (simpler, no extra query complexity for a template project with 3 known roles)
- Old `role` varchar column intentionally left in database -- Exposed ignores undeclared columns, and ALTER TABLE DROP COLUMN over R2DBC is risky; future cleanup migration can remove it
- Suppressed `createMissingTablesAndColumns` deprecation warning since this is a template project (production would use Flyway)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None - all changes compiled cleanly on first attempt.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 7 is complete: UserRole sealed class in core:models + typed throughout server
- All string-based role handling eliminated from application code
- JWT claims still encode role as plain string via UserRole.value (backward compatible with existing tokens)
- Database has referential integrity via roles table FK

## Self-Check: PASSED

All 10 files verified present. Both task commits (2d62e19, 44b155e) verified in git log.

---
*Phase: 07-role-system-refactor-tech-debt*
*Completed: 2026-02-15*
