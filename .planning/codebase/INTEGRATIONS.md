# External Integrations

**Analysis Date:** 2026-02-10

## APIs & External Services

**Not detected:**
- No third-party API integrations (Stripe, SendGrid, Twilio, etc.) detected at this time.

## Data Storage

**Databases:**
- PostgreSQL 15+ (primary)
  - Connection: Environment variables (`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`) or `DATABASE_URL`
  - Client: Exposed ORM with R2DBC reactive driver (`org.postgresql:r2dbc-postgresql` 1.0.7.RELEASE)
  - Connection pooling: HikariCP 7.0.2
  - File: `server/core/database/src/main/kotlin/com/m2f/core/database/connection/DataSource.kt`
  - Configuration: `server/core/database/src/main/kotlin/com/m2f/core/database/Startup.kt`

- H2 Database (testing/development fallback)
  - Version: 2.3.232
  - Used in test suites and development environments
  - In-memory or file-based

**File Storage:**
- Local filesystem only - No cloud storage integrations detected

**Caching:**
- None - Application uses live database queries without Redis or memcached

## Authentication & Identity

**Auth Provider:**
- Custom JWT-based authentication (no Auth0, Firebase, or OAuth2 providers)
  - Implementation: HMAC256-signed JWTs with audience and issuer validation
  - Framework: Ktor Server Auth JWT
  - Configuration location: `server/core/security/src/main/kotlin/com/m2f/core/security/SecurityPlugin.kt`

**JWT Configuration:**
- Secret: Environment variable `JWT_SECRET` or hardcoded default in code
- Audience: Environment variable `JWT_AUDIENCE` or hardcoded default
- Issuer: Environment variable `JWT_ISSUER` or hardcoded default
- Realm: Environment variable `JWT_REALM` or hardcoded default
- Algorithm: HMAC256 (symmetric)
- Validation: File `server/core/security/src/main/kotlin/com/m2f/core/security/SecurityPlugin.kt`

**Password Handling:**
- jBcrypt 0.4 included in JWT bundle for password hashing support

## Monitoring & Observability

**Error Tracking:**
- None - No Sentry, DataDog, or equivalent service detected

**Logs:**
- Logback 1.5.18 (SLF4J backend)
- File: `gradle/libs.versions.toml` (logback dependency)
- Ktor request/response logging via `ktor-server-call-logging`
- Request tracing via `ktor-server-call-id` for correlation

**Metrics:**
- Micrometer Prometheus 1.15.3 - Metrics export
- Ktor metrics instrumentation: `ktor-server-metrics-micrometer`
- Prometheus scrape endpoint available (Ktor Metrics plugin)

## CI/CD & Deployment

**Hosting:**
- Self-hosted deployment (no cloud platform detected)
- Runs on JVM 11+ with default port 8080

**CI Pipeline:**
- None detected - No GitHub Actions, GitLab CI, or Jenkins configuration found

**Docker:**
- Docker Compose configuration available: `docker-compose.yml`
- PostgreSQL service: `postgres:15-alpine` on port 5436
- Application deployment: No Dockerfile in repository (JVM JAR deployment assumed)

## Environment Configuration

**Required env vars for Runtime:**

Server/HTTP:
- `HOST` (default: "0.0.0.0")
- `PORT` (default: 8080)

JWT Authentication:
- `JWT_SECRET` (default: hardcoded strong string in code)
- `JWT_AUDIENCE` (default: "jwt-audience")
- `JWT_ISSUER` (default: "IssuerName")
- `JWT_REALM` (default: "Access to Your Application")

PostgreSQL Database:
- `DATABASE_URL` (if using standard postgres:// or postgresql:// URL format - auto-converts to R2DBC)
- OR individual components:
  - `PGHOST` (default: "localhost")
  - `PGPORT` (default: 5436)
  - `PGDATABASE` (default: "application")
  - `PGUSER` (default: "postgres")
  - `PGPASSWORD` (default: "postgres")

Development:
- `-Dio.ktor.development=true/false` (JVM system property for development mode)

**Secrets Location:**
- Environment variables only (`.env` file NOT committed)
- File: See `.gitignore` - no `.env*` patterns explicitly listed but standard practice
- Configuration code references: `com.m2f.core.config.configuration.Env` and `DataSource` classes

**Hardcoded Defaults:**
- Default JWT credentials in `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt` (intended for development only)
- Default PostgreSQL credentials in `server/core/database/src/main/kotlin/com/m2f/core/database/connection/DataSource.kt`

## Webhooks & Callbacks

**Incoming:**
- None detected - No webhook endpoints configured

**Outgoing:**
- None detected - No outbound webhook integrations

## Database Migration

**Framework:**
- Custom migration system (not Flyway or Liquibase)
- Location: `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/`
- Components:
  - `Migration.kt` - Base migration interface
  - `MigrationRegistry.kt` - Migration registration
  - `MigrationsTable.kt` - Migrations tracking table
  - `Startup.kt` - Migration execution

**Initialization:**
- Docker init scripts: `init-scripts/` directory
- Mounted to PostgreSQL container entrypoint for schema setup
- Custom Kotlin migration runner invoked on application startup

## Client-Server Communication

**Protocol:**
- HTTP/REST via Ktor (OpenAPI documented)
- JSON serialization via Ktor Serialization + Kotlinx Serialization

**Documentation:**
- OpenAPI 1.0 specification auto-generated
- Endpoint: `/docs` (configured in `server/src/main/kotlin/com/m2f/template/Application.kt`)
- Static definition file: `openapi/generated-api.json`
- Swagger UI available at `/docs`

---

*Integration audit: 2026-02-10*
