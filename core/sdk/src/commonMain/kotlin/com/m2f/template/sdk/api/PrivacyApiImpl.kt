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
import com.m2f.template.sdk.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PrivacyApiImpl(private val client: HttpClient) : PrivacyApi {

    override suspend fun getActiveConsents(): Either<AppError, List<ConsentStatus>> =
        apiCall { client.get(Privacy.GetConsents()) }

    override suspend fun grantConsent(request: GrantConsentRequest): Either<AppError, Unit> =
        apiCall {
            client.post(Privacy.GrantConsent()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun withdrawConsent(type: ConsentType): Either<AppError, Unit> =
        apiCall { client.post(Privacy.WithdrawConsent(type = type.name)) }

    override suspend fun getRequiredConsents(): Either<AppError, RequiredConsentsResponse> =
        apiCall { client.get(Privacy.RequiredConsents()) }

    override suspend fun getLegalDocument(type: ConsentType, locale: String?): Either<AppError, LegalDocumentResponse> =
        apiCall { client.get(Privacy.LegalDocument(type = type.name, locale = locale)) }

    override suspend fun requestDataExport(): Either<AppError, DataExportResponse> =
        apiCall { client.post(Privacy.RequestExport()) }

    override suspend fun getExportStatus(id: String): Either<AppError, DataExportResponse> =
        apiCall { client.get(Privacy.ExportStatus(id = id)) }

    override suspend fun getExportDownloadUrl(id: String): Either<AppError, String> =
        apiCall { client.get(Privacy.ExportDownload(id = id)) }

    override suspend fun requestAccountDeletion(request: DeletionRequest): Either<AppError, DeletionResponse> =
        apiCall {
            client.post(Privacy.RequestDeletion()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun getDeletionStatus(): Either<AppError, DeletionResponse?> =
        apiCall { client.get(Privacy.GetDeletionStatus()) }

    override suspend fun cancelDeletion(): Either<AppError, Unit> =
        apiCall { client.post(Privacy.CancelDeletion()) }

    override suspend fun restrictProcessing(): Either<AppError, Unit> =
        apiCall { client.post(Privacy.RestrictProcessing()) }

    override suspend fun liftRestriction(): Either<AppError, Unit> =
        apiCall { client.post(Privacy.LiftRestriction()) }
}
