plugins {
    id("server-module-convention")
}

group = "com.m2f.server.auth"

dependencies {
    api(projects.server.auth.contract)
    api(projects.server.auth.impl)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(libs.bundles.di)
    implementation(libs.bundles.fp)
}
