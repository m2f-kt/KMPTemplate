# Phase 10: MVI ViewModel Foundation - Research

**Researched:** 2026-02-18
**Domain:** MVI architecture base class for Kotlin Multiplatform Compose ViewModels
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Base class API shape
- Replicate the Airalo `AiraloViewModel` pattern exactly, renamed to `MviViewModel`
- 4 type parameters: `Intent`, `Model`, `Mutation`, `Event`
- Constructor takes `initialState: Model` and optional `modelSharingStarted: SharingStarted`
- Unified internal stream: `MutableSharedFlow<Either<Event, Mutation>>` using Arrow Either
- Two abstract methods: `take(intent: Intent)` and `reduce(model: Model, mutation: Mutation): Model`
- Three protected helpers: `sendEvent(event)`, `sendMutation(mutation)`, `sendStatement(statement)`
- Public API: `model: StateFlow<Model>` and `event: SharedFlow<Event>` (both lazy)
- Match reference API surface exactly -- no extra convenience methods beyond the reference

#### State & effect semantics
- State sharing: `SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)` (default, configurable)
- Events: `SharedFlow` with `SharingStarted.WhileSubscribed()` -- events are fire-and-forget, dropped if no collector
- Note: roadmap success criterion #3 mentions "Channel" but user prefers SharedFlow matching reference. Update criterion to reflect SharedFlow approach
- `reduce` should be `suspend` (diverges from reference) to support slow operations or IO in reduce

#### Module placement
- New `core:mvi` module -- dedicated module for MVI base class
- Koin is an `implementation` dependency only (not exposed to consumers)
- No Koin module exported -- each feature module registers its own ViewModels
- No demo ViewModel in this phase -- Phase 12 migration proves the pattern

#### Developer conventions
- Intent/Model/Mutation/Event defined as nested sealed classes/interfaces inside each ViewModel
- Every ViewModel must explicitly pass `initialState` -- no defaults
- Koin registration follows whatever pattern the existing codebase uses (match existing convention)

### Claude's Discretion
- Whether to add a custom `koinMviViewModel()` extension or rely on standard `koinViewModel()` -- decide based on whether it adds real value
- Internal implementation details (yield() placement, exact coroutine dispatchers)
- Module build file configuration details

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| MVI-01 | Developer can extend a generic MVI ViewModel base class with Intent/Model/Mutation/Event type parameters | Verified: `abstract class MviViewModel<Intent, Model, Mutation, Event>` extends `ViewModel()`. Four type parameters constrain the sealed type system. Kotlin generics fully support this. |
| MVI-02 | ViewModel exposes state as StateFlow<Model> with a pure reduce(Model, Mutation) function | Verified: `Flow.scan(initial, suspend (acc, value) -> R)` natively accepts suspend accumulator. `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState)` produces `StateFlow<Model>`. |
| MVI-03 | ViewModel emits one-shot events via SharedFlow<Event> (no double-firing) | Verified: `shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay=0)` with zero replay means events are fire-and-forget. No replay cache = no double-firing on recomposition. Note: roadmap says "Channel" but user decision locks SharedFlow. |
| MVI-04 | ViewModels are injectable via Koin across all KMP targets | Verified: Koin 4.1.1 `koinViewModel()` works on Android, iOS, Desktop (full support) and WASM (experimental). Already proven in this codebase with 5 existing ViewModels. `MviViewModel` extends `ViewModel()` so `koinViewModel<T>()` works identically. |
</phase_requirements>

## Summary

Phase 10 delivers a single abstract class `MviViewModel<Intent, Model, Mutation, Event>` that lives in a new `core:mvi` Gradle module. The class replicates the Airalo `AiraloViewModel` pattern with one deliberate deviation: `reduce` is `suspend` to support IO in the reducer. The implementation leverages a unified `MutableSharedFlow<Either<Event, Mutation>>` stream split via `filterIsInstance` into two derived flows -- state mutations fed through `scan` + `stateIn` for `StateFlow<Model>`, and events fed through `shareIn` for `SharedFlow<Event>`.

The codebase already uses `androidx.lifecycle.ViewModel` with `koinViewModel()` injection across all 5 existing ViewModels (LoginViewModel, RegisterViewModel, ForgotPasswordViewModel, DashboardViewModel, ProfileViewModel). Arrow `Either` is already a dependency in `core:models`. The new `core:mvi` module needs only `kotlinx-coroutines-core`, `arrow-core`, and `lifecycle-viewmodel-compose` as API dependencies, with `koin-core` as an `implementation` dependency.

The main technical risk is minimal: every library and pattern used is already proven in this codebase. The `scan` operator natively accepts `suspend` lambdas, making the suspend-reduce decision safe. The `filterIsInstance` approach works because Arrow's `Either.Left` and `Either.Right` are both `data class` subtypes, so reified type matching succeeds at runtime.

**Primary recommendation:** Create a single-file `core:mvi` module containing `MviViewModel.kt` (~50-60 lines), following the exact `core:models` module structure as the template for build configuration and KMP target setup.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` | 2.9.6 | Base `ViewModel` class with `viewModelScope` | Already used by all 5 existing ViewModels in this project. Provides KMP-compatible ViewModel with coroutine scope. |
| `io.arrow-kt:arrow-core` | 2.2.1.1 | `Either<L, R>` for unified event/mutation stream | Already an API dependency of `core:models`. `Either.Left`/`Either.Right` are data classes enabling `filterIsInstance`. |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | 1.10.2 | `Flow.scan`, `stateIn`, `shareIn`, `MutableSharedFlow` | Already used across the entire project. Provides the reactive pipeline operators. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `io.insert-koin:koin-core` | 4.1.1 | `implementation` dependency for DI | Only needed if `core:mvi` exports a Koin module. Per user decision, it should NOT export one -- each feature module registers its own ViewModels. Kept as `implementation` to avoid leaking. |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Hand-rolled MviViewModel | Orbit MVI, MVIKotlin | Out of scope per REQUIREMENTS.md: "Template should own its patterns, ~30 LOC base class" |
| SharedFlow for events | Channel for events | User explicitly chose SharedFlow to match reference. Channel would guarantee delivery but requires `consumeAsFlow()`. SharedFlow is fire-and-forget (dropped if no collector). |
| Arrow Either for stream splitting | Sealed class wrapper | Either is already a dependency and `filterIsInstance` works on `Left`/`Right` data classes. Custom wrapper would duplicate existing functionality. |

**Installation (build.gradle.kts for core:mvi):**
```kotlin
commonMain.dependencies {
    api(libs.arrow.core)
    api(libs.kotlinx.coroutines)
    api(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.koin.core)
}
```

## Architecture Patterns

### Recommended Module Structure
```
core/
  mvi/
    build.gradle.kts
    src/
      commonMain/
        kotlin/
          com/m2f/template/core/mvi/
            MviViewModel.kt
      commonTest/
        kotlin/
          com/m2f/template/core/mvi/
            (empty -- tests come in Phase 11)
```

### Pattern 1: Unified Stream with Either Split
**What:** A single `MutableSharedFlow<Either<Event, Mutation>>` receives both events and mutations. Two derived flows split them: `filterIsInstance<Either.Right<Mutation>>` feeds into `scan` for state, `filterIsInstance<Either.Left<Event>>` feeds into `shareIn` for events.
**When to use:** Always -- this is the core MVI wiring pattern.
**Example:**
```kotlin
// Source: Airalo reference + Arrow Either API docs
abstract class MviViewModel<Intent, Model, Mutation, Event>(
    initialState: Model,
    modelSharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5_000),
) : ViewModel() {

    private val pipeline = MutableSharedFlow<Either<Event, Mutation>>()

    val model: StateFlow<Model> by lazy {
        pipeline
            .filterIsInstance<Either.Right<Mutation>>()
            .map { it.value }
            .scan(initialState) { model, mutation -> reduce(model, mutation) }
            .stateIn(viewModelScope, modelSharingStarted, initialState)
    }

    val event: SharedFlow<Event> by lazy {
        pipeline
            .filterIsInstance<Either.Left<Event>>()
            .map { it.value }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())
    }

    abstract fun take(intent: Intent)
    abstract suspend fun reduce(model: Model, mutation: Mutation): Model

    protected suspend fun sendEvent(event: Event) {
        pipeline.emit(Either.Left(event))
    }

    protected suspend fun sendMutation(mutation: Mutation) {
        pipeline.emit(Either.Right(mutation))
    }

    protected suspend fun sendStatement(statement: Either<Event, Mutation>) {
        pipeline.emit(statement)
    }
}
```

### Pattern 2: ViewModel Consumer Pattern (Composable)
**What:** How composables consume an MviViewModel -- unchanged from current codebase pattern.
**When to use:** Every screen that uses an MviViewModel.
**Example:**
```kotlin
// Source: Existing codebase AppNavHost.kt pattern
@Composable
fun SomeScreen() {
    val viewModel = koinViewModel<SomeViewModel>()
    val state by viewModel.model.collectAsStateWithLifecycle()

    // Collect one-shot events
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is SomeViewModel.Event.NavigateTo -> { /* navigate */ }
                is SomeViewModel.Event.ShowToast -> { /* show toast */ }
            }
        }
    }

    // Dispatch intents
    SomeContent(
        state = state,
        onAction = { viewModel.take(SomeViewModel.Intent.DoSomething) },
    )
}
```

### Pattern 3: Nested Sealed Types Convention
**What:** Intent, Model, Mutation, Event defined as nested sealed classes/interfaces inside each ViewModel.
**When to use:** Every ViewModel that extends MviViewModel.
**Example:**
```kotlin
class LoginMviViewModel(
    private val authApi: AuthApi,
) : MviViewModel<LoginMviViewModel.Intent, LoginMviViewModel.Model, LoginMviViewModel.Mutation, LoginMviViewModel.Event>(
    initialState = Model(),
) {
    sealed interface Intent {
        data class EmailChanged(val email: String) : Intent
        data class PasswordChanged(val password: String) : Intent
        data object LoginClicked : Intent
    }

    data class Model(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Mutation {
        data class SetEmail(val email: String) : Mutation
        data class SetPassword(val password: String) : Mutation
        data object Loading : Mutation
        data class Error(val message: String) : Mutation
        data object Success : Mutation
    }

    sealed interface Event {
        data object NavigateToDashboard : Event
    }

    override fun take(intent: Intent) { /* dispatch to viewModelScope */ }
    override suspend fun reduce(model: Model, mutation: Mutation): Model { /* pure transformation */ }
}
```

### Anti-Patterns to Avoid
- **Exposing MutableSharedFlow publicly:** The internal `pipeline` must be `private`. Only `model: StateFlow<Model>` and `event: SharedFlow<Event>` are public.
- **Calling sendMutation/sendEvent outside viewModelScope:** These are `suspend` functions that emit to the shared pipeline. Always call from within `viewModelScope.launch {}` inside `take()`.
- **Making reduce impure with side effects:** Even though `reduce` is `suspend`, it should ideally remain a pure transformation. Use `take()` for side effects (API calls, etc.) and emit mutations as results. The `suspend` capability is a safety valve for edge cases, not an invitation to put business logic in the reducer.
- **Using replay > 0 on events:** Events with replay would re-deliver to new collectors, causing the exact double-fire problem this pattern prevents.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Stream splitting (events vs mutations) | Custom sealed wrapper class | Arrow `Either<Event, Mutation>` + `filterIsInstance` | Arrow is already a dependency; `Either.Left`/`Right` are data classes that work with reified type matching. |
| State accumulation | Manual `MutableStateFlow.update {}` loop | `Flow.scan(initial) { acc, value -> reduce(acc, value) }` | `scan` is the standard Flow accumulator. It handles suspend natively and emits every intermediate state. |
| Hot flow conversion | Manual coroutine collection + MutableStateFlow | `stateIn()` / `shareIn()` | Standard kotlinx.coroutines operators with built-in lifecycle awareness via `SharingStarted`. |
| ViewModel lifecycle scope | Custom `CoroutineScope` management | `viewModelScope` from lifecycle-viewmodel | Automatically cancelled on ViewModel clear. KMP-compatible since lifecycle 2.9.x. |

**Key insight:** The entire MviViewModel implementation is ~50 lines of wiring standard kotlinx.coroutines and Arrow primitives. Every piece already exists in the dependency graph. The value is in the opinionated composition, not custom infrastructure.

## Common Pitfalls

### Pitfall 1: Lazy Flow Initialization Order
**What goes wrong:** If `model` or `event` flows are never collected, the `lazy` delegate never triggers, and emissions to `pipeline` are silently dropped (SharedFlow with 0 replay + 0 extraBufferCapacity).
**Why it happens:** `MutableSharedFlow()` default constructor has `replay=0` and `extraBufferCapacity=0`, meaning `emit()` suspends until a collector is ready.
**How to avoid:** The `lazy` initialization happens when Compose first collects `model`/`event`. As long as the UI collects both flows, this is not an issue. However, if `take()` is called before the UI collects, emissions from `sendMutation`/`sendEvent` will suspend.
**Warning signs:** Intents dispatched during `init {}` appear to be lost. State never updates after the first intent.
**Mitigation:** Consider setting `extraBufferCapacity = 64` on the pipeline `MutableSharedFlow` (matching the Airalo reference) to buffer emissions before collectors are ready. Alternatively, use `BufferOverflow.DROP_OLDEST` if fire-and-forget semantics are acceptable for the pipeline.

### Pitfall 2: Suspend Reduce Serialization
**What goes wrong:** If `reduce` performs slow suspend operations, state updates are serialized through `scan` -- each mutation waits for the previous reduce to complete.
**Why it happens:** `Flow.scan` processes elements sequentially. A slow `suspend reduce` blocks the scan pipeline.
**How to avoid:** Keep `reduce` fast. Move slow operations to `take()`, which launches coroutines independently. Use `reduce` only for state transformations. The `suspend` keyword is a safety valve for rare edge cases (e.g., reading a cached value), not for API calls.
**Warning signs:** UI feels sluggish after rapid user interactions. State updates arrive in batches instead of incrementally.

### Pitfall 3: Event Loss with SharedFlow
**What goes wrong:** Events emitted when no collector is active are permanently lost.
**Why it happens:** `SharedFlow` with `replay=0` drops emissions that have no active collectors. Unlike `Channel`, there is no buffering of undelivered events.
**How to avoid:** This is the intended behavior per user decision. The Composable must collect `event` in a `LaunchedEffect(Unit)` that starts immediately. Do not collect events conditionally or lazily. Document this contract clearly.
**Warning signs:** Navigation events or toasts intermittently fail to appear, especially on quick screen transitions.

### Pitfall 4: WASM Lifecycle Quirk
**What goes wrong:** On WASM, the ViewModel lifecycle skips the `CREATED` state because the application is always attached to the page.
**Why it happens:** Browser-hosted Compose apps have a different lifecycle than Android/iOS/Desktop.
**How to avoid:** `SharingStarted.WhileSubscribed` handles this gracefully because it keys on subscriber count, not lifecycle state. No special handling needed, but be aware during testing.
**Warning signs:** Tests that assert lifecycle state transitions may behave differently on WASM targets.

### Pitfall 5: filterIsInstance with Arrow Either Generics
**What goes wrong:** `filterIsInstance<Either.Right<Mutation>>()` technically matches ALL `Either.Right` instances due to JVM type erasure -- the generic parameter `Mutation` is erased at runtime.
**Why it happens:** JVM erases generic type parameters. `filterIsInstance` checks `is Either.Right` not `is Either.Right<Mutation>`.
**How to avoid:** This is safe in this pattern because the unified `pipeline` flow is typed as `MutableSharedFlow<Either<Event, Mutation>>`. Every `Right` IS a `Right<Mutation>` and every `Left` IS a `Left<Event>` by construction. The erasure does not cause correctness issues because the flow's type parameter guarantees the content types.
**Warning signs:** None in practice -- this is a theoretical concern that does not manifest because the stream is homogeneously typed.

## Code Examples

### Complete MviViewModel Base Class
```kotlin
// Source: Airalo reference pattern adapted per CONTEXT.md decisions
package com.m2f.template.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

abstract class MviViewModel<Intent, Model, Mutation, Event>(
    initialState: Model,
    modelSharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5_000),
) : ViewModel() {

    private val pipeline = MutableSharedFlow<Either<Event, Mutation>>()

    val model: StateFlow<Model> by lazy {
        pipeline
            .filterIsInstance<Either.Right<Mutation>>()
            .map { it.value }
            .scan(initialState) { model, mutation -> reduce(model, mutation) }
            .stateIn(viewModelScope, modelSharingStarted, initialState)
    }

    val event: SharedFlow<Event> by lazy {
        pipeline
            .filterIsInstance<Either.Left<Event>>()
            .map { it.value }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())
    }

    abstract fun take(intent: Intent)

    abstract suspend fun reduce(model: Model, mutation: Mutation): Model

    protected suspend fun sendEvent(event: Event) {
        pipeline.emit(Either.Left(event))
    }

    protected suspend fun sendMutation(mutation: Mutation) {
        pipeline.emit(Either.Right(mutation))
    }

    protected suspend fun sendStatement(statement: Either<Event, Mutation>) {
        pipeline.emit(statement)
    }
}
```

### Module build.gradle.kts
```kotlin
// Source: Adapted from core:models build.gradle.kts pattern in this project
plugins {
    id("kmp-library-convention")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.arrow.core)
            api(libs.kotlinx.coroutines)
            api(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.m2f.template.core.mvi"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
```

### settings.gradle.kts Addition
```kotlin
// Add to existing core module includes
include("core:mvi")
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Android-only `ViewModel` | KMP `lifecycle-viewmodel-compose` | AndroidX Lifecycle 2.9.0 (May 2025) | ViewModel now officially supports Android, iOS, Desktop, WASM via JetBrains fork |
| `LiveData` for state | `StateFlow` + `stateIn` | 2021-2022 migration wave | Flow is the standard for reactive state in KMP; LiveData is Android-only |
| `Channel` for one-shot events | `SharedFlow` (or Channel, debate ongoing) | No consensus date | SharedFlow with replay=0 is fire-and-forget; Channel guarantees delivery. User chose SharedFlow per reference. |
| Moko MVVM / kmp-viewmodel third-party | Official AndroidX ViewModel for KMP | Lifecycle 2.9.0+ (2025) | Third-party ViewModel wrappers are no longer necessary |
| Koin 3.x `viewModel {}` DSL | Koin 4.x `viewModelOf(::Constructor)` | Koin 4.0 (2024) | Constructor-reference DSL is simpler and type-safe |

**Deprecated/outdated:**
- `moko-mvvm`: No longer needed since AndroidX Lifecycle went multiplatform
- `kmp-viewmodel` (hoc081098): Replaced by official AndroidX support
- `LiveData`: Not available in KMP; `StateFlow` is the standard
- Koin `viewModel { MyViewModel(get()) }` lambda DSL: Replaced by `viewModelOf(::MyViewModel)` in Koin 4.x

## Discretion Decisions

### koinMviViewModel() Extension: NOT RECOMMENDED

**Decision:** Rely on standard `koinViewModel<T>()`. Do not create a custom `koinMviViewModel()` extension.

**Rationale:**
1. `MviViewModel` extends `ViewModel()`, so `koinViewModel<T>()` works identically with no changes needed.
2. The existing codebase already uses `viewModelOf(::SomeViewModel)` for registration and `koinViewModel<SomeViewModel>()` for injection. `MviViewModel` subclasses fit this pattern exactly.
3. A custom extension would need to be imported alongside the standard one, creating confusion about which to use.
4. No additional factory logic is needed -- `MviViewModel` subclasses take constructor parameters just like current ViewModels.
5. Adding a wrapper violates the user decision to "match reference API surface exactly -- no extra convenience methods."

### Internal Implementation Details

**Pipeline buffer:** Use `MutableSharedFlow<Either<Event, Mutation>>(extraBufferCapacity = 64)` to prevent `emit()` from suspending when no collectors are ready yet. This matches typical Airalo reference patterns and prevents the lazy-initialization race described in Pitfall 1.

**yield() placement:** Not needed. The `scan` operator processes sequentially within the flow pipeline. Adding `yield()` calls would not improve behavior and could introduce subtle timing issues.

**Coroutine dispatchers:** No explicit dispatcher override. The `viewModelScope` uses `Dispatchers.Main.immediate` on Android and appropriate defaults on other KMP targets. The `stateIn`/`shareIn` operators inherit the scope's dispatcher. This is the standard approach used by the existing 5 ViewModels.

## Open Questions

1. **Success criterion #3 wording update**
   - What we know: The roadmap says "Channel" but user decided "SharedFlow". These have different semantics (Channel guarantees delivery; SharedFlow drops if no collector).
   - What's unclear: Whether the roadmap ROADMAP.md should be updated as part of this phase or deferred.
   - Recommendation: Update the success criterion text in ROADMAP.md during phase execution to say "SharedFlow<Event>" instead of "Channel". This is a documentation-only change aligned with the locked decision.

2. **Koin `implementation` dependency utility**
   - What we know: User decision says Koin is `implementation` only (not exposed). The module does NOT export a Koin module.
   - What's unclear: If Koin is `implementation` and no Koin module is exported, `koin-core` may not be needed at all in `core:mvi`. The only reason to include it would be if `MviViewModel` itself uses Koin internally, which it does not.
   - Recommendation: Omit `koin-core` from `core:mvi` entirely. The base class has no Koin dependency. Feature modules that register ViewModels already depend on `koin-compose-viewmodel`. This simplifies the module.

## Sources

### Primary (HIGH confidence)
- Arrow Either source code (GitHub arrow-kt/arrow) -- confirmed `Left`/`Right` are `data class` subtypes of `sealed class Either`
- [kotlinx.coroutines `Flow.scan` API](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/scan.html) -- confirmed `suspend` accumulator parameter
- [kotlinx.coroutines `SharingStarted.WhileSubscribed`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-sharing-started/-companion/-while-subscribed.html) -- confirmed `stopTimeoutMillis` parameter
- [Koin Compose Multiplatform docs](https://insert-koin.io/docs/reference/koin-compose/compose/) -- confirmed `koinViewModel()` KMP support
- Project codebase analysis (5 existing ViewModels, DI setup, build configuration) -- confirmed patterns and conventions

### Secondary (MEDIUM confidence)
- [JetBrains Lifecycle KMP docs](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-lifecycle.html) -- WASM lifecycle quirk (skips CREATED state)
- [Koin 4.1 release blog](https://blog.kotzilla.io/koin-4.1-is-here) -- confirmed WASM support status (experimental)
- [Android StateFlow/SharedFlow guide](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) -- SharedFlow semantics and best practices

### Tertiary (LOW confidence)
- None -- all findings verified with primary or secondary sources

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- all libraries already in use in this project, versions pinned in `libs.versions.toml`
- Architecture: HIGH -- pattern is well-defined by reference implementation and user decisions; all Flow operators verified
- Pitfalls: HIGH -- pitfalls derived from verified API behavior (SharedFlow replay semantics, scan serialization, JVM type erasure)

**Research date:** 2026-02-18
**Valid until:** 2026-03-18 (stable libraries, locked decisions, low change risk)
