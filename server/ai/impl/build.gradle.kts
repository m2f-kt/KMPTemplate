plugins {
    id("server-module-convention")
}

group = "com.m2f.server.ai"

dependencies {
    implementation(projects.server.ai.contract)
    implementation(project(":server:auth:contract"))
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(libs.bundles.koog)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.di)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.resources)
    testImplementation(project(":server:auth:wire"))
    testImplementation(projects.server.core.security)
    testImplementation(libs.bundles.testing.server)
    testImplementation(libs.ktor.client.content.negotiation)
}
