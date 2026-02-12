# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-10)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 4 - Navigation & UI Components

## Current Position

Phase: 4 of 6 (Navigation & UI Components)
Plan: 1 of 5 in current phase (04-01 complete)
Status: Executing Phase 4
Last activity: 2026-02-12 -- Plan 04-01 complete (Navigation setup with type-safe routes)

Progress: [██████████░_____] 65% (11/17 plans)

## Performance Metrics

**Velocity:**
- Total plans completed: 11
- Average duration: ~11 min
- Total execution time: ~123 min

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1 | 4/4 | ~46 min | ~12 min |
| 2 | 3/3 | ~58 min | ~19 min |
| 3 | 3/3 | ~15 min | ~5 min |
| 4 | 1/5 | ~4 min | ~4 min |

**Recent Trend:**
- Last 5 plans: 03-01 (~5 min), 03-02 (~5 min), 03-03 (~5 min), 04-01 (~4 min)
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
- [02-02]: Same InvalidCredentials error for both missing user and wrong password (prevents user enumeration)
- [02-02]: Uuid.parse() for JWT userId string to kotlin.uuid.Uuid conversion (consistent with Exposed layer)
- [02-02]: Refresh token rotation revokes old token before issuing new (fail-safe design)
- [02-03]: Used io.ktor.server.application for createRouteScopedPlugin (not io.ktor.server.routing)
- [02-03]: Uuid.parse() for string-to-UUID conversion in service layer
- [02-03]: Optional field validation with zipOrAccumulate: null fields pass through, non-null fields validated
- [03-02]: Used multiplatform-settings-no-arg Settings() factory for cross-platform Settings creation (no expect/actual needed)
- [03-02]: Cast Settings() to ObservableSettings for Flow-based preference observation (all no-arg platforms support it)
- [03-02]: multiplatform-settings 1.3.0 confirmed compatible with Kotlin 2.3.10 on all targets including WasmJs
- [03-01]: Used class simpleName matching for exception mapping in apiCall (uniform across KMP targets)
- [03-01]: CancellationException re-thrown in apiCall to preserve structured concurrency
- [03-01]: CIO engine used for both JVM and WasmJs targets (CIO has WasmJs support in Ktor 3.x)
- [03-03]: AuthInterceptor uses URLBuilder.buildString().contains() for refresh endpoint detection (encodedPath unavailable in Ktor 3.4.0)
- [03-03]: apiCall<T> checks T::class == Unit::class to skip body deserialization for logout endpoint
- [03-03]: SharedModule uses Koin includes() to compose storageModule and sdkModule transitively
- [03-03]: SdkModule uses Koin getProperty for BASE_URL with localhost default, overridable per platform
- [04-01]: All routes defined as @Serializable data objects in navigation/Routes.kt (type-safe, compile-time checked)
- [04-01]: popUpTo<LoginRoute> { inclusive = true } clears auth back stack on login success
- [04-01]: MaterialTheme removed from App.kt; TerminalTheme will wrap AppNavHost in plan 04-02
- [04-01]: BasicText with hardcoded colors for placeholder screens (to be replaced by TerminalTheme components)

### Pending Todos

None yet.

### Blockers/Concerns

- [Research]: Phase 3 (Client SDK) flagged for research -- token refresh mutex patterns in KMP, WASM DataStore limitations
- [Research]: Phase 6 (AI Agents) flagged for research -- Koog 0.6.1 is new, MCP integration and tool sandboxing patterns emerging

## Session Continuity

Last session: 2026-02-12
Stopped at: Completed 04-01-PLAN.md (Navigation setup with type-safe routes)
Resume file: None
