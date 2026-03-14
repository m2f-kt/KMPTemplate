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
include("core:navigation")
include("core:testing")
include("core:sdk")
include("core:storage")

// Server feature modules
include("server:auth")
include("server:auth:contract")
include("server:auth:impl")
include("server:auth:wire")
include("server:groups")
include("server:groups:contract")
include("server:groups:impl")
include("server:groups:wire")
include("server:files")
include("server:files:contract")
include("server:files:impl")
include("server:files:wire")
include("server:ai")
include("server:ai:contract")
include("server:ai:impl")
include("server:ai:wire")
include("server:privacy")
include("server:privacy:contract")
include("server:privacy:impl")
include("server:privacy:wire")

// App feature modules
include("app:auth:contract")
include("app:auth:impl")
include("app:auth:wire")
include("app:admin:contract")
include("app:admin:impl")
include("app:admin:wire")
include("app:dashboard:contract")
include("app:dashboard:impl")
include("app:dashboard:wire")
include("app:designsystem")
include("app:documents:contract")
include("app:documents:impl")
include("app:documents:wire")
include("app:profile:contract")
include("app:profile:impl")
include("app:profile:wire")