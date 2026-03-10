package com.m2f.template.app.documents.contract

import com.m2f.template.navigation.Route
import kotlinx.serialization.Serializable

@Serializable
data class DocumentsRoute(val groupId: String) : Route
