plugins {
    id("server-module-convention")
}

group = "com.m2f.server.privacy"

dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.auth.contract)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.exposed.core)
    implementation(libs.exposed.date.time)
}
