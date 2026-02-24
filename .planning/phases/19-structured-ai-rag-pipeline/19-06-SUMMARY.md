---
phase: 19-structured-ai-rag-pipeline
plan: 06
subsystem: ai
tags: [integration-tests, document-routes, structured-output, pgvector, testcontainers, kotest]

requires:
  - phase: 19-03
    provides: DocumentRoutes, DocumentIngestionService, DocumentRepository
  - phase: 19-04
    provides: StructuredOutputService, RelevanceDetector
provides:
  - DocumentRoutesTest with document CRUD and group isolation coverage
  - StructuredOutputTest with typed response and fail-open verification
  - TestHelpers with pgvector Testcontainers, fake LLMEmbedder, documentTestApp
affects: []

tech-stack:
  added: []
  patterns: [fake LLMEmbeddingProvider for test embedder, conditional API tests via Assume]

key-files:
  created:
    - server/ai/src/test/kotlin/com/m2f/server/ai/TestHelpers.kt
    - server/ai/src/test/kotlin/com/m2f/server/ai/DocumentRoutesTest.kt
    - server/ai/src/test/kotlin/com/m2f/server/ai/StructuredOutputTest.kt
  modified:
    - server/ai/build.gradle.kts

key-decisions:
  - "pgvector/pgvector:pg15 image for Testcontainers (matches Docker Compose; needed for vector migrations)"
  - "FakeEmbeddingProvider returns 768-dim zero vectors for LLMEmbedder construction"
  - "documentTestApp accepts roleChecker lambda for authorization test scenarios"
  - "StructuredOutputTest integration tests gated by AI_GOOGLE_API_KEY via JUnit Assume"
  - "RelevanceDetector fail-open test uses invalid API key to verify graceful degradation"

patterns-established:
  - "Fake LLMEmbeddingProvider pattern for testing AI services without live API"
  - "Configurable roleChecker in test app for flexible authorization testing"

requirements-completed: [DEBT-03, AISTR-01, AISTR-02, AISTR-03]

duration: 10min
completed: 2026-02-24
---

# Plan 19-06: Integration Tests Summary

**DocumentRoutesTest for document CRUD + scoping, StructuredOutputTest for typed response verification**

## Performance

- **Duration:** 10 min
- **Started:** 2026-02-24
- **Completed:** 2026-02-24
- **Tasks:** 2
- **Files modified:** 4 (3 created, 1 modified)

## Accomplishments
- TestHelpers.kt with pgvector Testcontainers, fake LLMEmbeddingProvider, JWT helpers, documentTestApp
- DocumentRoutesTest with 6 test cases: upload, 401 auth, list, group isolation, delete, member scope restriction, unsupported type
- StructuredOutputTest with 5 test cases: annotation verification, field structure, fail-open behavior, conditional real API tests
- Added server:core:security and ktor-client-content-negotiation test dependencies

## Task Commits

1. **Task 1 + Task 2** - `5b8f7bd` (test)

## Files Created/Modified
- `server/ai/src/test/.../TestHelpers.kt` - Test infrastructure with pgvector, fake embedder, documentTestApp
- `server/ai/src/test/.../DocumentRoutesTest.kt` - Document routes integration tests (6 tests)
- `server/ai/src/test/.../StructuredOutputTest.kt` - Structured output tests (5 tests)
- `server/ai/build.gradle.kts` - Added test dependencies

## Decisions Made
- Used fake LLMEmbeddingProvider returning zero vectors instead of mocking (no mockk in project)
- Integration tests with real Gemini API gated by `Assume.assumeNotNull(apiKey)` to skip gracefully in CI
- documentTestApp role checker defaults to `true` (admin) and can be overridden per test

## Deviations from Plan
- Plan suggested 5 document tests, implemented 6 (added unsupported file type test)
- Used JUnit Assume instead of kotest skipping for conditional API tests

## Issues Encountered
- PostgreSQLContainer generic type parameter not accepted in this version; used raw type

## Next Phase Readiness
- All 6 plans complete; ready for phase verification and roadmap update

---
*Phase: 19-structured-ai-rag-pipeline*
*Completed: 2026-02-24*
