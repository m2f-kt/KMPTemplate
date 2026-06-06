package com.m2f.core.observability

import ai.koog.agents.features.opentelemetry.attribute.CustomAttribute

/**
 * Langfuse environment that is zero-retention by default: in `production` the trace content (trace
 * input/output + per-generation content) is OMITTED unless the caller explicitly consented. Every
 * other environment (e.g. `development`) sends full fidelity.
 */
const val LANGFUSE_PROD_ENVIRONMENT: String = "production"

/**
 * Whether the trace content (trace input/output + unmasked generation content) may be sent to
 * Langfuse for this run. The two-environment privacy model: full fidelity everywhere EXCEPT
 * `production` without consent, where only metadata (cost, latency, prompt version, scores) is
 * allowed through. Pure so the matrix is unit-tested in isolation.
 */
fun traceContentAllowed(environment: String, consent: Boolean): Boolean =
    environment != LANGFUSE_PROD_ENVIRONMENT || consent

/**
 * Builds the Langfuse trace-level attributes for one agent run. Session/user grouping, the trace
 * name, the environment, and any [extraMetadata] are ALWAYS emitted (they carry no content). The
 * [input] (`langfuse.trace.input`) is added ONLY when [contentAllowed] — this is the prod
 * zero-retention guard, since `langfuse.trace.*` strings are sent UNMASKED (the `setVerbose` mask
 * only covers the per-generation `gen_ai.*` content, not these trace attributes). The trace OUTPUT
 * is added later by [LangfuseSpanAdapter], also gated on [contentAllowed].
 *
 * Top-level so the omit/keep behaviour is directly unit-testable without a live exporter.
 *
 * @param context the per-run identity (session/user/name/input + metadata) — see [TraceContext].
 * @param environment the Langfuse environment tag (drives [contentAllowed] via [traceContentAllowed]).
 * @param contentAllowed the privacy gate; when false the [TraceContext.input] is omitted.
 */
fun buildTraceAttributes(
    context: TraceContext,
    environment: String,
    contentAllowed: Boolean,
): List<CustomAttribute> = buildList {
    add(CustomAttribute("langfuse.trace.name", context.traceName))
    add(CustomAttribute("langfuse.environment", environment))
    context.sessionId?.let { add(CustomAttribute("langfuse.session.id", it)) }
    context.userId?.let { add(CustomAttribute("langfuse.user.id", it)) }
    if (contentAllowed) {
        add(CustomAttribute("langfuse.trace.input", context.input))
    }
    context.extraMetadata.forEach { (key, value) ->
        add(CustomAttribute("langfuse.trace.metadata.$key", value))
    }
}

/**
 * The per-run trace identity for one agent invocation. Content-free by contract EXCEPT [input]
 * (the only field gated on consent in [buildTraceAttributes]).
 *
 * @param extraMetadata freeform `key -> value` pairs emitted as `langfuse.trace.metadata.<key>`
 *   (models, language pairs, flags) — never put content/transcript text here.
 */
data class TraceContext(
    val traceName: String,
    val input: String,
    val sessionId: String? = null,
    val userId: String? = null,
    val extraMetadata: Map<String, String> = emptyMap(),
)
