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
    suspend fun getActiveExport(): Either<AppError, DataExportResponse?>
    suspend fun requestAccountDeletion(request: DeletionRequest): Either<AppError, DeletionResponse>
    suspend fun getDeletionStatus(): Either<AppError, DeletionResponse?>
    suspend fun cancelDeletion(): Either<AppError, Unit>
}
