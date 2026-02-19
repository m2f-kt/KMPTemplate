package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AddMemberRequest
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.dto.GroupResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.dto.PaginatedMemberResponse
import com.m2f.template.models.dto.RegisterMemberRequest
import com.m2f.template.models.dto.UpdateGroupRequest

/**
 * SDK functions for all group endpoints.
 *
 * All functions return [Either<AppError, T>] for consistent error handling.
 * Auth token is automatically attached by the [AuthInterceptor].
 */
interface GroupApi {
    suspend fun createGroup(request: CreateGroupRequest): Either<AppError, GroupResponse>
    suspend fun getGroup(groupId: String): Either<AppError, GroupResponse>
    suspend fun updateGroup(groupId: String, request: UpdateGroupRequest): Either<AppError, GroupResponse>
    suspend fun deleteGroup(groupId: String): Either<AppError, Unit>
    suspend fun listAllGroups(): Either<AppError, List<GroupResponse>>
    suspend fun getMembers(groupId: String, cursor: String? = null, limit: Int = 20): Either<AppError, PaginatedMemberResponse>
    suspend fun addMember(groupId: String, request: AddMemberRequest): Either<AppError, MemberResponse>
    suspend fun removeMember(groupId: String, userId: String): Either<AppError, Unit>
    suspend fun registerMember(groupId: String, request: RegisterMemberRequest): Either<AppError, MemberResponse>
}
