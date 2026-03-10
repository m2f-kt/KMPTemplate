package com.m2f.template.app.auth.contract

import com.m2f.template.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data class LoginRoute(val invitationToken: String? = null, val invitationEmail: String? = null) : Route

@Serializable
data class RegisterRoute(val invitationToken: String? = null, val invitationEmail: String? = null) : Route

@Serializable
data object ForgotPasswordRoute : Route

@Serializable
data class OAuthCallbackRoute(val accessToken: String, val refreshToken: String) : Route

@Serializable
data class InviteAcceptRoute(val token: String) : Route
