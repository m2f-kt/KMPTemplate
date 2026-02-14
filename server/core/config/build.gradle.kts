plugins {
    id("server-module-convention")
}

group = "com.m2f.core"

dependencies {
    api(projects.core.models)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.koin.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.bundles.ktor.monitoring)
    implementation(libs.ktor.server.sse)
    api(libs.kotlinx.coroutines)

    // Testing
    testImplementation(libs.bundles.testing.server)
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
}
