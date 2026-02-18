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
            implementation(projects.core.mvi)
            implementation(projects.core.models)
            implementation(libs.arrow.core)
            implementation(libs.kotlinx.coroutines)
        }
        jvmMain.dependencies {
            api(libs.kotlin.testJunit)
        }
    }
}

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
