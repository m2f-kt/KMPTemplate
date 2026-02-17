---
phase: 02-server-auth-users
plan: 02
subsystem: auth
tags: [jwt, login, refresh-token-rotation, logout, arrow-raise, ktor]

# Dependency graph
requires:
  - phase: 02-server-auth-users/01
    provides: "UserRepository, RefreshTokenRepository, JwtTokenProvider, PasswordHasher, AuthService (register), conduit/conduitAuth patterns, AuthErrors"
provides:
  - "AuthService.login() with credential verification and token generation"
  - "AuthService.refresh() with refresh token rotation (old revoked, new issued)"
  - "AuthService.logout() revoking all refresh tokens for a user"
  - "POST /api/auth/login endpoint via conduit pattern"
  - "POST /api/auth/refresh endpoint via conduit pattern"
  - "POST /api/auth/logout endpoint via conduitAuth (authenticated)"
affects: [03-client-sdk-networking, 02-server-auth-users/03]

# Tech tracking
tech-stack:
  added: []
  patterns: [refresh token rotation with SHA-256 hash comparison, same InvalidCredentials error for both missing user and wrong password (prevents enumeration), Uuid.parse() for JWT userId string to kotlin.uuid.Uuid conversion]

key-files:
  created: []
  modified:
    - server/auth/src/main/kotlin/com/m2f/server/auth/service/AuthService.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/routes/AuthRoutes.kt

key-decisions:
  - "Used same InvalidCredentials error for both non-existent email and wrong password to prevent user enumeration"
  - "Used Uuid.parse() instead of java.util.UUID.fromString() to stay consistent with kotlin.uuid.Uuid throughout the codebase"
  - "Refresh token rotation: old token revoked before new token issued (fail-safe -- if generation fails, old token is already revoked)"

patterns-established:
  - "Login pattern: findByEmail -> verify password -> generateTokenPair -> hash+store refresh token"
  - "Refresh rotation pattern: hash incoming token -> findValidToken -> revokeById -> lookup user -> generateTokenPair -> store new"
  - "Logout pattern: parse userId from JWT -> revokeByUserId (revokes all refresh tokens)"
  - "Authenticated route pattern: authenticate { post(\"/path\") { conduitAuth { userId -> ... } } }"

# Metrics
duration: 7min
completed: 2026-02-11
---

# Phase 02 Plan 02: Login, Refresh, Logout Summary

**Login with credential verification, refresh token rotation (revoke-then-reissue), and authenticated logout revoking all user sessions**

## Performance

- **Duration:** 7 min
- **Started:** 2026-02-11T16:54:36Z
- **Completed:** 2026-02-11T17:01:10Z
- **Tasks:** 1
- **Files modified:** 2

## Accomplishments
- Implemented AuthService.login() with BCrypt password verification and same InvalidCredentials error for missing user and wrong password (prevents user enumeration)
- Implemented AuthService.refresh() with full refresh token rotation: hash incoming token, find valid record, revoke old, look up user for role, generate new pair, store new hashed token
- Implemented AuthService.logout() revoking all refresh tokens for the authenticated user via Uuid.parse()
- Added three new routes: POST /api/auth/login (public), POST /api/auth/refresh (public), POST /api/auth/logout (JWT-authenticated via Ktor authenticate block)

## Task Commits

Each task was committed atomically:

1. **Task 1: Login service method and route (all 6 steps)** - `12f608a` (feat)

## Files Created/Modified
- `server/auth/src/main/kotlin/com/m2f/server/auth/service/AuthService.kt` - Added login(), refresh(), and logout() methods with Arrow Raise context receivers
- `server/auth/src/main/kotlin/com/m2f/server/auth/routes/AuthRoutes.kt` - Added POST /login, /refresh, and /logout routes using conduit and conduitAuth patterns

## Decisions Made
- Used same `InvalidCredentials()` error for both non-existent email and wrong password -- prevents user enumeration by returning identical 401 responses
- Used `Uuid.parse(userId)` instead of `java.util.UUID.fromString()` for consistency with kotlin.uuid.Uuid used throughout the Exposed R2DBC layer
- Refresh token rotation revokes old token before generating new one -- fail-safe design where if token generation fails, the old token is already invalidated (prevents replay)

## Deviations from Plan

None - plan executed exactly as written. All 6 steps (login service, login route, refresh service, refresh route, logout service, logout route) implemented as specified.

## Issues Encountered
None - compilation passed on first attempt. All existing patterns from Plan 02-01 (conduit, conduitAuth, Arrow Raise, repository methods) were used directly.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Full authentication lifecycle complete: register -> login -> refresh -> logout
- Ready for Plan 02-03 (user profile management and role-based authorization)
- conduitAuth pattern verified working for authenticated routes (logout endpoint)
- Refresh token rotation prevents token reuse attacks

## Self-Check: PASSED

All 2 modified files verified present. Task commit (12f608a) verified in git log. `:server:compileKotlin` passes.

---
*Phase: 02-server-auth-users*
*Completed: 2026-02-11*
