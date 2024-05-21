import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    task("testClasses")
    
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    listOf(
//        iosX64(),
//        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", libs.versions.app.packageName.get())
            isStatic = true
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.navigation.compose)
            
            api(libs.koin.core)
            implementation(libs.koin.compose)
            
            implementation(libs.g0dkar.qrcode.kotlin)
            
            // TESTING
            implementation(libs.junit)
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.chaintech.qrkit)
        }
        
        iosMain.dependencies {
            implementation(libs.chaintech.qrkit)
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
        }
    }
}

android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))
            
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    
    namespace = libs.versions.app.packageName.get()
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig {
        namespace = libs.versions.app.packageName.get()
        applicationId = libs.versions.app.packageName.get()
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
    }
    
    buildTypes {
        getByName("release") {
//            signingConfig = signingConfigs.getByName("release")
            
            resValue("string", "app_name", libs.versions.app.name.get())
            
            isMinifyEnabled = false
            isDebuggable = false
        }
        
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            resValue("string", "app_name", libs.versions.app.name.get() + " (Debug)")
            
            isMinifyEnabled = false
            isDebuggable = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = libs.versions.app.packageName.get() + libs.versions.app.mainName.get()
    }
}

compose.desktop {
    application {
        mainClass = libs.versions.app.packageName.get() + libs.versions.app.mainName.get()
        
        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
//                TargetFormat.Msi,
//                TargetFormat.Deb
            )
            packageName = libs.versions.app.packageName.get()
            packageVersion = libs.versions.app.versionName.get()
            version = libs.versions.app.versionName.get()
            description = libs.versions.app.description.get()
            copyright = libs.versions.app.copyright.get()
            vendor = libs.versions.app.vendor.get()
            
            macOS {
                bundleID = libs.versions.app.packageName.get()
                packageName = libs.versions.app.name.get()
                dockName = libs.versions.app.name.get()
                
                iconFile.set(project.file("assets/foundation-icon-256.icns"))
            }
            
            windows {
                iconFile.set(project.file("assets/foundation-icon-256.ico"))
            }
            
            linux {
                iconFile.set(project.file("assets/foundation-icon-256.png"))
            }
        }
    }
}
