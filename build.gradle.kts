plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}

// Formatting: `./gradlew spotlessApply` fixes it, `spotlessCheck` runs in scripts/verify.sh
spotless {
    val ktfmtVersion = libs.versions.ktfmt.get()
    // A file tree, not patterns: Spotless's patterns walk build/, whose iOS symlinks can be broken
    val ignored = listOf("**/build/**", "**/.gradle/**", "**/.kotlin/**", "foundation/**")
    kotlin {
        target(fileTree(rootDir) { include("**/*.kt"); exclude(ignored) })
        ktfmt(ktfmtVersion).kotlinlangStyle()
    }
    kotlinGradle {
        target(fileTree(rootDir) { include("**/*.gradle.kts"); exclude(ignored) })
        ktfmt(ktfmtVersion).kotlinlangStyle()
    }
}
