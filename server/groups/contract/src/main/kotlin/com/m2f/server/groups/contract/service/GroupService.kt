package com.m2f.server.groups.contract.service

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.template.models.UserRole
import com.m2f.template.models.dto.AddMemberRequest
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.dto.GroupResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.dto.MembershipSummary
import com.m2f.template.models.dto.PaginatedMemberResponse
import com.m2f.template.models.dto.RegisterMemberRequest
import com.m2f.template.models.dto.UpdateGroupRequest

/**
 * Business logic for group operations with RBAC enforcement.
 */
interface GroupService {
    context(raise: Raise<DomainError>)
    suspend fun createGroup(request: CreateGroupRequest, userId: String): GroupResponse

    context(raise: Raise<DomainError>)
    suspend fun getGroup(groupId: String, userId: String, userRole: UserRole): GroupResponse

    context(raise: Raise<DomainError>)
    suspend fun updateGroup(groupId: String, request: UpdateGroupRequest, userId: String, userRole: UserRole): GroupResponse

    context(raise: Raise<DomainError>)
    suspend fun deleteGroup(groupId: String, userId: String, userRole: UserRole)

    context(raise: Raise<DomainError>)
    suspend fun listAllGroups(): List<GroupResponse>

    suspend fun getMyMemberships(userId: String): List<MembershipSummary>

    context(raise: Raise<DomainError>)
    suspend fun getMembers(groupId: String, userId: String, userRole: UserRole, cursor: String?, limit: Int): PaginatedMemberResponse

    context(raise: Raise<DomainError>)
    suspend fun addMember(groupId: String, request: AddMemberRequest, userId: String, userRole: UserRole): MemberResponse

    context(raise: Raise<DomainError>)
    suspend fun removeMember(groupId: String, targetUserId: String, userId: String, userRole: UserRole)

    context(raise: Raise<DomainError>)
    suspend fun registerMember(groupId: String, request: RegisterMemberRequest, userId: String, userRole: UserRole): MemberResponse
}
