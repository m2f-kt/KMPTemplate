package com.m2f.template.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class AgentRequest(
    val message: String,
)

@Serializable
data class AgentResponse(
    val message: String,
    val agentType: String,
)

@Serializable
data class ChatRequest(
    val message: String,
    val conversationId: String? = null,
)

@Serializable
data class ChatResponse(
    val message: String,
    val conversationId: String,
)
