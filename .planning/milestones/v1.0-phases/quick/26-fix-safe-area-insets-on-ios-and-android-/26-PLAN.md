---
phase: quick-26
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
  - app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginScreen.kt
autonomous: true
requirements: [QUICK-26]

must_haves:
  truths:
    - "App content does not overlap the status bar on iOS"
    - "App content does not overlap the home indicator / navigation bar on iOS"
    - "App content does not overlap the status bar on Android"
    - "App content does not overlap the navigation bar on Android"
    - "Desktop and web layouts are unaffected (zero visual change)"
  artifacts:
    - path: "composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt"
      provides: "System bar insets padding wrapping NavHost"
      contains: "systemBarsPadding"
  key_links:
    - from: "AppNavHost.kt"
      to: "WindowInsets.systemBars"
      via: "Modifier.systemBarsPadding() on Box wrapping NavHost"
      pattern: "systemBarsPadding"
---

<objective>
Fix safe area insets on iOS and Android so content does not overlap system bars (status bar, home indicator, navigation bar).

Purpose: On both iOS and Android, the app draws behind system bars but has no insets handling, causing content to render under the status bar and home indicator/nav bar. Desktop and web are unaffected.

Output: Single-point fix in AppNavHost.kt that applies correct system bar padding for all screens.
</objective>

<execution_context>
@./.claude/get-shit-done/workflows/execute-plan.md
@./.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
@composeApp/src/commonMain/kotlin/com/m2f/template/App.kt
@composeApp/src/androidMain/kotlin/com/m2f/template/MainActivity.kt
@iosApp/iosApp/ContentView.swift
@app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginScreen.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Add systemBarsPadding to AppNavHost</name>
  <files>composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt</files>
  <action>
Wrap the `NavHost` composable inside `AppNavHost()` with a `Box` that applies `Modifier.fillMaxSize().systemBarsPadding()`.

Specifically, change the existing code at line 76-78:

```kotlin
NavHost(
    navController = navController,
    startDestination = LoginRoute,
) {
```

to:

```kotlin
Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
    NavHost(
        navController = navController,
        startDestination = LoginRoute,
    ) {
```

And close the Box after the NavHost closing brace (after line 197's `}`).

Add these imports:
- `import androidx.compose.foundation.layout.Box`
- `import androidx.compose.foundation.layout.fillMaxSize`
- `import androidx.compose.foundation.layout.systemBarsPadding`
- `import androidx.compose.ui.Modifier`

Use `systemBarsPadding()` specifically -- NOT `safeDrawingPadding()` or `safeContentPadding()`. The `safeDrawing` insets include IME (keyboard) which would cause layout issues with scrolling forms on Login/Register screens. `systemBars` only accounts for status bar (top) and navigation bar / home indicator (bottom), which is exactly what we need.

On Desktop and Web, `WindowInsets.systemBars` resolves to zero insets, so this has no visual effect on those platforms.
  </action>
  <verify>
Build the project to confirm compilation:
`./gradlew composeApp:compileKotlinDesktop` (fastest compilation target to verify syntax).
Verify the import `systemBarsPadding` resolves correctly in commonMain (it is part of `androidx.compose.foundation.layout` which is already a dependency).
  </verify>
  <done>AppNavHost wraps NavHost in a Box with systemBarsPadding. All screens (Login, Register, Dashboard, Profile, ForgotPassword) automatically get correct system bar insets on iOS and Android. Desktop/web unchanged.</done>
</task>

<task type="auto">
  <name>Task 2: Remove hardcoded top padding from LoginScreen mobile layout</name>
  <files>app/auth/src/commonMain/kotlin/com/m2f/template/app/auth/LoginScreen.kt</files>
  <action>
In `LoginMobileLayout`, the Column at line 302-306 has hardcoded padding:

```kotlin
.padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
```

Now that AppNavHost provides system bar padding globally, this hardcoded `top = 48.dp` will result in double padding on mobile (system bar insets + 48dp). Replace it with uniform padding that does not try to compensate for the status bar:

```kotlin
.padding(horizontal = 24.dp, vertical = 24.dp)
```

This gives 24dp padding on all sides, with the actual status bar offset handled by the parent `systemBarsPadding()`.

Do NOT change `LoginDesktopLayout` -- its `padding(horizontal = 64.dp, vertical = 48.dp)` is a design choice, not a status bar workaround, and desktop has zero system bar insets anyway.
  </action>
  <verify>
Build the project: `./gradlew composeApp:compileKotlinDesktop`.
Verify the padding change is correct by reading the file back and confirming no `top = 48.dp` remains in LoginMobileLayout.
  </verify>
  <done>LoginScreen mobile layout uses uniform 24dp padding instead of hardcoded 48dp top padding. Combined with Task 1's systemBarsPadding, the status bar area is correctly accounted for without double-padding.</done>
</task>

</tasks>

<verification>
1. Build succeeds: `./gradlew composeApp:compileKotlinDesktop`
2. AppNavHost.kt contains `systemBarsPadding()` wrapping the NavHost
3. LoginScreen.kt no longer has hardcoded `top = 48.dp` in mobile layout
4. No other screen files were modified (insets are handled globally)
</verification>

<success_criteria>
- AppNavHost wraps NavHost with Box + systemBarsPadding modifier
- LoginScreen mobile layout uses uniform padding (no status bar compensation)
- Project compiles successfully
- Desktop/web behavior is unchanged (zero insets on those platforms)
</success_criteria>

<output>
After completion, create `.planning/quick/26-fix-safe-area-insets-on-ios-and-android-/26-SUMMARY.md`
</output>
