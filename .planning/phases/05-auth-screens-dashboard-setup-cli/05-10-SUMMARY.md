---
phase: 05-auth-screens-dashboard-setup-cli
plan: 10
subsystem: navigation
tags: [compose-navigation, profile-route, dashboard, responsive-layout]

# Dependency graph
requires:
  - phase: 05-08
    provides: Dashboard state-based content switching with sidebar/bottom nav persistence
  - phase: 05-04
    provides: DashboardScreen, DashboardSidebar, responsive layout infrastructure
provides:
  - ProfileRoute as top-level navigation destination (no double sidebar)
  - Clean DashboardScreen API with onProfileClick callback
  - DashboardState without profile embedding state
affects: [05-11, profile-navigation, dashboard-layout]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Top-level route navigation for profile instead of composable slot injection"
    - "Callback-based profile click propagation from DashboardScreen to AppNavHost"

key-files:
  created: []
  modified:
    - "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardState.kt"
    - "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt"
    - "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt"
    - "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"

key-decisions:
  - "Profile navigates via navController.navigate(ProfileRoute) instead of embedded composable slot"
  - "DashboardScreen exposes single onProfileClick callback, removing onShowProfile/onHideProfile/profileContent"

patterns-established:
  - "Top-level route for standalone screens: profile, settings, etc. should be separate routes, not embedded slots"

# Metrics
duration: 3min
completed: 2026-02-13
---

# Phase 05 Plan 10: Profile Route Restoration Summary

**Restored ProfileRoute as top-level navigation destination, eliminating double-sidebar and doubled-header bugs from embedded profile slot**

## Performance

- **Duration:** 3 min
- **Started:** 2026-02-13T21:35:15Z
- **Completed:** 2026-02-13T21:38:25Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Removed all profile embedding from DashboardState, DashboardViewModel, and DashboardScreen
- Rewired AppNavHost to navigate to standalone ProfileRoute on profile clicks
- Eliminated double-sidebar bug on desktop (DashboardSidebar + ProfileSidebar)
- Eliminated doubled back-button headers on mobile
- Bottom nav now always visible in mobile dashboard (no longer hidden during profile)

## Task Commits

Each task was committed atomically:

1. **Task 1: Remove profile embedding from DashboardScreen, DashboardViewModel, and DashboardState** - `82d5750` (refactor)
2. **Task 2: Rewire AppNavHost DashboardRoute to use navController.navigate(ProfileRoute)** - `bfee838` (feat)

## Files Created/Modified
- `app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardState.kt` - Removed showProfile field
- `app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt` - Removed showProfile()/hideProfile() functions
- `app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt` - Replaced profile embedding with onProfileClick callback
- `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt` - Rewired DashboardRoute to navigate to ProfileRoute

## Decisions Made
- Profile navigates via `navController.navigate(ProfileRoute)` instead of embedded composable slot -- eliminates cross-module rendering and double layout issues
- DashboardScreen exposes single `onProfileClick` callback, removing the `onShowProfile`/`onHideProfile`/`profileContent` trio -- cleaner API surface

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Profile routing gap fully closed
- Plan 05-11 (remember-me gap closure) is the remaining plan in phase 05
- Dashboard state-based content switching for processes/logs/deployments/settings remains functional

## Self-Check: PASSED

All 4 modified files verified on disk. Both task commits (82d5750, bfee838) verified in git log.

---
*Phase: 05-auth-screens-dashboard-setup-cli*
*Completed: 2026-02-13*
