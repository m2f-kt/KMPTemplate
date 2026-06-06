plugins {
    id("server-module-convention")
    alias(libs.plugins.ktor)
    application
}

group = "com.m2f.template"
version = "1.0.0"
application {
    mainClass.set("com.m2f.template.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

// `.env` is the source of truth for the local `:server:run`: inject it into the run JVM at EXECUTION
// time so its values OVERRIDE any ambient shell var of the same name (e.g. a stray globally-exported
// EXAMPLE_VAR). The server still reads dotenv-kotlin at runtime, but with the .env values already in
// System.getenv, its `System.getenv(key) ?: dotenv[key]` precedence resolves to .env — killing the
// silent shell override. Dev-only: production runs the fat JAR with real platform env, not this task.
tasks.named<JavaExec>("run") {
    doFirst {
        EnvLoader.load(rootProject.projectDir).forEach { (k, v) -> environment(k, v) }
    }
}

dependencies {
    implementation(projects.core.models)
    implementation(projects.shared)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    implementation(projects.server.auth.wire)
    implementation(projects.server.groups.wire)
    implementation(projects.server.files.wire)
    implementation(projects.server.ai.wire)
    implementation(projects.server.privacy.wire)
    implementation(libs.bundles.logging.server)
    implementation(libs.bundles.ktor.security)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.bundles.ktor.monitoring)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.bundles.di)
    implementation(libs.bundles.suspendapp)
    implementation(libs.bundles.fp)
    testImplementation(libs.kotlin.testJunit)
}
