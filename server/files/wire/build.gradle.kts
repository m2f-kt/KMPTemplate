plugins {
    id("server-module-convention")
}

group = "com.m2f.server.files"

dependencies {
    api(projects.server.files.contract)
    api(projects.server.files.impl)
    implementation(libs.bundles.di)
}
