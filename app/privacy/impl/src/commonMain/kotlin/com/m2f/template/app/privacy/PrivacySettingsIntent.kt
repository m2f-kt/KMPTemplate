package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.ConsentType

sealed interface PrivacySettingsIntent {
    data object Load : PrivacySettingsIntent
    data object RequestExport : PrivacySettingsIntent
    data object DownloadExport : PrivacySettingsIntent
    data class ViewDocument(val type: ConsentType) : PrivacySettingsIntent
    data class ToggleConsent(val consent: ConsentStatus) : PrivacySettingsIntent
}
