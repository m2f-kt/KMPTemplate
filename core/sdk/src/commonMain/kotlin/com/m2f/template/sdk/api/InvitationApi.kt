package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.AcceptInvitationResponse
import com.m2f.template.models.dto.CreateInvitationRequest
import com.m2f.template.models.dto.InvitationResponse

/**
 * SDK functions for group invitation endpoints.
 *
 * All functions return [Either<AppError, T>] for consistent error handling.
 * Auth token is automatically attached by the [AuthInterceptor] for authenticated endpoints.
 */
interface InvitationApi {
    /**
     * Create an invitation for a user to join a group.
     * Requires ADMIN or OWNER role in the group.
     */
    suspend fun createInvitation(groupId: String, request: CreateInvitationRequest): Either<AppError, InvitationResponse>

    /**
     * Get invitation details by token.
     * Public endpoint -- no authentication required.
     */
    suspend fun getInvitation(token: String): Either<AppError, InvitationResponse>

    /**
     * Accept an invitation and join the group.
     * Requires authentication.
     */
    suspend fun acceptInvitation(request: AcceptInvitationRequest): Either<AppError, AcceptInvitationResponse>

    /**
     * List all invitations for a group.
     * Requires ADMIN or OWNER role in the group.
     */
    suspend fun listInvitations(groupId: String): Either<AppError, List<InvitationResponse>>

    /**
     * Revoke a pending invitation.
     * Requires ADMIN or OWNER role in the group.
     */
    suspend fun revokeInvitation(groupId: String, invitationId: String): Either<AppError, Unit>
}
