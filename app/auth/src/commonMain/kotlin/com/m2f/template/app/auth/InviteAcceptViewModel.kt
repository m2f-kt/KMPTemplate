package com.m2f.template.app.auth

import androidx.lifecycle.viewModelScope
import com.m2f.template.core.mvi.MviViewModel
import com.m2f.template.models.dto.AcceptInvitationRequest
import com.m2f.template.models.localization.StringKey
import com.m2f.template.sdk.Sdk
import com.m2f.template.storage.TokenStorage
import kotlinx.coroutines.launch

/**
 * ViewModel for the invitation acceptance screen.
 * Handles loading invitation details and accepting invitations.
 */
class InviteAcceptViewModel(
    private val sdk: Sdk,
    private val tokenStorage: TokenStorage,
) : MviViewModel<InviteAcceptIntent, InviteAcceptModel, InviteAcceptMutation, InviteAcceptEvent>(
    initialState = InviteAcceptModel()
) {

    override fun take(intent: InviteAcceptIntent) {
        viewModelScope.launch {
            when (intent) {
                is InviteAcceptIntent.LoadInvitation -> handleLoadInvitation(intent.token)
                is InviteAcceptIntent.AcceptInvitation -> handleAcceptInvitation()
                is InviteAcceptIntent.GoToLogin -> {
                    sendEvent(InviteAcceptEvent.NavigateToLogin(model.value.token))
                }
                is InviteAcceptIntent.GoToRegister -> {
                    sendEvent(InviteAcceptEvent.NavigateToRegister(model.value.token))
                }
                is InviteAcceptIntent.RequestNewInvitation -> {
                    sendEvent(InviteAcceptEvent.RequestedNewInvitation)
                }
            }
        }
    }

    private suspend fun handleLoadInvitation(token: String) {
        sendMutation(InviteAcceptMutation.SetToken(token))
        sendMutation(InviteAcceptMutation.SetLoadingInvitation(true))
        sendMutation(InviteAcceptMutation.SetError(null))

        val isLoggedIn = tokenStorage.getAccessToken() != null
        sendMutation(InviteAcceptMutation.SetLoggedIn(isLoggedIn))

        sdk.getInvitation(token)
            .fold(
                ifLeft = { error ->
                    val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                    sendMutation(InviteAcceptMutation.SetError(key))
                    sendMutation(InviteAcceptMutation.SetLoadingInvitation(false))
                },
                ifRight = { invitation ->
                    sendMutation(
                        InviteAcceptMutation.SetInvitationDetails(
                            groupName = invitation.groupName,
                            inviterName = invitation.inviterName,
                            role = invitation.role,
                            email = invitation.email,
                            isExpired = invitation.isExpired,
                            isAlreadyAccepted = invitation.isAccepted,
                            isRevoked = invitation.isRevoked,
                        )
                    )
                    sendMutation(InviteAcceptMutation.SetLoadingInvitation(false))
                },
            )
    }

    private suspend fun handleAcceptInvitation() {
        val current = model.value
        val token = current.token

        if (token.isBlank()) {
            sendMutation(InviteAcceptMutation.SetError(StringKey.GENERIC_ERROR))
            return
        }

        sendMutation(InviteAcceptMutation.SetAccepting(true))
        sendMutation(InviteAcceptMutation.SetError(null))

        sdk.acceptInvitation(AcceptInvitationRequest(token))
            .fold(
                ifLeft = { error ->
                    val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
                    sendMutation(InviteAcceptMutation.SetError(key))
                    sendMutation(InviteAcceptMutation.SetAccepting(false))
                },
                ifRight = { response ->
                    sendMutation(InviteAcceptMutation.SetAcceptSuccess(response.groupId))
                    sendEvent(InviteAcceptEvent.NavigateToGroup(response.groupId))
                },
            )
    }

    override suspend fun reduce(model: InviteAcceptModel, mutation: InviteAcceptMutation): InviteAcceptModel =
        when (mutation) {
            is InviteAcceptMutation.SetToken -> model.copy(token = mutation.token)
            is InviteAcceptMutation.SetLoadingInvitation -> model.copy(isLoadingInvitation = mutation.loading)
            is InviteAcceptMutation.SetAccepting -> model.copy(isAccepting = mutation.accepting)
            is InviteAcceptMutation.SetInvitationDetails -> model.copy(
                groupName = mutation.groupName,
                inviterName = mutation.inviterName,
                role = mutation.role,
                email = mutation.email,
                isExpired = mutation.isExpired,
                isAlreadyAccepted = mutation.isAlreadyAccepted,
                isRevoked = mutation.isRevoked,
            )
            is InviteAcceptMutation.SetError -> model.copy(error = mutation.error)
            is InviteAcceptMutation.SetAcceptSuccess -> model.copy(
                isAccepting = false,
                acceptSuccess = true,
                acceptedGroupId = mutation.groupId,
            )
            is InviteAcceptMutation.SetLoggedIn -> model.copy(isLoggedIn = mutation.loggedIn)
        }
}
