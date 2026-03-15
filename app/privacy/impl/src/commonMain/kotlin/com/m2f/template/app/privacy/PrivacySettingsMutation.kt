package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.DataExportResponse
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.localization.StringKey

sealed interface PrivacySettingsMutation {
    data class SetConsents(val consents: List<ConsentStatus>) : PrivacySettingsMutation
    data class SetExportStatus(val status: DataExportResponse?) : PrivacySettingsMutation
    data class SetDeletionStatus(val status: DeletionResponse?) : PrivacySettingsMutation
    data class SetLoading(val loading: Boolean) : PrivacySettingsMutation
    data class SetError(val error: StringKey?) : PrivacySettingsMutation
}
