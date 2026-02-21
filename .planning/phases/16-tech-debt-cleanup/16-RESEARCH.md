# Phase 16: Tech Debt Cleanup - Research

**Researched:** 2026-02-21
**Domain:** WASM browser persistence, Kotlin coroutine dispatcher configuration, Ktor Netty threading
**Confidence:** HIGH

## Summary

Phase 16 addresses two independent tech debt items before new v1.2 features land: (1) WASM locale selection not persisting across page reloads, and (2) server coroutine dispatcher configuration lacking explicit separation for AI streaming vs R2DBC database workloads.

The WASM locale bug has a clear root cause and straightforward fix. The `PreferencesStorage` already persists the language preference to `localStorage` via `multiplatform-settings` `StorageSettings`. The problem is a timing gap: on page reload, the `index.html` locale shim reads `navigator.languages` before Kotlin/WASM has initialized and before `App.kt`'s `LaunchedEffect` can call `setAppLocale()` to set `window.__customLocale`. The fix is to read from `localStorage` directly in the `index.html` script (before WASM loads) and also update `getAppLocale()` on WASM to read from `localStorage` as its persistence fallback rather than relying solely on the in-memory `overrideLocale` variable.

The dispatcher review requires introducing explicit, named dispatcher instances for three workload categories (database I/O, AI/network I/O, CPU-bound work) using `Dispatchers.IO.limitedParallelism()`. Currently, the `Configuration` class holds generic `io` and `default` dispatchers but nothing uses them -- all R2DBC `suspendTransaction` calls run on the caller's coroutine context (Ktor Netty's worker dispatcher), and the AI agent `streamChat()` uses a `callbackFlow` with a `runBlocking` in `awaitClose` that can block the event loop. The fix is to define purpose-specific dispatchers and wrap workloads appropriately.

**Primary recommendation:** Fix the WASM locale with a pre-WASM localStorage read in `index.html` and a WASM `getAppLocale()` fallback. For dispatchers, define named dispatcher instances in `Configuration` with `limitedParallelism()` and wrap R2DBC and AI operations in `withContext()`.

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| DEBT-04 | WASM locale selection persists across page reloads via localStorage | Root cause identified: `window.__customLocale` is not set before Compose Resources reads `navigator.languages`. Fix via pre-WASM `index.html` script that reads `localStorage` key `com.russhwolf.settings.pref_language`. See Architecture Pattern 1. |
| DEBT-05 | Server coroutine dispatcher configuration reviewed and optimized for concurrent AI + R2DBC workloads | Current state: no dispatcher separation. Fix via named dispatchers in `Configuration` with `Dispatchers.IO.limitedParallelism()` for DB and AI workloads. Remove `runBlocking` from `ChatAgent.kt` `awaitClose`. See Architecture Pattern 2. |
</phase_requirements>

## Standard Stack

### Core

No new library dependencies needed. This phase works entirely with existing infrastructure.

| Library | Version | Purpose | Relevance |
|---------|---------|---------|-----------|
| multiplatform-settings-no-arg | 1.3.0 | `Settings()` -> `StorageSettings` on WASM (localStorage) | Already persists `pref_language` to localStorage. No changes needed. |
| kotlinx-coroutines-core | 1.10.2 | `Dispatchers.IO.limitedParallelism()` | Used to create named, bounded dispatcher views for DB and AI workloads. |
| Ktor Server Netty | 3.3.0-openapi-eap-1394 | HTTP server engine | Netty uses event loop threads; blocking them causes timeouts. |
| Exposed R2DBC | 1.0.0 | `suspendTransaction()` for non-blocking DB access | R2DBC is inherently non-blocking but runs on caller's dispatcher context. |

### Supporting

No new supporting libraries needed.

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `Dispatchers.IO.limitedParallelism()` | `newFixedThreadPoolContext()` | Creates entirely new thread pool; `limitedParallelism()` reuses IO's elastic pool, which is more efficient and the modern Kotlin recommendation. |
| localStorage read in index.html | Compose `DisposableEffect` before first render | Cannot run before Compose initializes; the shim must execute before WASM boots. |

**Installation:** No new dependencies needed.

## Architecture Patterns

### Recommended Changes

```
composeApp/src/wasmJsMain/resources/index.html        # Add localStorage read before WASM init
composeApp/src/wasmJsMain/kotlin/.../AppLocale.wasmJs.kt  # Update getAppLocale() fallback
server/core/config/src/.../Configuration.kt            # Add named dispatchers
server/ai/src/.../ChatAgent.kt                         # Remove runBlocking, use proper dispatcher
```

### Pattern 1: Pre-WASM Locale Restoration from localStorage

**What:** Read the persisted locale from `localStorage` in the `index.html` `<script>` block before the WASM module loads, and set `window.__customLocale` so the `navigator.languages` shim returns the correct locale from the very first read.

**When to use:** Anytime a Kotlin/WASM app needs a browser-persisted value available before WASM initialization.

**Why it works:** The `multiplatform-settings` `StorageSettings` on WASM uses `window.localStorage` with keys in the format `com.russhwolf.settings.{key}`. The key for language is `pref_language`, so the full localStorage key is `com.russhwolf.settings.pref_language`. By reading this key in the index.html script (which runs synchronously before WASM loads), `window.__customLocale` is set before Compose Resources ever reads `navigator.languages`.

**Example (index.html):**
```html
<script>
    // Restore persisted locale from localStorage BEFORE WASM loads.
    // multiplatform-settings StorageSettings uses "com.russhwolf.settings." prefix.
    var savedLocale = localStorage.getItem("com.russhwolf.settings.pref_language");
    if (savedLocale) {
        window.__customLocale = savedLocale;
    }

    // Monkey-patch Navigator.prototype.languages (existing shim)
    var currentLanguagesImpl = Object.getOwnPropertyDescriptor(Navigator.prototype, "languages");
    var patchedLanguagesImpl = Object.assign({}, currentLanguagesImpl, {
        get: function () {
            if (window.__customLocale) {
                return [window.__customLocale];
            }
            return currentLanguagesImpl.get.apply(this);
        }
    });
    Object.defineProperty(Navigator.prototype, "languages", patchedLanguagesImpl);
</script>
```

**Also update AppLocale.wasmJs.kt:**
```kotlin
@OptIn(ExperimentalWasmJsInterop::class)
private fun getLocalStorageLocale(): JsString? =
    js("localStorage.getItem('com.russhwolf.settings.pref_language')")

actual fun getAppLocale(): String =
    overrideLocale
        ?: getLocalStorageLocale()?.toString()
        ?: browserLanguage()
```

This ensures `getAppLocale()` also has the correct fallback chain: in-memory override -> localStorage -> browser default.

### Pattern 2: Named Dispatcher Configuration

**What:** Define purpose-specific dispatchers in `Configuration` using `Dispatchers.IO.limitedParallelism()` with documented parallelism bounds for each workload category.

**When to use:** When a server handles mixed workloads (database I/O, AI/network streaming, CPU-bound tasks) that could starve each other.

**Why `limitedParallelism()` over separate thread pools:**
- Reuses the elastic `Dispatchers.IO` thread pool (threads are shared when not at limit)
- Each view is independently bounded (DB connections can't starve AI streaming)
- Modern Kotlin idiom (stable since kotlinx.coroutines 1.6)
- No manual thread pool lifecycle management

**Example (Configuration.kt):**
```kotlin
class Configuration(
    /**
     * Dispatcher for R2DBC database operations.
     * Bounded to match typical connection pool size.
     * R2DBC is non-blocking, but limiting parallelism prevents
     * overwhelming the connection pool under load.
     */
    val dbDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(16),

    /**
     * Dispatcher for AI/LLM network operations (streaming, embeddings).
     * Separate from DB to prevent long-running AI calls from
     * blocking database queries.
     */
    val aiDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(8),

    /**
     * Dispatcher for CPU-bound work (password hashing, JSON serialization).
     * Uses Default dispatcher which is sized to CPU core count.
     */
    val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,

    val maxDatabaseAttempts: Int = 3,
    val env: Env = Env(),
)
```

**Usage in repository layer:**
```kotlin
// Repositories don't need withContext because R2DBC suspendTransaction
// is already non-blocking. However, wrapping in the dbDispatcher
// provides explicit parallelism control and isolation.
suspend fun findByEmail(email: String): UserRecord? =
    withContext(config.dbDispatcher) {
        suspendTransaction(db = db) {
            UsersTable.select(UsersTable.columns)
                .where { UsersTable.email eq email }
                .toList()
                .singleOrNull()
                ?.toUserRecord()
        }
    }
```

**Usage in AI streaming:**
```kotlin
fun streamChat(userId: String, conversationId: String, input: String): Flow<String> =
    callbackFlow {
        // ... agent setup ...
        withContext(aiDispatcher) {
            agent.run(input)
        }
        close()
    }
```

### Pattern 3: Replace runBlocking in ChatAgent awaitClose

**What:** Remove the `runBlocking` call in `ChatAgent.kt`'s `awaitClose` lambda and replace with a non-blocking approach.

**Why critical:** `runBlocking` inside `awaitClose` of a `callbackFlow` blocks the thread until `agent.close()` completes. Under Ktor Netty, this can block an event loop or IO thread, causing thread starvation.

**Example fix:**
```kotlin
awaitClose {
    // Launch cleanup in a non-blocking way.
    // Agent.close() is a suspend function; use the flow's scope.
    launch {
        try { agent?.close() } catch (_: Exception) {}
    }
}
```

Alternative if `launch` is not available in the `awaitClose` context (it isn't -- `awaitClose` is a regular lambda):
```kotlin
awaitClose {
    // agent.close() is idempotent; fire-and-forget via GlobalScope
    // or handle via CoroutineScope provided to the service.
    agent?.let { a ->
        CoroutineScope(Dispatchers.IO).launch {
            try { a.close() } catch (_: Exception) {}
        }
    }
}
```

Or restructure the flow to use `invokeOnClose`:
```kotlin
fun streamChat(...): Flow<String> = callbackFlow {
    val agent = // ... create agent ...
    invokeOnClose {
        // Clean up synchronously if agent.close() is quick,
        // or use a cleanup scope
    }
    // ... emit frames ...
}
```

### Anti-Patterns to Avoid

- **Anti-pattern: `runBlocking` inside coroutine context.** Blocks the underlying thread, defeating the purpose of non-blocking R2DBC and Ktor Netty. The `runBlocking` in `ChatAgent.kt:86` is the most critical instance to fix.
- **Anti-pattern: Hardcoding `Dispatchers.IO` in service classes.** Makes testing impossible (can't inject a test dispatcher). Always use dispatchers from `Configuration` so tests can substitute `Dispatchers.Unconfined` or `StandardTestDispatcher`.
- **Anti-pattern: Wrapping every `suspendTransaction` individually.** If all repositories need the same dispatcher, provide it at the repository constructor level or via a base class, not in every function call.
- **Anti-pattern: Using `withContext` with the same dispatcher as the caller.** If Ktor is already running on Dispatchers.IO, wrapping in `withContext(Dispatchers.IO)` is a no-op. The value comes from `limitedParallelism()` views which actually bound concurrency.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Locale persistence on WASM | Custom JS interop for localStorage read/write | Read `com.russhwolf.settings.pref_language` in index.html; `multiplatform-settings` handles writes | Already persisted by `PreferencesStorage.language` setter; just need pre-WASM read |
| Thread pool management | Manual `Executors.newFixedThreadPool()` + lifecycle | `Dispatchers.IO.limitedParallelism(n)` | Elastic, managed by kotlinx-coroutines runtime, no shutdown logic needed |
| Observable settings on WASM | Custom Flow-based localStorage observer | `MutableStateFlow` wrapper (already in `PreferencesStorage`) | `StorageSettings` doesn't implement `ObservableSettings`; the existing `MutableStateFlow` approach is correct |

**Key insight:** Both fixes leverage existing infrastructure rather than adding new. The locale fix reads what `multiplatform-settings` already writes. The dispatcher fix uses a standard coroutines API that's already in the dependency tree.

## Common Pitfalls

### Pitfall 1: multiplatform-settings localStorage Key Format

**What goes wrong:** Assuming `localStorage.getItem("pref_language")` works.
**Why it happens:** `StorageSettings` prefixes all keys with `com.russhwolf.settings.`. The actual localStorage key is `com.russhwolf.settings.pref_language`.
**How to avoid:** Verify the exact key name in browser DevTools (Application > Storage > Local Storage) after setting a locale in the running app.
**Warning signs:** Locale reverts to English on reload despite localStorage appearing populated.

### Pitfall 2: suspendTransaction Dispatcher Context

**What goes wrong:** Passing a dispatcher directly to `suspendTransaction()` as a parameter.
**Why it happens:** Older Exposed versions accepted a `CoroutineContext` parameter. In Exposed 1.0.0, this overload is deprecated. The correct approach is to wrap `suspendTransaction` in `withContext()`.
**How to avoid:** Use `withContext(dispatcher) { suspendTransaction(db = db) { ... } }` instead of `suspendTransaction(db = db, context = dispatcher) { ... }`.
**Warning signs:** Deprecation warnings during compilation, or "No transaction in context" runtime errors.

### Pitfall 3: runBlocking in Coroutine Context

**What goes wrong:** `runBlocking` inside `awaitClose` blocks the underlying Netty/IO thread.
**Why it happens:** `awaitClose` is a regular (non-suspend) lambda, so developers reach for `runBlocking` to call suspend functions. Under low load this works fine, but under concurrent AI streaming + DB queries, threads get starved.
**How to avoid:** Use fire-and-forget `CoroutineScope(Dispatchers.IO).launch { ... }` for cleanup, or restructure to avoid needing a suspend call in `awaitClose`.
**Warning signs:** Server becoming unresponsive under moderate concurrent load, especially during AI streaming sessions.

### Pitfall 4: Dispatcher Parallelism Numbers

**What goes wrong:** Setting parallelism too high (exhausting connection pool) or too low (artificial bottleneck).
**Why it happens:** No clear mapping between parallelism limit and actual resource constraints.
**How to avoid:** Match DB dispatcher parallelism to R2DBC connection pool size. Match AI dispatcher to expected concurrent LLM streaming sessions. Start conservative, profile under load. Document the rationale for chosen numbers.
**Warning signs:** DB connection pool exhaustion logs, or AI requests queuing when server CPU is idle.

### Pitfall 5: Testing with Real Dispatchers

**What goes wrong:** Tests using `Configuration()` with default `Dispatchers.IO` dispatchers run on real thread pools, causing flaky timing-dependent tests.
**Why it happens:** Production dispatchers don't integrate with `TestCoroutineScheduler`.
**How to avoid:** Tests should create `Configuration()` with `Dispatchers.Unconfined` or `StandardTestDispatcher` for deterministic execution.
**Warning signs:** Tests that pass individually but fail when run in parallel or in CI.

## Code Examples

### WASM localStorage Read in index.html

```html
<!-- Source: multiplatform-settings StorageSettings WASM implementation -->
<!-- Key format: com.russhwolf.settings.{key} -->
<script>
    // 1. Restore persisted locale before WASM loads
    var savedLocale = localStorage.getItem("com.russhwolf.settings.pref_language");
    if (savedLocale) {
        window.__customLocale = savedLocale;
    }

    // 2. Monkey-patch navigator.languages (existing shim)
    var currentLanguagesImpl = Object.getOwnPropertyDescriptor(Navigator.prototype, "languages");
    var patchedLanguagesImpl = Object.assign({}, currentLanguagesImpl, {
        get: function () {
            if (window.__customLocale) {
                return [window.__customLocale];
            }
            return currentLanguagesImpl.get.apply(this);
        }
    });
    Object.defineProperty(Navigator.prototype, "languages", patchedLanguagesImpl);
</script>
```

### Updated AppLocale.wasmJs.kt

```kotlin
// Source: Existing pattern + localStorage fallback
package com.m2f.template.localization

import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
private fun setCustomLocale(locale: JsString): Unit = js("window.__customLocale = locale")

@OptIn(ExperimentalWasmJsInterop::class)
private fun clearCustomLocale(): Unit = js("window.__customLocale = null")

@OptIn(ExperimentalWasmJsInterop::class)
private fun getCustomLocale(): JsString? = js("window.__customLocale")

@OptIn(ExperimentalWasmJsInterop::class)
private fun getLocalStorageLocale(): JsString? =
    js("localStorage.getItem('com.russhwolf.settings.pref_language')")

@OptIn(ExperimentalWasmJsInterop::class)
private fun navigatorLanguage(): JsString = js("navigator.language")

@OptIn(ExperimentalWasmJsInterop::class)
private fun browserLanguage(): String = navigatorLanguage().toString().take(2)

private var overrideLocale: String? = null

actual fun setAppLocale(languageTag: String) {
    overrideLocale = languageTag
    @OptIn(ExperimentalWasmJsInterop::class)
    setCustomLocale(languageTag.toJsString())
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun getAppLocale(): String =
    overrideLocale
        ?: getLocalStorageLocale()?.toString()
        ?: browserLanguage()
```

### Named Dispatchers in Configuration

```kotlin
// Source: Kotlin official docs — Dispatchers.IO.limitedParallelism()
// https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-i-o.html
package com.m2f.core.config.configuration

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class Configuration(
    /** Bounded dispatcher for R2DBC database operations. */
    val dbDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(16),
    /** Bounded dispatcher for AI/LLM streaming and network calls. */
    val aiDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(8),
    /** CPU-bound work (hashing, serialization). */
    val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
    val maxDatabaseAttempts: Int = 3,
    val env: Env = Env(),
)
```

### PasswordHasher Using Injected Dispatcher

```kotlin
// Source: Existing PasswordHasher refactored to use Configuration dispatcher
class PasswordHasher(
    private val computeDispatcher: CoroutineDispatcher,
) {
    suspend fun hash(password: String): String = withContext(computeDispatcher) {
        BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS))
    }

    suspend fun verify(password: String, hash: String): Boolean = withContext(computeDispatcher) {
        BCrypt.checkpw(password, hash)
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `newFixedThreadPoolContext()` for DB isolation | `Dispatchers.IO.limitedParallelism(n)` | kotlinx.coroutines 1.6 (2022) | Elastic thread reuse, no manual lifecycle |
| Global `Dispatchers.IO` for everything | Named dispatcher views per workload | Community best practice ~2023 | Prevents cross-workload starvation |
| `suspendTransaction(context = ...)` | `withContext(dispatcher) { suspendTransaction(...) }` | Exposed 1.0.0 (Jan 2026) | Old overload deprecated in Exposed 1.0 |

**Deprecated/outdated:**
- `Dispatchers.IO` without `limitedParallelism()`: Still works but provides no isolation between workload types. All IO operations share the same 64-thread ceiling.
- `suspendTransaction` with `CoroutineContext` parameter: Deprecated in Exposed 1.0.0. Use `withContext()` wrapper instead.

## Open Questions

1. **Exact R2DBC connection pool size**
   - What we know: R2DBC PostgreSQL driver has a default pool, but the Exposed `R2dbcDatabase.connect {}` block in `Startup.kt` doesn't explicitly configure pool size.
   - What's unclear: What the default connection pool size is for the R2DBC PostgreSQL driver 1.0.7.RELEASE when no explicit pool config is provided.
   - Recommendation: During implementation, check `r2dbc-pool` defaults (typically 10). Set `dbDispatcher` parallelism to match. Consider adding explicit pool size configuration to `R2dbcDatabase.connect {}` for clarity.

2. **Koog agent.close() behavior**
   - What we know: `agent.close()` is a suspend function called in `awaitClose` via `runBlocking`.
   - What's unclear: How long `agent.close()` takes and whether it's safe to fire-and-forget. Does it release network connections? Can it hang?
   - Recommendation: During implementation, test `agent.close()` timing. If fast (<100ms), a fire-and-forget `launch` is fine. If slow, consider a timeout wrapper.

3. **localStorage key prefix stability**
   - What we know: `multiplatform-settings` 1.3.0 uses `com.russhwolf.settings.` prefix for `StorageSettings` on WASM.
   - What's unclear: Whether this prefix is guaranteed stable across versions or is an implementation detail.
   - Recommendation: Verify the prefix constant in the `multiplatform-settings` source. If it's internal/private, add a comment noting the coupling and pin the library version. Alternatively, read the key from a well-known constant.

## Sources

### Primary (HIGH confidence)

- **Codebase analysis** - Direct reading of all relevant source files:
  - `composeApp/src/wasmJsMain/kotlin/.../AppLocale.wasmJs.kt` - Current WASM locale implementation
  - `composeApp/src/wasmJsMain/resources/index.html` - Navigator.languages shim
  - `core/storage/src/commonMain/kotlin/.../PreferencesStorage.kt` - Settings-backed locale persistence
  - `core/storage/src/commonMain/kotlin/.../StorageModule.kt` - `Settings()` Koin binding
  - `server/core/config/src/main/kotlin/.../Configuration.kt` - Current dispatcher configuration
  - `server/ai/src/main/kotlin/.../ChatAgent.kt` - `runBlocking` in `awaitClose` (line 86)
  - `server/ai/src/main/kotlin/.../ExposedPersistenceStorage.kt` - R2DBC `suspendTransaction` usage
  - All repository files (`UserRepository`, `GroupRepository`, etc.) - `suspendTransaction` patterns
- [Kotlin official docs: Dispatchers.IO](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-i-o.html) - `limitedParallelism()` semantics
- [Kotlin official docs: limitedParallelism](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-dispatcher/limited-parallelism.html) - API contract

### Secondary (MEDIUM confidence)

- [Ktor GitHub Discussion #4790](https://github.com/ktorio/ktor/discussions/4790) - Ktor Netty dispatcher guidance for I/O operations
- [kt.academy: Best Dispatcher for Backend](https://kt.academy/article/dispatcher-for-backend) - `Dispatchers.IO.limitedParallelism(n)` recommendation for backend apps
- [multiplatform-settings GitHub README](https://github.com/russhwolf/multiplatform-settings) - StorageSettings WASM/localStorage behavior
- [Exposed 1.0 Blog Post](https://blog.jetbrains.com/kotlin/2026/01/exposed-1-0-is-now-available/) - Breaking changes including `suspendTransaction` API
- [Exposed transactions docs](https://www.jetbrains.com/help/exposed/transactions.html) - `suspendTransaction` wrapping with `withContext()`

### Tertiary (LOW confidence)

- [KTOR-7121 YouTrack](https://youtrack.jetbrains.com/issue/KTOR-7121) - testApplication dispatcher issue (referenced in project state as known concern, not directly related to Phase 16 but noted for context)

## Metadata

**Confidence breakdown:**
- WASM locale fix: HIGH - Root cause verified by reading source code; fix pattern is standard (pre-load from localStorage)
- Dispatcher architecture: HIGH - Standard Kotlin patterns, well-documented by JetBrains and community
- runBlocking fix: HIGH - Clearly identified, standard coroutine anti-pattern
- localStorage key prefix: MEDIUM - Empirically verified but reliance on internal implementation detail
- R2DBC connection pool defaults: LOW - Not explicitly verified for the specific driver version

**Research date:** 2026-02-21
**Valid until:** 2026-03-21 (stable domain, no fast-moving changes expected)
