package com.m2f.server.ai.agents

import ai.koog.agents.core.agent.AIAgentService
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.m2f.core.config.server.DomainError
import com.m2f.server.ai.contract.errors.AgentExecutionFailed
import com.m2f.server.ai.tools.UserTools

/**
 * Service for the ReAct tool-using agent. Uses reActStrategy with UserTools
 * to allow the LLM to look up user data from the database.
 *
 * Creates a standalone PromptExecutor with the provided Google API key
 * (no Koog Ktor plugin dependency).
 */
class AssistantAgentService(
    private val userTools: UserTools,
    private val googleApiKey: String,
) {
    private val systemPrompt = """
        |You are a helpful assistant for a web application.
        |You can look up user information using the available tools.
        |Always use tools when the user asks about specific users.
    """.trimMargin()

    private val executor by lazy {
        MultiLLMPromptExecutor(GoogleLLMClient(googleApiKey))
    }

    private val toolRegistry = ToolRegistry {
        tools(userTools)
    }

    private val agentConfig = AIAgentConfig(
        prompt = prompt("assistant-agent") {
            system(systemPrompt)
        },
        model = GoogleModels.Gemini2_5Flash,
        maxAgentIterations = 10,
    )

    private val agentService by lazy {
        AIAgentService(
            promptExecutor = executor,
            agentConfig = agentConfig,
            strategy = reActStrategy(),
            toolRegistry = toolRegistry,
        )
    }

    context(raise: Raise<DomainError>)
    suspend fun run(input: String): String = with(raise) {
        catch({
            agentService.createAgentAndRun(input)
        }) { e ->
            raise(AgentExecutionFailed(detail = "Assistant agent failed: ${e.message}"))
        }
    }
}
