---
phase: 06-ai-agent-infrastructure
plan: 02
subsystem: ai
tags: [koog, tools, persistence, exposed, r2dbc, agent-checkpoint]

# Dependency graph
requires:
  - phase: 06-01
    provides: "Koog 0.6.2 on server:ai classpath, AiErrors DomainError subtypes"
  - phase: 02-backend-core
    provides: "UserRepository, R2dbcDatabase, Exposed tables, MigrationRegistry"
provides:
  - "UserTools ToolSet with getUserByEmail tool querying UserRepository"
  - "ConversationsTable for agent checkpoint persistence (TEXT, not JSONB)"
  - "ExposedPersistenceStorage implementing PersistenceStorageProvider<AgentCheckpointPredicateFilter>"
  - "registerAiMigrations() for ai_conversations table creation"
affects: [06-03-PLAN]

# Tech tracking
tech-stack:
  added: []
  patterns: [annotation-based-toolset, exposed-persistence-storage-provider, upsert-checkpoint-pattern]

key-files:
  created:
    - "server/ai/src/main/kotlin/com/m2f/server/ai/tools/UserTools.kt"
    - "server/ai/src/main/kotlin/com/m2f/server/ai/persistence/ConversationsTable.kt"
    - "server/ai/src/main/kotlin/com/m2f/server/ai/persistence/ExposedPersistenceStorage.kt"
  modified:
    - "server/ai/src/main/kotlin/com/m2f/server/ai/Ai.kt"

key-decisions:
  - "PersistenceStorageProvider<AgentCheckpointPredicateFilter> is the actual Koog 0.6.2 interface (3 methods: saveCheckpoint, getCheckpoints, getLatestCheckpoint) -- differs from research docs"
  - "AgentCheckpointData serialized via PersistenceUtils.defaultCheckpointJson -- Koog provides the configured Json instance"
  - "Upsert pattern in saveCheckpoint for idempotent checkpoint saves"
  - "datetime columns (not timestamp) for consistency with existing auth tables"

patterns-established:
  - "Annotation-based ToolSet: @LLMDescription on class, @Tool on methods, return String for LLM consumption"
  - "Custom PersistenceStorageProvider: serialize to JSON TEXT, use suspendTransaction, filter in-memory after deserialization"

# Metrics
duration: 11min
completed: 2026-02-13
---

# Phase 6 Plan 2: Agent Tools and Conversation Persistence Summary

**Annotation-based UserTools ToolSet with database-backed ExposedPersistenceStorage implementing Koog's PersistenceStorageProvider for R2DBC Exposed**

## Performance

- **Duration:** 11 min
- **Started:** 2026-02-13T22:44:52Z
- **Completed:** 2026-02-13T22:56:01Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- UserTools ToolSet with getUserByEmail tool that queries UserRepository via Koog annotation-based @Tool pattern
- ConversationsTable stores checkpoint data as TEXT (not JSONB) following R2DBC compatibility research recommendation
- ExposedPersistenceStorage implements the actual Koog 0.6.2 PersistenceStorageProvider interface (3 methods, generic Filter type)
- registerAiMigrations() follows Auth.kt MigrationRegistry pattern for ai_conversations table

## Task Commits

Each task was committed atomically:

1. **Task 1: Create UserTools annotation-based ToolSet** - `67e2aa7` (feat)
2. **Task 2: Create conversation persistence table, storage provider, and migration** - `0c3c07c` (feat)

## Files Created/Modified
- `server/ai/src/main/kotlin/com/m2f/server/ai/tools/UserTools.kt` - Annotation-based ToolSet with getUserByEmail and getUserCount tools
- `server/ai/src/main/kotlin/com/m2f/server/ai/persistence/ConversationsTable.kt` - Exposed table for ai_conversations with TEXT checkpoint_data
- `server/ai/src/main/kotlin/com/m2f/server/ai/persistence/ExposedPersistenceStorage.kt` - Custom PersistenceStorageProvider using R2DBC Exposed
- `server/ai/src/main/kotlin/com/m2f/server/ai/Ai.kt` - Migration registration following Auth.kt pattern

## Decisions Made
- The actual Koog 0.6.2 PersistenceStorageProvider interface is `PersistenceStorageProvider<Filter>` with 3 methods (saveCheckpoint, getCheckpoints, getLatestCheckpoint), not the 5-method interface documented in research -- adapted implementation to match actual API
- Used `PersistenceUtils.defaultCheckpointJson` (from Koog) for AgentCheckpointData serialization instead of custom Json instance
- Used `AgentCheckpointPredicateFilter` as the filter type parameter (matches InMemoryPersistenceStorageProvider)
- Used `datetime` column type (consistent with auth tables) instead of `timestamp` for createdAt/updatedAt
- Used `kotlin.time.Clock` (not deprecated `kotlinx.datetime.Clock`) following existing AuthService.kt pattern

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Corrected Koog import paths from research documentation**
- **Found during:** Task 1 (UserTools creation) and Task 2 (ExposedPersistenceStorage)
- **Issue:** Research documented import paths did not match actual Koog 0.6.2 JAR structure. ToolSet is at `ai.koog.agents.core.tools.reflect.ToolSet` (not `ai.koog.agents.core.tools.ToolSet`). PersistenceStorageProvider is at `ai.koog.agents.snapshot.providers` (not documented path).
- **Fix:** Inspected Koog JARs via `jar tf` and `javap` to discover correct packages and interface signatures
- **Files modified:** UserTools.kt, ExposedPersistenceStorage.kt
- **Verification:** `./gradlew :server:ai:compileKotlin` passes clean
- **Committed in:** 67e2aa7, 0c3c07c

**2. [Rule 3 - Blocking] Adapted PersistenceStorageProvider to actual 3-method interface**
- **Found during:** Task 2 (ExposedPersistenceStorage creation)
- **Issue:** Research documented a 5-method interface (saveCheckpoint, loadCheckpoint, getLatestCheckpoint, listCheckpoints, deleteCheckpoint). Actual Koog 0.6.2 interface has 3 methods (saveCheckpoint, getCheckpoints, getLatestCheckpoint) with a generic Filter type parameter.
- **Fix:** Implemented the actual `PersistenceStorageProvider<AgentCheckpointPredicateFilter>` interface with correct method signatures
- **Files modified:** ExposedPersistenceStorage.kt
- **Verification:** Compiles clean with no abstract method errors
- **Committed in:** 0c3c07c

**3. [Rule 1 - Bug] Fixed Exposed datetime import path**
- **Found during:** Task 2 (ConversationsTable creation)
- **Issue:** Plan specified `org.jetbrains.exposed.v1.core.kotlin.datetime.timestamp` import which does not exist in Exposed 1.0
- **Fix:** Used `org.jetbrains.exposed.v1.datetime.datetime` matching existing auth table pattern
- **Files modified:** ConversationsTable.kt
- **Verification:** Compiles clean
- **Committed in:** 0c3c07c

---

**Total deviations:** 3 auto-fixed (2 blocking, 1 bug)
**Impact on plan:** All fixes necessary to adapt from research documentation to actual Koog 0.6.2 API. No scope creep -- identical functionality, correct import paths and interface signatures.

## Issues Encountered
None beyond the import/interface deviations documented above, which were expected per the plan's own "IMPORTANT" notes about verifying actual Koog import paths at compile time.

## User Setup Required
None for this plan. AI env vars and Koin wiring will be configured in plan 06-03.

## Next Phase Readiness
- UserTools ready for registration in ToolRegistry (plan 06-03)
- ExposedPersistenceStorage ready for Persistence feature installation (plan 06-03)
- registerAiMigrations() ready to be called from Application.kt alongside registerAuthMigrations()
- All files compile clean against Koog 0.6.2

## Self-Check: PASSED

All 4 created/modified files verified on disk. Both task commits (67e2aa7, 0c3c07c) verified in git log.

---
*Phase: 06-ai-agent-infrastructure*
*Completed: 2026-02-13*
