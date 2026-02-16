# Phase 9: WASM HTTP Engine Fix - Research

**Researched:** 2026-02-16
**Domain:** Ktor Client HTTP Engine / Kotlin Multiplatform wasmJs target
**Confidence:** HIGH

## Summary

The WASM login failure reported in Phase 8 UAT is caused by the CIO engine being used for the wasmJs target. CIO on wasmJs requires Node.js `net` module (socket-level networking), which is unavailable in browser environments. The browser error message is: `kotlin.IllegalArgumentException: Node.js net module is not available. Please verify that you are using Node.js`.

The fix is straightforward: swap the CIO engine dependency to `ktor-client-js` for the `wasmJsMain` source set. The `Js` engine (`io.ktor.client.engine.js.Js`) uses the browser's native `fetch` API, which works correctly in browser WASM environments. The `Js` engine is an `HttpClientEngineFactory<JsClientEngineConfig>` -- the same factory interface used by CIO, OkHttp, and Darwin -- so it drops into the existing `expect/actual platformEngine()` pattern with zero changes to common code.

**Primary recommendation:** Replace `ktor-client-cio` with `ktor-client-js` in `wasmJsMain.dependencies` and update `PlatformEngine.wasmJs.kt` to return `Js` instead of `CIO`. Also add CORS configuration to the server since the `Js` engine uses browser `fetch` which enforces CORS policy.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `io.ktor:ktor-client-js` | 3.4.0 | HTTP engine for JS/wasmJs targets using browser fetch API | Official Ktor engine for browser environments. Uses native `fetch` API which is the only way to make HTTP requests from browser WASM. Maven artifact: `ktor-client-js-wasm-js` (auto-resolved by Gradle for wasmJs target). |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `io.ktor:ktor-server-cors` | 3.4.0 | Server-side CORS plugin | Already in `libs.versions.toml` as `ktor-server-cors`. MUST be installed on the server when browser clients use `fetch` API, otherwise all cross-origin requests will be blocked by the browser. |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Explicit `Js` engine factory | Auto-detection (`HttpClient()` without engine) | Auto-detection works if only one engine is on classpath. Our project uses explicit `platformEngine()` expect/actual pattern which is cleaner and more maintainable -- stick with explicit. |
| `ktor-client-js` for wasmJs | Keep `ktor-client-cio` | CIO on wasmJs only works in Node.js runtime, not browser. Browser is our target. Not an option. |

**Installation (changes to `gradle/libs.versions.toml`):**
```toml
# Add under [libraries]
ktor-client-js = { group = "io.ktor", name = "ktor-client-js", version.ref = "ktor" }
```

## Architecture Patterns

### Recommended Project Structure (no structural changes needed)
```
core/sdk/src/
├── commonMain/kotlin/.../PlatformEngine.kt       # expect fun platformEngine(): HttpClientEngineFactory<*>
├── androidMain/kotlin/.../PlatformEngine.android.kt  # actual = OkHttp (unchanged)
├── iosMain/kotlin/.../PlatformEngine.ios.kt          # actual = Darwin (unchanged)
├── jvmMain/kotlin/.../PlatformEngine.jvm.kt          # actual = CIO (unchanged)
└── wasmJsMain/kotlin/.../PlatformEngine.wasmJs.kt    # actual = Js (CHANGE: was CIO)
```

### Pattern 1: Expect/Actual Engine Factory (existing pattern -- no change needed)
**What:** Each platform provides its own `HttpClientEngineFactory` via `expect/actual` declarations. Common code calls `HttpClient(platformEngine()) { ... }` without knowing which engine is used.
**When to use:** Always -- this is the established pattern in this codebase.
**Example:**
```kotlin
// commonMain -- PlatformEngine.kt (UNCHANGED)
expect fun platformEngine(): HttpClientEngineFactory<*>

// wasmJsMain -- PlatformEngine.wasmJs.kt (THE FIX)
import io.ktor.client.engine.js.Js
actual fun platformEngine(): HttpClientEngineFactory<*> = Js
```
**Source:** [Ktor Client Engines docs](https://ktor.io/docs/client-engines.html), verified against Ktor 3.4.0 source -- `Js` is a `data object : HttpClientEngineFactory<JsClientEngineConfig>`

### Pattern 2: Server CORS Configuration
**What:** The Ktor server must install the CORS plugin to allow browser-originated requests from the WASM app.
**When to use:** Whenever browser-based clients make HTTP requests to the server (which is exactly our case).
**Example:**
```kotlin
// Source: Ktor CORS docs - https://ktor.io/docs/server-cors.html
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

install(CORS) {
    allowHost("localhost:8080", schemes = listOf("http", "https"))
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Options)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Authorization)
    allowCredentials = true
}
```

### Anti-Patterns to Avoid
- **Using CIO for browser wasmJs:** CIO uses Node.js sockets -- will always fail in browser. The Ktor docs list CIO as supporting "wasmJs" but that means wasmJs *on Node.js*, not wasmJs in browser.
- **Removing the expect/actual pattern:** Do NOT switch to auto-detection. The explicit pattern gives clear control and makes engine selection visible in code.
- **Wildcard CORS (`anyHost()`):** Avoid `anyHost()` in production. Configure specific allowed hosts/origins.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| HTTP networking in browser WASM | Custom fetch/XMLHttpRequest interop | `ktor-client-js` (Js engine) | The Js engine correctly handles fetch API, streaming, headers, error mapping. Browser APIs have subtle differences from Node.js that the Ktor team already handles. |
| CORS handling | Manual preflight response headers | `ktor-server-cors` plugin | CORS has many edge cases (preflight caching, credential modes, exposed headers). The plugin handles all of them. |

**Key insight:** The entire fix is a dependency swap and a one-line code change. No custom solutions needed.

## Common Pitfalls

### Pitfall 1: Missing CORS on Server
**What goes wrong:** After swapping to the `Js` engine, browser requests fail with CORS errors instead of the previous Node.js `net` module error. The fix appears broken even though the engine swap was correct.
**Why it happens:** CIO bypassed browser security (it used raw sockets via Node.js interop). The `Js` engine uses browser `fetch` which enforces CORS policy. The server currently has NO CORS configuration (`Application.kt` does not install the CORS plugin). The `ktor-server-cors` dependency already exists in `libs.versions.toml` (via the `ktor-security` bundle) but is NOT installed in the application module.
**How to avoid:** Install CORS plugin on the server in the same phase as the engine swap. Test with browser DevTools Network tab to verify no CORS preflight failures.
**Warning signs:** Browser console shows `Access-Control-Allow-Origin` errors; requests work from non-browser clients but fail from WASM browser.

### Pitfall 2: Forgetting to Update libs.versions.toml
**What goes wrong:** Adding `implementation(libs.ktor.client.js)` to `build.gradle.kts` without first defining the catalog entry causes a build failure.
**Why it happens:** The version catalog doesn't currently have a `ktor-client-js` entry (only `ktor-client-cio`, `ktor-client-okhttp`, `ktor-client-darwin`).
**How to avoid:** Add `ktor-client-js = { group = "io.ktor", name = "ktor-client-js", version.ref = "ktor" }` to `gradle/libs.versions.toml` first.
**Warning signs:** Gradle sync fails with "unresolved reference: ktor.client.js".

### Pitfall 3: CIO Still on Classpath via Bundle
**What goes wrong:** The `ktor-client` bundle in `libs.versions.toml` includes `ktor-client-cio`. If any module pulls in the bundle, CIO may end up on the wasmJs classpath and auto-detection (if ever used) could pick it over Js.
**Why it happens:** The bundle was designed when CIO was the intended wasmJs engine.
**How to avoid:** This is NOT a blocking issue for this phase because we use explicit `platformEngine()`, not auto-detection. The bundle is used by other modules (server-side). Leave it as-is. Document for future cleanup.
**Warning signs:** None currently -- only a concern if someone switches to auto-detection.

### Pitfall 4: Auth Interceptor Uses Features Unsupported by Js Engine
**What goes wrong:** The `AuthInterceptor` uses `HttpSend.intercept`, which must be supported by the Js engine.
**Why it happens:** Different engines have different plugin support.
**How to avoid:** `HttpSend` is a core Ktor client plugin, not engine-specific. It works with all engines including Js. **Verified:** The interceptor uses only core Ktor client APIs (`HttpSend`, `bearerAuth`, `post`, `body`) -- none are engine-specific. **Confidence: HIGH.**
**Warning signs:** If interceptor methods threw `UnsupportedOperationException` at runtime.

### Pitfall 5: Js Engine Fetch Credentials Mode
**What goes wrong:** Browser `fetch` by default does not send credentials (cookies) cross-origin unless explicitly configured.
**Why it happens:** The `fetch` API's default `credentials` mode is `same-origin`.
**How to avoid:** This project uses `Authorization: Bearer` headers, NOT cookies, for auth. Bearer tokens are sent via `request.bearerAuth()` which adds the `Authorization` header. This header is NOT affected by `credentials` mode -- it's controlled by CORS `allowHeader(HttpHeaders.Authorization)` on the server. **No action needed** for bearer token auth.
**Warning signs:** If the project ever switches to cookie-based auth, credentials mode would need configuration.

## Code Examples

Verified patterns from official sources:

### The Complete Engine Swap (wasmJsMain)
```kotlin
// File: core/sdk/src/wasmJsMain/kotlin/com/m2f/template/sdk/PlatformEngine.wasmJs.kt
// Source: Ktor docs - https://ktor.io/docs/client-engines.html
package com.m2f.template.sdk

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual fun platformEngine(): HttpClientEngineFactory<*> = Js
```

### Gradle Dependency Change
```kotlin
// File: core/sdk/build.gradle.kts
// BEFORE:
wasmJsMain.dependencies {
    implementation(libs.ktor.client.cio)
}
// AFTER:
wasmJsMain.dependencies {
    implementation(libs.ktor.client.js)
}
```

### Version Catalog Entry
```toml
# File: gradle/libs.versions.toml -- add under [libraries] section
ktor-client-js = { group = "io.ktor", name = "ktor-client-js", version.ref = "ktor" }
```

### Server CORS Configuration
```kotlin
// File: server/src/main/kotlin/com/m2f/template/Application.kt
// Add in Application.module() before routing {}
// Source: Ktor CORS docs - https://ktor.io/docs/server-cors.html
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

install(CORS) {
    allowHost("localhost:8080", schemes = listOf("http", "https"))
    // For development, you may want broader access:
    // anyHost() // DO NOT use in production
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Options)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Authorization)
    allowCredentials = true
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| CIO for all non-mobile targets (including wasmJs) | CIO for JVM/Node.js, Js engine for browser wasmJs | Ktor 3.1.0 added CIO wasmJs support (Node.js only); Js engine existed since Ktor 2.x for JS target and got wasmJs support in 3.x | Must use the correct engine per runtime environment |
| No CORS needed (CIO bypassed browser security) | CORS required when using Js engine (fetch API) | Always was required for browser clients; was masked by CIO using Node.js sockets | Server must install CORS plugin |

**Correcting Phase 3 research:**
- Phase 3 research stated: _"`ktor-client-js` engine: Use `ktor-client-cio` for WasmJs (CIO has WasmJs support as of Ktor 3.x). The `Js` engine is for legacy JS target only."_
- **This was WRONG.** CIO on wasmJs works only in **Node.js** runtime. For **browser** runtime (which is what `wasmJs { browser() }` compiles to), the `Js` engine is required. The `Js` engine is NOT legacy -- it's the correct engine for browser-based JS and wasmJs targets. The `ktor-client-js` artifact publishes wasmJs variants (Maven artifact: `ktor-client-js-wasm-js`, version 3.4.0 confirmed on Maven Central).

## Open Questions

1. **CORS configuration scope for production**
   - What we know: The server needs CORS installed. For development, `localhost` origins are sufficient. For production, specific domain origins must be configured.
   - What's unclear: What production domains will be used. Whether CORS config should be environment-driven.
   - Recommendation: Add CORS with configurable origins (read from `Configuration`). Use restrictive defaults. This can be enhanced later but a working CORS config is needed NOW for the WASM fix to work.

2. **WASM development server port**
   - What we know: The Compose/WASM dev server runs on a different port than the Ktor backend (typically 8080). The WASM app at `localhost:XXXX` makes requests to `localhost:8080`.
   - What's unclear: The exact port the WASM dev server uses (typically 8081 or 3000).
   - Recommendation: Include `allowHost("localhost:XXXX")` for the dev server port in CORS config. Check `wasmJsBrowserDevelopmentRun` task output for the actual port.

3. **WebSocket support on Js engine**
   - What we know: The project uses WebSockets for AI chat (`Ai.Chat.WS_PATH`). The Js engine supports WebSockets according to Ktor docs.
   - What's unclear: Whether WebSocket upgrade works correctly through the Js engine + CORS in browser WASM.
   - Recommendation: Test WebSocket functionality after the engine swap. This is lower priority than HTTP requests (login/register/profile) for this phase.

## Sources

### Primary (HIGH confidence)
- [Ktor Client Engines documentation](https://ktor.io/docs/client-engines.html) - Confirms Js engine uses fetch API for browser, CIO uses coroutine-based I/O requiring Node.js socket APIs
- [Ktor Full-Stack KMP Tutorial](https://ktor.io/docs/full-stack-development-with-kotlin-multiplatform.html) - Official tutorial uses `ktor-client-js` (resolved as `ktor-client-js-wasm-js`) for wasmJs targets
- [Maven Central: io.ktor:ktor-client-js-wasm-js](https://central.sonatype.com/artifact/io.ktor/ktor-client-js-wasm-js) - Confirms artifact exists at version 3.4.0, published as klib
- [Ktor GitHub source](https://github.com/ktorio/ktor) - `Js` is a `data object : HttpClientEngineFactory<JsClientEngineConfig>` in `io.ktor.client.engine.js`
- Codebase inspection: `core/sdk/src/wasmJsMain/kotlin/.../PlatformEngine.wasmJs.kt` currently returns `CIO`, `core/sdk/build.gradle.kts` has `ktor-client-cio` in `wasmJsMain.dependencies`

### Secondary (MEDIUM confidence)
- [KTOR-7675: Client CIO engine support for wasm-js and js](https://youtrack.jetbrains.com/issue/KTOR-7675) - CIO wasmJs support was added in Ktor 3.1.0, but for Node.js runtime only
- [KTOR-8192: CIO client engine error on WasmJS and JS targets](https://youtrack.jetbrains.com/issue/KTOR-8192) - Bug report about CIO errors on wasmJs (same error pattern we're seeing)
- [Ktor 3.1.0 Release Blog](https://blog.jetbrains.com/kotlin/2025/02/ktor-3-1-0-release/) - Confirms CIO JS/wasmJs support was added in 3.1.0
- [Speednet Ktor Guide](https://speednetsoftware.com/mobile-and-web-http-client-with-ktor/) - Demonstrates `ktor-client-js` for wasmJs with auto-detection pattern

### Tertiary (LOW confidence)
- [Kotlin Slack discussion](https://slack-chats.kotlinlang.org/t/18468225/hello-is-kotlin-client-support-on-wasmjs-i-m-getting-errors-) - Community confirmation that Js engine is needed for browser wasmJs (rate-limited, couldn't fully verify)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Official Ktor tutorial uses `ktor-client-js` for wasmJs; Maven Central confirms 3.4.0 artifact exists; `Js` factory confirmed as `HttpClientEngineFactory` from source
- Architecture: HIGH - Existing `expect/actual platformEngine()` pattern needs only a one-line change in the wasmJs actual; no structural changes to common code
- Pitfalls: HIGH - CORS requirement verified by inspecting server code (no CORS plugin installed); Auth interceptor uses only core Ktor APIs (engine-agnostic)
- CORS configuration: MEDIUM - Server CORS is clearly needed but exact dev server port and production domain config need validation during implementation

**Research date:** 2026-02-16
**Valid until:** 2026-03-16 (30 days - stable domain, Ktor 3.4.0 is current)
