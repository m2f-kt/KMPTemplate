package com.m2f.template.app.privacy.contract

import com.m2f.template.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data object ConsentGateRoute : Route

@Serializable
data object PrivacySettingsRoute : Route

@Serializable
data class LegalDocumentRoute(val type: String, val locale: String? = null) : Route

@Serializable
data object AccountDeletionRoute : Route
