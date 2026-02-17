---
phase: 06-ai-agent-infrastructure
plan: 01
subsystem: ai
tags: [koog, openai, arrow-raise, ktor, koin, ai-agents]

# Dependency graph
requires:
  - phase: 01-foundation
    provides: "Arrow Raise patterns, DomainError interface, Koin DI, Ktor server conventions"
  - phase: 02-backend-core
    provides: "Auth module (UserRepository), database module (Exposed), error response helpers"
provides:
  - "Koog 0.6.2 on server:ai classpath with agents + ktor modules"
  - "Env.Ai configuration with conditional AI_ENABLED flag"
  - "AppError.AI sealed hierarchy (4 error types) in shared models"
  - "AiErrors.kt DomainError subtypes following AuthErrors.kt pattern"
affects: [06-02-PLAN, 06-03-PLAN]

# Tech tracking
tech-stack:
  added: [koog 0.6.2, koog-ktor, koog-agents]
  patterns: [conditional-feature-flag-via-env, ai-error-domain-mapping]

key-files:
  created:
    - "server/ai/src/main/kotlin/com/m2f/server/ai/errors/AiErrors.kt"
  modified:
    - "gradle/libs.versions.toml"
    - "server/ai/build.gradle.kts"
    - "server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt"
    - "core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt"

key-decisions:
  - "AI_ENABLED env var defaults to false for safe server startup without AI configuration"
  - "4 AI error types cover agent execution, agent lookup, conversation lookup, and provider availability"

patterns-established:
  - "Conditional feature enablement: Env.Ai.enabled flag gates AI functionality at runtime"
  - "AI error mapping: DomainError -> toAppError() -> AppError.AI following established AuthErrors.kt pattern"

# Metrics
duration: 4min
completed: 2026-02-13
---

# Phase 6 Plan 1: AI Foundation Dependencies and Error Types Summary

**Koog 0.6.2 AI framework on server:ai classpath with conditional Env.Ai configuration and 4 typed AI error pairs (AppError.AI + DomainError)**

## Performance

- **Duration:** 4 min
- **Started:** 2026-02-13T22:37:51Z
- **Completed:** 2026-02-13T22:42:28Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Koog 0.6.2 (agents + ktor modules) resolves and compiles on server:ai classpath
- AI configuration gated behind AI_ENABLED env var (defaults false) so server starts without AI env vars
- 4 AI-specific error types in shared models (AppError.AI) and 4 matching DomainError subtypes in server:ai
- All error types follow the established AuthErrors.kt pattern with toAppError() mapping and HTTP status responses

## Task Commits

Each task was committed atomically:

1. **Task 1: Add Koog dependencies and update server:ai build** - `4ce7d7e` (chore)
2. **Task 2: Add AI configuration, AppError.AI, and AiErrors DomainError subtypes** - `5f4ec53` (feat)

## Files Created/Modified
- `gradle/libs.versions.toml` - Added koog 0.6.2 version, koog-ktor/koog-agents libraries, koog bundle
- `server/ai/build.gradle.kts` - Added server:auth, core:database, Koog, Arrow, Koin, Ktor dependencies
- `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt` - Added Env.Ai data class with enabled/openaiApiKey
- `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` - Added AppError.AI sealed class with 4 subtypes
- `server/ai/src/main/kotlin/com/m2f/server/ai/errors/AiErrors.kt` - Created 4 DomainError subtypes matching AppError.AI

## Decisions Made
- AI_ENABLED env var defaults to false so server starts safely without any AI configuration
- 4 AI error types chosen to cover the core agent lifecycle: execution failure, agent not found, conversation not found, provider unavailable
- AiErrors follow AuthErrors.kt pattern exactly: data class implementing DomainError with context(RoutingContext) respond()

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None for this plan. AI env vars (AI_ENABLED, OPENAI_API_KEY) will be needed when AI endpoints are wired in subsequent plans.

## Next Phase Readiness
- server:ai module has Koog on classpath, ready for agent definitions (plan 06-02)
- AI error types available for Raise<DomainError> patterns in agent service layer
- Env.Ai accessible via Koin for conditional AI endpoint registration

## Self-Check: PASSED

All 5 created/modified files verified on disk. Both task commits (4ce7d7e, 5f4ec53) verified in git log.

---
*Phase: 06-ai-agent-infrastructure*
*Completed: 2026-02-13*
