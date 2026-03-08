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
            api(libs.arrow.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.m2f.template.models"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

// Coverage measured on JVM target only (WASM/iOS native not instrumented by Kover).
// Threshold starts at 0 — raise incrementally as tests are added to this module.
kover {
    reports {
        verify {
            rule("Minimum line coverage") {
                minBound(0)
            }
        }
    }
}
