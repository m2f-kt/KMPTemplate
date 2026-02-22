package com.m2f.template.app.auth

import com.m2f.template.models.localization.StringKey

/**
 * UI state model for the invitation acceptance screen.
 * Handles loading invitation details, displaying info, and accepting the invitation.
 */
data class InviteAcceptModel(
    val token: String = "",
    val isLoadingInvitation: Boolean = false,
    val isAccepting: Boolean = false,
    val groupName: String? = null,
    val inviterName: String? = null,
    val role: String? = null,
    val email: String? = null,
    val isExpired: Boolean = false,
    val isAlreadyAccepted: Boolean = false,
    val error: StringKey? = null,
    val acceptSuccess: Boolean = false,
    val acceptedGroupId: String? = null,
)
