package com.m2f.template.models.dto

import kotlinx.serialization.Serializable

/**
 * Server response representing an uploaded file.
 */
@Serializable
data class FileResponse(
    val id: String,
    val key: String,
    val originalName: String,
    val contentType: String,
    val size: Long,
    val url: String,
    val createdAt: String,
)
