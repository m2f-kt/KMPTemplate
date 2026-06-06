package com.m2f.core.observability

import ai.koog.agents.features.opentelemetry.attribute.CustomAttribute
import ai.koog.agents.utils.HiddenString
import ai.koog.prompt.message.Message

/**
 * The PURE core of [LangfuseSpanAdapter] — the span reshaping + trace-output decisions, extracted as
 * plain functions so they are unit-testable without constructing Koog's `internal` `GenAIAgentSpan`.
 * The adapter is a thin shell that feeds span messages here and applies the returned attributes.
 *
 * Each function mirrors exactly what the stock Koog `LangfuseSpanAdapter` emits (indexed
 * `gen_ai.prompt.{i}` / `gen_ai.completion.{i}`), plus the trace-output capture the stock adapter
 * lacks. Keeping it pure is what lets the trace-header contract be tested deterministically.
 */
internal object LangfuseSpanReshaping {

    /** Decompose INPUT messages into the indexed prompt attributes Langfuse renders. */
    fun decomposeInput(messages: List<Message>): List<CustomAttribute> = buildList {
        messages.forEachIndexed { index, message ->
            add(CustomAttribute("gen_ai.prompt.$index.role", message.role.name.lowercase()))
            add(CustomAttribute("gen_ai.prompt.$index.content", HiddenString(message.textContent())))
        }
    }

    /** Decompose OUTPUT messages into the indexed completion attributes Langfuse renders. */
    fun decomposeOutput(messages: List<Message>): List<CustomAttribute> = buildList {
        messages.forEachIndexed { index, message ->
            add(CustomAttribute("gen_ai.completion.$index.role", Message.Role.Assistant.name.lowercase()))
            add(CustomAttribute("gen_ai.completion.$index.content", HiddenString(message.textContent())))
            (message as? Message.Assistant)?.finishReason?.let { reason ->
                add(CustomAttribute("gen_ai.completion.$index.finish_reason", reason))
            }
        }
    }

    /** The last assistant text in an output message list — the candidate for `langfuse.trace.output`. */
    fun lastAssistantText(messages: List<Message>): String? =
        messages.filterIsInstance<Message.Assistant>().lastOrNull()?.textContent()

    /**
     * The `langfuse.trace.output` attribute for [lastCompletion], or null when withheld
     * ([emitTraceContent] false / no completion). [finalize] transforms the header text.
     */
    fun traceOutputAttribute(
        lastCompletion: String?,
        emitTraceContent: Boolean,
        finalize: (String) -> String,
    ): CustomAttribute? {
        if (!emitTraceContent || lastCompletion == null) return null
        return CustomAttribute("langfuse.trace.output", finalize(lastCompletion))
    }
}
