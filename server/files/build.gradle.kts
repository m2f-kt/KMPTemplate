plugins {
    id("server-module-convention")
}

group = "com.m2f.server"

dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.auth)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.bundles.di)
    implementation(libs.aws.s3)
    testImplementation(projects.server.core.database)
    testImplementation(projects.server.core.security)
    testImplementation(libs.bundles.testing.server)
    testImplementation(libs.ktor.client.content.negotiation)
}
