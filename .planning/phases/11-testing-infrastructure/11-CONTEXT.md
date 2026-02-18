# Phase 11: Testing Infrastructure - Context

**Gathered:** 2026-02-18
**Status:** Ready for planning

<domain>
## Phase Boundary

Reusable testing toolkit for the KMP template: a Turbine-based ViewModel test DSL, SDK interface extraction with fake builder DSL, shared test fixtures in a `core:testing` module, and Arrow/Kotest assertion support across all KMP targets. This phase builds the testing foundation — actual ViewModel migration and testing happens in Phase 12.

</domain>

<decisions>
## Implementation Decisions

### Test DSL shape
- Replicate the Airalo `ViewModelTestContext2` pattern exactly, adapted for `MviViewModel<Intent, Model, Mutation, Event>`
- Extension function `.test { }` on MviViewModel — sequential statement queuing via MutableSharedFlow
- Include both `test` and `scopedTest` variants (coroutine scope access in scopedTest)
- DSL methods: `intent(intent)`, `model(model)`, `event(event)` — queued as sealed Statement types, processed sequentially
- `verify` block uses Turbine's `.test {}` on both `model` and `event` flows, dispatches intents via `take()`
- Include `@ViewModelTestDsl` DSL marker annotation to prevent scope leaking
- Strip debug println logging — clean test output (no intent/model/event println statements)

### SDK interface extraction
- Extract interfaces for ALL SDK API classes, not just AuthApi and UserApi
- Naming: interface gets the clean name (`AuthApi`), concrete implementation gets `Impl` suffix (`AuthApiImpl`)
- Interfaces and implementations live in the same SDK module (no separate contracts module)
- Facade pattern: a single `Sdk` class implements all extracted interfaces using Kotlin `by` delegation
- Koin provides each `*ApiImpl` binding; `Sdk` delegates to them
- Consumers inject specific interfaces (`AuthApi`, `UserApi`) not the Sdk facade directly

### Fake behavior depth
- DSL builder pattern for fakes — replicate Airalo's `FakeAuthSDKBuilder` approach
- Each interface method gets a configurable lambda behavior in the builder
- Builder has setter methods for each behavior (e.g., `fun login(behavior: (...) -> Either<ClientError, T>)`)
- `build()` creates an internal implementation that delegates to the builder's lambdas
- `@FakeSDKDsl` marker annotation on builders
- Top-level DSL functions: `fakeAuthApi { login { Either.Right(token) } }` — clean entry points
- Default behavior for unconfigured methods: return `Either.Left(ClientError.Unknown)` — tests fail fast on unexpected paths
- SDK fakes (FakeAuthApi, FakeUserApi, etc.) live in core:testing for cross-module reuse
- Module-specific fakes stay in their own module's test sources

### Assertion style
- Kotest matchers as primary assertion library (`shouldBe`, etc.)
- Use `kotest-extensions-arrow` library for Arrow Either assertions (`shouldBeRight()`, `shouldBeLeft()`) — if KMP-compatible
- Custom Arrow matchers only as fallback if official extension doesn't support KMP
- core:testing module re-exports kotest + turbine via `api()` — consumers add single `testImplementation(projects.core.testing)` dependency

### Claude's Discretion
- Exact fake behavior depth (configurable stubs vs stateful) — Claude picks what makes sense per interface
- How to handle photos with no EXIF data equivalent edge cases in fakes
- Internal test fixture structure within core:testing
- Whether to include test helpers beyond the ViewModel DSL (e.g., coroutine test utilities)

</decisions>

<specifics>
## Specific Ideas

- Airalo ViewModelTestContext2 reference: sequential statement flow with `intent()` → `model()` → `event()` ordering, Turbine under the hood
- Airalo FakeAuthSDKBuilder reference: builder class with configurable lambdas, `@FakeSDKDsl` marker, internal build() method
- The template should demonstrate the full pattern with at least one working test (LoginViewModel per success criteria)

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 11-testing-infrastructure*
*Context gathered: 2026-02-18*
