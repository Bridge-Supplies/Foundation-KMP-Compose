import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "bridge.supplies.foundation"
version = libs.versions.app.versionName.get()

compose.resources {
    packageOfResClass = "bridge.supplies.foundation.ui.resources"
}

kotlin {
    explicitApiWarning() // becomes explicitApi() (errors) once the public API settles
    
    android {
        namespace = "bridge.supplies.foundation.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        
        // Required for Compose Multiplatform resources on Android
        androidResources { enable = true }
        
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm("desktop")
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        commonMain.dependencies {
            api(projects.foundationCore)
            
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            implementation(libs.compose.material.icons.extended)
            api(libs.navigation3.ui)
            
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.viewmodel.compose)
            api(libs.koin.core)
            api(libs.koin.compose)
            
            implementation(libs.g0dkar.qrcode.kotlin)
        }
        
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}
