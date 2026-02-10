# Stack Research

**Domain:** KMP Full-Stack Template with AI Agent Support
**Researched:** 2026-02-10
**Confidence:** MEDIUM-HIGH (most choices verified against official docs/releases; a few emerging areas flagged)

---

## Existing Stack (Already in Place -- Do Not Re-Add)

These are already integrated in the project. Listed here for context so the roadmap does not duplicate work.

| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.2.10 | Language (context parameters enabled) |
| Compose Multiplatform | 1.9.0-rc01 | Shared UI framework |
| Ktor Server | 3.3.0-openapi-eap-1394 | Backend HTTP framework |
| Exposed | 1.0.0-rc-1 | Database ORM (R2DBC + JDBC) |
| PostgreSQL + R2DBC | 42.7.7 / 1.0.7.RELEASE | Database driver |
| Koin | 4.1.1 | Dependency injection |
| Arrow | 2.1.2 | Functional programming (core/fx/resilience) |
| SuspendApp | 2.1.2 | Graceful lifecycle |
| JJWT | 0.13.0 | JWT auth |
| Kotest | 6.0.1 | Testing assertions |
| Logback | 1.5.18 | Server logging |
| kotlinx-coroutines | 1.10.2 | Async runtime |
| Micrometer/Prometheus | 1.15.3 | Metrics |

---

## Recommended Stack (New Additions)

### 1. AI Agent Infrastructure

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| Koog (koog-agents) | 0.6.1 | AI agent framework | Official JetBrains Kotlin-native agent framework. Idiomatic DSL, multiplatform support (JVM/JS/WasmJS/iOS), built-in fault tolerance, history compression, A2A + ACP protocol support. The only serious Kotlin-first agent framework; alternatives are Java wrappers (LangChain4j) or Python interop. | HIGH |
| Koog Ktor Plugin (koog-ktor) | 0.6.1 | Ktor server integration | First-party plugin -- `install(Koog)` in Application.module(), configure LLM providers in application.yaml, call agents directly from routes. Eliminates manual wiring of LLM clients across server modules. | HIGH |
| Koog MCP support | 0.6.1 (agent-mcp module) | Model Context Protocol tools | Built-in MCP client in Koog allows agents to connect to MCP servers via stdio/SSE transport, retrieve tools, and register them. MCP is now the universal standard for agent-tool integration (97M+ monthly SDK downloads, backed by Anthropic/OpenAI/Google). | HIGH |

**Gradle dependencies:**
```kotlin
// In server/build.gradle.kts or a new server:ai module
implementation("ai.koog:koog-agents:0.6.1")
implementation("ai.koog:koog-ktor:0.6.1")
```

**Key rationale:** Koog is the only Kotlin-native agent framework with JetBrains backing and direct Ktor integration. It targets the same platforms as this project (JVM server, multiplatform clients). The koog-ktor plugin provides seamless installation as a Ktor feature, matching the existing plugin-based server architecture (auth, CORS, content-negotiation, etc.).

### 2. Multiplatform Navigation

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| AndroidX Navigation Compose (Multiplatform) | 2.9.1 | Screen navigation | `org.jetbrains.androidx.navigation:navigation-compose:2.9.1`. Official JetBrains fork of AndroidX Navigation, supports all targets (Android/iOS/Desktop/Web). Type-safe routes with kotlinx-serialization. Mature, well-documented, battle-tested on Android with full multiplatform parity. | HIGH |

**Gradle dependencies:**
```kotlin
// In composeApp/build.gradle.kts, commonMain.dependencies
implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
```

**Key rationale:** This is now THE official navigation solution for Compose Multiplatform. JetBrains contributes multiplatform support directly to the AndroidX Navigation library. Navigation 3 exists (in CMP 1.10.0) but is alpha and too early for a template -- the standard Navigation library is stable and production-ready.

### 3. UI Component Library / Adaptive Layout

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| Compose Material3 | 1.9.0 (via compose.material3 DSL) | Design system | Already bundled with CMP 1.9.x. Material3 is the standard design system for Compose. Use `compose.material3` DSL accessor in gradle -- no separate dependency needed. | HIGH |
| Material3 Adaptive | 1.2.0 | Responsive layouts | `org.jetbrains.compose.material3.adaptive:adaptive:1.2.0`. Provides `ListDetailPaneScaffold`, `SupportingPaneScaffold`, window size classes. Essential for a template that targets phones, tablets, and desktop simultaneously. | HIGH |

**Gradle dependencies:**
```kotlin
// In composeApp/build.gradle.kts, commonMain.dependencies
implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.2.0")
implementation("org.jetbrains.compose.material3.adaptive:adaptive-layout:1.2.0")
implementation("org.jetbrains.compose.material3.adaptive:adaptive-navigation:1.2.0")
```

**Key rationale:** Material3 + Adaptive is the first-party solution from Google/JetBrains. A template project should NOT add third-party design system libraries (compose-cupertino, Composive) because they add maintenance burden and version-coupling risk. The adaptive layout library handles responsive design across all screen sizes natively.

### 4. Local Storage / Preferences

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| DataStore Preferences | 1.1.7 | Key-value local storage | Official Jetpack library with KMP support since 1.1.0. Coroutine-based, transactional, replaces SharedPreferences. Used for auth tokens, user preferences, onboarding state. Production-stable. | HIGH |

**Gradle dependencies:**
```kotlin
// In shared/build.gradle.kts or composeApp, commonMain.dependencies
implementation("androidx.datastore:datastore-preferences:1.1.7")
```

**Key rationale:** DataStore Preferences is the standard for key-value storage in KMP. It is coroutine-native, integrates with Flow, and works on all KMP targets. For this template, it covers: storing JWT tokens client-side, user preference flags, theme settings. No need for SQLDelight/Room for simple key-value storage.

### 5. Client SDK Layer (Either-Based Networking)

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| Ktor Client (multiplatform) | Match server version (3.3.x or upgrade to 3.4.0) | HTTP client | Already in version catalog as ktor-client bundle. Multiplatform engines: CIO (JVM/Android), Darwin (iOS), JS (WasmJs). | HIGH |
| Arrow Core (shared module) | 2.2.0 (upgrade from 2.1.2) | Either-based error handling | Arrow 2.2.0 adds `arrow.core.raise.context` package -- same API as `arrow.core.raise` but using context parameters instead of extension functions. Since this project already uses `-Xcontext-parameters`, Arrow 2.2.0 is the natural fit. The new Racing DSL and `validate` function also improve error composition. | HIGH |

**Pattern -- Either-based SDK:**
```kotlin
// In shared module, commonMain
// Wrap all Ktor client calls in Either<ApiError, T>
context(_: Raise<ApiError>)
suspend fun login(credentials: LoginRequest): AuthResponse {
    val response = httpClient.post("/api/auth/login") {
        contentType(ContentType.Application.Json)
        setBody(credentials)
    }
    ensure(response.status == HttpStatusCode.OK) { ApiError.Unauthorized }
    return response.body()
}
```

**Key rationale:** Arrow 2.2.0's context parameter support aligns perfectly with Kotlin 2.2.10's context parameters feature flag already enabled in this project. The `Raise` context parameter pattern provides typed errors without exception handling, making every API call's failure modes explicit in the type signature. No separate Retrofit-Arrow integration needed -- Ktor client + Arrow Raise is the idiomatic KMP approach.

**Ktor version decision:** The project currently uses a Ktor OpenAPI EAP build (`3.3.0-openapi-eap-1394`). Ktor 3.4.0 stable was released 2026-01-22 with OpenAPI generation built in. **Recommend upgrading to Ktor 3.4.0** when ready, as it makes the EAP dependency unnecessary. This is a separate migration task.

### 6. Koin Compose Integration

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| koin-compose | 4.1.1 | Compose DI integration | Multiplatform Koin APIs for Compose -- `KoinApplication`, `koinInject()`. Already using koin-core/koin-ktor at 4.1.1; this is the Compose-side counterpart. | HIGH |
| koin-compose-viewmodel | 4.1.1 | ViewModel injection | `koinViewModel<T>()` for lifecycle-aware ViewModel injection in composables. Works across Android/iOS/Desktop/Web. Essential for MVVM pattern in shared UI code. | HIGH |

**Gradle dependencies:**
```kotlin
// In composeApp/build.gradle.kts, commonMain.dependencies
implementation("io.insert-koin:koin-compose:4.1.1")
implementation("io.insert-koin:koin-compose-viewmodel:4.1.1")
```

### 7. Supporting Libraries

| Library | Version | Purpose | When to Use | Confidence |
|---|---|---|---|---|
| kotlinx-datetime | 0.7.1 | Multiplatform date/time | Token expiry, timestamps, scheduling. Uses kotlin.time.Instant from stdlib (Kotlin 2.1.20+). | HIGH |
| kotlinx-serialization-json | 1.8.1 | JSON (de)serialization | Already used implicitly via Ktor. Pin explicitly in shared module for DTOs. Compatible with Kotlin 2.2.10 (1.10.0 requires Kotlin 2.3.0). | HIGH |
| Kermit | 2.0.4 | Multiplatform logging | Client-side logging across Android (Logcat), iOS (OSLog), Desktop/Web (console). Lightweight, actively maintained by Touchlab. | MEDIUM |

**Gradle dependencies:**
```kotlin
// In shared/build.gradle.kts, commonMain.dependencies
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
implementation("co.touchlab:kermit:2.0.4")
```

### 8. Setup CLI / Template Customization

| Technology | Version | Purpose | Why Recommended | Confidence |
|---|---|---|---|---|
| Bash setup script | N/A | Project renaming / customization | Simple `setup.sh` that renames packages, application IDs, module names via sed/find-replace. No Kotlin CLI framework needed -- a bash script is the standard approach for GitHub template repos (see openMF/kmp-project-template's `customizer.sh`). | MEDIUM |

**Rationale:** A Kotlin-based CLI tool (clikt, kotlinx-cli) would add compilation overhead and a native target requirement. For a "clone and run" template, a bash script that runs once is simpler, faster, and has no dependencies. The script should handle: package renaming, applicationId, server group, compose desktop mainClass, and database schema prefix.

---

## Version Compatibility Matrix

| Package | Compatible With | Notes |
|---|---|---|
| Kotlin 2.2.10 | Compose Multiplatform 1.9.x | CMP 1.9.0-rc01 supports Kotlin 2.2.x. Upgrade to 1.9.3 (stable) recommended. |
| Kotlin 2.2.10 | Arrow 2.2.0 | Arrow 2.2.0 requires Kotlin 2.2.0+ for context parameters support |
| Kotlin 2.2.10 | kotlinx-serialization 1.8.1 | 1.8.x series targets Kotlin 2.1.x; compatible with 2.2.10. Do NOT use 1.10.0 (requires Kotlin 2.3.0). |
| Kotlin 2.2.10 | Koog 0.6.1 | Koog uses kotlinx-coroutines 1.10.2, kotlinx-serialization 1.8.1 -- aligned with project. |
| Kotlin 2.2.10 | Koin 4.1.1 | Fully compatible. |
| Ktor 3.3.x EAP | Koog koog-ktor 0.6.1 | Compatible. Ktor 3.4.0 upgrade is recommended but separate. |
| AndroidX Navigation 2.9.1 | Compose Multiplatform 1.9.x | Based on Jetpack Navigation 2.9.4. Tested with CMP 1.8.2+. |
| DataStore 1.1.7 | KMP all targets | Supported since DataStore 1.1.0. |

---

## Upgrade Recommendations (Existing Dependencies)

| Current | Upgrade To | Why | Priority |
|---|---|---|---|
| Compose Multiplatform 1.9.0-rc01 | 1.9.3 | RC01 is pre-release; 1.9.3 is the latest stable in the 1.9 line (Nov 2025). Includes accessibility improvements, web compat, AGP 9.0 support. | HIGH |
| Arrow 2.1.2 | 2.2.0 | Enables context parameter-based Raise API, Racing DSL, validate function. Aligns with project's Kotlin 2.2.10 + context parameters. | HIGH |
| Exposed 1.0.0-rc-1 | 1.0.0 | Exposed 1.0.0 stable released Jan 2026. Stable API guarantee, improved R2DBC support. | HIGH |
| Ktor 3.3.0-openapi-eap | 3.4.0 (when ready) | Ktor 3.4.0 stable (Jan 2026) includes OpenAPI generation, structured concurrency. Eliminates EAP dependency. | MEDIUM (do after milestone) |

---

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|---|---|---|---|
| AI Agents | Koog | LangChain4j | Java-centric, no Kotlin DSL, no multiplatform support, no Ktor integration plugin. Koog is Kotlin-native with JetBrains backing. |
| AI Agents | Koog | Spring AI | Ties you to Spring ecosystem. This project is Ktor-based. Spring AI has no KMP story. |
| Navigation | AndroidX Navigation 2.9.1 | Voyager | Third-party, smaller community, not backed by JetBrains/Google. AndroidX Navigation is now THE official solution. |
| Navigation | AndroidX Navigation 2.9.1 | Navigation 3 | Too new (alpha in CMP 1.10.0). Good future choice but not template-ready. Revisit when stable. |
| Navigation | AndroidX Navigation 2.9.1 | Decompose | More complex, own lifecycle management. Overkill for a template. Good for advanced architectures but steeper learning curve. |
| Local Storage | DataStore Preferences | multiplatform-settings | Wrapper over platform APIs. DataStore is official Jetpack, coroutine-native, more future-proof. |
| Local Storage | DataStore Preferences | SQLDelight | Overkill for key-value storage. Use SQLDelight only if you need structured relational queries on client side. |
| Local Storage | DataStore Preferences | Room KMP | Room KMP is still alpha quality for non-Android targets. DataStore is stable. |
| Logging | Kermit | Napier | Both are viable. Kermit has more maintainers (Touchlab), composable log outputs, and Crashlytics integration. Napier is simpler but less actively maintained. |
| UI Components | Material3 + Adaptive | compose-cupertino | Adds iOS-native look but at cost of maintaining a third-party dependency. A template should stay with official components. Users can add compose-cupertino if they want iOS-native aesthetics. |
| Client DI | Koin Compose | Kodein | Project already uses Koin server-side. Using Kodein client-side would split DI frameworks. Stay consistent. |
| Setup CLI | Bash script | Kotlin CLI (clikt) | Over-engineered for a one-time rename operation. Bash works everywhere, no compilation needed. |
| Serialization | kotlinx-serialization 1.8.1 | Moshi/Gson | Not multiplatform. kotlinx-serialization is the only KMP-compatible JSON library with compiler plugin support. |

---

## What NOT to Use

| Avoid | Why | Use Instead |
|---|---|---|
| Navigation 3 | Alpha status (CMP 1.10.0-alpha). Will change. Not production-ready for a template. | AndroidX Navigation 2.9.1 |
| Compose Multiplatform 1.10.0 | Requires Kotlin 2.3.0 which is newer than project's 2.2.10. Would force Kotlin upgrade and risk cascading dependency breaks. | CMP 1.9.3 (stable, Kotlin 2.2.x compatible) |
| kotlinx-serialization 1.10.0 | Requires Kotlin 2.3.0. Incompatible with project's Kotlin 2.2.10. | 1.8.1 (compatible with Kotlin 2.2.x) |
| Spring Boot / Spring AI | Wrong ecosystem. This is a Ktor project. Adding Spring creates architectural split. | Koog + Ktor plugin |
| Retrofit | Not multiplatform. Android-only. | Ktor Client (already in project) |
| Hilt / Dagger | Not multiplatform. Android-only DI. | Koin (already in project) |
| SharedPreferences / NSUserDefaults | Platform-specific, no coroutine support, no transactional guarantees. | DataStore Preferences |
| SQLDelight for preferences | Over-engineered for key-value storage. SQL schema overhead for simple flags/tokens. | DataStore Preferences |
| Room KMP | Alpha quality on non-Android targets. Unstable API. | DataStore for KV; Exposed (server) for relational data |

---

## Gradle Version Catalog Additions

The following entries should be added to `gradle/libs.versions.toml`:

```toml
[versions]
# New additions
koog = "0.6.1"
navigation-compose = "2.9.1"
material3-adaptive = "1.2.0"
datastore = "1.1.7"
kermit = "2.0.4"
kotlinx-datetime = "0.7.1"

# Upgrades
arrow = "2.2.0"              # was 2.1.2
composeMultiplatform = "1.9.3"  # was 1.9.0-rc01
exposed = "1.0.0"              # was 1.0.0-rc-1

[libraries]
# AI Agents
koog-agents = { module = "ai.koog:koog-agents", version.ref = "koog" }
koog-ktor = { module = "ai.koog:koog-ktor", version.ref = "koog" }

# Navigation
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }

# Adaptive Layout
material3-adaptive = { module = "org.jetbrains.compose.material3.adaptive:adaptive", version.ref = "material3-adaptive" }
material3-adaptive-layout = { module = "org.jetbrains.compose.material3.adaptive:adaptive-layout", version.ref = "material3-adaptive" }
material3-adaptive-navigation = { module = "org.jetbrains.compose.material3.adaptive:adaptive-navigation", version.ref = "material3-adaptive" }

# Local Storage
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# Koin Compose
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }

# Supporting
kermit = { module = "co.touchlab:kermit", version.ref = "kermit" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

[bundles]
koog = ["koog-agents", "koog-ktor"]
navigation = ["navigation-compose"]
adaptive = ["material3-adaptive", "material3-adaptive-layout", "material3-adaptive-navigation"]
koin-compose = ["koin-compose", "koin-compose-viewmodel"]
```

---

## Sources

### HIGH Confidence (Official docs, releases, verified)
- [Koog GitHub - JetBrains](https://github.com/JetBrains/koog) -- Version 0.6.1, module structure, platform support
- [Koog Official Docs](https://docs.koog.ai/) -- Getting started, Ktor plugin, MCP integration
- [Koog Ktor Plugin Docs](https://docs.koog.ai/ktor-plugin/) -- koog-ktor dependency and installation
- [Compose Multiplatform Releases](https://github.com/JetBrains/compose-multiplatform/releases) -- Version history, 1.9.3 and 1.10.0
- [Kotlin Multiplatform Navigation Docs](https://kotlinlang.org/docs/multiplatform/compose-navigation.html) -- AndroidX Navigation 2.9.1
- [Arrow 2.2.0 Release](https://arrow-kt.io/community/blog/2025/11/01/arrow-2-2/) -- Context parameters, Racing DSL
- [Exposed 1.0 Release](https://blog.jetbrains.com/kotlin/2026/01/exposed-1-0-is-now-available/) -- Stable API
- [Ktor 3.4.0 Release](https://blog.jetbrains.com/kotlin/2026/01/ktor-3-4-0-is-now-available/) -- OpenAPI, structured concurrency
- [DataStore KMP Setup](https://developer.android.com/kotlin/multiplatform/datastore) -- Official Google docs
- [Koin 4.1 Release](https://blog.kotzilla.io/koin-4.1-is-here) -- Compose support, koin-compose-viewmodel
- [Compose Compatibility Matrix](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html) -- CMP/Kotlin version alignment
- [kotlinx-serialization Releases](https://github.com/Kotlin/kotlinx.serialization/releases) -- Version 1.8.1/1.10.0 Kotlin requirements

### MEDIUM Confidence (Multiple sources, community verified)
- [Kermit by Touchlab](https://kermit.touchlab.co/docs/) -- Version 2.0.4, multiplatform logging
- [kotlinx-datetime Releases](https://github.com/Kotlin/kotlinx-datetime/releases) -- Version 0.7.1
- [Compose Material3 Adaptive Docs](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html) -- Adaptive layout APIs
- [Koog A2A Blog Post](https://blog.jetbrains.com/ai/2025/10/koog-a2a-building-connected-ai-agents-in-kotlin/) -- A2A integration
- [Koog ACP Blog Post](https://blog.jetbrains.com/ai/2026/02/koog-x-acp-connect-an-agent-to-your-ide-and-more/) -- ACP protocol

### LOW Confidence (Needs validation during implementation)
- Koog 0.6.1 compatibility with Ktor 3.3.0-openapi-eap specifically -- may need Ktor 3.4.0 upgrade first
- DataStore 1.1.7 vs 1.2.0 on WasmJs target -- WasmJs support status unclear, may need platform-specific expect/actual

---

*Stack research for: KMP Full-Stack Template with AI Agent Support*
*Researched: 2026-02-10*
