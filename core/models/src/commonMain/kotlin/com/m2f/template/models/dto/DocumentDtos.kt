package com.m2f.template.models.dto

import kotlinx.serialization.Serializable

/**
 * Request metadata for uploading a document.
 * Sent as form fields alongside the file in a multipart upload.
 */
@Serializable
data class DocumentUploadRequest(
    val groupId: String,
    val scope: String,
    val assignedToUserId: String? = null,
)

/**
 * Server response representing a document and its ingestion status.
 */
@Serializable
data class DocumentResponse(
    val id: String,
    val groupId: String,
    val name: String,
    val contentType: String,
    val scope: String,
    val status: String,
    val chunkCount: Int,
    val assignedToUserId: String?,
    val createdAt: String,
)

/**
 * Wrapper response for a list of documents.
 */
@Serializable
data class DocumentListResponse(
    val documents: List<DocumentResponse>,
)
