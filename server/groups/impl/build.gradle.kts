plugins {
    id("server-module-convention")
}

group = "com.m2f.server.groups"

dependencies {
    implementation(projects.server.groups.contract)
    implementation(project(":server:auth:contract"))
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.bundles.di)
    testImplementation(project(":server:auth:wire"))
    testImplementation(libs.bundles.testing.server)
    testImplementation(libs.ktor.client.content.negotiation)
}
