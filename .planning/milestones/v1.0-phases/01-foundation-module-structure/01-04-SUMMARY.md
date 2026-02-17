---
phase: 01-foundation-module-structure
plan: 04
subsystem: infra
tags: [kermit, log4j, slf4j, logging, json-logging, structured-logging]

requires:
  - phase: 01-01
    provides: "Kermit and Log4j version catalog entries, logging-server bundle"
  - phase: 01-02
    provides: "Module structure with server and shared modules"
provides:
  - "Kermit AppLogger utility with module-tagged loggers ([AUTH], [SDK], [STORAGE], [AI], [APP], [NAV], [DI])"
  - "Log4j JSON structured logging on server via EcsLayout"
  - "SLF4J bridged to Log4j2 via log4j-slf4j2-impl (logback removed)"
  - "Zero println statements in production Kotlin source"
affects: [02-auth-endpoints, 03-client-sdk, 04-compose-ui, 06-ai-agents]

tech-stack:
  added: [kermit-2.0.5, log4j-2.24.3, log4j-layout-template-json]
  patterns: [module-tagged-logging, structured-key-value-metadata, json-structured-server-logs]

key-files:
  created:
    - composeApp/src/commonMain/kotlin/com/m2f/template/logging/AppLogger.kt
    - server/src/main/resources/log4j2.json
  modified:
    - composeApp/build.gradle.kts
    - shared/build.gradle.kts
    - server/core/database/src/main/kotlin/com/m2f/core/database/migrations/Migration.kt
  deleted:
    - server/src/main/resources/logback.xml

key-decisions:
  - "Used EcsLayout.json (Elastic Common Schema) for server JSON log format via log4j-layout-template-json"
  - "Kermit exposed as api() in shared module for transitive availability to all client modules"
  - "SLF4J on server bridged to Log4j2 via log4j-slf4j2-impl, logback completely removed"

patterns-established:
  - "AppLogger.withTag(LogTag.Auth): module-tagged client logging with optional key-value metadata"
  - "Server logging via SLF4J LoggerFactory with module-based tag names (DATABASE, AUTH, etc.)"
  - "Structured log format: message | key=value, key=value for both client and server"

duration: ~5min
completed: 2026-02-10
---

# Plan 01-04: Structured Logging Summary

**Kermit client logging with module-tagged AppLogger utility and Log4j JSON server logging via EcsLayout, replacing all println with structured SLF4J/Kermit calls**

## Performance

- **Duration:** ~5 min
- **Started:** 2026-02-10T20:42:21Z
- **Completed:** 2026-02-10T20:47:21Z
- **Tasks:** 2
- **Files modified:** 6 (2 created, 3 modified, 1 deleted)

## Accomplishments
- Created AppLogger utility with module-tagged loggers ([AUTH], [SDK], [STORAGE], [AI], [APP], [NAV], [DI]) and key-value metadata formatting
- Configured Log4j JSON structured logging on server with EcsLayout and tuned per-library log levels (Ktor: info, Exposed: info, Netty: warn)
- Replaced all 2 println calls in Migration.kt with SLF4J parameterized logging using DATABASE tag
- Removed logback.xml and ensured SLF4J bridges to Log4j2 via log4j-slf4j2-impl

## Task Commits

Each task was committed atomically:

1. **Task 1: Kermit client logging and Log4j server configuration** - `6306e29` (feat)
2. **Task 2: Replace all println with structured logging** - `85726a5` (fix)

## Files Created/Modified
- `composeApp/src/commonMain/kotlin/com/m2f/template/logging/AppLogger.kt` - Kermit-based logging utility with sealed LogTag hierarchy and TaggedLogger with metadata formatting
- `server/src/main/resources/log4j2.json` - Log4j JSON configuration with EcsLayout, console appender, and tuned logger levels
- `composeApp/build.gradle.kts` - Added Kermit dependency to commonMain
- `shared/build.gradle.kts` - Added Kermit as api() dependency for transitive availability
- `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/Migration.kt` - Replaced 2 println calls with SLF4J LoggerFactory logging
- `server/src/main/resources/logback.xml` - Deleted (replaced by log4j2.json)

## Decisions Made
- Used EcsLayout.json (Elastic Common Schema) bundled with log4j-layout-template-json for server JSON log format -- production-ready structured output
- Exposed Kermit as `api()` in shared/build.gradle.kts so all client modules get transitive access without repeating the dependency
- SLF4J bridged to Log4j2 (not logback) on server -- logback.xml completely removed to avoid classpath conflicts

## Deviations from Plan
None - plan executed exactly as written.

## Issues Encountered
None.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- AppLogger utility ready for use in all client modules (auth, dashboard, SDK)
- Server logging infrastructure ready for all server feature modules
- Module tag convention established for consistent log filtering across the codebase

## Self-Check: PASSED

All 2 created files verified on disk. logback.xml confirmed deleted. Both task commits (6306e29, 85726a5) verified in git log.

---
*Plan: 01-04-foundation-module-structure*
*Completed: 2026-02-10*
