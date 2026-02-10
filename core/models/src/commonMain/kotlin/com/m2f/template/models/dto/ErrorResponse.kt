package com.m2f.template.models.dto

import kotlinx.serialization.Serializable

/**
 * Standard API error response body.
 * Used by server to format errors and by client to parse them.
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: List<String> = emptyList()
)
