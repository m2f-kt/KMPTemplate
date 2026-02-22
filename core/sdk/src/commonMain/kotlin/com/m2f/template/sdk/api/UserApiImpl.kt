package com.m2f.template.sdk.api

import arrow.core.Either
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.MembershipSummary
import com.m2f.template.models.dto.UpdateProfileRequest
import com.m2f.template.models.dto.UserResponse
import com.m2f.template.models.routes.Users
import com.m2f.template.sdk.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType

class UserApiImpl(private val client: HttpClient) : UserApi {

    override suspend fun getProfile(): Either<AppError, UserResponse> =
        apiCall { client.get(Users.Me()) }

    override suspend fun updateProfile(request: UpdateProfileRequest): Either<AppError, UserResponse> =
        apiCall {
            client.put(Users.Me()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    override suspend fun getUserById(id: String): Either<AppError, UserResponse> =
        apiCall { client.get(Users.ById(id = id)) }

    override suspend fun getMyMemberships(): Either<AppError, List<MembershipSummary>> =
        apiCall { client.get(Users.Me.Memberships()) }

    override suspend fun uploadAvatar(
        imageBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Either<AppError, UserResponse> = apiCall {
        client.submitFormWithBinaryData(
            url = "/api/users/me/avatar",
            formData = formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    append(HttpHeaders.ContentType, contentType)
                })
            },
        ) {
            method = HttpMethod.Put
        }
    }
}
