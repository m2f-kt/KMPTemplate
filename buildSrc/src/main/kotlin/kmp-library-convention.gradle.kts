// Convention plugin for KMP library modules (core:models, core:sdk, core:storage)
// Applied by KMP modules that target Android, iOS, JVM, and WASM
// Each module still declares its own KMP targets and dependencies
// This plugin provides the shared compiler configuration

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
}
