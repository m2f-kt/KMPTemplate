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
