// Consumer test: builds against Foundation the way apps do, as an included build.
// Apps use `includeBuild("foundation")` (git submodule); here Foundation is the parent folder.
// Run with Foundation's wrapper: ./gradlew -p consumer-test <tasks> (see scripts/consumer-test.sh)
rootProject.name = "FoundationConsumerTest"

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
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
    }
    versionCatalogs {
        // Foundation's catalog, so plugin and library versions match the included build exactly
        create("foundationLibs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("..")

include(":shared")
