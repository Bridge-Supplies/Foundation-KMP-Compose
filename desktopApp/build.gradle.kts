import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.composeApp)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.components.resources)
}

compose.desktop {
    application {
        // The entry point lives in package app-packageName, in a class named app-name
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
                iconFile.set(rootProject.file("assets/foundation-icon-256.icns"))
            }

            windows {
                packageName = libs.versions.app.name.get()
                packageVersion = libs.versions.app.versionName.get()
                iconFile.set(rootProject.file("assets/foundation-icon-256.ico"))
            }

            linux {
                packageName = libs.versions.app.name.get()
                packageVersion = libs.versions.app.versionName.get()
                iconFile.set(rootProject.file("assets/foundation-icon-256.png"))
            }
        }
    }
}
