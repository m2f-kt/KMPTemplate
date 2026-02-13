---
status: complete
phase: 05-auth-screens-dashboard-setup-cli
source: [05-01-SUMMARY.md, 05-02-SUMMARY.md, 05-03-SUMMARY.md, 05-04-SUMMARY.md, 05-05-SUMMARY.md, 05-06-SUMMARY.md, 05-07-SUMMARY.md]
started: 2026-02-13T16:30:00Z
updated: 2026-02-13T16:55:00Z
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

## Summary

total: 14
passed: 10
issues: 4
pending: 0
skipped: 0

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
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""

- truth: "Sidebar section navigation updates main content while keeping sidebar visible"
  status: failed
  reason: "User reported: Sidebar nav navigates to a totally new screen instead of replacing just the main content section while keeping the sidebar"
  severity: major
  test: 10
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""

- truth: "Setup CLI renames all package references, source directories, and configs after cloning"
  status: failed
  reason: "User reported: Some references and packages weren't changed. For example TerminalTypography and other references remained with old package names."
  severity: major
  test: 14
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
