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
  status: diagnosed
  reason: "User reported: sidebar navigation not translated (user directory, admin identity, access matrix, etc. remain English). Dashboard content section headers untranslated (platform_stats, user_directory, admin_identity, access_matrix, system_status, danger_zone, destructive_actions). Profile section partially translated when in Spanish but sidebar and dashboard content cards are not localized."
  severity: major
  test: 3
  root_cause: "ProfileSidebar.kt has zero stringResource() calls — all nav items (per tier), brand text, logout text, and footer content are hardcoded English strings. All 5 TierContent files (FreeTierContent.kt, PaidTierContent.kt, PremiumTierContent.kt, AdminTierContent.kt, PowerAdminTierContent.kt) have 100% hardcoded strings with zero stringResource() calls."
  artifacts:
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileSidebar.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/FreeTierContent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/PaidTierContent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/PremiumTierContent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/AdminTierContent.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/tier/PowerAdminTierContent.kt
    - app/profile/src/commonMain/composeResources/values/strings.xml
    - app/profile/src/commonMain/composeResources/values-es/strings.xml
  missing:
    - "Add string resource keys for all ProfileSidebar nav items, brand, logout, footer per tier"
    - "Add string resource keys for all TierContent files (Free, Paid, Premium, Admin, PowerAdmin)"
    - "Replace all hardcoded strings in ProfileSidebar.kt with stringResource() calls"
    - "Replace all hardcoded strings in all 5 TierContent files with stringResource() calls"
    - "Add Spanish translations for all new keys in values-es/strings.xml"
  debug_session: ".planning/debug/untranslated-ui-strings.md"

- truth: "Dashboard screen displays fully localized UI text — all stat cards, section headers, table headers, deployment labels from string resources"
  status: diagnosed
  reason: "User reported: only some values translated with Spanish default. Sidebar nav labels translated (panel, procesos, registros, despliegues, ajustes). But page title '$ system_overview' is raw key, stat card headers English (UPTIME, REQUESTS, AVG LATENCY, ERROR RATE), section headers use underscore keys (procesos_activos, estado_despliegue, actividad_reciente), deployment pipeline labels (Build, Tests, Deploy) English, activity items untranslated."
  severity: major
  test: 5
  root_cause: "THREE issues: (1) Dashboard strings.xml values are raw keys not translations (EN: '$ system_overview' instead of 'System Overview', ES: 'procesos_activos' instead of 'Procesos activos'). (2) DashboardMockData.kt embeds English labels in data classes (UPTIME, REQUESTS, AVG LATENCY, ERROR RATE, activity titles) — MetricCard renders these directly without stringResource(). (3) DashboardBottomNav.kt has zero stringResource() calls — all 5 tab labels hardcoded."
  artifacts:
    - app/dashboard/src/commonMain/composeResources/values/strings.xml
    - app/dashboard/src/commonMain/composeResources/values-es/strings.xml
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardMockData.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardBottomNav.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt
  missing:
    - "Fix all EN strings.xml values to be human-readable English (e.g. 'System Overview' not '$ system_overview')"
    - "Fix all ES strings.xml values to be proper Spanish translations (e.g. 'Resumen del sistema' not 'procesos_activos')"
    - "Refactor DashboardMockData.kt to use string resource keys instead of hardcoded English labels"
    - "Update MetricCard, ActivityList composables to resolve labels via stringResource()"
    - "Replace hardcoded labels in DashboardBottomNav.kt with stringResource() calls"
  debug_session: ".planning/debug/untranslated-ui-strings.md"

- truth: "Admin Panel displays fully localized UI text — menu items, group info, member list labels, buttons, and content areas all from string resources"
  status: diagnosed
  reason: "User reported: whether logging in as admin or power admin, most menu items are not translated and most of the main content is not translated either. Widespread untranslated strings across Admin Panel navigation and content areas."
  severity: major
  test: 6
  root_cause: "Admin strings.xml values are raw keys not translations (EN: '> admin_panel' instead of 'Admin Panel', 'load_more' instead of 'Load More', '+ register_member' instead of 'Register Member'). Table headers are English-only abbreviations. AdminPanelScreen.kt does use stringResource() but the underlying values are wrong."
  artifacts:
    - app/admin/src/commonMain/composeResources/values/strings.xml
    - app/admin/src/commonMain/composeResources/values-es/strings.xml
  missing:
    - "Fix all EN admin strings.xml values to be human-readable English"
    - "Fix all ES admin strings.xml values to be proper Spanish translations"
  debug_session: ".planning/debug/untranslated-ui-strings.md"

- truth: "Switching locale to Spanish immediately updates all UI strings across all screens including auth screens"
  status: diagnosed
  reason: "User reported: (1) No immediate update on Profile screen when switching locale — requires navigation to take effect. (2) Dashboard content is mixed Spanish/English after switch — only menu items translate, content remains partially English. (3) After logging out, auth screens revert to English instead of staying in Spanish. Locale switch is incomplete and not reactive."
  severity: major
  test: 8
  root_cause: "setAppLocale() calls Locale.setDefault() (platform-level static operation) which Compose cannot observe — no State/StateFlow/CompositionLocal changes, so no recomposition triggered. PreferencesStorage.observeLanguage() Flow exists but is never collected. Auth module has NO Spanish strings.xml (values-es/) — was the first module localized (Plan 02) and Spanish translations were never added. ProfileRoute locale state is local mutableStateOf, not propagated to app tree."
  artifacts:
    - composeApp/src/commonMain/kotlin/com/m2f/template/App.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
    - app/auth/src/commonMain/composeResources/values/strings.xml
  missing:
    - "Create LocalAppLocale CompositionLocal for reactive locale propagation"
    - "In App.kt collect observeLanguage() as Compose state, provide via CompositionLocalProvider, key() content tree on locale"
    - "Simplify ProfileRoute locale handling — remove local mutableStateOf, use CompositionLocal + write-only callback"
    - "Create app/auth/src/commonMain/composeResources/values-es/strings.xml with Spanish translations for all auth strings"
  debug_session: ".planning/debug/locale-not-reactive.md"

- truth: "Server returns localized error messages when client sends locale preference"
  status: diagnosed
  reason: "User reported: (1) WASM target fails to compile — AppLocale.wasmJs.kt:16:23 'js(code) must be single expression in top-level function body' error. (2) Android target blocked by cleartext traffic policy when connecting to localhost HTTP. (3) On working platforms (desktop/iOS), backend always returns English error messages — client never sends Accept-Language header with HTTP requests, and backend has no mechanism to determine user locale for non-authenticated requests. Authenticated requests could read user preference, but unauthenticated requests (login errors) require Accept-Language header."
  severity: major
  test: 10
  root_cause: "THREE issues: (1) WASM: js('navigator.language') is nested inside elvis expression — Kotlin/Wasm requires js() to be entire body of top-level function or property initializer. (2) Android: no usesCleartextTraffic or network_security_config.xml — Android 9+ blocks HTTP by default. (3) Server localization: ApiClient.kt never sets Accept-Language header in defaultRequest; server CORS config doesn't allow Accept-Language header. Server-side localization IS fully implemented (ServerStrings.resolve works) but client never sends locale."
  artifacts:
    - composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt
    - composeApp/src/androidMain/AndroidManifest.xml
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt
    - server/src/main/kotlin/com/m2f/template/Application.kt
  missing:
    - "Extract js() call to separate top-level function in AppLocale.wasmJs.kt"
    - "Add network_security_config.xml allowing cleartext for localhost/10.0.2.2 and reference in AndroidManifest.xml"
    - "Add Accept-Language header to ApiClient.kt defaultRequest via locale provider parameter"
    - "Wire locale provider in DI (SdkModule.kt)"
    - "Add allowHeader(HttpHeaders.AcceptLanguage) to CORS config in Application.kt"
  debug_session: ".planning/debug/wasm-cleartext-server-locale.md"
