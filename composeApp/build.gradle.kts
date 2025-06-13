import com.github.jk1.license.render.JsonReportRenderer
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.gmazzo.buildconfig)
    alias(libs.plugins.jk1.license.report)
}

licenseReport {
    renderers = arrayOf(
        JsonReportRenderer("licenses.json")
    )
    outputDir = "${project.layout.projectDirectory}/reports/licenses"
}

tasks.register("prepareLicenseReport") {
    dependsOn("generateLicenseReport")
    doLast {
        copy {
            from("${project.layout.projectDirectory}/reports/licenses/licenses.json")
            into("src/commonMain/composeResources/files/json")
        }
        delete("${project.layout.projectDirectory}/reports")
    }
}

tasks.named("preBuild") {
    dependsOn("prepareLicenseReport")
}

fun getKeystoreProperties(): Properties {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    return keystoreProperties
}

buildConfig {
    val keystoreProperties = getKeystoreProperties()
    
    packageName(libs.versions.app.packageName.get())
    buildConfigField("APP_VERSION", libs.versions.app.versionName.get())
    buildConfigField("APP_BUILD", libs.versions.app.versionCode.get())
    buildConfigField("ENCRYPTION_KEY", keystoreProperties["encryptionKey"] as String)
}

kotlin {
    androidTarget()
    jvm("desktop")
    tasks.register("testClasses")
    
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        
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
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.navigation.compose)
            
            api(libs.koin.core)
            implementation(libs.koin.compose)
            
            implementation(libs.soywiz.korlibs.krypto)
            implementation(libs.soywiz.korlibs.compression)
            implementation(libs.material.kolor)
            implementation(libs.g0dkar.qrcode.kotlin)
            
            // TESTING
            implementation(libs.junit)
        }
        
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ismai117.kscan)
            api(libs.moko.permissions)
            api(libs.moko.permissions.camera)
            api(libs.moko.permissions.compose)
        }
        
        iosMain.dependencies {
            implementation(libs.ismai117.kscan)
            api(libs.moko.permissions)
            api(libs.moko.permissions.camera)
            api(libs.moko.permissions.compose)
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
    
    listOf(
//        iosArm64(), // iOS Native
//        iosX64(), // MacOS Simulator (Intel)
        iosSimulatorArm64() // MacOS Simulator (Apple Silicon)
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", libs.versions.app.packageName.get())
            binaryOption("bundleVersion", libs.versions.app.versionCode.get())
            binaryOption("bundleShortVersionString", libs.versions.app.versionName.get())
            isStatic = true
        }
    }
    
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    
    signingConfigs {
        create("release") {
            val keystoreProperties = getKeystoreProperties()
            
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
        versionName = libs.versions.app.versionName.get()
        versionCode = libs.versions.app.versionCode.get().toInt()
    }
    
    buildTypes {
        getByName("release") {
//            signingConfig = signingConfigs.getByName("release")
            
            resValue("string", "app_name", libs.versions.app.name.get())
            
            isMinifyEnabled = true
            isDebuggable = false
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

compose.desktop {
    application {
        mainClass = libs.versions.app.packageName.get() + "." + libs.versions.app.name.get()
        
        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg, // TargetFormat.Pkg, // MacOS
//                TargetFormat.Exe, TargetFormat.Msi, // Windows
//                TargetFormat.Deb, TargetFormat.Rpm // Linux
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
                packageVersion = libs.versions.app.versionName.get()
                dockName = libs.versions.app.name.get()
                iconFile.set(project.file("assets/foundation-icon-256.icns"))
            }
            
            windows {
                packageName = libs.versions.app.name.get()
                packageVersion = libs.versions.app.versionName.get()
                iconFile.set(project.file("assets/foundation-icon-256.ico"))
            }
            
            linux {
                packageName = libs.versions.app.name.get()
                packageVersion = libs.versions.app.versionName.get()
                iconFile.set(project.file("assets/foundation-icon-256.png"))
            }
        }
    }
}
