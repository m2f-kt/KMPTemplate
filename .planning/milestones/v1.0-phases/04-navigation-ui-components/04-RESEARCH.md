# Phase 4: Navigation & UI Components - Research

**Researched:** 2026-02-12
**Domain:** Custom CompositionLocal theme system, 41-component terminal design system, type-safe KMP navigation, JetBrains Mono font loading
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Mirror Pencil design token names 1:1 in Compose: `TerminalTheme.colors.bg`, `.surface`, `.accent`, `.text`, `.textMuted`, `.textDim`, `.border`, `.inset`, `.accentMuted`
- Semantic colors: `.success`, `.successBg`, `.warning`, `.warningBg`, `.error`, `.errorBg`, `.info`, `.infoBg`
- All theme properties exposed as CompositionLocals (colors, spacing, typography, shadows, opacity, corner radius)
- Spacing as CompositionLocal: `LocalTerminalSpacing.current` with `.xs` (4px), `.sm` (8px), `.md` (12px), `.lg` (16px), `.xl` (20px)
- Gap as CompositionLocal: `LocalTerminalGap.current` with `.xs` (4px), `.sm` (8px), `.md` (12px), `.lg` (16px), `.xl` (24px)
- Shadows as CompositionLocal: `LocalTerminalShadows.current` with `.none`, `.sm`, `.md`, `.lg`
- Opacity as CompositionLocal: `LocalTerminalOpacity.current` with `.full` (1.0), `.high` (0.75), `.medium` (0.50), `.low` (0.25)
- Corner radius as CompositionLocal: `LocalTerminalRadius.current` with `.none` (0), `.sm` (4), `.md` (6), `.lg` (12), `.pill`, `.full`
- Single font: JetBrains Mono for all UI elements
- Exact Pencil sizes, no mobile adaptation: text-xs (11px), text-sm (12px), text-base (13px), text-md (14px), text-2xl (32px)
- Three weights: normal (400), semibold (600), bold (700)
- Implement ALL 41 components from the Pencil design system (`terminal_design_system.pen`)
- Consolidated components with enum/parameter variants, NOT separate composables per variant
- Example: `TerminalButton(variant = ButtonVariant.Secondary)` not `TerminalButtonSecondary()`
- Every component reads ALL styling from theme CompositionLocals -- no hardcoded values
- Dark mode works automatically through theme switching
- Pencil MCP as source of truth: executing agent reads exact specs from .pen file during implementation
- Three border thicknesses: thin (1px), default (2px), thick (3px)
- Border color from `--terminal-border` token

### Claude's Discretion
- Navigation route hierarchy and auth/main graph separation
- Deep linking configuration
- Back stack behavior
- Dark mode toggle mechanism (system-follow vs manual)
- Component internal implementation patterns (state management, animation)
- File organization for 41 components (single file vs package per component group)

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

## Summary

Phase 4 adds three capabilities: (1) type-safe multiplatform navigation with Navigation Compose 2.9.2, (2) a custom TerminalTheme system built entirely on CompositionLocals (NOT MaterialTheme), and (3) a 41-component UI library matching the Pencil terminal design system. This is a fundamental departure from the standard Material3 theming approach -- the user has chosen a custom muted/monospace aesthetic with JetBrains Mono font, desaturated colors, and terminal-inspired components.

The theme system uses `@Immutable` data classes for colors, spacing, gap, typography, shadows, opacity, and corner radius, each provided via `staticCompositionLocalOf`. A `TerminalTheme` object exposes all systems via `@Composable get()` properties. Components are built on `compose.foundation` primitives (`Box`, `Row`, `Column`, `BasicText`, `BasicTextField`, `Canvas`) with custom drawing, NOT Material3 wrappers. This is necessary because Material3 components (Button, TextField, Checkbox, Switch, Radio) impose Material Design styling that conflicts with the terminal aesthetic.

Navigation uses the stable Navigation Compose 2.9.2 bundled with CMP 1.10.1, with `@Serializable` route objects and `koin-compose-viewmodel-navigation:4.1.1` for DI-aware ViewModel injection. Feature modules (`app:auth`, `app:dashboard`) expose screen composables with callback parameters; `composeApp` owns the NavHost and all routing logic.

**Primary recommendation:** Build a fully custom theme system with CompositionLocals (no MaterialTheme dependency), implement all 41 components using Foundation primitives and Canvas drawing, load JetBrains Mono via Compose Resources, and wire navigation with type-safe `@Serializable` routes.

## Standard Stack

### Core (Bundled with Compose Multiplatform 1.10.1)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Compose Foundation | 1.10.1 | Base layer: `Box`, `Row`, `Column`, `BasicText`, `BasicTextField`, `Canvas`, `clickable`, `toggleable` | Foundation of all custom components; no Material3 styling imposed |
| Compose Runtime | 1.10.1 | `CompositionLocal`, `staticCompositionLocalOf`, `@Composable`, state management | Required for custom theme system |
| Compose UI | 1.10.1 | `Modifier`, `graphicsLayer`, `drawBehind`, `drawWithContent`, `dropShadow`, `innerShadow` | Drawing, shadows, layout modifiers |
| Navigation Compose | 2.9.2 | Type-safe multiplatform navigation with `@Serializable` routes | Bundled with CMP 1.10.1; stable on all targets |
| Lifecycle ViewModel Compose | 2.9.6 | ViewModel integration for navigation | Already in `libs.versions.toml` |
| Compose Resources | 1.10.1 | JetBrains Mono font loading via `Res.font.*` | Already used; font loading via composable `Font()` API |

### New Dependencies Required

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Navigation Compose (JetBrains) | 2.9.2 | `NavHost`, `composable<T>`, `NavController` | Official JetBrains KMP adaptation of Jetpack Navigation |
| Koin Compose ViewModel Navigation | 4.1.1 | `koinNavViewModel()` for nav-scoped ViewModels | Koin 4.1.1 multiplatform artifact; navigation-aware DI |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Custom CompositionLocal theme | MaterialTheme with custom ColorScheme | Material3 imposes 29 color roles, elevation system, shape system that don't match the terminal design. Custom theme maps 1:1 to Pencil tokens. **Decision: Custom theme (LOCKED).** |
| Foundation primitives for components | Material3 component wrappers | Material3 Button/TextField/Checkbox/Switch impose Material Design ripple, elevation, shape, color layers. Terminal aesthetic requires flat, bordered, monospace components. **Decision: Foundation-based (LOCKED).** |
| Manual component drawing | compose-unstyled library (composables.com) | compose-unstyled provides accessibility primitives but adds a dependency. Foundation's `toggleable`, `selectable`, `clickable` modifiers provide the same state+accessibility handling. **Recommendation: Use Foundation modifiers, skip the dependency.** |
| Navigation Compose 2.9.2 | Navigation 3 (1.0.0-alpha06) | Nav 3 is alpha on multiplatform. **Decision: Nav 2.9.2 (from prior research).** |

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

**Note:** Feature modules already apply `kmp-library-convention` which includes the serialization plugin. They do NOT need `navigation-compose` because they expose screen composables with callback lambdas, not NavGraphBuilder extensions.

## Architecture Patterns

### Recommended Project Structure

```
composeApp/src/commonMain/kotlin/com/m2f/template/
├── App.kt                                # Root: TerminalTheme + KoinApplication + NavHost
├── di/
│   └── AppModule.kt                      # Client DI module
├── navigation/
│   ├── Routes.kt                         # All @Serializable route definitions
│   └── AppNavHost.kt                     # NavHost with composable<Route> registrations
└── ui/
    ├── theme/
    │   ├── TerminalTheme.kt              # Theme composable + TerminalTheme accessor object
    │   ├── TerminalColors.kt             # @Immutable TerminalColors data class + light/dark instances
    │   ├── TerminalTypography.kt         # @Immutable TerminalTypography + JetBrains Mono loading
    │   ├── TerminalSpacing.kt            # @Immutable TerminalSpacing data class
    │   ├── TerminalGap.kt               # @Immutable TerminalGap data class
    │   ├── TerminalShadows.kt            # @Immutable TerminalShadows data class
    │   ├── TerminalOpacity.kt            # @Immutable TerminalOpacity data class
    │   ├── TerminalRadius.kt             # @Immutable TerminalRadius data class
    │   └── TerminalBorders.kt            # @Immutable TerminalBorders data class
    └── components/
        ├── button/
        │   └── TerminalButton.kt         # Default, Secondary, Ghost, Destructive, Icon variants
        ├── input/
        │   ├── TerminalInput.kt          # Input Group (Default, Filled)
        │   └── TerminalTextarea.kt       # Textarea
        ├── card/
        │   └── TerminalCard.kt           # Default, Accent, Info, Highlighted, Compact variants
        ├── feedback/
        │   ├── TerminalAlert.kt          # Info, Success, Warning, Error variants
        │   ├── TerminalBadge.kt          # Default, Accent, Success, Warning, Error variants
        │   ├── TerminalProgress.kt       # Default, Indeterminate variants
        │   └── TerminalTooltip.kt        # Tooltip
        ├── selection/
        │   ├── TerminalCheckbox.kt       # Default/Checked via boolean state
        │   ├── TerminalSwitch.kt         # Default/Checked via boolean state
        │   └── TerminalRadio.kt          # Default/Checked via boolean state
        ├── data/
        │   ├── TerminalTable.kt          # Table + Table Row
        │   └── TerminalList.kt           # List + List Items (Default, Hover, Selected, Disabled)
        └── display/
            ├── TerminalKbd.kt            # Keyboard shortcut display
            ├── TerminalAvatar.kt         # Avatar with initials
            └── TerminalDivider.kt        # Horizontal divider

composeApp/src/commonMain/composeResources/
└── font/
    ├── JetBrainsMono_Regular.ttf         # Weight 400
    ├── JetBrainsMono_SemiBold.ttf        # Weight 600
    └── JetBrainsMono_Bold.ttf            # Weight 700
```

**File organization rationale:** Components are grouped by functional category (button, input, card, feedback, selection, data, display) with one file per logical component type. This keeps related variants together (e.g., all button variants in one file with an enum) while avoiding a single massive components file. With 41 Pencil components consolidating to ~15 Kotlin files, this is manageable.

### Pattern 1: Custom CompositionLocal Theme System

**What:** A fully custom theme system using `@Immutable` data classes and `staticCompositionLocalOf` for each design subsystem. No dependency on `MaterialTheme`.
**When to use:** Always -- this is the foundation for all components.

```kotlin
// Source: https://developer.android.com/develop/ui/compose/designsystems/custom

// --- TerminalColors.kt ---
@Immutable
data class TerminalColors(
    val bg: Color,
    val surface: Color,
    val accent: Color,
    val accentMuted: Color,
    val text: Color,
    val textMuted: Color,
    val textDim: Color,
    val border: Color,
    val inset: Color,
    val success: Color,
    val successBg: Color,
    val warning: Color,
    val warningBg: Color,
    val error: Color,
    val errorBg: Color,
    val info: Color,
    val infoBg: Color,
)

val LocalTerminalColors = staticCompositionLocalOf {
    TerminalColors(
        bg = Color.Unspecified,
        surface = Color.Unspecified,
        accent = Color.Unspecified,
        accentMuted = Color.Unspecified,
        text = Color.Unspecified,
        textMuted = Color.Unspecified,
        textDim = Color.Unspecified,
        border = Color.Unspecified,
        inset = Color.Unspecified,
        success = Color.Unspecified,
        successBg = Color.Unspecified,
        warning = Color.Unspecified,
        warningBg = Color.Unspecified,
        error = Color.Unspecified,
        errorBg = Color.Unspecified,
        info = Color.Unspecified,
        infoBg = Color.Unspecified,
    )
}

// Light theme values extracted from Pencil design system variables
val TerminalLightColors = TerminalColors(
    bg = Color(0xFFE8E8E8),
    surface = Color(0xFFF5F5F5),
    accent = Color(0xFF4A9B6E),
    accentMuted = Color(0xFFD8E8DE),
    text = Color(0xFF1F1F1F),
    textMuted = Color(0xFF5A5A5A),
    textDim = Color(0xFF787878),
    border = Color(0xFFD0D0D0),
    inset = Color(0xFFEFEFEF),
    success = Color(0xFF4A9B6E),
    successBg = Color(0xFFD8E8DE),
    warning = Color(0xFFA08840),
    warningBg = Color(0xFFEDE8D8),
    error = Color(0xFFB05A5A),
    errorBg = Color(0xFFEDDCDC),
    info = Color(0xFF4A7EB0),
    infoBg = Color(0xFFDCE6EF),
)

// Dark theme values extracted from Pencil design system variables
val TerminalDarkColors = TerminalColors(
    bg = Color(0xFF101012),
    surface = Color(0xFF1A1A1C),
    accent = Color(0xFF6BAF8A),
    accentMuted = Color(0xFF1F3028),
    text = Color(0xFFD4D4D4),
    textMuted = Color(0xFF8A8A8A),
    textDim = Color(0xFF5A5A5A),
    border = Color(0xFF2A2A2E),
    inset = Color(0xFF212124),
    success = Color(0xFF6BAF8A),
    successBg = Color(0xFF1A2820),
    warning = Color(0xFFC4A860),
    warningBg = Color(0xFF28241A),
    error = Color(0xFFCA7A7A),
    errorBg = Color(0xFF2A1A1A),
    info = Color(0xFF7AA4CA),
    infoBg = Color(0xFF1A2530),
)
```

```kotlin
// --- TerminalSpacing.kt ---
@Immutable
data class TerminalSpacing(
    val xs: Dp,  // 4.dp
    val sm: Dp,  // 8.dp
    val md: Dp,  // 12.dp
    val lg: Dp,  // 16.dp
    val xl: Dp,  // 20.dp
)

val LocalTerminalSpacing = staticCompositionLocalOf {
    TerminalSpacing(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 20.dp)
}

// --- TerminalGap.kt ---
@Immutable
data class TerminalGap(
    val xs: Dp,  // 4.dp
    val sm: Dp,  // 8.dp
    val md: Dp,  // 12.dp
    val lg: Dp,  // 16.dp
    val xl: Dp,  // 24.dp
)

val LocalTerminalGap = staticCompositionLocalOf {
    TerminalGap(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 24.dp)
}
```

```kotlin
// --- TerminalTheme.kt ---
@Composable
fun TerminalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) TerminalDarkColors else TerminalLightColors
    val typography = terminalTypography() // composable because Font() is @Composable

    CompositionLocalProvider(
        LocalTerminalColors provides colors,
        LocalTerminalTypography provides typography,
        LocalTerminalSpacing provides TerminalSpacing(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 20.dp),
        LocalTerminalGap provides TerminalGap(xs = 4.dp, sm = 8.dp, md = 12.dp, lg = 16.dp, xl = 24.dp),
        LocalTerminalShadows provides TerminalShadowValues,
        LocalTerminalOpacity provides TerminalOpacityValues,
        LocalTerminalRadius provides TerminalRadiusValues,
        LocalTerminalBorders provides TerminalBorderValues,
        content = content,
    )
}

// Theme accessor object
object TerminalTheme {
    val colors: TerminalColors
        @Composable get() = LocalTerminalColors.current
    val typography: TerminalTypography
        @Composable get() = LocalTerminalTypography.current
    val spacing: TerminalSpacing
        @Composable get() = LocalTerminalSpacing.current
    val gap: TerminalGap
        @Composable get() = LocalTerminalGap.current
    val shadows: TerminalShadows
        @Composable get() = LocalTerminalShadows.current
    val opacity: TerminalOpacity
        @Composable get() = LocalTerminalOpacity.current
    val radius: TerminalRadius
        @Composable get() = LocalTerminalRadius.current
    val borders: TerminalBorders
        @Composable get() = LocalTerminalBorders.current
}
```

### Pattern 2: JetBrains Mono Font Loading

**What:** Load JetBrains Mono TTF files from Compose Resources and create a `FontFamily` inside a `@Composable` function. The `Font()` API in Compose Multiplatform IS `@Composable`, so typography MUST be created inside a composable context.
**When to use:** Inside `TerminalTheme` composable, called once.

```kotlin
// Source: https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html

// --- TerminalTypography.kt ---
@Immutable
data class TerminalTypography(
    val fontFamily: FontFamily,
    val xs: TextStyle,    // 11.sp
    val sm: TextStyle,    // 12.sp
    val base: TextStyle,  // 13.sp
    val md: TextStyle,    // 14.sp
    val xxl: TextStyle,   // 32.sp
)

val LocalTerminalTypography = staticCompositionLocalOf {
    TerminalTypography(
        fontFamily = FontFamily.Monospace,
        xs = TextStyle.Default,
        sm = TextStyle.Default,
        base = TextStyle.Default,
        md = TextStyle.Default,
        xxl = TextStyle.Default,
    )
}

@Composable
fun terminalTypography(): TerminalTypography {
    val fontFamily = FontFamily(
        Font(Res.font.JetBrainsMono_Regular, FontWeight.Normal),
        Font(Res.font.JetBrainsMono_SemiBold, FontWeight.SemiBold),
        Font(Res.font.JetBrainsMono_Bold, FontWeight.Bold),
    )
    return TerminalTypography(
        fontFamily = fontFamily,
        xs = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Normal),
        sm = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal),
        base = TextStyle(fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal),
        md = TextStyle(fontFamily = fontFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal),
        xxl = TextStyle(fontFamily = fontFamily, fontSize = 32.sp, fontWeight = FontWeight.Bold),
    )
}
```

**CRITICAL:** Font files must be placed in `composeApp/src/commonMain/composeResources/font/` as `.ttf` files. JetBrains Mono is available from Google Fonts / JetBrains as free OFL-licensed font files.

### Pattern 3: Custom Component with Variant Enum

**What:** Consolidated component with enum-based variant selection. All styling reads from `TerminalTheme` CompositionLocals.
**When to use:** Every component that has variants (buttons, cards, alerts, badges).

```kotlin
// --- button/TerminalButton.kt ---
enum class ButtonVariant { Default, Secondary, Ghost, Destructive }

@Composable
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius
    val typography = TerminalTheme.typography

    val (backgroundColor, contentColor, borderColor) = when (variant) {
        ButtonVariant.Default -> Triple(colors.accent, colors.surface, null)
        ButtonVariant.Secondary -> Triple(colors.surface, colors.text, colors.border)
        ButtonVariant.Ghost -> Triple(Color.Transparent, colors.textMuted, null)
        ButtonVariant.Destructive -> Triple(colors.errorBg, colors.error, null)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius.sm))
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(radius.sm))
                else Modifier
            )
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TerminalTheme.gap.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.invoke()
            BasicText(
                text = text,
                style = typography.sm.copy(
                    color = contentColor,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

// Icon button variant (separate because it has no text)
@Composable
fun TerminalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius.sm))
            .border(1.dp, colors.border, RoundedCornerShape(radius.sm))
            .background(colors.surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(TerminalTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
```

### Pattern 4: Custom Toggle Components (Checkbox, Switch, Radio)

**What:** Custom checkbox, switch, and radio built on Foundation's `toggleable`/`selectable` modifiers with Canvas drawing. NOT Material3 components.
**When to use:** All toggle/selection components.

```kotlin
// --- selection/TerminalCheckbox.kt ---
@Composable
fun TerminalCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    val colors = TerminalTheme.colors
    val radius = TerminalTheme.radius

    Row(
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(radius.sm))
                .background(if (checked) colors.accent else colors.surface)
                .then(
                    if (!checked) Modifier.border(2.dp, colors.border, RoundedCornerShape(radius.sm))
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                BasicText(
                    text = "\u2713", // checkmark
                    style = TextStyle(
                        color = colors.surface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = TerminalTheme.typography.fontFamily,
                    ),
                )
            }
        }
        if (label != null) {
            BasicText(
                text = label,
                style = TerminalTheme.typography.sm.copy(color = colors.text),
            )
        }
    }
}
```

```kotlin
// --- selection/TerminalSwitch.kt ---
@Composable
fun TerminalSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    val colors = TerminalTheme.colors

    Row(
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Track (40x22 with 3px padding, 16x16 knob)
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 22.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (checked) colors.accent else colors.inset)
                .padding(3.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            // Knob
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (checked) colors.surface else colors.textDim),
            )
        }
        if (label != null) {
            BasicText(
                text = label,
                style = TerminalTheme.typography.sm.copy(color = colors.text),
            )
        }
    }
}
```

### Pattern 5: Type-Safe Navigation with @Serializable Routes

**What:** All routes defined as `@Serializable` data objects/classes. Feature modules expose screen composables with callback lambdas. `composeApp` owns the NavHost.
**When to use:** All navigation.

```kotlin
// --- navigation/Routes.kt ---
@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object DashboardRoute

// --- navigation/AppNavHost.kt ---
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
        composable<LoginRoute> {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
            )
        }
        composable<DashboardRoute> {
            DashboardScreen()
        }
    }
}
```

### Pattern 6: Shadow System

**What:** Shadows defined as `@Immutable` data class with pre-configured `DropShadow` values. Applied via `Modifier.dropShadow()` (available in CMP 1.9+/1.10.1).
**When to use:** Cards, elevated surfaces.

```kotlin
// --- TerminalShadows.kt ---
@Immutable
data class TerminalShadows(
    val none: DropShadow?,      // null = no shadow
    val sm: DropShadow,         // 0 2px 4px rgba(0,0,0,0.12)
    val md: DropShadow,         // 0 4px 8px rgba(0,0,0,0.18)
    val lg: DropShadow,         // 0 8px 16px rgba(0,0,0,0.25)
)

val LocalTerminalShadows = staticCompositionLocalOf {
    TerminalShadowValues
}

// Values extracted from Pencil design system
val TerminalShadowValues = TerminalShadows(
    none = null,
    sm = DropShadow(blur = 4.dp, color = Color(0x20000000), offsetY = 2.dp),
    md = DropShadow(blur = 8.dp, color = Color(0x30000000), offsetY = 4.dp),
    lg = DropShadow(blur = 16.dp, color = Color(0x40000000), offsetY = 8.dp),
)

// Usage: Modifier.dropShadow(RoundedCornerShape(radius), TerminalTheme.shadows.sm)
```

**Confidence: MEDIUM.** The `Modifier.dropShadow()` API was introduced in Compose Foundation 1.9 and is confirmed available in CMP 1.9+. However, exact API signature for `DropShadow` data class may differ from the examples above. The implementing agent should verify the exact API during implementation. Fallback: use `Modifier.shadow(elevation, shape)` which is the older elevation-based API, or use `Modifier.drawBehind { drawRect(...) }` with blur for manual shadow rendering.

### Anti-Patterns to Avoid

- **Using MaterialTheme or Material3 components:** The terminal design system is NOT Material Design. Do not use `MaterialTheme`, `Button`, `OutlinedTextField`, `Checkbox`, `Switch`, `RadioButton`, `AlertDialog`, or any `material3` composable. Use Foundation primitives.
- **Hardcoding color hex values in components:** Every color must come from `TerminalTheme.colors.*`. The dark/light theme switch must "just work" by changing the provided `TerminalColors` instance.
- **Using `sp` for font sizes directly:** Use `TerminalTheme.typography.xs/sm/base/md/xxl` TextStyle objects which already encode the correct size + font family.
- **Creating separate composables per variant:** Use `TerminalButton(variant = ButtonVariant.Secondary)` not `TerminalButtonSecondary()`. The user explicitly decided on consolidated components with enum parameters.
- **Passing NavController to feature modules:** Feature modules expose screen composables with callback lambdas (`onLoginSuccess: () -> Unit`). Only `composeApp` touches `NavController`.
- **Forgetting `popUpTo` after login:** Always pop the auth back stack when navigating to the main graph.
- **Keeping `compose.material3` in feature module dependencies after migration:** If feature modules no longer use Material3 composables, remove the dependency to enforce the custom design system. However, `composeApp` may still need `compose.material3` for `isSystemInDarkTheme()` which lives in `androidx.compose.foundation` (NOT material3) -- verify import location.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Type-safe navigation | String route matching | Navigation Compose 2.9.2 `@Serializable` routes | Compile-time safety, argument type checking, back stack management |
| Accessibility for toggles | Manual contentDescription / semantics | Foundation `toggleable` / `selectable` modifiers with `Role.Checkbox` / `Role.Switch` / `Role.RadioButton` | Built-in screen reader support, state announcements |
| Font loading across platforms | Platform-specific font loading code | `Font(Res.font.*, weight)` from Compose Resources | Single API for Android, iOS, Desktop, WASM |
| Dark mode detection | Platform-specific dark mode checks | `isSystemInDarkTheme()` from `compose.foundation` | Works on all CMP targets |
| Navigation-scoped ViewModels | Manual ViewModel lifecycle management | `koinViewModel()` from `koin-compose-viewmodel` | Handles ViewModel scoping and lifecycle correctly |

**Key insight:** Foundation provides the critical accessibility infrastructure (`toggleable`, `selectable`, `clickable` with `Role`) that custom components need. Material3 components are NOT needed for accessibility -- Foundation has it built in.

## Common Pitfalls

### Pitfall 1: Font() is @Composable in CMP -- Cannot Create Typography Outside Composable

**What goes wrong:** Trying to define `val TerminalTypography = ...` with `Font(Res.font.*)` at the top level fails because `Font()` is a `@Composable` function in Compose Multiplatform.
**Why it happens:** Unlike Android Jetpack Compose where `Font()` is a regular function, CMP's resource-based `Font()` is `@Composable`.
**How to avoid:** Create typography inside a `@Composable` function (e.g., `terminalTypography()`) and call it inside `TerminalTheme`. Cache the result to avoid recreating on every recomposition.
**Warning signs:** Compilation error: "@Composable invocations can only happen from the context of a @Composable function."
**Confidence: HIGH** -- verified in official Compose Resources docs.

### Pitfall 2: Using sp vs dp for Font Sizes

**What goes wrong:** Font sizes defined as `dp` values (from the Pencil design system which uses `px`) need careful conversion. Compose uses `sp` (scaled pixels) for text, which respects user accessibility settings.
**Why it happens:** The Pencil design system specifies sizes in pixels (11px, 12px, 13px, 14px, 32px). In Compose, `1.sp` roughly equals `1.px` on a standard density device, but `sp` can scale with user text size preferences.
**How to avoid:** The user decision says "exact Pencil sizes, no mobile adaptation." Use `sp` values matching the pixel values (11.sp, 12.sp, etc.). For strict pixel-exact rendering, consider using `dp` for text sizes instead of `sp`, but this breaks accessibility text scaling. Recommendation: use `sp` and accept slight variation with accessibility settings.
**Warning signs:** Text appears larger/smaller than expected on different accessibility settings.
**Confidence: MEDIUM** -- trade-off between design fidelity and accessibility.

### Pitfall 3: Missing Serialization Plugin on composeApp

**What goes wrong:** `@Serializable` route classes compile but navigation crashes at runtime.
**Why it happens:** `composeApp` does NOT currently apply the serialization plugin (verified: only `org.jetbrains.kotlin.multiplatform`, `com.android.application`, `composeMultiplatform`, and `composeCompiler` are in its plugins block).
**How to avoid:** Add `id("org.jetbrains.kotlin.plugin.serialization")` to `composeApp/build.gradle.kts`.
**Warning signs:** Runtime crash: "No serializer found for class LoginRoute."
**Confidence: HIGH** -- verified by reading `composeApp/build.gradle.kts`.

### Pitfall 4: isSystemInDarkTheme() Not Working on All Platforms

**What goes wrong:** Dark mode detection returns incorrect values on iOS or Desktop.
**Why it happens:** There were historical issues with `isSystemInDarkTheme()` on iOS (Issue #3575). This has been fixed in recent CMP versions, but may still require the app to be configured correctly on iOS (respecting UIKit appearance traits).
**How to avoid:** Test dark mode on all four targets during UAT. On iOS, ensure the Info.plist doesn't force a specific appearance. On Desktop, `isSystemInDarkTheme()` reads the OS theme. On WASM, it reads the browser's `prefers-color-scheme` media query.
**Warning signs:** Theme always shows light mode on a specific platform.
**Confidence: MEDIUM** -- fixed in recent CMP but needs runtime verification.

### Pitfall 5: Foundation BasicText Has No Default Color

**What goes wrong:** Text rendered with `BasicText` is invisible or black on all backgrounds.
**Why it happens:** Unlike Material3's `Text` which inherits `LocalContentColor`, Foundation's `BasicText` renders with `Color.Black` by default. Every `BasicText` call MUST explicitly set the color in its `TextStyle`.
**How to avoid:** Always pass `style = TerminalTheme.typography.base.copy(color = TerminalTheme.colors.text)`. Consider creating a `TerminalText` helper composable that automatically applies theme colors:
```kotlin
@Composable
fun TerminalText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TerminalTheme.typography.base,
    color: Color = TerminalTheme.colors.text,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color),
    )
}
```
**Warning signs:** Text invisible on dark backgrounds; text always black.
**Confidence: HIGH** -- verified behavior of BasicText.

### Pitfall 6: compose.material3 Dependency Still Needed for isSystemInDarkTheme

**What goes wrong:** Removing `compose.material3` from dependencies causes `isSystemInDarkTheme()` to become unavailable.
**Why it happens:** `isSystemInDarkTheme()` is in `androidx.compose.foundation.isSystemInDarkTheme` (Foundation package), NOT Material3. However, it is re-exported by Material3. If only Foundation is on the classpath, the import should still work.
**How to avoid:** Verify that `isSystemInDarkTheme()` is importable from `compose.foundation` alone. If not, keep `compose.material3` in `composeApp` dependencies but do NOT use any Material3 composables. Feature modules can remove `compose.material3` entirely.
**Warning signs:** Import error for `isSystemInDarkTheme`.
**Confidence: MEDIUM** -- needs build-time verification.

### Pitfall 7: Back Stack Leak After Authentication

**What goes wrong:** User presses back from dashboard and returns to login screen.
**Why it happens:** `navController.navigate(DashboardRoute)` without `popUpTo`.
**How to avoid:** Always use:
```kotlin
navController.navigate(DashboardRoute) {
    popUpTo<LoginRoute> { inclusive = true }
}
```
**Confidence: HIGH** -- well-known navigation pattern.

## Code Examples

### Complete TerminalColors with All Pencil Tokens (Verified from .pen file)

```kotlin
// Light theme -- extracted from terminal_design_system.pen variables (first value of each pair)
val TerminalLightColors = TerminalColors(
    bg = Color(0xFFE8E8E8),           // --terminal-bg light
    surface = Color(0xFFF5F5F5),      // --terminal-surface light
    accent = Color(0xFF4A9B6E),       // --terminal-accent light
    accentMuted = Color(0xFFD8E8DE),  // --terminal-accent-muted light
    text = Color(0xFF1F1F1F),         // --terminal-text light
    textMuted = Color(0xFF5A5A5A),    // --terminal-text-muted light
    textDim = Color(0xFF787878),      // --terminal-text-dim light
    border = Color(0xFFD0D0D0),       // --terminal-border light
    inset = Color(0xFFEFEFEF),        // --terminal-inset light
    success = Color(0xFF4A9B6E),      // --terminal-success light
    successBg = Color(0xFFD8E8DE),    // --terminal-success-bg light
    warning = Color(0xFFA08840),      // --terminal-warning light
    warningBg = Color(0xFFEDE8D8),    // --terminal-warning-bg light
    error = Color(0xFFB05A5A),        // --terminal-error light
    errorBg = Color(0xFFEDDCDC),      // --terminal-error-bg light
    info = Color(0xFF4A7EB0),         // --terminal-info light
    infoBg = Color(0xFFDCE6EF),       // --terminal-info-bg light
)

// Dark theme -- extracted from terminal_design_system.pen variables (second value of each pair)
val TerminalDarkColors = TerminalColors(
    bg = Color(0xFF101012),           // --terminal-bg dark
    surface = Color(0xFF1A1A1C),      // --terminal-surface dark
    accent = Color(0xFF6BAF8A),       // --terminal-accent dark
    accentMuted = Color(0xFF1F3028),  // --terminal-accent-muted dark
    text = Color(0xFFD4D4D4),         // --terminal-text dark
    textMuted = Color(0xFF8A8A8A),    // --terminal-text-muted dark
    textDim = Color(0xFF5A5A5A),      // --terminal-text-dim dark
    border = Color(0xFF2A2A2E),       // --terminal-border dark
    inset = Color(0xFF212124),        // --terminal-inset dark
    success = Color(0xFF6BAF8A),      // --terminal-success dark
    successBg = Color(0xFF1A2820),    // --terminal-success-bg dark
    warning = Color(0xFFC4A860),      // --terminal-warning dark
    warningBg = Color(0xFF28241A),    // --terminal-warning-bg dark
    error = Color(0xFFCA7A7A),        // --terminal-error dark
    errorBg = Color(0xFF2A1A1A),      // --terminal-error-bg dark
    info = Color(0xFF7AA4CA),         // --terminal-info dark
    infoBg = Color(0xFF1A2530),       // --terminal-info-bg dark
)
```

### Complete Border System

```kotlin
// --- TerminalBorders.kt ---
@Immutable
data class TerminalBorders(
    val thin: Dp,     // 1.dp
    val default: Dp,  // 2.dp
    val thick: Dp,    // 3.dp
)

val LocalTerminalBorders = staticCompositionLocalOf {
    TerminalBorderValues
}

val TerminalBorderValues = TerminalBorders(
    thin = 1.dp,
    default = 2.dp,
    thick = 3.dp,
)

// Usage: Modifier.border(TerminalTheme.borders.thin, TerminalTheme.colors.border, shape)
```

### Complete TerminalRadius System

```kotlin
// --- TerminalRadius.kt ---
@Immutable
data class TerminalRadius(
    val none: Dp,   // 0.dp
    val sm: Dp,     // 4.dp
    val md: Dp,     // 6.dp
    val lg: Dp,     // 12.dp
    val pill: Dp,   // 24.dp (height/2 equivalent for standard sizes)
    val full: Dp,   // 9999.dp (effectively circular)
)

val LocalTerminalRadius = staticCompositionLocalOf {
    TerminalRadiusValues
}

val TerminalRadiusValues = TerminalRadius(
    none = 0.dp,
    sm = 4.dp,
    md = 6.dp,
    lg = 12.dp,
    pill = 24.dp,
    full = 9999.dp,
)
```

### Complete Opacity System

```kotlin
// --- TerminalOpacity.kt ---
@Immutable
data class TerminalOpacity(
    val full: Float,    // 1.0f
    val high: Float,    // 0.75f
    val medium: Float,  // 0.50f
    val low: Float,     // 0.25f
)

val LocalTerminalOpacity = staticCompositionLocalOf {
    TerminalOpacityValues
}

val TerminalOpacityValues = TerminalOpacity(
    full = 1.0f,
    high = 0.75f,
    medium = 0.50f,
    low = 0.25f,
)
```

### Complete App.kt Integration

```kotlin
@Composable
fun App() {
    KoinApplication(application = {
        modules(allAppModules)
    }) {
        TerminalTheme {
            AppNavHost()
        }
    }
}
```

### Component Variant Mapping (All 41 Pencil Components -> Kotlin Files)

| Pencil Component | Kotlin File | API |
|-----------------|-------------|-----|
| Terminal Button/Default | `TerminalButton.kt` | `TerminalButton(variant = ButtonVariant.Default)` |
| Terminal Button/Secondary | `TerminalButton.kt` | `TerminalButton(variant = ButtonVariant.Secondary)` |
| Terminal Button/Ghost | `TerminalButton.kt` | `TerminalButton(variant = ButtonVariant.Ghost)` |
| Terminal Button/Destructive | `TerminalButton.kt` | `TerminalButton(variant = ButtonVariant.Destructive)` |
| Terminal Icon Button | `TerminalButton.kt` | `TerminalIconButton(onClick, content)` |
| Terminal Input Group | `TerminalInput.kt` | `TerminalInput(state = InputState.Empty)` |
| Terminal Input Group/Filled | `TerminalInput.kt` | `TerminalInput(state = InputState.Filled)` |
| Terminal Textarea | `TerminalTextarea.kt` | `TerminalTextarea(value, onValueChange)` |
| Terminal Card | `TerminalCard.kt` | `TerminalCard(variant = CardVariant.Default)` |
| Terminal Card/Accent | `TerminalCard.kt` | `TerminalCard(variant = CardVariant.Accent)` |
| Terminal Card/Info | `TerminalCard.kt` | `TerminalCard(variant = CardVariant.Info)` |
| Terminal Card/Highlighted | `TerminalCard.kt` | `TerminalCard(variant = CardVariant.Highlighted)` |
| Terminal Card/Compact | `TerminalCard.kt` | `TerminalCard(variant = CardVariant.Compact)` |
| Terminal Alert/Info | `TerminalAlert.kt` | `TerminalAlert(variant = AlertVariant.Info)` |
| Terminal Alert/Success | `TerminalAlert.kt` | `TerminalAlert(variant = AlertVariant.Success)` |
| Terminal Alert/Warning | `TerminalAlert.kt` | `TerminalAlert(variant = AlertVariant.Warning)` |
| Terminal Alert/Error | `TerminalAlert.kt` | `TerminalAlert(variant = AlertVariant.Error)` |
| Terminal Badge/Default | `TerminalBadge.kt` | `TerminalBadge(variant = BadgeVariant.Default)` |
| Terminal Badge/Accent | `TerminalBadge.kt` | `TerminalBadge(variant = BadgeVariant.Accent)` |
| Terminal Badge/Success | `TerminalBadge.kt` | `TerminalBadge(variant = BadgeVariant.Success)` |
| Terminal Badge/Warning | `TerminalBadge.kt` | `TerminalBadge(variant = BadgeVariant.Warning)` |
| Terminal Badge/Error | `TerminalBadge.kt` | `TerminalBadge(variant = BadgeVariant.Error)` |
| Terminal Checkbox/Default | `TerminalCheckbox.kt` | `TerminalCheckbox(checked = false)` |
| Terminal Checkbox/Checked | `TerminalCheckbox.kt` | `TerminalCheckbox(checked = true)` |
| Terminal Switch/Default | `TerminalSwitch.kt` | `TerminalSwitch(checked = false)` |
| Terminal Switch/Checked | `TerminalSwitch.kt` | `TerminalSwitch(checked = true)` |
| Terminal Radio/Default | `TerminalRadio.kt` | `TerminalRadio(selected = false)` |
| Terminal Radio/Checked | `TerminalRadio.kt` | `TerminalRadio(selected = true)` |
| Terminal Table | `TerminalTable.kt` | `TerminalTable(headers, content)` |
| Terminal Table Row | `TerminalTable.kt` | `TerminalTableRow(content)` |
| Terminal Progress | `TerminalProgress.kt` | `TerminalProgress(progress = 0.67f)` |
| Terminal Progress/Indeterminate | `TerminalProgress.kt` | `TerminalProgress(indeterminate = true)` |
| Terminal Tooltip | `TerminalTooltip.kt` | `TerminalTooltip(text, content)` |
| Terminal Kbd | `TerminalKbd.kt` | `TerminalKbd(text = "Cmd+K")` |
| Terminal Avatar | `TerminalAvatar.kt` | `TerminalAvatar(initials = "JD")` |
| Terminal Divider | `TerminalDivider.kt` | `TerminalDivider()` |
| Terminal List | `TerminalList.kt` | `TerminalList(title, count, content)` |
| Terminal List Item/Default | `TerminalList.kt` | `TerminalListItem(state = ListItemState.Default)` |
| Terminal List Item/Hover | `TerminalList.kt` | `TerminalListItem(state = ListItemState.Hover)` |
| Terminal List Item/Selected | `TerminalList.kt` | `TerminalListItem(state = ListItemState.Selected)` |
| Terminal List Item/Disabled | `TerminalList.kt` | `TerminalListItem(state = ListItemState.Disabled)` |

**Total:** 41 Pencil components -> 15 Kotlin files

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Material3 ColorScheme (29 roles) | Custom CompositionLocal color system | Always available in Compose | Maps 1:1 to design system tokens; no "on*" color confusion |
| `Modifier.shadow(elevation, shape)` | `Modifier.dropShadow(shape, DropShadow(...))` | CMP 1.9.0 (Sept 2025) | CSS-like shadow control (blur, spread, offset, color) instead of elevation-based |
| `Font()` as regular function (Android) | `Font()` as `@Composable` function (CMP) | CMP 1.6.0 (2024) | Typography must be created in composable context |
| String-based navigation routes | `@Serializable` type-safe routes | Navigation 2.8.0 (2024) | Compile-time safety for all route arguments |
| Platform-specific font loading | `Res.font.*` from Compose Resources | CMP 1.6.0 (2024) | Single font API for all platforms |

## Open Questions

1. **Exact `Modifier.dropShadow()` API signature in CMP 1.10.1**
   - What we know: `dropShadow(shape, DropShadow(...))` is available in CMP 1.9+. The `DropShadow` class takes blur, color, offset, spread parameters.
   - What's unclear: The exact import path and parameter names in CMP 1.10.1. The API may have changed between 1.9 and 1.10.
   - Recommendation: The implementing agent should verify the exact API at implementation time. Fallback: use `Modifier.shadow(elevation, shape)` for basic shadows, or manual `drawBehind` for precise control. **Confidence: MEDIUM.**

2. **isSystemInDarkTheme() import from Foundation vs Material3**
   - What we know: `isSystemInDarkTheme()` is in `androidx.compose.foundation.isSystemInDarkTheme` package. It should be available from `compose.foundation` without `compose.material3`.
   - What's unclear: Whether removing `compose.material3` from `composeApp` dependencies breaks the import on all platforms.
   - Recommendation: Keep `compose.material3` in `composeApp` dependencies for now. Only use `isSystemInDarkTheme()` from it; no Material3 composables. Verify at build time. **Confidence: MEDIUM.**

3. **JetBrains Mono font file names for Compose Resources**
   - What we know: Files go in `composeResources/font/`. The generated `Res.font.*` accessor uses the filename (without extension, with underscores).
   - What's unclear: Whether font file names with hyphens work or must use underscores. JetBrains Mono distribution files are named `JetBrainsMono-Regular.ttf` -- the hyphen may cause issues.
   - Recommendation: Rename font files to use underscores: `JetBrainsMono_Regular.ttf`, `JetBrainsMono_SemiBold.ttf`, `JetBrainsMono_Bold.ttf`. **Confidence: HIGH** -- standard Compose Resources pattern.

4. **Component library location: composeApp vs shared module**
   - What we know: Feature modules (`app:auth`, `app:dashboard`) need access to components. Currently they depend on `compose.material3` for UI. `composeApp` depends on feature modules (not the reverse).
   - What's unclear: If components live in `composeApp`, feature modules cannot import them (dependency goes wrong way).
   - Recommendation: **Create a new `app:designsystem` module** that contains the theme + all components. Feature modules and `composeApp` both depend on `app:designsystem`. This resolves the circular dependency cleanly. Alternatively, feature modules can expose "headless" screens (no component library dependency) and `composeApp` wraps them with components -- but this is awkward for 41 components. The new module is cleaner. **Confidence: HIGH** for the need; the exact module name is Claude's discretion.

## Sources

### Primary (HIGH confidence)
- [Android Custom Design Systems in Compose](https://developer.android.com/develop/ui/compose/designsystems/custom) - CompositionLocal pattern, @Immutable data classes, theme object pattern
- [Android Theme Anatomy in Compose](https://developer.android.com/develop/ui/compose/designsystems/anatomy) - staticCompositionLocalOf vs compositionLocalOf, CompositionLocalProvider
- [Compose Multiplatform Resources Usage](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html) - Font() as @Composable, font loading from composeResources
- [CMP 1.10.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-110.html) - Navigation 2.9.2 version, Lifecycle 2.10.0-alpha06, Nav 3 alpha
- [CMP 1.10.0 Release Blog](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/) - Unified @Preview, Navigation 3, Hot Reload
- Pencil design system file (`terminal_design_system.pen`) - All 41 component specs, token values, light/dark theme variables (read via Pencil MCP tools)

### Secondary (MEDIUM confidence)
- [Compose Shadow APIs (Android docs)](https://developer.android.com/develop/ui/compose/graphics/draw/shadows) - dropShadow/innerShadow modifiers
- [CMP 1.9.0 What's New](https://kotlinlang.org/docs/multiplatform/whats-new-compose-190.html) - DropShadowPainter, InnerShadowPainter introduction
- [Compose Unstyled docs](https://composables.com/docs/compose-unstyled/components) - Reference for renderless component patterns (not used as dependency)
- [isSystemInDarkTheme iOS Issue #3575](https://github.com/JetBrains/compose-multiplatform/issues/3575) - Historical dark mode detection issue on iOS

### Tertiary (LOW confidence)
- [compose-shadow by LennartEgb](https://github.com/LennartEgb/compose-shadow) - Third-party shadow library for CMP (not needed if built-in API works)
- [Koin koinNavViewModel crash issue #1926](https://github.com/InsertKoinIO/koin/issues/1926) - Potential crash; use `koinViewModel()` instead of `koinNavViewModel()`

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Navigation Compose 2.9.2 and Compose Foundation are bundled with CMP 1.10.1. Custom CompositionLocal theming is a documented official pattern.
- Architecture: HIGH - CompositionLocal theme system follows official Android/Compose documentation. Component file organization is standard Compose practice. Navigation pattern from prior verified research.
- Theme token mapping: HIGH - All color values extracted directly from `terminal_design_system.pen` via Pencil MCP tools. Both light and dark theme values verified.
- Component implementation: MEDIUM - Custom Foundation-based components (especially Checkbox, Switch, Radio) require manual drawing and accessibility setup. Pattern is proven but implementation needs care.
- Shadow API: MEDIUM - `Modifier.dropShadow()` confirmed available in CMP 1.9+ but exact API signature in 1.10.1 needs build-time verification.
- Pitfalls: HIGH - Font composable constraint, serialization plugin requirement, and BasicText color defaults verified from official docs and codebase inspection.

**Research date:** 2026-02-12
**Valid until:** 2026-03-12 (stable libraries; shadow API may stabilize further)
