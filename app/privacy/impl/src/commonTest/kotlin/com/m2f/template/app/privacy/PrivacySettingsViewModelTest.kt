package com.m2f.template.app.privacy

import arrow.core.Either
import com.m2f.template.core.testing.ViewModelTest
import com.m2f.template.core.testing.fakes.fakeSdk
import com.m2f.template.core.testing.test
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.ConsentType
import com.m2f.template.models.dto.privacy.DataExportResponse
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.dto.privacy.DeletionStatus
import com.m2f.template.models.dto.privacy.ExportStatus
import com.m2f.template.models.localization.StringKey
import kotlin.test.Test

class PrivacySettingsViewModelTest : ViewModelTest() {

    private val sampleConsents = listOf(
        ConsentStatus(
            type = ConsentType.PRIVACY_POLICY,
            granted = true,
            grantedAt = "2026-01-01T00:00:00Z",
            documentVersion = "1.0",
        ),
        ConsentStatus(
            type = ConsentType.MARKETING,
            granted = false,
            grantedAt = null,
            documentVersion = null,
        ),
    )

    private val sampleDeletionStatus = DeletionResponse(
        id = "del-1",
        status = DeletionStatus.PENDING,
        scheduledAt = "2026-04-01T00:00:00Z",
    )

    private val sampleExport = DataExportResponse(
        id = "exp-1",
        status = ExportStatus.COMPLETED,
        downloadUrl = "https://example.com/export/exp-1",
        createdAt = "2026-03-14T00:00:00Z",
        expiresAt = "2026-03-21T00:00:00Z",
    )

    @Test
    fun `load populates active consents and deletion status`() {
        val sdk = fakeSdk {
            privacy {
                getActiveConsents { Either.Right(sampleConsents) }
                getDeletionStatus { Either.Right(sampleDeletionStatus) }
            }
        }
        val viewModel = PrivacySettingsViewModel(sdk)
        viewModel.test {
            intent(PrivacySettingsIntent.Load)
            model(
                PrivacySettingsModel(
                    activeConsents = sampleConsents,
                    deletionStatus = sampleDeletionStatus,
                    loading = false,
                    error = null,
                )
            )
        }
    }

    @Test
    fun `request export updates export status in model`() {
        val sdk = fakeSdk {
            privacy {
                requestDataExport { Either.Right(sampleExport) }
            }
        }
        val viewModel = PrivacySettingsViewModel(sdk)
        viewModel.test {
            intent(PrivacySettingsIntent.RequestExport)
            model(
                PrivacySettingsModel(
                    exportStatus = sampleExport,
                    loading = false,
                    error = null,
                )
            )
        }
    }

    @Test
    fun `withdraw consent reloads consents after success`() {
        val updatedConsents = listOf(
            ConsentStatus(
                type = ConsentType.PRIVACY_POLICY,
                granted = true,
                grantedAt = "2026-01-01T00:00:00Z",
                documentVersion = "1.0",
            ),
        )
        val sdk = fakeSdk {
            privacy {
                withdrawConsent { Either.Right(Unit) }
                getActiveConsents { Either.Right(updatedConsents) }
            }
        }
        val viewModel = PrivacySettingsViewModel(sdk)
        viewModel.test {
            intent(PrivacySettingsIntent.WithdrawConsent(ConsentType.MARKETING))
            model(
                PrivacySettingsModel(
                    activeConsents = updatedConsents,
                    loading = false,
                    error = null,
                )
            )
        }
    }

    @Test
    fun `view document emits NavigateToDocument event`() {
        val sdk = fakeSdk {
            privacy {
                getActiveConsents { Either.Right(sampleConsents) }
                getDeletionStatus { Either.Right(null) }
            }
        }
        val viewModel = PrivacySettingsViewModel(sdk)
        viewModel.test {
            intent(PrivacySettingsIntent.Load)
            model(
                PrivacySettingsModel(
                    activeConsents = sampleConsents,
                    deletionStatus = null,
                    loading = false,
                    error = null,
                )
            )
            intent(PrivacySettingsIntent.ViewDocument(ConsentType.PRIVACY_POLICY))
            event(PrivacySettingsEvent.NavigateToDocument(ConsentType.PRIVACY_POLICY))
        }
    }

    @Test
    fun `download export emits ExportReady event`() {
        val sdk = fakeSdk {
            privacy {
                requestDataExport { Either.Right(sampleExport) }
                getExportDownloadUrl { Either.Right("https://example.com/download/exp-1") }
            }
        }
        val viewModel = PrivacySettingsViewModel(sdk)
        viewModel.test {
            // First set up export status
            intent(PrivacySettingsIntent.RequestExport)
            model(
                PrivacySettingsModel(
                    exportStatus = sampleExport,
                    loading = false,
                    error = null,
                )
            )
            // Then download
            intent(PrivacySettingsIntent.DownloadExport)
            event(PrivacySettingsEvent.ExportReady("https://example.com/download/exp-1"))
        }
    }

    @Test
    fun `load with error shows error in model`() {
        val sdk = fakeSdk {
            privacy {
                getActiveConsents { Either.Left(AppError.Client.Unknown()) }
                getDeletionStatus { Either.Left(AppError.Client.Unknown()) }
            }
        }
        val viewModel = PrivacySettingsViewModel(sdk)
        viewModel.test {
            intent(PrivacySettingsIntent.Load)
            model(
                PrivacySettingsModel(
                    loading = false,
                    error = StringKey.CLIENT_UNKNOWN_ERROR,
                )
            )
        }
    }
}
