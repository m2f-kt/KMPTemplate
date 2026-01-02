group = "com.m2f.core"

dependencies {
    api(libs.bundles.database.backend)
    implementation(libs.bundles.ktor.core)
    implementation(libs.bundles.fp)
    implementation(projects.server.core.config)


    // Testing
    testImplementation(libs.bundles.testing.server)
}
