package com.m2f.template.models.dto

import kotlinx.serialization.Serializable

/**
 * Request to create a group invitation.
 * The invited user will receive an email with a unique token link.
 */
@Serializable
data class CreateInvitationRequest(
    val email: String,
)

/**
 * Server response representing a group invitation.
 * Contains metadata for displaying invitation details and status.
 * Note: Token is intentionally excluded from API responses for security.
 * The invitation link is only sent via email to the invitee.
 */
@Serializable
data class InvitationResponse(
    val id: String,
    val groupId: String,
    val groupName: String,
    val email: String,
    val inviterName: String,
    val role: String,
    val expiresAt: String,
    val isExpired: Boolean,
    val isAccepted: Boolean,
)

/**
 * Request to accept a group invitation.
 * The token identifies the specific invitation.
 */
@Serializable
data class AcceptInvitationRequest(
    val token: String,
)

/**
 * Server response after successfully accepting an invitation.
 * Contains the group details the user just joined.
 */
@Serializable
data class AcceptInvitationResponse(
    val groupId: String,
    val groupSlug: String,
    val role: String,
)
