---
phase: quick-7
plan: "01"
subsystem: server/privacy
tags: [data-export, privacy, server, inline-processing]
dependency_graph:
  requires: []
  provides: [inline-data-export-processing, completed-export-active-query]
  affects: [server/privacy/impl]
tech_stack:
  added: []
  patterns: [inline-processing, try-catch-raise-context-parameter]
key_files:
  modified:
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedDataExportRepository.kt
decisions:
  - "Used raise.raise() to call Raise from outside the context receiver scope (inside catch block)"
  - "Captured exception in a nullable var before the try/catch to allow raising after the block"
  - "ExportProcessorJob left unchanged as fallback for any residual PENDING records"
metrics:
  duration: "10 minutes"
  completed_date: "2026-03-15"
  tasks_completed: 2
  files_modified: 2
---

# Quick Task 7: Fix Data Export — Process Inline on Request

**One-liner:** Synchronous data export processing in requestExport() returning COMPLETED immediately, plus COMPLETED status included in findActiveByUser() for post-refresh visibility.

## What Was Done

### Task 1: Process export inline in requestExport() and include COMPLETED in active query

**DataExportServiceImpl.requestExport()** was updated to process the export synchronously instead of leaving it in PENDING for the background job:

1. Insert record as PENDING (unchanged)
2. Immediately update to PROCESSING
3. Collect all ExportContributor sections via `contributor.export(uuid)`
4. Build JSON fileKey string using the same pattern as ExportProcessorJob
5. Compute completedAt and expiresAt (now + 7 days)
6. Update status to COMPLETED with fileKey and timestamps
7. On any exception: update to FAILED and raise UnexpectedError
8. Re-fetch record and return COMPLETED DataExportResponse with downloadUrl populated

**ExposedDataExportRepository.findActiveByUser()** WHERE clause extended to include COMPLETED status alongside PENDING and PROCESSING. This ensures that after a page refresh, the client can see the completed export and display the download button.

### Task 2: Tests pass and server compiles

- `./gradlew :server:privacy:impl:test` — all tests pass
- `./gradlew :server:compileKotlin` — full server compiles successfully

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] raise() unresolved in catch block with context parameters**
- **Found during:** Task 1 verification (compile error)
- **Issue:** `raise(UnexpectedError(...))` inside a `catch` block caused "Unresolved reference 'raise'" because Kotlin context parameters are not automatically available as callable functions within catch blocks
- **Fix:** Captured the exception in a nullable `var processingError: Exception?` outside the try/catch, then called `raise.raise(UnexpectedError(...))` after the try/catch block (using the explicit context parameter reference)
- **Files modified:** DataExportServiceImpl.kt
- **Commit:** b0f0969

## Self-Check: PASSED

- DataExportServiceImpl.kt exists and contains "COMPLETED" in requestExport()
- ExposedDataExportRepository.kt exists and contains COMPLETED in findActiveByUser() WHERE clause
- Commit b0f0969 exists
