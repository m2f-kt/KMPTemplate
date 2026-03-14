package com.m2f.template.app.privacy

import com.m2f.template.models.dto.privacy.LegalDocumentResponse
import com.m2f.template.models.localization.StringKey

sealed interface LegalDocumentMutation {
    data class SetDocument(val document: LegalDocumentResponse?) : LegalDocumentMutation
    data class SetLoading(val loading: Boolean) : LegalDocumentMutation
    data class SetError(val error: StringKey?) : LegalDocumentMutation
}
