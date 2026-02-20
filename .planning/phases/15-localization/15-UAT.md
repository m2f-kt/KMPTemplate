---
status: diagnosed
phase: 15-localization
source: 15-01-SUMMARY.md, 15-02-SUMMARY.md, 15-03-SUMMARY.md, 15-04-SUMMARY.md, 15-05-SUMMARY.md, 15-06-SUMMARY.md, 15-07-SUMMARY.md
started: 2026-02-20T10:00:00Z
updated: 2026-02-20T10:15:00Z
---

## Current Test

number: complete
name: All tests completed
awaiting: diagnosis

## Tests

### 1. Auth Screens Display Localized UI Text (English)
expected: Run the app. Login, Register, and Forgot Password screens show proper English labels, buttons, hints, and links — no raw codes or placeholder text visible.
result: pass

### 2. Auth Screen Error Messages Are Localized
expected: On the Login screen, submit invalid credentials. The error message should display a human-readable English string (e.g., "Invalid email or password"), not a raw code like "AUTH_INVALID_CREDENTIALS". Same for Register validation errors (blank email, short password, etc.).
result: pass

### 3. Profile Screen Displays Localized UI Text
expected: Log in and navigate to Profile. All text — back button, section headers, edit/save/cancel buttons, input placeholders — should display proper English. No hardcoded terminal codes like "< back" or "> edit profile" should appear as raw strings.
result: issue
reported: "not everything is translated, the lateral menu is not translated — sidebar shows English labels (user directory, admin identity, access matrix, platform analytics, monitoring, alerts, system status, configuration, maintenance, system logs, danger zone). Dashboard content cards have untranslated section headers (platform_stats, user_directory, admin_identity, access_matrix, system_status, danger_zone, destructive_actions). The profile section itself is partially translated (Spanish locale active — shows '< atras', 'editar perfil', 'Idioma: > Espanol') but sidebar and dashboard content sections remain in English/raw keys."
severity: major

### 4. Profile Screen Error Messages Are Localized
expected: On the Profile screen, trigger a validation error (e.g., clear your name and save). The error should display a human-readable localized message, not a raw StringKey code like "VALIDATION_NAME_BLANK".
result: pass

### 5. Dashboard Screen Displays Localized UI Text
expected: Navigate to the Dashboard. System overview card, node counts, deployment info, table headers, sidebar nav labels, brand text, and logout button should all display proper English text from string resources.
result: issue
reported: "only some values are translated (using Spanish as default and everything should appear in Spanish but it's not the case). Sidebar nav labels ARE translated (panel, procesos, registros, despliegues, ajustes). But: page title '$ system_overview' is a raw key, stat card headers are English (UPTIME, REQUESTS, AVG LATENCY, ERROR RATE), section headers use underscore-style keys (procesos_activos, estado_despliegue, actividad_reciente), deployment pipeline labels (Build, Tests, Deploy) are English, activity items (deploy_v2.4.1, high_memory_alert, ssl_cert_renewed, auto_scaling up) are untranslated."
severity: major

### 6. Admin Panel Displays Localized UI Text
expected: Log in as an admin user. Navigate to the Admin Panel. Group info header, member list labels, "Register Member" button, loading indicators, and error badges should all display proper English text. No raw codes visible.
result: issue
reported: "Whether logging in as admin or power admin, most menu items are not translated and most of the main content is not translated either. The Admin Panel has widespread untranslated strings across navigation and content areas."
severity: major

### 7. Register Member Screen Displays Localized UI Text and Errors
expected: From Admin Panel, tap "Register Member". All form labels (first name, last name, email, password, role selector) and buttons should display proper English. Submit with empty fields — validation errors should show human-readable messages, not raw StringKey codes.
result: skip
reported: "User unable to find or access the Register Member section from the Admin Panel."

### 8. Locale Switch to Spanish
expected: Navigate to Profile. Find the locale/language selector dropdown. Switch from English to Espanol. All UI strings on the Profile screen should immediately update to Spanish. Navigate back to Dashboard — sidebar labels, dashboard content should be in Spanish. Check Login screen (log out first) — all auth text in Spanish.
result: issue
reported: "No automatic/immediate update on locale switch — Profile screen does not refresh. Update only happens when navigating back to Dashboard. On Dashboard, only menu items are in Spanish but content is mixed between Spanish and English. After logging out, everything is in English, not Spanish — locale change does not persist to auth screens."
severity: major

### 9. Locale Persists Across App Restart
expected: With Spanish selected, close and relaunch the app. The app should start in Spanish — the Login screen (or Dashboard if session persists) should show Spanish text without needing to re-select the locale.
result: pass

### 10. Server Returns Localized Error Messages
expected: With the locale set to Spanish (or by sending Accept-Language: es header), trigger a server error (e.g., invalid login). The error message returned from the server should be in Spanish (e.g., "El correo o la contrasena son incorrectos" instead of English).
result: issue
reported: "Multiple platform issues: (1) WASM target fails to compile — AppLocale.wasmJs.kt:16:23 error 'Calls to js(code) must be a single expression inside a top-level function body or a property initializer in Kotlin/Wasm'. (2) Android target cannot connect — running HTTP against localhost and cleartext traffic policy blocks it. (3) On working platforms (desktop/iOS), backend error messages always return in English — the client is not sending the language code via HTTP requests (e.g., Accept-Language header), and the backend has no way to determine user language for non-authenticated requests. For authenticated requests the backend could read user preference, but for unauthenticated requests (like login errors) the language must be sent via HTTP header."
severity: major

## Summary

total: 10
passed: 4
issues: 5
pending: 0
skipped: 1

## Gaps

- truth: "Profile screen displays fully localized UI text — all labels, headers, sidebar nav items from string resources"
  status: failed
  reason: "User reported: sidebar navigation not translated (user directory, admin identity, access matrix, etc. remain English). Dashboard content section headers untranslated (platform_stats, user_directory, admin_identity, access_matrix, system_status, danger_zone, destructive_actions). Profile section partially translated when in Spanish but sidebar and dashboard content cards are not localized."
  severity: major
  test: 3
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""

- truth: "Admin Panel displays fully localized UI text — menu items, group info, member list labels, buttons, and content areas all from string resources"
  status: failed
  reason: "User reported: whether logging in as admin or power admin, most menu items are not translated and most of the main content is not translated either. Widespread untranslated strings across Admin Panel navigation and content areas."
  severity: major
  test: 6
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""

- truth: "Server returns localized error messages when client sends locale preference"
  status: failed
  reason: "User reported: (1) WASM target fails to compile — AppLocale.wasmJs.kt:16:23 'js(code) must be single expression in top-level function body' error. (2) Android target blocked by cleartext traffic policy when connecting to localhost HTTP. (3) On working platforms (desktop/iOS), backend always returns English error messages — client never sends Accept-Language header with HTTP requests, and backend has no mechanism to determine user locale for non-authenticated requests. Authenticated requests could read user preference, but unauthenticated requests (login errors) require Accept-Language header."
  severity: major
  test: 10
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""

- truth: "Switching locale to Spanish immediately updates all UI strings across all screens including auth screens"
  status: failed
  reason: "User reported: (1) No immediate update on Profile screen when switching locale — requires navigation to take effect. (2) Dashboard content is mixed Spanish/English after switch — only menu items translate, content remains partially English. (3) After logging out, auth screens revert to English instead of staying in Spanish. Locale switch is incomplete and not reactive."
  severity: major
  test: 8
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
- truth: "Dashboard screen displays fully localized UI text — all stat cards, section headers, table headers, deployment labels from string resources"
  status: failed
  reason: "User reported: only some values translated with Spanish default. Sidebar nav labels translated (panel, procesos, registros, despliegues, ajustes). But page title '$ system_overview' is raw key, stat card headers English (UPTIME, REQUESTS, AVG LATENCY, ERROR RATE), section headers use underscore keys (procesos_activos, estado_despliegue, actividad_reciente), deployment pipeline labels (Build, Tests, Deploy) English, activity items untranslated."
  severity: major
  test: 5
  root_cause: ""
  artifacts: []
  missing: []
  debug_session: ""
