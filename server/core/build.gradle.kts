group = "com.m2f.core"

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

    kotlin {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xcontext-parameters"))
        }
    }
}