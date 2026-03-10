#!/bin/bash
set -euo pipefail

# Script to create a new app feature module with 3 submodules (contract, impl, wire)
# for the KMP + Koin + Compose Multiplatform project.
# Usage: ./create_app_module.sh --name "feature name"

# --------------------------------------------------------------------------
# Find project root
# --------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

while [[ ! -f "$PROJECT_ROOT/settings.gradle.kts" ]] || [[ ! -d "$PROJECT_ROOT/app" ]]; do
    if [[ "$PROJECT_ROOT" == "/" ]]; then
        echo "Error: Could not find project root (no settings.gradle.kts with app/ directory)"
        exit 1
    fi
    PROJECT_ROOT="$(dirname "$PROJECT_ROOT")"
done

cd "$PROJECT_ROOT" || exit 1
echo "Project root: $PROJECT_ROOT"

# --------------------------------------------------------------------------
# Parse arguments
# --------------------------------------------------------------------------
feature_name=""
while [[ $# -gt 0 ]]; do
    case $1 in
        --name)
            feature_name="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 --name \"feature name\""
            echo ""
            echo "Creates a 3-submodule feature under app/<feature>/ with:"
            echo "  contract  - shared types, navigation route placeholder"
            echo "  impl      - MVI ViewModel, Screen, tests"
            echo "  wire      - Koin DI module"
            exit 0
            ;;
        *)
            echo "Unknown argument: $1"
            echo "Usage: $0 --name \"feature name\""
            exit 1
            ;;
    esac
done

if [[ -z "$feature_name" ]]; then
    echo "Error: --name is required"
    echo "Usage: $0 --name \"feature name\""
    exit 1
fi

# --------------------------------------------------------------------------
# Derive names
# --------------------------------------------------------------------------

# package_name: lowercase, no spaces, no hyphens (e.g., "profile editor" -> "profileeditor")
package_name=$(echo "$feature_name" | tr '[:upper:]' '[:lower:]' | sed 's/[ -]//g')

# class_name: PascalCase (e.g., "profile editor" -> "ProfileEditor")
# Handle both space-separated and hyphen-separated words
normalized=$(echo "$feature_name" | sed 's/-/ /g')
IFS=' ' read -ra words <<< "$normalized"
class_name=""
for word in "${words[@]}"; do
    [[ -z "$word" ]] && continue
    word_lower=$(echo "$word" | tr '[:upper:]' '[:lower:]')
    first_char=$(echo "${word_lower:0:1}" | tr '[:lower:]' '[:upper:]')
    class_name+="${first_char}${word_lower:1}"
done

# Validate
if [[ -z "$class_name" ]]; then
    echo "Error: could not derive class name from \"$feature_name\""
    exit 1
fi

# Check the feature does not already exist
if [[ -d "app/$package_name" ]]; then
    echo "Error: app/$package_name already exists"
    exit 1
fi

echo ""
echo "Feature name:   $feature_name"
echo "Package name:   com.m2f.template.app.$package_name"
echo "Class prefix:   $class_name"
echo "Module path:    app/$package_name"
echo ""

# --------------------------------------------------------------------------
# Shared helpers
# --------------------------------------------------------------------------
BASE_PKG="com/m2f/template/app/$package_name"

make_src() {
    # make_src <submodule> <sourceSet>
    local sub="$1" ss="$2"
    echo "app/$package_name/$sub/src/$ss/kotlin/$BASE_PKG/$sub"
}

# --------------------------------------------------------------------------
# 1. CONTRACT MODULE
# --------------------------------------------------------------------------
echo "Creating CONTRACT module..."

CONTRACT_SRC=$(make_src contract commonMain)
mkdir -p "$CONTRACT_SRC"

# build.gradle.kts
cat > "app/$package_name/contract/build.gradle.kts" << GRADLE
plugins {
    id("kmp-library-convention")
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
        }
    }
}

android {
    namespace = "com.m2f.template.app.${package_name}.contract"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
GRADLE

# .gitignore
echo "/build" > "app/$package_name/contract/.gitignore"

# Placeholder contract file
cat > "$CONTRACT_SRC/${class_name}Contract.kt" << KOTLIN
package com.m2f.template.app.${package_name}.contract
KOTLIN

echo "  contract module created"

# --------------------------------------------------------------------------
# 2. IMPLEMENTATION MODULE
# --------------------------------------------------------------------------
echo "Creating IMPL module..."

IMPL_SRC=$(make_src impl commonMain)
IMPL_TEST=$(make_src impl commonTest)
mkdir -p "$IMPL_SRC"
mkdir -p "$IMPL_TEST"

# build.gradle.kts
cat > "app/$package_name/impl/build.gradle.kts" << GRADLE
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
            implementation(projects.app.${package_name}.contract)
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
    namespace = "com.m2f.template.app.${package_name}.impl"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
GRADLE

# .gitignore
echo "/build" > "app/$package_name/impl/.gitignore"

# Intent
cat > "$IMPL_SRC/${class_name}Intent.kt" << KOTLIN
package com.m2f.template.app.${package_name}.impl

sealed interface ${class_name}Intent {
    data object Load : ${class_name}Intent
}
KOTLIN

# Model
cat > "$IMPL_SRC/${class_name}Model.kt" << KOTLIN
package com.m2f.template.app.${package_name}.impl

data class ${class_name}Model(
    val isLoading: Boolean = false,
)
KOTLIN

# Mutation
cat > "$IMPL_SRC/${class_name}Mutation.kt" << KOTLIN
package com.m2f.template.app.${package_name}.impl

sealed interface ${class_name}Mutation {
    data class SetLoading(val isLoading: Boolean) : ${class_name}Mutation
}
KOTLIN

# Event
cat > "$IMPL_SRC/${class_name}Event.kt" << KOTLIN
package com.m2f.template.app.${package_name}.impl

sealed interface ${class_name}Event {
    data object NavigateBack : ${class_name}Event
}
KOTLIN

# ViewModel
cat > "$IMPL_SRC/${class_name}ViewModel.kt" << KOTLIN
package com.m2f.template.app.${package_name}.impl

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class ${class_name}ViewModel(
    private val sdk: Sdk,
) : MviViewModel<${class_name}Intent, ${class_name}Model, ${class_name}Mutation, ${class_name}Event>(
    initialState = ${class_name}Model()
) {

    override fun take(intent: ${class_name}Intent) {
        viewModelScope.launch {
            when (intent) {
                is ${class_name}Intent.Load -> {
                    sendMutation(${class_name}Mutation.SetLoading(true))
                    // TODO: implement
                    sendMutation(${class_name}Mutation.SetLoading(false))
                }
            }
        }
    }

    override suspend fun reduce(model: ${class_name}Model, mutation: ${class_name}Mutation): ${class_name}Model =
        when (mutation) {
            is ${class_name}Mutation.SetLoading -> model.copy(isLoading = mutation.isLoading)
        }
}
KOTLIN

# Screen
cat > "$IMPL_SRC/${class_name}Screen.kt" << KOTLIN
package com.m2f.template.app.${package_name}.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.m2f.template.designsystem.theme.TerminalTheme

@Composable
fun ${class_name}Screen(
    state: ${class_name}Model,
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
KOTLIN

# ViewModel Test
cat > "$IMPL_TEST/${class_name}ViewModelTest.kt" << KOTLIN
package com.m2f.template.app.${package_name}.impl

import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import kotlin.test.Test

class ${class_name}ViewModelTest : ViewModelTest() {

    @Test
    fun \`initial state is correct\`() {
        val sdk = fakeSdk()
        val viewModel = ${class_name}ViewModel(sdk)
        viewModel.test {
            model(${class_name}Model(isLoading = false))
        }
    }

    @Test
    fun \`Load toggles loading state\`() {
        val sdk = fakeSdk()
        val viewModel = ${class_name}ViewModel(sdk)
        viewModel.test {
            intent(${class_name}Intent.Load)
            model(${class_name}Model(isLoading = true))
            model(${class_name}Model(isLoading = false))
        }
    }
}
KOTLIN

echo "  impl module created"

# --------------------------------------------------------------------------
# 3. WIRE MODULE
# --------------------------------------------------------------------------
echo "Creating WIRE module..."

WIRE_SRC=$(make_src wire commonMain)
mkdir -p "$WIRE_SRC"

# build.gradle.kts
cat > "app/$package_name/wire/build.gradle.kts" << GRADLE
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
            api(projects.app.${package_name}.contract)
            implementation(projects.app.${package_name}.impl)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
        }
    }
}

android {
    namespace = "com.m2f.template.app.${package_name}.wire"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
GRADLE

# .gitignore
echo "/build" > "app/$package_name/wire/.gitignore"

# Koin Module
cat > "$WIRE_SRC/${class_name}Module.kt" << KOTLIN
package com.m2f.template.app.${package_name}.wire

import com.m2f.template.app.${package_name}.impl.${class_name}ViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ${package_name}Module = module {
    viewModelOf(::${class_name}ViewModel)
}
KOTLIN

echo "  wire module created"

# --------------------------------------------------------------------------
# 4. UPDATE settings.gradle.kts
# --------------------------------------------------------------------------
echo "Updating settings.gradle.kts..."

# Check if already included
if grep -q "include(\"app:${package_name}:" settings.gradle.kts 2>/dev/null; then
    echo "  WARNING: app:${package_name} modules already in settings.gradle.kts, skipping"
else
    # Find the last include("app: line number
    last_app_line=$(grep -n 'include("app:' settings.gradle.kts | tail -1 | cut -d: -f1)

    if [[ -z "$last_app_line" ]]; then
        # No app includes found -- append at end
        echo "" >> settings.gradle.kts
        echo "include(\"app:${package_name}:contract\")" >> settings.gradle.kts
        echo "include(\"app:${package_name}:impl\")" >> settings.gradle.kts
        echo "include(\"app:${package_name}:wire\")" >> settings.gradle.kts
    else
        # Insert after the last app include line
        sed -i '' "${last_app_line}a\\
include(\"app:${package_name}:contract\")\\
include(\"app:${package_name}:impl\")\\
include(\"app:${package_name}:wire\")
" settings.gradle.kts
    fi
    echo "  settings.gradle.kts updated"
fi

# --------------------------------------------------------------------------
# 5. ADD ROUTE TO Routes.kt
# --------------------------------------------------------------------------
echo "Adding route to Routes.kt..."

ROUTES_FILE="composeApp/src/commonMain/kotlin/com/m2f/template/navigation/Routes.kt"
if [[ -f "$ROUTES_FILE" ]]; then
    # Check if route already exists
    if grep -q "${class_name}Route" "$ROUTES_FILE" 2>/dev/null; then
        echo "  WARNING: ${class_name}Route already exists in Routes.kt, skipping"
    else
        # Append the new route at the end of the file
        cat >> "$ROUTES_FILE" << KOTLIN

@Serializable
data object ${class_name}Route : Route
KOTLIN
        echo "  ${class_name}Route added to Routes.kt"
    fi
else
    echo "  WARNING: Routes.kt not found at $ROUTES_FILE, skipping route creation"
fi

# --------------------------------------------------------------------------
# SUMMARY
# --------------------------------------------------------------------------
echo ""
echo "========================================="
echo "Feature module '${feature_name}' created!"
echo "========================================="
echo ""
echo "Package:  com.m2f.template.app.${package_name}"
echo "Class:    ${class_name}"
echo ""
echo "Created modules:"
echo "  app:${package_name}:contract   - shared types"
echo "  app:${package_name}:impl       - ViewModel + Screen + Tests"
echo "  app:${package_name}:wire       - Koin module"
echo ""
echo "Generated files:"
echo "  Contract:  ${class_name}Contract.kt"
echo "  Impl:      ${class_name}Intent.kt"
echo "             ${class_name}Model.kt"
echo "             ${class_name}Mutation.kt"
echo "             ${class_name}Event.kt"
echo "             ${class_name}ViewModel.kt"
echo "             ${class_name}Screen.kt"
echo "             ${class_name}ViewModelTest.kt"
echo "  Wire:      ${class_name}Module.kt"
echo "  Route:     ${class_name}Route (in Routes.kt)"
echo ""
echo "Next steps:"
echo "  1. Sync Gradle in your IDE"
echo "  2. Add wire dependency to composeApp/build.gradle.kts:"
echo "       implementation(projects.app.${package_name}.wire)"
echo "  3. Include Koin module in AppModule.kt:"
echo "       import com.m2f.template.app.${package_name}.wire.${package_name}Module"
echo "       includes(${package_name}Module)"
echo "  4. Add entry<${class_name}Route> in AppNavHost.kt"
echo "  5. Run tests: ./gradlew :app:${package_name}:impl:allTests"
echo "========================================="
