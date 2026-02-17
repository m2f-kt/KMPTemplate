# Phase 10: MVI ViewModel Foundation - Context

**Gathered:** 2026-02-18
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver a formal MVI base class (`MviViewModel`) that developers extend with typed Intent/Model/Mutation/Event parameters to build any ViewModel. The base class lives in a new `core:mvi` module. Migrating existing ViewModels is Phase 12.

</domain>

<decisions>
## Implementation Decisions

### Base class API shape
- Replicate the Airalo `AiraloViewModel` pattern exactly, renamed to `MviViewModel`
- 4 type parameters: `Intent`, `Model`, `Mutation`, `Event`
- Constructor takes `initialState: Model` and optional `modelSharingStarted: SharingStarted`
- Unified internal stream: `MutableSharedFlow<Either<Event, Mutation>>` using Arrow Either
- Two abstract methods: `take(intent: Intent)` and `reduce(model: Model, mutation: Mutation): Model`
- Three protected helpers: `sendEvent(event)`, `sendMutation(mutation)`, `sendStatement(statement)`
- Public API: `model: StateFlow<Model>` and `event: SharedFlow<Event>` (both lazy)
- Match reference API surface exactly — no extra convenience methods beyond the reference

### State & effect semantics
- State sharing: `SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)` (default, configurable)
- Events: `SharedFlow` with `SharingStarted.WhileSubscribed()` — events are fire-and-forget, dropped if no collector
- Note: roadmap success criterion #3 mentions "Channel" but user prefers SharedFlow matching reference. Update criterion to reflect SharedFlow approach
- `reduce` should be `suspend` (diverges from reference) to support slow operations or IO in reduce

### Module placement
- New `core:mvi` module — dedicated module for MVI base class
- Koin is an `implementation` dependency only (not exposed to consumers)
- No Koin module exported — each feature module registers its own ViewModels
- No demo ViewModel in this phase — Phase 12 migration proves the pattern

### Developer conventions
- Intent/Model/Mutation/Event defined as nested sealed classes/interfaces inside each ViewModel
- Every ViewModel must explicitly pass `initialState` — no defaults
- Koin registration follows whatever pattern the existing codebase uses (match existing convention)

### Claude's Discretion
- Whether to add a custom `koinMviViewModel()` extension or rely on standard `koinViewModel()` — decide based on whether it adds real value
- Internal implementation details (yield() placement, exact coroutine dispatchers)
- Module build file configuration details

</decisions>

<specifics>
## Specific Ideas

- Full reference implementation provided from Airalo codebase — replicate the `AiraloViewModel` pattern with these changes:
  - Rename to `MviViewModel`
  - Make `reduce` suspend
  - Place in `core:mvi` module
- The reference uses `filterIsInstance<Either.Right<Mutation>>` and `filterIsInstance<Either.Left<Event>>` to split the unified stream — keep this pattern

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 10-mvi-viewmodel-foundation*
*Context gathered: 2026-02-18*
