package com.m2f.server.ai.agents

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStreaming
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResults
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList

/**
 * Custom streaming strategy that replaces chatAgentStrategy().
 * Fixes the infinite loop bug by accepting assistant messages as valid responses.
 * Streams text frames via the processFrame callback for WebSocket delivery.
 */
fun chatStreamingStrategy(
    processFrame: (StreamFrame.TextDelta) -> Unit,
): AIAgentGraphStrategy<String, Any> =
    strategy(name = "chat-streaming") {
        val nodeStreaming by nodeLLMRequestStreaming()
        val executeTools by nodeExecuteTools(parallel = true)
        val sendToolResults by nodeLLMSendToolResults()

        // Koog 1.0: tool calls are now Message parts (MessagePart.Tool.Call) rather than
        // standalone Message.Response values, and the multi-tool node/edge builders were
        // consolidated (nodeExecuteMultipleTools -> nodeExecuteTools, onMultipleToolCalls ->
        // onToolCalls). The streamed frames are collected into a single Message.Assistant so the
        // graph edges can dispatch on its tool-call parts.
        val processFrames by node<Flow<StreamFrame>, Message.Assistant> { frames ->
            var end: StreamFrame.End? = null
            val toolCalls = frames
                .mapNotNull { frame ->
                    when (frame) {
                        is StreamFrame.TextDelta -> {
                            processFrame(frame)
                            null
                        }
                        is StreamFrame.End -> {
                            end = frame
                            null
                        }
                        is StreamFrame.ToolCallComplete -> frame
                        else -> null
                    }
                }
                .map { toolCall ->
                    MessagePart.Tool.Call(
                        id = toolCall.id,
                        tool = toolCall.name,
                        args = toolCall.content,
                    )
                }
                .toList()
            Message.Assistant(
                parts = toolCalls,
                metaInfo = end?.metaInfo ?: ResponseMetaInfo.Empty,
            )
        }

        edge(nodeStart forwardTo nodeStreaming)
        edge(nodeStreaming forwardTo processFrames)
        edge(processFrames forwardTo executeTools onToolCalls { true })
        edge(executeTools forwardTo sendToolResults)
        edge(
            processFrames forwardTo nodeFinish onCondition {
                it.parts.none { part -> part is MessagePart.Tool.Call }
            },
        )
    }
