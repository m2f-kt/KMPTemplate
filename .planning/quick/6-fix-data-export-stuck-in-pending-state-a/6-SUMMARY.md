---
phase: quick-6
plan: 01
subsystem: server/privacy + app/privacy
tags: [data-export, background-job, bug-fix]
dependency_graph:
  requires: []
  provides: [working-data-export-flow]
  affects: [server/privacy/impl, app/privacy/impl]
tech_stack:
  added: []
  patterns: [PrivacyJob background job pattern, ExportContributor composition]
key_files:
  created:
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportProcessorJob.kt
  modified:
    - server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/repository/DataExportRepository.kt
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedDataExportRepository.kt
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt
    - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/di/PrivacyModule.kt
    - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
decisions:
  - "Store export JSON directly as fileKey in DB (compact placeholder until real MinIO object storage is wired)"
  - "Run ExportProcessorJob every 1 minute so exports complete quickly after being requested"
  - "Show download button for any COMPLETED status; downloadUrl in model is informational, URL is fetched dynamically on demand"
metrics:
  duration: ~10 minutes
  completed: 2026-03-15T17:34:15Z
  tasks_completed: 3
  files_modified: 5
---

# Quick 6: Fix Data Export Stuck in PENDING State Summary

**One-liner:** ExportProcessorJob background job transitions PENDING exports to COMPLETED within 1 minute, with downloadUrl populated in responses and download button shown for all COMPLETED exports.

## What Was Done

### Root Cause

Two separate issues caused the export feature to appear broken:

1. **Server:** `requestExport()` created a DB record with `PENDING` status but no job ever processed it. Unlike `DeletionExecutorJob` for account deletions, no equivalent job existed for exports.
2. **Server:** `DataExportServiceImpl.toResponse()` never populated the `downloadUrl` field, so even if an export reached COMPLETED status, the response had `downloadUrl = null`.
3. **Client:** `PrivacySettingsScreen` showed the download button only when `status == COMPLETED && downloadUrl != null`, meaning the button would never appear even with a valid COMPLETED export.

### Changes

**Task 1: ExportProcessorJob + DataExportServiceImpl fix**

- Added `findPending()` to `DataExportRepository` interface and `ExposedDataExportRepository` implementation — returns all records with `status == "PENDING"`.
- Created `ExportProcessorJob` implementing `PrivacyJob` with `interval = 1.minutes`:
  - Queries `findPending()` on each run
  - Transitions each record PENDING -> PROCESSING (optimistic locking pattern)
  - Collects data from all `ExportContributor` instances (serialized to JSON object with section names as keys)
  - Updates to COMPLETED with `fileKey`, `completedAt = now`, `expiresAt = now + 7.days`
  - On failure: updates to FAILED so the record doesn't stay stuck in PROCESSING
- Fixed `DataExportServiceImpl.toResponse()` to set `downloadUrl = "/api/privacy/exports/$id/download"` when `status == COMPLETED`.
- Registered `ExportProcessorJob` in `PrivacyModule` via `single<PrivacyJob>(qualifier = named("exportProcessorJob"))`.

**Task 2: Client download button fix**

- Changed `PrivacySettingsScreen` download button condition from `status == COMPLETED && downloadUrl != null` to simply `status == COMPLETED`.
- The `DownloadExport` intent already calls `getExportDownloadUrl()` which fetches the URL dynamically from the server, so the `downloadUrl` field in the local model state is not the gating factor.

## Verification

- Server compiles: `./gradlew :server:privacy:impl:compileKotlin` — BUILD SUCCESSFUL
- Client tests: `./gradlew :app:privacy:impl:allTests` — BUILD SUCCESSFUL (all existing tests pass)

## Deviations from Plan

None — plan executed exactly as written.

## Self-Check: PASSED

- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportProcessorJob.kt` — created, verified
- `DataExportServiceImpl.toResponse()` contains `downloadUrl` — verified
- `PrivacySettingsScreen` shows download button without null check — verified
- `ExportProcessorJob` registered in `PrivacyModule` — verified
- Commits: c286168 (Task 1), f5209f2 (Task 2)
