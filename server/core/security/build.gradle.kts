group = "com.m2f.core"

dependencies {
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.kotlinx.coroutines)
    implementation(projects.server.core.config)

}

kotlin {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
}