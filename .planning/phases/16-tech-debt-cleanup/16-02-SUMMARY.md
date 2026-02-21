---
phase: 16-tech-debt-cleanup
plan: 02
subsystem: infra
tags: [coroutines, dispatchers, ktor, koin, ai-agents, performance]

requires:
  - phase: 06-ai-agent-infrastructure
    provides: "ChatAgentService with Koog agent streaming"
provides:
  - "Named bounded dispatchers (dbDispatcher, aiDispatcher, computeDispatcher) in Configuration"
  - "Injected dispatchers via Koin — testable and explicit"
  - "runBlocking removed from ChatAgentService awaitClose"
affects: [ai, auth, database, server]

tech-stack:
  added: []
  patterns:
    - "Named dispatcher pattern: Configuration defines purpose-specific bounded dispatcher views"
    - "Fire-and-forget cleanup: CoroutineScope(dispatcher).launch for non-blocking agent close"

key-files:
  created: []
  modified:
    - "server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Configuration.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/security/PasswordHasher.kt"
    - "server/auth/src/main/kotlin/com/m2f/server/auth/di/AuthModule.kt"
    - "server/ai/src/main/kotlin/com/m2f/server/ai/agents/ChatAgent.kt"
    - "server/ai/src/main/kotlin/com/m2f/server/ai/di/AiModule.kt"
    - "server/groups/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt"

key-decisions:
  - "dbDispatcher: IO.limitedParallelism(16) to match typical R2DBC pool size"
  - "aiDispatcher: IO.limitedParallelism(8) to isolate long-running AI streams from DB"
  - "computeDispatcher: Dispatchers.Default (CPU-bound, sized to core count)"
  - "Agent cleanup via CoroutineScope(aiDispatcher).launch instead of runBlocking"

patterns-established:
  - "Named dispatcher injection: services receive dispatchers via constructor, wired through Koin"
  - "Dispatcher isolation: AI and DB workloads on separate bounded views of IO pool"

requirements-completed: [DEBT-05]

duration: 5min
completed: 2026-02-21
---

# Plan 16-02: Named Dispatcher Configuration Summary

**Three named bounded dispatchers (db/ai/compute) with documented rationale replace generic dispatchers; runBlocking removed from ChatAgent streaming cleanup**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-21
- **Completed:** 2026-02-21
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- Defined dbDispatcher (IO, 16 threads), aiDispatcher (IO, 8 threads), computeDispatcher (Default) with KDoc rationale
- Removed runBlocking from ChatAgentService.streamChat() awaitClose block
- PasswordHasher now receives computeDispatcher via constructor injection
- All dispatcher wiring through Koin — fully testable

## Task Commits

Each task was committed atomically:

1. **Task 1: Define named dispatchers in Configuration and update PasswordHasher** - `582e9aa` (refactor)
2. **Task 2: Remove runBlocking from ChatAgent and wire aiDispatcher** - `94dabf2` (refactor)

## Files Created/Modified
- `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Configuration.kt` - Named dispatcher instances with limitedParallelism() bounds
- `server/auth/src/main/kotlin/com/m2f/server/auth/security/PasswordHasher.kt` - CPU-bound password hashing on injected computeDispatcher
- `server/auth/src/main/kotlin/com/m2f/server/auth/di/AuthModule.kt` - Koin wiring for computeDispatcher
- `server/ai/src/main/kotlin/com/m2f/server/ai/agents/ChatAgent.kt` - AI streaming without runBlocking, using aiDispatcher
- `server/ai/src/main/kotlin/com/m2f/server/ai/di/AiModule.kt` - Koin wiring for aiDispatcher
- `server/groups/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt` - Updated PasswordHasher constructor in test helper

## Decisions Made
- dbDispatcher bounded to 16 threads to match typical R2DBC connection pool size
- aiDispatcher bounded to 8 threads; separate from DB to prevent AI streams from starving queries
- computeDispatcher uses Dispatchers.Default (sized to CPU core count)
- Agent cleanup uses fire-and-forget CoroutineScope(aiDispatcher).launch instead of runBlocking

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Updated TestHelpers.kt PasswordHasher constructor call**
- **Found during:** Task 1 (PasswordHasher constructor change)
- **Issue:** TestHelpers.kt used zero-arg PasswordHasher() which no longer compiles
- **Fix:** Passed kotlinx.coroutines.Dispatchers.Default directly in test helper
- **Files modified:** server/groups/src/test/kotlin/com/m2f/server/groups/TestHelpers.kt
- **Verification:** ./gradlew :server:groups:compileTestKotlin + :server:groups:test pass
- **Committed in:** 582e9aa (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Fix necessary for compilation. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Named dispatchers ready for Phase 17+ services (file upload, email, RAG) to use
- dbDispatcher available for R2DBC repositories
- aiDispatcher available for embedding and RAG operations

---
*Phase: 16-tech-debt-cleanup*
*Completed: 2026-02-21*
