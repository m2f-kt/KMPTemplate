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
}
