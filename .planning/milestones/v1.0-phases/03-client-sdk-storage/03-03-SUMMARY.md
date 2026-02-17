---
phase: 03-client-sdk-storage
plan: 03
subsystem: api
tags: [ktor-client, auth-interceptor, bearer-token, 401-refresh, mutex, koin-di, kmp, either]

# Dependency graph
requires:
  - phase: 03-client-sdk-storage
    plan: 01
    provides: "HttpClient factory (createApiClient), apiCall<T> wrapper, ErrorMapper, platform engines"
  - phase: 03-client-sdk-storage
    plan: 02
    provides: "TokenStorage for auth token persistence, storageModule Koin module"
provides:
  - "AuthInterceptor with bearer token attachment and 401 refresh+retry via Mutex"
  - "AuthApi with register, login, refresh, logout (token lifecycle managed)"
  - "UserApi with getProfile, updateProfile, getUserById"
  - "SdkModule Koin module wiring HttpClient, AuthInterceptor, AuthApi, UserApi"
  - "SharedModule updated to include storageModule and sdkModule"
  - "apiCall<T> Unit return type handling for endpoints with no body"
affects: [04-screens, 05-ui-dashboard]

# Tech tracking
tech-stack:
  added: []
  patterns: [auth-interceptor-httpsend, mutex-double-check-refresh, token-lifecycle-in-api-layer, koin-module-includes]

key-files:
  created:
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/UserApi.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt
  modified:
    - core/sdk/build.gradle.kts
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt
    - shared/build.gradle.kts
    - shared/src/commonMain/kotlin/com/m2f/template/di/SharedModule.kt

key-decisions:
  - "AuthInterceptor uses URLBuilder.buildString().contains() for refresh endpoint detection (encodedPath unavailable in Ktor 3.4.0)"
  - "apiCall<T> checks T::class == Unit::class to skip body deserialization for logout endpoint"
  - "SharedModule uses Koin includes() to compose storageModule and sdkModule transitively"
  - "SdkModule uses Koin getProperty for BASE_URL with localhost default, overridable per platform"

patterns-established:
  - "Auth interceptor pattern: HttpSend plugin with Mutex double-check for concurrent refresh coordination"
  - "Token lifecycle pattern: AuthApi.login/register save tokens on success, logout always clears"
  - "DI composition pattern: SharedModule includes storageModule + sdkModule, AppModule lists sharedModule + appModule"

# Metrics
duration: 5min
completed: 2026-02-11
---

# Phase 3 Plan 3: Auth Interceptor & SDK API Integration Summary

**AuthInterceptor with Mutex-based 401 refresh+retry, AuthApi/UserApi typed functions, and complete Koin DI wiring across all KMP targets**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-11T18:33:59Z
- **Completed:** 2026-02-11T18:38:27Z
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments
- AuthInterceptor attaches bearer tokens to all requests and handles 401 with Mutex-based double-check refresh pattern
- AuthApi provides register, login, refresh, logout with automatic token lifecycle management (save on success, clear on logout)
- UserApi provides getProfile, updateProfile, getUserById returning Either<AppError, T>
- SdkModule + SharedModule wire complete Koin DI graph so UI code can inject AuthApi/UserApi directly
- All 4 KMP targets (Android, iOS, JVM, WasmJs) compile successfully

## Task Commits

Each task was committed atomically:

1. **Task 1: Create AuthInterceptor with token attachment and 401 refresh+retry** - `fce8ac6` (feat)
2. **Task 2: Create AuthApi, UserApi, SdkModule; wire DI into composeApp** - `a7e1012` (feat)

## Files Created/Modified
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt` - HttpSend interceptor with bearer token attachment and 401 refresh+retry via Mutex
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt` - SDK functions for register, login, refresh, logout with token lifecycle
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/UserApi.kt` - SDK functions for getProfile, updateProfile, getUserById
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt` - Koin module wiring AuthInterceptor, HttpClient, AuthApi, UserApi
- `core/sdk/build.gradle.kts` - Added core:storage dependency (api)
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt` - Replaced tokenProvider with AuthInterceptor parameter
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt` - Added Unit return type handling in apiCall<T>
- `shared/build.gradle.kts` - Added core:sdk dependency (api)
- `shared/src/commonMain/kotlin/com/m2f/template/di/SharedModule.kt` - includes(storageModule, sdkModule)

## Decisions Made
- Used `URLBuilder.buildString().contains("/auth/refresh")` instead of `encodedPath` for refresh endpoint detection, because `encodedPath` is not available on `URLBuilder` in Ktor 3.4.0 (only on built `Url` objects).
- Added `Unit` type check in `apiCall<T>` to skip body deserialization for endpoints returning no body (logout), preventing deserialization errors on empty 200 responses.
- Used Koin `includes()` in SharedModule to compose storageModule and sdkModule, keeping AppModule's `allAppModules` list unchanged.
- SdkModule uses `getProperty("BASE_URL", "http://localhost:8080")` for configurable base URL per platform (Android emulator needs `10.0.2.2`).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed URLBuilder.encodedPath unavailable in Ktor 3.4.0**
- **Found during:** Task 1 (AuthInterceptor creation)
- **Issue:** Plan used `request.url.encodedPath` but Ktor 3.4.0's URLBuilder does not expose `encodedPath` directly
- **Fix:** Changed to `request.url.buildString().contains("/auth/refresh")` which works with URLBuilder
- **Files modified:** core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt
- **Verification:** Compilation succeeds on all 4 KMP targets
- **Committed in:** fce8ac6 (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** Minor API difference in Ktor 3.4.0. Functionally identical behavior. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 3 (Client SDK & Storage) is complete with all 3 plans executed
- UI code can now inject `AuthApi` and `UserApi` via Koin and call functions returning `Either<AppError, T>`
- Token management is fully automatic (interceptor attaches, refreshes, and clears tokens)
- Ready for Phase 4/5 UI screens to build on top of this SDK layer

## Self-Check: PASSED
