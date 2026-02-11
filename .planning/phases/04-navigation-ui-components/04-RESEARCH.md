# Phase 4: Navigation & UI Components - Research

**Researched:** 2026-02-11
**Domain:** Compose Multiplatform navigation (type-safe routes), Material3 theming, shared component library
**Confidence:** HIGH

## Summary

Phase 4 adds three capabilities to the KMP template: type-safe multiplatform navigation, a reusable UI component library, and a custom theme system. The project already has Compose Multiplatform 1.10.1 with Material3 (`compose.material3`), Koin Compose integration (`koin-compose`, `koin-compose-viewmodel`), lifecycle ViewModel support, and two feature modules (`app:auth`, `app:dashboard`) with empty screen composables. The `composeApp` module currently renders a simple `MaterialTheme { }` wrapper around demo content with `KoinApplication` for DI.

**Navigation decision:** Use Navigation Compose 2.9.2 (the stable multiplatform library bundled with Compose Multiplatform 1.10.1). Navigation 3 is also available as `1.0.0-alpha06` for multiplatform but is still alpha on non-Android targets and lacks browser history support for WasmJs. Since this is a template project that must work reliably on all 4 targets (Android, iOS, Desktop, WasmJs), the stable Nav 2.9.2 with type-safe `@Serializable` routes is the correct choice. The project can migrate to Nav 3 when it reaches stable for multiplatform.

**Theme decision:** Use Material3's `MaterialTheme` composable with custom `lightColorScheme`/`darkColorScheme`, custom `Typography` (using Compose resources for custom fonts), and custom `Shapes`. All theme configuration lives in a single `theme/` package within `composeApp` so a developer can change colors, fonts, and shapes by editing one file.

**Component library decision:** Place reusable composables in `composeApp/src/commonMain/.../ui/components/` as thin wrappers around Material3 components. These wrappers enforce the project's design system (consistent padding, colors from theme, standardized sizes) while remaining customizable via parameters. The feature modules (`app:auth`, `app:dashboard`) consume these components.

**Primary recommendation:** Add `navigation-compose:2.9.2` and `koin-compose-viewmodel-navigation:4.1.1` to the version catalog; define routes as `@Serializable` data objects/classes in a shared navigation package; build the theme as a single-file configuration using Material3 APIs; implement the component library as composable functions that delegate to Material3 with project-specific defaults.

## Standard Stack

### Core (Bundled with Compose Multiplatform 1.10.1)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Navigation Compose | 2.9.2 | Type-safe multiplatform navigation | Bundled with CMP 1.10.1; stable on all targets; `@Serializable` route support |
| Material3 | 1.10.0-alpha05 | UI component foundation + theming | Bundled with CMP 1.10.1; `compose.material3` DSL accessor already in use |
| Lifecycle ViewModel Compose | 2.10.0-alpha06 | ViewModel integration for navigation | Already in `libs.versions.toml` as `androidx-lifecycle-viewmodelCompose` |
| Lifecycle Runtime Compose | 2.10.0-alpha06 | `collectAsState` and lifecycle-aware composition | Already in `libs.versions.toml` as `androidx-lifecycle-runtimeCompose` |
| Compose Resources | 1.10.1 | Custom fonts, drawables | Already used (`compose.components.resources`); font loading via `Res.font.*` |

### New Dependencies Required

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Navigation Compose (JetBrains) | 2.9.2 | `NavHost`, `composable<T>`, `NavController` | Official JetBrains KMP adaptation of Jetpack Navigation |
| Koin Compose ViewModel Navigation | 4.1.1 | `koinNavViewModel()` for nav-scoped ViewModels | Koin 4.1.1 multiplatform artifact; navigation-aware DI |
| kotlinx-serialization-core | (transitive) | `@Serializable` for route definitions | Required by navigation-compose type-safe APIs; already transitive via kotlinx-serialization-json |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Navigation Compose 2.9.2 | Navigation 3 (`navigation3-ui:1.0.0-alpha06`) | Nav 3 is alpha on multiplatform, no WasmJs browser history, requires polymorphic serialization boilerplate. More flexible back stack model but premature for a template that must be stable. **Recommendation: Nav 2.9.2 now, migrate to Nav 3 when stable.** |
| Navigation Compose 2.9.2 | Voyager / Decompose / Circuit | Third-party; adds external dependency for something the official library does well. Template should use official stack. **Recommendation: Official Navigation Compose.** |
| Material3 built-in components | Custom hand-drawn components | Massive effort, no accessibility, no platform-specific behavior. **Recommendation: Wrap M3 components.** |
| MaterialKolor (dynamic palette) | Manual color definitions | MaterialKolor generates full M3 palette from a seed color. Nice but adds a dependency for something a static palette handles. **Recommendation: Manual lightColorScheme/darkColorScheme. Mention MaterialKolor in docs as optional enhancement.** |

### Dependencies to Add

In `gradle/libs.versions.toml`:
```toml
[versions]
navigation-compose = "2.9.2"

[libraries]
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
koin-compose-viewmodel-navigation = { module = "io.insert-koin:koin-compose-viewmodel-navigation", version.ref = "koin" }
```

In `composeApp/build.gradle.kts`:
```kotlin
plugins {
    // ADD: serialization plugin (required for @Serializable route classes)
    id("org.jetbrains.kotlin.plugin.serialization")
}

commonMain.dependencies {
    // ADD
    implementation(libs.navigation.compose)
    implementation(libs.koin.compose.viewmodel.navigation)
}
```

Feature modules (`app:auth`, `app:dashboard`) already have `kmp-library-convention` which applies the serialization plugin, and already depend on `compose.material3`. They will need `navigation-compose` added if they define their own `NavGraphBuilder` extension functions.

## Architecture Patterns

### Recommended Project Structure

```
composeApp/src/commonMain/kotlin/com/m2f/template/
├── App.kt                          # Root composable: theme + KoinApplication + NavHost
├── di/
│   └── AppModule.kt                # Client DI module
├── navigation/
│   ├── Routes.kt                   # All @Serializable route definitions
│   └── AppNavHost.kt               # NavHost with composable<Route> registrations
├── theme/
│   ├── Theme.kt                    # AppTheme composable (MaterialTheme wrapper)
│   ├── Color.kt                    # Light/dark color scheme definitions
│   ├── Type.kt                     # Typography configuration
│   └── Shape.kt                    # Shape configuration
└── ui/
    └── components/
        ├── AppButton.kt            # Primary, secondary, outlined button variants
        ├── AppTextField.kt         # Text input with validation state
        ├── AppCard.kt              # Card with standard elevation/padding
        └── AppDialog.kt            # Confirmation and info dialog patterns

composeApp/src/commonMain/composeResources/
└── font/
    └── *.ttf                       # Custom font files (if using custom fonts)

app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/
├── AuthScreen.kt                   # Login/signup UI (uses shared components)
├── AuthViewModel.kt                # Handles auth state
└── AuthNavigation.kt               # NavGraphBuilder.authGraph() extension

app/dashboard/src/commonMain/kotlin/com/m2f/template/app/dashboard/
├── DashboardScreen.kt              # Dashboard UI
├── DashboardViewModel.kt           # Dashboard state
└── DashboardNavigation.kt          # NavGraphBuilder.dashboardGraph() extension
```

### Pattern 1: Type-Safe Route Definitions with @Serializable

**What:** Define all navigation destinations as `@Serializable` data objects (no arguments) or data classes (with arguments). Place them in a shared `navigation/Routes.kt` file so both `composeApp` and feature modules can reference them.
**When to use:** Every navigation destination in the app.

```kotlin
// Source: JetBrains Navigation Compose docs (kotlinlang.org/docs/multiplatform/compose-navigation-routing.html)
package com.m2f.template.navigation

import kotlinx.serialization.Serializable

// --- Auth flow ---
@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

// --- Main app flow ---
@Serializable
data object DashboardRoute

@Serializable
data class ProfileRoute(val userId: String? = null) // null = current user
```

### Pattern 2: NavHost with composable<Route> Registration

**What:** A single `NavHost` in `composeApp` that registers all routes using the type-safe `composable<T>` builder. Feature modules contribute their screens via `NavGraphBuilder` extension functions.
**When to use:** App entry point, called once in `App.kt`.

```kotlin
// Source: JetBrains Navigation Compose docs
package com.m2f.template.navigation

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute)
                }
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<DashboardRoute> {
            DashboardScreen()
        }
        composable<ProfileRoute> { backStackEntry ->
            val route: ProfileRoute = backStackEntry.toRoute()
            ProfileScreen(userId = route.userId)
        }
    }
}
```

### Pattern 3: Feature Module NavGraphBuilder Extensions

**What:** Each feature module defines a `NavGraphBuilder` extension that registers its own screens. This keeps the main NavHost clean and allows feature modules to own their navigation graph.
**When to use:** When a feature has multiple screens that form a logical group.

```kotlin
// In app:auth module
package com.m2f.template.app.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.m2f.template.navigation.LoginRoute
import com.m2f.template.navigation.RegisterRoute

fun NavGraphBuilder.authGraph(navController: NavController) {
    composable<LoginRoute> {
        AuthScreen(
            onLoginSuccess = {
                navController.navigate(DashboardRoute) {
                    popUpTo<LoginRoute> { inclusive = true }
                }
            },
            onNavigateToRegister = {
                navController.navigate(RegisterRoute)
            }
        )
    }
    composable<RegisterRoute> {
        // Register screen
    }
}
```

### Pattern 4: Custom Theme Configuration

**What:** A single `AppTheme` composable that wraps `MaterialTheme` with project-specific colors, typography, and shapes. A developer changes the theme by editing `Color.kt`, `Type.kt`, or `Shape.kt` -- the theme applies uniformly across all targets.
**When to use:** Wrap the entire app content.

```kotlin
// Source: Android Material3 theming guide + KMP adaptations
// theme/Color.kt
package com.m2f.template.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand colors -- developer changes these
val Seed = Color(0xFF6750A4)
val Primary = Color(0xFF6750A4)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFEADDFF)
val OnPrimaryContainer = Color(0xFF21005D)
// ... (full palette)

val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    // ... all 29 color roles
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    // ... all 29 color roles
)

// theme/Theme.kt
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,  // from Type.kt
        shapes = AppShapes,          // from Shape.kt
        content = content
    )
}
```

### Pattern 5: Reusable Component Wrappers

**What:** Thin composable wrappers around Material3 components that enforce the project's design system defaults (consistent padding, corner radius, text styles) while remaining customizable.
**When to use:** Every UI screen should use these instead of raw Material3 components.

```kotlin
// ui/components/AppButton.kt
package com.m2f.template.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text)
    }
}

@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text)
    }
}
```

```kotlin
// ui/components/AppTextField.kt
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        isError = error != null,
        supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
    )
}
```

### Pattern 6: ViewModel with Koin Navigation Integration

**What:** ViewModels are defined in feature modules and injected via `koinViewModel()` inside navigation composables. Route parameters are passed to ViewModels via Koin's `parametersOf`.
**When to use:** Every screen that has state management.

```kotlin
// In app:auth Koin module
val authModule = module {
    viewModel { AuthViewModel(authApi = get(), tokenStorage = get()) }
}

// In navigation composable
composable<LoginRoute> {
    val viewModel: AuthViewModel = koinViewModel()
    LoginScreen(viewModel = viewModel)
}
```

### Pattern 7: WasmJs Browser History Binding

**What:** On WasmJs target, bind the NavController to browser history so back/forward buttons work and URL fragments update.
**When to use:** WasmJs platform entry point only.

```kotlin
// wasmJsMain/kotlin/.../main.kt
@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App(
            onNavHostReady = { navController ->
                navController.bindToBrowserNavigation()
            }
        )
    }
}
```

### Anti-Patterns to Avoid

- **String-based routes:** Never use `navController.navigate("login")` or string route names. Always use `@Serializable` types: `navController.navigate(LoginRoute)`.
- **Passing complex objects as route arguments:** Pass IDs/keys only, load data from the data layer at the destination. Serializable route classes should have primitive fields.
- **Theme colors as hardcoded hex values in components:** Always use `MaterialTheme.colorScheme.*` to reference colors. Components must never hardcode `Color(0xFF...)`.
- **Creating a separate "design system" module for 5-10 components:** Overkill for a template. Put components in `composeApp/ui/components/`. Extract to a module only when the library grows large enough to warrant independent versioning.
- **Using `@Preview` with Koin dependencies in preview composables:** Previews don't have Koin context. Design components to accept data via parameters (not ViewModels) so they are previewable.
- **Forgetting `popUpTo` after login:** After successful authentication, always pop the auth graph off the back stack so the user cannot press back to return to the login screen.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Type-safe navigation | String route matching / manual deep link parsing | Navigation Compose 2.9.2 with `@Serializable` | Compile-time safety, argument type checking, back stack management |
| Dark/light theme switching | Custom `CompositionLocal` for colors | `MaterialTheme` + `isSystemInDarkTheme()` | System integration on all platforms, automatic color role propagation |
| Button loading states | Custom animated indicator + disable logic | Wrap `Button` + `CircularProgressIndicator` | Material3 handles elevation, ripple, disabled states correctly |
| Text field validation display | Custom error label + border coloring | `OutlinedTextField` `isError` + `supportingText` | Material3 handles animation, color transitions, accessibility |
| Dialog presentation | Custom overlay/backdrop management | `AlertDialog` from Material3 | Handles scrim, dismissal, focus management, accessibility |
| Color palette generation | Manual hex value picking | Material Theme Builder tool (web) or MaterialKolor | Ensures accessible contrast ratios, complete M3 color role coverage |

**Key insight:** Material3 components already implement the design system patterns -- accessible contrast, state layers, elevation, motion. The component library should wrap M3 components with project-specific defaults, not replace them.

## Common Pitfalls

### Pitfall 1: Missing Serialization Plugin on composeApp Module
**What goes wrong:** Route classes like `@Serializable data object LoginRoute` compile fine but navigation crashes at runtime with "No serializer found" errors.
**Why it happens:** The `composeApp` module uses its own plugin block and does NOT apply `kmp-library-convention`. The serialization plugin is not applied.
**How to avoid:** Add `id("org.jetbrains.kotlin.plugin.serialization")` to `composeApp/build.gradle.kts` plugins block.
**Warning signs:** Compilation succeeds but navigation crashes with serialization errors at runtime.

### Pitfall 2: Back Stack Leak After Authentication
**What goes wrong:** After login, the user presses the back button and returns to the login screen instead of exiting the app.
**Why it happens:** `navController.navigate(DashboardRoute)` pushes onto the stack without clearing the auth screens.
**How to avoid:** Use `popUpTo` with `inclusive = true` when navigating from login to dashboard:
```kotlin
navController.navigate(DashboardRoute) {
    popUpTo<LoginRoute> { inclusive = true }
}
```
**Warning signs:** Back button from dashboard shows login screen.

### Pitfall 3: Theme Not Applied to Dialog Components
**What goes wrong:** Dialogs appear with default Material3 colors instead of the custom theme.
**Why it happens:** `AlertDialog` uses its own surface/content color defaults. If the theme is not wrapping the navigation host correctly, dialogs may inherit the wrong scope.
**How to avoid:** Ensure `AppTheme` wraps everything including navigation. The `AlertDialog` composable automatically picks up `MaterialTheme.colorScheme` when it is within the theme scope.
**Warning signs:** Dialogs have different colors than the rest of the app.

### Pitfall 4: Custom Font as Non-Composable in Typography
**What goes wrong:** Trying to use `Font(Res.font.MyFont)` in a `val typography = Typography(...)` outside a `@Composable` scope causes a compilation error because `Font()` is a composable function in Compose Multiplatform.
**Why it happens:** Compose Multiplatform's resource-based `Font()` is `@Composable` (unlike Android's `Font()` which is not). This means `Typography` must be created inside a `@Composable` function.
**How to avoid:** Create typography inside the `AppTheme` composable or use a `@Composable` factory function:
```kotlin
@Composable
fun appTypography(): Typography {
    val fontFamily = FontFamily(
        Font(Res.font.Inter_Regular, FontWeight.Normal),
        Font(Res.font.Inter_Bold, FontWeight.Bold),
    )
    return Typography(
        bodyLarge = TextStyle(fontFamily = fontFamily, fontSize = 16.sp),
        // ...
    )
}
```
**Warning signs:** Compilation error: "@Composable invocations can only happen from the context of a @Composable function."

### Pitfall 5: Navigation State Lost on Configuration Change (Android)
**What goes wrong:** Rotating the device or changing system dark mode causes the navigation stack to reset.
**Why it happens:** If `rememberNavController()` is created inside a composable that gets fully recomposed (e.g., because the Activity recreates), the nav state is lost.
**How to avoid:** `rememberNavController()` already uses `rememberSaveable` internally for state preservation. Ensure the `NavController` is created at the correct scope (inside the `App` composable that survives recomposition, not inside platform-specific code).
**Warning signs:** Navigation resets to start destination after rotation.

### Pitfall 6: Feature Module Cannot Access Routes Without Circular Dependency
**What goes wrong:** `app:auth` needs to navigate to `DashboardRoute` (defined in `composeApp`) but `composeApp` depends on `app:auth`. Circular dependency.
**Why it happens:** Routes are defined in `composeApp` which depends on feature modules.
**How to avoid:** Define ALL route classes in a shared location accessible to both `composeApp` and feature modules. Options: (a) put routes in `composeApp` and have feature modules receive navigation callbacks (lambdas) instead of direct NavController access, or (b) put routes in a shared `navigation` module. For a template, option (a) is simpler -- feature modules expose screen composables with callback parameters (`onLoginSuccess: () -> Unit`), and `composeApp` handles the actual navigation.
**Warning signs:** Circular dependency Gradle error.

## Code Examples

### Complete App.kt with Theme + Navigation

```kotlin
// Source: JetBrains navigation docs + Material3 theming docs
package com.m2f.template

import androidx.compose.runtime.Composable
import com.m2f.template.di.allAppModules
import com.m2f.template.navigation.AppNavHost
import com.m2f.template.theme.AppTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(allAppModules)
    }) {
        AppTheme {
            AppNavHost()
        }
    }
}
```

### Complete Theme Configuration (Single-File Pattern)

```kotlin
// theme/Color.kt
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    // ... matching dark variants
)

// theme/Theme.kt
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
```

### NavHost with Feature Module Integration

```kotlin
// navigation/AppNavHost.kt
@Composable
fun AppNavHost(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        onNavHostReady(navController)
    }

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
        // Auth screens
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute)
                },
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
        // Dashboard
        composable<DashboardRoute> {
            DashboardScreen()
        }
    }
}
```

### Gradle Configuration for composeApp

```kotlin
// composeApp/build.gradle.kts additions
plugins {
    // ... existing plugins
    id("org.jetbrains.kotlin.plugin.serialization")  // NEW: for @Serializable routes
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ... existing deps
            implementation(libs.navigation.compose)
            implementation(libs.koin.compose.viewmodel.navigation)
        }
    }
}
```

### Version Catalog Additions

```toml
# gradle/libs.versions.toml additions
[versions]
navigation-compose = "2.9.2"

[libraries]
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
koin-compose-viewmodel-navigation = { module = "io.insert-koin:koin-compose-viewmodel-navigation", version.ref = "koin" }
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| String-based routes (`navigate("profile/123")`) | Type-safe `@Serializable` routes (`navigate(ProfileRoute(123))`) | Navigation 2.8.0 (2024) | Compile-time safety, no runtime route parsing errors |
| Navigation 2 (`NavHost` + `NavController`) | Navigation 3 (`NavDisplay` + user-owned backstack) on Android | Nav3 1.0 stable (Nov 2025) | More flexible, but multiplatform port is still alpha |
| `rememberNavController()` only on Android | Multiplatform `rememberNavController()` | CMP 1.6+ (2024) | Navigation works identically on all targets |
| Custom theme via `CompositionLocal` | `MaterialTheme` with `lightColorScheme`/`darkColorScheme` | Material3 stable (2023) | Standardized 29-color-role system with automatic contrast |
| Platform-specific font loading | `Font(Res.font.*)` composable via Compose Resources | CMP 1.6+ (2024) | Single font loading API for all platforms |

**Deprecated/outdated:**
- **String route navigation in Compose Navigation:** Replaced by `@Serializable` type-safe routes as of Navigation 2.8.0. String routes still work but are not recommended.
- **`NavType` for custom argument types:** With `@Serializable` routes, custom `NavType` implementations are no longer needed. Kotlinx serialization handles argument encoding/decoding automatically.
- **Navigation 2 on Android-only projects:** Google recommends migrating to Navigation 3 for Android. However, for KMP projects targeting all platforms, Navigation 2.9.2 remains the stable choice until Nav 3 multiplatform reaches stable.

## Open Questions

1. **Navigation 3 multiplatform timeline**
   - What we know: Nav 3 is 1.0.0-alpha06 for multiplatform as of CMP 1.10.1 (Feb 2026). It is stable on Android since Nov 2025. JetBrains is actively working on multiplatform support.
   - What's unclear: When Nav 3 will reach stable for multiplatform (beta? 6 months? 12 months?).
   - Recommendation: Use Nav 2.9.2 now. The migration from Nav 2 to Nav 3 is well-documented. Plan migration in a future phase when Nav 3 multiplatform reaches at least beta. **Confidence: HIGH for this decision.**

2. **Custom fonts vs system fonts in template**
   - What we know: Compose Resources supports custom font loading via `Res.font.*`. The `Font()` API is `@Composable` in CMP, which means Typography must be created in a composable context.
   - What's unclear: Whether to include a custom font (e.g., Inter) in the template or use the default Material3 system font.
   - Recommendation: Use default Material3 typography (system fonts) for the template, with the `Type.kt` file clearly structured so a developer can add custom fonts by following the commented pattern. This keeps the template lightweight and avoids bundling font files. **Confidence: HIGH.**

3. **Component library location: composeApp vs dedicated module**
   - What we know: The project has feature modules (`app:auth`, `app:dashboard`) that need shared components. Components could live in `composeApp` or in a new `app:design-system` module.
   - What's unclear: Whether the dependency direction works if components are in `composeApp` (feature modules depend on `composeApp`? No -- `composeApp` depends on feature modules).
   - Recommendation: Components should be accessible to feature modules. Two options: (a) put components in `composeApp` and feature modules use callback-based APIs (screens receive styled composables as parameters), or (b) create a thin `app:design-system` module. Option (b) is cleaner but adds a module. For a template with ~5 components, option (a) is sufficient -- feature modules expose screen composables that accept lambdas, `composeApp` composes them with the component library. If the component library grows, extract to a module. **Confidence: MEDIUM -- either approach works, (a) is simpler for now.**

4. **`koin-compose-viewmodel-navigation` known issues**
   - What we know: There are reported issues with `koinNavViewModel()` on Android (crashes) and ViewModel recreation with `parametersOf` in Koin + Navigation context.
   - What's unclear: Whether these affect Koin 4.1.1 specifically (some issues were fixed in updates).
   - Recommendation: Use `koinViewModel()` (from `koin-compose-viewmodel`) as the primary approach. Only use `koinNavViewModel()` if navigation-scoped ViewModels are needed. Test on all platforms early. **Confidence: MEDIUM.**

## Sources

### Primary (HIGH confidence)
- [JetBrains Navigation Compose docs](https://kotlinlang.org/docs/multiplatform/compose-navigation.html) - Setup, type-safe routes, NavHost, NavController, platform considerations
- [JetBrains Navigation Routing docs](https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html) - `@Serializable` route definitions, browser history binding, URL serialization
- [JetBrains Navigation 3 docs](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) - Nav 3 alpha status, polymorphic serialization requirement, multiplatform artifacts
- [CMP 1.10.1 release notes](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.10.1) - Bundled library versions (nav 2.9.2, nav3 1.0.0-alpha06, lifecycle 2.10.0-alpha06)
- [Android Material3 Design docs](https://developer.android.com/develop/ui/compose/designsystems/material3) - Complete MaterialTheme setup: ColorScheme, Typography, Shapes
- [Compose Multiplatform resources usage](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html) - Font loading via `Res.font.*` composable API
- [Koin Compose docs](https://insert-koin.io/docs/reference/koin-compose/compose/) - koin-compose, koin-compose-viewmodel, koin-compose-viewmodel-navigation artifacts

### Secondary (MEDIUM confidence)
- [JetBrains CMP 1.10.0 blog post](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/) - Nav 3 introduction, preview annotation unification
- [Google Nav 3 stable announcement](https://android-developers.googleblog.com/2025/11/jetpack-navigation-3-is-stable.html) - Nav 3 1.0 stable for Android, migration guide from Nav 2
- [CMP What's New 1.10.0 docs](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) - Nav 3 alpha status for multiplatform, browser history postponed
- [Koin 4.1 blog post](https://blog.kotzilla.io/koin-4.1-is-here) - Koin 4.1 multiplatform Compose support, navigation integration

### Tertiary (LOW confidence)
- [Koin koinNavViewModel crash issue #1926](https://github.com/InsertKoinIO/koin/issues/1926) - Potential koinNavViewModel crash on Android in CMP; needs validation with Koin 4.1.1
- [Koin Nav3 ViewModel recreation issue #2337](https://github.com/InsertKoinIO/koin/issues/2337) - ViewModel recreation instead of reuse with parametersOf in Nav3; may not affect Nav 2.x

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Navigation Compose 2.9.2 is bundled with CMP 1.10.1, verified via release notes. Material3 already in use. Koin 4.1.1 confirmed to have navigation artifact.
- Architecture: HIGH - Type-safe routes with `@Serializable` are the official documented pattern. Theme system follows Material3 official guide. Component library pattern is standard Compose composition.
- Pitfalls: HIGH - Serialization plugin requirement verified by checking composeApp build.gradle.kts (missing). Back stack leak is a well-known navigation pitfall. Font composable constraint verified in Compose Resources docs.
- Navigation 3 assessment: HIGH - Alpha status confirmed via release notes (1.0.0-alpha06). Browser history limitation confirmed via what's-new docs. Decision to use Nav 2.9.2 is well-supported.

**Research date:** 2026-02-11
**Valid until:** 2026-03-11 (stable libraries; Nav 3 multiplatform status may change)
