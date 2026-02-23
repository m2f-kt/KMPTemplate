package com.m2f.template.app.auth

import com.m2f.template.models.localization.StringKey

/**
 * State mutations for the invitation acceptance screen.
 */
sealed interface InviteAcceptMutation {
    data class SetToken(val token: String) : InviteAcceptMutation
    data class SetLoadingInvitation(val loading: Boolean) : InviteAcceptMutation
    data class SetAccepting(val accepting: Boolean) : InviteAcceptMutation
    data class SetInvitationDetails(
        val groupName: String,
        val inviterName: String,
        val role: String,
        val email: String,
        val isExpired: Boolean,
        val isAlreadyAccepted: Boolean,
    ) : InviteAcceptMutation
    data class SetError(val error: StringKey?) : InviteAcceptMutation
    data class SetAcceptSuccess(val groupId: String) : InviteAcceptMutation
    data class SetLoggedIn(val loggedIn: Boolean) : InviteAcceptMutation
}
