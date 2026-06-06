plugins {
    id("kmp-library-convention")
    id("com.android.library")
}

// Generate a Kotlin file holding constants pulled from the project's .env file
// (falls back to .env.example, then a hardcoded default). This keeps the client
// SDK's default base URL in sync with the server's runtime PORT without
// requiring developers to edit Kotlin sources after changing .env.
val sdkGeneratedSrcDir: Provider<Directory> =
    layout.buildDirectory.dir("generated/source/sdkConfig/commonMain/kotlin")

val generateSdkConfig by tasks.registering {
    val envFile = rootProject.file(".env")
    val envExampleFile = rootProject.file(".env.example")
    val outDir = sdkGeneratedSrcDir

    inputs.property("envFilePresent", envFile.exists())
    if (envFile.exists()) inputs.file(envFile)
    if (envExampleFile.exists()) inputs.file(envExampleFile)
    outputs.dir(outDir)

    doLast {
        fun readPort(file: File): Int? = file.takeIf { it.exists() }?.useLines { lines ->
            lines.map { it.trim() }
                .filterNot { it.isEmpty() || it.startsWith("#") }
                .map { it.split("=", limit = 2) }
                .firstOrNull { it.size == 2 && it[0].trim() == "PORT" }
                ?.get(1)?.trim()?.toIntOrNull()
        }

        val port = readPort(envFile) ?: readPort(envExampleFile) ?: 8080

        val pkgDir = outDir.get().asFile.resolve("com/m2f/template/sdk")
        pkgDir.mkdirs()
        pkgDir.resolve("SdkConfig.kt").writeText(
            """
            // Generated from .env at build time. Do not edit manually.
            package com.m2f.template.sdk

            internal const val DEFAULT_DEV_PORT: Int = $port
            """.trimIndent() + "\n"
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateSdkConfig)
        }
        commonMain.dependencies {
            api(projects.core.models)
            api(projects.core.storage)
            implementation(libs.arrow.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.koin.core)
            implementation(libs.kermit)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotest.assertionsCore)
            implementation(libs.kotest.arrow)
            implementation(libs.ktor.client.mock)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.m2f.template.sdk"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
