---
phase: 08-type-safe-shared-routes
plan: 01
subsystem: api
tags: [ktor-resources, type-safe-routing, kmp, multiplatform]

# Dependency graph
requires:
  - phase: 01-foundation
    provides: "Ktor server/client setup, version catalog, build conventions"
  - phase: 02-server-auth
    provides: "Auth and user route definitions that @Resource classes mirror"
  - phase: 06-ai-agents
    provides: "AI route definitions that @Resource classes mirror"
provides:
  - "@Resource-annotated route classes (Auth, Users, Ai) in core:models"
  - "ktor-resources, ktor-server-resources, ktor-client-resources dependencies"
  - "Resources plugin installed on server Application and client HttpClient"
affects: [08-02, 08-03]

# Tech tracking
tech-stack:
  added: [ktor-resources 3.4.0, ktor-server-resources 3.4.0, ktor-client-resources 3.4.0]
  patterns: ["@Resource nested class hierarchy for type-safe route definitions", "Parent reference pattern (val parent: ParentType = ParentType()) for Ktor nested resources"]

key-files:
  created:
    - "core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt"
  modified:
    - "gradle/libs.versions.toml"
    - "core/models/build.gradle.kts"
    - "core/sdk/build.gradle.kts"
    - "server/build.gradle.kts"
    - "server/auth/build.gradle.kts"
    - "server/ai/build.gradle.kts"
    - "server/src/main/kotlin/com/m2f/template/Application.kt"
    - "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt"

key-decisions:
  - "Regular classes (not data classes) for @Resource without properties -- no-arg constructor requirement"
  - "WebSocket path as companion const in Ai.Chat (KTOR-4369: type-safe routing unsupported for WebSockets)"

patterns-established:
  - "Nested @Resource classes: each child has val parent: ParentType = ParentType() for path resolution"
  - "Server uses io.ktor.server.resources.Resources, client uses io.ktor.client.plugins.resources.Resources (different import paths)"

# Metrics
duration: 4min
completed: 2026-02-15
---

# Phase 8 Plan 01: Ktor Resources Foundation Summary

**Ktor Resources dependencies, shared @Resource route classes (Auth/Users/Ai) in core:models, and Resources plugin on server+client**

## Performance

- **Duration:** 4 min
- **Started:** 2026-02-15T13:56:59Z
- **Completed:** 2026-02-15T14:01:09Z
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments
- Added ktor-resources, ktor-server-resources, and ktor-client-resources to version catalog and wired into correct modules
- Created ApiRoutes.kt with @Resource-annotated Auth (6 routes), Users (2 routes), and Ai (2 routes + WS_PATH constant) hierarchies
- Installed Resources plugin on both server Application.module() and client HttpClient builder
- Verified compilation on all KMP targets (JVM, Android, iOS, WasmJs)

## Task Commits

Each task was committed atomically:

1. **Task 1: Add Ktor Resources dependencies to version catalog and build files** - `dcf5670` (chore)
2. **Task 2: Define @Resource route classes and install Resources plugin** - `d4c0075` (feat)

## Files Created/Modified
- `gradle/libs.versions.toml` - Added 3 new ktor-resources library entries
- `core/models/build.gradle.kts` - Added ktor-resources dependency to commonMain
- `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt` - All @Resource route class definitions
- `core/sdk/build.gradle.kts` - Added ktor-client-resources dependency to commonMain
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/ApiClient.kt` - install(Resources) in HttpClient builder
- `server/build.gradle.kts` - Added ktor-server-resources dependency
- `server/auth/build.gradle.kts` - Added ktor-server-resources dependency
- `server/ai/build.gradle.kts` - Added ktor-server-resources dependency
- `server/src/main/kotlin/com/m2f/template/Application.kt` - install(Resources) in Application.module()

## Decisions Made
- Regular classes (not data classes) for @Resource without properties -- they need no-arg constructors for Ktor resource resolution
- WebSocket path kept as companion object constant `Ai.Chat.WS_PATH` because Ktor type-safe routing does not support WebSockets (KTOR-4369)
- Server Resources import: `io.ktor.server.resources.Resources`; Client Resources import: `io.ktor.client.plugins.resources.Resources` (different packages, easy to mix up)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- @Resource classes available in core:models for both server and client modules
- Server ready for Plan 02: migrate server routes from string-based to type-safe `get<R>`, `post<R>` handlers
- Client SDK ready for Plan 03: migrate client calls from string URLs to type-safe resource requests

## Self-Check: PASSED

All 9 files verified present. Both commit hashes (dcf5670, d4c0075) verified in git log.

---
*Phase: 08-type-safe-shared-routes*
*Completed: 2026-02-15*
