---
status: diagnosed
trigger: "Language change triggers logout on WASM (and likely all platforms)"
created: 2026-02-21T00:00:00Z
updated: 2026-02-21T00:00:00Z
---

## Current Focus

hypothesis: CONFIRMED — key(currentLocale) destroys and recreates AppNavHost, which (1) resets NavController to LoginRoute, and (2) re-runs LaunchedEffect(Unit) calling clearSessionTokens(), wiping tokens for non-rememberMe sessions
test: Complete code reading of causation chain
expecting: N/A — root cause confirmed
next_action: Report findings

## Symptoms

expected: Changing language should update UI strings without losing user session
actual: Changing language disconnects user and navigates to login screen
errors: No error messages — behavioral issue
reproduction: Login without "remember me", change language in settings
started: Since key(currentLocale) was introduced in Phase 15 Plan 10 (commit 68b5a4b)

## Eliminated

## Evidence

- timestamp: 2026-02-21T00:01:00Z
  checked: App.kt structure (lines 20-38)
  found: KoinApplication is OUTSIDE key(currentLocale). key() wraps TerminalTheme { AppNavHost() } at line 32. Koin singletons survive but entire composable tree inside key() is destroyed/recreated on locale change.
  implication: NavController, all LaunchedEffects, and all navigation state inside AppNavHost are destroyed.

- timestamp: 2026-02-21T00:02:00Z
  checked: AppNavHost.kt LaunchedEffect(Unit) at lines 63-74
  found: On every recreation, LaunchedEffect(Unit) calls tokenStorage.clearSessionTokens(). For non-rememberMe sessions (sessionOnly=true), this WIPES access+refresh tokens from persistent Settings.
  implication: Direct cause of logout for non-rememberMe users.

- timestamp: 2026-02-21T00:03:00Z
  checked: TokenStorage.kt clearSessionTokens() at lines 30-33
  found: Reads isSessionOnly() from persistent Settings. If true, calls clearTokens() removing all tokens. Tokens are in persistent Settings (localStorage on WASM via multiplatform-settings 1.3.0), NOT in-memory.
  implication: Tokens survive app restart but are deliberately wiped by clearSessionTokens() which was designed for app-startup cleanup, not mid-session recomposition.

- timestamp: 2026-02-21T00:04:00Z
  checked: NavHost startDestination (AppNavHost.kt line 103)
  found: startDestination = LoginRoute. Fresh NavController always starts at login. Even for rememberMe=true users, there's a visible flash to LoginRoute before LaunchedEffect navigates to Dashboard.
  implication: ALL users experience broken UX. Non-rememberMe users get permanently logged out.

- timestamp: 2026-02-21T00:05:00Z
  checked: Why key(currentLocale) exists — Phase 15 Plan 10 summary
  found: key() was intentionally added because Compose Resources' stringResource(Res.string.*) reads locale from Locale.getDefault() at composition time. It does NOT reactively observe locale changes. Full tree recomposition via key() is the only way to force all stringResource() calls to re-evaluate.
  implication: Simply removing key() would fix the logout but BREAK locale switching. The fix must preserve string re-evaluation while NOT destroying NavHost.

- timestamp: 2026-02-21T00:06:00Z
  checked: WASM-specific locale behavior (AppLocale.wasmJs.kt)
  found: setAppLocale() on WASM only stores locale in a private var. Comment says "Compose Resources on WASM reads navigator.languages at startup. Runtime override requires page reload."
  implication: On WASM, locale switching may not even work for stringResource() regardless of key() — but key() still destroys the NavHost and causes logout.

## Resolution

root_cause: |
  `key(currentLocale)` in App.kt line 32 destroys and recreates the entire composable subtree
  (TerminalTheme → AppNavHost) when locale changes. This causes:
  
  1. `rememberNavController()` creates a NEW NavController → navigation state lost
  2. NavHost starts at `startDestination = LoginRoute` → user sees login screen
  3. `LaunchedEffect(Unit)` at line 63 RE-RUNS → calls `tokenStorage.clearSessionTokens()`
  4. For non-rememberMe sessions, clearSessionTokens() deletes tokens from persistent storage
  5. Subsequent token check returns null → user stays permanently on LoginRoute
  
  Impact by session type:
  - rememberMe=false: PERMANENTLY LOGGED OUT (tokens wiped)
  - rememberMe=true: Flash to LoginRoute, then auto-navigates back to Dashboard (bad UX)

fix: NOT APPLIED (diagnosis only)
verification: NOT PERFORMED (diagnosis only)
files_changed: []
