plugins {
    id("server-module-convention")
}

group = "com.m2f.server"

dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(projects.server.auth)
    implementation(libs.bundles.koog)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.di)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.websockets)
    testImplementation(libs.bundles.testing.server)
}
