package com.m2f.server.ai.routes

import arrow.core.raise.Raise
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.ai.agents.AssistantAgentService
import com.m2f.server.ai.agents.ChatAgentService
import com.m2f.server.ai.contract.errors.ProviderUnavailable
import com.m2f.template.models.dto.AgentRequest
import com.m2f.template.models.dto.AgentResponse
import com.m2f.template.models.dto.ChatRequest
import com.m2f.template.models.dto.ChatResponse
import com.m2f.template.models.routes.Ai
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import com.m2f.template.models.dto.ChatStreamFrame
import com.m2f.template.models.dto.ErrorResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Ensure AI is enabled, raising [ProviderUnavailable] if not.
 */
context(raise: Raise<DomainError>)
private fun ensureAiEnabled(aiEnabled: Boolean) {
    if (!aiEnabled) raise.raise(ProviderUnavailable())
}

/**
 * AI agent routes behind authentication.
 * POST /api/ai/assistant -- ReAct agent with UserTools
 * POST /api/ai/chat -- Conversational agent with persistence + RAG
 *
 * Both routes are gated by [aiEnabled] flag -- when false, returns ProviderUnavailable error.
 *
 * @param roleChecker Lambda to check if user is admin in a group (avoids groups module dependency)
 */
@OptIn(ExperimentalUuidApi::class)
context(config: Configuration)
fun Route.aiRoutes(
    assistantAgentService: AssistantAgentService,
    chatAgentService: ChatAgentService,
    roleChecker: suspend (userId: String, groupId: String) -> Boolean = { _, _ -> false },
) {
    authenticate {
        post<Ai.Assistant> {
            conduitAuth { _ ->
                ensureAiEnabled(config.env.ai.enabled)
                val request = getModel<AgentRequest>()
                val result = assistantAgentService.run(request.message)
                AgentResponse(
                    message = result,
                    agentType = "assistant",
                )
            }
        }
        post<Ai.Chat> {
            conduitAuth { userId ->
                ensureAiEnabled(config.env.ai.enabled)
                val request = getModel<ChatRequest>()
                val conversationId = request.conversationId
                    ?: Uuid.random().toString()

                // Resolve RAG parameters if groupId is provided
                val groupUuid = request.groupId?.let { Uuid.parse(it) }
                val userUuid = Uuid.parse(userId)
                val isAdmin = request.groupId?.let { roleChecker(userId, it) } ?: false

                val result = chatAgentService.run(
                    userId = userId,
                    conversationId = conversationId,
                    input = request.message,
                    groupId = groupUuid,
                    userUuid = userUuid,
                    isAdmin = isAdmin,
                )
                ChatResponse(
                    message = result,
                    conversationId = conversationId,
                )
            }
        }
    }
    webSocket(Ai.Chat.WS_PATH) {
        getAuth { userId ->
            ensureAiEnabled(config.env.ai.enabled)

            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val request = Json.decodeFromString<ChatRequest>(frame.readText())

                val conversationId = request.conversationId
                    ?: Uuid.random().toString()

                // Resolve RAG parameters if groupId is provided
                val groupUuid = request.groupId?.let { Uuid.parse(it) }
                val userUuid = Uuid.parse(userId)
                val isAdmin = request.groupId?.let { roleChecker(userId, it) } ?: false

                try {
                    chatAgentService.streamChat(
                        userId = userId,
                        conversationId = conversationId,
                        input = request.message,
                        groupId = groupUuid,
                        userUuid = userUuid,
                        isAdmin = isAdmin,
                    ).collect { chunk ->
                        send(Frame.Text(Json.encodeToString(
                            ChatStreamFrame(
                                message = chunk,
                                conversationId = conversationId,
                                completed = false,
                            )
                        )))
                    }
                    send(Frame.Text(Json.encodeToString(
                        ChatStreamFrame(
                            message = "",
                            conversationId = conversationId,
                            completed = true,
                        )
                    )))
                } catch (e: Exception) {
                    send(Frame.Text(Json.encodeToString(
                        ErrorResponse(
                            code = "AGENT_ERROR",
                            message = "Error: ${e.message}",
                        )
                    )))
                }
            }
        }
    }
}
