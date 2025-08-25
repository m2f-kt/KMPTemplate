plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "com.m2f.template"
version = "1.0.0"
application {
    mainClass.set("com.m2f.template.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.bundles.ktor.security)
    implementation(libs.bundles.ktor.core)
    implementation(libs.bundles.ktor.monitoring)
    implementation(libs.bundles.di)
    implementation(libs.bundles.suspendapp)
    implementation(libs.bundles.fp)
    testImplementation(libs.kotlin.testJunit)
}