import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.android.application")
    id("kover-convention")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            @Suppress("DEPRECATION")
            implementation(compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kermit)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation3.ui)
            implementation(projects.shared)
            implementation(projects.core.models)
            api(projects.core.navigation)
            implementation(projects.core.mvi)
            implementation(projects.core.sdk)
            implementation(projects.app.designsystem)
            implementation(projects.app.auth.wire)
            implementation(projects.app.admin.wire)
            implementation(projects.app.dashboard.wire)
            implementation(projects.app.documents.wire)
            implementation(projects.app.privacy.wire)
            implementation(projects.app.profile.wire)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.slf4j.simple)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotest.assertionsCore)
        }
    }
}

android {
    namespace = "com.m2f.template"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.m2f.template"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}

compose.desktop {
    application {
        mainClass = "com.m2f.template.MainKt"

        // Desktop runtime footprint caps (tunable defaults): bound the heap, use the serial GC
        // (lower baseline footprint than G1 for a single-window UI app), and let the JVM scale heap
        // to at most 50% of host RAM (default is 25%). `-Xdock:name` sets the dev-run dock label so
        // the tile reads the app name instead of "java" (packaged .app uses packageName instead).
        jvmArgs += listOf(
            "-Xmx256m",
            "-Xms64m",
            "-XX:+UseSerialGC",
            "-XX:MaxRAMPercentage=50",
            "-Xdock:name=template",
        )

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.m2f.template"
            packageVersion = "1.0.0"

            // Per-OS app icons for the packaged artifacts. Uncomment once real assets exist at the
            // referenced paths (the build will fail if iconFile points at a missing file).
            //   macOS .icns -> composeApp/src/jvmMain/resources/icons/macos.icns
            //   windows .ico -> composeApp/src/jvmMain/resources/icons/windows.ico
            //   linux .png  -> composeApp/src/jvmMain/resources/icons/linux.png
            macOS {
                // A custom entitlementsFile REPLACES Compose Desktop's defaults wholesale — the
                // skeleton at composeApp/entitlements.plist repeats the 3 mandatory JVM
                // hardened-runtime keys so the bundled JVM still launches.
                entitlementsFile.set(project.file("entitlements.plist"))
                // iconFile.set(project.file("src/jvmMain/resources/icons/macos.icns"))
            }
            windows {
                // iconFile.set(project.file("src/jvmMain/resources/icons/windows.ico"))
            }
            linux {
                // iconFile.set(project.file("src/jvmMain/resources/icons/linux.png"))
            }

            // Narrow the bundled JDK image: JLink strips every JDK module not listed here, shrinking
            // the packaged runtime. `modules.clear()` resets Compose's default (additive) list so
            // the list below is authoritative — without clear(), JLink won't narrow.
            //
            // - java.base: required, always included.
            // - java.desktop: AWT/Swing — Compose Desktop renders through it.
            // - java.naming: JNDI — pulled by SLF4J + some networking paths.
            // - java.sql: JDBC drivers (jvm target).
            // - java.management: JMX — used by GC and metrics in dev.
            // - jdk.unsupported: sun.misc.Unsafe — atomicfu + kotlinx.coroutines reach for it.
            //
            // If a packaged-app runtime error surfaces (e.g. NoClassDefFoundError on a java.* class),
            // add the missing module here and re-package.
            modules.clear()
            modules(
                "java.base",
                "java.desktop",
                "java.naming",
                "java.sql",
                "java.management",
                "jdk.unsupported",
            )
        }
    }
}

// Optional plist linter wired through the reusable buildSrc `plistLint` helper. Ships with an EMPTY
// required-key list, so it is a no-op by default (the template carries the MECHANISM, not a specific
// assertion). Add keys to `requiredKeys` to enforce them, and wire into `check` if desired.
tasks.register("checkPlist") {
    group = "verification"
    description = "Lint composeApp/entitlements.plist for required keys (no-op until keys are added)."
    val plist = file("entitlements.plist")
    inputs.file(plist)
    doLast {
        val requiredKeys = emptyList<String>()
        val problems = plistLint(plist, requiredKeys)
        require(problems.isEmpty()) { problems.joinToString("\n") }
        logger.lifecycle("checkPlist OK: ${plist.name} (${requiredKeys.size} required keys)")
    }
}

// Coverage is measured on the JVM target only (Kover does not instrument WASM/iOS native).
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
