plugins {
    id("server-module-convention")
}

group = "com.m2f.core"

dependencies {
    // Env (Langfuse config) lives in server:core:config; observability depends on it (never the reverse).
    api(projects.server.core.config)

    implementation(libs.bundles.fp)
    api(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Koog OpenTelemetry agent feature + the Java OTLP/HTTP span exporter (the process-lifetime SDK).
    implementation(libs.koog.agents)
    implementation(libs.koog.agents.otel)
    implementation(libs.otel.exporter.otlp)

    // Ktor client for the Langfuse Public REST clients (prompt/scores/dataset).
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // SLF4J API for the prompt provider / scheduler logging seams.
    implementation(libs.slf4j.api)

    // Testing
    testImplementation(libs.bundles.testing.server)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
}
