@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.service

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.m2f.core.config.server.DomainError
import com.m2f.server.auth.repository.UserRepository
import com.m2f.server.auth.service.EmailService
import com.m2f.server.groups.errors.GroupForbidden
import com.m2f.server.groups.errors.GroupNotFound
import com.m2f.server.groups.errors.InvitationAlreadyAccepted
import com.m2f.server.groups.errors.InvitationExpired
import com.m2f.server.groups.errors.InvitationNotFound
import com.m2f.server.groups.errors.MemberAlreadyInGroup
import com.m2f.server.groups.repository.GroupRepository
import com.m2f.server.groups.repository.InvitationRecord
import com.m2f.server.groups.repository.InvitationRepository
import com.m2f.server.groups.repository.MembershipRepository
import com.m2f.template.models.GroupRole
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.AcceptInvitationResponse
import com.m2f.template.models.dto.CreateInvitationRequest
import com.m2f.template.models.dto.InvitationResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Business logic for group invitation operations.
 * Uses Arrow Raise for error handling -- zero try/catch for domain errors.
 *
 * Authorization:
 * - Creating invitations requires ADMIN or OWNER in group, or PowerAdmin
 * - Viewing invitations is public (by token) so recipients can see invite details
 * - Accepting invitations requires authentication
 */
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val groupRepository: GroupRepository,
    private val membershipRepository: MembershipRepository,
    private val userRepository: UserRepository,
    private val emailService: EmailService,
) {

    companion object {
        /** Default invitation expiry period. */
        private val INVITATION_EXPIRY = 7.days
    }

    /**
     * Create an invitation for a user to join a group.
     * Requires ADMIN or OWNER in group, or PowerAdmin.
     * Sends an email to the invitee with the invitation link.
     */
    context(raise: Raise<DomainError>)
    suspend fun createInvitation(
        groupId: String,
        request: CreateInvitationRequest,
        userId: String,
        userRole: UserRole,
    ): InvitationResponse {
        val gid = Uuid.parse(groupId)
        val uid = Uuid.parse(userId)

        val group = groupRepository.findById(gid)
        raise.ensure(group != null) { GroupNotFound() }

        // Authorization: ADMIN/OWNER or PowerAdmin
        if (userRole != UserRole.PowerAdmin) {
            requireGroupRole(uid, gid, GroupRole.Admin)
        }

        // Check if user is already a member by email
        val existingUser = userRepository.findByEmail(request.email)
        if (existingUser != null) {
            val existingMembership = membershipRepository.findByUserAndGroup(existingUser.id, gid)
            raise.ensure(existingMembership == null) { MemberAlreadyInGroup() }
        }

        // Calculate expiry
        val now = Clock.System.now()
        val expiresAt = (now + INVITATION_EXPIRY).toLocalDateTime(TimeZone.UTC)

        // Create invitation
        val invitation = invitationRepository.create(
            groupId = gid,
            email = request.email,
            invitedBy = uid,
            role = GroupRole.Member.value,
            expiresAt = expiresAt,
        )

        // Fetch inviter details for email
        val inviter = userRepository.findById(uid)
        val inviterName = inviter?.name ?: "A team member"

        // Send invitation email
        sendInvitationEmail(
            toEmail = request.email,
            groupName = group.name,
            inviterName = inviterName,
            token = invitation.token,
        )

        return invitation.toResponse(groupName = group.name, inviterName = inviterName)
    }

    /**
     * Get invitation details by token.
     * Public endpoint -- no authentication required.
     * Allows potential invitees to view invitation details before accepting.
     */
    context(raise: Raise<DomainError>)
    suspend fun getInvitation(token: String): InvitationResponse {
        val invitation = invitationRepository.findByToken(token)
        raise.ensure(invitation != null) { InvitationNotFound() }

        val group = groupRepository.findById(invitation.groupId)
        raise.ensure(group != null) { GroupNotFound() }

        val inviter = userRepository.findById(invitation.invitedBy)
        val inviterName = inviter?.name ?: "A team member"

        return invitation.toResponse(groupName = group.name, inviterName = inviterName)
    }

    /**
     * Accept an invitation and join the group.
     * Requires authentication. The accepting user joins with the role specified in the invitation.
     */
    context(raise: Raise<DomainError>)
    suspend fun acceptInvitation(
        request: AcceptInvitationRequest,
        userId: String,
    ): AcceptInvitationResponse {
        val uid = Uuid.parse(userId)

        val invitation = invitationRepository.findByToken(request.token)
        raise.ensure(invitation != null) { InvitationNotFound() }

        // Check if already accepted
        raise.ensure(invitation.acceptedAt == null) { InvitationAlreadyAccepted() }

        // Check if expired
        val nowLocal = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        raise.ensure(nowLocal < invitation.expiresAt) { InvitationExpired() }

        val group = groupRepository.findById(invitation.groupId)
        raise.ensure(group != null) { GroupNotFound() }

        // Check if user is already a member
        val existingMembership = membershipRepository.findByUserAndGroup(uid, invitation.groupId)
        raise.ensure(existingMembership == null) { MemberAlreadyInGroup() }

        // Add user to group
        membershipRepository.insert(uid, invitation.groupId, invitation.role)

        // Mark invitation as accepted
        invitationRepository.markAccepted(request.token, uid)

        return AcceptInvitationResponse(
            groupId = invitation.groupId.toString(),
            groupSlug = group.slug,
            role = invitation.role,
        )
    }

    // ---- Helpers ----

    /**
     * Verify that the user has at least the specified role in the group.
     */
    context(raise: Raise<DomainError>)
    private suspend fun requireGroupRole(userId: Uuid, groupId: Uuid, minRole: GroupRole) {
        val membership = membershipRepository.findByUserAndGroup(userId, groupId)
        raise.ensure(membership != null) { GroupForbidden() }
        val actualRole = GroupRole.fromString(membership.role)
        raise.ensure(actualRole.level >= minRole.level) { GroupForbidden() }
    }

    /**
     * Send invitation email to the invitee.
     */
    private suspend fun sendInvitationEmail(
        toEmail: String,
        groupName: String,
        inviterName: String,
        token: String,
    ) {
        val subject = "You've been invited to join $groupName"
        val body = """
            |Hi,
            |
            |$inviterName has invited you to join $groupName.
            |
            |Click the link below to accept the invitation:
            |https://app.example.com/invitations/$token
            |
            |This invitation will expire in 7 days.
            |
            |If you didn't expect this invitation, you can safely ignore this email.
            |
            |Best regards,
            |The Team
        """.trimMargin()

        emailService.sendEmail(toEmail, subject, body)
    }
}

private fun InvitationRecord.toResponse(groupName: String, inviterName: String): InvitationResponse {
    val nowLocal = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    return InvitationResponse(
        id = id.toString(),
        token = token,
        groupId = groupId.toString(),
        groupName = groupName,
        email = email,
        inviterName = inviterName,
        role = role,
        expiresAt = expiresAt.toString(),
        isExpired = nowLocal >= expiresAt,
        isAccepted = acceptedAt != null,
    )
}
