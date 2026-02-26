package com.m2f.template.core.testing.fakes

import arrow.core.Either
import com.m2f.template.core.testing.FakeSDKDsl
import com.m2f.template.models.AppError
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.dto.AcceptInvitationResponse
import com.m2f.template.models.dto.CreateInvitationRequest
import com.m2f.template.models.dto.InvitationResponse
import com.m2f.template.sdk.api.InvitationApi

/**
 * DSL builder for creating fake [InvitationApi] instances in tests.
 *
 * Each method defaults to returning `Either.Left(AppError.Client.Unknown())`
 * so that unconfigured code paths fail fast instead of silently succeeding.
 *
 * Usage:
 * ```kotlin
 * val invitationApi = fakeInvitationApi {
 *     createInvitation { _, _ -> Either.Right(InvitationResponse(...)) }
 *     getInvitation { Either.Right(InvitationResponse(...)) }
 *     acceptInvitation { Either.Right(AcceptInvitationResponse(...)) }
 * }
 * ```
 */
@FakeSDKDsl
class FakeInvitationApiBuilder {

    private var _createInvitation: suspend (String, CreateInvitationRequest) -> Either<AppError, InvitationResponse> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    private var _getInvitation: suspend (String) -> Either<AppError, InvitationResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _acceptInvitation: suspend (AcceptInvitationRequest) -> Either<AppError, AcceptInvitationResponse> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _listInvitations: suspend (String) -> Either<AppError, List<InvitationResponse>> =
        { Either.Left(AppError.Client.Unknown()) }

    private var _revokeInvitation: suspend (String, String) -> Either<AppError, Unit> =
        { _, _ -> Either.Left(AppError.Client.Unknown()) }

    fun createInvitation(behavior: suspend (String, CreateInvitationRequest) -> Either<AppError, InvitationResponse>) {
        _createInvitation = behavior
    }

    fun getInvitation(behavior: suspend (String) -> Either<AppError, InvitationResponse>) {
        _getInvitation = behavior
    }

    fun acceptInvitation(behavior: suspend (AcceptInvitationRequest) -> Either<AppError, AcceptInvitationResponse>) {
        _acceptInvitation = behavior
    }

    fun listInvitations(behavior: suspend (String) -> Either<AppError, List<InvitationResponse>>) {
        _listInvitations = behavior
    }

    fun revokeInvitation(behavior: suspend (String, String) -> Either<AppError, Unit>) {
        _revokeInvitation = behavior
    }

    internal fun build(): InvitationApi = object : InvitationApi {
        override suspend fun createInvitation(
            groupId: String,
            request: CreateInvitationRequest,
        ): Either<AppError, InvitationResponse> =
            _createInvitation(groupId, request)

        override suspend fun getInvitation(token: String): Either<AppError, InvitationResponse> =
            _getInvitation(token)

        override suspend fun acceptInvitation(request: AcceptInvitationRequest): Either<AppError, AcceptInvitationResponse> =
            _acceptInvitation(request)

        override suspend fun listInvitations(groupId: String): Either<AppError, List<InvitationResponse>> =
            _listInvitations(groupId)

        override suspend fun revokeInvitation(groupId: String, invitationId: String): Either<AppError, Unit> =
            _revokeInvitation(groupId, invitationId)
    }
}
