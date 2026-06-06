plugins {
    id("kmp-library-convention")
    id("com.android.library")
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
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(libs.navigation3.ui)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotest.assertionsCore)
        }
    }
}

// core:navigation transitively links Compose (navigation3's NavKey via navigation3.ui) but does
// NOT apply the Compose Gradle plugin, so the wasmJs *browser* test runner cannot resolve skiko's
// npm module ('skiko.mjs'). The pure kotlinx-coroutines logic here is covered on JVM/native/iOS/
// Android; skip only the wasmJs browser test RUNNER — the wasmJs target itself still compiles/links.
tasks.matching { it.name == "wasmJsBrowserTest" }.configureEach { enabled = false }

android {
    namespace = "com.m2f.template.core.navigation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
