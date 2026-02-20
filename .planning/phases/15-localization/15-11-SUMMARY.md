---
phase: 15-localization
plan: 11
subsystem: localization
tags: [kotlin-wasm, android, ktor-cors, accept-language, cleartext-traffic, kmp]

# Dependency graph
requires:
  - phase: 15-localization
    provides: "Localization system with StringKey, ServerStrings, PreferencesStorage language"
provides:
  - "WASM target compiles with js() in top-level function body"
  - "Android cleartext HTTP to localhost/emulator for dev"
  - "Accept-Language header on all SDK HTTP requests"
  - "Server CORS allows Accept-Language header"
affects: []

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Kotlin/Wasm js() calls must be entire body of top-level function"
    - "Android network_security_config.xml for domain-specific cleartext policy"
    - "localeProvider lambda on ApiClient for runtime locale header injection"

key-files:
  created:
    - "composeApp/src/androidMain/res/xml/network_security_config.xml"
  modified:
    - "composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt"
    - "composeApp/src/androidMain/AndroidManifest.xml"
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt"
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt"
    - "server/src/main/kotlin/com/m2f/template/Application.kt"

key-decisions:
  - "PreferencesStorage.language used as locale source (already in DI graph via core:storage dependency)"
  - "localeProvider is a lambda for runtime locale changes without recreating HttpClient"

patterns-established:
  - "Kotlin/Wasm js() extraction: wrap js() in separate top-level function, call from actual function"
  - "Accept-Language header injection via lambda parameter on createApiClient"

# Metrics
duration: 2min
completed: 2026-02-20
---

# Phase 15 Plan 11: Platform Fixes Summary

**Fix WASM js() compiler error, Android cleartext traffic block, and Accept-Language header for server-side localization**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-20T16:38:11Z
- **Completed:** 2026-02-20T16:40:18Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- WASM target compiles by extracting js("navigator.language") to a top-level browserLanguage() function
- Android can reach localhost HTTP backend via network_security_config.xml with emulator aliases
- Every SDK HTTP request now sends Accept-Language header with the user's current locale preference
- Server CORS allows Accept-Language header so browser/client requests aren't blocked

## Task Commits

Each task was committed atomically:

1. **Task 1: Fix WASM js() expression and Android cleartext traffic** - `c5649b2` (fix)
2. **Task 2: Add Accept-Language header to ApiClient and allow in server CORS** - `b5a188e` (feat)

## Files Created/Modified
- `composeApp/src/wasmJsMain/kotlin/com/m2f/template/localization/AppLocale.wasmJs.kt` - Extract js() to top-level browserLanguage() function
- `composeApp/src/androidMain/AndroidManifest.xml` - Add usesCleartextTraffic + networkSecurityConfig attributes
- `composeApp/src/androidMain/res/xml/network_security_config.xml` - Cleartext allowed for localhost, 10.0.2.2, 10.0.3.2
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt` - Add localeProvider param and Accept-Language header
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt` - Wire PreferencesStorage.language as localeProvider
- `server/src/main/kotlin/com/m2f/template/Application.kt` - Allow AcceptLanguage in CORS config

## Decisions Made
- Used PreferencesStorage.language as locale source rather than named("localeProvider") lambda — simpler since core:sdk already depends on core:storage
- localeProvider is a lambda (not static value) so locale changes are reflected on each request without recreating HttpClient

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 15 gap closure plans complete — all platform issues from UAT fixed
- All 4 KMP targets should compile and connect to backend with locale-aware requests

---
*Phase: 15-localization*
*Completed: 2026-02-20*
