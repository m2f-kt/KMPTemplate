---
status: complete
phase: 05-auth-screens-dashboard-setup-cli
source: [05-01-SUMMARY.md, 05-02-SUMMARY.md, 05-03-SUMMARY.md, 05-04-SUMMARY.md, 05-05-SUMMARY.md, 05-06-SUMMARY.md, 05-07-SUMMARY.md, 05-08-SUMMARY.md, 05-09-SUMMARY.md]
started: 2026-02-13T16:30:00Z
updated: 2026-02-13T22:00:00Z
retest-started: 2026-02-13T21:00:00Z
---

## Current Test

[testing complete]

## Tests

### 1. App Launch - Login Screen
expected: App opens to the login screen with email/password inputs, Login button, Google sign-in button (Apple on iOS/WASM only), "Create Account" and "Forgot Password" links. Desktop shows brand panel on left + form on right.
result: pass

### 2. Login Field Validation
expected: Submit login with empty fields — validation error messages appear for email and password. Enter an invalid email format and submit — email-specific error shown.
result: pass

### 3. Navigate to Register Screen
expected: Tap "Create Account" on login — navigates to Register screen showing first name, last name, email, password, confirm password fields, and a terms/conditions checkbox.
result: pass

### 4. Register Accumulated Validation
expected: Submit register form with all fields empty — multiple field error messages appear simultaneously (accumulated errors, not one at a time). Each invalid field shows its own error.
result: pass

### 5. Navigate to Forgot Password
expected: From login, tap "Forgot Password" — navigates to a screen with an email input field and a submit button. After submitting an email, a success alert appears.
result: pass

### 6. Auth Screen Back Navigation
expected: Can navigate freely between Login, Register, and Forgot Password using the links/buttons and back navigation works correctly.
result: pass

### 7. Dashboard Layout (Desktop)
expected: After reaching the dashboard, desktop view shows a sidebar (260dp) with nav items (Dashboard, Processes, Logs, Deployments, Settings) and a user row at bottom. Main content area shows metric cards, process table, deployment progress bars, and activity list.
result: issue
reported: "Main content not scrollable. Sidebar nav items navigate to separate screens instead of updating main content while keeping sidebar visible. Clicking user navigates to profile with its own sidebar but no back navigation. Profile content also not scrollable."
severity: major

### 8. Dashboard Mock Data
expected: Dashboard displays specific mock metrics: 99.98% uptime, 1.2M requests, 42ms avg latency, 0.03% error rate. Process table, deployment progress bars, and recent activity list are all populated.
result: pass

### 9. Dashboard Responsive Layout
expected: Resizing the window below ~840dp switches from sidebar to bottom navigation tabs (Dashboard, Processes, Logs, Settings). Expanding back restores the sidebar.
result: pass

### 10. Dashboard Section Navigation
expected: Clicking sidebar nav items (or bottom tabs on mobile) navigates to placeholder screens for Processes, Logs, Deployments, Settings. Each shows a terminal-styled placeholder card.
result: issue
reported: "Placeholder screens show correctly but sidebar nav navigates to a totally new screen instead of replacing just the main content section while keeping the sidebar"
severity: major

### 11. Profile Screen
expected: Navigate to profile (via user row in sidebar or settings). Profile shows user name, email, tier badge, and tier-specific content section with cards/tables appropriate to the user's tier.
result: pass

### 12. Edit Profile
expected: Tapping an edit button on the profile enters edit mode with editable name and email fields. "Save" persists changes, "Cancel" exits edit mode without saving.
result: pass

### 13. Logout Flow
expected: Tapping logout returns to the login screen. Pressing back does NOT return to the dashboard (back stack is cleared).
result: pass

### 14. Setup CLI
expected: Running `bash setup.sh` from the project root prompts for project name, package name, and database name. Shows a preview of changes, asks for confirmation, then renames packages, moves source directories, and updates configs. Verification step at the end confirms no old references remain.
result: issue
reported: "Some references and packages weren't changed. For example TerminalTypography and other references remained with old package names."
severity: major

## Re-tests (Gap Closure Verification)

### R1. Registration Flow (was blocker)
expected: Register with a new email succeeds — auth tokens returned, land on dashboard. No "user already exists" error for fresh emails.
result: pass
original-test: 7 (partial — registration bug)
fix: AuthService.kt line 84 — ensure(findByEmail == null) replacing ensureNotNull

### R2. Dashboard Sidebar Navigation (was major)
expected: Clicking sidebar nav items (Dashboard, Processes, Logs, Deployments, Settings) swaps the main content area while the sidebar stays visible and persistent. Content does not scroll away the sidebar.
result: issue
reported: "the profile loads in the content window instead of in a new page which is creating a very weird effect (profile has its own sidebar nested inside dashboard content area, double sidebar visible). Login session is not persisted across app reinitialization even when the checkbox for remember me is checked."
severity: major
original-test: 7, 10
fix: 05-08 — State-based content switching via selectedNavItem when() block

### R3. Profile in Dashboard Shell (was major)
expected: Clicking user row in sidebar opens profile embedded inside the dashboard shell (sidebar still visible on desktop). A back button returns to the previous dashboard section. On mobile, profile hides bottom nav and shows a back header.
result: issue
reported: "Profile screen should be a top-level screen, not embedded inside the dashboard content area. Currently displays nested inside dashboard creating double sidebar."
severity: major
original-test: 7
fix: 05-08 — Profile injected as composable slot, back button added

### R4. Setup CLI Module Discovery (was major)
expected: Running `bash setup.sh` renames ALL modules including app/profile. After running, no old package references remain (including in previously missed modules). The script discovers modules dynamically without hardcoded lists.
result: skipped
reason: Still broken. Deferred to future phase per user decision.
original-test: 14
fix: 05-09 — Dynamic find-based module discovery replacing hardcoded loops

## Summary

total: 14 (+4 retests)
passed: 10
issues: 4
pending: 0
skipped: 0
retests-passed: 1
retests-issues: 2
retests-skipped: 1

## Gaps

- truth: "Registration creates a new user and returns auth tokens"
  status: failed
  reason: "User reported: Registration always returns 'A user with this email already exists' no matter which email is used"
  severity: blocker
  test: 7
  root_cause: "Inverted logic in AuthService.kt:84 — ensureNotNull(findByEmail) raises when null (email NOT found), should use ensure(findByEmail == null)"
  artifacts:
    - path: "server/auth/src/main/kotlin/com/m2f/server/auth/service/AuthService.kt"
      issue: "ensureNotNull used instead of ensure for duplicate email check"
  missing:
    - "Change ensureNotNull(userRepository.findByEmail(validEmail)) to ensure(userRepository.findByEmail(validEmail) == null)"
  debug_session: ""

- truth: "Dashboard sidebar nav updates main content area while keeping sidebar visible; content is scrollable"
  status: failed
  reason: "User reported: Main content not scrollable. Sidebar nav navigates to separate screens instead of updating main content. Profile has own sidebar but no back nav. Profile also not scrollable."
  severity: major
  test: 7
  root_cause: "Flat navigation graph — all dashboard sub-screens (Processes, Logs, Deployments, Settings) are top-level NavHost destinations. DashboardSidebar callbacks trigger root navController.navigate() which replaces the entire DashboardScreen including sidebar. Profile is also a top-level route with no back button on desktop."
  artifacts:
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"
      issue: "Sidebar callbacks wired to root navController; sub-routes as top-level destinations"
    - path: "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt"
      issue: "onNavItemSelected immediately triggers top-level navigation instead of content swap"
    - path: "app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt"
      issue: "DesktopProfile missing onBack callback — no back button in desktop layout"
  missing:
    - "Use nested NavHost inside DashboardScreen content area OR state-based content switching"
    - "Move Processes/Logs/Deployments/Settings out of top-level nav into dashboard-scoped content"
    - "Add back navigation to DesktopProfile or embed profile within dashboard shell"
    - "Ensure content areas have verticalScroll on all sub-screens"
  debug_session: ".planning/debug/dashboard-sidebar-nav.md"

- truth: "Sidebar section navigation updates main content while keeping sidebar visible"
  status: failed
  reason: "User reported: Sidebar nav navigates to a totally new screen instead of replacing just the main content section while keeping the sidebar"
  severity: major
  test: 10
  root_cause: "Same root cause as test 7 — flat navigation graph with top-level routes for dashboard sub-sections"
  artifacts:
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"
      issue: "ProcessesRoute, LogsRoute, DeploymentsRoute, SettingsRoute are sibling top-level destinations"
  missing:
    - "See test 7 fix — same architectural change needed"
  debug_session: ".planning/debug/dashboard-sidebar-nav.md"

- truth: "Setup CLI renames all package references, source directories, and configs after cloning"
  status: failed
  reason: "User reported: Some references and packages weren't changed. For example TerminalTypography and other references remained with old package names."
  severity: major
  test: 14
  root_cause: "app/profile module missing from hardcoded module list at setup.sh line 204. Script has 'for mod in auth dashboard designsystem' but project has 4 app modules (profile missing). Text replacement via recursive find works, but directory moves skip app/profile entirely."
  artifacts:
    - path: "setup.sh"
      issue: "Line 204 hardcodes 3 app modules, missing app/profile. Brittle architecture requires manual update for every new module."
  missing:
    - "Add 'profile' to app module list at line 204"
    - "Consider dynamic module discovery (find app/*/build.gradle.kts) instead of hardcoded lists"
  debug_session: ".planning/debug/setup-sh-missing-pkg-refs.md"

- truth: "Dashboard sidebar nav swaps content while sidebar stays persistent; profile does not create nested sidebar"
  status: failed
  reason: "User reported: profile loads in content window with its own sidebar nested inside dashboard content area, creating double sidebar. Login session not persisted across app restart even with remember me checked."
  severity: major
  test: R2
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""

- truth: "Profile opens as top-level screen, not nested inside dashboard content"
  status: failed
  reason: "User reported: Profile should be top-level route. Currently embedded inside dashboard content area creating double sidebar with profile's own sub-nav."
  severity: major
  test: R3
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
