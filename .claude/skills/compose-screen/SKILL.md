---
name: compose-screen
description: Scaffold new Compose Multiplatform screens with MVI types, ViewModel, Screen composable, Navigation route, and Koin wiring. Use when creating new screens, adding UI features, or building new app pages.
---

# Compose Screen Scaffold Skill

This skill scaffolds new Compose Multiplatform screens following exact project patterns.

## Workflow

### Step 1: Classify

Ask: Which app module? (auth, admin, dashboard, documents, profile, or NEW module)
Ask: Screen name and purpose
Ask: What SDK methods does it need? (check core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/)

### Step 2: Define MVI Types (4 files)

All in: app/<module>/impl/src/commonMain/kotlin/com/m2f/template/app/<module>/

**XxxModel.kt:**
```kotlin
package com.m2f.template.app.<module>

data class XxxModel(
    val isLoading: Boolean = false,
    // domain fields with sensible defaults
    val error: StringKey? = null,
)
```

**XxxIntent.kt:**
```kotlin
package com.m2f.template.app.<module>

sealed interface XxxIntent {
    data object LoadData : XxxIntent
    data class FieldChanged(val value: String) : XxxIntent
    data object SubmitClicked : XxxIntent
}
```

**XxxMutation.kt:**
```kotlin
package com.m2f.template.app.<module>

sealed interface XxxMutation {
    data class SetLoading(val loading: Boolean) : XxxMutation
    data class SetData(val data: DataType) : XxxMutation
    data class SetError(val error: StringKey?) : XxxMutation
}
```

**XxxEvent.kt:**
```kotlin
package com.m2f.template.app.<module>

sealed interface XxxEvent {
    data object NavigateBack : XxxEvent
    // navigation events
}
```

### Step 3: Write Tests FIRST (TDD)

File: app/<module>/impl/src/commonTest/kotlin/com/m2f/template/app/<module>/XxxViewModelTest.kt

```kotlin
package com.m2f.template.app.<module>

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import kotlin.test.Test

class XxxViewModelTest : ViewModelTest() {

    @Test
    fun `load data success updates model`() {
        val sdk = fakeSdk {
            // configure needed API responses
        }
        val viewModel = XxxViewModel(sdk)
        viewModel.test {
            intent(XxxIntent.LoadData)
            model(XxxModel(isLoading = true))
            model(XxxModel(isLoading = false, data = expectedData))
        }
    }

    @Test
    fun `load data failure shows error`() {
        val sdk = fakeSdk()  // defaults to failure
        val viewModel = XxxViewModel(sdk)
        viewModel.test {
            intent(XxxIntent.LoadData)
            model(XxxModel(isLoading = true))
            model(XxxModel(isLoading = false, error = StringKey.GENERIC_ERROR))
        }
    }
}
```

Key testing rules:
- Extend ViewModelTest (sets up test dispatchers)
- Use fakeSdk { } builder — defaults to Either.Left(AppError.Client.Unknown())
- Use viewModel.test { } DSL — intent(), model(), event() statements
- model() assertions use full data class equality (all fields must match)
- Kotest shouldBe is used internally by the DSL

### Step 4: Implement ViewModel

File: app/<module>/impl/src/commonMain/kotlin/com/m2f/template/app/<module>/XxxViewModel.kt

```kotlin
package com.m2f.template.app.<module>

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class XxxViewModel(
    private val sdk: Sdk,
) : MviViewModel<XxxIntent, XxxModel, XxxMutation, XxxEvent>(
    initialState = XxxModel()
) {
    override fun take(intent: XxxIntent) {
        viewModelScope.launch {
            when (intent) {
                is XxxIntent.LoadData -> handleLoad()
                is XxxIntent.FieldChanged -> sendMutation(XxxMutation.SetField(intent.value))
                is XxxIntent.SubmitClicked -> handleSubmit()
            }
        }
    }

    private suspend fun handleLoad() {
        sendMutation(XxxMutation.SetLoading(true))
        sdk.someMethod().fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(XxxMutation.SetError(key))
            },
            ifRight = { data ->
                sendMutation(XxxMutation.SetData(data))
            },
        )
    }

    override suspend fun reduce(model: XxxModel, mutation: XxxMutation): XxxModel =
        when (mutation) {
            is XxxMutation.SetLoading -> model.copy(isLoading = mutation.loading, error = null)
            is XxxMutation.SetData -> model.copy(isLoading = false, data = mutation.data)
            is XxxMutation.SetError -> model.copy(isLoading = false, error = mutation.error)
        }
}
```

Key rules:
- Constructor takes Sdk (injected by Koin)
- take() always wraps in viewModelScope.launch
- reduce() is a pure function — no side effects, no API calls
- SDK responses handled with .fold(ifLeft, ifRight)
- Error mapping: StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR

### Step 5: Create Screen Composable

File: app/<module>/impl/src/commonMain/kotlin/com/m2f/template/app/<module>/XxxScreen.kt

```kotlin
package com.m2f.template.app.<module>

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m2f.template.designsystem.theme.TerminalTheme
import com.m2f.template.designsystem.components.button.TerminalButton
// ... other design system imports

@Composable
fun XxxScreen(
    state: XxxModel,
    onBackClick: () -> Unit = {},
    onFieldChange: (String) -> Unit = {},
    onSubmitClick: () -> Unit = {},
    // all user actions as callbacks — NO viewModel parameter
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > 840.dp) {
            // Desktop layout
            DesktopXxxContent(state, onBackClick, onFieldChange, onSubmitClick)
        } else {
            // Mobile layout
            MobileXxxContent(state, onBackClick, onFieldChange, onSubmitClick)
        }
    }
}
```

Key rules:
- Screen takes state + callback lambdas, NEVER a ViewModel directly
- Use BoxWithConstraints with 840.dp breakpoint for responsive layouts
- Use TerminalTheme.colors, TerminalTheme.typography, TerminalTheme.spacing for theming
- Use design system components: TerminalButton, TerminalInput, TerminalCard, TerminalAlert, etc.
- Foundation-only components (no Material3)

### Step 6: Add Navigation Route

File: app/<module>/contract/src/commonMain/kotlin/com/m2f/template/app/<module>/contract/XxxRoute.kt

```kotlin
package com.m2f.template.app.<module>.contract

import com.m2f.template.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data class XxxRoute(val param: String? = null) : Route
```
Or for no params:
```kotlin
@Serializable data object XxxRoute : Route
```

Routes go in the feature's **contract** module, not in composeApp.

### Step 7: Add Wire Navigation Extension

File: app/<module>/wire/src/commonMain/kotlin/com/m2f/template/app/<module>/wire/XxxNavigation.kt

Add an `EntryProviderScope<Route>` extension in the wire module:
```kotlin
package com.m2f.template.app.<module>.wire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.m2f.template.app.<module>.contract.XxxRoute
import com.m2f.template.app.<module>.XxxEvent
import com.m2f.template.app.<module>.XxxIntent
import com.m2f.template.app.<module>.XxxScreen
import com.m2f.template.app.<module>.XxxViewModel
import com.m2f.template.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.xxxEntries(
    backStack: MutableList<Route>,
) {
    entry<XxxRoute> { route ->
        val viewModel = koinViewModel<XxxViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        XxxScreen(
            state = state,
            onBackClick = { backStack.removeLastOrNull() },
            onFieldChange = { viewModel.take(XxxIntent.FieldChanged(it)) },
            onSubmitClick = { viewModel.take(XxxIntent.SubmitClicked) },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is XxxEvent.NavigateBack -> backStack.removeLastOrNull()
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.take(XxxIntent.LoadData)
        }
    }
}
```

Then call it in AppNavHost.kt's `entryProvider { }` block:
```kotlin
xxxEntries(backStack)
```

### Step 8: Wire Koin DI

File: app/<module>/wire/src/commonMain/kotlin/com/m2f/template/app/<module>/wire/XxxModule.kt

The Koin module in wire:
```kotlin
val xxxModule = module {
    viewModelOf(::XxxViewModel)
}
```

Then include in AppModule.kt:
```kotlin
val appModule = module {
    includes(xxxModule)
}
```

### Step 9: Verify

```bash
./gradlew :app:<module>:impl:allTests
./gradlew :composeApp:compileKotlinJvm
```

## New Module Checklist (if creating a new app module)

1. Create directory: app/<module>/contract/, app/<module>/impl/, app/<module>/wire/
2. Create build.gradle.kts for each submodule (contract depends on `core:navigation`, wire uses `implementation(impl)`)
3. Add to settings.gradle.kts: include("app:<module>:contract"), include("app:<module>:impl"), include("app:<module>:wire")
4. Add wire dependency in composeApp/build.gradle.kts: `implementation(projects.app.<module>.wire)`
5. Add route to contract module
6. Add navigation extension to wire module
7. Call wire extension in AppNavHost.kt
8. Include Koin module in AppModule.kt
9. Follow steps 2-9 above for implementation

## Critical Rules

- ALWAYS write tests FIRST (TDD)
- ALWAYS use callbacks pattern in screens, never pass ViewModel
- ALWAYS use TerminalTheme design system, never Material3
- ALWAYS handle responsive layout with BoxWithConstraints
- ALWAYS use fakeSdk { } for test setup
- ALWAYS extend ViewModelTest base class for tests
- Routes go in **contract** module, not composeApp
- Navigation entries go in **wire** as `EntryProviderScope<Route>` extensions
- Wire uses `implementation(impl)` — impl types are truly hidden
- reduce() must be pure — no side effects
- Events are for navigation/one-shot actions only
- Mutations are for state changes only
