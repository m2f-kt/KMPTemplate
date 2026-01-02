plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

// Apply all plugins to subprojects
allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

allprojects {
    apply {
        plugin(rootProject.libs.plugins.kotlinJvm.get().pluginId)
        plugin(rootProject.libs.plugins.kotlinx.serialization.get().pluginId)
        plugin(rootProject.libs.plugins.kover.get().pluginId)
        plugin(rootProject.libs.plugins.detekt.get().pluginId)
    }

    dependencies {
        implementation(rootProject.libs.bundles.di)
    }
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
    implementation(projects.server.core.config)
    implementation(projects.server.core.database)
    implementation(libs.logback)
    implementation(libs.bundles.ktor.security)
    implementation(libs.bundles.ktor.core)
    implementation(libs.bundles.ktor.monitoring)
    implementation(libs.bundles.di)
    implementation(libs.bundles.suspendapp)
    implementation(libs.bundles.fp)
    testImplementation(libs.kotlin.testJunit)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
}

ktor {
    openApi {
        title = "OpenAPI Documentation"
        version = "1.0"
        summary = "This is the API documentation for the project: M2F Template"
        enabled = true
    }
}