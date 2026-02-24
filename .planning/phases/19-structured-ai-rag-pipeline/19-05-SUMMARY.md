---
phase: 19-structured-ai-rag-pipeline
plan: 05
subsystem: sdk, ui
tags: [client-sdk, documents-ui, mvi, multipart-upload, koin, compose, navigation]

requires:
  - phase: 19-03
    provides: DocumentRoutes server endpoints (upload, list, get, delete)
provides:
  - DocumentApi SDK interface with multipart upload support
  - FakeDocumentApiBuilder for test DSL
  - app:documents module with MVI ViewModel + DocumentsScreen
  - DocumentsRoute navigation wiring
affects: [19-06]

tech-stack:
  added: []
  patterns: [multipart form upload via submitFormWithBinaryData, MVI ViewModel for documents]

key-files:
  created:
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/DocumentApi.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/DocumentApiImpl.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeDocumentApiBuilder.kt
    - app/documents/build.gradle.kts
    - app/documents/src/commonMain/kotlin/com/m2f/template/app/documents/DocumentsIntent.kt
    - app/documents/src/commonMain/kotlin/com/m2f/template/app/documents/DocumentsModel.kt
    - app/documents/src/commonMain/kotlin/com/m2f/template/app/documents/DocumentsMutation.kt
    - app/documents/src/commonMain/kotlin/com/m2f/template/app/documents/DocumentsEvent.kt
    - app/documents/src/commonMain/kotlin/com/m2f/template/app/documents/DocumentsViewModel.kt
    - app/documents/src/commonMain/kotlin/com/m2f/template/app/documents/DocumentsScreen.kt
    - app/documents/src/commonMain/kotlin/com/m2f/template/app/documents/StringKeyResolver.kt
    - app/documents/src/commonMain/composeResources/values/strings.xml
  modified:
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt
    - core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt
    - core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt
    - settings.gradle.kts
    - composeApp/build.gradle.kts
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/Routes.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt
    - composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt

key-decisions:
  - "DocumentApiImpl uses submitFormWithBinaryData for multipart upload (matches server expectation)"
  - "FakeDocumentApiBuilder follows FakeFileApiBuilder DSL pattern for test consistency"
  - "DocumentsScreen is a stateless composable; file picker is platform-specific and deferred"
  - "showUploadSuccess state managed at navigation level via event collection"

patterns-established:
  - "Module-local StringKeyResolver for exhaustive localized error resolution"
  - "Multipart file upload via Ktor client submitFormWithBinaryData"

requirements-completed: [RAG-07, RAG-08]

duration: 15min
completed: 2026-02-24
---

# Plan 19-05: Client SDK + Documents UI Summary

**DocumentApi SDK with multipart upload, FakeDocumentApiBuilder, app:documents MVI module, DocumentsScreen, navigation wiring**

## Performance

- **Duration:** 15 min
- **Started:** 2026-02-24
- **Completed:** 2026-02-24
- **Tasks:** 2
- **Files modified:** 20 (12 created, 8 modified)

## Accomplishments
- DocumentApi interface with uploadDocument (multipart), listDocuments, getDocument, deleteDocument
- DocumentApiImpl with submitFormWithBinaryData for multipart file upload
- FakeDocumentApiBuilder DSL for test stubbing, integrated into FakeSdkBuilder
- Sdk facade delegates to DocumentApi
- SdkModule registers DocumentApi in Koin DI
- app:documents module with full MVI stack (Intent, Model, Mutation, Event, ViewModel)
- DocumentsScreen with TerminalTable, status badges, upload/delete actions
- DocumentsRoute + composable block in AppNavHost with event-driven upload success
- DocumentsViewModel registered in AppModule

## Task Commits

1. **Task 1 + Task 2: SDK + UI** - `320fe9e` (feat)

## Files Created/Modified
- `core/sdk/.../api/DocumentApi.kt` - SDK interface for document CRUD
- `core/sdk/.../api/DocumentApiImpl.kt` - Ktor client implementation with multipart upload
- `core/sdk/.../Sdk.kt` - Added DocumentApi delegation
- `core/sdk/.../di/SdkModule.kt` - Koin registration for DocumentApi
- `core/testing/.../fakes/FakeDocumentApiBuilder.kt` - Test fake builder DSL
- `core/testing/.../fakes/FakeSdkBuilder.kt` - Added document DSL block
- `app/documents/build.gradle.kts` - New KMP module config
- `app/documents/.../DocumentsIntent.kt` - LoadDocuments, UploadFile, DeleteDocument, RefreshDocuments
- `app/documents/.../DocumentsModel.kt` - isLoading, documents, isUploading, error, groupId
- `app/documents/.../DocumentsMutation.kt` - SetLoading, SetDocuments, SetUploading, AddDocument, RemoveDocument, SetError
- `app/documents/.../DocumentsEvent.kt` - UploadSuccess
- `app/documents/.../DocumentsViewModel.kt` - MVI ViewModel with SDK integration
- `app/documents/.../DocumentsScreen.kt` - Stateless composable with design system components
- `app/documents/.../StringKeyResolver.kt` - Module-local error string resolution
- `app/documents/.../strings.xml` - Localized string resources
- `settings.gradle.kts` - Added app:documents module
- `composeApp/build.gradle.kts` - Added app.documents dependency
- `composeApp/.../Routes.kt` - Added DocumentsRoute
- `composeApp/.../AppNavHost.kt` - Added composable<DocumentsRoute> block
- `composeApp/.../AppModule.kt` - Registered DocumentsViewModel

## Decisions Made
- File picker integration deferred (platform-specific); upload callback is a placeholder
- showUploadSuccess state managed at navigation composable level via mutableStateOf + event collection
- Module-local StringKeyResolver pattern ensures compile-time exhaustiveness for error localization

## Deviations from Plan
None -- plan executed as specified.

## Issues Encountered
None -- clean compilation on first attempt after prior session's SDK work.

## User Setup Required
None.

## Next Phase Readiness
- Full client-server pipeline complete: upload -> ingest -> embed -> store -> retrieve -> inject -> display
- Ready for integration tests in Plan 19-06

---
*Phase: 19-structured-ai-rag-pipeline*
*Completed: 2026-02-24*
