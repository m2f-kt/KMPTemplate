rootProject.name = "template"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
include(":server")
include(":shared")
include("server:core")
include("server:core:config")
include("server:core:database")
include("server:core:security")

// Shared core modules
include("core:models")
include("core:mvi")
include("core:testing")
include("core:sdk")
include("core:storage")

// Server feature modules
include("server:auth")
include("server:groups")
include("server:files")
include("server:ai")

// App feature modules
include("app:auth")
include("app:admin")
include("app:dashboard")
include("app:designsystem")
include("app:documents:contract")
include("app:documents:impl")
include("app:documents:wire")
include("app:profile")