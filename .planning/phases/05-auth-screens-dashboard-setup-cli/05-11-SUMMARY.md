---
phase: 05-auth-screens-dashboard-setup-cli
plan: 11
subsystem: auth
tags: [remember-me, token-persistence, session-management, multiplatform-settings, navigation]

# Dependency graph
requires:
  - phase: 05-auth-screens-dashboard-setup-cli
    plan: 10
    provides: "ProfileRoute as standalone top-level route in AppNavHost"
  - phase: 03
    provides: "TokenStorage with Settings-based token persistence, AuthApi SDK"
provides:
  - "Session-only vs persistent token mode in TokenStorage"
  - "RememberMe parameter forwarded through login chain (ViewModel -> AuthApi -> TokenStorage)"
  - "Startup token check in AppNavHost with auto-navigate to DashboardRoute"
  - "Session token cleanup on app restart for non-remembered sessions"
affects: [auth, navigation, dashboard]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "LaunchedEffect(Unit) for one-time startup navigation logic"
    - "Session-only flag in Settings for cross-restart token behavior"
    - "Default parameter (rememberMe=true) for backward-compatible API extension"

key-files:
  created: []
  modified:
    - core/storage/src/commonMain/kotlin/com/m2f/template/storage/TokenStorage.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt

key-decisions:
  - "rememberMe=true default on saveTokens() preserves backward compatibility for register/refresh/OAuth flows"
  - "Session-only flag stored in Settings (not in-memory) so clearSessionTokens() works across app restarts"
  - "LaunchedEffect(Unit) for startup token check placed before OAuth callback LaunchedEffect for correct ordering"
  - "LoginRoute remains startDestination; LaunchedEffect redirects to DashboardRoute to avoid recomposition issues"

patterns-established:
  - "Startup navigation guard: LaunchedEffect(Unit) checks auth state and redirects before user sees login"
  - "Session-only persistence: Settings boolean flag controls whether tokens survive app restart"

# Metrics
duration: 2min
completed: 2026-02-13
---

# Phase 5 Plan 11: Remember-Me Summary

**End-to-end remember-me wiring: session-only TokenStorage mode, rememberMe forwarded through login chain, startup token check with auto-navigation to DashboardRoute**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-13T21:40:48Z
- **Completed:** 2026-02-13T21:42:59Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- TokenStorage now supports session-only mode via KEY_SESSION_ONLY settings flag with clearSessionTokens() and isSessionOnly()
- rememberMe parameter forwarded from LoginViewModel through AuthApi.login() to TokenStorage.saveTokens()
- AppNavHost checks for existing tokens on startup and auto-navigates to DashboardRoute, skipping LoginRoute
- Session-only tokens (rememberMe=false) are cleared on next app startup before token check

## Task Commits

Each task was committed atomically:

1. **Task 1: Add session-only mode to TokenStorage and wire rememberMe through AuthApi and LoginViewModel** - `4597ca0` (feat)
2. **Task 2: Add startup token check in AppNavHost to auto-navigate to DashboardRoute** - `9dba5b9` (feat)

## Files Created/Modified
- `core/storage/src/commonMain/kotlin/com/m2f/template/storage/TokenStorage.kt` - Added sessionOnly field, KEY_SESSION_ONLY, clearSessionTokens(), isSessionOnly(), rememberMe param on saveTokens()
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt` - Added rememberMe: Boolean param to login(), forwarded to saveTokens()
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt` - Passes current.rememberMe to authApi.login()
- `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt` - Added LaunchedEffect for startup token check and session cleanup

## Decisions Made
- rememberMe=true default on saveTokens() preserves backward compatibility for register/refresh/OAuth flows that always persist
- Session-only flag stored in Settings (not in-memory) so clearSessionTokens() works across app restarts
- LaunchedEffect(Unit) for startup token check placed before OAuth callback LaunchedEffect for correct ordering
- LoginRoute remains startDestination; LaunchedEffect redirects to DashboardRoute to avoid recomposition issues

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 5 (Auth Screens, Dashboard, Setup CLI) is now fully complete with all 11 plans executed
- All gap closure plans (10, 11) have been completed
- Remember-me feature works end-to-end across the full login chain
- Ready to proceed to Phase 6 (AI Agents) or project milestone review

## Self-Check: PASSED

All files verified present. All commits verified in git log.

---
*Phase: 05-auth-screens-dashboard-setup-cli*
*Completed: 2026-02-13*
