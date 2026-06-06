package com.m2f.core.observability

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.toOtelKotlinSpanExporter
import java.util.Base64
import java.util.UUID

/**
 * Coordinates for the process-lifetime OpenTelemetry SDK that ships traces to a Langfuse OTLP/HTTP
 * endpoint. Generic — no product specifics; a fork supplies its own [serviceName] / [serviceVersion].
 *
 * @param host Langfuse OTLP base URL (host only; [LANGFUSE_OTLP_TRACES_PATH] is appended).
 * @param headers extra HTTP headers for the exporter; the `Authorization: Basic …` header is added
 *   automatically from the Langfuse keys (see [forLangfuse]). Use this for any additional headers.
 * @param serviceName OpenTelemetry `service.name` resource attribute — the owning service in Langfuse.
 * @param serviceVersion OpenTelemetry `service.version` resource attribute.
 */
data class OtelExporterConfig(
    val host: String,
    val headers: Map<String, String>,
    val serviceName: String,
    val serviceVersion: String,
) {
    companion object {
        /** Builds a config whose only header is the Langfuse HTTP-Basic `pk:sk` authorization. */
        fun forLangfuse(
            config: LangfuseConfig,
            serviceName: String,
            serviceVersion: String,
        ): OtelExporterConfig = OtelExporterConfig(
            host = config.host,
            headers = mapOf("Authorization" to "Basic ${basicAuth(config.publicKey, config.secretKey)}"),
            serviceName = serviceName,
            serviceVersion = serviceVersion,
        )

        /** Langfuse OTLP ingest authenticates via HTTP Basic with `base64(publicKey:secretKey)`. */
        private fun basicAuth(publicKey: String, secretKey: String): String =
            Base64.getEncoder().encodeToString("$publicKey:$secretKey".encodeToByteArray())
    }
}

/** Langfuse's OTLP/HTTP traces ingest path, appended to the configured host. */
const val LANGFUSE_OTLP_TRACES_PATH: String = "/api/public/otel/v1/traces"

/**
 * Builds the PROCESS-LIFETIME OpenTelemetry SDK — ONE SDK for the whole process, shared by every
 * per-call agent via
 * [setSdk][ai.koog.agents.features.opentelemetry.feature.OpenTelemetryConfigAPI.setSdk].
 *
 * This is the interlocking fix for the FLAT / partial trace tree on a long-lived server, kept as ONE
 * unit (do not split it — each piece is load-bearing):
 *  1. **Process-lifetime SDK** (built once, never rebuilt per call) — no per-call OTel state
 *     accumulation; any future graph shape traces completely.
 *  2. **[NonClosingOpenTelemetry] wrapper** — a per-call agent close calls `shutdown()`; making it a
 *     no-op keeps the shared span pipeline alive while `closeSdks()` still releases the per-call
 *     `SdkMeterProvider` Koog builds per install. (Keep `setShutdownOnAgentClose(true)` at the call
 *     site for exactly that reason.)
 *  3. **SIMPLE span processor** (NOT batch) — Koog preview7's KMP batch processor never drains on
 *     close in a long-lived server. The standard Java OTLP/HTTP exporter is bridged to the KMP SDK.
 *  4. **JVM shutdown hook** releases the OkHttp exporter only at process exit — never on agent close.
 *  5. **Resource attributes** — `setSdk` makes Koog's internal resource construction inert, so the
 *     service identity + OS attributes Koog would otherwise add are reproduced here.
 *
 * Hold the returned instance as a DI singleton and pass it to `setSdk(...)` in every per-call
 * `install(OpenTelemetry) { … }` block.
 */
@OptIn(io.opentelemetry.kotlin.ExperimentalApi::class)
fun buildProcessLifetimeSdk(config: OtelExporterConfig): io.opentelemetry.kotlin.OpenTelemetry {
    val exporterBuilder = OtlpHttpSpanExporter.builder()
        .setEndpoint("${config.host}$LANGFUSE_OTLP_TRACES_PATH")
    config.headers.forEach { (name, value) -> exporterBuilder.addHeader(name, value) }
    val exporter = exporterBuilder.build()
    // Release the OkHttp client only at JVM exit — never on a per-call agent close.
    Runtime.getRuntime().addShutdownHook(Thread { exporter.shutdown() })

    val resourceAttributes = buildMap<String, Any> {
        put("service.name", config.serviceName)
        put("service.version", config.serviceVersion)
        put("service.instance.id", UUID.randomUUID().toString())
        System.getProperty("os.name")?.let { put("os.type", it) }
        System.getProperty("os.version")?.let { put("os.version", it) }
        System.getProperty("os.arch")?.let { put("os.arch", it) }
    }
    val sdk = io.opentelemetry.kotlin.createOpenTelemetry {
        tracerProvider {
            resource(resourceAttributes)
            export { simpleSpanProcessor(exporter.toOtelKotlinSpanExporter()) }
        }
    }
    return NonClosingOpenTelemetry(sdk)
}

/**
 * Delegating [io.opentelemetry.kotlin.OpenTelemetry] whose [TelemetryCloseable]
 * [shutdown][io.opentelemetry.kotlin.export.TelemetryCloseable.shutdown] / `forceFlush` are NO-OPS.
 * Wraps the process-lifetime SDK so Koog's per-call `closeSdks()` (kept enabled to release the
 * per-call `SdkMeterProvider`) cannot tear down the SHARED span processor + exporter — those live
 * until the JVM-exit hook. Everything else delegates to the real SDK.
 */
@OptIn(io.opentelemetry.kotlin.ExperimentalApi::class)
internal class NonClosingOpenTelemetry(
    private val delegate: io.opentelemetry.kotlin.OpenTelemetry,
) : io.opentelemetry.kotlin.OpenTelemetry by delegate, io.opentelemetry.kotlin.export.TelemetryCloseable {
    override suspend fun forceFlush(): io.opentelemetry.kotlin.export.OperationResultCode =
        io.opentelemetry.kotlin.export.OperationResultCode.Success

    override suspend fun shutdown(): io.opentelemetry.kotlin.export.OperationResultCode =
        io.opentelemetry.kotlin.export.OperationResultCode.Success
}
