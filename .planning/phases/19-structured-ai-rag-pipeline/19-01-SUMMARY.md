---
phase: 19-structured-ai-rag-pipeline
plan: 01
subsystem: database
tags: [exposed, pgvector, dto, ktor-resources, migration]

requires:
  - phase: 17-infrastructure-foundation
    provides: DocumentEmbeddingsTable, pgvector extension, vector column type
provides:
  - DocumentsTable for source document metadata
  - DocumentEmbeddingsTable with userId, documentId, chunkIndex columns
  - Migration for schema evolution
  - Document DTOs (upload request, response, list response)
  - Documents @Resource routes for CRUD operations
  - AppError.Document sealed hierarchy
affects: [19-02, 19-03, 19-04, 19-05, 19-06]

tech-stack:
  added: []
  patterns: [ALTER TABLE via TransactionManager.current().exec() for column additions]

key-files:
  created:
    - server/core/database/src/main/kotlin/com/m2f/core/database/tables/DocumentsTable.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/dto/DocumentDtos.kt
  modified:
    - server/core/database/src/main/kotlin/com/m2f/core/database/tables/DocumentEmbeddingsTable.kt
    - server/core/database/src/main/kotlin/com/m2f/core/database/migrations/VectorMigrations.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
    - core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt

key-decisions:
  - "Nullable userId/documentId on DocumentEmbeddingsTable for backward compatibility with existing embeddings"

patterns-established:
  - "ALTER TABLE with IF NOT EXISTS for adding columns to existing tables in migrations"

requirements-completed: [RAG-03, RAG-06]

duration: 5min
completed: 2026-02-24
---

# Plan 19-01: Database Schema & Shared Models Summary

**DocumentsTable for RAG source metadata, DocumentEmbeddingsTable evolution with scoping columns, shared DTOs, @Resource routes, and AppError.Document hierarchy**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-24
- **Completed:** 2026-02-24
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- DocumentsTable with all required columns (groupId, userId, assignedToUserId, scope, status, chunkCount)
- DocumentEmbeddingsTable evolved with userId, documentId, chunkIndex for document-level scoping
- Migration with SchemaUtils.create + ALTER TABLE statements for backward-compatible column additions
- Document DTOs available in core:models for all KMP targets
- Documents @Resource routes for upload, list, get, and delete operations
- AppError.Document sealed hierarchy with NotFound, IngestionFailed, AccessDenied

## Task Commits

Each task was committed atomically:

1. **Task 1 + Task 2: DocumentsTable + DocumentEmbeddingsTable + DTOs + routes + errors** - `d7bf1e2` (feat)

## Files Created/Modified
- `server/core/database/.../tables/DocumentsTable.kt` - Source document metadata table
- `server/core/database/.../tables/DocumentEmbeddingsTable.kt` - Added userId, documentId, chunkIndex columns
- `server/core/database/.../migrations/VectorMigrations.kt` - AddDocumentsTableAndEmbeddingColumnsMigration
- `core/models/.../dto/DocumentDtos.kt` - DocumentUploadRequest, DocumentResponse, DocumentListResponse
- `core/models/.../routes/ApiRoutes.kt` - Documents @Resource class with nested routes
- `core/models/.../AppError.kt` - AppError.Document sealed hierarchy

## Decisions Made
- Made userId and documentId nullable on DocumentEmbeddingsTable so existing embeddings (from Phase 17 tests) remain valid

## Deviations from Plan
None - plan executed exactly as written

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Database schema ready for DocumentRepository and DocumentIngestionService (Plan 03)
- Shared DTOs and routes ready for SDK implementation (Plan 05)
- AppError.Document types ready for error mapping in routes

---
*Phase: 19-structured-ai-rag-pipeline*
*Completed: 2026-02-24*
