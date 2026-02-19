---
phase: 15-localization
verified: 2026-02-19T23:45:00Z
status: gaps_found
score: 4/5 must-haves verified
must_haves:
  truths:
    - "A shared StringKey enum exists that both server and client reference for string lookup -- no hardcoded strings in ViewModels"
    - "Compose resource files (strings.xml) support locale qualifiers and load correctly on all KMP targets"
    - "Server returns localized error messages based on the Accept-Language header"
    - "User can switch locale at runtime and all UI strings update without app restart"
    - "A bridge function maps StringKey values to Compose Res.strings accessors for type-safe string resolution"
  artifacts:
    - path: "core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt"
      provides: "Shared StringKey enum bridging error codes and localization keys"
      contains: "enum class StringKey"
    - path: "composeApp/src/commonMain/composeResources/values/strings.xml"
      provides: "English locale string resources for all screens and error messages"
      contains: "<resources>"
    - path: "composeApp/src/commonMain/composeResources/values-es/strings.xml"
      provides: "Spanish locale string resources"
      contains: "<resources>"
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/localization/StringKeyResolver.kt"
      provides: "Composable bridge function mapping StringKey to Res.string accessors"
      contains: "@Composable"
    - path: "server/core/config/src/main/kotlin/com/m2f/core/config/server/localization/ServerStrings.kt"
      provides: "Server-side localized error strings with en/es translations"
      contains: "object ServerStrings"
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/localization/AppLocale.kt"
      provides: "expect declarations for platform locale switching"
      contains: "expect fun setAppLocale"
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/localization/LocaleSelector.kt"
      provides: "UI component for runtime locale selection"
      contains: "@Composable"
  key_links:
    - from: "StringKey.kt"
      to: "ViewModels"
      via: "StringKey? error fields in all MVI Models"
    - from: "ServerStrings.kt"
      to: "DomainError.respond()"
      via: "ServerStrings.resolve(error.code, locale) in all respond() methods"
    - from: "resolveStringKey()"
      to: "auth screens"
      via: "resolveStringKey(state.emailError) in LoginScreen/RegisterScreen/ForgotPasswordScreen"
    - from: "LocaleSelector"
      to: "ProfileScreen"
      via: "localeSelector composable slot parameter"
    - from: "App.kt"
      to: "PreferencesStorage"
      via: "setAppLocale(storedLocale) on startup"
gaps:
  - truth: "User can switch locale at runtime and all UI strings update without app restart"
    status: partial
    reason: "Feature module screens (Profile, AdminPanel, RegisterMember, Dashboard) still have hardcoded English UI strings for labels, titles, and buttons. Only auth screens are fully localized via stringResource(Res.string.*). Error messages use StringKey.code (raw enum name) instead of localized text in feature screens."
    artifacts:
      - path: "app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt"
        issue: "11+ hardcoded English strings: '< back', 'saved', 'error', '> edit profile', '> save', 'cancel', 'Enter your name', etc."
      - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/AdminPanelScreen.kt"
        issue: "8+ hardcoded English strings: '<-', '> admin_panel', '// loading...', 'load_more', '+ register_member', etc."
      - path: "app/admin/src/commonMain/kotlin/com/m2f/template/app/admin/RegisterMemberScreen.kt"
        issue: "Error display uses StringKey.code (raw enum name like 'VALIDATION_EMAIL_BLANK') not localized text"
      - path: "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardScreen.kt"
        issue: "10+ hardcoded English strings: '$ system_overview', '[4 nodes active]', 'active_processes', etc."
      - path: "app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/DashboardSidebar.kt"
        issue: "Hardcoded strings: 'terminal', '$ logout'"
    missing:
      - "Feature modules need compose.components.resources dependency + per-module strings.xml (like auth module pattern) OR architectural change to pass localized strings from composeApp"
      - "Feature screen error display needs resolveStringKey() bridge (currently shows raw StringKey.code)"
---

# Phase 15: Localization Verification Report

**Phase Goal:** The template demonstrates a complete localization system -- shared string keys between server and client, platform resource files, and runtime locale switching
**Verified:** 2026-02-19T23:45:00Z
**Status:** gaps_found
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | A shared StringKey enum exists that both server and client reference for string lookup -- no hardcoded strings in ViewModels | ✓ VERIFIED | StringKey.kt has 32 entries (24 error codes + 7 validation + GENERIC_ERROR). All 6 ViewModels use `StringKey?` error types. All 6 ViewModel test suites assert on StringKey values. Zero hardcoded English error strings in ViewModels. |
| 2 | Compose resource files (strings.xml) support locale qualifiers and load correctly on all KMP targets | ✓ VERIFIED | values/strings.xml (178 entries) and values-es/strings.xml (178 entries) exist with identical key coverage. Auth screens use `stringResource(Res.string.*)` which loads locale-qualified resources. |
| 3 | Server returns localized error messages based on the Accept-Language header | ✓ VERIFIED | ServerStrings.kt has en/es translations for 17 error codes. preferredLanguage() parses Accept-Language header. All 19 DomainError.respond() methods (7 DomainError.kt + 6 AuthErrors.kt + 6 GroupErrors.kt) use `ServerStrings.resolve(error.code, locale)`. |
| 4 | User can switch locale at runtime and all UI strings update without app restart | ⚠️ PARTIAL | Locale switching infrastructure is complete: expect/actual setAppLocale for 4 platforms, LocaleSelector composable in ProfileScreen, PreferencesStorage persistence, App.kt startup restoration. However, only auth screens (Login, Register, ForgotPassword) use stringResource(). Feature screens (Profile, AdminPanel, RegisterMember, Dashboard) have hardcoded English strings that won't update on locale switch. |
| 5 | A bridge function maps StringKey values to Compose Res.strings accessors for type-safe string resolution | ✓ VERIFIED | resolveStringKey() in composeApp has exhaustive when-mapping for all 32 StringKey entries → Res.string.* accessors. Auth module has its own copy. Both are @Composable and use stringResource() for locale-aware resolution. |

**Score:** 4/5 truths verified (1 partial)

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `core/models/.../StringKey.kt` | Shared StringKey enum | ✓ VERIFIED | 32 entries, @Serializable, fromCode() companion, code property |
| `composeApp/.../values/strings.xml` | English strings | ✓ VERIFIED | 178 entries covering all screens + errors |
| `composeApp/.../values-es/strings.xml` | Spanish strings | ✓ VERIFIED | 178 entries, 1:1 parity with English |
| `composeApp/.../StringKeyResolver.kt` | Bridge function | ✓ VERIFIED | Exhaustive when over 32 StringKey entries → Res.string.* |
| `server/.../ServerStrings.kt` | Server-side localization | ✓ VERIFIED | en/es maps with 17 error codes, resolve() with fallback chain |
| `server/.../Error.kt` | preferredLanguage() | ✓ VERIFIED | Parses Accept-Language header, 2-char language code extraction |
| `composeApp/.../AppLocale.kt` | expect declarations | ✓ VERIFIED | setAppLocale/getAppLocale expect functions |
| `composeApp/.../AppLocale.android.kt` | Android actual | ✓ VERIFIED | java.util.Locale.setDefault |
| `composeApp/.../AppLocale.ios.kt` | iOS actual | ✓ VERIFIED | NSUserDefaults AppleLanguages |
| `composeApp/.../AppLocale.jvm.kt` | JVM actual | ✓ VERIFIED | java.util.Locale.setDefault |
| `composeApp/.../AppLocale.wasmJs.kt` | WASM actual | ✓ VERIFIED | In-memory override (documented limitation) |
| `composeApp/.../LocaleSelector.kt` | Locale picker UI | ✓ VERIFIED | Terminal-styled dropdown, en/es options, onLocaleChanged callback |
| `app/auth/.../strings.xml` | Auth module resources | ✓ VERIFIED | Per-module string resources for auth screens |
| `app/auth/.../StringKeyResolver.kt` | Auth module bridge | ✓ VERIFIED | Local resolveStringKey for auth module |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| StringKey.kt | All ViewModels | StringKey? error types in Models | ✓ WIRED | 6/6 ViewModels import and use StringKey. All Models have StringKey? fields. |
| ServerStrings.kt | DomainError.respond() | ServerStrings.resolve() calls | ✓ WIRED | 19/19 respond() methods call ServerStrings.resolve(error.code, locale) |
| preferredLanguage() | DomainError.respond() | routingContext.preferredLanguage() | ✓ WIRED | All respond() methods extract locale before resolving |
| resolveStringKey() | Auth screens | resolveStringKey(state.error) | ✓ WIRED | LoginScreen, RegisterScreen, ForgotPasswordScreen use resolveStringKey for error display |
| LocaleSelector | ProfileScreen | localeSelector composable slot | ✓ WIRED | ProfileScreen has `localeSelector: (@Composable () -> Unit)? = null` parameter, invoked in 2 layout variants |
| AppNavHost | LocaleSelector | LocaleSelector() composable call | ✓ WIRED | AppNavHost creates LocaleSelector with preferencesStorage.language + setAppLocale callbacks |
| App.kt | PreferencesStorage | setAppLocale(storedLocale) in LaunchedEffect | ✓ WIRED | App.kt reads preferencesStorage.language and calls setAppLocale on startup |
| resolveStringKey() | Feature screens | .code fallback | ⚠️ PARTIAL | Feature screens (Profile, AdminPanel, RegisterMember) use StringKey.code instead of resolveStringKey — raw enum names displayed |

### Requirements Coverage

| Requirement | Status | Blocking Issue |
|-------------|--------|----------------|
| L10N-01: Shared StringKey bridges server and client | ✓ SATISFIED | — |
| L10N-02: Compose resource files support locale qualifiers | ✓ SATISFIED | — |
| L10N-03: Server returns localized errors via Accept-Language | ✓ SATISFIED | — |
| L10N-04: Client UI strings load from platform resource files | ⚠️ PARTIAL | Auth screens: yes. Feature screens (Profile, Admin, Dashboard): still hardcoded English |
| L10N-05: User can switch locale at runtime | ⚠️ PARTIAL | Infrastructure works but only auth screen strings actually change on locale switch |
| L10N-06: Bridge function maps StringKey to Res.strings | ✓ SATISFIED | — |

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| ProfileScreen.kt | 159,217,299,301,310,317,372,382,390,399 | Hardcoded English strings | ⚠️ Warning | Won't update on locale switch |
| AdminPanelScreen.kt | 65,69,78,93,116,155,161,169 | Hardcoded English strings | ⚠️ Warning | Won't update on locale switch |
| RegisterMemberScreen.kt | 90,101,112,123,156 | StringKey.code for error display | ⚠️ Warning | Shows raw enum names (e.g., "VALIDATION_EMAIL_BLANK") instead of localized text |
| DashboardScreen.kt | 199,265,270,298,304,417,446,508 | Hardcoded English strings | ⚠️ Warning | Won't update on locale switch |
| DashboardSidebar.kt | 91,96,171 | Hardcoded English strings | ⚠️ Warning | Won't update on locale switch |
| AppLocale.wasmJs.kt | 10 | In-memory only locale | ℹ️ Info | Page reload needed for full WASM locale switch (documented limitation) |

### Human Verification Required

### 1. Locale Switch UI Flow
**Test:** Launch app → navigate to Profile → tap locale dropdown → select Español
**Expected:** All auth screen strings (Login, Register, ForgotPassword) switch to Spanish. LocaleSelector label changes to "Idioma".
**Why human:** Runtime UI recomposition behavior can't be verified statically.

### 2. Server Accept-Language Response
**Test:** Send `Accept-Language: es` header with invalid login credentials
**Expected:** Error response message is "El correo o la contraseña son incorrectos" instead of English
**Why human:** Requires running server and making HTTP request.

### 3. Locale Persistence Across App Restart
**Test:** Select Español → kill app → relaunch
**Expected:** App starts with Spanish locale (stored in PreferencesStorage, restored in App.kt)
**Why human:** Requires app lifecycle testing on device/emulator.

### Gaps Summary

The localization infrastructure is solid and complete for its core purpose:
- **StringKey enum** properly bridges server error codes and client error display (32 entries, shared across all modules)
- **Server-side i18n** is fully wired with `ServerStrings.resolve()` in all 19 `DomainError.respond()` methods
- **Auth screens** are fully localized (stringResource + resolveStringKey for both UI text and errors)
- **Runtime locale switching** infrastructure is complete (expect/actual for 4 platforms, PreferencesStorage, LocaleSelector UI, App.kt restoration)
- **Bridge function** provides type-safe exhaustive mapping

The gap is **incomplete UI string localization in feature modules**: Profile, AdminPanel, RegisterMember, and Dashboard screens still display hardcoded English strings for UI text (labels, titles, buttons). These screens are in modules (app/profile, app/admin, app/dashboard) that cannot access composeApp's Res.string.* resources. The auth module solved this by adding per-module compose.components.resources + local strings.xml — the same pattern needs to be applied to the remaining feature modules.

Additionally, feature screen error display shows raw `StringKey.code` names (like "VALIDATION_EMAIL_BLANK") instead of human-readable localized text. This needs per-module resolveStringKey bridges.

---

_Verified: 2026-02-19T23:45:00Z_
_Verifier: Claude (gsd-verifier)_
