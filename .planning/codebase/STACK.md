# Technology Stack

**Analysis Date:** 2026-02-10

## Languages

**Primary:**
- Kotlin 2.2.10 - Used across all modules (server, client, shared/multiplatform)

**Secondary:**
- Java 11 - Compilation target for Android and server JVM modules
- JavaScript/WebAssembly - Generated targets via Kotlin Multiplatform (Wasm build)

## Runtime

**Environment:**
- JVM 11+ (compilation target via `jvmTarget.set(JvmTarget.JVM_11)`)
- Netty server engine (HTTP runtime)
- Kotlin Coroutines 1.10.2 (async/concurrency runtime)

**Package Manager:**
- Gradle 8.x (wrapper: `gradlew` at `/Users/marc/IdeaProjects/Template/gradlew`)
- Lockfile: Not detected (Gradle dependency management via `libs.versions.toml`)

## Frameworks

**Core Server:**
- Ktor 3.3.0-openapi-eap-1394 (OpenAPI preview release) - REST API framework with OpenAPI/Swagger support
- Ktor Server Netty - HTTP server engine
- OpenAPI/Swagger generation with automated documentation

**Authentication & Security:**
- Ktor Server Auth - Authentication framework
- Ktor Server Auth JWT - JWT token validation
- JJWT 0.13.0 - JWT creation/validation (bundles: jjwt-api, jjwt-impl, jjwt-jackson)
- jBcrypt 0.4 - Password hashing (included in jwt bundle)

**Database:**
- Exposed 1.0.0-rc-1 - Kotlin SQL framework with R2DBC support
- R2DBC 1.0.7.RELEASE (PostgreSQL driver) - Reactive database access
- PostgreSQL JDBC 42.7.7 - Fallback blocking driver
- HikariCP 7.0.2 - Connection pooling
- H2 2.3.232 - In-memory/file database (testing)

**Dependency Injection:**
- Koin 4.1.1 - Service locator DI framework
- Koin Ktor integration - DI plugin for Ktor

**Monitoring & Observability:**
- Micrometer Prometheus 1.15.3 - Metrics collection and export
- Ktor Server Metrics/Micrometer - HTTP metrics instrumentation
- Ktor Server Call Logging - HTTP request/response logging
- Ktor Server Call ID - Request tracing
- Logback 1.5.18 - SLF4J logging backend

**Functional Programming:**
- Arrow 2.1.2 (core, fx-coroutines, resilience) - FP library for error handling and functional composition
- SuspendApp Arrow integration - Structured concurrency framework

**Security Headers:**
- Ktor Server CSRF - CSRF token generation/validation
- Ktor Server Request Validation - Input validation framework
- Ktor Server CORS - Cross-origin resource sharing
- Ktor Server Caching Headers - Cache-Control header management
- Ktor Server Default Headers - Security header defaults
- Ktor Server HTTP Redirect - HTTP → HTTPS redirect support

**UI Frameworks:**
- Compose Multiplatform 1.9.0-rc01 - Declarative UI (Android, iOS, Desktop, Web)
- Android Gradle Plugin 8.11.1 - Android build system
- Android Lifecycle 2.9.3 - Lifecycle management
- Android Activity Compose 1.10.1 - Activity integration with Compose

**Build/Dev Tools:**
- Kotlin Multiplatform Plugin 2.2.10 - Cross-platform builds (JVM, Android, iOS, Wasm)
- Kotlin Serialization Plugin 2.2.10 - JSON/format serialization
- Compose Compiler Plugin 2.2.10 - Compose IR generation
- Detekt 1.23.8 - Static code analysis/linting
- Kover 0.9.1 - Code coverage reporting
- Ktor Plugin 3.3.0-openapi-eap-156 - Ktor plugin for Gradle

## Key Dependencies

**Critical:**
- `ktor-server-core`, `ktor-server-netty` - HTTP server runtime
- `exposed-core`, `exposed-r2dbc` - SQL framework with reactive database access
- `postgresql-r2dbc` - PostgreSQL R2DBC driver for async database operations
- `jjwt-api`, `jjwt-impl` - JWT token handling
- `koin-ktor`, `koin-core` - Service injection framework
- `kotlinx-coroutines-core` - Coroutine runtime for async code

**Infrastructure:**
- `arrow-core`, `arrow-fx-coroutines` - Functional error handling and typed effects
- `micrometer-registry-prometheus` - Metrics export for monitoring
- `logback-classic` - Structured logging output
- `hikari` - Connection pooling for database efficiency
- `io.r2dbc` - Reactive database connectivity API

## Configuration

**Environment:**
- Configuration loaded via `System.getenv()` with fallback defaults in code
- Environment variable prefixes: `HOST`, `PORT`, `JWT_*`, `PG*`, `DATABASE_URL`
- See `com.m2f.core.config.configuration.Env` for all supported env vars

**Build:**
- Gradle version catalog: `gradle/libs.versions.toml`
- Root build config: `build.gradle.kts`
- Settings/module inclusion: `settings.gradle.kts`
- Properties: `gradle.properties` (Kotlin style, JVM args, Gradle config-cache enabled)
- Ktor plugin OpenAPI config in main `server/build.gradle.kts` and module configs

**Runtime Configuration Files:**
- Docker Compose PostgreSQL setup: `docker-compose.yml` (postgres:15-alpine on port 5436)
- Init scripts: `init-scripts/` directory mounted to Docker entrypoint
- Application entrypoint: `com.m2f.template.ApplicationKt` (configured in `server/build.gradle.kts`)

## Platform Requirements

**Development:**
- Kotlin 2.2.10 compiler
- Java 11 JDK minimum
- Gradle 8.x (via wrapper)
- For Android builds: Android SDK (minSdk=24, targetSdk=35, compileSdk=36)
- For iOS builds: Xcode and iOS SDK
- Docker (recommended) for PostgreSQL database container

**Production:**
- Deployment target: JVM 11+ runtime
- PostgreSQL 15+ database (configured via environment variables or `DATABASE_URL`)
- Port 8080 (default, configurable via `PORT` env var)
- Memory: Application default JVM args in gradle.properties: -Xmx4096M for Gradle, application inherits platform defaults
- Ktor development mode toggle via system property `-Dio.ktor.development=true/false`

---

*Stack analysis: 2026-02-10*
