---
name: mvi-viewmodel
description: This skill should be used when creating, implementing, or testing ViewModels using the MVI (Model-View-Intent) pattern. It activates proactively when Claude detects intent to build a new screen feature, add a ViewModel, or write ViewModel tests. It enforces a TDD workflow (tests first) using the project's MviViewModel base class and custom test DSL.
---

# MVI ViewModel Skill

Guide for creating and testing ViewModels using the project's `MviViewModel<Intent, Model, Mutation, Event>` base class with a strict test-driven development workflow.

## When This Skill Triggers

**Proactive activation** — trigger when detecting any of these intents:
- Creating a new screen/feature (e.g., "add a settings screen", "build the cart feature")
- Explicitly creating a ViewModel (e.g., "create a ViewModel for notifications")
- Writing or adding ViewModel tests (e.g., "write tests for the ProfileViewModel")
- Refactoring an existing ViewModel to use MVI pattern

**Do NOT trigger for:**
- Server-side work (Ktor routes, database migrations, API endpoints)
- Compose-only UI changes (layouts, themes, design system components)
- SDK/API implementation (Ktor client, interceptors)
- Build configuration or dependency changes

## TDD Workflow (Strict Order)

Every ViewModel follows this exact sequence. Do not skip steps or reorder.

### Step 1: Define Types

Create the four type files in `app/<feature>/src/commonMain/kotlin/com/m2f/template/app/<feature>/`:

1. **`<Feature>Intent.kt`** — sealed interface for user actions
2. **`<Feature>Model.kt`** — data class for UI state (with sensible defaults)
3. **`<Feature>Mutation.kt`** — sealed interface for state changes
4. **`<Feature>Event.kt`** — sealed interface for one-shot side effects

Pattern for each:

```kotlin
// <Feature>Intent.kt
package com.m2f.template.app.<feature>

sealed interface <Feature>Intent {
    data object LoadData : <Feature>Intent
    data class UpdateField(val value: String) : <Feature>Intent
}
```

```kotlin
// <Feature>Model.kt
package com.m2f.template.app.<feature>

data class <Feature>Model(
    val isLoading: Boolean = false,
    val data: String = "",
    val error: String? = null,
)
```

```kotlin
// <Feature>Mutation.kt
package com.m2f.template.app.<feature>

sealed interface <Feature>Mutation {
    data object Loading : <Feature>Mutation
    data class DataLoaded(val data: String) : <Feature>Mutation
    data class Error(val message: String) : <Feature>Mutation
}
```

```kotlin
// <Feature>Event.kt
package com.m2f.template.app.<feature>

sealed interface <Feature>Event {
    data object NavigateBack : <Feature>Event
    data class ShowToast(val message: String) : <Feature>Event
}
```

### Step 2: Write Tests FIRST

Create the test file at `app/<feature>/src/commonTest/kotlin/com/m2f/template/app/<feature>/<Feature>ViewModelTest.kt` **before** implementing the ViewModel.

The test class extends `ViewModelTest` and uses the `MviViewModel.test {}` DSL:

```kotlin
package com.m2f.template.app.<feature>

import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import kotlin.test.Test

class <Feature>ViewModelTest : ViewModelTest() {

    @Test
    fun `load data success updates model with loaded data`() {
        val sdk = fakeSdk {
            auth { /* configure auth methods if needed */ }
            user { /* configure user methods if needed */ }
        }
        val viewModel = <Feature>ViewModel(sdk)
        viewModel.test {
            intent(<Feature>Intent.LoadData)
            model(<Feature>Model(isLoading = true))
            model(<Feature>Model(isLoading = false, data = "expected"))
        }
    }

    @Test
    fun `load data failure shows error in model`() {
        val sdk = fakeSdk {
            auth {
                getData { Either.Left(AppError.Server.Internal("Something went wrong")) }
            }
        }
        val viewModel = <Feature>ViewModel(sdk)
        viewModel.test {
            intent(<Feature>Intent.LoadData)
            model(<Feature>Model(isLoading = true))
            model(<Feature>Model(isLoading = false, error = "Something went wrong"))
        }
    }

    @Test
    fun `event is emitted for navigation`() {
        val sdk = fakeSdk()
        val viewModel = <Feature>ViewModel(sdk)
        viewModel.test {
            intent(<Feature>Intent.SomeAction)
            event(<Feature>Event.NavigateBack)
        }
    }
}
```

**Test DSL rules:**
- `intent(...)` — dispatches an intent and calls `advanceUntilIdle()` automatically
- `model(...)` — asserts the next model emission equals the expected value (uses Kotest `shouldBe`)
- `event(...)` — asserts the next event emission equals the expected value
- Statements execute sequentially — order matters
- Initial StateFlow emission is consumed automatically (no need to skip it)

**Dependency faking:**
- ViewModels ALWAYS take `Sdk` as their dependency (not sub-APIs like AuthApi/UserApi directly)
- Use `fakeSdk { auth { ... }; user { ... } }` to create a fake Sdk for tests
- Configure only the sub-API methods exercised by the test; unconfigured paths fail fast with `AppError.Client.Unknown()`
- Do NOT use `fakeAuthApi {}` or `fakeUserApi {}` directly in ViewModel tests -- always go through `fakeSdk {}`

### Step 3: Implement ViewModel

Create `app/<feature>/src/commonMain/kotlin/com/m2f/template/app/<feature>/<Feature>ViewModel.kt`:

```kotlin
package com.m2f.template.app.<feature>

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class <Feature>ViewModel(
    private val sdk: Sdk,
) : MviViewModel<<Feature>Intent, <Feature>Model, <Feature>Mutation, <Feature>Event>(
    initialState = <Feature>Model()
) {

    override fun take(intent: <Feature>Intent) {
        viewModelScope.launch {
            when (intent) {
                is <Feature>Intent.LoadData -> {
                    sendMutation(<Feature>Mutation.Loading)
                    // Call API through Sdk facade, handle result with Arrow Either
                    sdk.getData().fold(
                        ifLeft = { error -> sendMutation(<Feature>Mutation.Error(error.message)) },
                        ifRight = { data -> sendMutation(<Feature>Mutation.DataLoaded(data)) },
                    )
                }
                // Handle other intents...
            }
        }
    }

    override suspend fun reduce(model: <Feature>Model, mutation: <Feature>Mutation): <Feature>Model =
        when (mutation) {
            is <Feature>Mutation.Loading -> model.copy(isLoading = true, error = null)
            is <Feature>Mutation.DataLoaded -> model.copy(isLoading = false, data = mutation.data)
            is <Feature>Mutation.Error -> model.copy(isLoading = false, error = mutation.message)
        }
}
```

**Implementation rules:**
- Always extend `MviViewModel<Intent, Model, Mutation, Event>` with the 4 types
- Constructor takes `sdk: Sdk` as its single dependency (Sdk facade delegates to all sub-APIs)
- Use `viewModelScope.launch` inside `take()` for async work
- Call API methods directly on `sdk` (e.g., `sdk.login(...)`, `sdk.getProfile(...)`)
- Use Arrow `Either.fold()` for API result handling
- `sendMutation()` for state changes, `sendEvent()` for one-shot effects
- `reduce()` must be a pure function -- no side effects, no API calls
- Constructor injection resolved by Koin (Sdk provided by sdkModule)

### Step 4: Wire Koin DI

Add the ViewModel to `composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt`:

1. Add import: `import com.m2f.template.app.<feature>.<Feature>ViewModel`
2. Add registration inside `appModule`: `viewModelOf(::<Feature>ViewModel)`

Koin auto-resolves the `Sdk` dependency from `sdkModule`. No special wiring needed beyond `viewModelOf(::FeatureViewModel)`.

### Step 5: Run Tests

Execute the test suite to verify the TDD cycle:

```bash
./gradlew :app:<feature>:allTests
```

Tests should pass (green). If they fail, fix the implementation — not the tests (unless the test expectations were wrong).

## Creating New Fake Builders

When adding a new API to the SDK, also add a corresponding sub-builder to `FakeSdkBuilder` with a DSL function (e.g., `fun newApi(init: FakeNewApiBuilder.() -> Unit)`). All ViewModel tests use `fakeSdk {}` as the single entry point -- individual `fakeXxxApi {}` functions exist for builder composition but should NOT be used directly in ViewModel tests.

When the ViewModel depends on an API not yet faked, create a new builder in `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/`:

1. Annotate builder class with `@FakeSDKDsl`
2. For each API method, add a private lambda field defaulting to `Either.Left(AppError.Client.Unknown())`
3. Add a public setter function accepting a lambda override
4. Add `internal fun build()` returning an anonymous object implementing the interface
5. Add a top-level factory function: `fun fakeFooApi(block: FakeFooApiBuilder.() -> Unit = {}): FooApi`

Consult `references/source-patterns.md` for the exact FakeAuthApiBuilder and FakeUserApiBuilder implementations.

## Module Setup for New Features

When creating a new feature module (e.g., `app/notifications/`):

1. Create directory structure: `app/<feature>/src/commonMain/kotlin/...` and `app/<feature>/src/commonTest/kotlin/...`
2. Add `build.gradle.kts` following the pattern in `references/source-patterns.md` (Build Configuration section)
3. Register the module in `settings.gradle.kts`: `include(":app:<feature>")`
4. Ensure `commonTest.dependencies` includes `implementation(projects.core.testing)`
5. Ensure `commonMain.dependencies` includes `implementation(projects.core.mvi)`

## Resources

### references/

- **`source-patterns.md`** — Complete source code for MviViewModel base class, test DSL, ViewModelTest, Statement hierarchy, fake builders, Koin wiring, and build configuration. Load this reference when implementing to match exact project patterns.
