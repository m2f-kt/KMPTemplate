plugins {
    id("server-module-convention")
}

group = "com.m2f.server.ai"

dependencies {
    api(projects.server.ai.contract)
    api(projects.server.ai.impl)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(libs.bundles.di)
}
