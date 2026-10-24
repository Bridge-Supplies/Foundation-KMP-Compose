rootProject.name = "Foundation"
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
    }
}

plugins {
    // Auto-provisions the JDK requested by gradle/gradle-daemon-jvm.properties (and toolchains) on any machine
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
}

include(":foundation-core") // Foundation library: storage, crypto, platform services (no UI)
include(":foundation-ui") // Foundation library: theme, shell, components
include(":composeApp") // demo app shared code (KMP library)
include(":androidApp") // Android entry point
include(":desktopApp") // Desktop (JVM) entry point