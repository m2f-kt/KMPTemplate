---
phase: quick-7
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedDataExportRepository.kt
autonomous: true
requirements: [QUICK-7]

must_haves:
  truths:
    - "Requesting a data export returns a COMPLETED response immediately (no polling needed)"
    - "Refreshing the privacy screen after export shows the completed export with download URL"
    - "ExportProcessorJob still processes any PENDING records as a fallback"
  artifacts:
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt"
      provides: "Inline export processing in requestExport()"
      contains: "COMPLETED"
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedDataExportRepository.kt"
      provides: "Active query includes COMPLETED status"
      contains: "COMPLETED"
  key_links:
    - from: "DataExportServiceImpl.requestExport()"
      to: "ExportContributor.export()"
      via: "inline processing loop"
      pattern: "exportContributors\\.map"
    - from: "ExposedDataExportRepository.findActiveByUser()"
      to: "DataExportRequestsTable"
      via: "SQL query with COMPLETED status"
      pattern: "COMPLETED"
---

<objective>
Fix data export to process inline on request and return COMPLETED exports in the active query.

Purpose: Currently requesting an export creates a PENDING record that the background job processes after ~1 minute, but the client never polls -- so the user never sees completion. Additionally, `findActiveByUser()` excludes COMPLETED exports, so refreshing loses the completed export entirely.

Output: Export requests are processed synchronously and COMPLETED exports are visible via the active endpoint.
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt
@server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedDataExportRepository.kt
@server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportProcessorJob.kt
@server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/di/PrivacyModule.kt

<interfaces>
<!-- DataExportServiceImpl already has exportContributors injected -->
From DataExportServiceImpl constructor:
```kotlin
class DataExportServiceImpl(
    private val dataExportRepository: DataExportRepository,
    private val exportContributors: List<ExportContributor>,
) : DataExportService
```

From DataExportRepository:
```kotlin
suspend fun insert(userId: Uuid, status: String): Uuid
suspend fun findById(id: Uuid): DataExportRecord?
suspend fun findActiveByUser(userId: Uuid): DataExportRecord?
suspend fun updateStatus(id: Uuid, status: String, fileKey: String? = null, completedAt: LocalDateTime? = null, expiresAt: LocalDateTime? = null): Boolean
```

From ExportContributor:
```kotlin
interface ExportContributor {
    val sectionName: String
    suspend fun export(userId: Uuid): ExportSection
}
```

From ExportProcessorJob.execute() -- the inline processing pattern to replicate:
```kotlin
val sections = exportContributors.map { contributor -> contributor.export(export.userId) }
val fileKey = sections.joinToString(separator = ",", prefix = "{", postfix = "}") {
    section -> "\"${section.name}\":${section.jsonData}"
}
val now = Clock.System.now()
val completedAt = now.toLocalDateTime(TimeZone.UTC)
val expiresAt = (now + 7.days).toLocalDateTime(TimeZone.UTC)
dataExportRepository.updateStatus(id, "COMPLETED", fileKey, completedAt, expiresAt)
```
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: Process export inline in requestExport() and include COMPLETED in active query</name>
  <files>
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt,
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedDataExportRepository.kt
  </files>
  <action>
**Fix A -- DataExportServiceImpl.requestExport():**
After inserting the PENDING record, process the export inline (same pattern as ExportProcessorJob.execute()):
1. Update status to PROCESSING
2. Collect sections from all `exportContributors` via `contributor.export(uuid)`
3. Build the fileKey JSON string using `joinToString(separator=",", prefix="{", postfix="}")` with `"\"${section.name}\":${section.jsonData}"`
4. Compute `completedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)` and `expiresAt = (Clock.System.now() + 7.days).toLocalDateTime(TimeZone.UTC)`
5. Call `dataExportRepository.updateStatus(exportId, "COMPLETED", fileKey, completedAt, expiresAt)`
6. Re-fetch the record with `findById(exportId)` and return `.toResponse()`
7. Wrap the processing in try/catch -- on failure, update status to "FAILED" and re-raise the error as `UnexpectedError`

Add these imports to DataExportServiceImpl.kt:
- `kotlinx.datetime.TimeZone`
- `kotlinx.datetime.toLocalDateTime`
- `kotlin.time.Clock`
- `kotlin.time.Duration.Companion.days`
- `kotlin.time.ExperimentalTime`

Add `@file:OptIn(ExperimentalTime::class)` if not already present (check -- the file already has `@file:OptIn(ExperimentalUuidApi::class)`; combine them).

**Fix B -- ExposedDataExportRepository.findActiveByUser():**
Change the where clause to include COMPLETED status:
```kotlin
.where {
    (DataExportRequestsTable.userId eq userId) and
        ((DataExportRequestsTable.status eq "PENDING") or
            (DataExportRequestsTable.status eq "PROCESSING") or
            (DataExportRequestsTable.status eq "COMPLETED"))
}
```

This ensures that after processing completes (or after a page refresh), the client sees the COMPLETED export with its download URL.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :server:privacy:impl:compileKotlin 2>&1 | tail -5</automated>
  </verify>
  <done>
    - requestExport() processes the export inline and returns a COMPLETED DataExportResponse with downloadUrl populated
    - findActiveByUser() returns records with PENDING, PROCESSING, or COMPLETED status
    - Code compiles without errors
  </done>
</task>

<task type="auto">
  <name>Task 2: Verify server starts and run existing tests</name>
  <files></files>
  <action>
Run the server privacy module tests to confirm nothing is broken. Also do a quick compile of the full server to catch any transitive issues.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :server:privacy:impl:test :server:compileKotlin 2>&1 | tail -20</automated>
  </verify>
  <done>
    - All existing privacy module tests pass
    - Full server compiles successfully
  </done>
</task>

</tasks>

<verification>
1. `./gradlew :server:privacy:impl:compileKotlin` succeeds
2. `./gradlew :server:privacy:impl:test` passes all tests
3. `requestExport()` in DataExportServiceImpl processes inline (grep for "COMPLETED" and "exportContributors" in the method)
4. `findActiveByUser()` includes "COMPLETED" status in query (grep for three status values)
</verification>

<success_criteria>
- Data export request returns COMPLETED response immediately (no background job dependency)
- Active export query returns COMPLETED exports so UI can display download button after refresh
- ExportProcessorJob remains as fallback for edge cases (no changes needed)
- All existing tests pass
</success_criteria>

<output>
After completion, create `.planning/quick/7-fix-data-export-process-inline-on-reques/7-01-SUMMARY.md`
</output>
