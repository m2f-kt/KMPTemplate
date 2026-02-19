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
import com.m2f.template.models.routes.Groups
import com.m2f.template.sdk.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GroupApiImpl(private val client: HttpClient) : GroupApi {

    override suspend fun createGroup(request: CreateGroupRequest): Either<AppError, GroupResponse> =
        apiCall {
            client.post(Groups.Create()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun getGroup(groupId: String): Either<AppError, GroupResponse> =
        apiCall { client.get(Groups.ById(groupId = groupId)) }

    override suspend fun updateGroup(
        groupId: String,
        request: UpdateGroupRequest,
    ): Either<AppError, GroupResponse> =
        apiCall {
            client.post(Groups.Update(groupId = groupId)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun deleteGroup(groupId: String): Either<AppError, Unit> =
        apiCall { client.post(Groups.Delete(groupId = groupId)) }

    override suspend fun listAllGroups(): Either<AppError, List<GroupResponse>> =
        apiCall { client.get(Groups.ListAll()) }

    override suspend fun getMembers(
        groupId: String,
        cursor: String?,
        limit: Int,
    ): Either<AppError, PaginatedMemberResponse> =
        apiCall {
            client.get(Groups.Members(groupId = groupId, cursor = cursor, limit = limit))
        }

    override suspend fun addMember(
        groupId: String,
        request: AddMemberRequest,
    ): Either<AppError, MemberResponse> =
        apiCall {
            client.post(Groups.AddMember(groupId = groupId)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun removeMember(groupId: String, userId: String): Either<AppError, Unit> =
        apiCall { client.post(Groups.RemoveMember(groupId = groupId, userId = userId)) }

    override suspend fun registerMember(
        groupId: String,
        request: RegisterMemberRequest,
    ): Either<AppError, MemberResponse> =
        apiCall {
            client.post(Groups.RegisterMember(groupId = groupId)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
}
