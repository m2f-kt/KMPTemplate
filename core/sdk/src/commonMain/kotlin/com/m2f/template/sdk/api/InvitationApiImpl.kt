package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.AcceptInvitationResponse
import com.m2f.template.models.dto.CreateInvitationRequest
import com.m2f.template.models.dto.InvitationResponse
import com.m2f.template.models.routes.Groups
import com.m2f.template.models.routes.Invitations
import com.m2f.template.sdk.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class InvitationApiImpl(private val client: HttpClient) : InvitationApi {

    override suspend fun createInvitation(
        groupId: String,
        request: CreateInvitationRequest,
    ): Either<AppError, InvitationResponse> =
        apiCall {
            client.post(Groups.CreateInvitation(groupId = groupId)) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun getInvitation(token: String): Either<AppError, InvitationResponse> =
        apiCall { client.get(Invitations.ByToken(token = token)) }

    override suspend fun acceptInvitation(request: AcceptInvitationRequest): Either<AppError, AcceptInvitationResponse> =
        apiCall {
            client.post(Invitations.Accept()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
}
