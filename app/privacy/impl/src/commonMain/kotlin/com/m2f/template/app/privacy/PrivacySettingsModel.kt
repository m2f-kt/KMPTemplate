package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.ConsentStatus
import com.m2f.template.models.dto.privacy.DataExportResponse
import com.m2f.template.models.dto.privacy.DeletionResponse
import com.m2f.template.models.localization.StringKey

data class PrivacySettingsModel(
    val activeConsents: List<ConsentStatus> = emptyList(),
    val exportStatus: DataExportResponse? = null,
    val deletionStatus: DeletionResponse? = null,
    val loading: Boolean = true,
    val error: StringKey? = null,
)
