plugins {
    id("server-module-convention")
}

group = "com.m2f.server.privacy"

dependencies {
    implementation(projects.server.privacy.contract)
    implementation(projects.server.auth.contract)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.core.security)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.bundles.di)
    implementation(libs.exposed.core)
    implementation(libs.exposed.date.time)
    testImplementation(libs.bundles.testing.server)
}
