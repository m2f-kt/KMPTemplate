---
status: diagnosed
phase: 12-viewmodel-migration
source: [12-01-SUMMARY.md, 12-02-SUMMARY.md, 12-03-SUMMARY.md, 12-04-SUMMARY.md, 12-05-SUMMARY.md, 12-06-SUMMARY.md, 12-07-SUMMARY.md]
started: 2026-02-18T22:45:00Z
updated: 2026-02-19T00:00:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Login success navigates to dashboard
expected: Enter valid credentials on the Login screen and tap Submit. After successful authentication, the app navigates to the Dashboard screen automatically. No boolean flag flicker — navigation should be a clean one-shot transition.
result: pass

### 2. Login error shows server error
expected: Enter invalid credentials and tap Submit. The login form displays a server error message below the form fields. The form remains on screen with fields intact (not cleared).
result: issue
reported: "Error displayed correctly on login with bad credentials, but then the web app refreshed on its own and was logged in again (after having logged out). Feels like storage was not cleaned on logout. The auto-refresh without user action felt weird."
severity: major

### 3. Register success navigates to dashboard
expected: Fill all registration fields with valid data and tap Register. After successful registration, the app navigates to the Dashboard screen. Accumulated validation (Arrow zipOrAccumulate) still shows multiple field errors simultaneously if validation fails.
result: pass

### 4. Forgot password shows email sent confirmation
expected: Enter a valid email on the Forgot Password screen and tap Submit. The screen shows a success message confirming the email was sent. User stays on the Forgot Password screen (no navigation away).
result: pass

### 5. Profile loads user data on screen entry
expected: Navigate to the Profile screen. User data (name, email, tier) loads automatically without any manual action. A loading indicator may appear briefly before data is shown.
result: pass

### 6. Profile edit and save
expected: On the Profile screen, tap Edit. Name and email fields become editable. Change a value and tap Save. The profile updates and a success indicator appears. The screen exits edit mode after saving.
result: pass

### 7. Profile logout navigates to login
expected: On the Profile screen, tap Logout. The app navigates to the Login screen. Navigation is a clean one-shot transition (no double-fire on recomposition).
result: issue
reported: "Navigation to login happens but app refreshes randomly and navigates back to dashboard after successful logout. Only tested on WASM target. Feels like token is not cleaned."
severity: major

### 8. Dashboard loads with content
expected: Navigate to the Dashboard screen. A brief loading state appears, then the dashboard content populates with mock data. Bottom navigation items are selectable.
result: pass

## Summary

total: 8
passed: 6
issues: 2
pending: 0
skipped: 0

## Gaps

- truth: "App compiles and runs after MVI migration"
  status: resolved
- truth: "After logout, token storage is cleared and user stays logged out on page refresh"
  status: failed
  reason: "User reported: After logging out, login error displays correctly but web app auto-refreshed and was logged back in. Storage not cleaned on logout."
  severity: major
  test: 2
  root_cause: "Dashboard logout callback (AppNavHost lines 177-181) navigates to LoginRoute WITHOUT calling sdk.logout(), so tokens remain in WASM localStorage. AppNavHost LaunchedEffect (lines 50-62) detects tokens on recomposition and auto-navigates back to Dashboard."
  artifacts:
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"
      issue: "Dashboard onLogout callback doesn't call sdk.logout() — just navigates directly"
  missing:
    - "Dashboard logout must call sdk.logout() to clear tokens before navigating"
  debug_session: ""
- truth: "After logout, app stays on login screen and does not auto-navigate back to dashboard"
  status: failed
  reason: "User reported: Navigation to login happens but app refreshes randomly and navigates back to dashboard after successful logout. Only WASM target. Token possibly not cleaned."
  severity: major
  test: 7
  root_cause: "Same root cause as test 2 — Dashboard logout path bypasses sdk.logout(). Profile logout path (via ProfileViewModel) IS correct but if user logged out from Dashboard previously, tokens persist."
  artifacts:
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"
      issue: "Dashboard onLogout skips SDK logout; AppNavHost auto-login check finds stale tokens"
  missing:
    - "Consistent logout flow: all logout paths must call sdk.logout() before navigation"
  debug_session: ""
  reason: "User reported: App can't run — composeApp build fails because core:mvi is missing from composeApp dependencies"
  severity: blocker
  test: 1
  root_cause: "composeApp/build.gradle.kts missing implementation(projects.core.mvi) — ViewModels extend MviViewModel but Koin viewModelOf needs to see full ViewModel inheritance chain"
  artifacts:
    - path: "composeApp/build.gradle.kts"
      issue: "Missing core:mvi dependency"
  missing:
    - "implementation(projects.core.mvi) in composeApp commonMain.dependencies"
  fix_applied: "Added implementation(projects.core.mvi) to composeApp/build.gradle.kts"
