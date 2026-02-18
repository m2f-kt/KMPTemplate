# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-17)

**Core value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.
**Current focus:** Phase 11.1 -- Fake SDK Facade, Fix Android Compile, Update MVI-ViewModel Skill

## Current Position

Phase: 11.1 (Fake SDK Facade, Fix Android Compile, Update MVI-ViewModel Skill)
Plan: 1 of 2 in current phase
Status: Executing phase 11.1
Last activity: 2026-02-18 -- Completed 11.1-01-PLAN.md

Progress: [██████████░░░░░░░░░░] 71% (5/7 plans)

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
| 11.1 Fake SDK Facade + Fixes | 1 | 2min | 2min |

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
- Phase 11.1: FakeSdkBuilder uses direct Sdk constructor instead of subclassing (Sdk is final class)
- Phase 11.1: Android kotlin-test-junit declared separately from jvmMain (KMP androidTarget is independent compilation unit)

Decisions are logged in PROJECT.md Key Decisions table.

### Roadmap Evolution

- Phase 11.1 inserted after Phase 11: Fake SDK facade, fix Android compile, update mvi-viewmodel skill (URGENT)

### Blockers/Concerns

- RESOLVED: Turbine 1.2.1 + Kotlin 2.3.10 compatibility verified (compiles on all KMP targets including wasmJs)
- Research flags: Ktor testApplication dispatcher issue (KTOR-7121) needs spike in Phase 11
- Research flags: WASM async resource loading timing needs smoke test in Phase 15

## Session Continuity

Last session: 2026-02-18
Stopped at: Completed 11.1-01-PLAN.md
Resume file: .planning/phases/11.1-fake-sdk-facade-fix-android-compile-update-mvi-viewmodel-skill/11.1-01-SUMMARY.md
