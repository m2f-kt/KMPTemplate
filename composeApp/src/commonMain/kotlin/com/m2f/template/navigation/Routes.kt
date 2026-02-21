package com.m2f.template.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

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
