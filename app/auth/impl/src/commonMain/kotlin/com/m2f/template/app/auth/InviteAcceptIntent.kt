package com.m2f.template.app.auth

/**
 * User intents for the invitation acceptance screen.
 */
sealed interface InviteAcceptIntent {
    /**
     * Load invitation details using the provided token.
     */
    data class LoadInvitation(val token: String) : InviteAcceptIntent

    /**
     * User clicked the accept button.
     */
    data object AcceptInvitation : InviteAcceptIntent

    /**
     * User wants to navigate to login to accept invitation.
     */
    data object GoToLogin : InviteAcceptIntent

    /**
     * User wants to navigate to register to accept invitation.
     */
    data object GoToRegister : InviteAcceptIntent

    /**
     * User wants to request a new invitation (current one is expired).
     */
    data object RequestNewInvitation : InviteAcceptIntent
}
