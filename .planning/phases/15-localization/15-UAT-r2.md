---
status: diagnosed
phase: 15-localization
source: 15-08-SUMMARY.md, 15-09-SUMMARY.md, 15-10-SUMMARY.md, 15-11-SUMMARY.md
started: 2026-02-21T10:00:00Z
updated: 2026-02-21T10:30:00Z
round: 2
previous: 15-UAT.md (5 issues → 4 gap closure plans → executed)
---

## Current Test

[testing complete]

## Tests

### 1. English UI text — no raw keys visible
expected: Every screen (Dashboard, Profile sidebar + tier content, Admin Panel) displays human-readable English text. No raw underscore-style keys like "system_overview", "platform_stats", "admin_panel" appear as visible text anywhere.
result: pass

### 2. Spanish translations display correctly
expected: When locale is set to Spanish, every screen displays proper Spanish translations. Dashboard shows "Resumen del sistema", "Procesos activos", "Estado de despliegue". Profile sidebar shows Spanish nav labels. Admin Panel shows "Panel de Administración", "Registrar miembro". No English text remains.
result: pass

### 3. Dashboard fully localized (stat cards, tables, mock data)
expected: Dashboard displays localized text for: page title ("System Overview" / "Resumen del sistema"), stat card headers (Uptime, Requests, Avg Latency, Error Rate), section headers, deployment pipeline labels, activity items, bottom nav tabs, and sidebar nav labels — all from stringResource().
result: pass

### 4. Profile sidebar and tier content localized
expected: Profile sidebar nav items (User Directory, Admin Identity, Access Matrix, etc.), brand text, logout button, and footer text display in the current locale. All 5 tier content files (Free/Paid/Premium/Admin/PowerAdmin) show localized section titles, table headers, and stat labels.
result: pass

### 5. Admin Panel strings display properly
expected: Admin Panel shows human-readable text: "Admin Panel" header, "Register Member" button, "Load More", table headers (Name, Email, Role, Joined), member count badge — all in the current locale language. No raw key strings with underscores or "> " prefixes.
result: issue
reported: "I'm not able to see Admin Panel, I just see the profile and a different profile depending of the type of user but neither using admin or poweradmin i see admin panel"
severity: major

### 6. Locale switch updates UI immediately (reactive)
expected: Changing the language in the Profile locale selector causes all visible UI text to switch to the selected language immediately — without restarting the app or navigating away. Navigate to Dashboard after switching — all content is in the new language. Log out — auth screens display in the selected language.
result: pass

### 7. Auth screens display in Spanish after locale switch
expected: After switching to Spanish and logging out, Login screen shows Spanish text ("Iniciar sesión", "Correo electrónico", "Contraseña"). Register and Forgot Password screens also show Spanish translations.
result: pass

### 8. Server returns localized errors via Accept-Language
expected: With locale set to Spanish, trigger a server error (e.g., invalid login credentials). The error message returned from the server should be in Spanish. The client automatically sends Accept-Language: es with every HTTP request.
result: pass

### 9. WASM target compiles successfully
expected: The WASM/JS target compiles without errors. The previous js() expression error in AppLocale.wasmJs.kt is fixed — js() is now in a top-level function body.
result: issue
reported: "fail, I still see the same error, nothing changed, error: e: file:///Users/marc/IdeaProjects/Template/composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt:15:41 Calls to 'js(code)' must be a single expression inside a top-level function body or a property initializer in Kotlin/Wasm."
severity: blocker

### 10. Android connects to local backend
expected: Android app can make HTTP requests to a localhost backend during development. A network_security_config.xml permits cleartext traffic to localhost and 10.0.2.2 (Android emulator).
result: pass

## Summary

total: 10
passed: 8
issues: 2
pending: 0
skipped: 0

## Gaps

- truth: "Admin Panel is accessible and displays fully localized UI text for admin/poweradmin users"
  status: diagnosed
  reason: "User reported: I'm not able to see Admin Panel, I just see the profile and a different profile depending of the type of user but neither using admin or poweradmin i see admin panel"
  severity: major
  test: 5
  root_cause: "DashboardViewModel determines admin panel visibility exclusively from group-level memberships (GroupRole), not system-level roles (UserRole). A system Admin/PowerAdmin who hasn't been added to any group gets an empty membership list → isAdmin stays false → admin nav item never renders. Even if forced true, AdminPanelClicked guards on groupId != null which is null without group membership."
  artifacts:
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardViewModel.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardModel.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardSidebar.kt
    - app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardBottomNav.kt
  missing:
    - "Add dual-role check: also load user profile to check UserRole.Admin/PowerAdmin for admin panel visibility"
    - "Fix AdminPanelClicked null-guard — provide fallback when groupId is null (e.g., group selector or system admin panel)"
  debug_session: ".planning/debug/admin-panel-not-visible.md"

- truth: "WASM target compiles without js() expression error"
  status: diagnosed
  reason: "User reported: same error as before — AppLocale.wasmJs.kt:15:41 Calls to 'js(code)' must be a single expression inside a top-level function body or a property initializer in Kotlin/Wasm"
  severity: blocker
  test: 9
  root_cause: "The 15-11 fix extracted js() from the elvis expression into browserLanguage() but still chains .toString().take(2) on the js() call. In Kotlin/Wasm 2.3.10, js() must be the ENTIRE and SOLE expression of a top-level function body — no method chaining allowed. Current code: private fun browserLanguage(): String = js(\"navigator.language\").toString().take(2)"
  artifacts:
    - composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt
  missing:
    - "Extract js() to its own function returning JsString: private fun navigatorLanguage(): JsString = js(\"navigator.language\")"
    - "Have browserLanguage() call navigatorLanguage().toString().take(2) separately"
  debug_session: ".planning/debug/wasm-js-call-compile-error.md"
