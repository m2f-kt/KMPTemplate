package com.m2f.server.groups.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.AcceptInvitationResponse
import com.m2f.template.models.dto.CreateInvitationRequest
import com.m2f.template.models.dto.InvitationResponse

/**
 * Business logic for group invitation operations.
 */
interface InvitationService {
    context(raise: Raise<DomainError>)
    suspend fun createInvitation(groupId: String, request: CreateInvitationRequest, userId: String, userRole: UserRole): InvitationResponse

    context(raise: Raise<DomainError>)
    suspend fun getInvitation(token: String): InvitationResponse

    context(raise: Raise<DomainError>)
    suspend fun acceptInvitation(request: AcceptInvitationRequest, userId: String): AcceptInvitationResponse

    context(raise: Raise<DomainError>)
    suspend fun listInvitations(groupId: String, userId: String, userRole: UserRole): List<InvitationResponse>

    context(raise: Raise<DomainError>)
    suspend fun revokeInvitation(groupId: String, invitationId: String, userId: String, userRole: UserRole): Map<String, String>

    context(raise: Raise<DomainError>)
    suspend fun resendInvitation(groupId: String, invitationId: String, userId: String, userRole: UserRole): InvitationResponse
}
