# Architecture

**Analysis Date:** 2026-02-10

## Pattern Overview

**Overall:** Layered Multi-Module Architecture with Resource Scope Context Propagation

**Key Characteristics:**
- Kotlin Multiplatform Project (KMP) with separate concerns for server and client
- Vertical slice architecture at the module level (core modules for shared server concerns)
- Context-based dependency injection using Kotlin's context receivers (experimental compiler flag `-Xcontext-parameters`)
- Resource-based lifecycle management using Arrow FX's `ResourceScope`
- Functional error handling using Arrow Core's `Raise` DSL and Either types
- JWT-based authentication with Ktor security plugins
- R2DBC async database access layer with migration management

## Layers

**Presentation (Client):**
- Purpose: UI rendering for multiple platforms (Android, iOS, JVM Desktop, WebAssembly)
- Location: `composeApp/src/*/kotlin/com/m2f/template/`
- Contains: Jetpack Compose UI components, platform-specific entry points, view models
- Depends on: Shared module for business logic
- Used by: Platform-specific applications (MainActivity, MainViewController, main.kt)

**Application (Server):**
- Purpose: HTTP server setup, routing, request handling, and orchestration
- Location: `server/src/main/kotlin/com/m2f/template/`
- Contains: Ktor Application module configuration, startup routines, API endpoints
- Depends on: Core modules (config, database, security), shared logic
- Used by: Main entry point (`main()` function in `Application.kt`)

**Core Infrastructure (Server):**
- Purpose: Reusable infrastructure concerns shared across server features
- Location: `server/core/*/src/main/kotlin/com/m2f/core/`
- Contains: Three submodules managing config, database, and security
- Depends on: External libraries (Ktor, Exposed, Arrow, Auth0 JWT)
- Used by: Application layer and potentially other server modules

**Business Logic (Shared):**
- Purpose: Platform-agnostic business logic and data models
- Location: `shared/src/commonMain/kotlin/com/m2f/template/`
- Contains: Platform interfaces (Platform.kt), constants, greeting logic
- Depends on: Kotlin stdlib only (fully multiplatform)
- Used by: All platforms (Android, iOS, JVM, WASM, Server)

## Data Flow

**Server Startup:**

1. `main()` in `Application.kt` launches using Arrow's `SuspendApp`
2. Opens `resourceScope` from Arrow FX for lifecycle management
3. Calls `config { }` which creates a `Configuration` object with environment setup
4. Starts database: `startDatabase()` connects via R2DBC, runs migrations
5. Starts HTTP server: `startServer(Netty)` with configured host/port
6. Application module (`Application.module()`) is instantiated with context-based config and database
7. Configures security: JWT authentication via `configureSecurity()`
8. Sets up routing: OpenAPI docs and `/amazing` endpoint
9. Server awaits cancellation signal

**Request Handling:**

1. HTTP request arrives at Ktor server
2. Authentication middleware validates JWT token (if protected endpoint)
3. Route handler receives `RoutingContext` with request/response
4. Handler extracts parameters/body using context helpers (`getStringParam`, `getModel`, etc.)
5. Handler executes business logic using `Raise<DomainError>` context (Either monad)
6. On success: response serialized to JSON via Kotlinx Serialization
7. On error: `DomainError.respond()` called to format error response

**Client Startup:**

1. Platform-specific entry point (`MainActivity`, `MainViewController`, `main.kt`)
2. Calls `App()` composable from `composeApp/src/commonMain/`
3. App component initializes Compose Material theme
4. Renders UI with state management via `mutableStateOf` and `remember`

**State Management:**

- Server: Configuration is injected via context receivers; Database connection held in ResourceScope
- Client: Local Compose state via `mutableStateOf`; No global state management (yet)
- Shared: Greeting logic is stateless function

## Key Abstractions

**Configuration:**
- Purpose: Centralized environment and settings management
- Examples: `Configuration.kt`, `Env.kt` in `server/core/config/`
- Pattern: Data class with nested config objects; environment variable fallback with sensible defaults

**DomainError:**
- Purpose: Type-safe error representation with context-aware response formatting
- Examples: `DomainError.kt`, `ValidationError`, `Unauthorized`, `MissingParameter`, `InvalidParameter`
- Pattern: Sealed interface with context function for response generation; Each error type handles its own HTTP response

**R2dbcDatabase:**
- Purpose: Async database connection managed by Jetbrains Exposed v1
- Examples: Created in `Startup.kt` via R2DBC connection factory
- Pattern: Resource-scoped lifecycle; SQL execution via `suspendTransaction` blocks

**Migration:**
- Purpose: Database schema versioning and evolution
- Examples: `Migration.kt` interface; `MigrationRegistry` for registration; `Migrations` object for execution
- Pattern: Registry pattern with version tracking in `MigrationsTable`; Only unregistered migrations execute

**Ktor Application Module:**
- Purpose: HTTP server configuration and routing
- Examples: `Application.module()` context function in `Application.kt`
- Pattern: Extension function on Ktor Application with context receivers for dependencies

## Entry Points

**Server Entry:**
- Location: `server/src/main/kotlin/com/m2f/template/Application.kt::main()`
- Triggers: Process startup; `./gradlew run` from server directory
- Responsibilities: Orchestrate database, server, and config initialization; manage resource lifecycle

**Application Module:**
- Location: `server/src/main/kotlin/com/m2f/template/Application.kt::Application.module()`
- Triggers: Ktor server startup after configuration
- Responsibilities: Configure security, define routes, setup OpenAPI documentation

**Client Entry - JVM:**
- Location: `composeApp/src/jvmMain/kotlin/com/m2f/template/main.kt`
- Triggers: Desktop application launch
- Responsibilities: Create Compose window and render App composable

**Client Entry - Android:**
- Location: `composeApp/src/androidMain/kotlin/com/m2f/template/MainActivity.kt`
- Triggers: Android app launch
- Responsibilities: Set content to App composable

## Error Handling

**Strategy:** Functional error handling using Arrow Core's Raise DSL with sealed error types

**Patterns:**

- **Raise<DomainError> Context:** Route handlers use inline lambdas with `Raise<DomainError>` context to collect errors without exceptions
  ```kotlin
  conduit { // Implicit Raise<DomainError> context
    val userId = ensureNotNull(userId) { MissingParameter("userId") }
    // business logic returns A
  }
  ```

- **Either Folding:** Success/failure split via `either { }.fold(onError, onSuccess)`
  ```kotlin
  either {
    block(this) // Raise<DomainError> scope
  }.fold(
    { error -> error.respond() }, // onError: call context handler
    { value -> call.respond(status, value) } // onSuccess: respond with value
  )
  ```

- **Typed Validation Errors:** `ValidationError` sealed interface groups parameter/content errors; `UnexpectedError` for runtime exceptions

- **Context Receiver Propagation:** `context(RoutingContext, Raise<DomainError>)` functions can call each other without explicit passing (compiler flag `-Xcontext-parameters`)

## Cross-Cutting Concerns

**Logging:** Console print statements used in migration execution (`println`); no structured logging library yet

**Validation:** Parameter validation in route handlers via `getIntParam`, `getStringParam` helpers; serialization validation via Kotlinx Serialization

**Authentication:** JWT via Auth0 library (`com.auth0.jwt`); configured in `SecurityPlugin.kt` with HMAC256 algorithm; extracted via `JWTPrincipal` in route handlers

**Serialization:** Kotlinx Serialization for JSON request/response bodies; Ktor content negotiation plugin handles routing

---

*Architecture analysis: 2026-02-10*
