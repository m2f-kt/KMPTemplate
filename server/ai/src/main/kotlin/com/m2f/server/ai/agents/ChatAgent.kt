package com.m2f.server.ai.agents

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.context.RollbackStrategy
import ai.koog.agents.snapshot.feature.Persistence
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.m2f.core.config.server.DomainError
import com.m2f.server.ai.errors.AgentExecutionFailed
import com.m2f.server.ai.persistence.ExposedPersistenceStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Service for the conversational agent with persistence.
 * Uses a custom streaming strategy with per-request AIAgent instances
 * backed by ExposedPersistenceStorage so conversations can be resumed across HTTP requests.
 *
 * Composite agentId format: "user:{userId}:conv:{conversationId}" for multi-tenancy.
 */
class ChatAgentService(
    private val persistenceStorage: ExposedPersistenceStorage,
    private val googleApiKey: String,
    private val aiDispatcher: CoroutineDispatcher,
) {
    private val systemPrompt = """
        |You are a friendly conversational assistant.
        |You remember previous messages in this conversation and provide helpful, contextual responses.
    """.trimMargin()

    private val executor by lazy {
        SingleLLMPromptExecutor(GoogleLLMClient(googleApiKey))
    }

    private val agentConfig = AIAgentConfig(
        prompt = prompt("chat-agent") {
            system(systemPrompt)
        },
        model = GoogleModels.Gemini2_5Pro,
        maxAgentIterations = 10,
    )

    /**
     * Stream chat responses as text chunks via a Flow.
     * Creates a per-request AIAgent with the custom streaming strategy
     * and emits text frames as they arrive from the LLM.
     */
    fun streamChat(userId: String, conversationId: String, input: String): Flow<String> =
        callbackFlow {
            val agentId = "user:$userId:conv:$conversationId"
            var agent: AIAgent<String, Any>? = null

            val strategy = chatStreamingStrategy { frame ->
                trySend(frame.text)
            }

            try {
                agent = AIAgent(
                    id = agentId,
                    promptExecutor = executor,
                    agentConfig = agentConfig,
                    strategy = strategy,
                ) {
                    install(Persistence) {
                        storage = persistenceStorage
                        enableAutomaticPersistence = true
                        rollbackStrategy = RollbackStrategy.MessageHistoryOnly
                    }
                }

                withContext(aiDispatcher) {
                    agent.run(input)
                }
                close()
            } catch (e: Exception) {
                trySend("[ERROR] Agent failed: ${e.message}")
                close()
            }

            awaitClose {
                // Fire-and-forget cleanup. agent.close() is a suspend function,
                // so we launch in a separate scope rather than using runBlocking
                // which would block the Netty event loop thread.
                agent?.let { a ->
                    CoroutineScope(aiDispatcher).launch {
                        try { a.close() } catch (_: Exception) {}
                    }
                }
            }
        }

    /**
     * Run the chat agent and return the complete response.
     * Backward-compatible with the existing POST endpoint.
     * Collects the streaming flow into a single string.
     */
    context(raise: Raise<DomainError>)
    suspend fun run(userId: String, conversationId: String, input: String): String = with(raise) {
        catch({
            streamChat(userId, conversationId, input)
                .toList()
                .joinToString("")
        }) { e ->
            raise(AgentExecutionFailed(detail = "Chat agent failed: ${e.message}"))
        }
    }
}
