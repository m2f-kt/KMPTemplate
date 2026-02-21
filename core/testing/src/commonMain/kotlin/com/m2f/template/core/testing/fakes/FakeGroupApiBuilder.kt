package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AddMemberRequest
import com.m2f.template.models.dto.CreateGroupRequest
import com.m2f.template.models.dto.GroupResponse
import com.m2f.template.models.dto.MemberResponse
import com.m2f.template.models.dto.PaginatedMemberResponse
import com.m2f.template.models.dto.RegisterMemberRequest
import com.m2f.template.models.dto.UpdateGroupRequest
import com.m2f.template.sdk.api.GroupApi

/**
 * DSL builder for creating fake [GroupApi] instances in tests.
 *
 * Each method defaults to returning `Either.Left(AppError.Client.Unknown())`
 * so that unconfigured code paths fail fast instead of silently succeeding.
 *
 * Usage:
 * ```kotlin
 * val groupApi = fakeGroupApi {
 *     createGroup { Either.Right(GroupResponse(...)) }
 * }
 * ```
 */
@FakeSDKDsl
class FakeGroupApiBuilder {

    private var _createGroup: suspend (CreateGroupRequest) -> Either<AppError, GroupResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getGroup: suspend (String) -> Either<AppError, GroupResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _updateGroup: suspend (String, UpdateGroupRequest) -> Either<AppError, GroupResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    private var _deleteGroup: suspend (String) -> Either<AppError, Unit> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _listAllGroups: suspend () -> Either<AppError, List<GroupResponse>> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _getMembers: suspend (String, String?, Int) -> Either<AppError, PaginatedMemberResponse> =
        { _, _, _ -> Either.Left(AppError.Client.Unknown()) }

    private var _addMember: suspend (String, AddMemberRequest) -> Either<AppError, MemberResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    private var _removeMember: suspend (String, String) -> Either<AppError, Unit> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    private var _registerMember: suspend (String, RegisterMemberRequest) -> Either<AppError, MemberResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    fun createGroup(behavior: suspend (CreateGroupRequest) -> Either<AppError, GroupResponse>) {
        _createGroup = behavior
    }

    fun getGroup(behavior: suspend (String) -> Either<AppError, GroupResponse>) {
        _getGroup = behavior
    }

    fun updateGroup(behavior: suspend (String, UpdateGroupRequest) -> Either<AppError, GroupResponse>) {
        _updateGroup = behavior
    }

    fun deleteGroup(behavior: suspend (String) -> Either<AppError, Unit>) {
        _deleteGroup = behavior
    }

    fun listAllGroups(behavior: suspend () -> Either<AppError, List<GroupResponse>>) {
        _listAllGroups = behavior
    }

    fun getMembers(behavior: suspend (String, String?, Int) -> Either<AppError, PaginatedMemberResponse>) {
        _getMembers = behavior
    }

    fun addMember(behavior: suspend (String, AddMemberRequest) -> Either<AppError, MemberResponse>) {
        _addMember = behavior
    }

    fun removeMember(behavior: suspend (String, String) -> Either<AppError, Unit>) {
        _removeMember = behavior
    }

    fun registerMember(behavior: suspend (String, RegisterMemberRequest) -> Either<AppError, MemberResponse>) {
        _registerMember = behavior
    }

    internal fun build(): GroupApi = object : GroupApi {
        override suspend fun createGroup(request: CreateGroupRequest): Either<AppError, GroupResponse> =
            _createGroup(request)

        override suspend fun getGroup(groupId: String): Either<AppError, GroupResponse> =
            _getGroup(groupId)

        override suspend fun updateGroup(
            groupId: String,
            request: UpdateGroupRequest,
        ): Either<AppError, GroupResponse> =
            _updateGroup(groupId, request)

        override suspend fun deleteGroup(groupId: String): Either<AppError, Unit> =
            _deleteGroup(groupId)

        override suspend fun listAllGroups(): Either<AppError, List<GroupResponse>> =
            _listAllGroups()

        override suspend fun getMembers(
            groupId: String,
            cursor: String?,
            limit: Int,
        ): Either<AppError, PaginatedMemberResponse> =
            _getMembers(groupId, cursor, limit)

        override suspend fun addMember(
            groupId: String,
            request: AddMemberRequest,
        ): Either<AppError, MemberResponse> =
            _addMember(groupId, request)

        override suspend fun removeMember(groupId: String, userId: String): Either<AppError, Unit> =
            _removeMember(groupId, userId)

        override suspend fun registerMember(
            groupId: String,
            request: RegisterMemberRequest,
        ): Either<AppError, MemberResponse> =
            _registerMember(groupId, request)
    }
}
