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

/**
 * DSL builder for creating fake [PrivacyApi] instances in tests.
 *
 * Each method defaults to returning `Either.Left(AppError.Client.Unknown())`
 * so that unconfigured code paths fail fast instead of silently succeeding.
 *
 * Usage:
 * ```kotlin
 * val privacyApi = fakePrivacyApi {
 *     getActiveConsents { Either.Right(emptyList()) }
 * }
 * ```
 */
@FakeSDKDsl
class FakePrivacyApiBuilder {

    private var _getActiveConsents: suspend () -> Either<AppError, List<ConsentStatus>> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _grantConsent: suspend (GrantConsentRequest) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _withdrawConsent: suspend (ConsentType) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getRequiredConsents: suspend () -> Either<AppError, RequiredConsentsResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getLegalDocument: suspend (ConsentType, String?) -> Either<AppError, LegalDocumentResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    private var _requestDataExport: suspend () -> Either<AppError, DataExportResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getExportStatus: suspend (String) -> Either<AppError, DataExportResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getExportDownloadUrl: suspend (String) -> Either<AppError, String> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _requestAccountDeletion: suspend (DeletionRequest) -> Either<AppError, DeletionResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getDeletionStatus: suspend () -> Either<AppError, DeletionResponse?> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _cancelDeletion: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _restrictProcessing: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _liftRestriction: suspend () -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    fun getActiveConsents(behavior: suspend () -> Either<AppError, List<ConsentStatus>>) {
        _getActiveConsents = behavior
    }

    fun grantConsent(behavior: suspend (GrantConsentRequest) -> Either<AppError, Unit>) {
        _grantConsent = behavior
    }

    fun withdrawConsent(behavior: suspend (ConsentType) -> Either<AppError, Unit>) {
        _withdrawConsent = behavior
    }

    fun getRequiredConsents(behavior: suspend () -> Either<AppError, RequiredConsentsResponse>) {
        _getRequiredConsents = behavior
    }

    fun getLegalDocument(behavior: suspend (ConsentType, String?) -> Either<AppError, LegalDocumentResponse>) {
        _getLegalDocument = behavior
    }

    fun requestDataExport(behavior: suspend () -> Either<AppError, DataExportResponse>) {
        _requestDataExport = behavior
    }

    fun getExportStatus(behavior: suspend (String) -> Either<AppError, DataExportResponse>) {
        _getExportStatus = behavior
    }

    fun getExportDownloadUrl(behavior: suspend (String) -> Either<AppError, String>) {
        _getExportDownloadUrl = behavior
    }

    fun requestAccountDeletion(behavior: suspend (DeletionRequest) -> Either<AppError, DeletionResponse>) {
        _requestAccountDeletion = behavior
    }

    fun getDeletionStatus(behavior: suspend () -> Either<AppError, DeletionResponse?>) {
        _getDeletionStatus = behavior
    }

    fun cancelDeletion(behavior: suspend () -> Either<AppError, Unit>) {
        _cancelDeletion = behavior
    }

    fun restrictProcessing(behavior: suspend () -> Either<AppError, Unit>) {
        _restrictProcessing = behavior
    }

    fun liftRestriction(behavior: suspend () -> Either<AppError, Unit>) {
        _liftRestriction = behavior
    }

    internal fun build(): PrivacyApi = object : PrivacyApi {
        override suspend fun getActiveConsents(): Either<AppError, List<ConsentStatus>> =
            _getActiveConsents()

        override suspend fun grantConsent(request: GrantConsentRequest): Either<AppError, Unit> =
            _grantConsent(request)

        override suspend fun withdrawConsent(type: ConsentType): Either<AppError, Unit> =
            _withdrawConsent(type)

        override suspend fun getRequiredConsents(): Either<AppError, RequiredConsentsResponse> =
            _getRequiredConsents()

        override suspend fun getLegalDocument(type: ConsentType, locale: String?): Either<AppError, LegalDocumentResponse> =
            _getLegalDocument(type, locale)

        override suspend fun requestDataExport(): Either<AppError, DataExportResponse> =
            _requestDataExport()

        override suspend fun getExportStatus(id: String): Either<AppError, DataExportResponse> =
            _getExportStatus(id)

        override suspend fun getExportDownloadUrl(id: String): Either<AppError, String> =
            _getExportDownloadUrl(id)

        override suspend fun requestAccountDeletion(request: DeletionRequest): Either<AppError, DeletionResponse> =
            _requestAccountDeletion(request)

        override suspend fun getDeletionStatus(): Either<AppError, DeletionResponse?> =
            _getDeletionStatus()

        override suspend fun cancelDeletion(): Either<AppError, Unit> =
            _cancelDeletion()

        override suspend fun restrictProcessing(): Either<AppError, Unit> =
            _restrictProcessing()

        override suspend fun liftRestriction(): Either<AppError, Unit> =
            _liftRestriction()
    }
}
