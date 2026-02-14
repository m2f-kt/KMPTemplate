package com.m2f.server.ai.routes

import arrow.core.raise.Raise
import com.m2f.core.config.server.DomainError
import com.m2f.core.config.server.conduitAuth
import com.m2f.core.config.server.getAuth
import com.m2f.core.config.server.getModel
import com.m2f.server.ai.agents.AssistantAgentService
import com.m2f.server.ai.agents.ChatAgentService
import com.m2f.server.ai.errors.ProviderUnavailable
import com.m2f.template.models.dto.AgentRequest
import com.m2f.template.models.dto.AgentResponse
import com.m2f.template.models.dto.ChatRequest
import com.m2f.template.models.dto.ChatResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
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
 * POST /api/ai/chat -- Conversational agent with persistence
 *
 * Both routes are gated by [aiEnabled] flag -- when false, returns ProviderUnavailable error.
 */
@OptIn(ExperimentalUuidApi::class)
fun Route.aiRoutes(
    assistantAgentService: AssistantAgentService,
    chatAgentService: ChatAgentService,
    aiEnabled: Boolean,
    jwtSecret: String,
    jwtAudience: String,
    jwtIssuer: String,
) {
    route("/api/ai") {
        authenticate {
            post("/assistant") {
                conduitAuth { userId ->
                    ensureAiEnabled(aiEnabled)
                    val request = getModel<AgentRequest>()
                    val result = assistantAgentService.run(request.message)
                    AgentResponse(
                        message = result,
                        agentType = "assistant",
                    )
                }
            }
            post("/chat") {
                conduitAuth { userId ->
                    ensureAiEnabled(aiEnabled)
                    val request = getModel<ChatRequest>()
                    val conversationId = request.conversationId
                        ?: Uuid.random().toString()
                    val result = chatAgentService.run(
                        userId = userId,
                        conversationId = conversationId,
                        input = request.message,
                    )
                    ChatResponse(
                        message = result,
                        conversationId = conversationId,
                    )
                }
            }
        }

        sse("/chat/stream") {
            getAuth(jwtSecret, jwtAudience, jwtIssuer) { userId ->
                ensureAiEnabled(aiEnabled)

                val message = call.request.queryParameters["message"]
                if (message == null) {
                    this@sse.send(ServerSentEvent(data = "Error: Missing message", event = "error"))
                    return@getAuth
                }

                val conversationId = call.request.queryParameters["conversationId"]
                    ?: Uuid.random().toString()

                this@sse.send(ServerSentEvent(data = conversationId, event = "conversation"))

                try {
                    chatAgentService.streamChat(
                        userId = userId,
                        conversationId = conversationId,
                        input = message,
                    ).collect { chunk ->
                        this@sse.send(ServerSentEvent(data = chunk, event = "message"))
                    }
                    this@sse.send(ServerSentEvent(data = "[DONE]", event = "done"))
                } catch (e: Exception) {
                    this@sse.send(ServerSentEvent(data = "Error: ${e.message}", event = "error"))
                }
            }
        }
    }
}
