package com.m2f.core.observability

import ai.koog.agents.features.opentelemetry.attribute.CustomAttribute
import ai.koog.agents.features.opentelemetry.attribute.GenAIAttributes
import ai.koog.agents.features.opentelemetry.attribute.KoogAttributes
import ai.koog.agents.features.opentelemetry.integration.SpanAdapter
import ai.koog.agents.features.opentelemetry.span.GenAIAgentSpan
import ai.koog.agents.features.opentelemetry.span.SpanType
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Generic drop-in replacement for Koog's stock `LangfuseSpanAdapter`. It does the one thing the stock
 * adapter cannot: it sets `langfuse.trace.output` on the trace so the Langfuse trace HEADER shows the
 * agent's final text instead of `undefined`.
 *
 * Source of truth: the `koog-langfuse-tracing` skill's reference `TraceIoLangfuseSpanAdapter` — this
 * is that adapter, generalized (no product-specific JSON-envelope unwrap). Why not subclass or
 * compose the stock adapter? Koog's `addSpanAdapter` keeps a SINGLE adapter (a later add REPLACES the
 * prior), and `LangfuseSpanAdapter` is `internal` — so it can be neither wrapped nor stacked. This
 * adapter therefore reproduces the stock reshaping we rely on (trace-attribute propagation to every
 * span, inference prompt/completion decomposition into the indexed `gen_ai.prompt.{i}` /
 * `gen_ai.completion.{i}` attributes Langfuse renders, and langgraph node metadata) and layers the
 * trace-output capture on top. It intentionally omits the stock `langfuse.session.id = runId` write
 * so the caller's `langfuse.session.id` (in [traceAttributes]) is the only one set.
 *
 * Verified against Koog `agents-features-opentelemetry` 1.0.0-preview7. No tool-call handling: assumes
 * a tool-free agent (pure LLM inference nodes); port the tool-call branches from Koog's
 * `LangfuseSpanAdapter` source if a graph uses tools.
 *
 * ONE instance per agent run (the agent is built per call), so the captured references are single-run
 * state; do NOT share an instance across concurrent runs.
 *
 * @param traceAttributes the trace-level Langfuse attributes (see [buildTraceAttributes]). Propagated
 *   to EVERY span, which is how `langfuse.trace.input` / session / user reach the trace ROOT.
 * @param emitTraceContent when false (prod-without-consent), the trace OUTPUT — a plain UNMASKED
 *   string — is NOT emitted (metadata-only). Mirror the trace-INPUT omission in [buildTraceAttributes].
 * @param finalize maps the last inference completion text to what lands on the trace header. If a
 *   final node returns a structured-output envelope (`{"answer":"…"}`), unwrap it here so the header
 *   shows bare text. Default = identity.
 */
class LangfuseSpanAdapter(
    private val traceAttributes: List<CustomAttribute>,
    private val emitTraceContent: Boolean = true,
    private val finalize: (lastCompletion: String) -> String = { it },
) : SpanAdapter() {

    private val stepKey = AtomicInteger(0)
    private val lastCompletion = AtomicReference<String?>(null)

    override fun onBeforeSpanStarted(span: GenAIAgentSpan) {
        when (span.type) {
            SpanType.INFERENCE -> decomposeInput(span)
            SpanType.NODE -> tagNode(span)
            else -> {}
        }
        // Langfuse recommends propagating trace attributes onto every span; that is also how
        // `langfuse.trace.input` / session / user reach the root span.
        traceAttributes.forEach { span.addAttribute(it) }
    }

    override fun onBeforeSpanFinished(span: GenAIAgentSpan) {
        when (span.type) {
            SpanType.INFERENCE -> decomposeOutput(span)
            // Langfuse promotes `langfuse.trace.output` from the trace ROOT span — which is
            // CREATE_AGENT (`parentSpan == null`), NOT its INVOKE_AGENT child. CREATE_AGENT finishes
            // last (on agent close), by which point [lastCompletion] is set from the inference spans.
            // Set both for robustness against Langfuse's promotion rule; the root is the one that counts.
            SpanType.CREATE_AGENT, SpanType.INVOKE_AGENT -> emitTraceOutput(span)
            else -> {}
        }
    }

    private fun emitTraceOutput(span: GenAIAgentSpan) {
        // Privacy: the trace OUTPUT is a plain (unmasked) string, so in production-without-consent we
        // simply do not emit it (metadata-only). Mirrors the trace INPUT omission in the builder.
        LangfuseSpanReshaping.traceOutputAttribute(lastCompletion.get(), emitTraceContent, finalize)
            ?.let { span.addAttribute(it) }
    }

    /** Decompose `gen_ai.input.messages` into the indexed prompt attributes Langfuse renders. */
    private fun decomposeInput(span: GenAIAgentSpan) {
        val messages = span.attributes.filterIsInstance<GenAIAttributes.Input.Messages>()
            .firstOrNull()
            ?.messages
            .orEmpty()
        LangfuseSpanReshaping.decomposeInput(messages).forEach { span.addAttribute(it) }
    }

    /**
     * Decompose `gen_ai.output.messages` into the indexed completion attributes Langfuse renders, and
     * remember the last assistant text so the trace ROOT can lift it to `langfuse.trace.output`. The
     * agent (INVOKE_AGENT) span carries only the empty base prompt, so the final text is NOT on it —
     * the inference spans are the only place it lives.
     */
    private fun decomposeOutput(span: GenAIAgentSpan) {
        val messages = span.attributes.filterIsInstance<GenAIAttributes.Output.Messages>()
            .firstOrNull()
            ?.messages
            .orEmpty()
        LangfuseSpanReshaping.decomposeOutput(messages).forEach { span.addAttribute(it) }
        LangfuseSpanReshaping.lastAssistantText(messages)?.let { text ->
            lastCompletion.set(text)
            // Self-hosted Langfuse v3 gotcha #1: ALSO stamp the trace OUTPUT on THIS inference span.
            // The root CREATE_AGENT / INVOKE_AGENT spans (where emitTraceOutput sets it) are NOT
            // reliably ingested as observations on self-hosted v3, so the trace header showed no output
            // even though the generations did. INFERENCE spans ARE ingested; the last one to finish
            // wins the trace.output upsert -> the user-facing text. Gated on emitTraceContent.
            LangfuseSpanReshaping.traceOutputAttribute(text, emitTraceContent, finalize)
                ?.let { span.addAttribute(it) }
        }
    }

    /** Mirror the stock adapter's langgraph step/node metadata so Langfuse labels the graph nodes. */
    private fun tagNode(span: GenAIAgentSpan) {
        // Self-hosted Langfuse v3 gotcha #2: emit identity/version as FREEFORM metadata
        // (`langfuse.observation.metadata.*`), NEVER the reserved `langfuse.prompt.*` int field — the
        // KMP OTel SDK encodes an Int as an OTLP int (string-in-JSON), and v3's strict ingestion
        // requires a NUMBER there, so the reserved field makes the worker reject the WHOLE observation
        // ("expected number, received string"). Freeform metadata has no type schema, so it ingests cleanly.
        span.addAttribute(
            CustomAttribute("langfuse.observation.metadata.langgraph_step", stepKey.getAndIncrement()),
        )
        span.attributes.find { it.key == KoogAttributes.Koog.Node.Id("").key }?.value?.let { nodeId ->
            span.addAttribute(CustomAttribute("langfuse.observation.metadata.langgraph_node", nodeId))
        }
    }
}
