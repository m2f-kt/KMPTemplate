---
phase: 17-infrastructure-foundation
plan: 02
subsystem: database
tags: [pgvector, exposed, r2dbc, vector, embeddings, rag, migration]

# Dependency graph
requires:
  - phase: 17-infrastructure-foundation/01
    provides: pgvector Docker image and init-scripts/01-enable-pgvector.sql
provides:
  - Custom VectorColumnType for Exposed R2DBC mapping List<Float> to pgvector vector(N)
  - Table.vector() extension function for declaring vector columns
  - DocumentEmbeddingsTable with group-scoped vector(768) column
  - Migration enabling pgvector extension and creating document_embeddings table
affects: [19-structured-ai-rag]

# Tech tracking
tech-stack:
  added: []
  patterns: [custom Exposed ColumnType for pgvector, TransactionManager.current().exec() for raw SQL in migrations]

key-files:
  created:
    - server/core/database/src/main/kotlin/com/m2f/core/database/vector/VectorColumnType.kt
    - server/core/database/src/main/kotlin/com/m2f/core/database/tables/DocumentEmbeddingsTable.kt
    - server/core/database/src/main/kotlin/com/m2f/core/database/migrations/VectorMigrations.kt
  modified:
    - server/src/main/kotlin/com/m2f/template/Application.kt

key-decisions:
  - "Used TransactionManager.current().exec() instead of top-level exec() for raw SQL in migrations (exec is an R2dbcTransaction method, not a standalone function)"
  - "Added @file:OptIn(ExperimentalUuidApi::class) following UsersTable pattern for UUID columns"
  - "Vector dimension 768 matches Google text-embedding-004 output (Koog/Gemini stack)"
  - "metadata column uses text type (not JSONB) to avoid R2DBC driver issues, matching ConversationsTable pattern"

patterns-established:
  - "VectorColumnType: custom Exposed column type for pgvector, reusable for any vector dimension"
  - "Raw SQL in migrations via TransactionManager.current().exec() when SchemaUtils is insufficient"

requirements-completed: [RAG-01]

# Metrics
duration: 10min
completed: 2026-02-21
---

# Plan 17-02: VectorColumnType and document_embeddings Migration Summary

**Custom Exposed VectorColumnType mapping List<Float> to pgvector vector(768), DocumentEmbeddingsTable with group-scoped embeddings, and programmatic migration**

## Performance

- **Duration:** 10 min
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Custom VectorColumnType maps List<Float> to/from pgvector string format with dimension validation
- Table.vector() extension function for declaring vector columns in Exposed table definitions
- DocumentEmbeddingsTable with id, groupId (indexed), content, embedding (vector 768), metadata, createdAt
- Migration enables pgvector extension and creates document_embeddings table programmatically
- registerVectorMigrations() called in Application.kt startup before startDatabase()

## Task Commits

Each task was committed atomically:

1. **Task 1: VectorColumnType** - `220f612` (feat: custom VectorColumnType for pgvector Exposed R2DBC)
2. **Task 2: DocumentEmbeddingsTable + migration + registration** - `388875d` (feat: DocumentEmbeddingsTable and pgvector migration)

## Files Created/Modified
- `server/core/database/src/main/kotlin/com/m2f/core/database/vector/VectorColumnType.kt` - Custom Exposed column type and Table.vector() extension
- `server/core/database/src/main/kotlin/com/m2f/core/database/tables/DocumentEmbeddingsTable.kt` - Exposed table definition for document_embeddings
- `server/core/database/src/main/kotlin/com/m2f/core/database/migrations/VectorMigrations.kt` - Migration class and registerVectorMigrations() function
- `server/src/main/kotlin/com/m2f/template/Application.kt` - Added registerVectorMigrations() call

## Decisions Made
- Used `TransactionManager.current().exec()` for raw SQL because `exec` is a method on `R2dbcTransaction`, not available as a top-level import in Exposed R2DBC v1
- Added `@file:OptIn(ExperimentalUuidApi::class)` following UsersTable pattern for UUID auto-generate
- Used `text` type for metadata column (not JSONB) to avoid R2DBC JSONB driver issues, consistent with ConversationsTable

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] exec() import resolution**
- **Found during:** Task 2 (VectorMigrations compilation)
- **Issue:** Plan specified `import org.jetbrains.exposed.v1.r2dbc.exec` but `exec` is a method on `R2dbcTransaction`, not a top-level function
- **Fix:** Used `TransactionManager.current().exec()` to access the current transaction's exec method
- **Files modified:** VectorMigrations.kt
- **Verification:** `./gradlew :server:compileKotlin` passes
- **Committed in:** 388875d (Task 2 commit)

**2. [Rule 3 - Blocking] ExperimentalUuidApi opt-in missing**
- **Found during:** Task 2 (DocumentEmbeddingsTable compilation)
- **Issue:** `uuid()` and `autoGenerate()` require `@OptIn(ExperimentalUuidApi::class)` in Kotlin 2.1
- **Fix:** Added `@file:OptIn(ExperimentalUuidApi::class)` following UsersTable pattern
- **Files modified:** DocumentEmbeddingsTable.kt
- **Verification:** `./gradlew :server:compileKotlin` passes
- **Committed in:** 388875d (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Both fixes necessary for compilation. No scope creep.

## Issues Encountered
None beyond the auto-fixed compilation issues.

## User Setup Required
None - migration runs automatically on server startup.

## Next Phase Readiness
- VectorColumnType ready for Phase 19 RAG pipeline to store and query embeddings
- DocumentEmbeddingsTable ready for embedding storage with group-scoped isolation
- Phase 17 infrastructure foundation complete

---
*Plan: 17-02-infrastructure-foundation*
*Completed: 2026-02-21*
