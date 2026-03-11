plugins {
    id("server-module-convention")
}

group = "com.m2f.server.ai"

dependencies {
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
}
