---
phase: 19-structured-ai-rag-pipeline
plan: 02
subsystem: ai
tags: [koog, structured-output, pgvector, embeddings, rag, vector-storage]

requires:
  - phase: 17-infrastructure-foundation
    provides: pgvector extension, DocumentEmbeddingsTable, VectorColumnType
  - phase: 19-01
    provides: DocumentEmbeddingsTable with userId/documentId/chunkIndex columns
provides:
  - StructuredOutputService for typed Koog executeStructured calls
  - RelevanceDetector for auto-RAG heuristic
  - PgVectorStorage implementing VectorStorage<String> with cosine similarity
  - TextEmbedding004 model constant (768 dims)
affects: [19-03, 19-04, 19-06]

tech-stack:
  added: []
  patterns: [executeStructured with StructureFixingParser, R2DBC exec with Row transform for raw pgvector SQL]

key-files:
  created:
    - server/ai/src/main/kotlin/com/m2f/server/ai/structured/StructuredOutputService.kt
    - server/ai/src/main/kotlin/com/m2f/server/ai/rag/RelevanceDetector.kt
    - server/ai/src/main/kotlin/com/m2f/server/ai/rag/PgVectorStorage.kt
    - server/ai/src/main/kotlin/com/m2f/server/ai/Models.kt
  modified: []

key-decisions:
  - "@PublishedApi internal for executor in StructuredOutputService (inline reified requires non-private)"
  - "R2DBC exec with Row transform for pgvector SQL (not JDBC ResultSet callback)"
  - "Fail-open on RelevanceDetector errors (needsContext=false default)"

patterns-established:
  - "StructuredOutputService inline reified pattern for typed LLM output"
  - "R2DBC raw SQL with Row transform: exec(sql, StatementType.SELECT) { row -> ... }"

requirements-completed: [AISTR-01, AISTR-02, AISTR-03, RAG-02, RAG-03, RAG-07]

duration: 10min
completed: 2026-02-24
---

# Plan 19-02: Core AI Services Summary

**StructuredOutputService wrapping Koog executeStructured with Arrow Either, RelevanceDetector with auto-RAG heuristic, and PgVectorStorage with pgvector cosine similarity**

## Performance

- **Duration:** 10 min
- **Started:** 2026-02-24
- **Completed:** 2026-02-24
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- StructuredOutputService provides generic typed output via Koog executeStructured with StructureFixingParser
- RelevanceDetector determines if chat queries need document context using structured output
- PgVectorStorage implements full VectorStorage<String> interface with pgvector <=> cosine similarity
- TextEmbedding004 model constant defined for 768-dim embeddings matching pgvector column

## Task Commits

1. **Task 1 + Task 2: All three services** - `e5ed73f` (feat)

## Files Created/Modified
- `server/ai/.../structured/StructuredOutputService.kt` - Generic executeStructured wrapper with Arrow Either
- `server/ai/.../rag/RelevanceDetector.kt` - Auto-RAG heuristic using structured output
- `server/ai/.../rag/PgVectorStorage.kt` - VectorStorage<String> with pgvector cosine similarity
- `server/ai/.../Models.kt` - TextEmbedding004 model constant

## Decisions Made
- Used @PublishedApi internal for executor in StructuredOutputService (inline reified requires non-private access)
- Used R2DBC exec with Row transform for pgvector raw SQL (not JDBC-style ResultSet callback)
- RelevanceDetector fails open on errors (defaults to needsContext=false to never break chat)

## Deviations from Plan
None - plan executed exactly as written

## Issues Encountered
- Initial compilation error: inline reified function couldn't access private executor -- resolved with @PublishedApi internal
- DocumentWithPayload import path was ai.koog.rag.base (not ai.koog.embeddings.base as initially assumed)
- VectorStorage import path was ai.koog.rag.vector (not ai.koog.vectors.storage)

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- StructuredOutputService ready for DI wiring in Plan 03
- PgVectorStorage ready for DocumentIngestionService and RagService
- RelevanceDetector ready for ChatAgentService RAG integration in Plan 04

---
*Phase: 19-structured-ai-rag-pipeline*
*Completed: 2026-02-24*
