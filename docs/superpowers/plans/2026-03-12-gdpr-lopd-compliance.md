# GDPR/LOPD Compliance Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add GDPR/LOPD compliance features (consent management, data export, account deletion, processing restriction, legal documents) to the template app.

**Architecture:** Compliance-as-a-Module approach — new `server:privacy` (contract/impl/wire) and `app:privacy` (contract/impl/wire) modules. New `PrivacyApi` in SDK facade. Cross-cutting concerns: processing restriction middleware in `conduitAuth`, consent gate navigation guard in auth post-login flow.

**Tech Stack:** Ktor + Exposed R2DBC (server), Compose Multiplatform + MVI (client), Arrow Either, Koin DI, Kotest assertions, Pencil MCP for screen designs.

**Spec:** `docs/superpowers/specs/2026-03-12-gdpr-lopd-compliance-design.md`

---

## File Structure

### New Files

**Core Layer (shared DTOs, errors, routes, SDK):**
- `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/` — Privacy DTOs (ConsentStatus, GrantConsentRequest, etc.)
- `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt` — Add `Privacy` resource routes (modify)
- `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt` — Add `AppError.Privacy` sealed class (modify)
- `core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt` — Add privacy StringKeys (modify)
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt` — Privacy API interface
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt` — Privacy API implementation
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt` — Add PrivacyApi delegation (modify)
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakePrivacyApiBuilder.kt` — Fake for tests
- `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt` — Add privacy builder (modify)

**Server Module (`server:privacy`):**
- `server/privacy/build.gradle.kts`
- `server/privacy/contract/build.gradle.kts`
- `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/tables/` — ConsentRecordsTable, LegalDocumentsTable, DataExportRequestsTable, AccountDeletionRequestsTable
- `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/repository/` — Repository interfaces + Record types
- `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/` — Service interfaces
- `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/errors/` — Privacy DomainError types
- `server/privacy/impl/build.gradle.kts`
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/Privacy.kt` — Migration registration
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/` — Repository implementations
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/` — Service implementations
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/` — Route definitions
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/di/` — Koin module
- `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/` — Scheduled jobs
- `server/privacy/wire/build.gradle.kts`
- `server/privacy/wire/src/main/kotlin/com/m2f/server/privacy/wire/PrivacyWireModule.kt`

**Client Module (`app:privacy`):**
- `app/privacy/contract/build.gradle.kts`
- `app/privacy/contract/src/commonMain/kotlin/com/m2f/template/app/privacy/contract/PrivacyRoutes.kt`
- `app/privacy/impl/build.gradle.kts`
- `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/` — MVI types + ViewModels + Screens
- `app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/` — ViewModel tests
- `app/privacy/wire/build.gradle.kts`
- `app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/` — Koin module + navigation

### Modified Files

- `settings.gradle.kts` — Add privacy module includes
- `server/src/main/kotlin/com/m2f/template/Application.kt` — Register privacy migrations + routes
- `server/src/main/kotlin/com/m2f/template/di/ServerModule.kt` — Include privacyWireModule
- `composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt` — Include privacyModule
- `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt` — Add privacyEntries
- `server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/tables/UsersTable.kt` — Add processingRestricted column
- `server/auth/impl/src/main/kotlin/com/m2f/server/auth/Auth.kt` — Add migration for processingRestricted
- `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt` — Wire PrivacyApi into Sdk
- `server/build.gradle.kts` — Add server:privacy:wire dependency
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt` — Add `conflict()` helper
- `server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/repository/UserRepository.kt` — Add processingRestricted to UserRecord + update method
- `server/auth/impl/src/main/kotlin/com/m2f/server/auth/repository/ExposedUserRepository.kt` — Read/write processingRestricted column
- `app/auth/wire/build.gradle.kts` — Add dependency on app:privacy:contract (for ConsentGateRoute)

---

## Chunk 1: Core DTOs, Errors, Routes, and SDK API

This chunk establishes the shared data types and SDK interface that both server and client depend on. No server or client module code yet — just the contract layer.

### Task 1: Privacy DTOs

**Files:**
- Create: `core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt`

- [ ] **Step 1: Create the privacy DTOs file**

```kotlin
package com.m2f.template.models.dto.privacy

import kotlinx.serialization.Serializable

@Serializable
enum class ConsentType {
    PRIVACY_POLICY,
    TERMS_OF_SERVICE,
    MARKETING,
    ANALYTICS,
}

@Serializable
data class ConsentStatus(
    val type: ConsentType,
    val granted: Boolean,
    val grantedAt: String?,
    val documentVersion: String?,
)

@Serializable
data class GrantConsentRequest(
    val type: ConsentType,
    val documentVersion: String,
)

@Serializable
data class RequiredConsent(
    val type: ConsentType,
    val currentVersion: String,
    val acceptedVersion: String?,
    val needsUpdate: Boolean,
)

@Serializable
data class RequiredConsentsResponse(
    val consents: List<RequiredConsent>,
    val hasOutdated: Boolean,
)

@Serializable
data class LegalDocumentResponse(
    val type: ConsentType,
    val version: String,
    val locale: String,
    val content: String,
    val publishedAt: String,
)

@Serializable
enum class ExportStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    EXPIRED,
}

@Serializable
data class DataExportResponse(
    val id: String,
    val status: ExportStatus,
    val downloadUrl: String? = null,
    val createdAt: String,
    val expiresAt: String? = null,
)

@Serializable
data class DeletionRequest(
    val password: String,
    val reason: String? = null,
)

@Serializable
enum class DeletionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
}

@Serializable
data class DeletionResponse(
    val id: String,
    val status: DeletionStatus,
    val scheduledAt: String,
    val completedAt: String? = null,
)
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core:models:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt
git commit -m "feat(privacy): add privacy DTOs for consent, export, and deletion"
```

### Task 2: AppError.Privacy and StringKeys

**Files:**
- Modify: `core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt`
- Modify: `core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt`

- [ ] **Step 1: Add AppError.Privacy sealed class**

In `AppError.kt`, after the `File` sealed class (line ~271), add:

```kotlin
    @Serializable
    sealed class Privacy : AppError() {
        @Serializable
        data class ProcessingRestricted(
            override val code: String = "PRIVACY_PROCESSING_RESTRICTED",
            override val message: String = "Your data processing is currently restricted"
        ) : Privacy()

        @Serializable
        data class ConsentRequired(
            override val code: String = "PRIVACY_CONSENT_REQUIRED",
            override val message: String = "You must accept the required privacy policies"
        ) : Privacy()

        @Serializable
        data class DeletionPending(
            override val code: String = "PRIVACY_DELETION_PENDING",
            override val message: String = "Your account is scheduled for deletion"
        ) : Privacy()

        @Serializable
        data class ExportNotReady(
            override val code: String = "PRIVACY_EXPORT_NOT_READY",
            override val message: String = "Data export is not ready for download"
        ) : Privacy()
    }
```

- [ ] **Step 2: Add privacy StringKeys**

In `StringKey.kt`, before the `GENERIC_ERROR` entry, add:

```kotlin
    // Privacy errors
    PRIVACY_PROCESSING_RESTRICTED("PRIVACY_PROCESSING_RESTRICTED"),
    PRIVACY_CONSENT_REQUIRED("PRIVACY_CONSENT_REQUIRED"),
    PRIVACY_DELETION_PENDING("PRIVACY_DELETION_PENDING"),
    PRIVACY_EXPORT_NOT_READY("PRIVACY_EXPORT_NOT_READY"),
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :core:models:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt core/models/src/commonMain/kotlin/com/m2f/template/models/localization/StringKey.kt
git commit -m "feat(privacy): add AppError.Privacy sealed class and StringKeys"
```

### Task 3: Privacy @Resource Routes

**Files:**
- Modify: `core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt`

- [ ] **Step 1: Add Privacy resource routes**

At the end of `ApiRoutes.kt`, add:

Note: The existing codebase uses `post` for all mutating operations (including deletes like `deleteGroup`). Follow this pattern — no `client.delete()` from the resources plugin. Each `@Resource` class must have a **unique path** to avoid Ktor ambiguity.

```kotlin
@Serializable
@Resource("/api/privacy")
class Privacy {
    @Serializable @Resource("consent/list")
    class GetConsents(val parent: Privacy = Privacy())

    @Serializable @Resource("consent/grant")
    class GrantConsent(val parent: Privacy = Privacy())

    @Serializable @Resource("consent/{type}/withdraw")
    class WithdrawConsent(val parent: Privacy = Privacy(), val type: String)

    @Serializable @Resource("consent/required")
    class RequiredConsents(val parent: Privacy = Privacy())

    @Serializable @Resource("legal/{type}")
    class LegalDocument(val parent: Privacy = Privacy(), val type: String, val locale: String? = null)

    @Serializable @Resource("legal/{type}/versions")
    class LegalDocumentVersions(val parent: Privacy = Privacy(), val type: String)

    @Serializable @Resource("export/request")
    class RequestExport(val parent: Privacy = Privacy())

    @Serializable @Resource("export/{id}")
    class ExportStatus(val parent: Privacy = Privacy(), val id: String)

    @Serializable @Resource("export/{id}/download")
    class ExportDownload(val parent: Privacy = Privacy(), val id: String)

    @Serializable @Resource("deletion/request")
    class RequestDeletion(val parent: Privacy = Privacy())

    @Serializable @Resource("deletion/status")
    class GetDeletionStatus(val parent: Privacy = Privacy())

    @Serializable @Resource("deletion/cancel")
    class CancelDeletion(val parent: Privacy = Privacy())

    @Serializable @Resource("restrict/enable")
    class RestrictProcessing(val parent: Privacy = Privacy())

    @Serializable @Resource("restrict/disable")
    class LiftRestriction(val parent: Privacy = Privacy())
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core:models:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt
git commit -m "feat(privacy): add @Resource routes for privacy API endpoints"
```

### Task 4: PrivacyApi Interface

**Files:**
- Create: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt`

- [ ] **Step 1: Create the PrivacyApi interface**

```kotlin
package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.DataExportResponse
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.dto.privacy.LegalDocumentResponse
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse

interface PrivacyApi {
    suspend fun getActiveConsents(): Either<AppError, List<ConsentStatus>>
    suspend fun grantConsent(request: GrantConsentRequest): Either<AppError, Unit>
    suspend fun withdrawConsent(type: ConsentType): Either<AppError, Unit>
    suspend fun getRequiredConsents(): Either<AppError, RequiredConsentsResponse>
    suspend fun getLegalDocument(type: ConsentType, locale: String? = null): Either<AppError, LegalDocumentResponse>
    suspend fun requestDataExport(): Either<AppError, DataExportResponse>
    suspend fun getExportStatus(id: String): Either<AppError, DataExportResponse>
    suspend fun getExportDownloadUrl(id: String): Either<AppError, String>
    suspend fun requestAccountDeletion(request: DeletionRequest): Either<AppError, DeletionResponse>
    suspend fun getDeletionStatus(): Either<AppError, DeletionResponse?>
    suspend fun cancelDeletion(): Either<AppError, Unit>
    suspend fun restrictProcessing(): Either<AppError, Unit>
    suspend fun liftRestriction(): Either<AppError, Unit>
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core:sdk:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt
git commit -m "feat(privacy): add PrivacyApi interface to SDK"
```

### Task 5: PrivacyApiImpl

**Files:**
- Create: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt`

Reference the existing `AuthApiImpl` pattern for `apiCall()` usage.

- [ ] **Step 1: Create the PrivacyApiImpl**

```kotlin
package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.DataExportResponse
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.dto.privacy.LegalDocumentResponse
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse
import com.m2f.template.models.routes.Privacy
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PrivacyApiImpl(
    private val client: HttpClient,
) : PrivacyApi {

    override suspend fun getActiveConsents(): Either<AppError, List<ConsentStatus>> =
        apiCall {
            client.get(Privacy.GetConsents())
        }

    override suspend fun grantConsent(request: GrantConsentRequest): Either<AppError, Unit> =
        apiCall {
            client.post(Privacy.GrantConsent()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun withdrawConsent(type: ConsentType): Either<AppError, Unit> =
        apiCall {
            client.post(Privacy.WithdrawConsent(type = type.name))
        }

    override suspend fun getRequiredConsents(): Either<AppError, RequiredConsentsResponse> =
        apiCall {
            client.get(Privacy.RequiredConsents())
        }

    override suspend fun getLegalDocument(type: ConsentType, locale: String?): Either<AppError, LegalDocumentResponse> =
        apiCall {
            client.get(Privacy.LegalDocument(type = type.name, locale = locale))
        }

    override suspend fun requestDataExport(): Either<AppError, DataExportResponse> =
        apiCall {
            client.post(Privacy.RequestExport())
        }

    override suspend fun getExportStatus(id: String): Either<AppError, DataExportResponse> =
        apiCall {
            client.get(Privacy.ExportStatus(id = id))
        }

    override suspend fun getExportDownloadUrl(id: String): Either<AppError, String> =
        apiCall {
            client.get(Privacy.ExportDownload(id = id))
        }

    override suspend fun requestAccountDeletion(request: DeletionRequest): Either<AppError, DeletionResponse> =
        apiCall {
            client.post(Privacy.RequestDeletion()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun getDeletionStatus(): Either<AppError, DeletionResponse?> =
        apiCall {
            client.get(Privacy.GetDeletionStatus())
        }

    override suspend fun cancelDeletion(): Either<AppError, Unit> =
        apiCall {
            client.post(Privacy.CancelDeletion())
        }

    override suspend fun restrictProcessing(): Either<AppError, Unit> =
        apiCall {
            client.post(Privacy.RestrictProcessing())
        }

    override suspend fun liftRestriction(): Either<AppError, Unit> =
        apiCall {
            client.post(Privacy.LiftRestriction())
        }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core:sdk:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApiImpl.kt
git commit -m "feat(privacy): add PrivacyApiImpl with apiCall() wrapper"
```

### Task 6: Wire PrivacyApi into Sdk Facade

**Files:**
- Modify: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt`
- Modify: `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt`

- [ ] **Step 1: Add PrivacyApi to Sdk facade**

Update `Sdk.kt` to add PrivacyApi delegation:

```kotlin
package com.m2f.template.sdk

import com.m2f.template.sdk.api.AuthApi
import com.m2f.template.sdk.api.DocumentApi
import com.m2f.template.sdk.api.FileApi
import com.m2f.template.sdk.api.GroupApi
import com.m2f.template.sdk.api.InvitationApi
import com.m2f.template.sdk.api.PrivacyApi
import com.m2f.template.sdk.api.UserApi

class Sdk(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val groupApi: GroupApi,
    private val fileApi: FileApi,
    private val invitationApi: InvitationApi,
    private val documentApi: DocumentApi,
    private val privacyApi: PrivacyApi,
) : AuthApi by authApi, UserApi by userApi, GroupApi by groupApi, FileApi by fileApi, InvitationApi by invitationApi, DocumentApi by documentApi, PrivacyApi by privacyApi
```

- [ ] **Step 2: Update SdkModule to wire PrivacyApiImpl**

In `core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/di/SdkModule.kt`, add PrivacyApiImpl construction and pass it to Sdk constructor. The exact wiring follows the existing pattern — check how other APIs like DocumentApiImpl are wired and follow the same pattern for PrivacyApiImpl.

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :shared:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/Sdk.kt shared/src/commonMain/kotlin/com/m2f/template/di/SdkModule.kt
git commit -m "feat(privacy): wire PrivacyApi into Sdk facade and DI"
```

### Task 7: FakePrivacyApiBuilder for Tests

**Files:**
- Create: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakePrivacyApiBuilder.kt`
- Modify: `core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt`

- [ ] **Step 1: Create FakePrivacyApiBuilder**

Follow the exact pattern from `FakeAuthApiBuilder.kt` — each method defaults to `Either.Left(AppError.Client.Unknown())`.

```kotlin
package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.DataExportResponse
import com.m2f.template.models.dto.privacy.DeletionRequest
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.dto.privacy.LegalDocumentResponse
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse
import com.m2f.template.sdk.api.PrivacyApi

@FakeSDKDsl
class FakePrivacyApiBuilder {

    private var getActiveConsents: suspend () -> Either<AppError, List<ConsentStatus>> =
        { Either.Left(AppError.Client.Unknown()) }
    private var grantConsent: suspend (GrantConsentRequest) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }
    private var withdrawConsent: suspend (ConsentType) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }
    private var getRequiredConsents: suspend () -> Either<AppError, RequiredConsentsResponse> =
        { Either.Left(AppError.Client.Unknown()) }
    private var getLegalDocument: suspend (ConsentType, String?) -> Either<AppError, LegalDocumentResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }
    private var requestDataExport: suspend () -> Either<AppError, DataExportResponse> =
        { Either.Left(AppError.Client.Unknown()) }
    private var getExportStatus: suspend (String) -> Either<AppError, DataExportResponse> =
        { Either.Left(AppError.Client.Unknown()) }
    private var getExportDownloadUrl: suspend (String) -> Either<AppError, String> =
        { Either.Left(AppError.Client.Unknown()) }
    private var requestAccountDeletion: suspend (DeletionRequest) -> Either<AppError, DeletionResponse> =
        { Either.Left(AppError.Client.Unknown()) }
    private var getDeletionStatus: suspend () -> Either<AppError, DeletionResponse?> =
        { Either.Left(AppError.Client.Unknown()) }
    private var cancelDeletion: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }
    private var restrictProcessing: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }
    private var liftRestriction: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    fun getActiveConsents(block: suspend () -> Either<AppError, List<ConsentStatus>>) {
        getActiveConsents = block
    }

    fun grantConsent(block: suspend (GrantConsentRequest) -> Either<AppError, Unit>) {
        grantConsent = block
    }

    fun withdrawConsent(block: suspend (ConsentType) -> Either<AppError, Unit>) {
        withdrawConsent = block
    }

    fun getRequiredConsents(block: suspend () -> Either<AppError, RequiredConsentsResponse>) {
        getRequiredConsents = block
    }

    fun getLegalDocument(block: suspend (ConsentType, String?) -> Either<AppError, LegalDocumentResponse>) {
        getLegalDocument = block
    }

    fun requestDataExport(block: suspend () -> Either<AppError, DataExportResponse>) {
        requestDataExport = block
    }

    fun getExportStatus(block: suspend (String) -> Either<AppError, DataExportResponse>) {
        getExportStatus = block
    }

    fun getExportDownloadUrl(block: suspend (String) -> Either<AppError, String>) {
        getExportDownloadUrl = block
    }

    fun requestAccountDeletion(block: suspend (DeletionRequest) -> Either<AppError, DeletionResponse>) {
        requestAccountDeletion = block
    }

    fun getDeletionStatus(block: suspend () -> Either<AppError, DeletionResponse?>) {
        getDeletionStatus = block
    }

    fun cancelDeletion(block: suspend () -> Either<AppError, Unit>) {
        cancelDeletion = block
    }

    fun restrictProcessing(block: suspend () -> Either<AppError, Unit>) {
        restrictProcessing = block
    }

    fun liftRestriction(block: suspend () -> Either<AppError, Unit>) {
        liftRestriction = block
    }

    internal fun build(): PrivacyApi = object : PrivacyApi {
        override suspend fun getActiveConsents() = this@FakePrivacyApiBuilder.getActiveConsents()
        override suspend fun grantConsent(request: GrantConsentRequest) = this@FakePrivacyApiBuilder.grantConsent(request)
        override suspend fun withdrawConsent(type: ConsentType) = this@FakePrivacyApiBuilder.withdrawConsent(type)
        override suspend fun getRequiredConsents() = this@FakePrivacyApiBuilder.getRequiredConsents()
        override suspend fun getLegalDocument(type: ConsentType, locale: String?) = this@FakePrivacyApiBuilder.getLegalDocument(type, locale)
        override suspend fun requestDataExport() = this@FakePrivacyApiBuilder.requestDataExport()
        override suspend fun getExportStatus(id: String) = this@FakePrivacyApiBuilder.getExportStatus(id)
        override suspend fun getExportDownloadUrl(id: String) = this@FakePrivacyApiBuilder.getExportDownloadUrl(id)
        override suspend fun requestAccountDeletion(request: DeletionRequest) = this@FakePrivacyApiBuilder.requestAccountDeletion(request)
        override suspend fun getDeletionStatus() = this@FakePrivacyApiBuilder.getDeletionStatus()
        override suspend fun cancelDeletion() = this@FakePrivacyApiBuilder.cancelDeletion()
        override suspend fun restrictProcessing() = this@FakePrivacyApiBuilder.restrictProcessing()
        override suspend fun liftRestriction() = this@FakePrivacyApiBuilder.liftRestriction()
    }
}
```

- [ ] **Step 2: Update FakeSdkBuilder to include privacy**

In `FakeSdkBuilder.kt`, add:
- Field: `private var privacyApiBuilder: FakePrivacyApiBuilder = FakePrivacyApiBuilder()`
- Method: `fun privacy(init: FakePrivacyApiBuilder.() -> Unit) { privacyApiBuilder.init() }`
- In `build()`: add `privacyApi = privacyApiBuilder.build()` to Sdk constructor

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :core:testing:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakePrivacyApiBuilder.kt core/testing/src/commonMain/kotlin/com/m2f/template/core/testing/fakes/FakeSdkBuilder.kt
git commit -m "feat(privacy): add FakePrivacyApiBuilder and wire into FakeSdkBuilder"
```

---

## Chunk 2: Server Privacy Module — Tables, Repositories, Migrations

This chunk creates the `server:privacy` module structure with database tables, repository layer, and migrations. No services or routes yet.

### Task 8: Gradle Module Setup

**Files:**
- Modify: `settings.gradle.kts`
- Create: `server/privacy/build.gradle.kts`
- Create: `server/privacy/contract/build.gradle.kts`
- Create: `server/privacy/impl/build.gradle.kts`
- Create: `server/privacy/wire/build.gradle.kts`

- [ ] **Step 1: Add module includes to settings.gradle.kts**

After the `server:ai:wire` line, add:

```kotlin
include("server:privacy")
include("server:privacy:contract")
include("server:privacy:impl")
include("server:privacy:wire")
```

**Note:** Do NOT add `app:privacy:*` includes yet — those are added in Chunk 4 (Task 19) when the client module is scaffolded.

- [ ] **Step 2: Create server/privacy/build.gradle.kts**

```kotlin
// Parent module - no sources. See contract/, impl/, wire/ submodules.
```

- [ ] **Step 3: Create server/privacy/contract/build.gradle.kts**

```kotlin
plugins {
    id("server-module-convention")
}

group = "com.m2f.server.privacy"

dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.auth.contract)  // For UsersTable FK references
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.exposed.core)
    implementation(libs.exposed.date.time)
}
```

- [ ] **Step 4: Create server/privacy/impl/build.gradle.kts**

```kotlin
plugins {
    id("server-module-convention")
}

group = "com.m2f.server.privacy"

dependencies {
    implementation(projects.server.privacy.contract)
    implementation(projects.server.auth.contract)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.bundles.di)
    implementation(libs.exposed.core)
    implementation(libs.exposed.date.time)
    testImplementation(libs.bundles.testing.server)
}
```

- [ ] **Step 5: Create server/privacy/wire/build.gradle.kts**

```kotlin
plugins {
    id("server-module-convention")
}

group = "com.m2f.server.privacy"

dependencies {
    api(projects.server.privacy.contract)
    api(projects.server.privacy.impl)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(libs.bundles.di)
    implementation(libs.bundles.fp)
}
```

- [ ] **Step 6: Verify Gradle sync succeeds**

Run: `./gradlew :server:privacy:contract:dependencies --configuration compileClasspath`
Expected: BUILD SUCCESSFUL (or at least resolves dependencies)

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts server/privacy/build.gradle.kts server/privacy/contract/build.gradle.kts server/privacy/impl/build.gradle.kts server/privacy/wire/build.gradle.kts
git commit -m "feat(privacy): scaffold server:privacy module with contract/impl/wire"
```

### Task 9: Database Tables (Contract)

**Files:**
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/tables/ConsentRecordsTable.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/tables/LegalDocumentsTable.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/tables/DataExportRequestsTable.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/tables/AccountDeletionRequestsTable.kt`

- [ ] **Step 1: Create ConsentRecordsTable**

```kotlin
@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.tables

import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

object ConsentRecordsTable : Table("consent_records") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val consentType = varchar("consent_type", 50)
    val granted = bool("granted")
    val legalDocumentVersion = varchar("legal_document_version", 20)
    val ipAddress = varchar("ip_address", 45).nullable()
    val userAgent = text("user_agent").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
```

- [ ] **Step 2: Create LegalDocumentsTable**

```kotlin
package com.m2f.server.privacy.contract.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object LegalDocumentsTable : Table("legal_documents") {
    val id = uuid("id").autoGenerate()
    val type = varchar("type", 50)
    val version = varchar("version", 20)
    val locale = varchar("locale", 5)
    val content = text("content")
    val publishedAt = datetime("published_at").defaultExpression(CurrentDateTime)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
```

- [ ] **Step 3: Create DataExportRequestsTable**

```kotlin
@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.tables

import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

object DataExportRequestsTable : Table("data_export_requests") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val status = varchar("status", 20)
    val fileKey = varchar("file_key", 512).nullable()
    val completedAt = datetime("completed_at").nullable()
    val expiresAt = datetime("expires_at").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
```

- [ ] **Step 4: Create AccountDeletionRequestsTable**

```kotlin
@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.tables

import com.m2f.server.auth.contract.tables.UsersTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi

object AccountDeletionRequestsTable : Table("account_deletion_requests") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(UsersTable.id)
    val status = varchar("status", 20)
    val reason = text("reason").nullable()
    val scheduledAt = datetime("scheduled_at")
    val completedAt = datetime("completed_at").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
```

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :server:privacy:contract:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/privacy/contract/src/
git commit -m "feat(privacy): add Exposed table definitions for privacy module"
```

### Task 10: Repository Interfaces and Records (Contract)

**Files:**
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/repository/ConsentRepository.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/repository/LegalDocumentRepository.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/repository/DataExportRepository.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/repository/AccountDeletionRepository.kt`

- [ ] **Step 1: Create ConsentRepository**

```kotlin
@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ConsentRecord(
    val id: Uuid,
    val userId: Uuid,
    val consentType: String,
    val granted: Boolean,
    val legalDocumentVersion: String,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: LocalDateTime,
)

interface ConsentRepository {
    suspend fun insert(
        userId: Uuid,
        consentType: String,
        granted: Boolean,
        legalDocumentVersion: String,
        ipAddress: String?,
        userAgent: String?,
    ): Uuid

    suspend fun findLatestByUserAndType(userId: Uuid, consentType: String): ConsentRecord?
    suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord>
    suspend fun findAllByUser(userId: Uuid): List<ConsentRecord>
    suspend fun anonymizeByUser(userId: Uuid, anonymizedId: Uuid)
}
```

- [ ] **Step 2: Create LegalDocumentRepository**

```kotlin
@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class LegalDocumentRecord(
    val id: Uuid,
    val type: String,
    val version: String,
    val locale: String,
    val content: String,
    val publishedAt: LocalDateTime,
)

interface LegalDocumentRepository {
    suspend fun findCurrentByTypeAndLocale(type: String, locale: String): LegalDocumentRecord?
    suspend fun findAllVersionsByType(type: String): List<LegalDocumentRecord>
    suspend fun insert(type: String, version: String, locale: String, content: String): Uuid
}
```

- [ ] **Step 3: Create DataExportRepository**

```kotlin
@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class DataExportRecord(
    val id: Uuid,
    val userId: Uuid,
    val status: String,
    val fileKey: String?,
    val completedAt: LocalDateTime?,
    val expiresAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

interface DataExportRepository {
    suspend fun insert(userId: Uuid, status: String): Uuid
    suspend fun findById(id: Uuid): DataExportRecord?
    suspend fun findActiveByUser(userId: Uuid): DataExportRecord?
    suspend fun updateStatus(id: Uuid, status: String, fileKey: String? = null, completedAt: LocalDateTime? = null, expiresAt: LocalDateTime? = null): Boolean
    suspend fun findExpired(): List<DataExportRecord>
    suspend fun deleteByUser(userId: Uuid)
}
```

- [ ] **Step 4: Create AccountDeletionRepository**

```kotlin
@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.contract.repository

import kotlinx.datetime.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AccountDeletionRecord(
    val id: Uuid,
    val userId: Uuid,
    val status: String,
    val reason: String?,
    val scheduledAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
)

interface AccountDeletionRepository {
    suspend fun insert(userId: Uuid, reason: String?, scheduledAt: LocalDateTime): Uuid
    suspend fun findPendingByUser(userId: Uuid): AccountDeletionRecord?
    suspend fun findById(id: Uuid): AccountDeletionRecord?
    suspend fun updateStatus(id: Uuid, status: String, completedAt: LocalDateTime? = null): Boolean
    suspend fun findDueForExecution(): List<AccountDeletionRecord>
    suspend fun cancelByUser(userId: Uuid): Boolean
}
```

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :server:privacy:contract:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/privacy/contract/src/
git commit -m "feat(privacy): add repository interfaces and record types"
```

### Task 11: DomainError Types (Contract)

**Files:**
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/errors/PrivacyErrors.kt`

- [ ] **Step 1: Create privacy domain errors**

Follow the pattern from `server/auth/contract/errors/AuthErrors.kt`:

```kotlin
package com.m2f.server.privacy.contract.errors

import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.preferredLanguage
import com.m2f.core.config.server.forbidden
import com.m2f.core.config.server.conflict
import com.m2f.core.config.server.notFound
import com.m2f.core.config.server.ServerStrings
import com.m2f.template.models.AppError
import io.ktor.server.routing.RoutingContext

data object ProcessingRestricted : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.ProcessingRestricted()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.forbidden(error.code, message)
    }
}

data object ConsentRequired : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.ConsentRequired()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.forbidden(error.code, message)
    }
}

data object DeletionAlreadyPending : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.DeletionPending()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.conflict(error.code, message)
    }
}

data object ExportNotReady : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.ExportNotReady()

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.notFound(error.code, message)
    }
}

data object ExportAlreadyActive : DomainError {
    override fun toAppError(): AppError = AppError.Privacy.ExportNotReady(
        message = "An export is already in progress"
    )

    context(routingContext: RoutingContext)
    override suspend fun respond() {
        val error = toAppError()
        val locale = routingContext.preferredLanguage()
        val message = ServerStrings.resolve(error.code, locale)
        routingContext.conflict(error.code, message)
    }
}
```

**IMPORTANT:** The `conflict()` helper does NOT exist in `Error.kt` yet. The existing helpers are: `unexpected`, `unprocessable`, `unauthorized`, `forbidden`, `notFound`, `payloadTooLarge`, `unsupportedMediaType`, `gone`. You must first add a `conflict()` helper to `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt` following the same pattern as `forbidden()` but using `HttpStatusCode.Conflict` (409). Alternatively, use `unprocessable()` for the conflict errors instead. Check existing `DomainError` implementations for the exact `respond()` pattern.

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :server:privacy:contract:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/privacy/contract/src/
git commit -m "feat(privacy): add privacy domain error types"
```

### Task 12: Repository Implementations (Impl)

**Files:**
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedConsentRepository.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedLegalDocumentRepository.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedDataExportRepository.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedAccountDeletionRepository.kt`

- [ ] **Step 1: Create ExposedConsentRepository**

Follow the `ExposedUserRepository` pattern exactly — `suspendTransaction(db = db)`, `ResultRow.toRecord()` extension.

```kotlin
@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.privacy.repository

import com.m2f.server.privacy.contract.repository.ConsentRecord
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.server.privacy.contract.tables.ConsentRecordsTable
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ExposedConsentRepository(private val db: R2dbcDatabase) : ConsentRepository {

    override suspend fun insert(
        userId: Uuid,
        consentType: String,
        granted: Boolean,
        legalDocumentVersion: String,
        ipAddress: String?,
        userAgent: String?,
    ): Uuid = suspendTransaction(db = db) {
        ConsentRecordsTable.insert {
            it[ConsentRecordsTable.userId] = userId
            it[ConsentRecordsTable.consentType] = consentType
            it[ConsentRecordsTable.granted] = granted
            it[ConsentRecordsTable.legalDocumentVersion] = legalDocumentVersion
            it[ConsentRecordsTable.ipAddress] = ipAddress
            it[ConsentRecordsTable.userAgent] = userAgent
        }[ConsentRecordsTable.id]
    }

    override suspend fun findLatestByUserAndType(userId: Uuid, consentType: String): ConsentRecord? =
        suspendTransaction(db = db) {
            ConsentRecordsTable
                .select(ConsentRecordsTable.columns)
                .where { (ConsentRecordsTable.userId eq userId) and (ConsentRecordsTable.consentType eq consentType) }
                .orderBy(ConsentRecordsTable.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.toConsentRecord()
        }

    override suspend fun findAllActiveByUser(userId: Uuid): List<ConsentRecord> =
        suspendTransaction(db = db) {
            // Get latest record per consent type for this user
            // Simple approach: get all, group by type, take latest
            ConsentRecordsTable
                .select(ConsentRecordsTable.columns)
                .where { ConsentRecordsTable.userId eq userId }
                .orderBy(ConsentRecordsTable.createdAt, SortOrder.DESC)
                .toList()
                .map { it.toConsentRecord() }
                .distinctBy { it.consentType }
        }

    override suspend fun findAllByUser(userId: Uuid): List<ConsentRecord> =
        suspendTransaction(db = db) {
            ConsentRecordsTable
                .select(ConsentRecordsTable.columns)
                .where { ConsentRecordsTable.userId eq userId }
                .orderBy(ConsentRecordsTable.createdAt, SortOrder.ASC)
                .toList()
                .map { it.toConsentRecord() }
        }

    override suspend fun anonymizeByUser(userId: Uuid, anonymizedId: Uuid) {
        suspendTransaction(db = db) {
            ConsentRecordsTable.update({ ConsentRecordsTable.userId eq userId }) {
                it[ConsentRecordsTable.userId] = anonymizedId
                it[ConsentRecordsTable.ipAddress] = null
                it[ConsentRecordsTable.userAgent] = null
            }
        }
    }
}

private fun ResultRow.toConsentRecord(): ConsentRecord = ConsentRecord(
    id = this[ConsentRecordsTable.id],
    userId = this[ConsentRecordsTable.userId],
    consentType = this[ConsentRecordsTable.consentType],
    granted = this[ConsentRecordsTable.granted],
    legalDocumentVersion = this[ConsentRecordsTable.legalDocumentVersion],
    ipAddress = this[ConsentRecordsTable.ipAddress],
    userAgent = this[ConsentRecordsTable.userAgent],
    createdAt = this[ConsentRecordsTable.createdAt],
)
```

- [ ] **Step 2: Create ExposedLegalDocumentRepository**

Same pattern. Key query: `findCurrentByTypeAndLocale` orders by `publishedAt DESC` and takes first.

- [ ] **Step 3: Create ExposedDataExportRepository**

Same pattern. `findActiveByUser` checks for `PENDING` or `PROCESSING` status. `findExpired` checks `expiresAt <= now()` and `status = COMPLETED`.

- [ ] **Step 4: Create ExposedAccountDeletionRepository**

Same pattern. `findDueForExecution` checks `status = PENDING` and `scheduledAt <= now()`. `cancelByUser` updates status to `CANCELLED` where `status = PENDING`.

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/privacy/impl/src/
git commit -m "feat(privacy): add Exposed repository implementations"
```

### Task 13: Migrations and Users Table Extension

**Files:**
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/Privacy.kt`
- Modify: `server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/tables/UsersTable.kt`
- Modify: `server/auth/impl/src/main/kotlin/com/m2f/server/auth/Auth.kt`

- [ ] **Step 1: Add processingRestricted to UsersTable**

In `UsersTable.kt`, add after the `updatedAt` column:

```kotlin
val processingRestricted = bool("processing_restricted").default(false)
```

- [ ] **Step 2: Update UserRecord and UserRepository**

In `server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/repository/UserRepository.kt`:
- Add `processingRestricted: Boolean = false` to `UserRecord` data class
- Add method: `suspend fun updateProcessingRestricted(id: Uuid, restricted: Boolean): Boolean`

In `server/auth/impl/src/main/kotlin/com/m2f/server/auth/repository/ExposedUserRepository.kt`:
- In `toUserRecord()`, add: `processingRestricted = this[UsersTable.processingRestricted]`
- Implement `updateProcessingRestricted`:
```kotlin
override suspend fun updateProcessingRestricted(id: Uuid, restricted: Boolean): Boolean =
    suspendTransaction(db = db) {
        val rowsUpdated = UsersTable.update({ UsersTable.id eq id }) { stmt ->
            stmt[processingRestricted] = restricted
        }
        rowsUpdated > 0
    }
```

- [ ] **Step 3: Add migration for processingRestricted in Auth module**

In `server/auth/impl/Auth.kt`, add a new migration class and register it:

```kotlin
internal class AddProcessingRestrictedToUsersMigration : Migration {
    override val version: String = "20260312000001"
    override val description: String = "Add processing_restricted column to users table"

    override suspend fun migrate() {
        SchemaUtils.createMissingTablesAndColumns(UsersTable)
    }
}
```

Register it in `registerAuthMigrations()`.

- [ ] **Step 3: Create Privacy.kt with privacy table migrations**

```kotlin
package com.m2f.server.privacy

import com.m2f.core.database.migrations.Migration
import com.m2f.core.database.migrations.MigrationRegistry
import com.m2f.server.privacy.contract.tables.AccountDeletionRequestsTable
import com.m2f.server.privacy.contract.tables.ConsentRecordsTable
import com.m2f.server.privacy.contract.tables.DataExportRequestsTable
import com.m2f.server.privacy.contract.tables.LegalDocumentsTable
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils

internal class CreateConsentRecordsTableMigration : Migration {
    override val version: String = "20260312000010"
    override val description: String = "Create consent_records table"

    override suspend fun migrate() {
        SchemaUtils.create(ConsentRecordsTable)
    }
}

internal class CreateLegalDocumentsTableMigration : Migration {
    override val version: String = "20260312000011"
    override val description: String = "Create legal_documents table"

    override suspend fun migrate() {
        SchemaUtils.create(LegalDocumentsTable)
    }
}

internal class CreateDataExportRequestsTableMigration : Migration {
    override val version: String = "20260312000012"
    override val description: String = "Create data_export_requests table"

    override suspend fun migrate() {
        SchemaUtils.create(DataExportRequestsTable)
    }
}

internal class CreateAccountDeletionRequestsTableMigration : Migration {
    override val version: String = "20260312000013"
    override val description: String = "Create account_deletion_requests table"

    override suspend fun migrate() {
        SchemaUtils.create(AccountDeletionRequestsTable)
    }
}

fun registerPrivacyMigrations() {
    MigrationRegistry.register(CreateConsentRecordsTableMigration())
    MigrationRegistry.register(CreateLegalDocumentsTableMigration())
    MigrationRegistry.register(CreateDataExportRequestsTableMigration())
    MigrationRegistry.register(CreateAccountDeletionRequestsTableMigration())
}
```

- [ ] **Step 4: Verify it compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin && ./gradlew :server:auth:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add server/privacy/impl/src/ server/auth/contract/src/ server/auth/impl/src/
git commit -m "feat(privacy): add database migrations for privacy tables and processingRestricted column"
```

---

## Chunk 3: Server Services, Routes, DI, and Wire

This chunk implements the server business logic, API routes, Koin DI, and wire module.

### Task 14: Service Interfaces (Contract)

**Files:**
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/ConsentService.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/LegalDocumentService.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/DataExportService.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/AccountDeletionService.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/ProcessingRestrictionService.kt`
- Create: `server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/ExportContributor.kt`

- [ ] **Step 1: Create service interfaces**

All use `context(raise: Raise<DomainError>)` per project convention.

ConsentService:
```kotlin
package com.m2f.server.privacy.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse

interface ConsentService {
    context(raise: Raise<DomainError>)
    suspend fun getActiveConsents(userId: String): List<ConsentStatus>

    context(raise: Raise<DomainError>)
    suspend fun grantConsent(userId: String, request: GrantConsentRequest, ipAddress: String?, userAgent: String?)

    context(raise: Raise<DomainError>)
    suspend fun withdrawConsent(userId: String, consentType: String)

    context(raise: Raise<DomainError>)
    suspend fun getRequiredConsents(userId: String): RequiredConsentsResponse
}
```

LegalDocumentService, DataExportService, AccountDeletionService, ProcessingRestrictionService follow the same pattern with their respective operations.

ExportContributor interface:
```kotlin
package com.m2f.server.privacy.contract.service

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ExportSection(
    val name: String,
    val jsonData: String,
    val files: List<ExportFile> = emptyList(),
)

data class ExportFile(
    val name: String,
    val data: ByteArray,
)

@OptIn(ExperimentalUuidApi::class)
interface ExportContributor {
    val sectionName: String
    suspend fun export(userId: Uuid): ExportSection
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :server:privacy:contract:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/privacy/contract/src/
git commit -m "feat(privacy): add service interfaces and ExportContributor"
```

### Task 15: Service Implementations (Impl)

**Files:**
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/LegalDocumentServiceImpl.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/DataExportServiceImpl.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ProcessingRestrictionServiceImpl.kt`

- [ ] **Step 1: Implement ConsentServiceImpl**

Key logic:
- `getActiveConsents`: queries latest consent per type, maps to `ConsentStatus` DTOs
- `grantConsent`: validates document version exists via `LegalDocumentRepository`, inserts consent record
- `withdrawConsent`: inserts record with `granted = false`, no re-auth required
- `getRequiredConsents`: for each required type (PRIVACY_POLICY, TERMS_OF_SERVICE), compares user's accepted version vs current version

Uses `context(raise: Raise<DomainError>)` and Arrow's `ensure`/`ensureNotNull`.

- [ ] **Step 2: Implement LegalDocumentServiceImpl**

Key logic:
- `getCurrentDocument`: queries by type + locale, falls back to "en" if locale not found
- `getAllVersions`: returns all versions ordered by publishedAt DESC

- [ ] **Step 3: Implement DataExportServiceImpl**

Key logic:
- `requestExport`: checks no active export exists (rate limit), inserts PENDING record
- Export assembly is done by the scheduled job — service just manages the request lifecycle
- `getExportDownloadUrl`: checks status is COMPLETED, generates presigned MinIO URL

Depends on `List<ExportContributor>` injected via Koin.

- [ ] **Step 4: Implement AccountDeletionServiceImpl**

Key logic:
- `requestDeletion`: validates re-authentication (password verification via PasswordHasher + UserRepository), checks no pending deletion, inserts with scheduledAt = now + 7 days
- `cancelDeletion`: only if status is PENDING
- Actual deletion is done by the scheduled job

- [ ] **Step 5: Implement ProcessingRestrictionServiceImpl**

Key logic:
- `restrict`: sets `processingRestricted = true` on users table
- `lift`: sets `processingRestricted = false` on users table
- Uses `UserRepository` from auth contract

- [ ] **Step 6: Verify it compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add server/privacy/impl/src/
git commit -m "feat(privacy): implement privacy services"
```

### Task 16: Routes (Impl)

**Files:**
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/ConsentRoutes.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/LegalRoutes.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/ExportRoutes.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/RestrictionRoutes.kt`

- [ ] **Step 1: Create ConsentRoutes**

```kotlin
package com.m2f.server.privacy.routes

import com.m2f.core.config.server.conduit
import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.privacy.contract.service.ConsentService
import com.m2f.server.privacy.contract.service.LegalDocumentService
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.routes.Privacy
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.origin
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route

fun Route.consentRoutes(consentService: ConsentService) {
    authenticate {
        get<Privacy.GetConsents> {
            conduitAuth { userId ->
                consentService.getActiveConsents(userId)
            }
        }
        post<Privacy.GrantConsent> {
            conduitAuth { userId ->
                val request = getModel<GrantConsentRequest>()
                val ipAddress = context.request.origin.remoteAddress
                val userAgent = context.request.headers["User-Agent"]
                consentService.grantConsent(userId, request, ipAddress, userAgent)
                mapOf("message" to "Consent granted")
            }
        }
        post<Privacy.WithdrawConsent> { route ->
            conduitAuth { userId ->
                consentService.withdrawConsent(userId, route.type)
                mapOf("message" to "Consent withdrawn")
            }
        }
        get<Privacy.RequiredConsents> {
            conduitAuth { userId ->
                consentService.getRequiredConsents(userId)
            }
        }
    }
}
```

- [ ] **Step 2: Create LegalRoutes**

Legal document GET endpoints — `conduit` (unauthenticated) for public legal documents.

- [ ] **Step 3: Create ExportRoutes**

Authenticated routes using `conduitAuth` — POST to request, GET for status, GET for download.

- [ ] **Step 4: Create DeletionRoutes**

Authenticated routes — POST to request (requires password in body), GET for status, DELETE to cancel.

- [ ] **Step 5: Create RestrictionRoutes**

Authenticated routes — POST to restrict, DELETE to lift.

- [ ] **Step 6: Verify it compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add server/privacy/impl/src/
git commit -m "feat(privacy): add privacy API route definitions"
```

### Task 17: Koin DI Module and Wire

**Files:**
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/di/PrivacyModule.kt`
- Create: `server/privacy/wire/src/main/kotlin/com/m2f/server/privacy/wire/PrivacyWireModule.kt`

- [ ] **Step 1: Create PrivacyModule (DI)**

```kotlin
package com.m2f.server.privacy.di

import com.m2f.server.privacy.contract.repository.AccountDeletionRepository
import com.m2f.server.privacy.contract.repository.ConsentRepository
import com.m2f.server.privacy.contract.repository.DataExportRepository
import com.m2f.server.privacy.contract.repository.LegalDocumentRepository
import com.m2f.server.privacy.contract.service.AccountDeletionService
import com.m2f.server.privacy.contract.service.ConsentService
import com.m2f.server.privacy.contract.service.DataExportService
import com.m2f.server.privacy.contract.service.LegalDocumentService
import com.m2f.server.privacy.contract.service.ProcessingRestrictionService
import com.m2f.server.privacy.repository.ExposedAccountDeletionRepository
import com.m2f.server.privacy.repository.ExposedConsentRepository
import com.m2f.server.privacy.repository.ExposedDataExportRepository
import com.m2f.server.privacy.repository.ExposedLegalDocumentRepository
import com.m2f.server.privacy.service.AccountDeletionServiceImpl
import com.m2f.server.privacy.service.ConsentServiceImpl
import com.m2f.server.privacy.service.DataExportServiceImpl
import com.m2f.server.privacy.service.LegalDocumentServiceImpl
import com.m2f.server.privacy.service.ProcessingRestrictionServiceImpl
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.koin.dsl.module

val privacyModule = module {
    single<ConsentRepository> { ExposedConsentRepository(get<R2dbcDatabase>()) }
    single<LegalDocumentRepository> { ExposedLegalDocumentRepository(get<R2dbcDatabase>()) }
    single<DataExportRepository> { ExposedDataExportRepository(get<R2dbcDatabase>()) }
    single<AccountDeletionRepository> { ExposedAccountDeletionRepository(get<R2dbcDatabase>()) }
    single<ConsentService> { ConsentServiceImpl(get(), get()) }
    single<LegalDocumentService> { LegalDocumentServiceImpl(get()) }
    single<DataExportService> { DataExportServiceImpl(get(), getAll()) }
    single<AccountDeletionService> { AccountDeletionServiceImpl(get(), get(), get(), get()) }
    single<ProcessingRestrictionService> { ProcessingRestrictionServiceImpl(get()) }
}
```

- [ ] **Step 2: Create PrivacyWireModule**

```kotlin
package com.m2f.server.privacy.wire

import com.m2f.server.privacy.di.privacyModule
import com.m2f.server.privacy.registerPrivacyMigrations
import org.koin.dsl.module

val privacyWireModule = module {
    includes(privacyModule)
}

fun registerPrivacyWireMigrations() {
    registerPrivacyMigrations()
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :server:privacy:wire:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add server/privacy/impl/src/ server/privacy/wire/src/
git commit -m "feat(privacy): add Koin DI module and wire module"
```

### Task 18: Wire into Server Application

**Files:**
- Modify: `server/src/main/kotlin/com/m2f/template/di/ServerModule.kt`
- Modify: `server/src/main/kotlin/com/m2f/template/Application.kt`

- [ ] **Step 1: Add server:privacy:wire dependency to server/build.gradle.kts**

In `server/build.gradle.kts`, add to dependencies:
```kotlin
implementation(projects.server.privacy.wire)
```

- [ ] **Step 2: Add privacyWireModule to ServerModule**

In `ServerModule.kt`, add import and include:
```kotlin
import com.m2f.server.privacy.wire.privacyWireModule
// ...
includes(privacyWireModule)
```

- [ ] **Step 3: Register privacy migrations in Application.kt**

In `Application.kt`, add:
```kotlin
import com.m2f.server.privacy.wire.registerPrivacyWireMigrations
// ...
registerPrivacyWireMigrations()  // Before startDatabase()
```

- [ ] **Step 4: Add privacy routes in Application.module()**

In the `routing { }` block:
```kotlin
val consentService: ConsentService by inject()
val legalDocumentService: LegalDocumentService by inject()
val dataExportService: DataExportService by inject()
val accountDeletionService: AccountDeletionService by inject()
val processingRestrictionService: ProcessingRestrictionService by inject()
consentRoutes(consentService)
legalRoutes(legalDocumentService)
exportRoutes(dataExportService)
deletionRoutes(accountDeletionService)
restrictionRoutes(processingRestrictionService)
```

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :server:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/build.gradle.kts server/src/
git commit -m "feat(privacy): wire privacy module into server application"
```

---

## Chunk 4: Client Privacy Module — Pencil Designs, MVI, ViewModels, Tests

This chunk creates the `app:privacy` client module with Pencil screen designs, MVI types, ViewModels (TDD), and Screens.

### Task 19: Client Module Gradle Setup

**Files:**
- Create: `app/privacy/contract/build.gradle.kts`
- Create: `app/privacy/impl/build.gradle.kts`
- Create: `app/privacy/wire/build.gradle.kts`

- [ ] **Step 1: Add app:privacy modules to settings.gradle.kts**

After the `app:profile:wire` line in `settings.gradle.kts`, add:

```kotlin
include("app:privacy:contract")
include("app:privacy:impl")
include("app:privacy:wire")
```

- [ ] **Step 2: Create contract build.gradle.kts**

Follow `app/auth/contract/build.gradle.kts` pattern exactly:

```kotlin
plugins {
    id("kmp-library-convention")
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.navigation)
            implementation(projects.core.models)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "com.m2f.template.app.privacy.contract"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
```

- [ ] **Step 3: Create impl build.gradle.kts**

Follow `app/auth/impl/build.gradle.kts` pattern exactly. Key dependencies:
```kotlin
commonMain.dependencies {
    implementation(projects.app.privacy.contract)
    implementation(projects.core.models)
    implementation(projects.core.sdk)
    implementation(projects.core.mvi)
    implementation(projects.app.designsystem)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.components.resources)
    implementation(libs.koin.compose.viewmodel)
}
commonTest.dependencies {
    implementation(projects.core.testing)
}
```

Set `compose.resources.packageOfResClass = "template.app.privacy.generated.resources"` and `android.namespace = "com.m2f.template.app.privacy"`.

- [ ] **Step 4: Create wire build.gradle.kts**

Follow `app/auth/wire/build.gradle.kts` pattern. Key dependencies:
```kotlin
commonMain.dependencies {
    api(projects.app.privacy.contract)
    implementation(projects.app.privacy.impl)
    implementation(projects.core.mvi)
    implementation(projects.core.sdk)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.navigation3.ui)
}
```

Set `android.namespace = "com.m2f.template.app.privacy.wire"`.

- [ ] **Step 5: Verify Gradle sync**

Run: `./gradlew :app:privacy:contract:dependencies --configuration commonMainApi`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add settings.gradle.kts app/privacy/
git commit -m "feat(privacy): scaffold app:privacy module with contract/impl/wire"
```

### Task 20: Privacy Routes (Contract)

**Files:**
- Create: `app/privacy/contract/src/commonMain/kotlin/com/m2f/template/app/privacy/contract/PrivacyRoutes.kt`

- [ ] **Step 1: Create privacy routes**

```kotlin
package com.m2f.template.app.privacy.contract

import com.m2f.template.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data object ConsentGateRoute : Route

@Serializable
data object PrivacySettingsRoute : Route

@Serializable
data class LegalDocumentRoute(val type: String, val locale: String? = null) : Route

@Serializable
data object AccountDeletionRoute : Route
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:privacy:contract:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/privacy/contract/src/
git commit -m "feat(privacy): add privacy navigation routes"
```

### Task 21: Pencil Screen Designs

**Status:** COMPLETE — all designs created in `terminal_design_system.pen`.

Use `batch_get` with the node IDs below to extract layout/style details when implementing Screen composables.

#### Privacy & Compliance — Pencil Node ID Reference

| Feature | Frame | Node ID | Viewport | Status |
|---|---|---|---|---|
| **Screen 1: Consent Gate** | Unchecked | `CJ3ls` | Mobile | New |
| | Checked | `HQP5l` | Mobile | New |
| | Unchecked Desktop | _(child of `htpbU`)_ | Desktop | New |
| | Checked Desktop | _(child of `htpbU`)_ | Desktop | New |
| **Screen 2: Privacy Settings** | Mobile | `uqYr1` | Mobile | New |
| | Desktop | `WBLgM` | Desktop | New |
| **Screen 3: Legal Document** | English | `0Pzj6` | Mobile | New |
| **Screen 4: Account Deletion** | Step 1 — Warning | `QPZk8` | Mobile | New |
| | Step 1 — Warning Desktop | _(child of `77j60`)_ | Desktop | New |
| | Step 2 — Re-auth | `CK6X7` | Mobile | New |
| | Step 2 — Re-auth Desktop | _(child of `77j60`)_ | Desktop | New |
| | Step 3 — Reason | `P2Won` | Mobile | New |
| | Step 3 — Reason Desktop | _(child of `77j60`)_ | Desktop | New |
| | Step 4 — Confirm | `7Ergr` | Mobile | New |
| | Step 4 — Confirm Desktop | `M6GOH` | Desktop | New |
| | Step 5 — Scheduled | `w9mBg` | Mobile | New |
| | Step 5 — Scheduled Desktop | `M7TNv` | Desktop | New |
| **Screen 5: Login/Register Footer** | Login Mobile | `9UXn1` | Mobile | Modified |
| | Register Mobile | `KXp69` | Mobile | Modified |
| | Login Desktop | `xNUU3` | Desktop | Modified |
| | Register Desktop | `B1nWB` | Desktop | Modified |
| **Screen 6: Profile Entry** | Free/Paid/Premium/Admin/Power (desktop) | `EnD7X` `ENKv0` `d4oDS` `uGWqD` `QFwfO` | Desktop | Modified |
| | Free/Paid/Premium/Admin/Power (mobile) | `rCzUG` `k6yQw` `qgU7x` `jiyew` `OSLp4` | Mobile | Modified |

> **Note:** The Consent Gate desktop variants and Deletion Steps 1-3 desktop variants were created inside container frames. Inspect `htpbU` and `77j60` directly with `batch_get` to resolve their exact child node IDs.

#### How to use during implementation

When implementing a Screen composable, fetch the design reference:

```
batch_get(nodeIds=["<nodeId>"]) → extract colors, spacing, typography, layout structure
get_screenshot(nodeId="<nodeId>") → visual reference
```

- [ ] **Step 7: Commit any generated assets**

```bash
git add *.pen
git commit -m "feat(privacy): add Pencil screen designs for privacy features"
```

### Task 22: ConsentGate MVI Types and ViewModel (TDD)

**Files:**
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateModel.kt`
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateIntent.kt`
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateMutation.kt`
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateEvent.kt`
- Create: `app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/ConsentGateViewModelTest.kt`
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateViewModel.kt`

- [ ] **Step 1: Create MVI types**

ConsentGateModel:
```kotlin
package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.localization.StringKey

data class ConsentItem(
    val type: ConsentType,
    val currentVersion: String,
    val accepted: Boolean,
)

data class ConsentGateModel(
    val consents: List<ConsentItem> = emptyList(),
    val allAccepted: Boolean = false,
    val loading: Boolean = true,
    val error: StringKey? = null,
)
```

ConsentGateIntent:
```kotlin
package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentType

sealed interface ConsentGateIntent {
    data object LoadRequiredConsents : ConsentGateIntent
    data class ToggleConsent(val type: ConsentType) : ConsentGateIntent
    data object AcceptAll : ConsentGateIntent
    data class ViewDocument(val type: ConsentType) : ConsentGateIntent
}
```

ConsentGateMutation:
```kotlin
package com.m2f.template.app.privacy

sealed interface ConsentGateMutation {
    data class SetConsents(val consents: List<ConsentItem>) : ConsentGateMutation
    data class UpdateConsentToggle(val type: com.m2f.template.models.dto.privacy.ConsentType, val accepted: Boolean) : ConsentGateMutation
    data class SetLoading(val loading: Boolean) : ConsentGateMutation
    data class SetError(val error: com.m2f.template.models.localization.StringKey?) : ConsentGateMutation
}
```

ConsentGateEvent:
```kotlin
package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentType

sealed interface ConsentGateEvent {
    data class NavigateToDocument(val type: ConsentType) : ConsentGateEvent
    data object ConsentCompleted : ConsentGateEvent
    data class ShowError(val message: String) : ConsentGateEvent
}
```

- [ ] **Step 2: Write failing tests**

```kotlin
package com.m2f.template.app.privacy

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.RequiredConsent
import com.m2f.template.models.dto.privacy.RequiredConsentsResponse
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

class ConsentGateViewModelTest : ViewModelTest() {

    @Test
    fun `loading required consents shows consent items`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Right(RequiredConsentsResponse(
                        consents = listOf(
                            RequiredConsent(ConsentType.PRIVACY_POLICY, "1.0.0", null, true),
                            RequiredConsent(ConsentType.TERMS_OF_SERVICE, "1.0.0", null, true),
                        ),
                        hasOutdated = true,
                    ))
                }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.LoadRequiredConsents)
            model(ConsentGateModel(
                consents = listOf(
                    ConsentItem(ConsentType.PRIVACY_POLICY, "1.0.0", false),
                    ConsentItem(ConsentType.TERMS_OF_SERVICE, "1.0.0", false),
                ),
                allAccepted = false,
                loading = false,
            ))
        }
    }

    @Test
    fun `toggling consent updates accepted state`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Right(RequiredConsentsResponse(
                        consents = listOf(
                            RequiredConsent(ConsentType.PRIVACY_POLICY, "1.0.0", null, true),
                        ),
                        hasOutdated = true,
                    ))
                }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.LoadRequiredConsents)
            model(ConsentGateModel(
                consents = listOf(ConsentItem(ConsentType.PRIVACY_POLICY, "1.0.0", false)),
                allAccepted = false,
                loading = false,
            ))
            intent(ConsentGateIntent.ToggleConsent(ConsentType.PRIVACY_POLICY))
            model(ConsentGateModel(
                consents = listOf(ConsentItem(ConsentType.PRIVACY_POLICY, "1.0.0", true)),
                allAccepted = true,
                loading = false,
            ))
        }
    }

    @Test
    fun `accept all grants all consents and emits completed event`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Right(RequiredConsentsResponse(
                        consents = listOf(
                            RequiredConsent(ConsentType.PRIVACY_POLICY, "1.0.0", null, true),
                        ),
                        hasOutdated = true,
                    ))
                }
                grantConsent { Either.Right(Unit) }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.LoadRequiredConsents)
            model(ConsentGateModel(
                consents = listOf(ConsentItem(ConsentType.PRIVACY_POLICY, "1.0.0", false)),
                allAccepted = false,
                loading = false,
            ))
            intent(ConsentGateIntent.AcceptAll)
            event(ConsentGateEvent.ConsentCompleted)
        }
    }

    @Test
    fun `view document emits navigation event`() {
        val sdk = fakeSdk {
            privacy {
                getRequiredConsents {
                    Either.Right(RequiredConsentsResponse(consents = emptyList(), hasOutdated = false))
                }
            }
        }
        val viewModel = ConsentGateViewModel(sdk)
        viewModel.test {
            intent(ConsentGateIntent.ViewDocument(ConsentType.PRIVACY_POLICY))
            event(ConsentGateEvent.NavigateToDocument(ConsentType.PRIVACY_POLICY))
        }
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `./gradlew :app:privacy:impl:allTests`
Expected: FAIL (ConsentGateViewModel does not exist yet)

- [ ] **Step 4: Implement ConsentGateViewModel**

```kotlin
package com.m2f.template.app.privacy

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.privacy.GrantConsentRequest
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class ConsentGateViewModel(
    private val sdk: Sdk,
) : MviViewModel<ConsentGateIntent, ConsentGateModel, ConsentGateMutation, ConsentGateEvent>(
    initialState = ConsentGateModel()
) {

    override fun take(intent: ConsentGateIntent) {
        viewModelScope.launch {
            when (intent) {
                is ConsentGateIntent.LoadRequiredConsents -> handleLoadConsents()
                is ConsentGateIntent.ToggleConsent -> handleToggle(intent)
                is ConsentGateIntent.AcceptAll -> handleAcceptAll()
                is ConsentGateIntent.ViewDocument -> sendEvent(ConsentGateEvent.NavigateToDocument(intent.type))
            }
        }
    }

    private suspend fun handleLoadConsents() {
        sendMutation(ConsentGateMutation.SetLoading(true))
        sdk.getRequiredConsents().fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(ConsentGateMutation.SetError(key))
            },
            ifRight = { response ->
                val items = response.consents.map { consent ->
                    ConsentItem(
                        type = consent.type,
                        currentVersion = consent.currentVersion,
                        accepted = !consent.needsUpdate,
                    )
                }
                sendMutation(ConsentGateMutation.SetConsents(items))
            },
        )
    }

    private suspend fun handleToggle(intent: ConsentGateIntent.ToggleConsent) {
        val current = model.value.consents.find { it.type == intent.type } ?: return
        sendMutation(ConsentGateMutation.UpdateConsentToggle(intent.type, !current.accepted))
    }

    private suspend fun handleAcceptAll() {
        sendMutation(ConsentGateMutation.SetLoading(true))
        val consents = model.value.consents
        var allSucceeded = true
        for (consent in consents) {
            sdk.grantConsent(GrantConsentRequest(consent.type, consent.currentVersion))
                .fold(
                    ifLeft = {
                        allSucceeded = false
                        val key = StringKey.fromCode(it.code) ?: StringKey.GENERIC_ERROR
                        sendMutation(ConsentGateMutation.SetError(key))
                    },
                    ifRight = { /* ok */ },
                )
            if (!allSucceeded) break
        }
        if (allSucceeded) {
            sendEvent(ConsentGateEvent.ConsentCompleted)
        }
    }

    override suspend fun reduce(model: ConsentGateModel, mutation: ConsentGateMutation): ConsentGateModel =
        when (mutation) {
            is ConsentGateMutation.SetConsents -> model.copy(
                consents = mutation.consents,
                allAccepted = mutation.consents.all { it.accepted },
                loading = false,
                error = null,
            )
            is ConsentGateMutation.UpdateConsentToggle -> {
                val updated = model.consents.map {
                    if (it.type == mutation.type) it.copy(accepted = mutation.accepted) else it
                }
                model.copy(consents = updated, allAccepted = updated.all { it.accepted })
            }
            is ConsentGateMutation.SetLoading -> model.copy(loading = mutation.loading, error = null)
            is ConsentGateMutation.SetError -> model.copy(error = mutation.error, loading = false)
        }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :app:privacy:impl:allTests`
Expected: ALL PASS

- [ ] **Step 6: Commit**

```bash
git add app/privacy/impl/src/
git commit -m "feat(privacy): add ConsentGate MVI types, ViewModel, and tests"
```

### Task 23: PrivacySettings MVI Types and ViewModel (TDD)

Same TDD pattern as Task 22. Create:
- PrivacySettingsModel, Intent, Mutation, Event
- PrivacySettingsViewModelTest (test load, export request, withdrawal, restriction toggle)
- PrivacySettingsViewModel

Key behaviors to test:
- Loading active consents populates model
- Requesting export updates exportStatus
- Withdrawing optional consent updates consent list
- Toggling restriction updates isRestricted
- Download export emits ExportReady event

- [ ] **Step 1: Create MVI types** (PrivacySettingsModel, Intent, Mutation, Event)
- [ ] **Step 2: Write failing tests**
- [ ] **Step 3: Run tests to verify they fail**
- [ ] **Step 4: Implement PrivacySettingsViewModel**
- [ ] **Step 5: Run tests to verify they pass**
- [ ] **Step 6: Commit**

```bash
git add app/privacy/impl/src/
git commit -m "feat(privacy): add PrivacySettings MVI types, ViewModel, and tests"
```

### Task 24: LegalDocument MVI Types and ViewModel (TDD)

Same TDD pattern. Simpler ViewModel — load document, switch locale.

- [ ] **Step 1: Create MVI types** (LegalDocumentModel, Intent, Mutation, Event)
- [ ] **Step 2: Write failing tests**
- [ ] **Step 3: Run tests to verify they fail**
- [ ] **Step 4: Implement LegalDocumentViewModel**
- [ ] **Step 5: Run tests to verify they pass**
- [ ] **Step 6: Commit**

```bash
git add app/privacy/impl/src/
git commit -m "feat(privacy): add LegalDocument MVI types, ViewModel, and tests"
```

### Task 25: AccountDeletion MVI Types and ViewModel (TDD)

Same TDD pattern. Multi-step flow with DeletionStep enum.

Key behaviors to test:
- Initial load checks for pending deletion
- Re-authentication step validates password
- Confirm deletion calls SDK and emits DeletionScheduled
- Cancel deletion calls SDK and emits DeletionCancelled

- [ ] **Step 1: Create MVI types** (AccountDeletionModel, Intent, Mutation, Event)
- [ ] **Step 2: Write failing tests**
- [ ] **Step 3: Run tests to verify they fail**
- [ ] **Step 4: Implement AccountDeletionViewModel**
- [ ] **Step 5: Run tests to verify they pass**
- [ ] **Step 6: Commit**

```bash
git add app/privacy/impl/src/
git commit -m "feat(privacy): add AccountDeletion MVI types, ViewModel, and tests"
```

### Task 26: Screen Composables

**Files:**
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateScreen.kt`
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt`
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/LegalDocumentScreen.kt`
- Create: `app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt`

All screens follow the callbacks pattern (no direct ViewModel access), use `BoxWithConstraints` with 840.dp breakpoint, and use `TerminalTheme` styling. Reference the Pencil designs created in Task 21 for exact layout. Use the Pencil MCP `get_screenshot` tool to validate visual output matches design.

**IMPORTANT:** These function signatures MUST match the callsites in Task 27's `PrivacyNavigation.kt`. Do not rename parameters.

- [ ] **Step 1: Create ConsentGateScreen**

Signature:
```kotlin
@Composable
fun ConsentGateScreen(
    state: ConsentGateModel,
    onToggleConsent: (ConsentType) -> Unit,
    onAcceptAll: () -> Unit,
    onViewDocument: (ConsentType) -> Unit,
    onDecline: () -> Unit,
)
```

Layout: full-screen modal. Checkbox list for each consent item. Each item has a "Read" link. "Accept All" primary button (disabled until `state.allAccepted`). "Decline" secondary button. Loading indicator when `state.loading`. Error message from `state.error`.

- [ ] **Step 2: Create PrivacySettingsScreen**

Signature:
```kotlin
@Composable
fun PrivacySettingsScreen(
    state: PrivacySettingsModel,
    onRequestExport: () -> Unit,
    onDownloadExport: () -> Unit,
    onRequestDeletion: () -> Unit,
    onToggleRestriction: () -> Unit,
    onViewDocument: (ConsentType) -> Unit,
    onWithdrawConsent: (ConsentType) -> Unit,
    onBack: () -> Unit,
)
```

Layout: scrollable column with sections. "Your Consents" section with toggle switches for optional consents. "Data Export" section with status states. "Processing Restriction" toggle. "Delete Account" danger button.

- [ ] **Step 3: Create LegalDocumentScreen**

Signature:
```kotlin
@Composable
fun LegalDocumentScreen(
    state: LegalDocumentModel,
    onSwitchLocale: (String) -> Unit,
    onBack: () -> Unit,
)
```

Layout: header with document type, version badge, locale switcher. Scrollable markdown content area.

- [ ] **Step 4: Create AccountDeletionScreen**

Signature:
```kotlin
@Composable
fun AccountDeletionScreen(
    state: AccountDeletionModel,
    onReAuthenticate: (String) -> Unit,
    onSetReason: (String) -> Unit,
    onConfirmDeletion: () -> Unit,
    onCancelDeletion: () -> Unit,
    onBack: () -> Unit,
)
```

Layout: multi-step flow based on `state.step`. Step WARNING: warning text + continue. Step RE_AUTH: password field + verify. Step REASON: optional text area. Step CONFIRM: summary + danger button. Step SCHEDULED: confirmation + cancel option.
- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :app:privacy:impl:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/privacy/impl/src/
git commit -m "feat(privacy): add privacy screen composables"
```

---

## Chunk 5: Wire Module, App Integration, and Cross-Cutting Concerns

This chunk wires everything together — client DI, navigation, consent gate guard, scheduled jobs, and final integration.

### Task 27: Client Wire Module

**Files:**
- Create: `app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyModule.kt`
- Create: `app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt`

- [ ] **Step 1: Create PrivacyModule (Koin)**

```kotlin
package com.m2f.template.app.privacy.wire

import com.m2f.template.app.privacy.AccountDeletionViewModel
import com.m2f.template.app.privacy.ConsentGateViewModel
import com.m2f.template.app.privacy.LegalDocumentViewModel
import com.m2f.template.app.privacy.PrivacySettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val privacyModule = module {
    viewModelOf(::ConsentGateViewModel)
    viewModelOf(::PrivacySettingsViewModel)
    viewModelOf(::LegalDocumentViewModel)
    viewModelOf(::AccountDeletionViewModel)
}
```

- [ ] **Step 2: Create PrivacyNavigation**

```kotlin
package com.m2f.template.app.privacy.wire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entry
import com.m2f.template.app.privacy.AccountDeletionScreen
import com.m2f.template.app.privacy.AccountDeletionEvent
import com.m2f.template.app.privacy.AccountDeletionIntent
import com.m2f.template.app.privacy.AccountDeletionViewModel
import com.m2f.template.app.privacy.ConsentGateEvent
import com.m2f.template.app.privacy.ConsentGateIntent
import com.m2f.template.app.privacy.ConsentGateScreen
import com.m2f.template.app.privacy.ConsentGateViewModel
import com.m2f.template.app.privacy.LegalDocumentEvent
import com.m2f.template.app.privacy.LegalDocumentIntent
import com.m2f.template.app.privacy.LegalDocumentScreen
import com.m2f.template.app.privacy.LegalDocumentViewModel
import com.m2f.template.app.privacy.PrivacySettingsEvent
import com.m2f.template.app.privacy.PrivacySettingsIntent
import com.m2f.template.app.privacy.PrivacySettingsScreen
import com.m2f.template.app.privacy.PrivacySettingsViewModel
import com.m2f.template.app.privacy.contract.AccountDeletionRoute
import com.m2f.template.app.privacy.contract.ConsentGateRoute
import com.m2f.template.app.privacy.contract.LegalDocumentRoute
import com.m2f.template.app.privacy.contract.PrivacySettingsRoute
import com.m2f.template.navigation.Route
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.privacyEntries(
    backStack: MutableList<Route>,
    onConsentCompleted: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    entry<ConsentGateRoute> {
        val viewModel = koinViewModel<ConsentGateViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.take(ConsentGateIntent.LoadRequiredConsents)
        }

        ConsentGateScreen(
            state = state,
            onToggleConsent = { viewModel.take(ConsentGateIntent.ToggleConsent(it)) },
            onAcceptAll = { viewModel.take(ConsentGateIntent.AcceptAll) },
            onViewDocument = { viewModel.take(ConsentGateIntent.ViewDocument(it)) },
            onDecline = {
                // Declining means the user cannot use the app — log them out
                onAccountDeleted()  // Reuse the same logout flow
            },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is ConsentGateEvent.NavigateToDocument -> backStack.add(LegalDocumentRoute(event.type.name))
                    is ConsentGateEvent.ConsentCompleted -> onConsentCompleted()
                    is ConsentGateEvent.ShowError -> { /* handled in model.error */ }
                }
            }
        }
    }

    entry<PrivacySettingsRoute> {
        val viewModel = koinViewModel<PrivacySettingsViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.take(PrivacySettingsIntent.Load)
        }

        PrivacySettingsScreen(
            state = state,
            onRequestExport = { viewModel.take(PrivacySettingsIntent.RequestExport) },
            onDownloadExport = { viewModel.take(PrivacySettingsIntent.DownloadExport) },
            onRequestDeletion = { backStack.add(AccountDeletionRoute) },
            onToggleRestriction = { viewModel.take(PrivacySettingsIntent.ToggleRestriction) },
            onViewDocument = { viewModel.take(PrivacySettingsIntent.ViewDocument(it)) },
            onWithdrawConsent = { viewModel.take(PrivacySettingsIntent.WithdrawConsent(it)) },
            onBack = { backStack.removeLastOrNull() },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is PrivacySettingsEvent.NavigateToDeletion -> backStack.add(AccountDeletionRoute)
                    is PrivacySettingsEvent.NavigateToDocument -> backStack.add(LegalDocumentRoute(event.type.name))
                    is PrivacySettingsEvent.ExportReady -> { /* handled in model */ }
                    is PrivacySettingsEvent.ShowError -> { /* handled in model.error */ }
                }
            }
        }
    }

    entry<LegalDocumentRoute> { route ->
        val viewModel = koinViewModel<LegalDocumentViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(route.type, route.locale) {
            viewModel.take(LegalDocumentIntent.Load(route.type, route.locale))
        }

        LegalDocumentScreen(
            state = state,
            onSwitchLocale = { viewModel.take(LegalDocumentIntent.SwitchLocale(it)) },
            onBack = { backStack.removeLastOrNull() },
        )
    }

    entry<AccountDeletionRoute> {
        val viewModel = koinViewModel<AccountDeletionViewModel>()
        val state by viewModel.model.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.take(AccountDeletionIntent.Load)
        }

        AccountDeletionScreen(
            state = state,
            onReAuthenticate = { viewModel.take(AccountDeletionIntent.ReAuthenticate(it)) },
            onSetReason = { viewModel.take(AccountDeletionIntent.SetReason(it)) },
            onConfirmDeletion = { viewModel.take(AccountDeletionIntent.ConfirmDeletion) },
            onCancelDeletion = { viewModel.take(AccountDeletionIntent.CancelDeletion) },
            onBack = { backStack.removeLastOrNull() },
        )

        LaunchedEffect(Unit) {
            viewModel.event.collect { event ->
                when (event) {
                    is AccountDeletionEvent.DeletionScheduled -> backStack.removeLastOrNull()
                    is AccountDeletionEvent.DeletionCancelled -> backStack.removeLastOrNull()
                    is AccountDeletionEvent.NavigateToLogin -> onAccountDeleted()
                    is AccountDeletionEvent.ShowError -> { /* handled in model.error */ }
                }
            }
        }
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:privacy:wire:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/privacy/wire/src/
git commit -m "feat(privacy): add client wire module with Koin DI and navigation"
```

### Task 28: App Integration

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/com/m2f/template/di/AppModule.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/m2f/template/navigation/AppNavHost.kt`

- [ ] **Step 1: Add privacyModule to AppModule**

In `AppModule.kt`:
```kotlin
import com.m2f.template.app.privacy.wire.privacyModule
// In the includes(...) block, add:
privacyModule,
```

- [ ] **Step 2: Add privacyEntries to AppNavHost**

In `AppNavHost.kt`, inside the `entryProvider { }` block:
```kotlin
import com.m2f.template.app.privacy.wire.privacyEntries
// ...
val tokenStorage = koinInject<TokenStorage>()
// ...
privacyEntries(
    backStack = backStack,
    onConsentCompleted = {
        backStack.clear()
        backStack.add(DashboardRoute)
    },
    onAccountDeleted = {
        tokenStorage.clearTokens()
        backStack.clear()
        backStack.add(LoginRoute())
    },
)
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/
git commit -m "feat(privacy): wire privacy module into app navigation and DI"
```

### Task 29: Consent Gate Navigation Guard

**Files:**
- Modify: `app/auth/wire/build.gradle.kts` — Add `implementation(projects.app.privacy.contract)`
- Modify: `app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/LoginEvent.kt`
- Modify: `app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/LoginViewModel.kt`
- Modify: `app/auth/wire/src/commonMain/kotlin/com/m2f/template/app/auth/wire/AuthNavigation.kt`
- Modify: `app/auth/impl/src/commonTest/kotlin/com/m2f/template/app/auth/LoginViewModelTest.kt`

- [ ] **Step 1: Add privacy contract dependency to auth wire**

In `app/auth/wire/build.gradle.kts`, add:
```kotlin
implementation(projects.app.privacy.contract)
```

- [ ] **Step 2: Add consent check to post-login flow**

In `LoginViewModel.kt`, after successful login (in `handlePostLogin()`), before navigating to dashboard:

```kotlin
// After successful login, check required consents
sdk.getRequiredConsents().fold(
    ifLeft = {
        // If we can't check, proceed to dashboard
        sendEvent(LoginEvent.NavigateToDashboard)
    },
    ifRight = { response ->
        if (response.hasOutdated) {
            sendEvent(LoginEvent.NavigateToConsentGate)
        } else {
            sendEvent(LoginEvent.NavigateToDashboard)
        }
    },
)
```

Add `NavigateToConsentGate` to `LoginEvent`:
```kotlin
data object NavigateToConsentGate : LoginEvent
```

Update the auth wire module's navigation to handle the new event:
```kotlin
is LoginEvent.NavigateToConsentGate -> {
    backStack.clear()
    backStack.add(ConsentGateRoute)
}
```

- [ ] **Step 3: Update LoginViewModel tests**

Add test for consent gate redirection after login:
```kotlin
@Test
fun `successful login with outdated consents navigates to consent gate`() {
    val sdk = fakeSdk {
        auth {
            login { _, _ ->
                Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600))
            }
        }
        privacy {
            getRequiredConsents {
                Either.Right(RequiredConsentsResponse(
                    consents = listOf(RequiredConsent(ConsentType.PRIVACY_POLICY, "2.0.0", "1.0.0", true)),
                    hasOutdated = true,
                ))
            }
        }
    }
    val viewModel = LoginViewModel(sdk)
    viewModel.test {
        intent(LoginIntent.EmailChanged("user@test.com"))
        model(LoginModel(email = "user@test.com"))
        intent(LoginIntent.PasswordChanged("password123"))
        model(LoginModel(email = "user@test.com", password = "password123"))
        intent(LoginIntent.SubmitLoginClicked)
        model(LoginModel(email = "user@test.com", password = "password123", isLoading = true))
        event(LoginEvent.NavigateToConsentGate)
    }
}

@Test
fun `successful login with no outdated consents navigates to dashboard`() {
    val sdk = fakeSdk {
        auth {
            login { _, _ ->
                Either.Right(AuthResponse(accessToken = "tok", refreshToken = "ref", expiresIn = 3600))
            }
        }
        privacy {
            getRequiredConsents {
                Either.Right(RequiredConsentsResponse(consents = emptyList(), hasOutdated = false))
            }
        }
    }
    val viewModel = LoginViewModel(sdk)
    viewModel.test {
        intent(LoginIntent.EmailChanged("user@test.com"))
        model(LoginModel(email = "user@test.com"))
        intent(LoginIntent.PasswordChanged("password123"))
        model(LoginModel(email = "user@test.com", password = "password123"))
        intent(LoginIntent.SubmitLoginClicked)
        model(LoginModel(email = "user@test.com", password = "password123", isLoading = true))
        event(LoginEvent.NavigateToDashboard)
    }
}
```

- [ ] **Step 4: Run tests**

Run: `./gradlew :app:auth:impl:allTests`
Expected: ALL PASS

- [ ] **Step 5: Commit**

```bash
git add app/auth/wire/build.gradle.kts app/auth/impl/src/ app/auth/wire/src/
git commit -m "feat(privacy): add consent gate navigation guard to post-login flow"
```

### Task 30: Scheduled Jobs

**Files:**
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/PrivacyJobScheduler.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/DeletionExecutorJob.kt`
- Create: `server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/jobs/ExportCleanupJob.kt`

- [ ] **Step 1: Create PrivacyJobScheduler**

A simple coroutine-based scheduler that runs jobs at a configurable interval:

```kotlin
package com.m2f.server.privacy.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

interface PrivacyJob {
    val name: String
    val interval: Duration get() = 24.hours
    suspend fun execute()
}

class PrivacyJobScheduler(
    private val scope: CoroutineScope,
    private val jobs: List<PrivacyJob>,
) {
    fun start() {
        jobs.forEach { job ->
            scope.launch {
                while (true) {
                    try {
                        job.execute()
                    } catch (e: Exception) {
                        // Log and continue — don't crash the scheduler
                    }
                    delay(job.interval)
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create DeletionExecutorJob**

Queries account deletion requests due for execution, cascades deletion across modules.

- [ ] **Step 3: Create ExportCleanupJob**

Queries expired exports, deletes files from MinIO, updates status.

- [ ] **Step 4: Wire jobs into the privacy Koin module and start them in the Application**

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :server:privacy:impl:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/privacy/impl/src/
git commit -m "feat(privacy): add scheduled jobs for deletion execution and export cleanup"
```

### Task 31: Final Verification

- [ ] **Step 1: Run all tests**

Run: `./gradlew testAll`
Expected: ALL PASS

- [ ] **Step 2: Run detekt**

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL (no new violations)

- [ ] **Step 3: Verify server starts**

Run: `./gradlew devUp && ./gradlew :server:run`
Expected: Server starts, migrations execute, privacy routes registered

- [ ] **Step 4: Final commit with any fixups**

```bash
git add -A
git commit -m "feat(privacy): final integration verification and fixes"
```
