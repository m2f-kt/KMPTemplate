package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentType

sealed interface PrivacySettingsEvent {
    data object NavigateToDeletion : PrivacySettingsEvent
    data class NavigateToDocument(val type: ConsentType) : PrivacySettingsEvent
    data class ExportReady(val downloadUrl: String) : PrivacySettingsEvent
    data class ShowError(val message: String) : PrivacySettingsEvent
}
