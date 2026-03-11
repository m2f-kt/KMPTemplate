plugins {
    id("server-module-convention")
}

group = "com.m2f.server.auth"

dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.jbcrypt)
    implementation(libs.exposed.core)
    implementation(libs.exposed.date.time)
}
