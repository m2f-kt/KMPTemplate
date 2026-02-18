# Phase 11: Testing Infrastructure - Research

**Researched:** 2026-02-18
**Domain:** KMP testing toolkit -- ViewModel test DSL, SDK interface extraction with fakes, shared test fixtures
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Test DSL shape
- Replicate the Airalo `ViewModelTestContext2` pattern exactly, adapted for `MviViewModel<Intent, Model, Mutation, Event>`
- Extension function `.test { }` on MviViewModel -- sequential statement queuing via MutableSharedFlow
- Include both `test` and `scopedTest` variants (coroutine scope access in scopedTest)
- DSL methods: `intent(intent)`, `model(model)`, `event(event)` -- queued as sealed Statement types, processed sequentially
- `verify` block uses Turbine's `.test {}` on both `model` and `event` flows, dispatches intents via `take()`
- Include `@ViewModelTestDsl` DSL marker annotation to prevent scope leaking
- Strip debug println logging -- clean test output (no intent/model/event println statements)

#### SDK interface extraction
- Extract interfaces for ALL SDK API classes, not just AuthApi and UserApi
- Naming: interface gets the clean name (`AuthApi`), concrete implementation gets `Impl` suffix (`AuthApiImpl`)
- Interfaces and implementations live in the same SDK module (no separate contracts module)
- Facade pattern: a single `Sdk` class implements all extracted interfaces using Kotlin `by` delegation
- Koin provides each `*ApiImpl` binding; `Sdk` delegates to them
- Consumers inject specific interfaces (`AuthApi`, `UserApi`) not the Sdk facade directly

#### Fake behavior depth
- DSL builder pattern for fakes -- replicate Airalo's `FakeAuthSDKBuilder` approach
- Each interface method gets a configurable lambda behavior in the builder
- Builder has setter methods for each behavior (e.g., `fun login(behavior: (...) -> Either<ClientError, T>)`)
- `build()` creates an internal implementation that delegates to the builder's lambdas
- `@FakeSDKDsl` marker annotation on builders
- Top-level DSL functions: `fakeAuthApi { login { Either.Right(token) } }` -- clean entry points
- Default behavior for unconfigured methods: return `Either.Left(ClientError.Unknown)` -- tests fail fast on unexpected paths
- SDK fakes (FakeAuthApi, FakeUserApi, etc.) live in core:testing for cross-module reuse
- Module-specific fakes stay in their own module's test sources

#### Assertion style
- Kotest matchers as primary assertion library (`shouldBe`, etc.)
- Use `kotest-extensions-arrow` library for Arrow Either assertions (`shouldBeRight()`, `shouldBeLeft()`) -- if KMP-compatible
- Custom Arrow matchers only as fallback if official extension doesn't support KMP
- core:testing module re-exports kotest + turbine via `api()` -- consumers add single `testImplementation(projects.core.testing)` dependency

### Claude's Discretion
- Exact fake behavior depth (configurable stubs vs stateful) -- Claude picks what makes sense per interface
- Internal test fixture structure within core:testing
- Whether to include test helpers beyond the ViewModel DSL (e.g., coroutine test utilities)

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| TEST-01 | core:testing module provides MVI ViewModel test DSL with Turbine | Verified: Turbine 1.2.1 supports all KMP targets (jvm, ios, wasmJs). The `.test {}` extension on Flow provides `awaitItem()`, `awaitComplete()`, `awaitError()`. The `turbineScope` + `testIn(backgroundScope)` pattern enables testing both `model: StateFlow` and `event: SharedFlow` concurrently. Combined with `kotlinx-coroutines-test` `runTest`, a DSL wrapping these into `intent()` / `model()` / `event()` statement queuing is feasible. |
| TEST-03 | SDK API classes extracted to interfaces for fake substitution in tests | Verified: Only 2 SDK API classes exist currently -- `AuthApi` (6 methods) and `UserApi` (3 methods). Both are concrete classes in `core:sdk`. All methods return `Either<AppError, T>`. Interface extraction is straightforward: extract interface, rename implementation with `Impl` suffix, update Koin bindings. |
| TEST-04 | Hand-written fake implementations exist for SDK contracts | Verified: Each method returns `Either<AppError, T>`, making configurable-lambda fakes natural. Builder DSL pattern with `@FakeSDKDsl` marker annotation is idiomatic Kotlin (per Kotlin type-safe builders docs). Default behavior `Either.Left(AppError.Client.Unknown())` matches existing error hierarchy. |
| TEST-05 | Shared test fixtures and utilities available across modules | Verified: New `core:testing` module can expose Turbine, Kotest assertions, coroutine test utilities, and fake SDK builders via `api()` dependencies. Consumers add single `testImplementation(projects.core.testing)` in their `commonTest.dependencies`. |
| TEST-06 | Kotest assertions work with Arrow Either/Raise in multiplatform tests | Verified: `io.kotest:kotest-assertions-arrow` 6.1.3 published for wasmJs 12 days ago (Feb 2026). Part of main Kotest repo now (moved from separate `kotest-extensions-arrow` repo, which was archived Feb 2025). Provides `shouldBeRight()`, `shouldBeRight(v)`, `shouldBeLeft()`, `shouldBeLeft(v)`. Also `io.kotest:kotest-assertions-arrow-fx-coroutines` 6.1.3 available for wasmJs. |
| MVI-06 | Developer can test ViewModels using a Turbine-based DSL that asserts intent/model/event sequences | Verified: The DSL will use `turbineScope` to create parallel Turbine receivers on `viewModel.model` and `viewModel.event`, then process queued `Statement` items (Intent, ExpectModel, ExpectEvent) sequentially. `Dispatchers.setMain(testDispatcher)` in `@BeforeTest`/`@AfterTest` handles `viewModelScope` in KMP tests. |
</phase_requirements>

<research_summary>
## Summary

Phase 11 builds a reusable `core:testing` module containing three pillars: (1) a Turbine-based ViewModel test DSL, (2) SDK interface extraction with hand-written fake builders, and (3) shared test fixture re-exports (Kotest, Turbine, coroutine-test).

All required libraries exist with verified KMP multiplatform support. Turbine 1.2.1 supports jvm, ios, wasmJs, and was compiled with Kotlin 2.1.21 -- binary-compatible with this project's Kotlin 2.3.10 for JVM/Native targets but needs validation for wasmJs (WASM binary format changed between Kotlin versions). Kotest 6.1.3 (the version already in `libs.versions.toml`) includes `kotest-assertions-arrow` as a first-party module with wasmJs support, compiled with Kotlin 2.2.21. The `kotlinx-coroutines-test` library (matching existing 1.10.2 version) provides `runTest` and `Dispatchers.setMain` for all KMP targets.

The SDK interface extraction scope is small: only `AuthApi` (6 methods) and `UserApi` (3 methods) exist. Both return `Either<AppError, T>`, making fake builder DSL construction straightforward. The `Sdk.kt` file is currently an empty package declaration -- the facade pattern will populate it.

**Primary recommendation:** Create `core:testing` module with Turbine + Kotest + coroutine-test as `api()` dependencies, implement the ViewModel test DSL as a `MviViewModel.test {}` extension function, extract SDK interfaces in `core:sdk`, and place fake builders in `core:testing`. Validate Turbine 1.2.1 compiles against Kotlin 2.3.10 for all targets before proceeding with DSL implementation.
</research_summary>

<standard_stack>
## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `app.cash.turbine:turbine` | 1.2.1 | Flow testing with `awaitItem()`, `.test {}`, `turbineScope` | De facto standard for Kotlin Flow testing. Supports all KMP targets including wasmJs. Built with Kotlin 2.1.21, coroutines 1.10.2. |
| `io.kotest:kotest-assertions-core` | 6.1.3 | Primary assertion library (`shouldBe`, `shouldContain`, etc.) | Already in version catalog. 300+ matchers, KMP multiplatform including wasmJs. Compiled with Kotlin 2.2.21. |
| `io.kotest:kotest-assertions-arrow` | 6.1.3 | Arrow Either matchers (`shouldBeRight()`, `shouldBeLeft()`) | Moved to main Kotest repo (first-party). wasmJs artifact published Feb 2026. Versions match kotest-assertions-core. |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | 1.10.2 | `runTest`, `Dispatchers.setMain`, `TestScope`, `backgroundScope` | Official coroutine testing library. Same version as project's `kotlinx-coroutines`. KMP multiplatform including wasmJs. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `io.kotest:kotest-assertions-arrow-fx-coroutines` | 6.1.3 | Arrow fx-coroutines test matchers | Only if testing `Resource`, `Schedule`, or other arrow-fx types. Already in version catalog as `kotest-arrow-fx`. |
| `org.jetbrains.kotlin:kotlin-test` | 2.3.10 | KMP test annotations (`@Test`, `expect/actual` test infrastructure) | Already used in all modules' `commonTest`. Provides platform-agnostic test entry point. |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Turbine `.test {}` for Flows | `kotlinx-coroutines-test` `runTest` + manual collection | Manual collection is verbose, error-prone with SharedFlow timing, and requires explicit cancellation management. Turbine handles all this. |
| Kotest assertions | kotlin-test assertions (`assertEquals`) | kotlin-test is minimal. Kotest provides `shouldBe`, `shouldBeRight()`, richer error messages, and Arrow integration. |
| Hand-written fakes | MockK / Mockative | User explicitly chose hand-written fakes with DSL builders. Mock libraries add complexity and have inconsistent KMP support (MockK does not support wasmJs). |

### Version Catalog Additions Required

```toml
# In [libraries] section of gradle/libs.versions.toml
turbine = { module = "app.cash.turbine:turbine", version = "1.2.1" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
```

Note: `kotest-assertionsCore`, `kotest-arrow`, and `kotest-arrow-fx` are already in the version catalog. The group ID for kotest-arrow entries may need updating from `io.kotest.extensions` to `io.kotest` since the module moved to the main repo -- **validate during implementation**.
</standard_stack>

<architecture_patterns>
## Architecture Patterns

### Recommended Module Structure
```
core/
  testing/
    build.gradle.kts
    src/
      commonMain/
        kotlin/
          com/m2f/template/core/testing/
            MviViewModelTestDsl.kt        # .test {} / .scopedTest {} extensions
            ViewModelTestContext.kt        # DSL receiver with intent/model/event
            Statement.kt                  # Sealed class for queued statements
            Annotations.kt               # @ViewModelTestDsl, @FakeSDKDsl markers
            fakes/
              FakeAuthApiBuilder.kt       # fakeAuthApi { login { ... } }
              FakeUserApiBuilder.kt       # fakeUserApi { getProfile { ... } }
              FakeTokenStorage.kt         # In-memory token storage for tests
```

```
core/
  sdk/
    src/
      commonMain/
        kotlin/
          com/m2f/template/sdk/
            api/
              AuthApi.kt                  # Interface (extracted)
              AuthApiImpl.kt              # Implementation (renamed from AuthApi)
              UserApi.kt                  # Interface (extracted)
              UserApiImpl.kt              # Implementation (renamed from UserApi)
            Sdk.kt                        # Facade: class Sdk(...) : AuthApi by authApi, UserApi by userApi
            di/
              SdkModule.kt               # Updated: binds *ApiImpl, binds interfaces
```

### Pattern 1: ViewModel Test DSL with Sequential Statement Queuing
**What:** Extension function `.test { }` on MviViewModel that queues `intent()`, `model()`, and `event()` calls as sealed `Statement` types, then processes them sequentially inside a `verify` block backed by Turbine.
**When to use:** Every ViewModel test.
**Example:**
```kotlin
// Source: Adapted from Airalo ViewModelTestContext2 pattern per CONTEXT.md decisions
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
annotation class ViewModelTestDsl

sealed interface Statement<out Intent, out Model, out Event> {
    data class IntentStatement<Intent>(val intent: Intent) : Statement<Intent, Nothing, Nothing>
    data class ModelStatement<Model>(val expected: Model) : Statement<Nothing, Model, Nothing>
    data class EventStatement<Event>(val expected: Event) : Statement<Nothing, Nothing, Event>
}

@ViewModelTestDsl
class ViewModelTestContext<Intent, Model, Mutation, Event> {
    internal val statements = mutableListOf<Statement<Intent, Model, Event>>()

    fun intent(intent: Intent) {
        statements.add(Statement.IntentStatement(intent))
    }

    fun model(expected: Model) {
        statements.add(Statement.ModelStatement(expected))
    }

    fun event(expected: Event) {
        statements.add(Statement.EventStatement(expected))
    }
}

fun <Intent, Model, Mutation, Event> MviViewModel<Intent, Model, Mutation, Event>.test(
    block: ViewModelTestContext<Intent, Model, Mutation, Event>.() -> Unit
) = runTest {
    val context = ViewModelTestContext<Intent, Model, Mutation, Event>().apply(block)

    turbineScope {
        val modelTurbine = model.testIn(backgroundScope)
        val eventTurbine = event.testIn(backgroundScope)

        // Consume initial state
        modelTurbine.awaitItem()

        for (statement in context.statements) {
            when (statement) {
                is Statement.IntentStatement -> take(statement.intent)
                is Statement.ModelStatement -> {
                    val actual = modelTurbine.awaitItem()
                    actual shouldBe statement.expected
                }
                is Statement.EventStatement -> {
                    val actual = eventTurbine.awaitItem()
                    actual shouldBe statement.expected
                }
            }
        }

        modelTurbine.cancelAndIgnoreRemainingEvents()
        eventTurbine.cancelAndIgnoreRemainingEvents()
    }
}
```

### Pattern 2: Fake SDK Builder DSL
**What:** DSL builder that creates fake implementations of SDK interfaces with configurable lambda behaviors per method.
**When to use:** Every test that needs SDK dependencies.
**Example:**
```kotlin
// Source: Adapted from Airalo FakeAuthSDKBuilder pattern per CONTEXT.md decisions
@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class FakeSDKDsl

@FakeSDKDsl
class FakeAuthApiBuilder {
    private var _login: suspend (LoginRequest, Boolean) -> Either<AppError, AuthResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }
    private var _register: suspend (RegisterRequest) -> Either<AppError, AuthResponse> =
        { Either.Left(AppError.Client.Unknown()) }
    private var _logout: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }
    private var _refresh: suspend (RefreshTokenRequest) -> Either<AppError, AuthResponse> =
        { Either.Left(AppError.Client.Unknown()) }
    private var _forgotPassword: suspend (ForgotPasswordRequest) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }
    private var _resetPassword: suspend (ResetPasswordRequest) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    fun login(behavior: suspend (LoginRequest, Boolean) -> Either<AppError, AuthResponse>) {
        _login = behavior
    }

    fun register(behavior: suspend (RegisterRequest) -> Either<AppError, AuthResponse>) {
        _register = behavior
    }

    fun logout(behavior: suspend () -> Either<AppError, Unit>) {
        _logout = behavior
    }

    // ... other setters ...

    internal fun build(): AuthApi = object : AuthApi {
        override suspend fun login(request: LoginRequest, rememberMe: Boolean) =
            _login(request, rememberMe)
        override suspend fun register(request: RegisterRequest) =
            _register(request)
        override suspend fun logout() =
            _logout()
        override suspend fun refresh(request: RefreshTokenRequest) =
            _refresh(request)
        override suspend fun forgotPassword(request: ForgotPasswordRequest) =
            _forgotPassword(request)
        override suspend fun resetPassword(request: ResetPasswordRequest) =
            _resetPassword(request)
    }
}

fun fakeAuthApi(block: FakeAuthApiBuilder.() -> Unit = {}): AuthApi =
    FakeAuthApiBuilder().apply(block).build()
```

### Pattern 3: SDK Interface Extraction
**What:** Extract interface from concrete class, rename implementation with `Impl` suffix, update Koin bindings.
**When to use:** For every SDK API class.
**Example (AuthApi):**
```kotlin
// AuthApi.kt (interface)
interface AuthApi {
    suspend fun register(request: RegisterRequest): Either<AppError, AuthResponse>
    suspend fun login(request: LoginRequest, rememberMe: Boolean = true): Either<AppError, AuthResponse>
    suspend fun refresh(request: RefreshTokenRequest): Either<AppError, AuthResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Either<AppError, Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): Either<AppError, Unit>
    suspend fun logout(): Either<AppError, Unit>
}

// AuthApiImpl.kt (implementation)
class AuthApiImpl(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
) : AuthApi {
    // ... existing implementation unchanged ...
}

// SdkModule.kt (updated bindings)
val sdkModule = module {
    single { AuthInterceptor(tokenStorage = get()) }
    single { createApiClient(authInterceptor = get(), baseUrl = defaultBaseUrl()) }
    single<AuthApi> { AuthApiImpl(client = get(), tokenStorage = get()) }
    single<UserApi> { UserApiImpl(client = get()) }
}
```

### Pattern 4: Sdk Facade with Delegation
**What:** A single `Sdk` class that implements all extracted interfaces using `by` delegation.
**When to use:** When consumers want a single entry point to all SDK functionality (optional -- consumers typically inject specific interfaces).
**Example:**
```kotlin
// Sdk.kt
class Sdk(
    private val authApi: AuthApi,
    private val userApi: UserApi,
) : AuthApi by authApi, UserApi by userApi
```

### Pattern 5: Test Setup with Dispatchers.setMain
**What:** Configure `Dispatchers.Main` for tests so `viewModelScope` works in KMP `commonTest`.
**When to use:** Every test file that creates ViewModels.
**Example:**
```kotlin
// Source: kotlinx-coroutines-test documentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class ViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
```

### Anti-Patterns to Avoid
- **Using MockK/Mockative for fakes:** MockK does not support wasmJs. Hand-written fakes are KMP-safe and match user decision.
- **Testing ViewModels without Dispatchers.setMain:** `viewModelScope` uses `Dispatchers.Main.immediate`. Without `setMain`, tests crash on non-Android targets with "Module with the Main dispatcher is missing."
- **Manual Flow collection in tests:** Leads to timing races with SharedFlow. Always use Turbine.
- **Putting fakes in `core:sdk` test sources:** Cross-module tests cannot access them. Fakes for SDK interfaces go in `core:testing`.
- **Forgetting to consume initial StateFlow emission:** `model: StateFlow` emits `initialState` immediately. The DSL must call `modelTurbine.awaitItem()` before processing statements to skip the initial state.
</architecture_patterns>

<dont_hand_roll>
## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Flow assertion timing | Manual `launch { flow.collect { } }` with delays | Turbine `.test {}` / `awaitItem()` | Turbine handles suspension, timeouts, cancellation. Manual collection causes flaky tests with SharedFlow. |
| Coroutine test infrastructure | Custom `TestCoroutineScope` wrappers | `kotlinx-coroutines-test` `runTest` + `backgroundScope` | `runTest` provides virtual time, eager execution, and proper cleanup. |
| Either assertions | `assertTrue(result.isRight())` + casting | Kotest `shouldBeRight()` / `shouldBeLeft()` | Type-safe extraction, better error messages ("expected Right but was Left(...)"). |
| Multi-flow coordination | Nested `launch` blocks collecting flows | Turbine `turbineScope` + `testIn(backgroundScope)` | `turbineScope` validates all events consumed. `testIn` returns named Turbine instances for parallel flow testing. |
| Test dispatcher setup | Per-test `Dispatchers.setMain` boilerplate | Base `ViewModelTest` class with `@BeforeTest`/`@AfterTest` | Eliminates repetition, prevents forgetting `resetMain()` which leaks state between tests. |

**Key insight:** The ViewModel test DSL is the only custom code in this phase. Everything else (Turbine, Kotest, coroutine-test) is standard library infrastructure that the DSL wraps. The DSL's value is in composing these libraries into a domain-specific `intent()` -> `model()` -> `event()` vocabulary -- not in replacing any of them.
</dont_hand_roll>

<common_pitfalls>
## Common Pitfalls

### Pitfall 1: Turbine + Kotlin Version Mismatch on wasmJs
**What goes wrong:** Turbine 1.2.1 was compiled with Kotlin 2.1.21. This project uses Kotlin 2.3.10. For JVM and Native targets, Kotlin maintains binary compatibility. For wasmJs, the WASM binary format may have changed between versions, causing compilation or linking failures.
**Why it happens:** WASM/JS targets use klib format where binary compatibility is less guaranteed across Kotlin versions than JVM class files.
**How to avoid:** Run `./gradlew :core:testing:wasmJsTest` as the first validation step before writing any DSL code. If it fails, either: (a) pin to Turbine 1.3.0-SNAPSHOT which may target a newer Kotlin, or (b) exclude wasmJs from `core:testing` test targets and revisit when Turbine updates.
**Warning signs:** Build errors like "Module was compiled with an incompatible version of Kotlin" or wasm linking failures.

### Pitfall 2: StateFlow Initial Emission in Test DSL
**What goes wrong:** Tests fail because the first `model()` assertion receives `initialState` instead of the expected post-intent state.
**Why it happens:** `StateFlow` created by `stateIn()` immediately emits its initial value. Turbine's `.testIn()` captures this emission.
**How to avoid:** The DSL's `verify` block must consume the initial state emission with `modelTurbine.awaitItem()` before processing any user-defined statements. Do NOT expose this to the test author -- handle it inside the DSL.
**Warning signs:** First `model()` assertion always fails with "expected X but was InitialState."

### Pitfall 3: SharedFlow Event Timing
**What goes wrong:** `event()` assertion in the DSL never receives the event, causing a Turbine timeout.
**Why it happens:** `event: SharedFlow` with replay=0 drops events if no collector is active. If the DSL dispatches an intent and the event is emitted before the event Turbine is collecting, the event is lost.
**How to avoid:** `turbineScope` + `testIn(backgroundScope)` ensures both model and event Turbines are actively collecting BEFORE any intents are dispatched. The DSL must set up both collectors before processing the first `IntentStatement`.
**Warning signs:** Tests that assert events after intents timeout intermittently.

### Pitfall 4: Dispatchers.Main Missing in commonTest
**What goes wrong:** ViewModel creation crashes with "Module with the Main dispatcher is missing."
**Why it happens:** `viewModelScope` uses `Dispatchers.Main.immediate`. On JVM/Native test targets, no Main dispatcher is registered by default.
**How to avoid:** Every test that creates a ViewModel must call `Dispatchers.setMain(testDispatcher)` in `@BeforeTest`. Provide a `ViewModelTest` base class in `core:testing` that handles this automatically.
**Warning signs:** `IllegalStateException: Module with the Main dispatcher is missing` on first ViewModel test.

### Pitfall 5: Koin Binding Regression After Interface Extraction
**What goes wrong:** Runtime crashes in the app because Koin cannot resolve `AuthApi` or `UserApi`.
**Why it happens:** After extracting interfaces, Koin bindings must change from `single { AuthApi(...) }` to `single<AuthApi> { AuthApiImpl(...) }`. If the type qualifier is missing, Koin registers `AuthApiImpl` (the concrete class) but consumers inject `AuthApi` (the interface).
**How to avoid:** Update `SdkModule.kt` with explicit interface type: `single<AuthApi> { AuthApiImpl(...) }`. Run the app's Koin graph validation after the change.
**Warning signs:** `NoBeanDefFoundException: No definition found for class 'AuthApi'` at runtime.

### Pitfall 6: Default Fake Behavior Confusion
**What goes wrong:** Tests pass when they should fail because a fake method was called but returned `Either.Left(Unknown)` silently.
**Why it happens:** Default behavior returns `Either.Left(AppError.Client.Unknown())`. If a test forgets to configure a method that gets called, the fake returns an error instead of crashing.
**How to avoid:** This is intentional -- it causes assertion failures downstream (the ViewModel will enter an error state, which the test will detect). Document this behavior clearly. Consider adding a `strict()` mode option that throws `NotImplementedError` for unconfigured methods as an alternative.
**Warning signs:** Tests that assert on error paths pass unexpectedly because the fake returned an unconfigured error.
</common_pitfalls>

<code_examples>
## Code Examples

### Complete core:testing build.gradle.kts
```kotlin
// Source: Adapted from core:mvi/build.gradle.kts pattern
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
            // Re-export test libraries so consumers get everything from one dependency
            api(libs.turbine)
            api(libs.kotest.assertionsCore)
            api(libs.kotest.arrow)
            api(libs.kotlinx.coroutines.test)
            api(libs.kotlin.test)

            // Internal dependencies for the DSL
            implementation(projects.core.mvi)
            implementation(projects.core.sdk)
            implementation(projects.core.models)
            implementation(libs.arrow.core)
            implementation(libs.kotlinx.coroutines)
        }
    }
}

android {
    namespace = "com.m2f.template.core.testing"
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

### Complete ViewModel Test DSL (MviViewModelTestDsl.kt)
```kotlin
// Source: Adapted from Airalo ViewModelTestContext2 reference pattern
package com.m2f.template.core.testing

import app.cash.turbine.turbineScope
import com.m2f.template.core.mvi.MviViewModel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
annotation class ViewModelTestDsl

sealed interface Statement<out Intent, out Model, out Event> {
    data class IntentStatement<Intent>(val intent: Intent) : Statement<Intent, Nothing, Nothing>
    data class ModelStatement<Model>(val expected: Model) : Statement<Nothing, Model, Nothing>
    data class EventStatement<Event>(val expected: Event) : Statement<Nothing, Nothing, Event>
}

@ViewModelTestDsl
class ViewModelTestContext<Intent, Model, Mutation, Event> {
    internal val statements = mutableListOf<Statement<Intent, Model, Event>>()

    fun intent(intent: Intent) {
        statements.add(Statement.IntentStatement(intent))
    }

    fun model(expected: Model) {
        statements.add(Statement.ModelStatement(expected))
    }

    fun event(expected: Event) {
        statements.add(Statement.EventStatement(expected))
    }
}

/**
 * Test an MviViewModel by queuing intent/model/event expectations.
 * Statements are processed sequentially -- Turbine verifies flows under the hood.
 *
 * Usage:
 * ```
 * loginViewModel.test {
 *     intent(Intent.EmailChanged("user@test.com"))
 *     model(Model(email = "user@test.com"))
 *     intent(Intent.PasswordChanged("pass123"))
 *     model(Model(email = "user@test.com", password = "pass123"))
 *     intent(Intent.LoginClicked)
 *     model(Model(email = "user@test.com", password = "pass123", isLoading = true))
 *     event(Event.NavigateToDashboard)
 *     model(Model(email = "user@test.com", password = "pass123", isLoading = false, loginSuccess = true))
 * }
 * ```
 */
fun <Intent, Model, Mutation, Event> MviViewModel<Intent, Model, Mutation, Event>.test(
    context: CoroutineContext = EmptyCoroutineContext,
    block: ViewModelTestContext<Intent, Model, Mutation, Event>.() -> Unit,
) = runTest(context) {
    val testContext = ViewModelTestContext<Intent, Model, Mutation, Event>().apply(block)

    turbineScope {
        val modelTurbine = model.testIn(backgroundScope)
        val eventTurbine = event.testIn(backgroundScope)

        // Skip initial state emission from StateFlow
        modelTurbine.awaitItem()

        for (statement in testContext.statements) {
            when (statement) {
                is Statement.IntentStatement -> take(statement.intent)
                is Statement.ModelStatement -> {
                    modelTurbine.awaitItem() shouldBe statement.expected
                }
                is Statement.EventStatement -> {
                    eventTurbine.awaitItem() shouldBe statement.expected
                }
            }
        }

        modelTurbine.cancelAndIgnoreRemainingEvents()
        eventTurbine.cancelAndIgnoreRemainingEvents()
    }
}

/**
 * Variant with coroutine scope access for advanced test scenarios.
 */
fun <Intent, Model, Mutation, Event> MviViewModel<Intent, Model, Mutation, Event>.scopedTest(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend ViewModelTestContext<Intent, Model, Mutation, Event>.() -> Unit,
) = runTest(context) {
    val testContext = ViewModelTestContext<Intent, Model, Mutation, Event>()
    testContext.block()

    turbineScope {
        val modelTurbine = model.testIn(backgroundScope)
        val eventTurbine = event.testIn(backgroundScope)

        modelTurbine.awaitItem()

        for (statement in testContext.statements) {
            when (statement) {
                is Statement.IntentStatement -> take(statement.intent)
                is Statement.ModelStatement -> {
                    modelTurbine.awaitItem() shouldBe statement.expected
                }
                is Statement.EventStatement -> {
                    eventTurbine.awaitItem() shouldBe statement.expected
                }
            }
        }

        modelTurbine.cancelAndIgnoreRemainingEvents()
        eventTurbine.cancelAndIgnoreRemainingEvents()
    }
}
```

### FakeAuthApiBuilder (complete)
```kotlin
// Source: Adapted from Airalo FakeAuthSDKBuilder reference pattern
package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.*
import com.m2f.template.sdk.api.AuthApi

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class FakeSDKDsl

@FakeSDKDsl
class FakeAuthApiBuilder {
    private var _register: suspend (RegisterRequest) -> Either<AppError, AuthResponse> =
        { Either.Left(AppError.Client.Unknown()) }
    private var _login: suspend (LoginRequest, Boolean) -> Either<AppError, AuthResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }
    private var _refresh: suspend (RefreshTokenRequest) -> Either<AppError, AuthResponse> =
        { Either.Left(AppError.Client.Unknown()) }
    private var _forgotPassword: suspend (ForgotPasswordRequest) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }
    private var _resetPassword: suspend (ResetPasswordRequest) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }
    private var _logout: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    fun register(behavior: suspend (RegisterRequest) -> Either<AppError, AuthResponse>) {
        _register = behavior
    }

    fun login(behavior: suspend (LoginRequest, Boolean) -> Either<AppError, AuthResponse>) {
        _login = behavior
    }

    fun refresh(behavior: suspend (RefreshTokenRequest) -> Either<AppError, AuthResponse>) {
        _refresh = behavior
    }

    fun forgotPassword(behavior: suspend (ForgotPasswordRequest) -> Either<AppError, Unit>) {
        _forgotPassword = behavior
    }

    fun resetPassword(behavior: suspend (ResetPasswordRequest) -> Either<AppError, Unit>) {
        _resetPassword = behavior
    }

    fun logout(behavior: suspend () -> Either<AppError, Unit>) {
        _logout = behavior
    }

    internal fun build(): AuthApi = object : AuthApi {
        override suspend fun register(request: RegisterRequest) = _register(request)
        override suspend fun login(request: LoginRequest, rememberMe: Boolean) = _login(request, rememberMe)
        override suspend fun refresh(request: RefreshTokenRequest) = _refresh(request)
        override suspend fun forgotPassword(request: ForgotPasswordRequest) = _forgotPassword(request)
        override suspend fun resetPassword(request: ResetPasswordRequest) = _resetPassword(request)
        override suspend fun logout() = _logout()
    }
}

fun fakeAuthApi(block: FakeAuthApiBuilder.() -> Unit = {}): AuthApi =
    FakeAuthApiBuilder().apply(block).build()
```

### FakeTokenStorage (in-memory)
```kotlin
package com.m2f.template.core.testing.fakes

import com.m2f.template.storage.TokenStorage

/**
 * In-memory TokenStorage for tests. No Settings dependency needed.
 * Note: TokenStorage is a concrete class, so this may need an
 * interface extraction or a constructor-based approach.
 */
class FakeTokenStorage : /* TokenStorage interface if extracted */ {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var sessionOnly: Boolean = false

    fun getAccessToken(): String? = accessToken
    fun getRefreshToken(): String? = refreshToken

    fun saveTokens(accessToken: String, refreshToken: String, rememberMe: Boolean = true) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.sessionOnly = !rememberMe
    }

    fun clearTokens() {
        accessToken = null
        refreshToken = null
        sessionOnly = false
    }

    fun isSessionOnly(): Boolean = sessionOnly
}
```

### Example Test Using the DSL
```kotlin
// Source: Phase success criterion -- LoginViewModel test
package com.m2f.template.app.auth

import arrow.core.Either
import com.m2f.template.core.testing.fakes.fakeAuthApi
import com.m2f.template.core.testing.test
import com.m2f.template.models.dto.AuthResponse
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LoginMviViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success emits navigate event`() {
        val authApi = fakeAuthApi {
            login { _, _ ->
                Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref"))
            }
        }
        val viewModel = LoginMviViewModel(authApi)

        viewModel.test {
            intent(LoginMviViewModel.Intent.EmailChanged("user@test.com"))
            model(LoginMviViewModel.Model(email = "user@test.com"))
            intent(LoginMviViewModel.Intent.PasswordChanged("password"))
            model(LoginMviViewModel.Model(email = "user@test.com", password = "password"))
            intent(LoginMviViewModel.Intent.LoginClicked)
            model(LoginMviViewModel.Model(email = "user@test.com", password = "password", isLoading = true))
            event(LoginMviViewModel.Event.NavigateToDashboard)
        }
    }
}
```

### Version Catalog Additions (libs.versions.toml)
```toml
# Add to [libraries] section:
turbine = { module = "app.cash.turbine:turbine", version = "1.2.1" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
```
</code_examples>

<sota_updates>
## State of the Art (2025-2026)

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `kotest-extensions-arrow` (separate repo, `io.kotest.extensions` group) | `kotest-assertions-arrow` in main kotest repo (`io.kotest` group) | Feb 2025 (repo archived) | Versions now match kotest-core. No separate version management. |
| MockK for KMP test doubles | Hand-written fakes with DSL builders | N/A (pattern choice) | MockK does not support wasmJs. Fakes are KMP-safe and predictable. |
| Turbine `turbineScope` mandatory wrapper | `turbineScope` still required for multi-flow | Turbine 1.0.0 (Jun 2023) | No change -- `turbineScope` remains the standard pattern. |
| `TestCoroutineDispatcher` (deprecated) | `StandardTestDispatcher` + `runTest` | kotlinx-coroutines 1.6.0 | `TestCoroutineDispatcher` removed. `runTest` is the standard entry point. |
| Separate `kotest-assertions-arrow` version management | Unified version with Kotest 6.x | Kotest 6.0 (2025) | Single version (`6.1.3`) for all kotest assertions including Arrow. |

**New tools/patterns:**
- **Kotest 6.1.3:** Minimum JDK 11, Kotlin 2.2+. Arrow assertions are first-party modules.
- **Turbine 1.2.1:** wasmJs + wasmWasi support. Full KMP target coverage.

**Deprecated/outdated:**
- `io.kotest.extensions:kotest-assertions-arrow` (old group ID): Archived. Use `io.kotest:kotest-assertions-arrow`.
- `TestCoroutineDispatcher` / `TestCoroutineScope`: Use `StandardTestDispatcher()` + `runTest`.
- MockK for KMP: Does not support wasmJs target. Use hand-written fakes.
</sota_updates>

<open_questions>
## Open Questions

1. **Turbine 1.2.1 + Kotlin 2.3.10 wasmJs compatibility**
   - What we know: Turbine 1.2.1 was compiled with Kotlin 2.1.21. Kotest 6.1.3 was compiled with Kotlin 2.2.21. This project uses Kotlin 2.3.10. JVM binary compatibility is maintained. Native/WASM klib compatibility across Kotlin minor versions is not guaranteed.
   - What's unclear: Whether wasmJs klib format from Kotlin 2.1.21/2.2.21 is forward-compatible with Kotlin 2.3.10 compilation.
   - Recommendation: First task in Phase 11 should be a validation spike: add Turbine + Kotest to `core:testing` with wasmJs target enabled and run `./gradlew :core:testing:wasmJsTest`. If wasmJs fails, options: (a) use Turbine 1.3.0-SNAPSHOT, (b) exclude wasmJs from `core:testing`, (c) wait for Turbine update.

2. **TokenStorage is a concrete class -- needs interface for faking?**
   - What we know: `TokenStorage` in `core:storage` is a concrete class that takes `Settings` in its constructor. The `AuthApi` and `AuthInterceptor` depend on it. Faking it in tests either requires interface extraction or a constructor-based approach (pass a `MapSettings` or in-memory `Settings`).
   - What's unclear: Whether to extract a `TokenStorage` interface (like SDK APIs) or use multiplatform-settings `MapSettings()` as a test-friendly constructor arg.
   - Recommendation: Use `MapSettings()` from the multiplatform-settings library (already a dependency) to construct `TokenStorage` in tests. This avoids interface extraction for TokenStorage while keeping fakes simple. If this doesn't work, extract a `TokenStorage` interface as a fallback.

3. **kotest-assertions-arrow group ID transition**
   - What we know: The version catalog currently has `kotest-arrow = { module = "io.kotest:kotest-assertions-arrow", ... }` and `kotest-arrow-fx = { module = "io.kotest:kotest-assertions-arrow-fx-coroutines", ... }` which already use the new `io.kotest` group. These are in the `testing-server` bundle.
   - What's unclear: Whether the existing catalog entries resolve correctly for KMP targets (they may only resolve JVM artifacts if the module coordinates are wrong).
   - Recommendation: Validate that `implementation(libs.kotest.arrow)` resolves the correct multiplatform artifact in a `commonMain` source set. If not, may need to adjust the catalog entry to the multiplatform root module.

4. **KTOR-7121: Ktor testApplication dispatcher issue**
   - What we know: Flagged in STATE.md as a known blocker/concern for Phase 11.
   - What's unclear: The exact nature of the issue and whether it affects this phase. Phase 11 focuses on ViewModel testing and SDK fakes, not Ktor server testing.
   - Recommendation: This issue likely affects server-side integration tests, not the client-side ViewModel/SDK testing that Phase 11 covers. Defer to a future server testing phase. Document as out of scope.
</open_questions>

<sources>
## Sources

### Primary (HIGH confidence)
- [Turbine GitHub README](https://github.com/cashapp/turbine) - `.test {}` API, `turbineScope`, `testIn`, `awaitItem()` patterns, KMP target list
- [Turbine CHANGELOG](https://github.com/cashapp/turbine/blob/trunk/CHANGELOG.md) - Version history, wasmJs added in 1.1.0, wasmWasi added in 1.2.0
- [Turbine releases](https://github.com/cashapp/turbine/releases) - 1.2.1 release date (Jun 2024), build with Kotlin 1.8.22/coroutines 1.7.1 (1.0.0 era)
- [Maven Central: turbine-wasm-js](https://central.sonatype.com/artifact/app.cash.turbine/turbine-wasm-js) - Confirmed 1.2.1 published, compiled with Kotlin 2.1.21, coroutines 1.10.2
- [Kotest Arrow docs](https://kotest.io/docs/assertions/arrow.html) - `shouldBeRight()`, `shouldBeLeft()` API, `io.kotest:kotest-assertions-arrow` module coordinate
- [Maven Central: kotest-assertions-arrow-wasm-js](https://central.sonatype.com/artifact/io.kotest/kotest-assertions-arrow-wasm-js) - 6.1.3 published (Feb 2026)
- [Maven Central: kotest-assertions-core-wasm-js](https://central.sonatype.com/artifact/io.kotest/kotest-assertions-core-wasm-js) - 6.1.3, compiled with Kotlin 2.2.21
- [Maven Central: kotest-assertions-arrow-fx-coroutines-wasm-js](https://central.sonatype.com/artifact/io.kotest/kotest-assertions-arrow-fx-coroutines-wasm-js) - 6.1.3 published (Feb 2026)
- [Kotlin type-safe builders](https://kotlinlang.org/docs/type-safe-builders.html) - `@DslMarker` pattern for scope control
- Project codebase analysis - MviViewModel.kt, AuthApi.kt, UserApi.kt, SdkModule.kt, libs.versions.toml, build.gradle.kts files

### Secondary (MEDIUM confidence)
- [kotest-extensions-arrow archived repo](https://github.com/kotest/kotest-extensions-arrow) - Confirmed archived Feb 2025, moved to main kotest repo
- [kotlinx-coroutines-test docs](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) - `runTest`, `Dispatchers.setMain` API
- [Unit tests in KMP for ViewModel](https://medium.com/@olena.kramarenko/unit-tests-in-kmp-for-viewmodel-with-coroutines-flows-and-savedstatehandle-5035dc586936) - `Dispatchers.setMain` pattern for KMP ViewModel testing

### Tertiary (LOW confidence - needs validation)
- Turbine 1.2.1 + Kotlin 2.3.10 wasmJs binary compatibility: Not verified. Turbine compiled with 2.1.21, project uses 2.3.10. JVM safe, wasmJs uncertain. **Must validate in first implementation task.**
- Kotest 6.1.3 + Kotlin 2.3.10 wasmJs binary compatibility: Kotest compiled with 2.2.21, closer to 2.3.10 but still uncertain for wasmJs. **Must validate alongside Turbine.**
</sources>

## Discretion Decisions

### Fake Behavior Depth: Configurable Stubs (not stateful)
**Decision:** Use configurable-lambda stubs (not stateful fakes with internal state machines).

**Rationale:**
1. The SDK API interfaces are simple request/response -- no stateful session management at the API level.
2. Configurable lambdas let each test define exactly the behavior it needs without managing internal fake state.
3. Stateful fakes (e.g., "after login, getProfile returns the logged-in user") add complexity without clear benefit at this stage.
4. If stateful behavior is needed for a specific test, the test author can use mutable variables inside the lambda closures.

### Internal Test Fixture Structure
**Decision:** Flat package structure under `com.m2f.template.core.testing` with `fakes/` sub-package.

**Rationale:**
1. The module is small (DSL + 2-3 fake builders + annotations + base class).
2. Nested packages add navigation overhead without organizational benefit at this scale.
3. `fakes/` sub-package separates fake builders from the DSL infrastructure.

### Coroutine Test Utilities
**Decision:** Include a `ViewModelTest` abstract base class with `Dispatchers.setMain`/`resetMain` setup.

**Rationale:**
1. Every ViewModel test needs `Dispatchers.setMain` -- boilerplate that is easy to forget.
2. A base class eliminates this repetition and prevents `resetMain` being forgotten (which leaks state between tests).
3. The base class is optional -- tests can still use `@BeforeTest`/`@AfterTest` directly if they prefer.

<metadata>
## Metadata

**Research scope:**
- Core technology: Turbine 1.2.1, Kotest 6.1.3, kotlinx-coroutines-test 1.10.2
- Ecosystem: kotest-assertions-arrow (now in main kotest repo), kotlin-test, Arrow Either
- Patterns: ViewModel test DSL with sequential statement queuing, fake builder DSL with @DslMarker, interface extraction with Koin rebinding
- Pitfalls: Kotlin version compatibility on wasmJs, StateFlow initial emission, SharedFlow timing, Dispatchers.Main in tests

**Confidence breakdown:**
- Standard stack: HIGH - All libraries verified on Maven Central with KMP wasmJs artifacts. Versions confirmed.
- Architecture: HIGH - DSL patterns derived from locked user decisions with verified Turbine/Kotest APIs. SDK extraction scope is small and well-understood.
- Pitfalls: HIGH - Pitfalls derived from verified API behavior. wasmJs compatibility flagged as LOW confidence open question.
- Code examples: MEDIUM - Code is synthesized from verified APIs and locked decisions. Not copy-pasted from running tests. Patterns are sound but implementation details may need adjustment.

**Research date:** 2026-02-18
**Valid until:** 2026-03-18 (stable libraries, locked decisions)
</metadata>

---

*Phase: 11-testing-infrastructure*
*Research completed: 2026-02-18*
*Ready for planning: yes*
