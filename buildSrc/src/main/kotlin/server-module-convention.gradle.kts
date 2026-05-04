import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlinx.kover")
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
}

// Detekt is enabled per-module with per-module baselines so existing tech debt
// can be tracked without blocking CI. New violations still fail the build.
// The shipped build-config/detekt-config.yml references plugins not on the
// classpath (formatting, Compose) so we use detekt defaults for now.
extensions.configure<DetektExtension>("detekt") {
    baseline = file("detekt-baseline.xml")
    buildUponDefaultConfig = true
    autoCorrect = false
}
