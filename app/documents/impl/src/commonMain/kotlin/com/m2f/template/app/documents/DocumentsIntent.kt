package com.m2f.template.app.documents

sealed interface DocumentsIntent {
    data class LoadDocuments(val groupId: String) : DocumentsIntent
    data class UploadFile(
        val groupId: String,
        val fileName: String,
        val fileBytes: ByteArray,
        val contentType: String,
    ) : DocumentsIntent
    data class DeleteDocument(val documentId: String) : DocumentsIntent
    data object RefreshDocuments : DocumentsIntent
}
