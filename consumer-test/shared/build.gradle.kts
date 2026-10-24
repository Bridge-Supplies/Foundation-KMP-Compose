import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(foundationLibs.plugins.kotlinMultiplatform)
    alias(foundationLibs.plugins.androidMultiplatformLibrary)
    alias(foundationLibs.plugins.jetbrainsCompose)
    alias(foundationLibs.plugins.compose.compiler)
    alias(foundationLibs.plugins.kotlinxSerialization)
}

kotlin {
    android {
        namespace = "bridge.supplies.consumertest"
        compileSdk = foundationLibs.versions.android.compileSdk.get().toInt()
        minSdk = foundationLibs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm("desktop")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ConsumerShared"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Resolved from the included Foundation build (dependency substitution by group:name)
            implementation("bridge.supplies.foundation:foundation-ui:${foundationLibs.versions.app.versionName.get()}")
            implementation(foundationLibs.kotlinx.serialization.json)
            implementation(foundationLibs.compose.material.icons.extended)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(foundationLibs.kotlinx.coroutines.test)
        }
        
        named("desktopTest") {
            dependencies {
                // Skia native library for the host OS (Compose graphics classes load it)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
