---
name: create-app-module
description: Creates a new client-side feature module with 3 submodules (contract, impl, wire) for the KMP + Compose Multiplatform app. Use when creating new app features, adding client modules, or scaffolding feature screens.
allowed-tools: Write, Read, Edit, Bash
---

# App Feature Module Creator (KMP + Koin + Compose Multiplatform)

## Purpose
Creates new client-side feature modules following the project's 3-submodule architecture adapted for Kotlin Multiplatform with Koin DI and Compose Multiplatform.

## Why 3 Submodules?

| Submodule | Role | Visibility |
|-----------|------|------------|
| **contract** | Public API: Route definitions, shared types other features may depend on. Depends on `core:navigation`. | Any module can depend on it |
| **impl** | Hidden internals: ViewModel, MVI types, Screen composable, tests | Only wire depends on it (via `implementation`) |
| **wire** | DI + Navigation bridge: Koin module, `EntryProviderScope<Route>` extension. Uses `implementation(impl)` so impl is truly hidden. Re-exports contract as `api`. | composeApp imports this |

This replaces Hilt's `@InstallIn` auto-discovery: the wire module exports a `val featureModule` that the aggregator (`AppModule.kt`) explicitly includes, and an `EntryProviderScope<Route>` extension that AppNavHost calls.

## Instructions

### 1. Understand the Feature Requirements
- Ask for the feature name (e.g., "settings", "notifications", "profile editor")
- Clarify the UI requirements (screens, composables needed)
- Identify dependencies on other features (for cross-feature navigation)

### 2. Generate Module Structure
Run the `create_app_module.sh` script to scaffold the complete structure:

```bash
.claude/skills/create-app-module/scripts/create_app_module.sh --name "feature name"
```

The script will create:
- **3 submodules**: contract, impl, wire
- Directory structure (`app/<featurename>/`)
- Base package structure (`com.m2f.template.app.<featurename>`)
- `build.gradle.kts` for each submodule
- Update `settings.gradle.kts` to include all 3 modules
- Route definition in contract
- ViewModel, Screen, Intent/Model/Mutation/Event in impl
- Koin module + navigation extension in wire

### 3. Feature Module Structure
After the script runs, you will have this structure:

```
app/<featurename>/
|-- contract/
|   |-- build.gradle.kts
|   `-- src/commonMain/kotlin/com/m2f/template/app/<featurename>/contract/
|       `-- <Feature>Route.kt                   # Route definition (extends Route from core:navigation)
|
|-- impl/
|   |-- build.gradle.kts
|   `-- src/
|       |-- commonMain/kotlin/com/m2f/template/app/<featurename>/impl/
|       |   |-- <Feature>Intent.kt            # Sealed interface for user intents
|       |   |-- <Feature>Model.kt             # Data class for UI state
|       |   |-- <Feature>Mutation.kt          # Sealed interface for state mutations
|       |   |-- <Feature>Event.kt             # Sealed interface for one-time events
|       |   |-- <Feature>ViewModel.kt         # Extends MviViewModel, uses Sdk
|       |   `-- <Feature>Screen.kt            # Composable UI (callbacks pattern)
|       `-- commonTest/kotlin/com/m2f/template/app/<featurename>/impl/
|           `-- <Feature>ViewModelTest.kt     # ViewModelTest + test{} DSL + fakeSdk
|
`-- wire/
    |-- build.gradle.kts
    `-- src/commonMain/kotlin/com/m2f/template/app/<featurename>/wire/
        |-- <Feature>Module.kt                # Koin module with viewModelOf
        `-- <Feature>Navigation.kt            # EntryProviderScope<Route> extension
```

### 4. Generated Files Reference

#### Contract Module - Route
```kotlin
package com.m2f.template.app.<featurename>.contract

import com.m2f.template.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data object <Feature>Route : Route
```

#### Impl Module - Intent (sealed interface)
```kotlin
package com.m2f.template.app.<featurename>.impl

sealed interface <Feature>Intent {
    data object Load : <Feature>Intent
}
```

#### Impl Module - Model (data class)
```kotlin
package com.m2f.template.app.<featurename>.impl

data class <Feature>Model(
    val isLoading: Boolean = false,
)
```

#### Impl Module - Mutation (sealed interface)
```kotlin
package com.m2f.template.app.<featurename>.impl

sealed interface <Feature>Mutation {
    data class SetLoading(val isLoading: Boolean) : <Feature>Mutation
}
```

#### Impl Module - Event (sealed interface)
```kotlin
package com.m2f.template.app.<featurename>.impl

sealed interface <Feature>Event {
    data object NavigateBack : <Feature>Event
}
```

#### Impl Module - ViewModel (extends MviViewModel)
```kotlin
package com.m2f.template.app.<featurename>.impl

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
                is <Feature>Intent.Load -> {
                    sendMutation(<Feature>Mutation.SetLoading(true))
                    // TODO: implement
                    sendMutation(<Feature>Mutation.SetLoading(false))
                }
            }
        }
    }

    override suspend fun reduce(model: <Feature>Model, mutation: <Feature>Mutation): <Feature>Model =
        when (mutation) {
            is <Feature>Mutation.SetLoading -> model.copy(isLoading = mutation.isLoading)
        }
}
```

#### Impl Module - Screen (Composable, callbacks pattern)
```kotlin
package com.m2f.template.app.<featurename>.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.m2f.template.designsystem.theme.TerminalTheme

@Composable
fun <Feature>Screen(
    state: <Feature>Model,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TerminalTheme.colors

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(colors.bg),
        contentAlignment = Alignment.Center,
    ) {
        // TODO: implement responsive layout
        // if (maxWidth > 840.dp) { /* Desktop */ } else { /* Mobile */ }
    }
}
```

#### Impl Module - ViewModel Test (ViewModelTest + test{} DSL)
```kotlin
package com.m2f.template.app.<featurename>.impl

import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import kotlin.test.Test

class <Feature>ViewModelTest : ViewModelTest() {

    @Test
    fun `initial state is correct`() {
        val sdk = fakeSdk()
        val viewModel = <Feature>ViewModel(sdk)
        viewModel.test {
            model(<Feature>Model(isLoading = false))
        }
    }

    @Test
    fun `Load toggles loading state`() {
        val sdk = fakeSdk()
        val viewModel = <Feature>ViewModel(sdk)
        viewModel.test {
            intent(<Feature>Intent.Load)
            model(<Feature>Model(isLoading = true))
            model(<Feature>Model(isLoading = false))
        }
    }
}
```

#### Wire Module - Koin Module
```kotlin
package com.m2f.template.app.<featurename>.wire

import com.m2f.template.app.<featurename>.impl.<Feature>ViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val <featurename>Module = module {
    viewModelOf(::<Feature>ViewModel)
}
```

#### Wire Module - Navigation Extension
```kotlin
package com.m2f.template.app.<featurename>.wire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.m2f.template.app.<featurename>.contract.<Feature>Route
import com.m2f.template.app.<featurename>.impl.<Feature>Event
import com.m2f.template.app.<featurename>.impl.<Feature>Intent
import com.m2f.template.app.<featurename>.impl.<Feature>Screen
import com.m2f.template.app.<featurename>.impl.<Feature>ViewModel
import com.m2f.template.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.<featurename>Entries(
    backStack: MutableList<Route>,
) {
    entry<<Feature>Route> {
        val viewModel = koinViewModel<<Feature>ViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        <Feature>Screen(
            state = state,
            onBack = { backStack.removeLastOrNull() },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is <Feature>Event.NavigateBack -> backStack.removeLastOrNull()
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.take(<Feature>Intent.Load)
        }
    }
}
```

### 5. Build.gradle.kts Files

#### Contract build.gradle.kts
```kotlin
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
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.navigation)
            implementation(projects.core.models)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "com.m2f.template.app.<featurename>.contract"
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

#### Implementation build.gradle.kts
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
            implementation(projects.app.<featurename>.contract)
            implementation(projects.core.models)
            implementation(projects.core.sdk)
            implementation(projects.core.mvi)
            implementation(projects.app.designsystem)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
        }
    }
}

android {
    namespace = "com.m2f.template.app.<featurename>.impl"
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

#### Wire build.gradle.kts
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
            api(projects.app.<featurename>.contract)
            implementation(projects.app.<featurename>.impl)
            implementation(projects.core.mvi)
            implementation(projects.core.sdk)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation3.ui)
        }
    }
}

android {
    namespace = "com.m2f.template.app.<featurename>.wire"
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

### 6. Next Steps After Creation
1. **Sync Gradle** in your IDE
2. **Add wire dependency to composeApp** — in `composeApp/build.gradle.kts`:
   ```kotlin
   implementation(projects.app.<featurename>.wire)
   ```
3. **Include Koin module in AppModule.kt**:
   ```kotlin
   import com.m2f.template.app.<featurename>.wire.<featurename>Module

   val appModule = module {
       includes(<featurename>Module)
       // ... existing includes
   }
   ```
4. **Call wire navigation extension in AppNavHost.kt**:
   ```kotlin
   // Inside entryProvider { ... } block:
   <featurename>Entries(backStack)
   ```
5. **Implement** the ViewModel logic and Screen UI
6. **Run tests**: `./gradlew :app:<featurename>:impl:allTests`

### 7. Best Practices
- Do NOT write comments unless explicitly requested
- Do NOT use mocks in tests -- always use `fakeSdk{}` and fakes
- Use `sealed interface` for Intent/Mutation/Event, not sealed class or enum
- Do NOT put lambdas in data classes used for UI state (breaks Compose stability)
- Screen composables use the **callbacks pattern** (state + lambdas), never take a ViewModel parameter
- Use `BoxWithConstraints` for responsive layouts (`maxWidth > 840.dp` = desktop)
- Use `TerminalTheme` (colors, typography) -- never Material3 `MaterialTheme`
- Use `TerminalText`, `TerminalCard`, `TerminalBadge`, etc. from `app:designsystem`
- ViewModel `reduce` is `suspend fun` (not plain fun) in this project
- Wire uses `implementation(impl)` -- impl types are truly hidden outside wire
- Navigation uses `EntryProviderScope<Route>` extensions from wire modules
- Back navigation uses `backStack.removeLastOrNull()` (mutable list pattern)
- Cross-feature navigation: wire depends on other features' contracts for route references

## Examples
- "Create a new settings feature"
- "Add a feature module for notifications"
- "Scaffold a new chat feature"
