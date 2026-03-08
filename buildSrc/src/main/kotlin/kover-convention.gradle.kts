// Applies Kover coverage plugin. Coverage reporting and thresholds are configured
// per-module in each module's own build.gradle.kts, where the koverReport
// extension accessors are available because Kover types come from buildSrc.
plugins {
    id("org.jetbrains.kotlinx.kover")
}
