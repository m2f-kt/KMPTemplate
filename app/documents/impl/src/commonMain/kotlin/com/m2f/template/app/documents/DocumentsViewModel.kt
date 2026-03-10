package com.m2f.template.app.documents

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import kotlinx.coroutines.launch

class DocumentsViewModel(
    private val sdk: Sdk,
) : MviViewModel<DocumentsIntent, DocumentsModel, DocumentsMutation, DocumentsEvent>(
    initialState = DocumentsModel(),
) {

    override fun take(intent: DocumentsIntent) {
        viewModelScope.launch {
            when (intent) {
                is DocumentsIntent.LoadDocuments -> handleLoadDocuments(intent.groupId)
                is DocumentsIntent.UploadFile -> handleUploadFile(intent)
                is DocumentsIntent.DeleteDocument -> handleDeleteDocument(intent.documentId)
                is DocumentsIntent.RefreshDocuments -> {
                    model.value.groupId?.let { handleLoadDocuments(it) }
                }
            }
        }
    }

    override suspend fun reduce(model: DocumentsModel, mutation: DocumentsMutation): DocumentsModel =
        when (mutation) {
            is DocumentsMutation.SetLoading -> model.copy(isLoading = mutation.isLoading)
            is DocumentsMutation.SetDocuments -> model.copy(
                documents = mutation.documents,
                groupId = mutation.groupId,
                isLoading = false,
                error = null,
            )
            is DocumentsMutation.SetUploading -> model.copy(isUploading = mutation.isUploading)
            is DocumentsMutation.AddDocument -> model.copy(
                documents = model.documents + mutation.document,
                isUploading = false,
            )
            is DocumentsMutation.RemoveDocument -> model.copy(
                documents = model.documents.filter { it.id != mutation.documentId },
            )
            is DocumentsMutation.SetError -> model.copy(error = mutation.error, isLoading = false, isUploading = false)
        }

    private suspend fun handleLoadDocuments(groupId: String) {
        sendMutation(DocumentsMutation.SetLoading(true))
        sdk.listDocuments(groupId).fold(
            ifLeft = { error ->
                sendMutation(DocumentsMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
            },
            ifRight = { response ->
                sendMutation(DocumentsMutation.SetDocuments(response.documents, groupId))
            },
        )
    }

    private suspend fun handleUploadFile(intent: DocumentsIntent.UploadFile) {
        sendMutation(DocumentsMutation.SetUploading(true))
        sdk.uploadDocument(
            groupId = intent.groupId,
            scope = "personal",
            fileName = intent.fileName,
            fileBytes = intent.fileBytes,
            contentType = intent.contentType,
        ).fold(
            ifLeft = { error ->
                sendMutation(DocumentsMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
            },
            ifRight = { document ->
                sendMutation(DocumentsMutation.AddDocument(document))
                sendEvent(DocumentsEvent.UploadSuccess)
            },
        )
    }

    private suspend fun handleDeleteDocument(documentId: String) {
        sdk.deleteDocument(documentId).fold(
            ifLeft = { error ->
                sendMutation(DocumentsMutation.SetError(StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR))
            },
            ifRight = {
                sendMutation(DocumentsMutation.RemoveDocument(documentId))
            },
        )
    }
}
