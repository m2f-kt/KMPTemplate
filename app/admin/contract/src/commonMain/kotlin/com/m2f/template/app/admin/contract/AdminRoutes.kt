package com.m2f.template.app.admin.contract

import com.m2f.template.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data class AdminPanelRoute(val groupId: String? = null) : Route

@Serializable
data class RegisterMemberRoute(val groupId: String) : Route
