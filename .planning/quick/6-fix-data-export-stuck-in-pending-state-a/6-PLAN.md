---
phase: quick-6
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportProcessorJob.kt
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/di/PrivacyModule.kt
  - server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/ExportRoutes.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
autonomous: true
requirements: [QUICK-6]

must_haves:
  truths:
    - "Requesting a data export transitions to COMPLETED status (not stuck in PENDING)"
    - "A download button appears when export status is COMPLETED"
    - "The download URL is populated in the DataExportResponse when status is COMPLETED"
    - "Stale PENDING records do not permanently block new export requests"
  artifacts:
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportProcessorJob.kt"
      provides: "Background job that processes PENDING exports"
      min_lines: 30
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt"
      provides: "Export service with downloadUrl in response"
      contains: "downloadUrl"
  key_links:
    - from: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportProcessorJob.kt"
      to: "DataExportRepository.updateStatus"
      via: "Updates PENDING -> PROCESSING -> COMPLETED with fileKey and expiresAt"
      pattern: "updateStatus.*COMPLETED"
    - from: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt"
      to: "DataExportResponse.downloadUrl"
      via: "toResponse includes downloadUrl when status is COMPLETED"
      pattern: "downloadUrl"
    - from: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt"
      to: "onDownloadExport"
      via: "Download button shown when COMPLETED (regardless of downloadUrl null check)"
      pattern: "ExportStatus.COMPLETED"
---

<objective>
Fix data export stuck in PENDING state forever and missing download button.

Purpose: The export feature creates a DB record with PENDING status but no background job or inline processing ever transitions it to COMPLETED. Additionally, the server never populates the `downloadUrl` field in the response, so even if status were COMPLETED, the client-side condition `status == COMPLETED && downloadUrl != null` would never show the download button.

Output: Working export flow where requesting an export processes data and completes, and the download button appears for completed exports.
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/STATE.md

<interfaces>
From server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/repository/DataExportRepository.kt:
```kotlin
interface DataExportRepository {
    suspend fun insert(userId: Uuid, status: String): Uuid
    suspend fun findById(id: Uuid): DataExportRecord?
    suspend fun findActiveByUser(userId: Uuid): DataExportRecord?
    suspend fun updateStatus(id: Uuid, status: String, fileKey: String? = null, completedAt: LocalDateTime? = null, expiresAt: LocalDateTime? = null): Boolean
    suspend fun findExpired(): List<DataExportRecord>
    suspend fun deleteByUser(userId: Uuid)
}
```

From server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/ExportContributor.kt:
```kotlin
interface ExportContributor {
    val sectionName: String
    suspend fun export(userId: Uuid): ExportSection
}
```

From server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/PrivacyJobScheduler.kt:
```kotlin
interface PrivacyJob {
    val name: String
    val interval: Duration get() = 24.hours
    suspend fun execute()
}
```

From core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt:
```kotlin
@Serializable
data class DataExportResponse(
    val id: String,
    val status: ExportStatus,
    val downloadUrl: String? = null,
    val createdAt: String,
    val expiresAt: String? = null,
)
```

From app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt (line 192):
```kotlin
if (state.exportStatus.status == ExportStatus.COMPLETED && state.exportStatus.downloadUrl != null) {
    TerminalButton(text = "Download", onClick = onDownloadExport, variant = ButtonVariant.Success)
}
```
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create ExportProcessorJob and fix DataExportServiceImpl to populate downloadUrl</name>
  <files>
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportProcessorJob.kt
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/di/PrivacyModule.kt
  </files>
  <action>
    **Root cause:** `requestExport()` creates a PENDING record but nothing processes it. There is no job like `DeletionExecutorJob` for exports.

    1. **Create `ExportProcessorJob`** at `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportProcessorJob.kt`:
       - Implements `PrivacyJob` with `name = "export-processor"` and `interval = 1.minutes` (shorter than default 24h so exports process quickly).
       - In `execute()`: query the repository for PENDING exports using a new `findPending()` method. For each:
         a. Update status to PROCESSING via `dataExportRepository.updateStatus(id, "PROCESSING")`
         b. Collect data from all `ExportContributor` instances (there may be none currently, that's fine - empty export is valid)
         c. Serialize collected sections to JSON string as the "export data"
         d. For now, since MinIO integration is placeholder, store the serialized JSON as the `fileKey` value (this is the export content itself, compact enough for DB storage until real object storage is wired)
         e. Update status to COMPLETED with `completedAt = Clock.System.now()` and `expiresAt = now + 7.days`
         f. Wrap in try/catch - on failure, update status to FAILED
       - Needs constructor params: `DataExportRepository`, `List<ExportContributor>`

    2. **Add `findPending()` to `DataExportRepository`** interface and `ExposedDataExportRepository`:
       - Returns `List<DataExportRecord>` where `status == "PENDING"`
       - In the Exposed implementation, query `DataExportRequestsTable.select().where { status eq "PENDING" }.toList().map { it.toDataExportRecord() }`

    3. **Fix `DataExportServiceImpl.toResponse()`** to populate `downloadUrl`:
       - When `status == "COMPLETED"`, set `downloadUrl = "/api/privacy/exports/${id}/download"` in the `DataExportResponse`
       - This is the same placeholder URL already returned by `getExportDownloadUrl()` method

    4. **Register ExportProcessorJob in `PrivacyModule.kt`**:
       - Add: `single<PrivacyJob>(qualifier = named("exportProcessorJob")) { ExportProcessorJob(get(), getAll()) }`
       - This ensures the `PrivacyJobScheduler` picks it up via `getAll<PrivacyJob>()`
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :server:privacy:impl:compileKotlin 2>&1 | tail -5</automated>
  </verify>
  <done>ExportProcessorJob exists and compiles, processes PENDING exports to COMPLETED. DataExportServiceImpl.toResponse() populates downloadUrl for COMPLETED exports. Job registered in DI.</done>
</task>

<task type="auto">
  <name>Task 2: Fix client download button visibility and add findPending to repository</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
    server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/repository/DataExportRepository.kt
    server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedDataExportRepository.kt
  </files>
  <action>
    1. **Fix download button condition in `PrivacySettingsScreen.kt`** (line 192):
       - Change from: `if (state.exportStatus.status == ExportStatus.COMPLETED && state.exportStatus.downloadUrl != null)`
       - Change to: `if (state.exportStatus.status == ExportStatus.COMPLETED)`
       - Rationale: The downloadUrl is now populated by the server when status is COMPLETED. But even if it weren't, the `DownloadExport` intent fetches the URL dynamically via `getExportDownloadUrl()`, so the button should always be shown for COMPLETED exports. The `downloadUrl` in the model is informational, not gating.

    2. **Add `findPending()` to `DataExportRepository` interface** (if not done in Task 1 due to file ownership):
       - Add: `suspend fun findPending(): List<DataExportRecord>`

    3. **Implement `findPending()` in `ExposedDataExportRepository`**:
       ```kotlin
       override suspend fun findPending(): List<DataExportRecord> =
           suspendTransaction(db = db) {
               DataExportRequestsTable
                   .select(DataExportRequestsTable.columns)
                   .where { DataExportRequestsTable.status eq "PENDING" }
                   .toList()
                   .map { it.toDataExportRecord() }
           }
       ```

    4. **Verify existing tests still pass** for the privacy module. The `PrivacySettingsViewModelTest` already has `sampleExport` with `status = COMPLETED` and `downloadUrl` set, so the download test should continue to work.
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:allTests 2>&1 | tail -10</automated>
  </verify>
  <done>Download button appears for any COMPLETED export. Repository has findPending() for the processor job. Client tests pass.</done>
</task>

<task type="auto">
  <name>Task 3: Verify full server compilation and run all privacy tests</name>
  <files></files>
  <action>
    1. Run full server compilation to ensure no breakage across the server privacy module (routes, service, repository, jobs, DI all compile together).
    2. Run server tests if any exist for privacy.
    3. Run client privacy tests to confirm no regressions.
    4. If there are stale PENDING export records in the database and the user has docker services running, note that the ExportProcessorJob will automatically clean them up on next server start (processes them to COMPLETED within 1 minute).
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :server:privacy:impl:compileKotlin :app:privacy:impl:allTests 2>&1 | tail -15</automated>
  </verify>
  <done>Server privacy module compiles cleanly. Client privacy tests pass. No regressions introduced.</done>
</task>

</tasks>

<verification>
- Server compiles: `./gradlew :server:privacy:impl:compileKotlin` succeeds
- Client tests pass: `./gradlew :app:privacy:impl:allTests` succeeds
- ExportProcessorJob.kt exists and implements PrivacyJob
- DataExportServiceImpl.toResponse() includes downloadUrl for COMPLETED status
- PrivacySettingsScreen download button no longer requires non-null downloadUrl
- ExportProcessorJob registered in PrivacyModule DI
</verification>

<success_criteria>
- Data exports transition from PENDING to COMPLETED via the ExportProcessorJob background job
- The download button is visible on the client when an export has COMPLETED status
- The downloadUrl field is populated in DataExportResponse for completed exports
- All existing tests continue to pass
</success_criteria>

<output>
After completion, create `.planning/quick/6-fix-data-export-stuck-in-pending-state-a/6-SUMMARY.md`
</output>
