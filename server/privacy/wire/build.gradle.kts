plugins {
    id("server-module-convention")
}

group = "com.m2f.server.privacy"

dependencies {
    api(projects.server.privacy.contract)
    api(projects.server.privacy.impl)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(libs.bundles.di)
    implementation(libs.bundles.fp)
}
