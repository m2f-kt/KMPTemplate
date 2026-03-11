plugins {
    id("server-module-convention")
}

group = "com.m2f.server.groups"

dependencies {
    api(projects.server.groups.contract)
    api(projects.server.groups.impl)
    implementation(projects.core.models)
    implementation(projects.server.core.config)
    implementation(libs.bundles.di)
}
