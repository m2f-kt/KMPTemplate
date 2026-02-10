# Project Research Summary

**Project:** KMP Full-Stack Template with AI Agent Support
**Domain:** Kotlin Multiplatform (Server + Client)
**Researched:** 2026-02-10
**Confidence:** MEDIUM-HIGH

## Executive Summary

This is a full-stack Kotlin Multiplatform template targeting production-ready applications with AI agent capabilities. The architecture consists of a Ktor server (already implemented with R2DBC, JWT auth, OpenAPI), Compose Multiplatform clients (Android, iOS, Desktop, WASM), and the addition of JetBrains Koog AI agent infrastructure. The recommended approach prioritizes establishing solid architectural boundaries through focused modules (sdk, storage, ai) before building features, because module structure mistakes are exponentially costly to fix later.

The template's biggest differentiator is the Either-based client SDK pattern combined with Koog AI agents - no competing KMP template offers both typed error handling and working AI infrastructure. However, this also introduces the project's highest risks: JVM target mismatches (Koog requires JVM 17 while Android targets JVM 11), WASM dependency compatibility traps, and context parameter migration complexity. The mitigation strategy is strict module isolation - Koog stays in JVM-only modules, WASM compatibility is verified on every dependency addition, and context parameters are audited in Phase 1 before features are built.

The key insight from research is that "working on one platform" is NOT sufficient for a multiplatform template. Every feature must be verified on all targets (especially WASM and iOS) immediately, not at the end. Template bloat is the other critical risk - the shared module must stay thin (only domain models and constants), with all logic living in focused modules (sdk for networking, storage for persistence, ai for agents).

## Key Findings

### Recommended Stack

The existing stack (Kotlin 2.2.10, Compose Multiplatform 1.9.0-rc01, Ktor 3.3.0, Exposed 1.0.0-rc-1, Arrow 2.1.2, Koin 4.1.1) provides a solid foundation. The recommended additions target three capability gaps: AI agents, client-side architecture, and developer experience.

**Core technologies to add:**

- **Koog 0.6.1** (koog-agents, koog-ktor) — JetBrains' official AI agent framework with native Ktor integration, MCP support, and multiplatform capabilities. The only Kotlin-native agent framework; alternatives are Java wrappers (LangChain4j) or require Spring ecosystem.

- **AndroidX Navigation Compose 2.9.1** — Official JetBrains fork of AndroidX Navigation for all KMP targets. Type-safe routes with kotlinx-serialization. Navigation 3 exists but is alpha; stick with 2.9.1 for template stability.

- **Material3 Adaptive 1.2.0** — Responsive layout components (ListDetailPaneScaffold, window size classes) essential for phone/tablet/desktop unified UI. First-party Google/JetBrains solution.

- **DataStore Preferences 1.1.7** — Official Jetpack library with KMP support for key-value storage (auth tokens, user preferences). Coroutine-native, replaces SharedPreferences.

- **Arrow 2.2.0** (upgrade from 2.1.2) — Enables context parameter-based Raise API matching Kotlin 2.2.10's -Xcontext-parameters flag. The upgraded Racing DSL and validate function improve error composition for the Either-based SDK.

- **Koin Compose 4.1.1** — Multiplatform DI integration (koin-compose, koin-compose-viewmodel) for ViewModel injection in shared UI code. Essential for MVVM pattern across targets.

**Critical version dependencies:**
- Kotlin 2.2.10 compatible with CMP 1.9.x (recommend upgrading to 1.9.3 stable), Arrow 2.2.0, Koog 0.6.1, and kotlinx-serialization 1.8.1 (NOT 1.10.0 which requires Kotlin 2.3.0)
- Koog requires JVM 17+, creating a module isolation requirement
- WASM target limits: DataStore has alpha WASM support; some Ktor client engines unsupported

### Expected Features

Research identified clear tiers: table stakes (missing these makes the template incomplete), differentiators (competitive advantages), and anti-features (commonly requested but problematic).

**Must have (table stakes):**
- Complete auth flow (login/register/token refresh) — server already has JWT skeleton; needs user table, endpoints, and client-side flow with token storage and 401 retry
- Type-safe navigation — every CMP template ships this; users refuse to wire manually
- Ktor client HTTP layer — server exists, client SDK is the missing piece
- Either-based client SDK — the DX differentiator using Arrow; wraps all API calls in Either<ApiError, T>
- Shared data models — the :shared module is nearly empty (just Greeting.kt)
- Local key-value storage — for auth tokens, preferences; without it the app can't stay logged in
- Koin client-side DI — server has Koin, client must mirror it
- Material3 theming — dark/light toggle, professional look out of box
- Docker Compose polish — `docker compose up` must just work
- Setup CLI / init script — automated project renaming (30-minute manual process → 60 seconds)

**Should have (competitive advantage):**
- **Koog AI agent infrastructure** — BIGGEST differentiator; no competitor template ships working AI agents. Provides sample agent route, tool calling, MCP integration, streaming responses.
- **Either-based client SDK with Arrow Raise DSL** — unique in market; dramatically better than raw HTTP calls or basic Result
- **Sample dashboard screen** — proves the architecture end-to-end with real API calls, state management, loading/error states
- Arrow-based server error handling with context parameters — already partially built; architectural advantage worth showcasing
- Structured observability — Prometheus + correlation IDs + Koog OpenTelemetry tracing
- R2DBC reactive database — unusual and forward-looking; most templates use blocking JDBC

**Defer (v2+):**
- Analytics abstraction — design once users report actual needs
- Push notification infrastructure — Firebase/APNs setup requires credentials that can't work out of box in template
- In-app purchases — vendor lock-in risk; template should document integration points
- Social login — OAuth app registration required per provider; ship email/password auth that works immediately

**Anti-features (deliberately excluded):**
- Payment SDK integration — creates vendor coupling (RevenueCat/Stripe)
- Deep multi-module feature architecture — over-modularization overwhelms new users
- Bundled translations — content, not architecture; noise in template

### Architecture Approach

The recommended architecture isolates concerns through focused modules to prevent the "shared module bloat" anti-pattern. The project structure separates into client stack (composeApp → sdk → shared), server stack (server → server:core:* + server:ai + server:features:*), and a thin shared layer containing only domain models.

**Major components:**

1. **sdk/ (new KMP module)** — Client HTTP layer returning Either<ApiError, T>. Wraps Ktor Client, maps HTTP responses to typed errors, provides platform-specific engines (CIO for JVM, Darwin for iOS, JS for WASM). Enforces the contract that UI never imports Ktor directly.

2. **storage/ (new KMP module)** — DataStore Preferences for local key-value persistence. Platform-specific file paths via expect/actual. Used for auth tokens, user preferences, onboarding state.

3. **server:ai/ (new JVM module)** — Koog agent infrastructure. Isolated because Koog dependencies are JVM-heavy (LLM clients, MCP). Depends on server:core:config and server:core:database to give tools access to app state.

4. **server:features:user/ (new JVM module)** — Auth flow (registration, login, token refresh), user CRUD, routes. Self-contained feature module pattern.

5. **shared/ (keep thin)** — Domain models (User, AuthToken), constants, platform abstractions (expect/actual). NO business logic, NO framework dependencies beyond kotlinx-serialization.

**Key architectural patterns:**

- **Either-based SDK Layer** — Every SDK function returns Either<DomainError, T>. Ktor client calls wrapped in Either.catch, HTTP errors mapped to typed DomainError subtypes. Eliminates exception handling in UI.

- **Context Parameter Propagation** (existing) — Server functions declare dependencies as context receivers (context(Configuration, R2dbcDatabase)). Arrow's ResourceScope manages lifecycle. Extend to new modules.

- **Koog Agent as Ktor Plugin** — Install Koog via plugin. Define agents with tools accessing database/services. Invoke from dedicated routes. Tools bridge LLM reasoning and app domain logic.

- **Navigation 3 with Type-Safe Routes** — Routes as @Serializable data classes/objects implementing NavKey. User-owned SnapshotStateList back stack. Polymorphic serialization for non-JVM targets.

**Build order (dependency-driven):**
1. Foundation (shared models + error types)
2. Server features (auth flow, user CRUD)
3. Client SDK (depends on server endpoints)
4. Local storage (parallel with SDK)
5. AI infrastructure (server-only, parallel with client work)
6. Navigation & UI (depends on SDK + storage)
7. Polish (setup CLI, dashboard, observability)

### Critical Pitfalls

Research identified 7 critical pitfalls that must be addressed proactively in specific phases.

1. **JVM Target Version Mismatch** — Koog requires JDK 17+ but Android targets JVM 11. Adding Koog to shared module breaks Android build with "Unsupported class file major version 61" errors. **Mitigation:** Isolate Koog into dedicated server:ai module with JVM 17. Do NOT add Koog to :shared. Address in Phase 1 (Module Structure).

2. **WASM Target Dependency Compatibility Trap** — Libraries added to commonMain without checking WASM support break WASM build silently. Room, some DataStore features, Koin SLF4J logger don't publish wasmJs artifacts. **Mitigation:** Run ./gradlew :composeApp:wasmJsBrowserDevelopmentRun after EVERY new dependency. Use intermediate source sets (nonWasmMain). Check klibs.io before adding to commonMain. Address in Phase 1 and verify in every subsequent phase.

3. **Context Parameters Migration Landmine** — Project uses -Xcontext-parameters (Kotlin 2.2.10) which has breaking behavioral differences from deprecated -Xcontext-receivers. Context parameters do NOT act as implicit receivers. Code like bar() must change to foo.bar() with explicit receiver. **Mitigation:** Audit all existing context(...) usages, ensure named parameters with explicit member access. Never use callable references to context-parameter functions until Kotlin 2.3. Address in Phase 1 (Foundation).

4. **Shared Module Bloat Destroys Template Reusability** — :shared becomes dumping ground for everything "multiplatform": networking, data models, error types, agent interfaces, navigation, storage. Makes template impossible to customize. **Mitigation:** Split immediately into :core:model, :core:network (sdk), :core:storage, :agents, :ui:components, :ui:navigation. Each module declares only needed targets. Address in Phase 1 (Module Structure).

5. **Koog Agent Module Placement Creates Platform Coupling** — Placing agents in :shared commonMain assumes they run on all KMP targets, but Koog's capabilities differ per platform (WASM/iOS networking restrictions, JVM-only LLM SDKs, JVM-only persistence). **Mitigation:** Two-tier architecture: server-side agents (JVM-only module) + client-side agent proxy (KMP calls server via API). Only interfaces/DTOs in shared. Address in Phase 1 (module placement) and Phase 2 (AI Agents).

6. **Either-Based SDK Leaks Internal Error Types** — SDK returns Either<DomainError, T> but DomainError is defined in server's config module (JVM-only). Cannot be shared with KMP client. Duplicating error types creates divergence risk. **Mitigation:** Separate error hierarchies: ServerError (server modules) and ApiError (shared KMP modules). Client SDK's ApiError represents actionable states: NetworkError, Unauthorized, NotFound, ValidationFailed. Map at API boundary. Address in Phase 1 (:core:model with ApiError types) and Phase 2 (Client SDK).

7. **Koin DI Wiring Fails Silently Across KMP Targets** — Koin modules in commonMain reference platform-specific implementations via expect/actual. Works on JVM/Android (full reflection) but fails on iOS/WASM (limited reflection). The server's koin-logger-slf4j bundle leaks to non-JVM targets (SLF4J is JVM-only). **Mitigation:** Use koin-core in commonMain, koin-ktor only in server, koin-compose in composeApp, koin-logger-slf4j only in JVM source sets. Use expect val platformModule: Module pattern. Verify Koin graph with koin.verify() in tests for EACH target. Address in Phase 1 (DI Setup).

## Implications for Roadmap

Based on research, the roadmap must follow a strict dependency-driven build order with proactive pitfall mitigation. The module structure phase is non-negotiable as first priority - retrofitting module splits is exponentially harder.

### Phase 1: Foundation & Module Structure
**Rationale:** All 7 critical pitfalls have mitigation steps in Phase 1. Module boundaries, JVM targets, Koin wiring, WASM verification, and context parameter auditing must be correct before features are built. This is the cheapest time to fix architectural issues.

**Delivers:**
- Focused module structure (sdk/, storage/, server:ai/, server:features:user/)
- Gradle version catalog updates (Koog, Navigation, DataStore, Arrow 2.2.0, Material3 Adaptive)
- JVM target configuration (server:ai at JVM 17, shared at JVM 11)
- WASM build verification CI step
- Context parameter audit and standardization
- Koin multiplatform wiring with platform modules
- Separate error hierarchies (ServerError in server, ApiError in shared)
- Intermediate source sets for non-WASM dependencies

**Addresses:**
- Table stakes: Shared data models (create :core:model for User, AuthToken DTOs)
- Architecture: Component boundaries established
- Stack: All version upgrades (Arrow 2.2.0, CMP 1.9.3, Exposed 1.0.0)

**Avoids:**
- Pitfall 1 (JVM target mismatch) — server:ai isolated at JVM 17
- Pitfall 2 (WASM trap) — verification CI established
- Pitfall 3 (context parameters) — audit complete before features built
- Pitfall 4 (shared bloat) — module structure defined
- Pitfall 5 (Koog platform coupling) — server-only module created
- Pitfall 6 (error type leakage) — ApiError hierarchy in shared
- Pitfall 7 (Koin wiring) — platform modules configured

**Research flag:** SKIP research-phase. Well-documented module structure patterns, official Gradle docs, Koin multiplatform guides.

### Phase 2: Server Auth Foundation
**Rationale:** SDK needs server endpoints to call. Client can't exist without server. Auth is the first feature every user tests. Server work can proceed while client SDK is being built (parallel work possible).

**Delivers:**
- PostgreSQL user table + Exposed migration
- Registration endpoint (POST /api/auth/register)
- Login endpoint (POST /api/auth/login) returning JWT access + refresh tokens
- Token refresh endpoint (POST /api/auth/refresh)
- Refresh token rotation (security best practice)
- Password hashing (BCrypt)
- User CRUD routes (authenticated)
- server:features:user module with service/repository/routes pattern

**Uses:**
- Stack: Existing Exposed R2DBC, JWT security, Koin DI
- Architecture: Feature module pattern, conduitAuth routing

**Implements:**
- Table stakes: Complete auth flow (server half)
- Differentiator: Arrow-based error handling with context parameters

**Avoids:**
- JWT secret in default config (fail fast if JWT_SECRET env var missing)
- Direct database access in routes (use repository layer)

**Research flag:** SKIP research-phase. Standard Ktor auth + Exposed CRUD, well-documented.

### Phase 3: Client SDK & Storage
**Rationale:** With server auth endpoints live, client SDK can be built. Storage is needed for token persistence. These are independent of UI and can be tested in isolation.

**Delivers:**
- sdk/ module created (KMP)
- Ktor HttpClient factory with platform engines (CIO/JVM, Darwin/iOS, JS/WASM)
- AuthApi: login(), register(), refresh() returning Either<ApiError, T>
- UserApi: getProfile(), updateProfile() returning Either<ApiError, T>
- HTTP error → ApiError mapping (401 → Unauthorized, 422 → ValidationFailed, etc.)
- Token refresh interceptor (automatic 401 retry with refresh, mutex-guarded to prevent races)
- Koin module for SDK dependencies
- storage/ module created (KMP)
- DataStore Preferences setup with platform-specific file paths (expect/actual)
- Token persistence (accessToken, refreshToken, expiresAt)
- User preferences storage (theme, onboarding state)

**Uses:**
- Stack: Ktor Client (already in version catalog), Arrow 2.2.0, DataStore 1.1.7
- Architecture: Either-based SDK pattern, separate ApiError hierarchy

**Implements:**
- Table stakes: Ktor client HTTP layer, Either-based client SDK, Local key-value storage
- Differentiator: Arrow Raise DSL with typed errors (unique in market)

**Avoids:**
- Leaking HTTP into ViewModels (all HTTP goes through SDK)
- Storing tokens in WASM localStorage (XSS risk) — use memory + refresh-on-reload for WASM
- SDK importing Compose (pure data/networking layer)

**Research flag:** NEEDS research-phase. Token refresh patterns in KMP have sparse documentation. WASM DataStore limitations need investigation. Either serialization with Arrow 2.2.0 needs verification.

### Phase 4: Navigation & Material3 Theming
**Rationale:** With SDK ready, UI can be built. Navigation and theming are foundational UI concerns that every screen depends on.

**Delivers:**
- Navigation 3 setup in composeApp
- Type-safe route definitions (@Serializable HomeRoute, LoginRoute, ProfileRoute)
- Polymorphic serialization for non-JVM targets (SerializersModule)
- Browser history binding for WASM (NavController.bindToBrowserNavigation())
- Material3 custom theme (color scheme, typography, dark/light toggle)
- Dynamic color support (Material You on Android)
- Theme preference stored in DataStore
- Koin Compose integration (KoinApplication, koinViewModel)
- Platform-specific entry points (Android Activity, iOS UIViewController, Desktop main, WASM main)

**Uses:**
- Stack: Navigation Compose 2.9.1, Material3 Adaptive 1.2.0, Koin Compose 4.1.1
- Architecture: Navigation pattern from ARCHITECTURE.md

**Implements:**
- Table stakes: Type-safe navigation, Material 3 theming, Koin client-side DI

**Avoids:**
- Navigation hardcoding (use registration pattern for feature modules)
- Missing WASM browser history binding
- Single Compose recomposition scope (use derivedStateOf, key() blocks)

**Research flag:** SKIP research-phase. Well-documented official patterns.

### Phase 5: Auth UI Flow
**Rationale:** With navigation, theming, SDK, and storage complete, the full auth flow can be implemented. This is the first end-to-end feature users will test.

**Delivers:**
- Login screen (email/password form, loading/error states)
- Registration screen (email/password/confirm, validation)
- ViewModels for login/register (koinViewModel injection)
- Either error handling in ViewModels (map ApiError → UI state)
- Token storage on successful login
- Automatic navigation to dashboard on existing valid token
- Logout (clear tokens, navigate to login)
- Password field validation (strength indicator)
- Form validation with Arrow validate function

**Uses:**
- SDK AuthApi (login, register)
- Storage (token persistence)
- Navigation (route to dashboard on success)
- Material3 components (OutlinedTextField, Button, CircularProgressIndicator)

**Implements:**
- Table stakes: Complete auth flow (client half), User management (basic logout)

**Avoids:**
- Storing credentials in memory longer than needed (clear after login)
- Showing internal error types to users (map ApiError → user-facing strings)

**Research flag:** SKIP research-phase. Standard Compose form validation + state management.

### Phase 6: Dashboard & User Profile
**Rationale:** With auth complete, users can log in. Dashboard is the proof point that the architecture works end-to-end. Profile completes basic user management.

**Delivers:**
- Dashboard screen (home after login)
- Sample data fetched from server via SDK (demonstrates API integration)
- Loading/error/success states
- Pull-to-refresh
- Profile screen (view user data, password change form)
- Settings screen (theme toggle, logout button)
- Adaptive layout (responsive for phone/tablet/desktop using Material3 Adaptive)
- Navigation between screens

**Uses:**
- SDK UserApi (getProfile, updateProfile)
- Material3 Adaptive (window size classes, ListDetailPaneScaffold)
- ViewModels with Either-based state

**Implements:**
- Table stakes: User management (profile view, password change)
- Differentiator: Sample dashboard screen (end-to-end architecture proof)

**Avoids:**
- Hardcoded sample data (fetch from real server)
- Ignoring loading states (always show loading indicator)

**Research flag:** SKIP research-phase. Standard Compose UI patterns.

### Phase 7: Koog AI Agent Infrastructure
**Rationale:** With server and client both working, AI agents can be added as a differentiating capability. This is server-only work and doesn't block client functionality.

**Delivers:**
- server:ai/ module with Koog dependencies
- Koog Ktor plugin installed (application.yaml config)
- LLM provider configuration (OpenAI, Anthropic, Google, Ollama fallback)
- Sample agent route (POST /api/ai/chat)
- ReAct strategy agent with tool calling
- Example tools: QueryDatabaseTool (read-only user queries), GetUserProfileTool
- MCP integration example (connect to MCP server via stdio/SSE)
- Streaming response support (SSE to client)
- Agent conversation persistence (Postgres state store)
- OpenTelemetry tracing for agent execution
- Tool sandboxing (read-only database views, explicit permission scopes)
- Agent iteration limits (maxIterations = 20)

**Uses:**
- Stack: Koog 0.6.1 (koog-agents, koog-ktor)
- Architecture: Koog plugin pattern, tools inject feature services via Koin
- Server: Depends on server:core:config, server:core:database, server:features:user

**Implements:**
- Differentiator: Koog AI agent infrastructure (BIGGEST competitive advantage)
- Differentiator: Structured observability (agent tracing)

**Avoids:**
- Agents with unbounded tool access (narrow, read-heavy tool sets)
- API keys in client code (all LLM calls server-side only)
- Koog in shared module (JVM 17 requirement)

**Research flag:** NEEDS research-phase. Koog 0.6.1 is new, MCP integration patterns need investigation, tool sandboxing strategies not well-documented.

### Phase 8: Polish & Setup CLI
**Rationale:** Template is functionally complete. Final phase adds developer experience improvements.

**Delivers:**
- Setup CLI script (Bash or Kotlin script)
- Automated project renaming: package name, applicationId, project name, DB schema prefix
- .env file generation from .env.example
- JWT secret generation
- Docker Compose validation
- Post-setup build verification
- Structured logging with correlation IDs
- Prometheus dashboard config (Grafana JSON)
- Component catalog screen (showcase all UI components)
- Documentation: README, architecture guide, deployment guide

**Uses:**
- Existing: Docker Compose, Prometheus/Micrometer
- New: Kermit for client logging, correlation ID middleware

**Implements:**
- Table stakes: Setup CLI / init script, Docker Compose polish
- Differentiator: Structured observability (Prometheus + correlation IDs)

**Avoids:**
- Hardcoded template strings in source files
- Writing credentials to git-tracked files (.env should be in .gitignore)

**Research flag:** SKIP research-phase. Bash scripting + sed/find-replace are well-understood.

### Phase Ordering Rationale

- **Phase 1 first (Foundation)** — All critical pitfalls have Phase 1 mitigation steps. Module boundaries cannot be changed later without massive refactoring cost. WASM verification discipline must be established immediately.

- **Phase 2 before Phase 3** — Client SDK needs server endpoints to call. Building client first leads to mock data that must be replaced later.

- **Phase 3 (SDK + Storage) before Phase 4 (Navigation)** — UI screens depend on SDK for data and storage for persistence. Navigation without data is just empty shells.

- **Phase 5 (Auth UI) after Phase 4** — Auth screens need navigation routing, Material3 theming, and ViewModel injection. All foundational UI pieces must exist first.

- **Phase 6 (Dashboard) after Phase 5** — Dashboard is behind authentication. Auth flow must work before building protected screens.

- **Phase 7 (AI Agents) can run in parallel with Phases 3-6** — Server-only feature, no client dependencies. But module placement must be decided in Phase 1. Listed after Phase 6 because demo integration with dashboard is easier once dashboard exists.

- **Phase 8 (Polish) last** — CLI script needs the final project structure to rename correctly. Observability is post-MVP. Documentation describes the finished template.

### Research Flags

**Phases needing deeper research during planning:**

- **Phase 3 (Client SDK & Storage)** — Token refresh patterns in KMP have sparse documentation. Need to research mutex-guarded refresh to prevent race conditions. WASM DataStore support is alpha; need to investigate limitations and potentially use multiplatform-settings library as fallback. Arrow 2.2.0 Either serialization integration with Ktor content negotiation needs verification (requires arrow-core-serialization module).

- **Phase 7 (Koog AI Agents)** — Koog 0.6.1 is relatively new (released late 2025). Need to research: best practices for tool sandboxing (read-only database views, permission scopes), MCP integration patterns (stdio vs SSE transport, tool registration), conversation persistence strategies (Postgres state store vs in-memory with TTL), streaming response patterns (SSE from server to client), OpenTelemetry tracing configuration. The official docs are good but community patterns are still emerging.

**Phases with standard patterns (skip research-phase):**

- **Phase 1 (Foundation)** — Gradle multiplatform module setup, Koin configuration, version catalog management are all well-documented in official docs. Context parameter audit is code review, not research.

- **Phase 2 (Server Auth)** — Ktor auth + Exposed CRUD is extensively documented. JWT refresh token rotation is a standard security pattern with many references.

- **Phase 4 (Navigation & Theming)** — Navigation 3 and Material3 have official JetBrains guides. Koin Compose integration is well-documented.

- **Phase 5 (Auth UI)** — Standard Compose form validation and state management. Compose docs are comprehensive.

- **Phase 6 (Dashboard & Profile)** — Standard Compose UI patterns. Material3 Adaptive has official guides.

- **Phase 8 (Polish)** — Bash scripting, Docker Compose, Prometheus configuration are mature technologies with extensive documentation.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | All recommended technologies verified against official releases and docs. Version compatibility matrix confirmed. Only low-confidence area: Koog 0.6.1 compatibility with Ktor 3.3.0-openapi-eap specifically (may need Ktor 3.4.0 upgrade first). |
| Features | MEDIUM-HIGH | Table stakes and differentiators validated against competitor analysis (KMPShip, Multiplatform Kickstarter, AppKickstarter, JetBrains Wizard). MVP definition is clear. Uncertainty: how users will perceive AI agents (novel for KMP templates) and whether Either-based SDK is too opinionated. |
| Architecture | MEDIUM-HIGH | Module structure and component boundaries are based on established KMP patterns. Build order is dependency-driven and logical. Uncertainty: optimal source set structure for WASM compatibility (commonMain vs nonWasmMain tradeoffs) and whether Navigation 3 alpha status will cause issues. |
| Pitfalls | MEDIUM-HIGH | 7 critical pitfalls identified from official docs, GitHub issues, community reports. Mitigation strategies are specific and actionable. Uncertainty: new pitfalls may emerge from Koog 0.6.1 (recent release) and Navigation 3 (alpha on non-Android). |

**Overall confidence:** MEDIUM-HIGH

The stack, architecture, and pitfall research is grounded in official documentation and verified against real-world projects. The main uncertainty is around newer technologies (Koog 0.6.1, Navigation 3, Arrow 2.2.0 context parameters) which are forward-looking choices but have thinner community validation. The template is deliberately choosing the "next standard" (Navigation 3, context parameters) over legacy approaches (Voyager, context receivers), which increases short-term risk but aligns with long-term ecosystem direction.

### Gaps to Address

**Gap 1: Koog + Ktor OpenAPI EAP Compatibility**
The project uses Ktor 3.3.0-openapi-eap-1394 (pre-release). Koog 0.6.1 documentation shows compatibility with Ktor 3.4.0 stable (released Jan 2026). There may be API surface differences between the EAP and stable release.

**How to handle:** Phase 1 should include upgrading Ktor to 3.4.0 stable as part of version catalog updates. This eliminates the EAP dependency and ensures Koog compatibility. If upgrade introduces breaking changes, defer Koog to Phase 7 after Ktor stabilization.

**Gap 2: WASM DataStore Support Limitations**
DataStore Preferences 1.1.7 has alpha WASM support. The exact limitations (performance, storage quotas, persistence guarantees) are not fully documented.

**How to handle:** Phase 3 research-phase should test DataStore on WASM target. If limitations are severe (e.g., no persistence across browser refreshes), switch to multiplatform-settings library which has explicit wasmJs support via StorageSettings wrapping browser localStorage. Provide expect/actual for storage implementation per platform.

**Gap 3: Navigation 3 Alpha Stability on Non-Android Targets**
Navigation 3 is alpha on iOS/WASM/Desktop in CMP 1.10.0. Research shows it's stable on Android (Navigation 2.9.x). The template is targeting CMP 1.9.3 where Navigation 3 is not yet available - need to use Navigation 2.9.1.

**How to handle:** Phase 4 uses Navigation Compose 2.9.1 (multiplatform-stable), not Navigation 3. Update STACK.md recommendation to clarify. Monitor Navigation 3 for stable release and provide upgrade path in template documentation.

**Gap 4: Token Refresh Race Condition Prevention**
The pattern for mutex-guarded token refresh in KMP (preventing multiple simultaneous 401s from triggering parallel refresh calls) is not well-documented. Found one Medium article (LOW confidence source).

**How to handle:** Phase 3 research-phase must investigate and test the pattern. Use kotlinx.coroutines.sync.Mutex in SDK's token refresh interceptor. Create integration test that simulates concurrent 401 responses and verifies only one refresh call is made.

**Gap 5: Arrow 2.2.0 Context Parameter Migration Impact**
Arrow 2.2.0 introduces context parameter-based Raise API. The migration impact from context receivers (current project) to context parameters is documented in JetBrains blog but not battle-tested in this project's context.

**How to handle:** Phase 1 audit must test the migration on a small module first (e.g., server:core:config). Verify IDE navigation, compile times, and runtime behavior. Document any breaking changes before rolling out to all modules. If migration risk is too high, stay on Arrow 2.1.2 with context receivers until Kotlin 2.3 stabilizes context parameters.

## Sources

### Primary (HIGH confidence)

- [Koog GitHub - JetBrains](https://github.com/JetBrains/koog) — Version 0.6.1, module structure, platform support, JVM 17 requirement
- [Koog Official Docs](https://docs.koog.ai/) — Getting started, Ktor plugin, MCP integration, tool sandboxing
- [Compose Multiplatform Releases](https://github.com/JetBrains/compose-multiplatform/releases) — Version history, 1.9.3 stable, 1.10.0 alpha
- [Kotlin Multiplatform Navigation Docs](https://kotlinlang.org/docs/multiplatform/compose-navigation.html) — Navigation Compose 2.9.1
- [Arrow 2.2.0 Release](https://arrow-kt.io/community/blog/2025/11/01/arrow-2-2/) — Context parameters, Racing DSL, validate function
- [Exposed 1.0 Release](https://blog.jetbrains.com/kotlin/2026/01/exposed-1-0-is-now-available/) — Stable API, R2DBC improvements
- [Ktor 3.4.0 Release](https://blog.jetbrains.com/kotlin/2026/01/ktor-3-4-0-is-now-available/) — OpenAPI built-in, structured concurrency
- [DataStore KMP Setup](https://developer.android.com/kotlin/multiplatform/datastore) — Official Google docs, platform support
- [Koin 4.1 Release](https://blog.kotzilla.io/koin-4.1-is-here) — Compose support, WASM-safe UUIDs
- [Kotlin Context Parameters Update](https://blog.jetbrains.com/kotlin/2025/04/update-on-context-parameters/) — Breaking changes, migration from context receivers
- [Kotlin/WASM Documentation](https://kotlinlang.org/docs/wasm-overview.html) — Browser compatibility, Beta status
- [Ktor Full-Stack KMP Guide](https://ktor.io/docs/full-stack-development-with-kotlin-multiplatform.html) — Official full-stack patterns
- [Koin Compose Multiplatform](https://insert-koin.io/docs/quickstart/cmp/) — Setup, platform modules
- [Compose Compatibility Matrix](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html) — CMP/Kotlin version alignment

### Secondary (MEDIUM confidence)

- [Kermit by Touchlab](https://kermit.touchlab.co/docs/) — Multiplatform logging, version 2.0.4
- [multiplatform-settings GitHub](https://github.com/russhwolf/multiplatform-settings) — WASM-compatible key-value storage alternative
- [KMPShip](https://www.kmpship.app) — Commercial KMP starter kit, feature comparison
- [Multiplatform Kickstarter](https://multiplatformkickstarter.com/) — Commercial full-stack template competitor
- [AppKickstarter OSS Template](https://github.com/AppKickstarter/Kotlin-Multiplatform-Template) — Open-source KMP template patterns
- [openMF KMP Project Template](https://github.com/openMF/kmp-project-template) — Setup script reference (customizer.sh)
- [Navigation 3 Recipes](https://github.com/terrakok/nav3-recipes) — Community patterns
- [Compose Material3 Adaptive Docs](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html) — Adaptive layout APIs

### Tertiary (LOW confidence)

- [Token Refresh with Ktor KMP](https://medium.com/@lahirujay/token-refresh-implementation-with-ktor-in-kotlin-multiplatform-mobile-f4d77b33b355) — Single community source for mutex-guarded refresh pattern; needs validation
- [Arrow + Ktor Client Slack Discussion](https://slack-chats.kotlinlang.org/t/8618955/is-there-an-arrow-http-client-or-do-people-wrap-e-g-ktor) — Community pattern discussion, not official
- [KMP Pitfalls Medium Article](https://medium.com/@karelvdmmisc/my-journey-with-kotlin-multiplatform-mobile-pitfalls-anti-patterns-and-solutions-525df7058018) — Shared module boundaries, team structure; single author perspective

---
*Research completed: 2026-02-10*
*Ready for roadmap: yes*
