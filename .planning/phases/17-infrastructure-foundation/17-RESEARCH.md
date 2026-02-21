# Phase 17: Infrastructure Foundation - Research

## Codebase Analysis

### Docker Compose (Current State)
- **File:** `docker-compose.yml` (root)
- **Services:** PostgreSQL only (`postgres:15-alpine` on port 5436)
- **Volumes:** `postgres_data` for persistence, `./init-scripts` mounted to `/docker-entrypoint-initdb.d`
- **Init scripts directory:** Exists but empty -- available for pgvector extension SQL
- **Healthcheck:** `pg_isready` pattern established

### Env Configuration Pattern
- **File:** `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt`
- **Pattern:** Nested data classes inside `Env`, each property reads `System.getenv()` with fallback defaults
- **Existing sections:** `Http`, `Auth`, `OAuth`, `Ai`, `ServerConfig`
- **Naming convention:** Environment variables use SCREAMING_SNAKE_CASE (`JWT_SECRET`, `GOOGLE_API_KEY`, etc.)
- **Principle:** Defaults allow server to start with zero env var configuration against `docker compose up`

### Database/Migration Pattern
- **Migration interface:** `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/Migration.kt`
- **Registration:** Each module provides a `registerXxxMigrations()` top-level function
- **Version format:** `YYYYMMDDHHMMSS` (timestamp-based)
- **Application startup:** `Application.kt` calls `registerAuthMigrations()`, `registerGroupMigrations()`, `registerAiMigrations()` before `startDatabase()`
- **Table definitions:** Exposed `object : Table("name")` pattern, e.g., `ConversationsTable`
- **SchemaUtils:** `SchemaUtils.create(TableObject)` for migration execution

### DataSource Configuration
- **File:** `server/core/database/src/main/kotlin/com/m2f/core/database/connection/DataSource.kt`
- **Env vars:** `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`, `DATABASE_URL`
- **Default port:** 5436 (mapped from container 5432)

### Server Module Structure
```
server/
  core/
    config/    -- Configuration, Env
    database/  -- DataSource, migrations, Startup
    security/  -- JWT security
  auth/        -- Auth migrations, routes, services
  groups/      -- Group migrations, routes, services
  ai/          -- AI migrations, routes, agents
  src/         -- Application.kt entrypoint
```

## Technical Decisions

### pgvector Image
- Switch `postgres:15-alpine` to `pgvector/pgvector:pg15` -- drop-in replacement that includes the vector extension
- No Dockerfile needed; the official pgvector image is based on the same postgres image
- Extension must still be explicitly enabled via `CREATE EXTENSION IF NOT EXISTS vector`
- Add SQL file to `init-scripts/` for first-run extension creation AND use migration for programmatic tracking

### MinIO Service
- Image: `minio/minio:latest`
- Ports: 9000 (API), 9001 (Console)
- Default bucket: Create via `mc` client in a one-shot sidecar service (standard Docker pattern)
- Health check: `curl -f http://localhost:9000/minio/health/live`
- Credentials: `minioadmin`/`minioadmin` (standard dev defaults)

### MailHog Service
- Image: `mailhog/mailhog:latest`
- Ports: 1025 (SMTP), 8025 (Web UI)
- No auth needed for local dev
- Health check: HTTP GET on port 8025

### Vector Column Type
- Exposed R2DBC has no native vector column type
- Need custom `VectorColumnType` extending `ColumnType` to map `vector(N)` SQL type
- Dimension TBD (depends on embedding model -- 768 for Google's `text-embedding-004`, 1536 for OpenAI)
- Use 768 as default (Gemini embedding model, aligns with Koog/Google stack)

### document_embeddings Table Schema
```sql
CREATE TABLE document_embeddings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    content TEXT NOT NULL,
    embedding vector(768) NOT NULL,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_embeddings_group ON document_embeddings(group_id);
```
- `group_id` for scope isolation (Phase 19 RAG)
- No FK to groups table yet (RAG module may evolve)
- Index on `group_id` for efficient filtering before cosine similarity

## Wave Analysis

This phase has two natural groupings:
1. **Docker + Config** (docker-compose.yml, Env.kt, init-scripts) -- no Kotlin compilation needed
2. **Database table** (Migration, Table definition, Application.kt registration) -- depends on pgvector being available

However, both are small enough that 2-3 plans in a single wave works well. The Docker changes and Env config changes can be in one plan, the migration in another. They can execute in parallel since the migration tests would need `docker compose up` anyway (manual verification step).

## Files to Modify

### Docker & Config (Plan 01)
- `docker-compose.yml` -- Add MinIO, MailHog services; switch postgres image to pgvector
- `init-scripts/01-enable-pgvector.sql` -- CREATE EXTENSION IF NOT EXISTS vector
- `server/core/config/src/main/kotlin/com/m2f/core/config/configuration/Env.kt` -- Add S3 and Email sections

### Database Migration (Plan 02)
- New file: `server/core/database/src/main/kotlin/com/m2f/core/database/vector/VectorColumnType.kt` -- Custom Exposed column type
- New file: `server/core/database/src/main/kotlin/com/m2f/core/database/tables/DocumentEmbeddingsTable.kt` -- Exposed table definition
- New file: `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/VectorMigrations.kt` -- Migration + registration function
- `server/src/main/kotlin/com/m2f/template/Application.kt` -- Register vector migrations

## Risks/Notes

- **Exposed R2DBC vector column:** Custom `ColumnType` may have issues with R2DBC driver serialization. The pgvector JDBC driver uses `PGvector` class; R2DBC uses `io.r2dbc.postgresql` which handles vector as `String`. The custom type should map to/from String representation `[0.1,0.2,...]`.
- **MinIO bucket creation race:** The `mc` sidecar must wait for MinIO health before running. Use `depends_on` with `condition: service_healthy`.
- **MailHog vs Mailpit:** MailHog is established but unmaintained. Mailpit is the active fork. Context says MailHog; keeping that decision.

## RESEARCH COMPLETE
