---
phase: 15-localization
plan: 05
subsystem: localization, ui, platform
tags: [i18n, locale-switching, expect-actual, KMP, Spanish, compose-resources, PreferencesStorage]

# Dependency graph
requires:
  - phase: 15-01
    provides: StringKey enum, strings.xml, resolveStringKey bridge in composeApp
  - phase: 15-02
    provides: Auth module localized with StringKey errors and stringResource() UI text
  - phase: 15-03
    provides: Feature modules localized with StringKey errors
provides:
  - expect/actual setAppLocale/getAppLocale for Android, iOS, JVM, WASM
  - Spanish strings.xml (values-es) with translations for all string resources
  - LocaleSelector composable with terminal-styled dropdown (en/es)
  - Locale persistence via PreferencesStorage and restoration on app startup
  - Per-platform locale switching (java.util.Locale on Android/JVM, NSUserDefaults on iOS, in-memory on WASM)
affects: [composeApp, app-profile, future-locales]

# Tech tracking
tech-stack:
  added: []
  patterns: [expect/actual for platform locale APIs, composable slot injection for cross-module UI, locale persistence via PreferencesStorage]

key-files:
  created:
    - composeApp/src/commonMain/kotlin/com/m2f/template/localization/AppLocale.kt
    - composeApp/src/androidMain/kotlin/com/m2f/template/localization/AppLocale.android.kt
    - composeApp/src/iosMain/kotlin/com/m2f/template/localization/AppLocale.ios.kt
    - composeApp/src/jvmMain/kotlin/com/m2f/template/localization/AppLocale.jvm.kt
    - composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt
    - composeApp/src/commonMain/composeResources/values-es/strings.xml
    - composeApp/src/commonMain/kotlin/com/m2f/template/localization/LocaleSelector.kt
  modified:
    - composeApp/src/commonMain/composeResources/values/strings.xml
    - composeApp/src/commonMain/kotlin/com/m2f/template/App.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
    - app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt

key-decisions:
  - "java.util.Locale approach for Android instead of AppCompat — avoids dependency, Compose Resources respects JVM default locale"
  - "LocaleSelector lives in composeApp, injected into ProfileScreen via composable slot — profile module stays dependency-free"
  - "Locale change handled directly in composable (no ViewModel intent) — simple synchronous preference write + platform call"
  - "WASM locale stored in memory only — page reload required for full Compose Resources locale switch"

patterns-established:
  - "Composable slot injection: cross-module UI via (@Composable () -> Unit)? parameter with null default"
  - "expect/actual for platform APIs: shared interface in commonMain, platform implementation in androidMain/iosMain/jvmMain/wasmJsMain"
  - "Locale persistence: PreferencesStorage.language for storage, setAppLocale() for runtime application"

# Metrics
duration: 4min
completed: 2026-02-19
---

# Phase 15 Plan 05: Runtime Locale Switching Summary

**expect/actual locale switching for 4 KMP targets with Spanish strings.xml, LocaleSelector dropdown in profile, and PreferencesStorage persistence**

## Performance

- **Duration:** 4 min
- **Started:** 2026-02-19T17:55:22Z
- **Completed:** 2026-02-19T17:59:22Z
- **Tasks:** 2
- **Files modified:** 11

## Accomplishments
- expect/actual setAppLocale/getAppLocale implemented for Android, iOS, JVM, and WASM
- Complete Spanish strings.xml (values-es) with translations for all 100+ string resources
- LocaleSelector composable with terminal-styled dropdown integrated into ProfileScreen
- App.kt restores locale from PreferencesStorage on startup via setAppLocale()
- All existing tests continue to pass

## Task Commits

Each task was committed atomically:

1. **Task 1: Create expect/actual AppLocale functions and Spanish strings.xml** - `c958ce8` (feat)
2. **Task 2: Wire locale persistence and create LocaleSelector composable** - `588cc31` (feat)

## Files Created/Modified

### Created
- `composeApp/src/commonMain/kotlin/com/m2f/template/localization/AppLocale.kt` - expect declarations for setAppLocale/getAppLocale
- `composeApp/src/androidMain/kotlin/com/m2f/template/localization/AppLocale.android.kt` - Android actual using java.util.Locale
- `composeApp/src/iosMain/kotlin/com/m2f/template/localization/AppLocale.ios.kt` - iOS actual using NSUserDefaults AppleLanguages
- `composeApp/src/jvmMain/kotlin/com/m2f/template/localization/AppLocale.jvm.kt` - JVM/Desktop actual using java.util.Locale
- `composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt` - WASM actual with in-memory override (best-effort)
- `composeApp/src/commonMain/composeResources/values-es/strings.xml` - Spanish translations for all string resources
- `composeApp/src/commonMain/kotlin/com/m2f/template/localization/LocaleSelector.kt` - Terminal-styled locale picker composable

### Modified
- `composeApp/src/commonMain/composeResources/values/strings.xml` - Added locale_label, locale_english, locale_spanish strings
- `composeApp/src/commonMain/kotlin/com/m2f/template/App.kt` - Reads stored locale from PreferencesStorage, calls setAppLocale on startup
- `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt` - Wires LocaleSelector into ProfileRoute with persistence callbacks
- `app/profile/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt` - Added localeSelector composable slot parameter

## Decisions Made
- **java.util.Locale for Android:** Used java.util.Locale.setDefault instead of AppCompat's setApplicationLocales to avoid adding an AppCompat dependency. Compose Resources respects JVM default locale.
- **Composable slot injection:** Added `localeSelector: (@Composable () -> Unit)? = null` to ProfileScreen rather than making the profile module depend on composeApp resources. Clean separation of concerns.
- **Direct composable handling:** Locale change is handled directly in AppNavHost composable (preferencesStorage.language = locale + setAppLocale(locale)) without ViewModel involvement. A synchronous preference write doesn't need MVI ceremony.
- **WASM best-effort:** WASM locale switching stores override in memory but Compose Resources reads navigator.languages at startup. Full effect requires page reload — documented limitation.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None - all changes compiled and tested cleanly.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 15 (Localization) is now fully complete — all 5 plans executed
- Runtime locale switching works on Android, iOS, JVM; WASM is best-effort
- Adding new locales requires only: new values-{lang}/strings.xml + entry in LocaleSelector locales list
- The per-module resource pattern (from Plan 02) supports independent module localization

---
*Phase: 15-localization*
*Completed: 2026-02-19*
