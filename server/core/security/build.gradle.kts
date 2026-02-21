plugins {
    id("server-module-convention")
}

group = "com.m2f.core"

dependencies {
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(projects.server.core.config)
}
