package com.m2f.core.observability

import ai.koog.agents.utils.HiddenString
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

/**
 * Unit test for the generic Langfuse span adapter's PURE core ([LangfuseSpanReshaping]). Koog's
 * `GenAIAgentSpan` constructor is `internal`, so the testable reshaping logic is extracted as pure
 * functions — these tests pin the exact trace-header contract the adapter feeds those functions:
 *  - inference INPUT messages → indexed `gen_ai.prompt.{i}.*`,
 *  - inference OUTPUT messages → indexed `gen_ai.completion.{i}.*`,
 *  - the LAST assistant completion is the trace.output candidate,
 *  - the pluggable `finalize` finalizer transforms the header text,
 *  - `emitTraceContent = false` (prod-without-consent) withholds trace.output.
 */
class LangfuseSpanAdapterTest {

    private fun user(text: String) = Message.User(text, RequestMetaInfo.Empty)
    private fun assistant(text: String) = Message.Assistant(text, ResponseMetaInfo.Empty)

    private fun contentOf(attr: ai.koog.agents.features.opentelemetry.attribute.CustomAttribute): String =
        (attr.value as? HiddenString)?.value ?: attr.value.toString()

    @Test
    fun `decomposeInput emits indexed prompt role + content`() {
        val attrs = LangfuseSpanReshaping.decomposeInput(listOf(user("hello"), user("world")))

        attrs.map { it.key } shouldContainExactly listOf(
            "gen_ai.prompt.0.role",
            "gen_ai.prompt.0.content",
            "gen_ai.prompt.1.role",
            "gen_ai.prompt.1.content",
        )
        attrs.first { it.key == "gen_ai.prompt.0.role" }.value shouldBe "user"
    }

    @Test
    fun `decomposeOutput emits indexed completion role + content`() {
        val attrs = LangfuseSpanReshaping.decomposeOutput(listOf(assistant("the answer")))

        attrs.map { it.key } shouldContainExactly listOf(
            "gen_ai.completion.0.role",
            "gen_ai.completion.0.content",
        )
        attrs.first { it.key == "gen_ai.completion.0.role" }.value shouldBe "assistant"
        contentOf(attrs.first { it.key == "gen_ai.completion.0.content" }) shouldBe "the answer"
    }

    @Test
    fun `lastAssistantText returns the final assistant message`() {
        LangfuseSpanReshaping.lastAssistantText(
            listOf(assistant("first"), assistant("last")),
        ) shouldBe "last"
    }

    @Test
    fun `traceOutputAttribute lifts the last completion to the trace header`() {
        val attr = LangfuseSpanReshaping.traceOutputAttribute(
            lastCompletion = "final text",
            emitTraceContent = true,
            finalize = { it },
        )
        attr?.key shouldBe "langfuse.trace.output"
        attr?.value shouldBe "final text"
    }

    @Test
    fun `traceOutputAttribute applies the finalize transform`() {
        val attr = LangfuseSpanReshaping.traceOutputAttribute(
            lastCompletion = "hi",
            emitTraceContent = true,
            finalize = { it.uppercase() },
        )
        attr?.value shouldBe "HI"
    }

    @Test
    fun `traceOutputAttribute is withheld when content is not allowed (prod zero-retention)`() {
        LangfuseSpanReshaping.traceOutputAttribute(
            lastCompletion = "secret",
            emitTraceContent = false,
            finalize = { it },
        ).shouldBeNull()
    }

    @Test
    fun `traceOutputAttribute is null when there is no completion`() {
        LangfuseSpanReshaping.traceOutputAttribute(
            lastCompletion = null,
            emitTraceContent = true,
            finalize = { it },
        ).shouldBeNull()
    }
}
