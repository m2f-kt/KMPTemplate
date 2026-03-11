plugins {
    id("server-module-convention")
}

group = "com.m2f.server.auth"

dependencies {
    implementation(projects.server.auth.contract)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.jbcrypt)
    implementation(libs.jakarta.mail)
    implementation(libs.ktor.server.resources)
    implementation(libs.bundles.di)
    testImplementation(libs.bundles.testing.server)
    testImplementation(libs.greenmail)
}
