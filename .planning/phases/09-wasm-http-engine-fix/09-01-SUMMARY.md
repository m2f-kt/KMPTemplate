---
phase: 09-wasm-http-engine-fix
plan: 01
subsystem: sdk
tags: [ktor, wasm, cors, browser, fetch, http-engine]

# Dependency graph
requires:
  - phase: 03-client-sdk-storage
    provides: "Platform engine expect/actual pattern and SDK module"
  - phase: 08-type-safe-shared-routes
    provides: "Current stable codebase with type-safe routing"
provides:
  - "Js HTTP engine for wasmJs browser target (replaces CIO)"
  - "CORS plugin on server for browser fetch compatibility"
  - "Working browser WASM HTTP networking to Ktor server"
affects: [wasm, browser, sdk, server]

# Tech tracking
tech-stack:
  added: [ktor-client-js]
  patterns: [browser-cors-for-wasm-fetch]

key-files:
  created: []
  modified:
    - "gradle/libs.versions.toml"
    - "core/sdk/build.gradle.kts"
    - "core/sdk/src/wasmJsMain/kotlin/com/m2f/template/sdk/PlatformEngine.wasmJs.kt"
    - "server/src/main/kotlin/com/m2f/template/Application.kt"

key-decisions:
  - "Js engine uses browser native fetch API (CIO requires Node.js net module unavailable in browsers)"
  - "CORS allows localhost:8080/8081/3000 for development flexibility, no anyHost() for security"
  - "allowCredentials=true required for Authorization header bearer token auth flows"

patterns-established:
  - "Platform engine mapping: Android=OkHttp, iOS=Darwin, JVM=CIO, WASM=Js"
  - "Server CORS: explicit dev origins, production domains added via env config"

# Metrics
duration: 53min
completed: 2026-02-16
---

# Phase 9 Plan 01: WASM HTTP Engine Fix Summary

**Swap CIO to Js engine for wasmJs browser target with CORS server config for browser fetch API compatibility**

## Performance

- **Duration:** 53 min
- **Started:** 2026-02-16T13:30:52Z
- **Completed:** 2026-02-16T14:24:21Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Replaced CIO HTTP engine with Js engine on wasmJs target, enabling browser-native fetch API for network requests
- Installed CORS plugin on server with secure development defaults (explicit localhost origins, no anyHost)
- All 4 platform engines verified: Android=OkHttp, iOS=Darwin, JVM=CIO, WASM=Js
- Full project build passes with no regressions across all targets

## Task Commits

Each task was committed atomically:

1. **Task 1: Swap CIO to Js engine for wasmJs target** - `b560a22` (feat)
2. **Task 2: Install CORS plugin on server for browser fetch compatibility** - `a0bd8c0` (feat)

## Files Created/Modified
- `gradle/libs.versions.toml` - Added ktor-client-js library entry with ktor version ref
- `core/sdk/build.gradle.kts` - Changed wasmJsMain dependency from ktor-client-cio to ktor-client-js
- `core/sdk/src/wasmJsMain/kotlin/com/m2f/template/sdk/PlatformEngine.wasmJs.kt` - Returns Js factory instead of CIO
- `server/src/main/kotlin/com/m2f/template/Application.kt` - Added install(CORS) with dev origins, methods, headers, credentials

## Decisions Made
- Js engine chosen because CIO on wasmJs requires Node.js `net` module unavailable in browser environments; Js uses native `fetch`
- CORS configured with explicit localhost ports (8080, 8081, 3000) instead of anyHost() for secure defaults
- allowCredentials=true enabled for bearer token Authorization header in auth flows
- OPTIONS method explicitly allowed for CORS preflight requests

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 9 is the last phase in the roadmap
- Phase complete, ready for milestone transition

---
*Phase: 09-wasm-http-engine-fix*
*Completed: 2026-02-16*
