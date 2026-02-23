package com.m2f.template.navigation

import kotlinx.serialization.Serializable

@Serializable
data class LoginRoute(val invitationToken: String? = null)

@Serializable
data class RegisterRoute(val invitationToken: String? = null)

@Serializable
data object DashboardRoute

@Serializable
data object ProfileRoute

@Serializable
data object ForgotPasswordRoute

/**
 * Route for handling OAuth callback redirects.
 * Navigation arguments carry the JWT tokens from the server redirect.
 */
@Serializable
data class OAuthCallbackRoute(
    val accessToken: String,
    val refreshToken: String,
)

/**
 * Route for the group admin panel.
 */
@Serializable
data class AdminPanelRoute(val groupId: String? = null)

/**
 * Route for the register-member form.
 */
@Serializable
data class RegisterMemberRoute(val groupId: String)

/**
 * Route for accepting a group invitation via email link.
 * The token is extracted from the invitation URL.
 */
@Serializable
data class InviteAcceptRoute(val token: String)
