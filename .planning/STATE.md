# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-10)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 2 - Server Auth & Users

## Current Position

Phase: 2 of 6 (Server Auth & Users) -- COMPLETE
Plan: 3 of 3 in current phase (02-01, 02-02, 02-03 complete)
Status: Phase 2 Complete
Last activity: 2026-02-11 -- Plan 02-03 complete (User profile & RBAC)

Progress: [███████_________] 41% (7/17 plans)

## Performance Metrics

**Velocity:**
- Total plans completed: 7
- Average duration: ~15 min
- Total execution time: ~104 min

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1 | 4/4 | ~46 min | ~12 min |
| 2 | 3/3 | ~58 min | ~19 min |

**Recent Trend:**
- Last 5 plans: 01-04 (~5 min), 01-03 (~17 min), 02-01 (~53 min), 02-03 (~5 min)
- Trend: Fast execution when building on established patterns

*Updated after each plan completion*

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- [Roadmap]: 6 phases derived from 32 requirements at standard depth
- [Roadmap]: Cross-cutting requirements (CC-01, CC-02) mapped to Phase 1 since Arrow Raise patterns must be established before features
- [Roadmap]: DX-01 (Setup CLI) placed in Phase 5 with dashboard since CLI needs final project structure
- [Roadmap]: Phase 6 (AI Agents) depends only on Phase 2, enabling parallel execution with Phases 3-5
- [01-01]: Used hardcoded plugin coordinates in buildSrc (dynamic libs.plugins unreliable in buildSrc classpath)
- [01-01]: Removed logback from ktor-monitoring bundle; server logging moves to Log4j
- [01-02]: Used id("com.android.library") instead of alias because AGP is on buildSrc classpath
- [01-02]: Error response functions take code+message params for structured error codes
- [01-02]: DomainError.toAppError() added as interface method for shared error mapping
- [01-03]: KoinApplication composable wraps App for all client targets (single DI initialization point)
- [01-03]: Server uses install(Koin) Ktor plugin with configurationModule + serverModule
- [01-03]: Validation helpers use context(raise: Raise<FieldError>) pattern with raise.ensure() calls
- [01-04]: Used EcsLayout.json (Elastic Common Schema) for server JSON log format
- [01-04]: Kermit exposed as api() in shared module for transitive availability
- [01-04]: SLF4J bridged to Log4j2 via log4j-slf4j2-impl, logback removed
- [01-fix]: Forced kotlin-stdlib version resolution to match compiler 2.2.10 (Arrow 2.2.0 pulls in 2.2.21, breaking WASM)
- [02-01]: Used kotlin.uuid.Uuid with @OptIn(ExperimentalUuidApi) for Exposed 1.0 table columns
- [02-01]: Used top-level Exposed operators (eq, and, greater) instead of deprecated SqlExpressionBuilder
- [02-01]: Used getKoin().declare(database) for R2dbcDatabase runtime DI registration
- [02-01]: Called registerAuthMigrations() in main() before startDatabase() for migration ordering
- [02-01]: Arrow zipOrAccumulate with withError maps FieldError to IncorrectInput for accumulated validation
- [02-03]: Used io.ktor.server.application for createRouteScopedPlugin (not io.ktor.server.routing)
- [02-03]: Uuid.parse() for string-to-UUID conversion in service layer
- [02-03]: Optional field validation with zipOrAccumulate: null fields pass through, non-null fields validated

### Pending Todos

None yet.

### Blockers/Concerns

- [Research]: Phase 3 (Client SDK) flagged for research -- token refresh mutex patterns in KMP, WASM DataStore limitations
- [Research]: Phase 6 (AI Agents) flagged for research -- Koog 0.6.1 is new, MCP integration and tool sandboxing patterns emerging

## Session Continuity

Last session: 2026-02-11
Stopped at: Completed 02-03-PLAN.md (User profile & RBAC) -- Phase 2 complete
Resume file: None
