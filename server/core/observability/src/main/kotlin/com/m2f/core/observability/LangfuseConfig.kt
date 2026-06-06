package com.m2f.core.observability

/**
 * Langfuse OTLP exporter coordinates for the OpenTelemetry feature. A non-null instance turns tracing
 * ON ([ProcessLifetimeOtelSdk] builds the exporter once and shares it across every per-call agent);
 * null leaves the exporter uninstalled and the graph byte-identical to the untraced path.
 *
 * Build it from `Env.Observability` only when `langfuseEnabled` is true (both keys present), so a
 * deployment without Langfuse keys carries a null config and pays zero tracing cost.
 *
 * @param host Langfuse OTLP base URL (host only; the SDK appends the OTLP traces path).
 * @param publicKey Langfuse project public key (`pk-lf-…`).
 * @param secretKey Langfuse project secret key (`sk-lf-…`).
 * @param environment the Langfuse environment tag (`development` / `production`). It alone drives
 * content visibility (see [traceContentAllowed]): in `development` (and consented `production`) the
 * transcript + generation content are emitted UNMASKED; in `production` without consent they are
 * masked/omitted (the two-environment zero-retention model).
 */
data class LangfuseConfig(
    val host: String,
    val publicKey: String,
    val secretKey: String,
    val environment: String = "development",
)
