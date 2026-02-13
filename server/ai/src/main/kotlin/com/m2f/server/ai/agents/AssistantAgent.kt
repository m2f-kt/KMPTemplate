package com.m2f.server.ai.agents

import ai.koog.agents.core.agent.AIAgentService
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import arrow.core.raise.Raise
import arrow.core.raise.catch
import com.m2f.core.config.server.DomainError
import com.m2f.server.ai.errors.AgentExecutionFailed
import com.m2f.server.ai.tools.UserTools

/**
 * Service for the ReAct tool-using agent. Uses reActStrategy with UserTools
 * to allow the LLM to look up user data from the database.
 *
 * Creates a standalone PromptExecutor with the provided OpenAI API key
 * (no Koog Ktor plugin dependency).
 */
class AssistantAgentService(
    private val userTools: UserTools,
    private val openaiApiKey: String,
) {
    private val systemPrompt = """
        |You are a helpful assistant for a web application.
        |You can look up user information using the available tools.
        |Always use tools when the user asks about specific users.
    """.trimMargin()

    private val executor by lazy {
        SingleLLMPromptExecutor(OpenAILLMClient(openaiApiKey))
    }

    private val toolRegistry = ToolRegistry {
        tools(userTools)
    }

    private val agentConfig = AIAgentConfig(
        prompt = prompt("assistant-agent") {
            system(systemPrompt)
        },
        model = OpenAIModels.Chat.GPT4o,
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
