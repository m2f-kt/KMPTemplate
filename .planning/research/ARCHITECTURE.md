# Architecture Research

**Domain:** KMP Full-Stack Template with AI Agent Support
**Researched:** 2026-02-10
**Confidence:** MEDIUM-HIGH

## Standard Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Client Platforms                                │
│  ┌───────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐            │
│  │  Android  │ │   iOS    │ │ Desktop  │ │  WASM/Web │            │
│  └─────┬─────┘ └─────┬────┘ └────┬─────┘ └─────┬─────┘            │
│        └──────────────┴───────────┴──────────────┘                  │
│                          │                                          │
│  ┌───────────────────────┴──────────────────────────────────────┐   │
│  │                    composeApp (UI Layer)                      │   │
│  │  Navigation 3 · Material3 Components · ViewModels · Koin DI  │   │
│  └───────────────────────┬──────────────────────────────────────┘   │
│                          │                                          │
│  ┌───────────────────────┴──────────────────────────────────────┐   │
│  │                    shared:sdk (SDK Layer)                     │   │
│  │  ApiClient · Either<DomainError, T> · DTOs · Domain Models    │   │
│  └───────────────────────┬──────────────────────────────────────┘   │
│                          │                                          │
│  ┌───────────────────────┴──────────────────────────────────────┐   │
│  │                    shared (Common Layer)                      │   │
│  │  Domain Models · Constants · Platform Expect/Actual           │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                          │                                          │
│                          │  HTTP (Ktor Client)                      │
├──────────────────────────┼──────────────────────────────────────────┤
│                          │                                          │
│  ┌───────────────────────┴──────────────────────────────────────┐   │
│  │                    server (Ktor + Netty)                      │   │
│  │  Routes · conduit/conduitAuth · Koin DI · OpenAPI             │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐  │   │
│  │  │  server:core  │ │  server:ai   │ │  Feature Modules     │  │   │
│  │  │  :config      │ │  :agents     │ │  (user, etc.)        │  │   │
│  │  │  :database    │ │  :tools      │ │                      │  │   │
│  │  │  :security    │ │              │ │                      │  │   │
│  │  └──────────────┘ └──────────────┘ └──────────────────────┘  │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │                    Infrastructure                             │   │
│  │  PostgreSQL (R2DBC) · Koog LLM · Prometheus · Logback        │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| **composeApp** | UI rendering, navigation, user interaction, platform entry points | shared:sdk, shared |
| **shared** | Domain models, constants, platform abstractions (expect/actual) | None (leaf dependency) |
| **shared:sdk** | HTTP client wrapper, API calls returning `Either<DomainError, T>`, DTOs | shared, server (via HTTP) |
| **shared:storage** | Multiplatform local persistence (DataStore Preferences) | shared |
| **server** | Application entry point, route wiring, Ktor plugin installation | server:core:*, server:ai, feature modules |
| **server:core:config** | Configuration, `Env` data classes, `DomainError` hierarchy, `conduit` helpers | None (leaf) |
| **server:core:database** | R2DBC connection, migration system, database startup | server:core:config |
| **server:core:security** | JWT authentication, Ktor auth plugin configuration | server:core:config |
| **server:ai** | Koog agent infrastructure: agent registry, tool definitions, LLM provider config | server:core:config, server:core:database, feature modules |
| **Feature modules** (e.g. server:features:user) | Domain services, repositories, route definitions per feature | server:core:* |

### Data Flow

#### Client Request Flow (SDK Layer)

```
[User Action in Compose UI]
    ↓
[ViewModel calls SDK function]
    ↓
[SDK function wraps Ktor Client call in Either.catch]
    ↓
    ├── Success → Either.Right(DomainModel)
    │       ↓
    │   [ViewModel updates UI state]
    │       ↓
    │   [Compose recomposes with new data]
    │
    └── Failure → Either.Left(DomainError)
            ↓
        [ViewModel maps to UI error state]
            ↓
        [Compose shows error feedback]
```

#### Server Request Flow (Existing Pattern)

```
[HTTP Request]
    ↓
[Ktor Route]
    ↓
[conduit { } or conduitAuth { }]         ← Raise<DomainError> scope
    ↓
[Service/Repository layer]               ← context(Configuration, R2dbcDatabase)
    ↓
    ├── Success → respond(status, result)
    └── Failure → DomainError.respond()   ← Renders as GenericErrorModel JSON
```

#### AI Agent Request Flow (New)

```
[HTTP Request to /ai/* route]
    ↓
[Ktor Route with Koog plugin]
    ↓
[aiAgent(strategy, model, input)]         ← Koog extension function
    ↓
[Agent selects strategy: reAct/chat]
    ↓
    ├── [LLM reasoning step]
    │       ↓
    │   [Tool call decision]
    │       ↓
    │   [Tool executes: DB query, API call, computation]
    │       ↓
    │   [Result fed back to LLM]
    │       ↓
    │   [Loop until done or maxIterations]
    │
    └── [Final response]
            ↓
        [HTTP Response]
```

## Recommended Project Structure

```
template/
├── composeApp/                          # Compose Multiplatform UI
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── App.kt                   # Root composable
│       │   ├── navigation/              # Navigation 3 setup, routes
│       │   ├── ui/
│       │   │   ├── components/          # Shared UI components
│       │   │   ├── screens/             # Screen composables
│       │   │   └── theme/               # Material3 theme
│       │   └── di/                      # Koin module definitions
│       ├── androidMain/
│       ├── iosMain/
│       ├── jvmMain/
│       └── wasmJsMain/
│
├── shared/                              # KMP shared code
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── model/                   # Domain models (shared between client & server)
│       │   ├── error/                   # Shared DomainError types (for SDK)
│       │   └── Platform.kt
│       ├── androidMain/
│       ├── iosMain/
│       ├── jvmMain/
│       └── wasmJsMain/
│
├── sdk/                                 # Client SDK module (NEW, KMP)
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── client/                  # Ktor HttpClient factory
│       │   ├── api/                     # API service interfaces + implementations
│       │   │   ├── AuthApi.kt           # suspend fun login(...): Either<DomainError, AuthToken>
│       │   │   └── UserApi.kt
│       │   ├── dto/                     # Network DTOs (kotlinx.serialization)
│       │   ├── mapper/                  # DTO ↔ Domain Model mappers
│       │   └── di/                      # Koin module for SDK dependencies
│       ├── androidMain/                 # OkHttp engine
│       ├── iosMain/                     # Darwin engine
│       ├── jvmMain/                     # CIO engine
│       └── wasmJsMain/                  # JS engine
│
├── storage/                             # Local storage module (NEW, KMP)
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── preferences/             # DataStore Preferences wrappers
│       │   └── di/                      # Koin module
│       ├── androidMain/
│       └── iosMain/
│
├── server/                              # Ktor server application
│   ├── src/main/kotlin/
│   │   ├── Application.kt              # SuspendApp entry point
│   │   └── startup/
│   ├── core/                            # Server infrastructure (EXISTING)
│   │   ├── config/                      # Configuration, DomainError, conduit
│   │   ├── database/                    # R2DBC, migrations
│   │   └── security/                    # JWT auth
│   ├── ai/                              # AI agent infrastructure (NEW)
│   │   ├── src/main/kotlin/
│   │   │   ├── AgentConfig.kt           # Koog plugin installation
│   │   │   ├── agents/                  # Agent definitions
│   │   │   ├── tools/                   # Tool definitions (DB-backed, API, etc.)
│   │   │   ├── routes/                  # AI-specific routes (/ai/chat, etc.)
│   │   │   └── di/                      # Koin module for agents
│   │   └── build.gradle.kts
│   └── features/                        # Feature modules (NEW pattern)
│       └── user/                        # Example feature
│           ├── src/main/kotlin/
│           │   ├── routes/
│           │   ├── service/
│           │   ├── repository/
│           │   └── di/
│           └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

### Structure Rationale

- **sdk/ as separate KMP module:** Isolates all HTTP concerns from UI and shared code. The composeApp depends on sdk but never imports Ktor Client directly. This enforces the contract that all API calls return `Either<DomainError, T>`.
- **storage/ as separate KMP module:** DataStore Preferences requires platform-specific file path configuration (expect/actual). Isolating it keeps shared module clean and allows SDK and UI to both depend on it for token persistence.
- **server/ai/ as separate JVM module:** Koog dependencies are JVM-heavy (LLM clients, MCP). Isolating keeps the core server infrastructure free of AI concerns. The ai module depends on core:config and core:database to give tools access to app state.
- **server/features/ pattern:** Each feature (user, etc.) is a self-contained module with its own routes, services, repositories, and Koin module. The server application wires them at startup.
- **shared/ stays thin:** Only domain models and constants shared across client and server. No business logic, no framework dependencies.

## Architectural Patterns

### Pattern 1: Either-Based SDK Layer

**What:** Every SDK function returns `Either<DomainError, T>`. The Ktor client call is wrapped in `Either.catch`, HTTP error codes are mapped to typed `DomainError` subtypes. Callers never see exceptions.

**When to use:** All client-to-server API calls.

**Trade-offs:** Explicit error paths make UI error handling predictable. Adds a mapping layer between HTTP responses and domain types. Worth it because the existing server already uses `DomainError` for error responses.

**Example:**
```kotlin
// sdk/src/commonMain/kotlin/api/AuthApi.kt
class AuthApi(private val client: HttpClient) {

    suspend fun login(email: String, password: String): Either<DomainError, AuthToken> =
        Either.catch {
            client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
        }.mapLeft { NetworkError(it.message ?: "Connection failed") }
         .flatMap { response ->
             when (response.status) {
                 HttpStatusCode.OK -> response.body<AuthTokenDto>().toDomain().right()
                 HttpStatusCode.Unauthorized -> Unauthorized("Invalid credentials").left()
                 HttpStatusCode.UnprocessableEntity ->
                     response.body<GenericErrorModel>().toDomainError().left()
                 else -> UnexpectedError("Unexpected: ${response.status}").left()
             }
         }
}
```

### Pattern 2: Context Receiver Propagation (Existing)

**What:** Server functions declare their dependencies as context receivers (`context(Configuration, R2dbcDatabase)`). Arrow's `ResourceScope` manages lifecycle. `conduit { }` provides `Raise<DomainError>` scope inside routes.

**When to use:** All server-side business logic. Extend this pattern to new modules (AI tools, feature services).

**Trade-offs:** Powerful compile-time dependency checking. Requires `-Xcontext-parameters` compiler flag. Learning curve for developers unfamiliar with context receivers.

**Example (existing):**
```kotlin
context(_: Configuration, _: R2dbcDatabase)
fun Application.module() {
    configureSecurity()
    routing { /* ... */ }
}
```

### Pattern 3: Koog Agent as Ktor Plugin

**What:** Install Koog as a Ktor plugin. Define agents with tools that can access database, services, and external APIs. Invoke agents from dedicated routes. Tools bridge between the LLM's reasoning and the app's domain logic.

**When to use:** Any server endpoint that requires LLM reasoning with access to application state.

**Trade-offs:** Koog handles retries, history compression, and tool execution. Requires LLM API keys. Agent behavior is non-deterministic by nature. Version 0.6.0 as of writing -- API surface may change.

**Example:**
```kotlin
// server/ai/src/main/kotlin/AgentConfig.kt
context(config: Configuration)
fun Application.configureAgents() {
    install(Koog) {
        llm {
            openAI(apiKey = config.env.ai.openAiKey)
            fallback {
                provider = LLMProvider.OpenAI
                model = OpenAIModels.Chat.GPT4o
            }
        }
        agentConfig {
            prompt(name = "assistant") {
                system("You are a helpful assistant for this application.")
            }
            maxAgentIterations = 20
        }
    }
}

// server/ai/src/main/kotlin/routes/AiRoutes.kt
fun Route.aiRoutes() {
    route("/ai") {
        post("/chat") {
            val input = call.receiveText()
            val output = aiAgent(
                strategy = reActStrategy(),
                model = OpenAIModels.Chat.GPT4o,
                input = input,
                toolRegistry = ToolRegistry {
                    tool(QueryDatabaseTool)
                    tool(GetUserProfileTool)
                }
            )
            call.respond(HttpStatusCode.OK, output)
        }
    }
}
```

### Pattern 4: Navigation 3 with Type-Safe Routes

**What:** Navigation 3 (Compose Multiplatform 1.10+) uses a user-owned `SnapshotStateList` back stack. Routes are `@Serializable` data classes/objects implementing `NavKey`. On non-JVM platforms, polymorphic serialization is required.

**When to use:** All screen navigation in composeApp.

**Trade-offs:** Full control over back stack. Requires explicit serializer registration for iOS/WASM targets. Still in alpha (1.0.0-alpha05) but aligned with official JetBrains/Google direction.

**Example:**
```kotlin
@Serializable
data object HomeRoute : NavKey

@Serializable
data class ProfileRoute(val userId: String) : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeRoute::class, HomeRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
        }
    }
}

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navConfig, HomeRoute)
    NavDisplay(backStack = backStack) { key ->
        when (key) {
            is HomeRoute -> HomeScreen(
                onNavigateToProfile = { backStack.add(ProfileRoute(it)) }
            )
            is ProfileRoute -> ProfileScreen(userId = key.userId)
        }
    }
}
```

## Component Boundaries

### What Talks to What

```
composeApp ──depends──→ sdk ──depends──→ shared
composeApp ──depends──→ shared
composeApp ──depends──→ storage
sdk        ──depends──→ shared
sdk        ──HTTP────→ server (runtime only)
storage    ──depends──→ shared

server     ──depends──→ shared (for domain models)
server     ──depends──→ server:core:config
server     ──depends──→ server:core:database
server     ──depends──→ server:core:security
server     ──depends──→ server:ai
server     ──depends──→ server:features:*
server:ai  ──depends──→ server:core:config
server:ai  ──depends──→ server:core:database
server:features:* ──depends──→ server:core:config
server:features:* ──depends──→ server:core:database
```

### Boundary Rules

1. **composeApp never imports Ktor Client directly.** All HTTP goes through sdk module.
2. **shared has zero framework dependencies.** Only kotlinx-serialization for model annotations.
3. **sdk never imports Compose.** It is a pure data/networking layer.
4. **server:ai depends on core infrastructure** but feature modules do NOT depend on server:ai. AI tools that need feature-specific data receive it via Koin injection or tool parameters.
5. **DomainError types are shared** between server:core:config (server-side hierarchy) and shared/error (client-side hierarchy). They mirror each other but are not the same classes -- the server ones include `context(RoutingContext)` for HTTP response rendering.

### Key Boundary Decision: Separate DomainError Hierarchies

The server's `DomainError` uses context receivers for HTTP response rendering (`context(routingContext: RoutingContext) suspend fun respond()`). This pattern cannot cross to the client since RoutingContext is server-only.

The SDK needs its own client-side `DomainError` sealed hierarchy in `shared/error/`:

```kotlin
// shared/src/commonMain/kotlin/error/DomainError.kt
sealed interface ClientError {
    data class Network(val message: String) : ClientError
    data class Unauthorized(val message: String) : ClientError
    data class Validation(val errors: List<String>) : ClientError
    data class Server(val message: String) : ClientError
    data class Unknown(val message: String) : ClientError
}
```

The SDK maps HTTP responses to these types. ViewModels consume `Either<ClientError, T>`.

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| Template usage (0-100 users) | Monolith is correct. Single Ktor server, single DB. No changes needed. |
| Growing app (100-10k users) | Add connection pooling (HikariCP already present). Add caching layer (Ktor caching headers already wired). Consider read replicas. |
| Production scale (10k+ users) | Extract server:ai into a separate service (LLM calls are slow/expensive). Add Redis for session caching. Feature modules could become separate services. |

### Scaling Priorities

1. **First bottleneck: LLM latency.** AI agent calls are 2-30 seconds. Handle with async processing, streaming responses, and request queuing. The Koog Ktor plugin supports streaming natively.
2. **Second bottleneck: Database connections.** R2DBC is already non-blocking. Add connection pooling configuration. Monitor with Prometheus metrics (already wired).

## Anti-Patterns

### Anti-Pattern 1: Leaking HTTP into ViewModels

**What people do:** Import Ktor Client in ViewModel, make HTTP calls directly, catch exceptions manually.
**Why it's wrong:** Breaks testability, scatters error handling logic, couples UI to transport.
**Do this instead:** All HTTP goes through the SDK module. ViewModels call `sdk.authApi.login(...)` and receive `Either<ClientError, T>`. Mock the SDK interface in tests.

### Anti-Pattern 2: Putting Business Logic in shared

**What people do:** Add service classes, use cases, or repository implementations in the shared module.
**Why it's wrong:** shared is compiled for all KMP targets. Adding server-only deps (Exposed, Koog) breaks iOS/WASM compilation. Adding client-only deps (Ktor Client engines) bloats server builds.
**Do this instead:** shared contains only data classes, enums, constants, and interfaces. Logic lives in sdk (client-side) or server feature modules (server-side).

### Anti-Pattern 3: Agents with Unbounded Tool Access

**What people do:** Give Koog agents access to all database tables and all service methods via tools.
**Why it's wrong:** LLMs are non-deterministic. An agent with write access to user data could corrupt state. Over-broad tool sets confuse the LLM, increasing latency and error rates.
**Do this instead:** Define narrow, read-heavy tool sets per agent purpose. Use separate agents for different domains. Wrap destructive operations in confirmation flows. Set `maxIterations` conservatively.

### Anti-Pattern 4: Sharing DomainError Across Client/Server

**What people do:** Try to reuse the server's `DomainError` classes (with `context(RoutingContext)`) in client code.
**Why it's wrong:** RoutingContext is a Ktor server class. It cannot compile on iOS/WASM targets. Even if you strip context receivers, the error semantics differ -- server errors are about HTTP response rendering, client errors are about UI state.
**Do this instead:** Define a separate `ClientError` sealed hierarchy in shared. The SDK maps HTTP status codes to `ClientError` subtypes.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| LLM Providers (OpenAI, Anthropic, Google) | Koog plugin handles provider abstraction | API keys in environment variables. Fallback model config in application.yaml. |
| PostgreSQL | R2DBC via Exposed (existing) | Connection params from environment. Docker Compose for local dev. |
| DataStore Preferences | AndroidX DataStore KMP (1.1.7+) | BundledSQLiteDriver for consistency across platforms. Expect/actual for file path. |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| composeApp <-> sdk | Direct Kotlin function calls (in-process) | SDK is a KMP library dependency. No IPC. |
| sdk <-> server | HTTP (Ktor Client to Ktor Server) | JSON via kotlinx-serialization. OpenAPI for contract documentation. |
| server:ai <-> LLM | HTTP via Koog's prompt executors | Async. Streaming supported. Token usage tracked via Koog observability. |
| server:ai tools <-> server:features | Koin DI for injecting feature services into tools | Tools call service methods, not DB directly. Keeps tool layer thin. |
| composeApp <-> storage | Direct Kotlin function calls (in-process) | DataStore Preferences. Coroutine Flow for reactive reads. |

## Build Order (Dependency-Driven)

Components must be built in this order due to compile-time dependencies:

```
Phase 1: Foundation (no new dependencies)
  └── shared/error/ClientError         ← Client-side error hierarchy
  └── shared/model/*                   ← Shared domain models (User, AuthToken, etc.)

Phase 2: Server Features (extends existing patterns)
  └── server:features:user             ← Auth flow, user CRUD, routes
  └── server routes wired in Application.kt

Phase 3: Client SDK (depends on Phase 1 models)
  └── sdk module created (KMP)
  └── Ktor Client factory + platform engines
  └── AuthApi, UserApi returning Either<ClientError, T>
  └── Koin module for SDK

Phase 4: Local Storage (independent of SDK, but used together)
  └── storage module created (KMP)
  └── DataStore Preferences setup
  └── Token persistence for auth sessions

Phase 5: AI Infrastructure (server-only, depends on Phase 2 services)
  └── server:ai module created
  └── Koog plugin installed
  └── Example agent + tools wired to user service
  └── AI routes exposed

Phase 6: Navigation & UI (depends on Phase 3 SDK + Phase 4 Storage)
  └── Navigation 3 setup in composeApp
  └── UI component library (buttons, inputs, cards, dialogs)
  └── Auth screens, dashboard screen
  └── Koin DI wired across targets

Phase 7: Polish
  └── Setup CLI script
  └── Structured logging
  └── Sample dashboard demonstrating all capabilities
```

**Build order rationale:**
- Phase 1 first because every other module depends on shared models and error types.
- Phase 2 before Phase 3 because the SDK needs server endpoints to call.
- Phase 3 before Phase 6 because UI screens need the SDK to fetch data.
- Phase 4 can run in parallel with Phase 3 (no dependency), but is listed after because token storage is only useful once auth SDK exists.
- Phase 5 is independent of client work -- server:ai only depends on server:core and server:features. Can run in parallel with Phases 3-4.
- Phase 6 last because it consumes everything: SDK for data, storage for persistence, navigation for routing.

## Sources

- [Koog Official Documentation](https://docs.koog.ai/) -- HIGH confidence
- [Koog GitHub Repository](https://github.com/JetBrains/koog) -- HIGH confidence
- [Koog Ktor Plugin Documentation](https://docs.koog.ai/ktor-plugin/) -- HIGH confidence
- [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) -- HIGH confidence
- [Compose Multiplatform 1.10.0 Release Blog](https://blog.jetbrains.com/kotlin/2026/01/compose-multiplatform-1-10-0/) -- HIGH confidence
- [DataStore for KMP Setup](https://developer.android.com/kotlin/multiplatform/datastore) -- HIGH confidence
- [Room Database for KMP](https://developer.android.com/kotlin/multiplatform/room) -- HIGH confidence (not recommended over DataStore for preferences)
- [Arrow-kt Integrations](https://arrow-kt.io/learn/integrations/) -- HIGH confidence
- [Arrow Ktor Resilience](https://arrow-kt.io/learn/integrations/) -- MEDIUM confidence (arrow-resilience-ktor-client module exists)
- [Koin Compose Multiplatform](https://insert-koin.io/docs/quickstart/cmp/) -- HIGH confidence
- [Ktor Full-Stack KMP Guide](https://ktor.io/docs/full-stack-development-with-kotlin-multiplatform.html) -- HIGH confidence
- [Koog 0.4.0 Release](https://blog.jetbrains.com/ai/2025/08/koog-0-4-0-is-out-observable-predictable-and-deployable-anywhere-you-build/) -- MEDIUM confidence (version may have advanced)
- [Navigation 3 Recipes](https://github.com/terrakok/nav3-recipes) -- MEDIUM confidence (community resource)
- [Arrow + Ktor Client Slack Discussion](https://slack-chats.kotlinlang.org/t/8618955/is-there-an-arrow-http-client-or-do-people-wrap-e-g-ktor) -- LOW confidence (community pattern, not official)

---
*Architecture research for: KMP Full-Stack Template with AI Agent Support*
*Researched: 2026-02-10*
