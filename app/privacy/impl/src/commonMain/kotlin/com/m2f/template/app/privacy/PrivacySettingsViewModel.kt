package com.m2f.template.app.privacy

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PrivacySettingsViewModel(
    private val sdk: Sdk,
) : MviViewModel<PrivacySettingsIntent, PrivacySettingsModel, PrivacySettingsMutation, PrivacySettingsEvent>(
    initialState = PrivacySettingsModel()
) {

    override fun take(intent: PrivacySettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is PrivacySettingsIntent.Load -> handleLoad()
                is PrivacySettingsIntent.RequestExport -> handleRequestExport()
                is PrivacySettingsIntent.DownloadExport -> handleDownloadExport()
                is PrivacySettingsIntent.ViewDocument -> sendEvent(PrivacySettingsEvent.NavigateToDocument(intent.type))
                is PrivacySettingsIntent.WithdrawConsent -> handleWithdrawConsent(intent.type)
            }
        }
    }

    private suspend fun handleLoad() {
        sendMutation(PrivacySettingsMutation.SetLoading(true))
        val consentsDeferred = viewModelScope.async { sdk.getActiveConsents() }
        val deletionDeferred = viewModelScope.async { sdk.getDeletionStatus() }
        val exportDeferred = viewModelScope.async { sdk.getActiveExport() }

        consentsDeferred.await().fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(PrivacySettingsMutation.SetError(key))
            },
            ifRight = { consents ->
                sendMutation(PrivacySettingsMutation.SetConsents(consents))
            },
        )

        deletionDeferred.await().fold(
            ifLeft = { /* ignore deletion status error if consents loaded */ },
            ifRight = { status ->
                sendMutation(PrivacySettingsMutation.SetDeletionStatus(status))
            },
        )

        exportDeferred.await().fold(
            ifLeft = { /* ignore export status error */ },
            ifRight = { export ->
                if (export != null) {
                    sendMutation(PrivacySettingsMutation.SetExportStatus(export))
                }
            },
        )
    }

    private suspend fun handleRequestExport() {
        sendMutation(PrivacySettingsMutation.SetLoading(true))
        sdk.requestDataExport().fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(PrivacySettingsMutation.SetError(key))
            },
            ifRight = { response ->
                sendMutation(PrivacySettingsMutation.SetExportStatus(response))
            },
        )
    }

    private suspend fun handleWithdrawConsent(type: com.m2f.template.models.dto.privacy.ConsentType) {
        sendMutation(PrivacySettingsMutation.SetLoading(true))
        sdk.withdrawConsent(type).fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(PrivacySettingsMutation.SetError(key))
            },
            ifRight = {
                // Reload consents after withdrawal
                sdk.getActiveConsents().fold(
                    ifLeft = { error ->
                        val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                        sendMutation(PrivacySettingsMutation.SetError(key))
                    },
                    ifRight = { consents ->
                        sendMutation(PrivacySettingsMutation.SetConsents(consents))
                    },
                )
            },
        )
    }

    private suspend fun handleDownloadExport() {
        val exportId = model.value.exportStatus?.id ?: return
        sdk.getExportDownloadUrl(exportId).fold(
            ifLeft = { error ->
                val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                sendMutation(PrivacySettingsMutation.SetError(key))
            },
            ifRight = { url ->
                sendEvent(PrivacySettingsEvent.ExportReady(url))
            },
        )
    }

    override suspend fun reduce(model: PrivacySettingsModel, mutation: PrivacySettingsMutation): PrivacySettingsModel =
        when (mutation) {
            is PrivacySettingsMutation.SetConsents -> model.copy(
                activeConsents = mutation.consents,
                loading = false,
                error = null,
            )
            is PrivacySettingsMutation.SetExportStatus -> model.copy(
                exportStatus = mutation.status,
                loading = false,
                error = null,
            )
            is PrivacySettingsMutation.SetDeletionStatus -> model.copy(
                deletionStatus = mutation.status,
            )
            is PrivacySettingsMutation.SetLoading -> model.copy(
                loading = mutation.loading,
                error = null,
            )
            is PrivacySettingsMutation.SetError -> model.copy(
                error = mutation.error,
                loading = false,
            )
        }
}
