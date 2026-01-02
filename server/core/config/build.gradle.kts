group = "com.m2f.core"

dependencies {
    implementation(libs.bundles.fp)
    implementation(libs.bundles.ktor.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.bundles.ktor.monitoring)
    api(libs.kotlinx.coroutines)

    // Testing
    testImplementation(libs.bundles.testing.server)
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
}

ktor {
    openApi {
        enabled = true
    }
}