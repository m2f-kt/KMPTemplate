plugins {
    id("server-module-convention")
}

group = "com.m2f.server.groups"

dependencies {
    implementation(project(":server:auth:contract"))
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.exposed.core)
    implementation(libs.exposed.date.time)
}
