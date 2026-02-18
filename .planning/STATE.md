# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-17)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 11 -- Testing Infrastructure

## Current Position

Phase: 11 of 15 (Testing Infrastructure) -- COMPLETE
Plan: 3 of 3 in current phase
Status: Phase 11 complete
Last activity: 2026-02-18 -- Completed 11-03-PLAN.md

Progress: [██████████░░░░░░░░░░] 67% (4/6 plans)

## v1.0 Performance (archived)

Progress: [████████████████████] 100% (39/39 plans)

## Performance Metrics

**Velocity (v1.0):**
- Total plans completed: 39
- Average duration: ~8 min
- Total execution time: ~315 min

**By Phase (v1.1):**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 10. MVI ViewModel Foundation | 1 | 2min | 2min |
| 11. Testing Infrastructure | 3 | 24min | 8min |

## Accumulated Context

### Decisions

- Phase 10: extraBufferCapacity=64 on MviViewModel pipeline to prevent emit suspension before collectors ready
- Phase 10: reduce is suspend per user decision (diverges from Airalo reference)
- Phase 10: koin-core kept as implementation dependency (not exposed to consumers)
- Phase 11: Interface gets clean name (AuthApi), Impl suffix for concrete class (AuthApiImpl)
- Phase 11: SdkModule Koin bindings use interface type qualifiers (single<AuthApi>) for correct DI resolution
- Phase 11: Added kotlin-test-junit JVM dependency for @BeforeTest/@AfterTest resolution in commonMain test library
- Phase 11: Statement queuing pattern for ViewModel test DSL (intent/model/event calls build list, runner processes sequentially)
- Phase 11: Initial StateFlow emission auto-consumed in DSL so test authors only see state changes
- Phase 11: MviViewModel.test{} DSL sets Dispatchers.Main to UnconfinedTestDispatcher(testScheduler) for scheduler alignment
- Phase 11: advanceUntilIdle() after each intent dispatch in DSL ensures viewModelScope coroutines complete

Decisions are logged in PROJECT.md Key Decisions table.

### Blockers/Concerns

- RESOLVED: Turbine 1.2.1 + Kotlin 2.3.10 compatibility verified (compiles on all KMP targets including wasmJs)
- Research flags: Ktor testApplication dispatcher issue (KTOR-7121) needs spike in Phase 11
- Research flags: WASM async resource loading timing needs smoke test in Phase 15

## Session Continuity

Last session: 2026-02-18
Stopped at: Completed 11-03-PLAN.md (Phase 11 complete)
Resume file: .planning/phases/11-testing-infrastructure/11-03-SUMMARY.md
