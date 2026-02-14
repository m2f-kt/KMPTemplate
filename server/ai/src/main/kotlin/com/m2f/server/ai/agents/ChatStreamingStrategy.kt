package com.m2f.server.ai.agents

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteMultipleTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStreaming
import ai.koog.agents.core.dsl.extension.nodeLLMSendMultipleToolResults
import ai.koog.agents.core.dsl.extension.onMultipleToolCalls
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList

/**
 * Custom streaming strategy that replaces chatAgentStrategy().
 * Fixes the infinite loop bug by accepting assistant messages as valid responses.
 * Streams text frames via the processFrame callback for SSE delivery.
 */
fun chatStreamingStrategy(
    processFrame: (StreamFrame.Append) -> Unit
): AIAgentGraphStrategy<String, Any> =
    strategy(name = "chat-streaming") {
        val nodeStreaming by nodeLLMRequestStreaming()
        val executeMultipleTools by nodeExecuteMultipleTools(parallelTools = true)
        val sendToolResults by nodeLLMSendMultipleToolResults()

        val processFrames by node<Flow<StreamFrame>, List<Message.Response>> { frames ->
            var end: StreamFrame.End? = null
            frames
                .mapNotNull { frame ->
                    when (frame) {
                        is StreamFrame.Append -> {
                            processFrame(frame)
                            null
                        }
                        is StreamFrame.End -> {
                            end = frame
                            null
                        }
                        is StreamFrame.ToolCall -> frame
                    }
                }
                .map { toolCall ->
                    Message.Tool.Call(
                        id = toolCall.id,
                        tool = toolCall.name,
                        content = toolCall.content,
                        metaInfo = end?.metaInfo ?: ResponseMetaInfo.Empty,
                    )
                }
                .toList()
        }

        edge(nodeStart forwardTo nodeStreaming)
        edge(nodeStreaming forwardTo processFrames)
        edge(processFrames forwardTo executeMultipleTools onMultipleToolCalls { true })
        edge(executeMultipleTools forwardTo sendToolResults)
        edge(
            processFrames forwardTo nodeFinish onCondition {
                it.filterIsInstance<Message.Tool.Call>().isEmpty()
            }
        )
    }
