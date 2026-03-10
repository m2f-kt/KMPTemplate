package com.m2f.template.navigation

import kotlinx.serialization.Serializable

@Serializable
data class LoginRoute(val invitationToken: String? = null, val invitationEmail: String? = null) : Route

@Serializable
data class RegisterRoute(val invitationToken: String? = null, val invitationEmail: String? = null) : Route

@Serializable
data object DashboardRoute : Route

@Serializable
data object ProfileRoute : Route

@Serializable
data object ForgotPasswordRoute : Route

/**
 * Route for handling OAuth callback redirects.
 * Navigation arguments carry the JWT tokens from the server redirect.
 */
@Serializable
data class OAuthCallbackRoute(
    val accessToken: String,
    val refreshToken: String,
) : Route

/**
 * Route for the group admin panel.
 */
@Serializable
data class AdminPanelRoute(val groupId: String? = null) : Route

/**
 * Route for the register-member form.
 */
@Serializable
data class RegisterMemberRoute(val groupId: String) : Route

/**
 * Route for accepting a group invitation via email link.
 * The token is extracted from the invitation URL.
 */
@Serializable
data class InviteAcceptRoute(val token: String) : Route

/**
 * Route for the documents management screen (RAG document upload/list/delete).
 */
@Serializable
data class DocumentsRoute(val groupId: String) : Route
