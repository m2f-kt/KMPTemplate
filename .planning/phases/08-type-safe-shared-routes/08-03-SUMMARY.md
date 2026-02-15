---
phase: 08-type-safe-shared-routes
plan: 03
subsystem: api
tags: [ktor-resources, ktor-client-resources, type-safe-routing, kmp, sdk]

# Dependency graph
requires:
  - phase: 08-type-safe-shared-routes
    plan: 01
    provides: "@Resource route classes (Auth, Users) and ktor-client-resources dependency"
provides:
  - "Type-safe SDK client API calls using @Resource instances instead of string URLs"
  - "Type-safe AuthInterceptor refresh detection via href(ResourcesFormat(), Auth.Refresh())"
  - "Zero hardcoded API URL strings in entire core:sdk module"
affects: []

# Tech tracking
tech-stack:
  added: []
  patterns: ["client.post(Auth.Register()) type-safe client resource requests", "href(ResourcesFormat(), Auth.Refresh()) for runtime path derivation from @Resource"]

key-files:
  created: []
  modified:
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt"
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/UserApi.kt"
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt"
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt"

key-decisions:
  - "href(ResourcesFormat(), Auth.Refresh()) for type-safe refresh path detection instead of hardcoded string"
  - "URLBuilder.buildString().contains() for path matching (encodedPath unavailable on URLBuilder in Ktor 3.4.0)"

patterns-established:
  - "Client SDK uses io.ktor.client.plugins.resources.post/get/put instead of io.ktor.client.request equivalents"
  - "Path parameters via typed resource properties: Users.ById(id = id) instead of string interpolation"

# Metrics
duration: 5min
completed: 2026-02-15
---

# Phase 8 Plan 03: SDK Client Migration Summary

**All 11 SDK HTTP calls migrated from hardcoded URL strings to type-safe client.post/get/put(Resource()) calls with href()-based refresh detection in AuthInterceptor**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-15T14:04:01Z
- **Completed:** 2026-02-15T14:09:48Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Migrated all 6 AuthApi post calls to use Auth.Register/Login/Refresh/ForgotPassword/ResetPassword/Logout resources
- Migrated all 3 UserApi get/put calls to use Users.Me and Users.ById(id=id) resources with typed path parameters
- AuthInterceptor refresh detection now derives path from Auth.Refresh resource via href(ResourcesFormat(), Auth.Refresh())
- AuthInterceptor refresh request uses client.post(Auth.Refresh()) instead of string URL
- Zero hardcoded API URL strings remain in the entire core:sdk module
- Verified compilation on JVM, WasmJs, and iOS targets

## Task Commits

Each task was committed atomically:

1. **Task 1: Migrate AuthApi.kt and UserApi.kt to type-safe client requests** - `e15c4c3` (feat)
2. **Task 2: Migrate AuthInterceptor refresh detection and update ErrorMapper KDoc** - `bfd8ded` (feat)

## Files Created/Modified
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/AuthApi.kt` - 6 type-safe Auth.* post calls replacing string URLs
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/UserApi.kt` - 3 type-safe Users.* get/put calls with typed path params
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt` - Type-safe refresh path detection and refresh request
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ErrorMapper.kt` - KDoc example updated with type-safe call

## Decisions Made
- Used `href(ResourcesFormat(), Auth.Refresh())` for deriving the refresh path from the @Resource definition at runtime, keeping the path detection fully type-safe and in sync with ApiRoutes.kt
- Used `URLBuilder.buildString().contains()` for path matching because `encodedPath` is not available on `URLBuilder` in Ktor 3.4.0 (consistent with the original implementation pattern per decision [03-03])

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed encodedPath unavailable on URLBuilder**
- **Found during:** Task 2 (AuthInterceptor migration)
- **Issue:** Plan suggested `request.url.encodedPath.endsWith(refreshPath)` but `encodedPath` is not a property on `URLBuilder` in Ktor 3.4.0
- **Fix:** Used `request.url.buildString().contains(refreshPath)` which matches the original pattern and is consistent with prior decision [03-03]
- **Files modified:** AuthInterceptor.kt
- **Verification:** SDK compiles on all 3 targets (JVM, WasmJs, iOS)
- **Committed in:** bfd8ded (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** Minor API adjustment; same behavior and type-safety achieved. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Full stack is now type-safe: route changes in ApiRoutes.kt cause compiler errors in both server handlers AND client SDK calls
- Phase 8 (Type-Safe Shared Routes) is complete: all 3 plans executed
- The template project has zero hardcoded API URL strings in either server or client code

## Self-Check: PASSED

All 4 modified files verified present. Both commit hashes (e15c4c3, bfd8ded) verified in git log.

---
*Phase: 08-type-safe-shared-routes*
*Completed: 2026-02-15*
