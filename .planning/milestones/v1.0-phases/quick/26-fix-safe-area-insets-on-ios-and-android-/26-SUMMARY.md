---
phase: quick-26
plan: 01
subsystem: ui
tags: [compose-multiplatform, safe-area, system-bars, ios, android, window-insets]

# Dependency graph
requires:
  - phase: 04-01
    provides: AppNavHost with NavHost navigation graph
provides:
  - Global system bar insets handling via systemBarsPadding on AppNavHost
  - Correct safe area rendering on iOS (status bar, home indicator) and Android (status bar, nav bar)
affects: [navigation, login, all-screens]

# Tech tracking
tech-stack:
  added: []
  patterns: [global-system-bar-padding-at-navhost-level]

key-files:
  created: []
  modified:
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
    - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginScreen.kt

key-decisions:
  - "systemBarsPadding (not safeDrawingPadding) to avoid IME insets affecting scrolling forms"
  - "Global padding at NavHost level so all screens inherit insets automatically"
  - "Removed hardcoded 48dp top padding from LoginMobileLayout to prevent double-padding"

patterns-established:
  - "System bar insets: handled once at AppNavHost level, not per-screen"

requirements-completed: [QUICK-26]

# Metrics
duration: 2min
completed: 2026-02-17
---

# Quick 26: Fix Safe Area Insets on iOS and Android Summary

**Global systemBarsPadding on AppNavHost Box wrapping NavHost, with LoginScreen hardcoded top padding removed to prevent double-insets**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-16T23:33:11Z
- **Completed:** 2026-02-16T23:35:48Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- All screens (Login, Register, Dashboard, Profile, ForgotPassword) now render within safe area on iOS and Android
- Single-point fix at navigation level -- no per-screen changes needed except removing compensatory padding
- Desktop and web layouts completely unaffected (WindowInsets.systemBars resolves to zero on those platforms)

## Task Commits

Each task was committed atomically:

1. **Task 1: Add systemBarsPadding to AppNavHost** - `9c2a537` (feat)
2. **Task 2: Remove hardcoded top padding from LoginScreen mobile layout** - `3156a12` (fix)

## Files Created/Modified
- `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt` - Box wrapper with Modifier.fillMaxSize().systemBarsPadding() around NavHost
- `app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginScreen.kt` - LoginMobileLayout padding changed from top=48dp to uniform 24dp

## Decisions Made
- Used `systemBarsPadding()` instead of `safeDrawingPadding()` -- safeDrawing includes IME (keyboard) insets which would cause layout issues with scrolling forms on Login/Register screens
- Applied padding at the NavHost wrapper level so all screens inherit it automatically without per-screen modifications
- Removed LoginScreen's hardcoded `top = 48.dp` which was a manual status bar workaround, now redundant with global insets

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- `composeApp:compileKotlinDesktop` task does not exist in this project (no desktop/JVM target in composeApp module). Used `composeApp:compileCommonMainKotlinMetadata` instead for build verification. Both tasks compiled successfully.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Safe area insets are now handled globally for all current and future screens
- No further per-screen padding adjustments needed

---
*Quick task: 26-fix-safe-area-insets-on-ios-and-android*
*Completed: 2026-02-17*
