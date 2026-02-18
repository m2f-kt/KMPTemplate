# MVI ViewModel Source Patterns Reference

This document contains the exact source code patterns from the project that the skill must follow. Load this reference when implementing any ViewModel.

## MviViewModel Base Class

**File**: `core/mvi/src/commonMain/kotlin/com/m2f/template/core/mvi/MviViewModel.kt`

```kotlin
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

    private val pipeline = MutableSharedFlow<Either<Event, Mutation>>(extraBufferCapacity = 64)

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
}
```

**Key concepts**:
- 4 generic parameters: `Intent` (user actions), `Model` (UI state), `Mutation` (state changes), `Event` (one-shot side effects)
- Pipeline is `MutableSharedFlow<Either<Event, Mutation>>` — Left = events, Right = mutations
- `model: StateFlow<Model>` — collects mutations via `scan` (reduction)
- `event: SharedFlow<Event>` — collects events (one-shot, not replayed)
- `take(intent)` — entry point for UI to dispatch intents
- `reduce(model, mutation)` — pure function producing new state
- `sendEvent()` / `sendMutation()` — emit into the pipeline from `take()`

## Test DSL

**File**: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/MviViewModelTestDsl.kt`

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
fun <Intent, Model, Mutation, Event> MviViewModel<Intent, Model, Mutation, Event>.test(
    context: CoroutineContext = EmptyCoroutineContext,
    block: ViewModelTestContext<Intent, Model, Mutation, Event>.() -> Unit,
) {
    val ctx = ViewModelTestContext<Intent, Model, Mutation, Event>().apply(block)
    runTest(context) {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        turbineScope {
            val modelTurbine = model.testIn(backgroundScope)
            val eventTurbine = event.testIn(backgroundScope)
            modelTurbine.awaitItem() // Skip initial emission
            for (statement in ctx.statements) {
                when (statement) {
                    is Statement.IntentStatement -> {
                        take(statement.intent)
                        advanceUntilIdle()
                    }
                    is Statement.ModelStatement -> modelTurbine.awaitItem() shouldBe statement.expected
                    is Statement.EventStatement -> eventTurbine.awaitItem() shouldBe statement.expected
                }
            }
            modelTurbine.cancelAndIgnoreRemainingEvents()
            eventTurbine.cancelAndIgnoreRemainingEvents()
        }
    }
}
```

Also available: `scopedTest {}` — suspend variant for setup operations.

## ViewModelTest Base Class

**File**: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/ViewModelTest.kt`

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
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

## ViewModelTestContext (DSL Receiver)

**File**: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/ViewModelTestContext.kt`

```kotlin
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
```

## Statement Sealed Hierarchy

**File**: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/Statement.kt`

```kotlin
sealed interface Statement<out Intent, out Model, out Event> {
    data class IntentStatement<Intent>(val intent: Intent) : Statement<Intent, Nothing, Nothing>
    data class ModelStatement<Model>(val expected: Model) : Statement<Nothing, Model, Nothing>
    data class EventStatement<Event>(val expected: Event) : Statement<Nothing, Nothing, Event>
}
```

## DSL Markers

**File**: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/Annotations.kt`

```kotlin
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
annotation class ViewModelTestDsl

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class FakeSDKDsl
```

## Fake SDK Builder Pattern

**File**: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeAuthApiBuilder.kt`

```kotlin
@FakeSDKDsl
class FakeAuthApiBuilder {
    private var _login: suspend (LoginRequest, Boolean) -> Either<AppError, AuthResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    fun login(behavior: suspend (LoginRequest, Boolean) -> Either<AppError, AuthResponse>) {
        _login = behavior
    }

    // ... other methods follow same pattern

    internal fun build(): AuthApi = object : AuthApi {
        override suspend fun login(request: LoginRequest, rememberMe: Boolean) = _login(request, rememberMe)
        // ...
    }
}

fun fakeAuthApi(block: FakeAuthApiBuilder.() -> Unit = {}): AuthApi =
    FakeAuthApiBuilder().apply(block).build()
```

**Pattern for new fake builders**:
1. Annotate builder class with `@FakeSDKDsl`
2. Each API method has a private lambda field defaulting to `Either.Left(AppError.Client.Unknown())`
3. Public setter function accepts a lambda to override the behavior
4. `internal fun build()` returns an anonymous object implementing the API interface
5. Top-level factory function: `fun fakeFooApi(block: FakeFooApiBuilder.() -> Unit = {}): FooApi`

## FakeSdkBuilder (Facade Composition Pattern)

**File**: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt`

```kotlin
package com.m2f.template.core.testing.fakes

import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.sdk.Sdk

/**
 * DSL builder for creating a fake [Sdk] instance in tests.
 *
 * Composes [FakeAuthApiBuilder] and [FakeUserApiBuilder] so tests configure
 * only the API methods they exercise, while unconfigured paths fail fast.
 *
 * Usage:
 * ```kotlin
 * val sdk = fakeSdk {
 *     auth { login { _, _ -> Either.Right(AuthResponse(...)) } }
 *     user { getProfile { Either.Right(UserResponse(...)) } }
 * }
 * ```
 */
@FakeSDKDsl
class FakeSdkBuilder {

    private var authApiBuilder: FakeAuthApiBuilder = FakeAuthApiBuilder()
    private var userApiBuilder: FakeUserApiBuilder = FakeUserApiBuilder()

    fun auth(init: FakeAuthApiBuilder.() -> Unit) {
        authApiBuilder.init()
    }

    fun user(init: FakeUserApiBuilder.() -> Unit) {
        userApiBuilder.init()
    }

    internal fun build(): Sdk {
        return Sdk(
            authApi = authApiBuilder.build(),
            userApi = userApiBuilder.build(),
        )
    }
}

/**
 * Top-level DSL entry point for creating a fake [Sdk].
 *
 * @param block optional configuration block to override default API behaviors
 * @return a configured [Sdk] instance backed by fake builders
 */
fun fakeSdk(block: FakeSdkBuilder.() -> Unit = {}): Sdk =
    FakeSdkBuilder().apply(block).build()
```

**Key concepts**:
- `FakeSdkBuilder` composes `FakeAuthApiBuilder` and `FakeUserApiBuilder` via DSL delegation
- `build()` constructs a real `Sdk` instance with fake API implementations (Sdk is final, no subclassing)
- `fakeSdk {}` is the ONLY entry point for ViewModel tests -- do NOT use `fakeAuthApi {}` or `fakeUserApi {}` directly
- When adding a new API to Sdk, add a corresponding sub-builder and DSL function to `FakeSdkBuilder`

## Reference Test: CounterViewModel (MviViewModel.test {} DSL)

**File**: `core/testing/src/commonTest/kotlin/com/m2f/template/core/testing/MviViewModelTestDslTest.kt`

```kotlin
class MviViewModelTestDslTest : ViewModelTest() {

    private sealed interface CounterIntent {
        data object Increment : CounterIntent
        data object Decrement : CounterIntent
        data object NotifyDone : CounterIntent
    }

    private data class CounterModel(val count: Int = 0)

    private sealed interface CounterMutation {
        data class SetCount(val count: Int) : CounterMutation
    }

    private sealed interface CounterEvent {
        data object Done : CounterEvent
    }

    private class CounterViewModel : MviViewModel<CounterIntent, CounterModel, CounterMutation, CounterEvent>(
        initialState = CounterModel()
    ) {
        override fun take(intent: CounterIntent) {
            viewModelScope.launch {
                when (intent) {
                    CounterIntent.Increment -> sendMutation(CounterMutation.SetCount(model.value.count + 1))
                    CounterIntent.Decrement -> sendMutation(CounterMutation.SetCount(model.value.count - 1))
                    CounterIntent.NotifyDone -> sendEvent(CounterEvent.Done)
                }
            }
        }

        override suspend fun reduce(model: CounterModel, mutation: CounterMutation): CounterModel =
            when (mutation) {
                is CounterMutation.SetCount -> model.copy(count = mutation.count)
            }
    }

    @Test
    fun `test DSL dispatches intents and asserts model states`() {
        val viewModel = CounterViewModel()
        viewModel.test {
            intent(CounterIntent.Increment)
            model(CounterModel(count = 1))
            intent(CounterIntent.Increment)
            model(CounterModel(count = 2))
            intent(CounterIntent.Decrement)
            model(CounterModel(count = 1))
        }
    }

    @Test
    fun `test DSL asserts events`() {
        val viewModel = CounterViewModel()
        viewModel.test {
            intent(CounterIntent.NotifyDone)
            event(CounterEvent.Done)
        }
    }
}
```

## Reference Test: LoginViewModel (fakeSdk {} pattern)

**File**: `app/auth/src/commonTest/kotlin/com/m2f/template/app/auth/LoginViewModelTest.kt`

This is the canonical example of a ViewModel test using the `fakeSdk {}` DSL. Note that `LoginViewModel` takes `Sdk` (not `AuthApi`) as its dependency, and tests construct it via `fakeSdk { auth { ... } }`.

```kotlin
package com.m2f.template.app.auth

import arrow.core.Either
import app.cash.turbine.test
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AuthResponse
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : ViewModelTest() {

    @Test
    fun `login success updates state with loginSuccess true`() = runTest {
        val sdk = fakeSdk {
            auth {
                login { _, _ ->
                    Either.Right(AuthResponse(accessToken = "test-access", refreshToken = "test-refresh", expiresIn = 3600))
                }
            }
        }
        val viewModel = LoginViewModel(sdk)

        viewModel.state.test {
            awaitItem() shouldBe LoginState()
            viewModel.onEmailChange("user@test.com")
            awaitItem().email shouldBe "user@test.com"
            viewModel.onPasswordChange("password123")
            awaitItem().password shouldBe "password123"

            viewModel.login()
            advanceUntilIdle()

            val loading = awaitItem()
            loading.isLoading shouldBe true

            val success = awaitItem()
            success.loginSuccess shouldBe true
            success.isLoading shouldBe false

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login with blank email shows email error`() = runTest {
        val sdk = fakeSdk() // unconfigured -- won't be called
        val viewModel = LoginViewModel(sdk)

        viewModel.state.test {
            awaitItem()
            viewModel.login()
            val errorState = awaitItem()
            errorState.emailError shouldBe "Email must not be blank"
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login failure shows server error`() = runTest {
        val sdk = fakeSdk {
            auth {
                login { _, _ ->
                    Either.Left(AppError.Auth.InvalidCredentials())
                }
            }
        }
        val viewModel = LoginViewModel(sdk)

        viewModel.state.test {
            awaitItem()
            viewModel.onEmailChange("user@test.com")
            awaitItem()
            viewModel.onPasswordChange("password123")
            awaitItem()

            viewModel.login()
            advanceUntilIdle()

            val loading = awaitItem()
            loading.isLoading shouldBe true

            val error = awaitItem()
            error.serverError shouldBe "Email or password is incorrect"
            error.isLoading shouldBe false

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

**Key patterns**:
- `fakeSdk { auth { login { ... } } }` -- configure only the API methods exercised by the test
- `fakeSdk()` -- no configuration needed when the API won't be called (e.g., validation-only tests)
- `LoginViewModel(sdk)` -- ViewModel always takes `Sdk`, never `AuthApi` directly
- Uses Turbine `test {}` for StateFlow assertions with Kotest `shouldBe`

## Koin DI Wiring

**File**: `composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt`

```kotlin
val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::ProfileViewModel)
}
```

To register a new ViewModel:
1. Add import for the new ViewModel class
2. Add `viewModelOf(::NewFeatureViewModel)` inside the `appModule` block

**Sdk auto-resolution:** `sdkModule` (in `core/sdk/src/commonMain/.../di/SdkModule.kt`) registers `single { Sdk(authApi = get(), userApi = get()) }`. Since all ViewModels take `Sdk` as their constructor parameter, `viewModelOf(::FeatureViewModel)` auto-resolves it from the DI graph -- no explicit `get()` calls needed.

## Build Configuration for Feature Modules

Feature modules under `app/<feature>/` use this pattern in `build.gradle.kts`:

```kotlin
plugins {
    id("kmp-library-convention")
    id("com.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.models)
            implementation(projects.core.sdk)
            implementation(projects.core.mvi)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.arrow.core)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}

android {
    namespace = "com.m2f.template.app.<feature>"
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
