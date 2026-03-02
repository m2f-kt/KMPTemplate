# Phase 22: Developer Onboarding - Context

**Gathered:** 2026-03-02
**Status:** Ready for planning

<domain>
## Phase Boundary

A new developer can go from `git clone` to running the full app with all services in under 10 minutes. Covers: setup CLI (prerequisite checks), Gradle tooling shortcuts, architecture documentation, and first-run walkthrough. Does NOT include automated scaffolding tools or Claude skills.

</domain>

<decisions>
## Implementation Decisions

### Setup CLI & prerequisite detection
- Implemented as a Gradle task (`./gradlew checkSetup`), not a shell script
- Checks: Docker & Docker Compose running, JDK version is 11+, environment config files exist (.env), port availability (5436, 9002, 9003, 1025, 8025, 8080)
- On failure: report ALL issues at once with actionable fix instructions per check, then fail the task
- Output style: checklist with pass/fail marks (✅/❌) — clean, scannable

### Gradle task design
- Tasks defined in root `build.gradle.kts` — all dev tasks in one place
- Tasks to create:
  - `devUp` — docker compose up, wait for healthy, run migrations, seed sample data. The one-command full dev environment
  - `seedData` — insert a demo user account with known credentials (e.g. dev@example.com / password) for immediate login. Idempotent: checks if data exists, skips if present
  - `testAll` — run all tests across server + shared + composeApp modules
  - `devSetup` — runs checkSetup + devUp in sequence. The true first-time command
- No devDown task — not in scope (standard `docker compose down` suffices)

### Architecture documentation
- README.md rewritten as hub: setup instructions + architecture overview + links to detailed docs in docs/
- Architecture doc includes:
  - Module dependency diagram (ASCII/text: server modules auth/ai/files/groups, shared, composeApp, how they connect)
  - Data flow overview (client → Ktor route → use case → repository → DB)
  - Module responsibility guide (what each server module does and its boundary)
  - Configuration reference (env vars, Docker services, ports, credentials)
- "Add a feature" section: pattern reference guide pointing to existing modules (auth, groups) as examples of the pattern to follow (create module under server/, add routes, register migrations, wire Koin)

### First-run walkthrough
- Format: markdown checklist document in docs/, linked from README
- Scope: full path — clone → devSetup (checks + docker + migrations + seed) → start server → verify endpoints → open UIs → run tests
- Verification at end includes:
  - API health check commands (curl endpoints)
  - Service URLs to visit (MailHog 8025, MinIO 9003, app)
  - Docker container status commands
  - Automated verification task (`./gradlew verifySetup`)
- Troubleshooting section included in the walkthrough doc (not separate file)
- Troubleshooting format: Problem → Cause → Fix (exact command)
- Issues covered: port conflicts, Docker issues, JDK/Gradle issues, database connectivity

### Environment variable loading
- Add a dotenv library for auto-loading .env files at server startup — must be compatible with current `System.getenv()` pattern in Env.kt (researcher to investigate compatible Kotlin/JVM dotenv libraries)
- Create `.env.example` with ALL env vars from Env.kt, with defaults filled in
- Organize by Env.kt section: HTTP, Auth/JWT, OAuth, AI, S3, Email — each with header comment
- Add `.env` to `.gitignore`
- Walkthrough explains how .env file is loaded into the system (dotenv library → System.getenv() → Env.kt data classes)

### Verification task
- `./gradlew verifySetup` — runs after devUp to confirm everything works
- Checks both layers: Docker container health (direct) AND server /health endpoint
- A new `/health` endpoint on the server that checks DB connectivity, MinIO reachable, SMTP (MailHog) reachable — returns status JSON
- Output format: same checklist style as checkSetup (✅/❌ per service with fix suggestions on failure)

### Claude's Discretion
- Exact dotenv library choice (after researcher investigates compatibility)
- Health endpoint response JSON structure
- Exact demo user credentials
- Gradle task implementation details (Exec vs custom task classes)
- ASCII diagram style for module dependencies
- Order of troubleshooting entries

</decisions>

<specifics>
## Specific Ideas

- Developer explicitly wants a dotenv library integrated so `.env` files auto-load — not just documentation of `System.getenv()` pattern
- All env vars should be in `.env.example` even if they have defaults — complete reference
- Seed data is minimal: just a demo user for login. No sample groups, files, or AI data
- Troubleshooting is embedded in walkthrough, not a separate document — keeps context together
- "Add a feature" doc should be a pattern reference guide, not a concrete example walkthrough (scaffolding skill deferred)

</specifics>

<code_context>
## Existing Code Insights

### Reusable Assets
- `docker-compose.yml`: Already configures PostgreSQL (pgvector:pg15), MinIO, MailHog with health checks and init scripts
- `init-scripts/01-enable-pgvector.sql`: Auto-runs on first DB startup
- `Env.kt`: All env vars centralized with `System.getenv()` + defaults — dotenv library would slot in before this
- `MigrationRegistry`: Feature modules register migrations via `MigrationRegistry.register()` — devUp can call migration flow
- Server modules (auth, ai, files, groups): Well-structured examples for "add a feature" documentation

### Established Patterns
- `System.getenv()` with fallback defaults in data class constructors (Env.kt) — dotenv must be compatible with this
- Koin DI with `module {}` pattern — new health check endpoint follows this
- Context parameters pattern for dependency injection — documentation should explain this
- Feature modules under `server/` with own `build.gradle.kts` — pattern guide references this

### Integration Points
- Root `build.gradle.kts`: Where new Gradle tasks (devUp, checkSetup, etc.) will be defined
- `server/src/main/kotlin/com/m2f/template/`: Where /health endpoint route will be added
- `.gitignore`: Needs `.env` entry added
- `README.md`: Full rewrite needed (currently generic KMP boilerplate)
- `docs/`: Currently empty — walkthrough doc goes here

</code_context>

<deferred>
## Deferred Ideas

- **Claude skill for feature scaffolding** — Create a skill with a script that generates initial module scaffolding (build.gradle.kts, route file, Koin module, migration stub) to prevent hallucinations and ensure consistency. Should use the existing skill-creator skill as a guide. → Add to backlog as its own phase.

</deferred>

---

*Phase: 22-developer-onboarding*
*Context gathered: 2026-03-02*
