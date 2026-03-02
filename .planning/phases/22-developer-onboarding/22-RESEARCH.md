# Phase 22: Developer Onboarding - Research

**Researched:** 2026-03-02
**Status:** Complete

## Summary

Phase 22 delivers developer onboarding tooling: prerequisite checks, one-command dev environment setup, architecture documentation, and a first-run walkthrough. All work is Gradle tasks + documentation — no new server features except a `/health` endpoint.

## Codebase Analysis

### Current State

**Project structure (settings.gradle.kts):**
- `composeApp` — Compose Multiplatform client (Android, iOS, JVM Desktop, WASM)
- `server` — Ktor server application (main entry point)
- `server:auth`, `server:groups`, `server:files`, `server:ai` — Feature modules
- `server:core:config`, `server:core:database`, `server:core:security` — Core modules
- `core:models`, `core:mvi`, `core:testing`, `core:sdk`, `core:storage` — Shared core
- `app:auth`, `app:admin`, `app:dashboard`, `app:designsystem`, `app:documents`, `app:profile` — App features
- `shared` — Code shared between all targets

**Root build.gradle.kts:** Currently only has plugin declarations and allprojects config. No custom tasks exist — all dev tooling tasks will be new.

**docker-compose.yml services:**
- `postgres` (pgvector/pgvector:pg15) — port 5436, DB "application", has healthcheck
- `minio` (minio/minio:latest) — API port 9002, console port 9003, has healthcheck
- `minio-init` (minio/mc:latest) — creates "uploads" bucket on startup
- `mailhog` (mailhog/mailhog:latest) — SMTP port 1025, web UI port 8025, has healthcheck

**Env.kt (server/core/config):** All env vars use `System.getenv()` with fallback defaults. Sections: Http, Auth, OAuth, Ai, S3, Email, ServerConfig. All defaults point to local Docker services.

**Existing setup.sh:** Interactive project renaming wizard (package names, DB name). Not related to dev environment setup.

**.gitignore:** Already includes `.env` entry.

**README.md:** Generic KMP boilerplate — needs complete rewrite.

**docs/:** Only contains Swagger codegen ignore files — effectively empty.

### Migration System

Migrations are registered in `Application.kt` before `startDatabase()`:
```kotlin
registerAuthMigrations()
registerGroupMigrations()
registerAiMigrations()
registerVectorMigrations()
val database = startDatabase()
```

Each feature module has a `register*Migrations()` function that calls `MigrationRegistry.register()`. The `startDatabase()` function runs all registered migrations.

**Implication for devUp:** The Gradle task cannot run migrations directly — migrations are embedded in the server application startup. The devUp task should start Docker services and then start the server (which runs migrations automatically). Alternatively, a separate migration runner could be extracted, but that's out of scope. The simplest approach: devUp starts Docker, then the first server run handles migrations.

**Revised approach:** devUp should: (1) start Docker services, (2) wait for healthy, (3) document that migrations run automatically on first server start. Seed data can be a separate `seedData` task that hits the server API or runs SQL directly.

### Server Application Entry Point

Uses Arrow's `SuspendApp` with `resourceScope`. Server starts via `startServer(Netty)` with Koin DI. The server already has CORS, StatusPages, WebSockets, Content Negotiation configured.

### Health Endpoint Considerations

No `/health` endpoint currently exists. Need to add one that checks:
- Database connectivity (R2dbcDatabase query)
- MinIO reachability (HTTP health check to S3 endpoint)
- SMTP reachability (socket connection to MailHog)

The health endpoint should be unauthenticated (outside `conduitAuth`). It can be added directly in the server module's routing block in Application.kt.

### Dotenv Library Research

**Best option: `io.github.cdimascio:dotenv-kotlin`** (aka java-dotenv)
- Maven: `io.github.cdimascio:dotenv-kotlin:6.4.2` (latest stable)
- Loads `.env` file and makes values available via `dotenv["KEY"]`
- **Compatibility concern:** Env.kt uses `System.getenv()` directly. The dotenv-kotlin library does NOT modify `System.getenv()` — it provides its own `dotenv["KEY"]` accessor.
- **Integration options:**
  1. **Modify Env.kt** to use dotenv accessor instead of `System.getenv()` — cleanest but touches existing code everywhere
  2. **Use `systemProperties` approach** — set system properties from .env, read with `System.getProperty()` fallback
  3. **Use `dotenv-kotlin` with `SystemProperties` configuration** — the library has an option to put entries into system environment (not directly, but close)

**Recommended approach:** The simplest approach that preserves `System.getenv()` compatibility is to use a JVM agent or startup hook that loads `.env` into the process environment. However, `System.getenv()` returns an unmodifiable map in Java.

**Practical solution:** Modify Env.kt to create a helper function:
```kotlin
private val dotenv = Dotenv.configure().ignoreIfMissing().load()
private fun env(key: String): String? = dotenv[key] ?: System.getenv(key)
```
Then replace all `System.getenv("X")` calls with `env("X")`. This is a small, localized change that preserves all existing behavior (System.getenv still works as fallback) while adding .env file support.

**Dependency:** Add to `server/core/config/build.gradle.kts`:
```kotlin
implementation("io.github.cdimascio:dotenv-kotlin:6.4.2")
```

### Gradle Task Implementation

Gradle tasks for Docker operations should use the `Exec` task type. Key considerations:

- `devUp`: `docker compose up -d --wait` (the `--wait` flag blocks until healthchecks pass)
- `checkSetup`: Custom task with `doLast {}` block that runs checks programmatically
- `seedData`: Can use `Exec` to run `psql` via Docker exec, or a custom SQL file
- `verifySetup`: Custom task that runs HTTP checks after server is running
- `testAll`: Depends on `:server:test`, `:shared:allTests`, `:composeApp:allTests`
- `devSetup`: Depends on `checkSetup`, `devUp` in sequence

**Port availability check:** Can be done with Java `ServerSocket` in Gradle task:
```kotlin
try { java.net.ServerSocket(port).close(); true }
catch (_: Exception) { false }
```

### Seed Data Strategy

Context says: "just a demo user for login." The seed data should:
1. Insert a user with known credentials (e.g., `dev@example.com` / `password`)
2. Be idempotent (check if exists, skip if present)
3. Run via direct SQL against PostgreSQL

Implementation: Create `init-scripts/02-seed-dev-data.sql` that inserts the demo user. Since the app uses bcrypt (jbcrypt library), the password hash needs to be pre-computed. A bcrypt hash of "password" can be hardcoded in the seed script.

**Alternative:** Run seedData as a Gradle task that executes SQL via `docker exec` against the running PostgreSQL container:
```bash
docker exec template-postgres psql -U postgres -d application -c "INSERT INTO ..."
```

This is simpler and doesn't require the seed to run on every Docker startup (only when explicitly requested).

## Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Dotenv library | `io.github.cdimascio:dotenv-kotlin:6.4.2` | Most popular Kotlin dotenv library, 2K+ GitHub stars, actively maintained |
| Dotenv integration | Helper function in Env.kt wrapping dotenv + System.getenv | Minimal code change, preserves existing System.getenv fallback |
| Gradle task type | Mix of Exec (Docker commands) and custom tasks (checks) | Exec for shell commands, custom for programmatic checks |
| Migration running | Documented as automatic on server start (existing behavior) | Migrations are embedded in server startup, no separate runner needed |
| Seed data | SQL via `docker exec` in Gradle task | Idempotent, doesn't require server to be running |
| Health endpoint | Unauthenticated GET /health in server routing | Standard pattern, checks DB + MinIO + SMTP |
| devUp --wait | Uses `docker compose up -d --wait` flag | Blocks until all healthchecks pass, built into Docker Compose |

## Requirement Coverage

| Requirement | Coverage Plan |
|-------------|--------------|
| ONBOARD-01 | `checkSetup` task (prereq checks) + `devUp` task (Docker + migrations + seed) |
| ONBOARD-02 | README.md rewrite + docs/ARCHITECTURE.md + "add a feature" pattern reference |
| ONBOARD-03 | `devUp`, `seedData`, `testAll`, `devSetup`, `checkSetup`, `verifySetup` tasks |
| ONBOARD-04 | docs/GETTING-STARTED.md walkthrough with troubleshooting section |

## Risk Assessment

| Risk | Mitigation |
|------|------------|
| dotenv-kotlin library compatibility with Kotlin 2.3.10 | Library is pure Java/Kotlin, no compiler plugin — should work. Test during implementation |
| Docker Compose v2 vs v1 CLI differences | Use `docker compose` (v2 style) which is standard since Docker Desktop 3.x |
| Seed data bcrypt hash portability | Pre-compute hash with jbcrypt and hardcode in SQL — identical to app's hashing |
| Port conflict detection false positives | Check only the specific ports used by Docker services (5436, 9002, 9003, 1025, 8025) |

## RESEARCH COMPLETE
