package com.m2f.template.app.auth

/**
 * One-shot events for the invitation acceptance screen.
 */
sealed interface InviteAcceptEvent {
    /**
     * Navigate to the group after successfully accepting the invitation.
     */
    data class NavigateToGroup(val groupId: String) : InviteAcceptEvent

    /**
     * Navigate to login screen, preserving the invitation token.
     */
    data class NavigateToLogin(val token: String, val email: String) : InviteAcceptEvent

    /**
     * Navigate to register screen, preserving the invitation token.
     */
    data class NavigateToRegister(val token: String, val email: String) : InviteAcceptEvent

    /**
     * Navigate away after requesting a new invitation (expired token).
     */
    data object RequestedNewInvitation : InviteAcceptEvent
}
