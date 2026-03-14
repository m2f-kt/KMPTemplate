package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.LegalDocumentResponse
import com.m2f.template.models.localization.StringKey

data class LegalDocumentModel(
    val document: LegalDocumentResponse? = null,
    val loading: Boolean = true,
    val error: StringKey? = null,
)
