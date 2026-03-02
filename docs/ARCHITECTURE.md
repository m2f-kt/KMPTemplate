# Architecture

Detailed module structure, data flow, and patterns for the Template project.

## Module Dependency Diagram

```
┌─────────────────────────────────────────────────────┐
│                   composeApp                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐            │
│  │ app:auth │ │app:admin │ │app:dash  │ ...        │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘            │
│       └─────────────┼───────────┘                   │
│                     ▼                                │
│             app:designsystem                         │
└─────────────────────┬───────────────────────────────┘
                      │
               ┌──────▼──────┐
               │  core:sdk   │ ← HTTP client (Ktor)
               └──────┬──────┘
                      │ network
               ┌──────▼──────┐
               │   shared    │ ← DTOs, request/response models
               └──────┬──────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                    server                            │
│  ┌────────────┐ ┌────────────┐ ┌──────────────┐    │
│  │server:auth │ │server:groups│ │ server:files │    │
│  └─────┬──────┘ └──────┬─────┘ └──────┬───────┘    │
│        │               │              │              │
│  ┌─────▼───────────────▼──────────────▼───────┐     │
│  │           server:core                       │     │
│  │  ┌────────┐  ┌──────────┐  ┌──────────┐   │     │
│  │  │ config │  │ database │  │ security │   │     │
│  │  └────────┘  └──────────┘  └──────────┘   │     │
│  └────────────────────────────────────────────┘     │
│                                                      │
│  ┌────────────┐                                     │
│  │ server:ai  │ (agents, RAG, vector search)        │
│  └────────────┘                                     │
└──────────────────────────────────────────────────────┘

Shared core modules (used by both client and server):
  core:models   — Domain models
  core:mvi      — MVI ViewModel base classes
  core:testing  — Test utilities and DSL
  core:storage  — Multiplatform key-value storage
```

## Data Flow

### Client → Server Request Flow

```
User Action
  → ViewModel (MVI: Intent → Reduce → Effect)
    → SDK method (core:sdk)
      → Ktor HTTP Client
        → Ktor Server Route
          → Service layer (business logic)
            → Repository (Exposed R2DBC)
              → PostgreSQL
```

### Concrete Example: User Login

```
LoginScreen → LoginViewModel.send(LoginIntent.Submit)
  → LoginViewModel.reduce() → calls SDK
    → AuthApi.login(email, password)
      → POST /api/auth/login (Ktor client)
        → authRoutes() in server:auth
          → AuthService.login()
            → UserRepository.findByEmail()
              → UsersTable SELECT via R2DBC
```

## Module Responsibilities

### Server Modules

| Module | Responsibility | Key Classes |
|---|---|---|
| `server:auth` | JWT authentication, OAuth (Google/Apple), password reset, user CRUD | `AuthService`, `UserRepository`, `OAuthService`, `PasswordResetService` |
| `server:groups` | Group CRUD, membership management, invitation system | `GroupService`, `InvitationService`, `MembershipRepository` |
| `server:files` | S3/MinIO file upload and retrieval | `FileService`, `S3Client` |
| `server:ai` | AI agents (chat, assistant), RAG pipeline, document ingestion | `ChatAgentService`, `AssistantAgentService`, `DocumentIngestionService` |
| `server:core:config` | Environment configuration, dotenv loading | `Env`, `Configuration` |
| `server:core:database` | R2DBC database setup, migration runner, vector column types | `startDatabase()`, `MigrationRegistry`, `VectorColumnType` |
| `server:core:security` | JWT validation, authentication plugin configuration | `configureSecurity()` |

### Client Modules

| Module | Responsibility |
|---|---|
| `app:auth` | Login, registration, password reset screens |
| `app:admin` | Admin panel — user management, invitations table |
| `app:dashboard` | Main dashboard screen |
| `app:documents` | Document management UI for RAG pipeline |
| `app:profile` | User profile, avatar upload |
| `app:designsystem` | Shared UI components, theme, typography |

### Shared Core Modules

| Module | Responsibility |
|---|---|
| `core:models` | Domain models shared across client and server |
| `core:sdk` | API client — typed HTTP methods for all server endpoints |
| `core:mvi` | `MviViewModel` base class, test DSL |
| `core:testing` | Shared test utilities |
| `core:storage` | Multiplatform key-value storage (settings) |

## Configuration Reference

All environment variables are defined in `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt` and documented in `.env.example`.

### Docker Services

| Service | Container Name | Internal Port | External Port |
|---|---|---|---|
| PostgreSQL (pgvector) | `template-postgres` | 5432 | 5436 |
| MinIO (S3) | `template-minio` | 9000 / 9001 | 9002 / 9003 |
| MailHog (SMTP) | `template-mailhog` | 1025 / 8025 | 1025 / 8025 |

Credentials: PostgreSQL `postgres`/`postgres`, MinIO `minioadmin`/`minioadmin`

## Key Patterns

### Context Parameters for DI

Server modules use Kotlin context parameters to inject shared dependencies:

```kotlin
context(config: Configuration, database: R2dbcDatabase)
fun Application.module() { ... }
```

This pattern threads `Configuration` and `R2dbcDatabase` through the application without global state.

### Koin Module Registration

Each server feature module defines a Koin module for its services and repositories:

```kotlin
val authModule = module {
    single { UserRepository(get()) }
    single { AuthService(get(), get()) }
}
```

Modules are composed in `server/src/main/kotlin/.../di/ServerModule.kt`.

### Feature Module Structure

Each server feature module follows a consistent structure:

```
server/<feature>/
├── build.gradle.kts          — Uses server-module-convention plugin
├── src/main/kotlin/.../
│   ├── routes/               — Ktor route definitions
│   ├── service/              — Business logic
│   ├── repository/           — Database access (Exposed R2DBC)
│   ├── tables/               — Exposed Table definitions
│   ├── models/               — Module-specific data classes
│   └── migrations/           — Database migrations
└── src/test/kotlin/.../      — Tests
```

### Arrow Either for Error Handling

Services return `Either<DomainError, Result>` for typed error handling:

```kotlin
suspend fun login(email: String, password: String): Either<AuthError, TokenPair> = either {
    val user = userRepository.findByEmail(email) ?: raise(AuthError.InvalidCredentials)
    ensure(BCrypt.checkpw(password, user.passwordHash)) { AuthError.InvalidCredentials }
    generateTokenPair(user)
}
```

### MVI ViewModel Pattern

Client ViewModels extend `MviViewModel<State, Intent, Effect>`:

```kotlin
class LoginViewModel : MviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {
    override fun reduce(intent: LoginIntent) { ... }
}
```

See `core:mvi` for the base class and test DSL.

### Migration Registration

Migrations are registered via `MigrationRegistry` and run automatically on server startup:

```kotlin
fun registerAuthMigrations() {
    MigrationRegistry.register(CreateUsersTableMigration())
    MigrationRegistry.register(CreateRolesTableMigration())
}
```

## Adding a New Feature

Follow the `server:auth` and `server:groups` modules as canonical examples.

### 1. Create the module directory

```
server/<feature>/src/main/kotlin/com/m2f/server/<feature>/
```

### 2. Add build.gradle.kts

```kotlin
plugins {
    id("server-module-convention")
}

group = "com.m2f.server"

dependencies {
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    // Add feature-specific dependencies
}
```

### 3. Include in settings.gradle.kts

```kotlin
include("server:<feature>")
```

### 4. Create routes, service, repository

Follow the route → service → repository layering pattern from existing modules.

### 5. Register migrations

```kotlin
fun register<Feature>Migrations() {
    MigrationRegistry.register(Create<Feature>TableMigration())
}
```

Call this in `Application.kt` main function before `startDatabase()`.

### 6. Create Koin module

Define a Koin module with your services and repositories, add it to `serverModule`.

### 7. Wire routes in Application.kt

```kotlin
routing {
    val service: <Feature>Service by inject()
    <feature>Routes(service)
}
```

### 8. Add shared DTOs

If the client needs to consume this feature, add request/response models in `shared/`.
