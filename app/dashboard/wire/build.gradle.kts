plugins {
    id("kmp-library-convention")
    id("com.android.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
            api(projects.app.dashboard.contract)
            implementation(projects.app.dashboard.impl)
            implementation(projects.core.mvi)
            implementation(projects.core.sdk)
            implementation(projects.app.auth.contract)
            implementation(projects.app.admin.contract)
            implementation(projects.app.profile.contract)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation3.ui)
        }
    }
}

android {
    namespace = "com.m2f.template.app.dashboard.wire"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
