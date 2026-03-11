plugins {
    id("server-module-convention")
}

group = "com.m2f.server.files"

dependencies {
    implementation(projects.server.files.contract)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(project(":server:auth:contract"))
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.bundles.di)
    implementation(libs.aws.s3)
    testImplementation(projects.server.core.database)
    testImplementation(projects.server.core.security)
    testImplementation(project(":server:auth:wire"))
    testImplementation(libs.bundles.testing.server)
    testImplementation(libs.ktor.client.content.negotiation)
}
