# Codebase Structure

**Analysis Date:** 2026-02-10

## Directory Layout

```
Template/
├── .gradle/                    # Gradle caching and build artifacts
├── .idea/                      # IntelliJ IDEA project settings
├── .planning/                  # GSD planning documents (generated)
├── .kotlin/                    # Kotlin compiler cache
├── .git/                       # Version control
├── build/                      # Root-level build outputs and WASM packages
├── build-config/               # Build configuration utilities
├── gradle/                     # Gradle wrapper
├── init-scripts/               # Gradle init scripts
├── androidApp/                 # Android platform-specific code (minimal wrapper)
├── iosApp/                     # iOS platform-specific code (minimal wrapper)
├── composeApp/                 # Multiplatform Compose UI application
│   ├── src/
│   │   ├── commonMain/         # Shared Compose code
│   │   ├── androidMain/        # Android-specific Compose setup
│   │   ├── iosMain/            # iOS-specific Compose setup
│   │   ├── jvmMain/            # Desktop (JVM) Compose setup
│   │   ├── wasmJsMain/         # Web (WASM) Compose setup
│   │   └── commonTest/         # Compose tests
│   └── build.gradle.kts
├── server/                     # Ktor HTTP server application
│   ├── src/
│   │   └── main/
│   │       └── kotlin/
│   │           └── com/m2f/template/    # Main server app code
│   ├── core/                   # Core infrastructure modules
│   │   ├── config/             # Configuration management
│   │   │   ├── src/main/kotlin/com/m2f/core/config/
│   │   │   │   ├── configuration/      # Configuration classes
│   │   │   │   └── server/             # Error handling and DSL helpers
│   │   │   └── build.gradle.kts
│   │   ├── database/           # Database access and migrations
│   │   │   ├── src/main/kotlin/com/m2f/core/database/
│   │   │   │   ├── migrations/         # Migration registry and implementation
│   │   │   │   ├── connection/         # DataSource configuration
│   │   │   │   └── Startup.kt
│   │   │   ├── src/main/resources/     # SQL migration files (empty)
│   │   │   └── build.gradle.kts
│   │   ├── security/           # Authentication and authorization
│   │   │   ├── src/main/kotlin/com/m2f/core/security/
│   │   │   │   └── SecurityPlugin.kt   # JWT configuration
│   │   │   └── build.gradle.kts
│   │   └── build.gradle.kts    # Core modules aggregate build
│   └── build.gradle.kts        # Server main build
├── shared/                     # Multiplatform business logic
│   ├── src/
│   │   ├── commonMain/         # Platform-agnostic logic
│   │   │   └── kotlin/com/m2f/template/
│   │   ├── androidMain/        # Android-specific implementations
│   │   ├── iosMain/            # iOS-specific implementations
│   │   ├── jvmMain/            # JVM-specific implementations
│   │   ├── wasmJsMain/         # WebAssembly-specific implementations
│   │   └── commonTest/         # Shared tests
│   └── build.gradle.kts
├── docs/                       # Documentation
├── settings.gradle.kts         # Root project module configuration
├── build.gradle.kts            # Root build configuration
├── gradle.properties           # Gradle properties
├── local.properties            # Local overrides (git-ignored)
├── docker-compose.yml          # Local PostgreSQL for development
├── Template.iml                # IntelliJ module file
└── README.md
```

## Directory Purposes

**composeApp:**
- Purpose: Multiplatform UI application using Jetpack Compose
- Contains: Composable UI components, platform entry points for Android/iOS/Desktop/Web
- Key files: `src/commonMain/kotlin/com/m2f/template/App.kt` (main UI), platform-specific `main.kt` and `MainActivity.kt`

**server:**
- Purpose: HTTP API server using Ktor framework
- Contains: Route handlers, application configuration, main entry point
- Key files: `src/main/kotlin/com/m2f/template/Application.kt`, `startup/Server.kt`, `startup/Config.kt`

**server/core/config:**
- Purpose: Centralized configuration and error handling
- Contains: Configuration data classes, environment variable loading, error types, route DSL helpers
- Key files: `Configuration.kt`, `Env.kt`, `DomainError.kt`, `Error.kt`

**server/core/database:**
- Purpose: Database connection, lifecycle management, and schema migrations
- Contains: R2DBC connection setup, migration registry and execution, migrations table schema
- Key files: `Startup.kt`, `connection/DataSource.kt`, `migrations/Migration.kt`, `migrations/MigrationRegistry.kt`

**server/core/security:**
- Purpose: Authentication and authorization configuration
- Contains: JWT validation setup, security middleware configuration
- Key files: `SecurityPlugin.kt`

**shared:**
- Purpose: Business logic and data models shared across all platforms
- Contains: Platform abstractions, greeting logic, constants
- Key files: `commonMain/kotlin/com/m2f/template/Platform.kt`, `Greeting.kt`, `Constants.kt`

**androidApp:**
- Purpose: Android platform wrapper (minimal)
- Contains: Android manifest and gradle configuration only
- Uses: composeApp and shared modules

**iosApp:**
- Purpose: iOS platform wrapper (minimal)
- Contains: iOS project structure and gradle configuration
- Uses: composeApp and shared modules

**docs:**
- Purpose: Project documentation
- Contains: Markdown documentation files

**build-config:**
- Purpose: Gradle build utility scripts
- Contains: Custom build configuration and initialization

## Key File Locations

**Entry Points:**

- `server/src/main/kotlin/com/m2f/template/Application.kt::main()` - Server startup with resource management
- `composeApp/src/jvmMain/kotlin/com/m2f/template/main.kt` - Desktop app entry
- `composeApp/src/androidMain/kotlin/com/m2f/template/MainActivity.kt` - Android app entry
- `composeApp/src/iosMain/kotlin/com/m2f/template/MainViewController.kt` - iOS app entry
- `composeApp/src/wasmJsMain/kotlin/com/m2f/template/main.kt` - Web app entry

**Configuration:**

- `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Configuration.kt` - Configuration object with dispatchers and limits
- `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt` - Environment variables with HTTP, Auth, and ServerConfig nested classes
- `server/core/database/src/main/kotlin/com/m2f/core/database/connection/DataSource.kt` - PostgreSQL connection configuration
- `server/build.gradle.kts` - Server dependencies and OpenAPI config
- `gradle.properties` - Gradle version properties
- `local.properties` - Local build overrides

**Core Logic:**

- `shared/src/commonMain/kotlin/com/m2f/template/Greeting.kt` - Business logic shared across platforms
- `shared/src/commonMain/kotlin/com/m2f/template/Platform.kt` - Platform interface with implementations
- `server/src/main/kotlin/com/m2f/template/startup/Server.kt` - Generic server startup utility
- `server/core/security/src/main/kotlin/com/m2f/core/security/SecurityPlugin.kt` - JWT configuration

**Testing:**

- `shared/src/commonTest/kotlin/com/m2f/template/SharedCommonTest.kt` - Shared test utilities
- `composeApp/src/commonTest/kotlin/com/m2f/template/ComposeAppCommonTest.kt` - Compose UI tests
- `server/core/config/src/test/kotlin/` - Config module tests
- `server/core/database/src/test/kotlin/` - Database module tests

**Database:**

- `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/Migration.kt` - Migration interface and registry
- `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/MigrationRegistry.kt` - Migration registration point
- `server/core/database/src/main/kotlin/com/m2f/core/database/Startup.kt` - Database initialization and migration execution

**Error Handling:**

- `server/core/config/src/main/kotlin/com/m2f/core/config/server/DomainError.kt` - Error type hierarchy
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt` - Route handler DSL and helper functions

## Naming Conventions

**Files:**

- `*.kt` - Kotlin source files
- `PascalCase.kt` for classes, interfaces, objects (e.g., `Configuration.kt`, `DomainError.kt`)
- `camelCase.kt` for functions-as-files (e.g., `main.kt`)
- `build.gradle.kts` - Gradle build scripts

**Directories:**

- `commonMain/`, `commonTest/` - Multiplatform shared code
- `androidMain/`, `iosMain/`, `jvmMain/`, `wasmJsMain/` - Platform-specific code
- `src/main/kotlin/`, `src/test/kotlin/` - Standard Maven layout for JVM projects
- `src/main/resources/`, `src/test/resources/` - Configuration and test fixtures
- PascalCase packages within `com.m2f.*` hierarchy (e.g., `com.m2f.core.config.configuration`)

**Packages:**

- `com.m2f.template` - Main server and shared code
- `com.m2f.core.config` - Config module
- `com.m2f.core.database` - Database module
- `com.m2f.core.security` - Security module
- Nested packages by concern (e.g., `configuration`, `server`, `migrations`, `connection`)

**Functions:**

- `camelCase` for regular functions
- `camelCase()` with parentheses in context function examples
- `suspend` keyword for async operations (coroutine functions)
- Context receiver prefix for functions requiring context (e.g., `context(Configuration) fun`)

**Classes and Types:**

- `PascalCase` for class names
- `PascalCase` for interface names with `Error` suffix for error types
- Sealed interfaces for error type hierarchies
- Data classes for configuration objects

## Where to Add New Code

**New Feature/Endpoint:**

- Primary code: `server/src/main/kotlin/com/m2f/template/` (create feature module or add to existing structure)
- Core logic: `shared/src/commonMain/kotlin/com/m2f/template/` (if applicable)
- Tests: `server/src/test/kotlin/` for endpoint tests, or corresponding test directories

**New Component/Module:**

- Implementation: Create under `server/` (e.g., `server/features/userManagement/`)
- Configuration: Add to `server/core/config/` if infrastructure-related
- Tests: `src/test/` at same level as implementation

**Utilities and Helpers:**

- Shared helpers: `shared/src/commonMain/kotlin/com/m2f/template/` for multiplatform
- Server helpers: `server/src/main/kotlin/com/m2f/template/` for server-specific utilities
- Core DSL helpers: `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt` for route handling

**Database Migrations:**

- Location: Create migration class in `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/`
- Pattern: Implement `Migration` interface with unique version string (YYYYMMDDHHMMSS recommended)
- Registration: Add to `MigrationRegistry.registerMigrations()` in `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/MigrationRegistry.kt`

**Authentication/Authorization Rules:**

- Location: `server/core/security/src/main/kotlin/com/m2f/core/security/`
- Pattern: Extend security configuration in `SecurityPlugin.kt` or create additional security modules

**New Error Types:**

- Location: `server/core/config/src/main/kotlin/com/m2f/core/config/server/DomainError.kt`
- Pattern: Implement `DomainError` interface with context receiver for response formatting

## Special Directories

**build/:**
- Purpose: Gradle build outputs
- Generated: Yes
- Committed: No
- Contains: Compiled classes, WASM packages, generated resources

**.gradle/:**
- Purpose: Gradle caching
- Generated: Yes
- Committed: No

**.idea/:**
- Purpose: IntelliJ IDEA project configuration
- Generated: Yes (partially)
- Committed: Yes (some parts like runConfigurations)

**kotlin-js-store/:**
- Purpose: Kotlin/JS and WASM dependency cache
- Generated: Yes
- Committed: No

**docker-compose.yml:**
- Purpose: Local development PostgreSQL database
- Generated: No
- Committed: Yes
- Run: `docker-compose up` to start PostgreSQL on localhost:5436

---

*Structure analysis: 2026-02-10*
