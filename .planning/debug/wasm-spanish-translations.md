---
status: diagnosed
trigger: "Spanish translations not working on WASM - language selector changes but UI strings stay English"
created: 2026-02-21T00:00:00Z
updated: 2026-02-21T00:00:00Z
---

## Current Focus

hypothesis: CONFIRMED — Compose Resources on WASM always reads `window.navigator.languages` for locale, and the app's `setAppLocale()` + `key(currentLocale)` trick cannot change what the browser reports
test: Traced full chain from stringResource() → rememberResourceState → DefaultComposeEnvironment → Locale.current → navigator.languages
expecting: N/A — root cause confirmed
next_action: Report findings

## Symptoms

expected: After selecting Spanish in the language selector on WASM, UI strings should display in Spanish
actual: Language selector works (changes are persisted), but all UI text remains in English
errors: None reported
reproduction: Select Spanish language on WASM target, observe UI strings remain English
started: Likely always been this way on WASM

## Eliminated

- hypothesis: Compose Resources caches locale at startup and never re-checks
  evidence: Actually it re-checks on every recomposition via `DefaultComposeEnvironment.rememberEnvironment()` which calls `Locale.current`. The problem is that `Locale.current` on WASM always returns `navigator.languages` which can't be changed by app code.
  timestamp: 2026-02-21

- hypothesis: The `key(currentLocale)` trick is not triggering recomposition
  evidence: It does trigger recomposition (destroys/recreates tree), but the ResourceEnvironment still resolves to the browser locale because `Locale.current` reads `navigator.languages`. The recomposition changes nothing.
  timestamp: 2026-02-21

## Evidence

- timestamp: 2026-02-21
  checked: WASM setAppLocale() implementation
  found: Sets a private `overrideLocale` variable. This is only used by `getAppLocale()` — it does NOT affect Compose's `Locale.current` or Compose Resources' `ResourceEnvironment`.
  implication: The override is completely disconnected from the Compose Resources resolution chain.

- timestamp: 2026-02-21
  checked: Compose Resources `ResourceState.web.kt` — how `rememberResourceState` gets locale
  found: Line 17: `val environment = LocalComposeEnvironment.current.rememberEnvironment()` → uses `DefaultComposeEnvironment` which reads `Locale.current` (Compose UI Text)
  implication: Resource resolution goes through Compose's `Locale.current`, NOT through the app's `getAppLocale()`.

- timestamp: 2026-02-21
  checked: Compose UI Text `Locale.current` on WASM (`Actuals.wasm.kt` + `PlatformLocale.web.kt`)
  found: `Locale.current` calls `platformLocaleDelegate.current` which calls `userPreferredLanguages()` which is `js("window.navigator.languages")`. This ALWAYS returns the browser's configured languages.
  implication: No Kotlin/WASM code can change what `Locale.current` returns — it's hardwired to the browser.

- timestamp: 2026-02-21
  checked: Compose Resources `ResourceEnvironment.kt` — `LocalComposeEnvironment` and `ComposeEnvironment`
  found: `internal val LocalComposeEnvironment = staticCompositionLocalOf { DefaultComposeEnvironment }`. Both `ComposeEnvironment` interface and `LocalComposeEnvironment` are **internal** to the library — cannot be overridden by consumer code.
  implication: There is NO public API to provide a custom `ComposeEnvironment` that would let us override the locale for resource resolution.

- timestamp: 2026-02-21
  checked: JVM/Desktop `Locale.current` implementation for comparison
  found: Desktop's `createPlatformLocaleDelegate().current` returns `LocaleList(listOf(Locale(JavaLocale.getDefault())))`. When Android/JVM calls `Locale.setDefault(...)`, `Locale.current` immediately reflects the change.
  implication: On JVM, `setAppLocale()` → `Locale.setDefault(...)` directly changes what `Locale.current` returns, so Compose Resources picks it up. On WASM, `setAppLocale()` sets a private variable that nothing in the Compose Resources chain reads.

- timestamp: 2026-02-21
  checked: Spanish resource files
  found: `values-es/strings.xml` exists in all modules (auth, profile, dashboard, admin, composeApp)
  implication: The strings exist — it's purely a locale resolution issue, not a missing translations issue.

- timestamp: 2026-02-21
  checked: Compose Multiplatform version
  found: Version 1.10.1 — no public API for locale override in Compose Resources
  implication: This is a framework limitation, not a version-specific bug

## Resolution

root_cause: |
  On WASM, the Compose Resources string resolution chain is:
  `stringResource()` → `rememberResourceState()` → `DefaultComposeEnvironment.rememberEnvironment()` → `Locale.current` → `navigator.languages` (browser JS API)

  The app's `setAppLocale("es")` sets a private `overrideLocale` variable in `AppLocale.wasmJs.kt`, but this variable is NEVER read by Compose Resources. Compose Resources always reads `window.navigator.languages` which returns the browser's configured language and CANNOT be changed by application code.

  On JVM/Android, this works because `setAppLocale()` calls `java.util.Locale.setDefault()`, which directly changes what Compose's `Locale.current` returns. On WASM, there is no equivalent mechanism — the browser's `navigator.languages` is read-only.

  The `key(currentLocale)` recomposition trick in App.kt does destroy and recreate the composable tree, but the new tree still resolves the same `navigator.languages` locale, so strings remain in English.

  `LocalComposeEnvironment` and `ComposeEnvironment` are **internal** to the Compose Resources library (version 1.10.1), so there is no public API to provide a custom locale override for resource resolution.

fix: (not applied — research only)
verification: (not applied — research only)
files_changed: []
