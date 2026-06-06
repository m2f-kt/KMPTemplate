package com.m2f.core.observability

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import kotlin.test.Test

/**
 * Two-environment privacy model. The trace content (`langfuse.trace.input`) is the sensitive payload;
 * it MUST be withheld in `production` without consent and present otherwise — while metadata
 * (`langfuse.environment`, session, model) always flows. A wrong gate here silently ships content to
 * the observability backend in violation of zero-retention.
 */
class TracePrivacyTest {

    @Test
    fun `development always allows content`() {
        traceContentAllowed(environment = "development", consent = false).shouldBeTrue()
        traceContentAllowed(environment = "development", consent = true).shouldBeTrue()
    }

    @Test
    fun `production allows content only with consent`() {
        traceContentAllowed(environment = "production", consent = false).shouldBeFalse()
        traceContentAllowed(environment = "production", consent = true).shouldBeTrue()
    }

    @Test
    fun `production without consent omits the input but keeps metadata`() {
        val keys = buildTraceAttributes(
            context = TraceContext(
                traceName = "agent",
                input = "the sensitive input",
                sessionId = "sess-1",
                userId = "user-1",
                extraMetadata = mapOf("model" to "gpt-4o-mini"),
            ),
            environment = "production",
            contentAllowed = false,
        ).map { it.key }

        keys shouldNotContain "langfuse.trace.input"
        keys shouldContain "langfuse.environment"
        keys shouldContain "langfuse.session.id"
        keys shouldContain "langfuse.trace.metadata.model"
    }

    @Test
    fun `content-allowed run includes the input`() {
        val keys = buildTraceAttributes(
            context = TraceContext(traceName = "agent", input = "the input"),
            environment = "development",
            contentAllowed = true,
        ).map { it.key }
        keys shouldContain "langfuse.trace.input"
    }
}
