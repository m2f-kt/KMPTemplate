---
phase: 02-server-auth-users
plan: 03
subsystem: auth
tags: [rbac, user-profile, ktor-plugin, arrow-raise, koin, accumulated-validation]

# Dependency graph
requires:
  - phase: 02-server-auth-users
    plan: 01
    provides: "UserRepository, AuthErrors (UserNotFound, Forbidden), Koin authModule, conduit/conduitAuth pattern, DomainError hierarchy"
provides:
  - "RoleAuthorizationPlugin for route-scoped RBAC via createRouteScopedPlugin"
  - "withRole() convenience extension for role-guarded route blocks"
  - "UserService with getProfile, updateProfile (accumulated validation), getUserById"
  - "GET /api/users/me endpoint for authenticated user profile retrieval"
  - "PUT /api/users/me endpoint for profile updates with validation"
  - "GET /api/users/{id} endpoint restricted to ADMIN role"
affects: [03-client-sdk-networking]

# Tech tracking
tech-stack:
  added: []
  patterns: [createRouteScopedPlugin with AuthenticationChecked hook for RBAC, optional field zipOrAccumulate validation pattern]

key-files:
  created:
    - server/auth/src/main/kotlin/com/m2f/server/auth/authorization/RoleAuthorization.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/UserService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/routes/UserRoutes.kt
  modified:
    - server/auth/src/main/kotlin/com/m2f/server/auth/di/AuthModule.kt
    - server/src/main/kotlin/com/m2f/template/Application.kt

key-decisions:
  - "Used io.ktor.server.application package for createRouteScopedPlugin and RouteScopedPlugin (not io.ktor.server.routing as researched)"
  - "Used Uuid.parse() for string-to-UUID conversion in UserService (Kotlin stdlib Uuid API)"
  - "Optional field validation with zipOrAccumulate: null fields pass through, only non-null fields are validated"

patterns-established:
  - "RBAC pattern: withRole('ADMIN') { routes } installs RoleAuthorizationPlugin checking JWT role claim"
  - "Optional field validation: zipOrAccumulate with nullable let blocks for partial update requests"
  - "User profile service: context(Raise<DomainError>) with ensure for null checks, withError for accumulated validation"

# Metrics
duration: 5min
completed: 2026-02-11
---

# Phase 02 Plan 03: User Profile & RBAC Summary

**User profile CRUD with GET/PUT /api/users/me, admin GET /api/users/{id}, and reusable route-scoped RBAC plugin enforcing JWT role claims**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-11T16:54:36Z
- **Completed:** 2026-02-11T16:59:53Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Created RoleAuthorizationPlugin using Ktor's createRouteScopedPlugin with AuthenticationChecked hook for reusable RBAC
- Built UserService with getProfile, updateProfile (accumulated validation via zipOrAccumulate), and getUserById using Arrow Raise
- Wired GET /api/users/me, PUT /api/users/me, and admin-only GET /api/users/{id} routes with full Koin DI integration

## Task Commits

Each task was committed atomically:

1. **Task 1: RBAC plugin and UserService** - `e8c3678` (feat)
2. **Task 2: User routes, Koin wiring, and Application integration** - `8ab4fb1` (feat)

## Files Created/Modified
- `server/auth/src/main/kotlin/com/m2f/server/auth/authorization/RoleAuthorization.kt` - Route-scoped RBAC plugin with RoleConfig, AuthenticationChecked hook, and withRole() extension
- `server/auth/src/main/kotlin/com/m2f/server/auth/service/UserService.kt` - Profile read/update/admin-lookup business logic with Arrow Raise context
- `server/auth/src/main/kotlin/com/m2f/server/auth/routes/UserRoutes.kt` - GET/PUT /api/users/me and GET /api/users/{id} route definitions
- `server/auth/src/main/kotlin/com/m2f/server/auth/di/AuthModule.kt` - Added UserService singleton to Koin authModule
- `server/src/main/kotlin/com/m2f/template/Application.kt` - Wired userRoutes into main routing block

## Decisions Made
- Used `io.ktor.server.application.createRouteScopedPlugin` and `io.ktor.server.application.RouteScopedPlugin` (the research doc referenced `io.ktor.server.routing` package which does not contain these in Ktor 3.4.0)
- Used `Uuid.parse(userId)` for string-to-UUID conversion consistent with existing Kotlin stdlib Uuid usage in the project
- Optional field validation pattern: `request.name?.let { validateName(it) }` inside `zipOrAccumulate` -- null fields skip validation, only provided fields are validated

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] createRouteScopedPlugin lives in io.ktor.server.application, not io.ktor.server.routing**
- **Found during:** Task 1 (RBAC plugin compilation)
- **Issue:** Plan and research document specified `io.ktor.server.routing.createRouteScopedPlugin` but in Ktor 3.4.0 the function is in `io.ktor.server.application`
- **Fix:** Changed imports to `io.ktor.server.application.createRouteScopedPlugin` and `io.ktor.server.application.RouteScopedPlugin`
- **Files modified:** RoleAuthorization.kt
- **Verification:** `:server:auth:compileKotlin` passes
- **Committed in:** e8c3678 (Task 1 commit)

**2. [Rule 3 - Blocking] Plan 02-02 code (login/refresh/logout) present but uncommitted**
- **Found during:** Task 1 (pre-commit git status)
- **Issue:** AuthService.kt and AuthRoutes.kt had uncommitted changes from plan 02-02 (login, refresh, logout methods and routes). These were working tree changes that needed to be committed before 02-03 work
- **Fix:** Committed the 02-02 changes as a separate feat commit (12f608a) before proceeding with 02-03 task commits
- **Files modified:** AuthService.kt, AuthRoutes.kt
- **Verification:** Clean git status between 02-02 and 02-03 commits
- **Committed in:** 12f608a (separate 02-02 commit)

---

**Total deviations:** 2 auto-fixed (1 bug, 1 blocking)
**Impact on plan:** Import fix was trivial. The uncommitted 02-02 code was already correct and just needed committing. No scope creep.

## Issues Encountered
- Docker daemon not running -- runtime verification (curl tests) could not be performed. Compilation verification passes. Runtime testing deferred to integration test phase or manual verification.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Complete user management CRUD operational: register, login, refresh, logout, get profile, update profile, admin get user
- RBAC plugin is reusable for any future endpoint needing role-based access control
- Phase 02 (Server Auth & Users) is fully complete, ready for Phase 03 (Client SDK & Networking)

## Self-Check: PASSED

All 3 created files and 2 modified files verified present. Both task commits (e8c3678, 8ab4fb1) verified in git log. `:server:compileKotlin` passes.

---
*Phase: 02-server-auth-users*
*Completed: 2026-02-11*
