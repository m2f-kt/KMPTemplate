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
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.turbine)
            api(libs.kotest.assertionsCore)
            api(libs.kotest.arrow)
            api(libs.kotlinx.coroutines.test)
            api(libs.kotlin.test)
            api(libs.multiplatform.settings)
            implementation(projects.core.mvi)
            implementation(projects.core.models)
            implementation(projects.core.sdk)
            implementation(libs.arrow.core)
            implementation(libs.kotlinx.coroutines)
        }
        jvmMain.dependencies {
            api(libs.kotlin.testJunit)
        }
        androidMain.dependencies {
            api(libs.kotlin.testJunit)
        }
    }
}

// core:testing transitively links Compose (core:mvi -> lifecycle-viewmodel-compose) but does NOT
// apply the Compose Gradle plugin, so the wasmJs *browser* test runner cannot resolve skiko's npm
// module ('skiko.mjs'). The test-DSL logic is covered on jvm/native/ios/android; skip only the
// wasmJs browser test RUNNER (the wasmJs target itself still compiles/links).
tasks.matching { it.name == "wasmJsBrowserTest" }.configureEach { enabled = false }

android {
    namespace = "com.m2f.template.core.testing"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
