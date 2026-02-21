# Phase 17: Infrastructure Foundation - Context

**Gathered:** 2026-02-21
**Status:** Ready for planning

<domain>
## Phase Boundary

Scaffold all Docker services and Kotlin configuration sections needed by feature phases (18-21). This phase creates the infrastructure layer -- Docker Compose services (MinIO, MailHog, pgvector extension), Env config sections (S3, Email), and the vector storage table. No application-level services or API endpoints are built here.

</domain>

<decisions>
## Implementation Decisions

### Docker Compose services
- Extend existing `docker-compose.yml` (already has PostgreSQL on port 5436) -- do not create a separate file
- Switch PostgreSQL image from `postgres:15-alpine` to `pgvector/pgvector:pg15` to include the vector extension
- Add MinIO service with default bucket auto-created via `mc` sidecar or entrypoint command
- Add MailHog service for local SMTP capture
- All services must have healthchecks for `docker compose up --wait` to work
- MinIO API on port 9000, console on port 9001
- MailHog SMTP on port 1025, web UI on port 8025

### Configuration sections
- Follow existing `Env` pattern: nested data classes with `System.getenv()` and sensible defaults
- `Env.S3` section: endpoint, bucket, region, accessKey, secretKey -- defaults point to local MinIO
- `Env.Email` section: host, port, fromAddress, username, password -- defaults point to local MailHog (no auth needed)
- Default values must allow `./gradlew :server:run` to work against `docker compose up` with zero env var setup

### Vector storage table
- Table name: `document_embeddings`
- Create via Exposed migration (existing migration system)
- Enable pgvector extension (`CREATE EXTENSION IF NOT EXISTS vector`) in migration
- Vector column for embeddings -- dimension size decided by researcher (depends on Koog embedding model output)
- Include: id (UUID), groupId (UUID FK), content (TEXT), embedding (VECTOR), metadata (JSONB), createdAt (TIMESTAMP)
- groupId is required for scope isolation (Phase 19 RAG needs group-scoped queries)

### Init scripts
- Use existing `init-scripts/` volume mount for pgvector extension enablement as fallback
- Primary extension creation through Exposed migration (programmatic, version-controlled)

### Claude's Discretion
- MinIO bucket creation approach (mc sidecar vs. entrypoint script vs. application-level init)
- Exact volume mount paths for MinIO data persistence
- Whether to add a `docker-compose.override.yml` pattern for developer customization
- MailHog vs. Mailpit choice (both work, MailHog is more established)

</decisions>

<specifics>
## Specific Ideas

- Docker Compose must remain a single `docker compose up` command -- no multi-step startup
- Environment variable naming should follow the existing pattern (SCREAMING_SNAKE_CASE with descriptive prefixes: `S3_ENDPOINT`, `SMTP_HOST`, etc.)
- The `.env.example` file should be created/updated with all new env vars and their default values documented
- PostgreSQL port stays at 5436 (already established, avoid breaking existing developer setups)

</specifics>

<deferred>
## Deferred Ideas

None -- discussion stayed within phase scope

</deferred>

---

*Phase: 17-infrastructure-foundation*
*Context gathered: 2026-02-21*
