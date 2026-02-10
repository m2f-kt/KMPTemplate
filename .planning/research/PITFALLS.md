# Pitfalls Research

**Domain:** KMP Full-Stack Template (Ktor server + Compose Multiplatform clients + Koog AI agents)
**Researched:** 2026-02-10
**Confidence:** MEDIUM-HIGH (verified against official docs, GitHub repos, community reports)

## Critical Pitfalls

### Pitfall 1: JVM Target Version Mismatch Between Modules

**What goes wrong:**
The current project compiles shared/composeApp modules with `JvmTarget.JVM_11`, but Koog requires JDK 17+. When Koog is added as a dependency to a shared KMP module that also targets Android (minSdk 24, JVM 11), the build will either fail outright or produce subtle runtime class version errors. This conflict is invisible until the first Koog import in shared code.

**Why it happens:**
Template projects set a single JVM target early and forget to revisit when adding JVM-17-only dependencies. Koog's JVM 17 requirement is documented in its README but not enforced by Gradle until compilation or linking.

**How to avoid:**
- Isolate Koog into a dedicated KMP module (e.g., `:agents` or `:ai`) with its own JVM target set to 17.
- Do NOT add Koog to the `:shared` module that also compiles for Android with JVM 11.
- Keep the server module at JVM 17+ (it already runs on Netty, no Android constraint).
- If Koog agents must be callable from the shared SDK layer, define interfaces in `:shared` (JVM 11) and implementations in `:agents` (JVM 17), wired via Koin.

**Warning signs:**
- `Unsupported class file major version 61` errors at runtime
- Gradle resolution succeeds but compilation fails with "Cannot inline bytecode built with JVM target 17"
- Android build passes but JVM desktop build fails (or vice versa)

**Phase to address:**
Phase 1 (Module Structure) -- module boundaries and JVM targets must be decided before any Koog code is written.

---

### Pitfall 2: WASM Target Dependency Compatibility Trap

**What goes wrong:**
Adding a library to `commonMain` that does not publish a `wasmJs` artifact breaks the entire WASM build. Gradle resolution succeeds for other targets but fails for WASM with opaque "Could not resolve" errors. This is the single most common KMP-with-WASM failure mode, and it silently accumulates -- each new dependency is a potential WASM blocker.

**Why it happens:**
Many Kotlin libraries publish for JVM, Android, iOS, and JS but do NOT yet publish wasmJs artifacts. Developers add dependencies to `commonMain` without checking WASM compatibility, because the Android/iOS/Desktop builds all pass. The WASM build is often not run until late in development.

Libraries known to be problematic for WASM in this stack:
- Room (no WASM support)
- DataStore (limited alpha WASM support, only Preferences)
- Some Ktor client engines (only CIO and JS engines support WASM)
- Koin annotations/KSP (codegen limitations on WASM)
- Logging libraries that rely on `java.util.logging` or SLF4J

**How to avoid:**
- Run `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` after EVERY new dependency addition -- not at the end of a phase.
- Create a `nonWasmMain` intermediate source set for libraries that cannot support WASM. Use hierarchical source sets: `commonMain` -> `nonWasmMain` (Android + iOS + JVM) and `commonMain` -> `wasmJsMain`.
- Before adding any library to `commonMain`, check [klibs.io](https://klibs.io) for WASM target publication.
- Keep a compatibility matrix in the template README.

**Warning signs:**
- WASM build not included in local development workflow
- Dependencies added to `commonMain.dependencies {}` without checking target publications
- No CI step that builds the WASM target

**Phase to address:**
Phase 1 (Foundation) -- establish the WASM build verification discipline from the start, and create the intermediate source set structure.

---

### Pitfall 3: Context Parameters Migration Landmine

**What goes wrong:**
The project uses `-Xcontext-parameters` (Kotlin 2.2.10) which is the new Beta replacement for the deprecated `-Xcontext-receivers`. However, context parameters have breaking behavioral differences from context receivers: they do NOT act as implicit receivers. Code like `bar()` inside a context must change to `foo.bar()` with an explicit receiver. Callable references to context-parameter functions are also unsupported until Kotlin 2.3+. Classes cannot have context parameters at all (no direct equivalent). Any template user on an older Kotlin version or any library still using `-Xcontext-receivers` will hit compilation errors. The two flags cannot coexist.

**Why it happens:**
The project already uses `context(config: Configuration)` patterns extensively in the server module. As more modules adopt context parameters, subtle behavioral changes compound. Library authors (Arrow, Koin, Ktor plugins) may still be on context receivers or may have changed their APIs.

**How to avoid:**
- Audit all existing `context(...)` usages and ensure they use named context parameters with explicit member access (not implicit receiver calls).
- Pin Arrow version to one that fully supports context parameters (Arrow 2.1.2 does).
- Document the context parameter pattern in the template's developer guide so template users don't mix paradigms.
- Never use callable references to context-parameter functions until Kotlin 2.3.
- For classes that currently use context receivers, migrate to constructor injection or companion factory functions.

**Warning signs:**
- `context(Foo)` without a named parameter (legacy syntax that may still compile but behave differently)
- Build warnings about deprecated context receiver syntax
- IDE showing "Unresolved reference" on previously working implicit receiver calls
- Arrow or other library updates changing context receiver to context parameter APIs

**Phase to address:**
Phase 1 (Foundation) -- audit and standardize context parameter usage before building new features on top.

---

### Pitfall 4: Shared Module Bloat Destroys Template Reusability

**What goes wrong:**
The `:shared` module becomes a dumping ground for everything "multiplatform": networking, data models, error types, agent interfaces, navigation state, storage abstractions, UI state. This makes the template impossible to customize because removing any feature requires surgery on a monolithic module. Template users who don't need AI agents or specific UI components must still compile and carry all that code.

**Why it happens:**
KMP projects naturally converge on "put it in shared" because that's where `commonMain` lives. The cognitive overhead of creating new modules is high (Gradle config, source sets, target declarations), so developers resist splitting.

**How to avoid:**
- Split `:shared` into focused modules immediately:
  - `:core:model` -- domain models, error types (pure Kotlin, no platform deps)
  - `:core:network` -- Ktor client SDK, API abstractions
  - `:core:storage` -- local persistence abstractions
  - `:agents` -- Koog agent infrastructure (server + client interfaces)
  - `:ui:components` -- shared Compose components
  - `:ui:navigation` -- navigation graph and state
- Each module declares only the KMP targets it actually needs. The `:agents` module may not need WASM. The `:core:model` module needs all targets.
- Use Gradle convention plugins to reduce per-module boilerplate.

**Warning signs:**
- `:shared` has more than 20 source files
- Removing one feature breaks unrelated code
- Build times are long because changing a model recompiles everything
- Template users report "I just want X but I have to understand Y and Z"

**Phase to address:**
Phase 1 (Module Structure) -- must be the first thing designed. Retrofitting module splits is exponentially harder.

---

### Pitfall 5: Koog Agent Module Placement Creates Platform Coupling

**What goes wrong:**
Koog agents are placed in the `:shared` commonMain and expected to run on all KMP targets (Android, iOS, WASM, Desktop, Server). But Koog's actual runtime capabilities differ per platform: WASM/iOS restrict long-lived connections and parallelism, certain LLM provider SDKs only work on JVM, and persistence features (Postgres state store) are JVM-only. The result is agent code that compiles everywhere but only works correctly on JVM/Server, with runtime failures on other platforms.

**Why it happens:**
Koog advertises KMP support across JVM, JS, WasmJS, and iOS. Developers assume "supports" means "feature-complete on all targets." In practice, the JVM implementation is the most mature, and browser/WASM targets have networking constraints that silently degrade agent behavior (dropped connections, missing streaming, no persistence).

**How to avoid:**
- Design a two-tier agent architecture:
  - **Server-side agents** (JVM-only module): Full Koog capabilities, persistence, tool execution, long-running conversations.
  - **Client-side agent proxy** (KMP module): Thin layer that calls server-side agents via API, handles UI streaming, manages conversation display.
- Only put agent interfaces/DTOs in shared KMP modules. Agent execution stays on the server.
- If lightweight client-side agents are needed (e.g., on-device with Ollama on Android), create a `jvmAndAndroidMain` source set, not `commonMain`.
- Test agent code on every target early, not just JVM.

**Warning signs:**
- Agent tests only run on JVM (`jvmTest`)
- No `wasmJsTest` or `iosTest` for agent-related code
- Streaming/SSE connections fail silently on browser targets
- Koog persistence imports in `commonMain`

**Phase to address:**
Phase 2 (AI Agents) -- but module placement must be decided in Phase 1 (Module Structure).

---

### Pitfall 6: Either-Based SDK Leaks Internal Error Types Across Module Boundaries

**What goes wrong:**
The client SDK returns `Either<DomainError, T>` from every API call. But `DomainError` is a sealed interface defined in the server's config module (`:server:core:config`). The client SDK (in a KMP shared module) cannot depend on a JVM-only server module. Developers either duplicate the error types (divergence risk) or create a shared error module that couples client and server concerns. Over time, server-internal error details (like `MissingParameter`, `InvalidField`) leak into client SDKs where they make no sense.

**Why it happens:**
Arrow's `Either`/`Raise` pattern encourages a single error type hierarchy. When the same team writes client and server, they naturally reach for the same types. The template structure makes it easy because everything is in one repo.

**How to avoid:**
- Define separate error hierarchies: `ServerError` (in server modules) and `ApiError` (in shared KMP modules).
- The client SDK's `ApiError` should represent what the client can actionably handle: `NetworkError`, `Unauthorized`, `NotFound`, `ValidationFailed(fields)`, `ServerError(code, message)`.
- Map between them at the API boundary (Ktor response -> ApiError mapping in the SDK).
- Never expose server-internal error details like parameter names or stack traces in the SDK error types.

**Warning signs:**
- `DomainError` imported in client-side code
- Client code pattern-matching on server-specific error subtypes like `MissingParameter`
- Error types in `:shared` that reference server concepts (HTTP status codes, database constraint names)

**Phase to address:**
Phase 2 (Client SDK) -- but the `:core:model` module with client error types should be created in Phase 1.

---

### Pitfall 7: Koin DI Wiring Fails Silently Across KMP Targets

**What goes wrong:**
Koin modules defined in `commonMain` reference platform-specific implementations via `expect`/`actual`. On JVM and Android, everything works because Koin's reflection capabilities are full-featured. On iOS and WASM, Koin's runtime resolution fails silently or throws opaque errors because:
- Kotlin reflection is limited on non-JVM targets (no type parameter inspection, no subtype checking)
- WASM has no `koin-logger-slf4j` (SLF4J is JVM-only) -- the current `di` bundle includes it for ALL targets
- `KoinAppAlreadyStartedException` when Activity lifecycle triggers re-initialization on Android
- Koin annotations/KSP cannot resolve across Gradle module boundaries

**Why it happens:**
The current template bundles `koin-ktor`, `koin-core`, and `koin-logger-slf4j` together for the server. Extending Koin to all KMP targets requires different Koin artifacts per platform, and the server-specific logger must not leak to non-JVM targets.

**How to avoid:**
- Use `koin-core` in `commonMain`, `koin-ktor` only in server modules, `koin-compose` in composeApp, `koin-logger-slf4j` only in JVM source sets.
- Define Koin modules per feature module, not one giant module.
- Use the `expect val platformModule: Module` pattern for platform-specific bindings.
- Verify Koin graph completeness with `koin.verify()` in tests for EACH target.
- Handle the Android `KoinAppAlreadyStartedException` by using `KoinApplication.allowOverride(true)` or checking `KoinPlatformTools.defaultContext()`.

**Warning signs:**
- `ClassCastException` or `NoSuchMethodError` at Koin resolution time on iOS/WASM
- SLF4J "no binding found" warnings on non-JVM targets
- Tests pass on JVM but fail on other targets with "No definition found for class"
- Single `koinModule {}` block with 50+ definitions

**Phase to address:**
Phase 1 (DI Setup) -- Koin multiplatform wiring is foundational and must be correct before features are built.

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Put all dependencies in `commonMain` | Faster initial dev, single place to manage deps | WASM build breaks, binary size bloat on all targets, compilation time increases | Never -- always scope to the narrowest source set |
| Skip WASM testing during development | Faster iteration, avoid WASM compilation slowness | Discover WASM incompatibilities late, expensive rework to extract into source sets | Never in a template -- WASM compatibility is a selling point |
| Use `expect`/`actual` for everything | Appears clean from commonMain perspective | Explosion of actual implementations, some trivially delegating to common code; maintenance across 4-5 targets | Only for genuine platform differences (file system, crypto, platform UI entry points) |
| One big Koin module | Quick to wire, easy to find bindings | Cannot lazy-load features, app startup slows, hard to test modules in isolation | MVP only -- split before shipping template |
| Hardcode template strings (package names, app IDs) | Faster bootstrapping | CLI setup script must find-and-replace across dozens of files, fragile to new file additions | Never in a template -- use Gradle buildConfigField or a properties file |
| Server error types in shared module | Saves creating a second hierarchy | Couples client to server implementation details, blocks independent versioning | Never |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| Koog + Ktor Server | Importing `koog-agents` in the server's main module and coupling agent lifecycle to server lifecycle | Create a separate `:agents` module; use Koin to inject agent registry; manage agent lifecycle via Arrow `ResourceScope` separate from server startup |
| Ktor Client + WASM | Using the `CIO` engine in `commonMain` and assuming it works on WASM | CIO works on WASM but verify each Ktor version; for WASM, the JS engine (`ktor-client-js`) is sometimes more stable. Use `expect`/`actual` for engine selection or use `HttpClient {}` without engine (auto-selection). |
| DataStore / Settings + WASM | Adding `androidx.datastore` to `commonMain` | DataStore has limited WASM support. Use `multiplatform-settings` library (by russhwolf) which has explicit `wasmJs` support via `StorageSettings` wrapping browser `localStorage`. |
| Arrow `Either` + Kotlinx Serialization | Assuming `Either<L, R>` is serializable out of the box | Arrow 2.x supports kotlinx.serialization for `Either`, but you must include `arrow-core-serialization` and register the serializers. Without this, Ktor content negotiation silently fails to serialize Either responses. |
| Koin + Compose Multiplatform | Using `koinInject()` in Compose without `KoinApplication` in the composition tree | On WASM and Desktop, there is no Android-like auto-initialization. You must explicitly call `KoinApplication { modules(...) }` at the composition root for each platform entry point. |
| Compose Navigation + WASM | Not binding navigation to browser history | Call `NavController.bindToBrowserNavigation()` in the WASM entry point. Without this, browser back/forward buttons do nothing. |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| WASM full recompilation on every change | 30-60s build times during development, developers disable WASM target | Enable incremental compilation for WASM (`kotlin.incremental.wasm=true`); keep WASM module dependencies minimal | Immediate -- from first development cycle |
| Koog agent state held in memory on server | Works fine for single user, OOM for 100+ concurrent conversations | Use Koog's Postgres persistence provider; set conversation TTLs; implement history compression | ~50 concurrent agent sessions without persistence |
| All KMP targets compiled on every Gradle sync | IDE becomes unresponsive, 5+ minute sync times | Use Gradle's `kotlin.mpp.enableCinteropCommonization=true` and configure IDE to resolve only needed targets; use `local.properties` to skip unused targets during development | ~10 modules with 5 targets each |
| Single Compose recomposition scope for dashboard | UI jank when any state changes trigger full-screen recomposition | Use `derivedStateOf`, `key()` blocks, and state hoisting; split dashboard into independent composables | Visible on lower-end Android devices and WASM from the start |

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Koog agent tools executing with server privileges | An AI agent with a "database query" tool could be prompt-injected to exfiltrate data | Sandbox agent tools with explicit permission scopes; never give agents direct database access; use read-only views; audit tool invocations |
| API keys for LLM providers in shared KMP code | Keys compiled into Android APK or WASM bundle are trivially extractable | All LLM API keys must live server-side only; client agents proxy through authenticated server endpoints |
| JWT secret in default configuration fallback | Production deployment uses template default secret if env var is missing | Remove default fallback; fail fast at startup if `JWT_SECRET` env var is not set (already flagged in CONCERNS.md) |
| Client SDK stores auth tokens in WASM localStorage | XSS can steal tokens from localStorage | Use httpOnly cookies for WASM target; or store tokens in memory only with refresh-on-reload pattern |
| Setup CLI writes credentials to git-tracked files | Database passwords and API keys committed to template user's repo | CLI should write to `.env` files that are in `.gitignore`; validate `.gitignore` entries exist before writing |

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Template requires manual renaming of 20+ files/strings | Developers spend 30+ minutes on setup, miss a rename, get runtime errors | CLI script that takes project name, package, and app ID as input and does all renaming atomically; validate with a build check |
| Component library has no preview/showcase | Developers don't know what components exist or how to use them | Include a "Component Catalog" screen in the sample app that renders every component with variations |
| Navigation structure is hardcoded | Template users must understand the entire navigation graph to add a screen | Use a registration/plugin pattern where each feature module registers its screens; navigation graph assembles automatically |
| Koog agent responses are not streamed | User waits 5-30s staring at a spinner for agent responses | Implement SSE/streaming from server to client; show incremental token output; provide cancel capability |
| Error messages show internal types | User sees "Left(NetworkError.Timeout)" instead of a human message | Map all `ApiError` subtypes to user-facing strings in a centralized error display composable |

## "Looks Done But Isn't" Checklist

- [ ] **Koin DI:** Modules compile on all targets -- verify bindings actually resolve at runtime on iOS and WASM (not just JVM tests)
- [ ] **WASM build:** `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` succeeds -- verify it also works in Safari (WasmGC support varies)
- [ ] **Navigation:** Screens navigate forward -- verify deep links, browser back button (WASM), and state restoration after process death (Android)
- [ ] **Auth flow:** Login works -- verify token refresh, session expiry handling, logout clearing all local state, and re-auth after 401
- [ ] **Koog agents:** Agent responds -- verify streaming, error recovery on LLM timeout, conversation persistence across server restarts, token budget limits
- [ ] **Setup CLI:** Script renames the project -- verify Android manifest, iOS bundle ID, Gradle module names, package declarations in all source sets, and that the project still builds after rename
- [ ] **Either SDK:** API calls return Either -- verify error serialization/deserialization round-trips correctly, that network timeouts produce the right error type, and that cancellation is handled
- [ ] **Shared components:** Components render on Android -- verify they render identically on WASM (font rendering, touch targets, accessibility) and Desktop (window resizing)
- [ ] **Local storage:** Data persists on Android -- verify WASM uses localStorage correctly, iOS uses NSUserDefaults, and Desktop uses the right file path
- [ ] **Context parameters:** Server code compiles -- verify IDE navigation works (go-to-definition on context-provided values) and that Arrow's Raise DSL integrates correctly with named context parameters

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| JVM target mismatch (Koog JVM 17 vs shared JVM 11) | MEDIUM | Extract Koog-dependent code into separate module with JVM 17 target; update shared module to expose interfaces only; rewire Koin bindings |
| WASM dependency incompatibility discovered late | HIGH | Create intermediate source sets (`nonWasmMain`, `mobileMain`); move incompatible deps out of commonMain; may require significant refactoring of imports across all source sets |
| Shared module bloat | HIGH | Create new focused modules; move files one-by-one (Gradle doesn't support partial module splits); update all import paths; rewire Koin modules; update all build.gradle.kts files |
| Context parameter migration breaks | LOW | Change compiler flag back temporarily; use the IDE's migration inspection to convert one file at a time; context receivers still compile in Kotlin 2.2 alongside context parameters (just can't use both flags) |
| Koin resolution failures on iOS/WASM | MEDIUM | Add `koin.verify()` to platform-specific test suites; replace reflection-heavy bindings with factory lambdas; add explicit platform module declarations |
| Either error type coupling | MEDIUM | Define new `ApiError` sealed hierarchy in shared module; create mapper functions at the Ktor response level; update all client code to use new types; can be done incrementally per endpoint |
| Koog agent code in wrong module | HIGH | Must move agent implementations to server-only module; extract interfaces to shared module; rewrite tests; update Koin bindings; potential API changes if agents were called directly |

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| JVM target mismatch | Phase 1: Module Structure | Koog sample agent compiles and runs; Android build still targets JVM 11; no cross-module JVM target conflicts |
| WASM dependency trap | Phase 1: Foundation + every subsequent phase | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` passes in CI after every PR |
| Context parameter migration | Phase 1: Foundation | All `context(...)` usages use named parameters; no deprecated `-Xcontext-receivers` warnings; Arrow Raise DSL works with explicit receiver syntax |
| Shared module bloat | Phase 1: Module Structure | Each module has a clear single responsibility; no module has more than 15 source files; dependency graph has no cycles |
| Koog platform coupling | Phase 2: AI Agents | Agent tests pass on JVM; client proxy tests pass on all targets; no Koog imports in commonMain |
| Either error type leakage | Phase 2: Client SDK | Client code only imports `ApiError`; no server `DomainError` types in shared module; error round-trip test passes |
| Koin multiplatform wiring | Phase 1: DI Setup | `koin.verify()` passes on JVM, iOS, WASM, and Android test suites |
| Setup CLI fragility | Phase 5: CLI & Polish | CLI renames project; full build passes after rename; no hardcoded template strings remain in source |
| Navigation hardcoding | Phase 3: Navigation | New screen can be added by a feature module without modifying the navigation module |
| Agent security (tool sandboxing) | Phase 2: AI Agents | Agent tools have explicit permission declarations; no tool has direct database write access; audit log captures all tool invocations |

## Sources

- [Koog GitHub Repository](https://github.com/JetBrains/koog) -- JVM 17 requirement, KMP target support, module structure
- [Kotlin Context Parameters Update (JetBrains Blog, April 2025)](https://blog.jetbrains.com/kotlin/2025/04/update-on-context-parameters/) -- breaking changes, migration guidance
- [Kotlin/Wasm Documentation](https://kotlinlang.org/docs/wasm-overview.html) -- browser compatibility, exception handling, Beta status
- [Koin 4.1 Release (Kotzilla Blog)](https://blog.kotzilla.io/koin-4.1-is-here) -- WASM-safe UUIDs, KMP patterns
- [Koin KMP Advanced Patterns](https://insert-koin.io/docs/reference/koin-mp/kmp/) -- platform wrapper pattern, expect/actual modules
- [Compose Multiplatform 1.10.0 Release (JetBrains Blog, Jan 2026)](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/) -- Navigation 3, WASM Beta status
- [Compose Navigation in KMP (JetBrains Docs)](https://kotlinlang.org/docs/multiplatform/compose-navigation.html) -- browser history binding, type-safe navigation
- [multiplatform-settings (GitHub)](https://github.com/russhwolf/multiplatform-settings) -- WASM-compatible key-value storage
- [KMP Pitfalls and Anti-Patterns (Karel van der Merwe, Medium)](https://medium.com/@karelvdmmisc/my-journey-with-kotlin-multiplatform-mobile-pitfalls-anti-patterns-and-solutions-525df7058018) -- shared module boundaries, team structure issues
- [Ktor Client Engines Documentation](https://ktor.io/docs/client-engines.html) -- WASM engine support, CIO multiplatform
- [Arrow Kotlin GitHub](https://github.com/arrow-kt/arrow) -- Either serialization, KMP support
- [DataStore KMP Setup (Android Developers)](https://developer.android.com/kotlin/multiplatform/datastore) -- WASM limitations, platform support
- [openMF/kmp-project-template (GitHub)](https://github.com/openMF/kmp-project-template) -- CLI customizer.sh reference for template renaming automation

---
*Pitfalls research for: KMP Full-Stack Template*
*Researched: 2026-02-10*
