@file:OptIn(ExperimentalUuidApi::class)

package com.m2f.server.groups.service

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.m2f.core.config.server.DomainError
import com.m2f.server.auth.contract.errors.UserNotFound
import com.m2f.server.auth.contract.repository.UserRepository
import com.m2f.server.auth.contract.service.AuthService
import com.m2f.server.groups.contract.errors.CannotRemoveOwner
import com.m2f.server.groups.contract.errors.GroupAlreadyExists
import com.m2f.server.groups.contract.errors.GroupForbidden
import com.m2f.server.groups.contract.errors.GroupNotFound
import com.m2f.server.groups.contract.errors.MemberAlreadyInGroup
import com.m2f.server.groups.contract.errors.MemberNotInGroup
import com.m2f.server.groups.contract.repository.MembershipRepository
import com.m2f.server.groups.contract.service.GroupService
import com.m2f.server.groups.repository.GroupRecord
import com.m2f.server.groups.repository.GroupRepository
import com.m2f.template.models.GroupRole
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AddMemberRequest
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.dto.GroupResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.dto.MembershipSummary
import com.m2f.template.models.dto.PaginatedMemberResponse
import com.m2f.template.models.dto.RegisterMemberRequest
import com.m2f.template.models.dto.RegisterRequest
import com.m2f.template.models.dto.UpdateGroupRequest
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Business logic for group operations with RBAC enforcement.
 * Uses Arrow Raise for error handling -- zero try/catch for domain errors.
 *
 * Authorization hierarchy:
 * - System PowerAdmin bypasses all group-level checks
 * - Group Owner > Group Admin > Group Member
 */
class GroupServiceImpl(
    private val groupRepository: GroupRepository,
    private val membershipRepository: MembershipRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService,
) : GroupService {

    // ---- Group CRUD ----

    /**
     * Create a new group. Caller becomes the OWNER.
     */
    context(raise: Raise<DomainError>)
    override suspend fun createGroup(request: CreateGroupRequest, userId: String): GroupResponse {
        val uid = Uuid.parse(userId)

        // Check slug uniqueness
        raise.ensure(groupRepository.findBySlug(request.slug) == null) { GroupAlreadyExists() }

        // Insert group
        val groupId = groupRepository.insert(
            name = request.name,
            slug = request.slug,
            description = request.description,
            createdBy = uid,
        )

        // Creator becomes OWNER
        membershipRepository.insert(uid, groupId, GroupRole.Owner.value)

        // Fetch and return
        val group = groupRepository.findById(groupId)
        raise.ensure(group != null) { GroupNotFound() }
        return group.toGroupResponse(memberCount = 1)
    }

    /**
     * Get a group by ID. Members can read their own group. PowerAdmin can read any group.
     */
    context(raise: Raise<DomainError>)
    override suspend fun getGroup(groupId: String, userId: String, userRole: UserRole): GroupResponse {
        val gid = Uuid.parse(groupId)
        val uid = Uuid.parse(userId)

        val group = groupRepository.findById(gid)
        raise.ensure(group != null) { GroupNotFound() }

        // Authorization: PowerAdmin can access any group; others must be members
        if (userRole != UserRole.PowerAdmin) {
            val membership = membershipRepository.findByUserAndGroup(uid, gid)
            raise.ensure(membership != null) { GroupForbidden() }
        }

        val memberCount = membershipRepository.countByGroup(gid)
        return group.toGroupResponse(memberCount = memberCount.toInt())
    }

    /**
     * Update a group. Requires ADMIN or OWNER in group, or PowerAdmin.
     */
    context(raise: Raise<DomainError>)
    override suspend fun updateGroup(
        groupId: String,
        request: UpdateGroupRequest,
        userId: String,
        userRole: UserRole,
    ): GroupResponse {
        val gid = Uuid.parse(groupId)
        val uid = Uuid.parse(userId)

        val group = groupRepository.findById(gid)
        raise.ensure(group != null) { GroupNotFound() }

        // Authorization
        if (userRole != UserRole.PowerAdmin) {
            requireGroupRole(uid, gid, GroupRole.Admin)
        }

        groupRepository.update(gid, request.name, request.description)

        val updated = groupRepository.findById(gid)
        raise.ensure(updated != null) { GroupNotFound() }
        val memberCount = membershipRepository.countByGroup(gid)
        return updated.toGroupResponse(memberCount = memberCount.toInt())
    }

    /**
     * Delete a group. Requires OWNER of the group, or PowerAdmin.
     */
    context(raise: Raise<DomainError>)
    override suspend fun deleteGroup(groupId: String, userId: String, userRole: UserRole) {
        val gid = Uuid.parse(groupId)
        val uid = Uuid.parse(userId)

        val group = groupRepository.findById(gid)
        raise.ensure(group != null) { GroupNotFound() }

        // Authorization: Only OWNER or PowerAdmin can delete
        if (userRole != UserRole.PowerAdmin) {
            requireGroupRole(uid, gid, GroupRole.Owner)
        }

        // Delete memberships first (no ON DELETE CASCADE on FK)
        membershipRepository.deleteByGroup(gid)
        groupRepository.delete(gid)
    }

    /**
     * List all groups. PowerAdmin only (enforced at route level).
     */
    context(raise: Raise<DomainError>)
    override suspend fun listAllGroups(): List<GroupResponse> {
        val groups = groupRepository.listAll()
        return groups.map { group ->
            val memberCount = membershipRepository.countByGroup(group.id)
            group.toGroupResponse(memberCount = memberCount.toInt())
        }
    }

    // ---- Member Management ----

    /**
     * Get all group memberships for the current user.
     * Returns a lightweight summary with groupId, groupName, and GroupRole.
     * No RBAC check needed -- users can always see their own memberships.
     */
    override suspend fun getMyMemberships(userId: String): List<MembershipSummary> {
        val uid = Uuid.parse(userId)
        val memberships = membershipRepository.findByUserId(uid)
        return memberships.map { record ->
            val group = groupRepository.findById(record.groupId)
            MembershipSummary(
                groupId = record.groupId.toString(),
                groupName = group?.name ?: "",
                groupRole = GroupRole.fromString(record.role),
            )
        }
    }

    /**
     * List members of a group with cursor-based pagination.
     * Requires ADMIN or OWNER in group, or PowerAdmin. Regular members cannot list all members.
     */
    context(raise: Raise<DomainError>)
    override suspend fun getMembers(
        groupId: String,
        userId: String,
        userRole: UserRole,
        cursor: String?,
        limit: Int,
    ): PaginatedMemberResponse {
        val gid = Uuid.parse(groupId)
        val uid = Uuid.parse(userId)

        val group = groupRepository.findById(gid)
        raise.ensure(group != null) { GroupNotFound() }

        // Authorization: ADMIN/OWNER or PowerAdmin
        if (userRole != UserRole.PowerAdmin) {
            requireGroupRole(uid, gid, GroupRole.Admin)
        }

        val cursorUuid = cursor?.let { Uuid.parse(it) }
        val members = membershipRepository.listByGroupWithUsers(gid, cursorUuid, limit)

        val items = members.map { record ->
            MemberResponse(
                userId = record.userId.toString(),
                email = record.email,
                name = record.name,
                role = GroupRole.fromString(record.role),
                joinedAt = record.joinedAt.toString(),
            )
        }

        val nextCursor = if (items.size == limit) items.lastOrNull()?.userId else null
        val hasMore = items.size == limit

        return PaginatedMemberResponse(
            items = items,
            cursor = nextCursor,
            hasMore = hasMore,
        )
    }

    /**
     * Add an existing user to a group.
     * Requires ADMIN or OWNER in group, or PowerAdmin.
     */
    context(raise: Raise<DomainError>)
    override suspend fun addMember(
        groupId: String,
        request: AddMemberRequest,
        userId: String,
        userRole: UserRole,
    ): MemberResponse {
        val gid = Uuid.parse(groupId)
        val uid = Uuid.parse(userId)
        val targetUid = Uuid.parse(request.userId)

        val group = groupRepository.findById(gid)
        raise.ensure(group != null) { GroupNotFound() }

        // Authorization
        if (userRole != UserRole.PowerAdmin) {
            requireGroupRole(uid, gid, GroupRole.Admin)
        }

        // Check target user exists
        val targetUser = userRepository.findById(targetUid)
        raise.ensure(targetUser != null) { UserNotFound() }

        // Check not already a member
        raise.ensure(membershipRepository.findByUserAndGroup(targetUid, gid) == null) { MemberAlreadyInGroup() }

        // Insert membership
        membershipRepository.insert(targetUid, gid, request.role.value)

        return MemberResponse(
            userId = targetUser.id.toString(),
            email = targetUser.email,
            name = targetUser.name,
            role = request.role,
            joinedAt = "", // Will be set by DB default; client can re-fetch
        )
    }

    /**
     * Remove a member from a group.
     * Requires ADMIN or OWNER in group, or PowerAdmin. Cannot remove the OWNER.
     */
    context(raise: Raise<DomainError>)
    override suspend fun removeMember(
        groupId: String,
        targetUserId: String,
        userId: String,
        userRole: UserRole,
    ) {
        val gid = Uuid.parse(groupId)
        val uid = Uuid.parse(userId)
        val targetUid = Uuid.parse(targetUserId)

        val group = groupRepository.findById(gid)
        raise.ensure(group != null) { GroupNotFound() }

        // Authorization
        if (userRole != UserRole.PowerAdmin) {
            requireGroupRole(uid, gid, GroupRole.Admin)
        }

        // Check target is a member
        val targetMembership = membershipRepository.findByUserAndGroup(targetUid, gid)
        raise.ensure(targetMembership != null) { MemberNotInGroup() }

        // Cannot remove OWNER
        raise.ensure(targetMembership.role != GroupRole.Owner.value) { CannotRemoveOwner() }

        membershipRepository.delete(targetUid, gid)
    }

    /**
     * Register a new user and add them directly to a group.
     * Requires ADMIN or OWNER in group, or PowerAdmin.
     */
    context(raise: Raise<DomainError>)
    override suspend fun registerMember(
        groupId: String,
        request: RegisterMemberRequest,
        userId: String,
        userRole: UserRole,
    ): MemberResponse {
        val gid = Uuid.parse(groupId)
        val uid = Uuid.parse(userId)

        val group = groupRepository.findById(gid)
        raise.ensure(group != null) { GroupNotFound() }

        // Authorization
        if (userRole != UserRole.PowerAdmin) {
            requireGroupRole(uid, gid, GroupRole.Admin)
        }

        // Register the user via AuthService
        val registerRequest = RegisterRequest(
            email = request.email,
            password = request.password,
            firstName = request.firstName,
            lastName = request.lastName,
        )
        authService.register(registerRequest)

        // Look up the newly created user by email to get their ID
        val newUser = userRepository.findByEmail(request.email)
        raise.ensure(newUser != null) { UserNotFound() }

        // Add to group with specified role
        membershipRepository.insert(newUser.id, gid, request.role.value)

        return MemberResponse(
            userId = newUser.id.toString(),
            email = newUser.email,
            name = newUser.name,
            role = request.role,
            joinedAt = "",
        )
    }

    // ---- Helpers ----

    /**
     * Verify that the user has at least the specified role in the group.
     * Uses [GroupRole.level] for comparison: Owner(2) > Admin(1) > Member(0).
     */
    context(raise: Raise<DomainError>)
    private suspend fun requireGroupRole(userId: Uuid, groupId: Uuid, minRole: GroupRole) {
        val membership = membershipRepository.findByUserAndGroup(userId, groupId)
        raise.ensure(membership != null) { GroupForbidden() }
        val actualRole = GroupRole.fromString(membership.role)
        raise.ensure(actualRole.level >= minRole.level) { GroupForbidden() }
    }
}

private fun GroupRecord.toGroupResponse(memberCount: Int): GroupResponse = GroupResponse(
    id = id.toString(),
    name = name,
    slug = slug,
    description = description,
    createdBy = createdBy.toString(),
    memberCount = memberCount,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)
