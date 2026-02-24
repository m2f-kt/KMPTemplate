package com.m2f.template.app.documents

import com.m2f.template.models.dto.DocumentResponse
import com.m2f.template.models.localization.StringKey

sealed interface DocumentsMutation {
    data class SetLoading(val isLoading: Boolean) : DocumentsMutation
    data class SetDocuments(val documents: List<DocumentResponse>, val groupId: String) : DocumentsMutation
    data class SetUploading(val isUploading: Boolean) : DocumentsMutation
    data class AddDocument(val document: DocumentResponse) : DocumentsMutation
    data class RemoveDocument(val documentId: String) : DocumentsMutation
    data class SetError(val error: StringKey?) : DocumentsMutation
}
