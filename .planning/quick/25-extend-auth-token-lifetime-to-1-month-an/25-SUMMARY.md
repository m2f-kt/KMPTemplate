---
phase: quick-25
plan: 01
subsystem: auth
tags: [jwt, token-lifetime, session-expiry, sharedflow, navigation]

# Dependency graph
requires:
  - phase: 02-auth
    provides: "JWT auth with refresh rotation, AuthInterceptor, TokenStorage"
  - phase: 04-navigation
    provides: "AppNavHost with NavController, LoginRoute"
provides:
  - "Extended token lifetimes (1 day access, 30 days refresh)"
  - "Session expiry signaling via AuthInterceptor.sessionExpired SharedFlow"
  - "Auto-navigation to login on session expiry with full back stack clear"
affects: [auth, navigation]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "SharedFlow for one-shot event signaling from interceptor to UI"
    - "LaunchedEffect + collect for observing events in Compose navigation"

key-files:
  created: []
  modified:
    - "server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt"
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt"
    - "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"

key-decisions:
  - "SharedFlow with extraBufferCapacity=1 for session expiry (one-shot event, not state)"
  - "tryEmit after both clearTokens() paths (else branch + catch block) for complete coverage"
  - "No snackbar/toast for session expiry -- navigation to login is sufficient UX signal"

patterns-established:
  - "SharedFlow event signaling: interceptor -> UI layer via Koin-injected singleton"

requirements-completed: [AUTH-TOKEN-LIFETIME, AUTH-SESSION-EXPIRY]

# Metrics
duration: 3min
completed: 2026-02-17
---

# Quick Task 25: Extend Auth Token Lifetime Summary

**Extended JWT token defaults (access: 1 day, refresh: 30 days) with auto-logout on session expiry via SharedFlow event signaling**

## Performance

- **Duration:** 3 min
- **Started:** 2026-02-16T23:12:17Z
- **Completed:** 2026-02-16T23:15:14Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Access token default extended from 15 minutes to 1 day (86400000ms)
- Refresh token default extended from 7 days to 30 days (2592000000ms)
- AuthInterceptor emits sessionExpired event when refresh token fails (both error paths covered)
- AppNavHost observes sessionExpired and navigates to LoginRoute with full back stack clear

## Task Commits

Each task was committed atomically:

1. **Task 1: Extend token lifetimes and add session expiry signaling** - `495a0a2` (feat)
2. **Task 2: Observe session expiry in AppNavHost and force navigate to login** - `549565d` (feat)

## Files Created/Modified
- `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt` - Updated ACCESS_TOKEN_EXPIRY to 86400000L (1 day) and REFRESH_TOKEN_EXPIRY to 2592000000L (30 days)
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt` - Added sessionExpired SharedFlow, emits on both refresh failure paths
- `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt` - Added LaunchedEffect collecting sessionExpired, navigates to LoginRoute with popUpTo(0)

## Decisions Made
- SharedFlow (not StateFlow) because session expiry is a one-shot event, not persistent state
- extraBufferCapacity = 1 ensures tryEmit never drops the event even if no collector is active yet
- No toast/snackbar for "Session expired" -- no snackbar infrastructure exists in AppNavHost, and navigation to login is sufficient UX signal
- Token lifetimes remain fully configurable via JWT_ACCESS_EXPIRY and JWT_REFRESH_EXPIRY env vars

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required. Token lifetimes use updated defaults but remain overridable via environment variables.

---
*Quick Task: 25-extend-auth-token-lifetime*
*Completed: 2026-02-17*
