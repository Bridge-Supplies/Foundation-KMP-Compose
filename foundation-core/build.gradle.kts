import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
}

group = "bridge.supplies.foundation"
version = libs.versions.app.versionName.get()

kotlin {
    explicitApiWarning() // becomes explicitApi() (errors) once the public API settles
    
    android {
        namespace = "bridge.supplies.foundation.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm("desktop")
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.serialization.json)
            api(libs.androidx.datastore)
            api(libs.androidx.datastore.preferences)
            
            implementation(libs.soywiz.korlibs.crypto)
            implementation(libs.soywiz.korlibs.compression)
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
