---
phase: 19-structured-ai-rag-pipeline
plan: 03
subsystem: ai
tags: [document-ingestion, rag, embeddings, koog, multipart-upload, s3]

requires:
  - phase: 19-01
    provides: DocumentsTable, DocumentEmbeddingsTable with userId/documentId/chunkIndex
  - phase: 19-02
    provides: TextEmbedding004 model constant, GoogleLLMClient
provides:
  - DocumentRepository for CRUD on DocumentsTable
  - DocumentIngestionService for chunking + embedding pipeline
  - DocumentRoutes with upload/list/get/delete endpoints
  - Document-specific error types (NotFound, AccessDenied, IngestionFailed)
  - DI wiring with shared GoogleLLMClient, LLMEmbedder
affects: [19-04, 19-05, 19-06]

tech-stack:
  added: []
  patterns: [lambda bridges for cross-module dependencies, background coroutine ingestion]

key-files:
  created:
    - server/ai/src/main/kotlin/com/m2f/server/ai/rag/DocumentRepository.kt
    - server/ai/src/main/kotlin/com/m2f/server/ai/rag/DocumentIngestionService.kt
    - server/ai/src/main/kotlin/com/m2f/server/ai/routes/DocumentRoutes.kt
  modified:
    - server/ai/src/main/kotlin/com/m2f/server/ai/errors/AiErrors.kt
    - server/ai/src/main/kotlin/com/m2f/server/ai/di/AiModule.kt
    - server/src/main/kotlin/com/m2f/template/Application.kt

key-decisions:
  - "Lambda bridges for cross-module dependencies (fileUploader, fileDeleter, roleChecker) instead of direct module dependencies"
  - "Auto-ingest on upload via background CoroutineScope(aiDispatcher).launch"
  - "FileService.upload() returns FileResponse, lambda extracts .key for document record"
  - "arrow.core.raise.context.ensure import (not arrow.core.raise.ensure) for Kotlin 2.x context parameters"
  - "Shared GoogleLLMClient singleton in Koin DI"

patterns-established:
  - "Lambda bridge pattern for cross-module service access in routes"
  - "Background ingestion with status tracking (pending->processing->indexed/failed)"

requirements-completed: [RAG-04, RAG-06, RAG-07]

duration: 15min
completed: 2026-02-24
---

# Plan 19-03: Document Ingestion Pipeline Summary

**DocumentRepository CRUD, DocumentIngestionService chunking+embedding, DocumentRoutes upload/list/get/delete, DI wiring with shared GoogleLLMClient**

## Performance

- **Duration:** 15 min
- **Started:** 2026-02-24
- **Completed:** 2026-02-24
- **Tasks:** 2
- **Files modified:** 6 (3 created, 3 modified)

## Accomplishments
- DocumentRepository provides full CRUD for DocumentsTable with scope/group/user filtering
- DocumentIngestionService chunks text (~500 chars, ~100 overlap), embeds via Koog LLMEmbedder, stores in pgvector
- DocumentRoutes provides authenticated multipart upload, list, get, delete endpoints with role-based access
- Document-specific errors added to AiErrors.kt (NotFound, AccessDenied, IngestionFailed)
- AiModule wires shared GoogleLLMClient, LLMEmbedder, DocumentRepository, DocumentIngestionService
- Application.kt registers documentRoutes with lambda bridges to FileService and MembershipRepository

## Task Commits

1. **Task 1 + Task 2: All pipeline components** - `16ec765` (feat)

## Files Created/Modified
- `server/ai/.../rag/DocumentRepository.kt` - CRUD for DocumentsTable with DocumentRow data class
- `server/ai/.../rag/DocumentIngestionService.kt` - Chunk + embed + store pipeline with status tracking
- `server/ai/.../routes/DocumentRoutes.kt` - Upload/list/get/delete with lambda bridge pattern
- `server/ai/.../errors/AiErrors.kt` - Added DocumentNotFound, DocumentAccessDenied, DocumentIngestionFailed
- `server/ai/.../di/AiModule.kt` - Shared GoogleLLMClient, LLMEmbedder, DocumentRepository, DocumentIngestionService
- `server/.../Application.kt` - Document routes registration with cross-module lambda bridges

## Decisions Made
- Used lambda parameters (fileUploader, fileDeleter, roleChecker) to avoid circular module dependencies between ai, files, and groups modules
- Auto-ingest on upload via background coroutine -- upload returns immediately with "pending" status
- Used `arrow.core.raise.context.ensure` (not `arrow.core.raise.ensure`) for Kotlin 2.x context parameter compatibility
- Created shared GoogleLLMClient singleton in Koin (replaces per-service instances per research pitfall #5)

## Deviations from Plan
- Plan suggested direct FileService dependency in documentRoutes; implemented lambda bridges instead to keep ai module decoupled
- StructuredOutputService and RelevanceDetector DI wiring deferred to Plan 19-04 (not needed for ingestion pipeline)

## Issues Encountered
- `arrow.core.raise.ensure` didn't resolve inside `conduitAuth` blocks (wrong import for Kotlin 2.x context parameters) -- fixed with `arrow.core.raise.context.ensure`
- `fileUploader(this, ...)` passed RoutingContext instead of Raise<DomainError> -- fixed by removing explicit `this` (context resolved implicitly)
- `Models.TextEmbedding004` reference failed because TextEmbedding004 is a top-level val, not object member -- fixed import

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- DocumentRepository and DocumentIngestionService ready for RagService in Plan 19-04
- DocumentRoutes ready for client SDK integration in Plan 19-05
- DI wiring provides foundation for StructuredOutputService/RelevanceDetector in Plan 19-04

---
*Phase: 19-structured-ai-rag-pipeline*
*Completed: 2026-02-24*
