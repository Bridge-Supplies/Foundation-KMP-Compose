import com.github.jk1.license.render.JsonReportRenderer
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.gmazzo.buildconfig)
    alias(libs.plugins.jk1.license.report)
}

licenseReport {
    // KMP has no plain runtimeClasspath; scan the Android and desktop runtime dependencies
    configurations = arrayOf("androidRuntimeClasspath", "desktopRuntimeClasspath")
    // Foundation's own modules aren't third-party licenses (and resolving their Android variants is ambiguous)
    excludeGroups = arrayOf("bridge.supplies.foundation")
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

// The license JSON is a Compose resource, so generate it before Compose resources are prepared.
tasks.matching { it.name.startsWith("prepareComposeResourcesTaskFor") }.configureEach {
    dependsOn("prepareLicenseReport")
}

// keystore.properties is optional: without it the build uses a dev-only encryption key
// (see README > Publishing). Release signing lives in androidApp.
fun getKeystoreProperties(): Properties? {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (!keystorePropertiesFile.exists()) return null
    return Properties().apply { FileInputStream(keystorePropertiesFile).use { load(it) } }
}

// Dev-only fallback. Must be exactly 16 bytes (used as the AES key and IV). Never ship it.
val devEncryptionKey = "BridgeDevKey0016"

buildConfig {
    val keystoreProperties = getKeystoreProperties()
    if (keystoreProperties == null) {
        logger.warn("keystore.properties not found: using the dev-only encryption key; release signing is disabled.")
    }

    packageName(libs.versions.app.packageName.get())
    buildConfigField("APP_NAME", libs.versions.app.name.get())
    buildConfigField("PACKAGE_NAME", libs.versions.app.packageName.get())
    buildConfigField("APP_VERSION", libs.versions.app.versionName.get())
    buildConfigField("APP_BUILD", libs.versions.app.versionCode.get())
    buildConfigField("ENCRYPTION_KEY", keystoreProperties?.getProperty("encryptionKey") ?: devEncryptionKey)
}

compose.resources {
    // androidApp and desktopApp read shared strings/drawables (e.g. app_name) from this module
    publicResClass = true
}

kotlin {
    android {
        namespace = libs.versions.app.packageName.get() + ".composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        // Required for Compose Multiplatform resources on Android
        androidResources { enable = true }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm("desktop")
    tasks.register("testClasses") // Android Studio workaround: lets the IDE run KMP tests

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        commonMain.dependencies {
            api(projects.foundationUi)
            
            // api: these types appear in this module's public API, used by androidApp/desktopApp
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            implementation(libs.compose.components.ui.tooling.preview)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            api(libs.koin.core)
            api(libs.koin.compose)

            implementation(libs.soywiz.korlibs.crypto)
            implementation(libs.soywiz.korlibs.compression)
            implementation(libs.material.kolor)
            implementation(libs.g0dkar.qrcode.kotlin)
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

        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }

    listOf(
        iosArm64(), // iOS devices
//        iosX64(), // MacOS Simulator (Intel)
        iosSimulatorArm64() // iOS Simulator (Apple Silicon)
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
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
