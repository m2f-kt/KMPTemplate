---
phase: 03-client-sdk-storage
plan: 01
subsystem: api
tags: [ktor-client, kmp, okhttp, darwin, cio, arrow-either, error-mapping, http-client]

# Dependency graph
requires:
  - phase: 01-foundation
    provides: Arrow Core Either, AppError sealed hierarchy, ErrorResponse DTO, KMP module structure
provides:
  - HttpClient factory with platform-specific engines via expect/actual
  - apiCall<T> reified wrapper returning Either<AppError, T>
  - Centralized HTTP error status code to AppError mapping
  - Platform engine implementations for Android (OkHttp), iOS (Darwin), JVM (CIO), WasmJs (CIO)
affects: [03-02-auth-api-functions, 03-03-auth-interceptor, 05-ui-screens]

# Tech tracking
tech-stack:
  added: [ktor-client-okhttp, ktor-client-darwin]
  patterns: [expect-actual-platform-engine, either-wrapped-api-call, centralized-error-mapping]

key-files:
  created:
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/PlatformEngine.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt
    - core/sdk/src/androidMain/kotlin/com/m2f/template/sdk/PlatformEngine.android.kt
    - core/sdk/src/iosMain/kotlin/com/m2f/template/sdk/PlatformEngine.ios.kt
    - core/sdk/src/jvmMain/kotlin/com/m2f/template/sdk/PlatformEngine.jvm.kt
    - core/sdk/src/wasmJsMain/kotlin/com/m2f/template/sdk/PlatformEngine.wasmJs.kt
  modified:
    - gradle/libs.versions.toml
    - core/sdk/build.gradle.kts

key-decisions:
  - "Used class simpleName matching for exception mapping (IOException, TimeoutCancellationException) to avoid import issues across KMP targets"
  - "CancellationException is re-thrown to preserve coroutine cancellation semantics"
  - "tokenProvider parameter in createApiClient is nullable and unused -- reserved for Plan 03-03 AuthInterceptor"

patterns-established:
  - "expect/actual platformEngine(): Each platform source set provides its own HttpClientEngineFactory"
  - "apiCall<T> wrapper: All SDK API functions go through this for consistent Either<AppError, T> error handling"
  - "mapHttpError/mapException: Centralized error mapping with ErrorResponse body deserialization"

# Metrics
duration: 5min
completed: 2026-02-11
---

# Phase 3 Plan 1: SDK HTTP Infrastructure Summary

**Ktor Client HTTP layer with platform engines (OkHttp/Darwin/CIO), Either-based apiCall wrapper, and centralized HTTP-to-AppError mapping**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-11T18:24:50Z
- **Completed:** 2026-02-11T18:30:28Z
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments
- Platform-specific HTTP engines configured for all 4 KMP targets (Android, iOS, JVM, WasmJs) via expect/actual pattern
- HttpClient factory with expectSuccess=false, ContentNegotiation (kotlinx-json), and Logging plugins
- Reified `apiCall<T>` wrapper that converts HTTP responses to `Either<AppError, T>` with comprehensive error mapping
- All HTTP status codes mapped to specific AppError subtypes (401, 403, 404, 409, 422, 5xx)

## Task Commits

Each task was committed atomically:

1. **Task 1: Add Ktor Client platform engine dependencies and configure build** - `26bf637` (chore)
2. **Task 2: Create platform engine expect/actual, HttpClient factory, and error mapper** - `caef7b1` (feat)

## Files Created/Modified
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/PlatformEngine.kt` - expect fun platformEngine() declaration
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt` - createApiClient() factory with ContentNegotiation, Logging, expectSuccess=false
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt` - apiCall<T>, mapHttpError, mapException functions
- `core/sdk/src/androidMain/kotlin/com/m2f/template/sdk/PlatformEngine.android.kt` - actual platformEngine() returning OkHttp
- `core/sdk/src/iosMain/kotlin/com/m2f/template/sdk/PlatformEngine.ios.kt` - actual platformEngine() returning Darwin
- `core/sdk/src/jvmMain/kotlin/com/m2f/template/sdk/PlatformEngine.jvm.kt` - actual platformEngine() returning CIO
- `core/sdk/src/wasmJsMain/kotlin/com/m2f/template/sdk/PlatformEngine.wasmJs.kt` - actual platformEngine() returning CIO
- `gradle/libs.versions.toml` - Added ktor-client-okhttp and ktor-client-darwin library entries
- `core/sdk/build.gradle.kts` - Added all Ktor Client deps to commonMain and platform source sets

## Decisions Made
- Used class simpleName matching for exception types in `mapException()` rather than direct `is` checks, because `kotlin.io.IOException` availability varies across KMP targets and `TimeoutCancellationException` import paths differ -- simpleName matching works uniformly.
- Re-throw `CancellationException` in `apiCall` to preserve structured concurrency semantics -- catching it would break coroutine cancellation.
- Left `tokenProvider` parameter unused in `createApiClient()` -- the AuthInterceptor in Plan 03-03 will wire it.
- CIO engine used for both JVM and WasmJs targets per research recommendation (CIO has WasmJs support in Ktor 3.x).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Added CancellationException re-throw in apiCall**
- **Found during:** Task 2 (ErrorMapper implementation)
- **Issue:** Plan's catch(Exception) block would swallow CancellationException, breaking coroutine cancellation
- **Fix:** Added explicit `catch (e: CancellationException) { throw e }` before the general Exception catch
- **Files modified:** core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt
- **Verification:** Code compiles, follows Kotlin coroutine best practices
- **Committed in:** caef7b1 (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 missing critical)
**Impact on plan:** Essential for correctness of coroutine lifecycle management. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- SDK HTTP infrastructure is ready for Plan 03-02 (Auth/User API functions)
- `apiCall<T>` wrapper and `createApiClient()` provide the foundation for all typed SDK functions
- Plan 03-03 (AuthInterceptor) will add token management via the `tokenProvider` parameter

## Self-Check: PASSED

All 8 created files verified on disk. Both task commits (26bf637, caef7b1) verified in git log.

---
*Phase: 03-client-sdk-storage*
*Completed: 2026-02-11*
