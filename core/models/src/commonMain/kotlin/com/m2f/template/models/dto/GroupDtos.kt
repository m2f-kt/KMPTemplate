package com.m2f.template.models.dto

import com.m2f.template.models.GroupRole
import kotlinx.serialization.Serializable

/**
 * Server response representing a group.
 */
@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val createdBy: String,
    val memberCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

/**
 * Request to create a new group.
 */
@Serializable
data class CreateGroupRequest(
    val name: String,
    val slug: String,
    val description: String = "",
)

/**
 * Request to update an existing group. All fields are optional for partial updates.
 */
@Serializable
data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
)

/**
 * Request to add an existing user to a group.
 */
@Serializable
data class AddMemberRequest(
    val userId: String,
    val role: GroupRole = GroupRole.Member,
)

/**
 * Request to register a new user and add them directly to a group.
 */
@Serializable
data class RegisterMemberRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: GroupRole = GroupRole.Member,
)

/**
 * Lightweight summary of a user's membership in a group.
 * Used by the client to determine the user's role in each group (e.g. for role-gated navigation).
 */
@Serializable
data class MembershipSummary(
    val groupId: String,
    val groupName: String,
    val groupRole: GroupRole,
)

/**
 * Server response representing a group member.
 */
@Serializable
data class MemberResponse(
    val userId: String,
    val email: String,
    val name: String,
    val role: GroupRole,
    val joinedAt: String,
)

/**
 * Paginated response for member lists with cursor-based pagination.
 *
 * [cursor] is an opaque string (encoded last-seen ID) for requesting the next page.
 * [hasMore] indicates whether additional pages exist.
 */
@Serializable
data class PaginatedMemberResponse(
    val items: List<MemberResponse>,
    val cursor: String? = null,
    val hasMore: Boolean = false,
)
