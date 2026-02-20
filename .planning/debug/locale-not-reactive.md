---
status: diagnosed
trigger: "Locale switch not reactive + auth screens revert to English"
created: 2026-02-20T00:00:00Z
updated: 2026-02-20T00:15:00Z
---

## Current Focus

hypothesis: CONFIRMED — three distinct root causes identified
test: Complete code trace of locale propagation chain
expecting: N/A — diagnosis complete
next_action: Return structured diagnosis

## Symptoms

expected: Switching locale in Profile screen immediately updates all visible text; auth screens respect saved locale
actual: 1) Profile screen doesn't refresh until navigating away/back 2) Dashboard shows mixed languages 3) Auth screens revert to English after logout
errors: No crash errors - behavioral issue
reproduction: Switch locale from English to Spanish in Profile screen
started: Current implementation (phase 15 localization)

## Eliminated

## Evidence

- timestamp: 2026-02-20T00:05:00Z
  checked: AppLocale implementations (all 4 platforms)
  found: setAppLocale() calls Locale.setDefault() on JVM/Android — this is a static setter with no Compose observability
  implication: Compose has NO way to detect when system locale changes; no recomposition is triggered

- timestamp: 2026-02-20T00:06:00Z
  checked: AppNavHost.kt ProfileRoute composable (lines 210-246)
  found: currentLocale is `var currentLocale by remember { mutableStateOf(preferencesStorage.language) }` — local to ProfileRoute only
  implication: This local state only changes the LocaleSelector display itself; it does NOT trigger recomposition of stringResource() calls elsewhere in the tree

- timestamp: 2026-02-20T00:07:00Z
  checked: How stringResource() from Compose Multiplatform Resources works
  found: stringResource() reads Locale.getDefault() at composition time. No Compose State drives it — it's a one-shot read.
  implication: Even after setAppLocale() changes Locale.getDefault(), already-composed screens don't recompose because no Compose State changed

- timestamp: 2026-02-20T00:08:00Z
  checked: PreferencesStorage.observeLanguage()
  found: observeLanguage() Flow exists but is NEVER collected anywhere in the codebase
  implication: The infrastructure for reactive observation exists but is unused

- timestamp: 2026-02-20T00:09:00Z
  checked: Auth module values-es directory
  found: app/auth/src/commonMain/composeResources/values-es/ does NOT EXIST
  implication: Auth screens will ALWAYS show English because there are no Spanish string resources for the auth module

- timestamp: 2026-02-20T00:10:00Z
  checked: Other modules values-es
  found: app/dashboard, app/profile, app/admin ALL have values-es/strings.xml; app/auth does NOT
  implication: Auth module was missed in the per-module Spanish translation effort. Plan 02 summary confirms only English was created.

- timestamp: 2026-02-20T00:11:00Z
  checked: App.kt locale initialization
  found: `val storedLocale = remember { preferencesStorage.language }` uses `remember` without key — will never re-read after initial composition
  implication: App-level locale initialization is one-shot. But this is startup behavior, not the reactivity issue.

- timestamp: 2026-02-20T00:12:00Z
  checked: No CompositionLocal for locale exists anywhere
  found: No LocalAppLocale or similar CompositionLocal defined anywhere in codebase
  implication: There is no mechanism to propagate locale changes through the Compose tree

## Resolution

root_cause: Three root causes identified — see diagnosis below
fix: See structured diagnosis
verification: N/A — diagnosis only
files_changed: []
