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

@Serializable
data object ProcessesRoute

@Serializable
data object LogsRoute

@Serializable
data object DeploymentsRoute

@Serializable
data object SettingsRoute

/**
 * Route for handling OAuth callback redirects.
 * Navigation arguments carry the JWT tokens from the server redirect.
 */
@Serializable
data class OAuthCallbackRoute(
    val accessToken: String,
    val refreshToken: String,
)
