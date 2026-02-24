package com.m2f.template.app.documents

import com.m2f.template.models.dto.DocumentResponse
import com.m2f.template.models.localization.StringKey

data class DocumentsModel(
    val isLoading: Boolean = false,
    val documents: List<DocumentResponse> = emptyList(),
    val isUploading: Boolean = false,
    val error: StringKey? = null,
    val groupId: String? = null,
)
