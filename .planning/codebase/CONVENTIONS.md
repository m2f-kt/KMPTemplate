# Coding Conventions

**Analysis Date:** 2026-02-10

## Naming Patterns

**Files:**
- PascalCase for main types: `Configuration.kt`, `DomainError.kt`, `Migration.kt`
- PascalCase for data classes: `GenericErrorModel.kt`, `InvalidField.kt`
- Descriptive names tied to single responsibility: `SecurityPlugin.kt`, `DataSource.kt`
- Test files: `*Test.kt` or `*CommonTest.kt` suffix

**Functions:**
- camelCase for all functions: `startDatabase()`, `configureSecurity()`, `migrate()`
- Extension functions on types: `getIntParam()`, `getStringQuery()`, `getOptionalStringParam()` (defined in `Error.kt`)
- Suspend functions for async operations: `migrate()`, `startDatabase()`, `respond()`
- Inline functions with reified type parameters: `conduit<A>()`, `getModel<T>()`

**Variables:**
- camelCase for all variables: `jwtSecret`, `jwtAudience`, `jwtRealm`, `databaseUrl`
- Private constants in uppercase with underscores: `R2DBC_URL`, `R2DBC_PORT`, `AUTH_SECRET`
- Local temporary collections: `migs`, `appliedMigrations`, `formattedErrors`

**Types:**
- PascalCase for interfaces: `DomainError`, `InvalidField`
- PascalCase for data classes: `Configuration`, `Env`, `DataSource`
- Sealed classes/interfaces for domain types: `sealed interface ValidationError : DomainError`
- Use `data object` for singleton error types: `data object InvalidContent : ValidationError`

**Nested Types:**
- Inner data classes in companion objects: `Env.Http`, `Env.Auth`, `Env.ServerConfig` (nested in parent class)
- Access via qualified names: `Env.Http()`, `Env.Auth()`, `Env.ServerConfig()`

## Code Style

**Formatting:**
- 4-space indentation (Kotlin default)
- PascalCase for packages and camelCase functions within
- Line length appears to be 120+ characters
- Consistent use of blank lines between logical sections

**Linting:**
- Detekt is configured for code quality checks
- Kover is configured for code coverage analysis
- Detekt plugin applied to all server subprojects: see `server/build.gradle.kts`
- Suppress annotations used selectively:
  - `@Suppress("ForbiddenComment")` in `SecurityPlugin.kt` line 12 (for JWT secret handling)
  - `@Suppress("TooManyFunctions")` in `Error.kt` line 1 (utility extension functions file)

## Import Organization

**Order:**
1. Package declaration
2. Blank line
3. Standard library imports: `import kotlin.*`, `import kotlinx.*`
4. Third-party framework imports: `import io.ktor.*`, `import arrow.*`
5. Database/ORM imports: `import org.jetbrains.exposed.*`
6. Local project imports: `import com.m2f.*`

**Path Aliases:**
- No import aliases used in codebase
- Full qualified imports preferred for clarity
- Multi-level packages organized by domain: `com.m2f.core.config`, `com.m2f.core.database`, `com.m2f.core.security`

**Wildcard imports:**
- Not observed in codebase
- Explicit imports preferred

## Error Handling

**Patterns:**
- **Arrow-kt Either/Raise pattern** for domain-level error handling:
  - Functions use `Raise<DomainError>` context for error propagation
  - Example in `Error.kt` line 18-23: `conduit<A>()` uses `either { block() }.fold()`
  - Validation errors use `EitherNel<String, A>` (Either with NonEmptyList)

- **Domain Error Hierarchy** in `DomainError.kt`:
  - Interface `DomainError` with suspend fun `respond()` context-bound method
  - Sealed `ValidationError : DomainError` for input validation
  - Data classes for specific error types: `UnexpectedError`, `MappingError`, `IncorrectInput`, `Unauthorized`
  - Each error type implements HTTP response logic in `respond()`

- **HTTP Response Mapping**:
  - Errors respond with specific HTTP status codes
  - 422 (UnprocessableEntity) for validation: `unprocessable()`
  - 500 (InternalServerError) for unexpected: `unexpected()`
  - 401 (Unauthorized) for auth failures: `unauthorized()`

- **Exception Handling**:
  - Uses Arrow's `catch()` block for exception-to-error conversion
  - Example in `Error.kt` line 44-48: `catch({ context.call.receive<T>() }) { raise(InvalidContent) }`

## Logging

**Framework:** No dedicated logging framework observed in code
- Simple `println()` used for migration execution logs in `Migration.kt` lines 66, 75
- Logback dependency included in stack: `logback 1.5.18`
- Expected: Logback configuration via `logback.xml` in resources (standard Ktor pattern)

**Patterns:**
- Informational logs for lifecycle events: "Executing migration", "Migration executed successfully"
- No structured logging observed yet
- Consider adding SLF4J through Logback for production

## Comments

**When to Comment:**
- Document public API contracts in interfaces and functions
- Explain non-obvious algorithm logic or workarounds

**JSDoc/KDoc Pattern:**
```kotlin
/**
 * Base interface for database migrations.
 * All migrations should implement this interface and be registered in the [Migrations] object.
 */
internal interface Migration {
    /**
     * The version of the migration. This should be a unique identifier for the migration.
     * It's recommended to use a timestamp in the format YYYYMMDDHHMMSS.
     */
    val version: String
```
- Triple-slash comments for public documentation
- Reference related types with square brackets: `[Migrations]`
- Explain "why" not just "what"
- Present in database migrations (`Migration.kt`), less common in error handling code

## Function Design

**Size:**
- Most functions 10-50 lines (see `Error.kt` extension functions)
- Larger functions (50-80 lines) acceptable for complex logic like migration flow in `Migration.kt`
- Single extension functions grouped in shared utility files

**Parameters:**
- Use context parameters with `context(Type)` syntax for dependency injection:
  - `context(_: Configuration, _: R2dbcDatabase) fun Application.module()` in `Application.kt`
  - `context(config: Configuration) fun Application.configureSecurity()` in `SecurityPlugin.kt`
  - `context(context: RoutingContext) suspend fun conduit()` in `Error.kt`
- Named parameters for clarity in builder patterns
- Reified type parameters for inline functions: `inline fun <reified A : Any>`

**Return Values:**
- Explicit nullable types: `String?` for optional values
- Either/Raise monad returns: `suspend fun migrate()`
- Arrow provides functional return handling with `fold()`, `recover()`, `catch()`

## Module Design

**Exports:**
- Each module exports via `build.gradle.kts` dependencies
- Example: `server/core/database` exports `exposed-core`, `exposed-r2dbc`
- Public APIs exposed at package root level
- Internal utilities marked with `internal` keyword: `internal interface Migration`, `internal object Migrations`

**Barrel Files:**
- `Module.kt` pattern for Koin DI configuration:
  - `configurationModule` in `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Module.kt`
  - Contains: `module { single { Configuration() } }`
- No evidence of re-export barrels for public APIs

**Package Structure:**
- Domain logic grouped by capability:
  - `com.m2f.core.config.configuration` - app config and environment
  - `com.m2f.core.config.server` - HTTP/API error handling
  - `com.m2f.core.database` - database setup and migrations
  - `com.m2f.core.security` - authentication/security

## Context Parameters (Kotlin 1.7+)

**Heavy Usage Pattern:**
- Compiler flag enabled: `-Xcontext-parameters` in all `build.gradle.kts` files
- Function receivers as implicit context parameters
- Example: `context(config: Configuration)` allows access to `config` without parameter
- Enables clean dependency injection without passing explicit parameters

**Example from `SecurityPlugin.kt`:**
```kotlin
context(config: Configuration)
fun Application.configureSecurity() {
    with(config) {
        val jwtAudience = env.auth.audience
```

## Data Class Usage

**Patterns:**
- Extensive use for configuration: `Configuration`, `Env`, `DataSource`
- Nested data classes for configuration hierarchy: `Env.Http()`, `Env.Auth()`
- Error types as data classes: `UnexpectedError`, `MappingError`, `IncorrectInput`
- Auto-generated `equals()`, `hashCode()`, `toString()` preferred

**Sealed Hierarchies:**
- Used for algebraic domain types: `sealed interface ValidationError : DomainError`
- Enables exhaustive pattern matching and type safety

---

*Convention analysis: 2026-02-10*
