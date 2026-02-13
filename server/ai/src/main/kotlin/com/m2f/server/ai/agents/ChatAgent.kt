package com.m2f.server.ai.agents

import ai.koog.agents.core.agent.AIAgentService
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.context.RollbackStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.chatAgentStrategy
import ai.koog.agents.snapshot.feature.Persistence
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.m2f.core.config.server.DomainError
import com.m2f.server.ai.errors.AgentExecutionFailed
import com.m2f.server.ai.persistence.ExposedPersistenceStorage

/**
 * Service for the conversational agent with persistence.
 * Uses chatAgentStrategy with Persistence feature backed by ExposedPersistenceStorage
 * so conversations can be resumed across HTTP requests.
 *
 * Composite agentId format: "user:{userId}:conv:{conversationId}" for multi-tenancy.
 */
class ChatAgentService(
    private val persistenceStorage: ExposedPersistenceStorage,
    private val openaiApiKey: String,
) {
    private val systemPrompt = """
        |You are a friendly conversational assistant.
        |You remember previous messages in this conversation and provide helpful, contextual responses.
    """.trimMargin()

    private val executor by lazy {
        SingleLLMPromptExecutor(OpenAILLMClient(openaiApiKey))
    }

    private val agentConfig = AIAgentConfig(
        prompt = prompt("chat-agent") {
            system(systemPrompt)
        },
        model = OpenAIModels.Chat.GPT4o,
        maxAgentIterations = 10,
    )

    private val agentService by lazy {
        AIAgentService(
            promptExecutor = executor,
            agentConfig = agentConfig,
            strategy = chatAgentStrategy(),
            toolRegistry = ToolRegistry.EMPTY,
        ) {
            install(Persistence.Feature) {
                storage = persistenceStorage
                enableAutomaticPersistence = true
                rollbackStrategy = RollbackStrategy.MessageHistoryOnly
            }
        }
    }

    context(raise: Raise<DomainError>)
    suspend fun run(userId: String, conversationId: String, input: String): String {
        val agentId = "user:$userId:conv:$conversationId"
        return with(raise) {
            catch({
                agentService.createAgentAndRun(input, agentId)
            }) { e ->
                raise(AgentExecutionFailed(detail = "Chat agent failed: ${e.message}"))
            }
        }
    }
}
