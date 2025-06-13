# Foundation (KMP-Compose Template)
## Presented by [Bridge Supplies](https://bridge.supplies)
### Latest version: 1.2.1 ([Releases](https://github.com/Bridge-Supplies/Foundation-KMP-Compose/releases))

This is an opinionated [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) [Compose](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html) project, targeting Android, iOS, and Desktop JVM (MacOS, Windows, Linux) platforms. It's an intentionally designed template to develop good-looking, decentralized, lightweight apps for commonly used platforms.

## Features
- Material You theming
- Nested, multiple-backstack navigation
  - Adaptive BottomBar and NavigationRail
  - TopAppBar back button handling
- Easily share information between apps offline via QR codes
  - Android/iOS can scan and import codes
  - All platforms can generate codes
  - Encryption and compression support
- Utilities for orientation, build configurations, and customizable theming
- Platform-specific screen transition animations
- Automatic compilation of licensing information

## Screenshots
### Android

<img width="150" alt="android_home" src="/demo/Android/v1_2_0_android_1_home.png"> <img width="150" alt="android_qr" src="/demo/Android/v1_2_0_android_2_columns.png"> <img width="150" alt="android_settings" src="/demo/Android/v1_2_0_android_3_share.png"> <img width="150" alt="android_settings" src="/demo/Android/v1_2_0_android_4_settings.png">

### iOS

<img width="150" alt="android_home" src="/demo/iOS/v1_2_0_ios_1_home.png"> <img width="150" alt="android_qr" src="/demo/iOS/v1_2_0_ios_2_columns.png"> <img width="150" alt="android_settings" src="/demo/iOS/v1_2_0_ios_3_share.png"> <img width="150" alt="android_settings" src="/demo/iOS/v1_2_0_ios_4_settings.png">

### Desktop JVM

<img width="300" alt="macos_home" src="/demo/Desktop/v1_2_0_desktop_1_home.png"> <img width="300" alt="macos_qr" src="/demo/Desktop/v1_2_0_desktop_2_columns.png">
<br>
<img width="300" alt="macos_settings" src="/demo/Desktop/v1_2_0_desktop_3_share.png"> <img width="300" alt="macos_settings" src="/demo/Desktop/v1_2_0_desktop_4_settings.png">

## Multiplatform libraries

> [!TIP]
> We've pre-configured KMP-supporting libraries that work across all platforms, with few limitations. It is recommended you familiarize yourself with these libraries before developing with Foundation.

- UI
  - [Compose](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html)
  - [Material3](https://m3.material.io/)
- KotlinX
  - [Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
  - [Serialization](https://github.com/Kotlin/kotlinx.serialization)
  - [DateTime](https://github.com/Kotlin/kotlinx-datetime)
- AndroidX
  - [ViewModels](https://developer.android.com/jetpack/androidx/releases/lifecycle)
  - [DataStore](https://developer.android.com/kotlin/multiplatform/datastore)
  - [Compose Navigation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html)
- Dependency injection
  - [Koin](https://insert-koin.io/docs/reference/koin-mp/kmp/)
- Permission handling
  - [icerockdev's Moko Permissions](https://github.com/icerockdev/moko-permissions)
- QR code handling
  - [g0dkar's QRCode](https://github.com/g0dkar/qrcode-kotlin) (Android, iOS, Desktop - QR generation)
  - [ismai117's KScan](https://github.com/ismai117/KScan) (Android, iOS - QR scanning)
- Build
  - [gmazzo's BuildConfig](https://github.com/gmazzo/gradle-buildconfig-plugin) (shared BuildConfig)
- Cryptography & compression
  - [Korlib's Krypto](https://github.com/korlibs/korlibs/tree/main/korlibs-crypto) (Encryption, decryption)
  - [Korlib's Compression](https://github.com/korlibs/korlibs/tree/main/korlibs-compression) (Zipping, unzipping)
- Theming
  - [jordond's MaterialKolor](https://github.com/jordond/materialkolor) (Material theme generation)
- Licensing
  - [jk1's Gradle License Report](https://github.com/jk1/Gradle-License-Report) (Automated package license reporting)


## Project structure

* `/composeApp/src` contains all shared Compose Multiplatform applications code:
  - `build.gradle.kts` declares per-platform package dependencies and signing configurations
  - `/commonMain` is shared with all platforms.
    - `/composeResources` contains `.xml` resources like `strings.xml`
    - `App.kt` contains shared Compose UI with `Koin` and `Material`
    - `AppConfig.kt` declares supported platform features and `expect`-ed platform functions
    - `Color.kt` and `Theme.kt` contains theme information derived from the [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)
    - `DataStore.kt` and `DataRepository.kt` contain the `DataStore Preferences` `expect` implementation and wrapper
    - `FoundationTheme.kt` `expect`s a Theme definition for each platform
    - `MainViewModel.kt` contains the `ViewModel` and `DataStore` implementations injected with `Koin`
    - `Navigation.kt` and `/ui` contain Composable definitions for each screen
  - `/androidMain` is for Android-specific code.
    - `Foundation.android.kt` is the `Application` definition to initialize libraries (Koin)
    - `MainActivity.kt` displays the shared `App` Composable
    - `actual` implementations of `FoundationTheme`, `AppConfig`, `DataStore`, and UI
  - `/iosMain` is for iOS-specific code.
    - `MainViewController.kt` initializes libraries (Koin) and displays shared `App` Composable
    - `actual` implementations of `FoundationTheme`, `AppConfig`, `DataStore`, and UI
  - `/desktopMain` is for JVM-specific code.
    - `Foundation.desktop.kt` contains the `main()` class to initialize libraries (Koin) and display shared `App` Composable
     - `actual` implementations of `FoundationTheme`, `AppConfig`, `DataStore`, and UI
* `/iosApp` contains the iOS Xcode project files.
  - `Config.xcconfig` declares iOS package and app name
  - `ContentView.swift` displays the `MainViewController`'s implementation of the shared `App` Composable
  - `iOSApp.swift` displays the `ContentView` as the `App` controller
  - `Info.plist` contains build definitions, permissions
  - `project.pbxproj` contains build version information
* `/gradle` contains shared Gradle package versions
  - `libs.versions.toml` defines build info (package name, versionCode, versionName, supported Android SDKs) and package libraries
    - App build/version here applies to Android and Desktop, iOS must modify `project.pbxproj` manually

## Project Configuration Setup

> [!CAUTION]
> Apparently naming your app "Foundation" doesn't work on iOS due to a conflict with some Accessibility package, so it's called "Foundation_" there instead.

### Before building
* Rename package name and directories
  - `bridge.supplies.foundation` to your package name
  - ie `/composeApp/src/androidMain/kotlin/bridge/supplies/foundation`
* Create `keystore.properties` file in root directory
  - example in [Publishing](#publishing)
* Find + Replace All some values
  - `foundation.composeapp.generated.resources` = `[app-name].composeapp.generated.resources`
  - `bridge.supplies.foundation.BuildConfig` = `your.package.name.BuildConfig`
* Update `settings.gradle.kts`
  - `rootProject.name` = your app name
* Update `libs.versions.toml`
  - `app-packageName` = the full package name, for example `bridge.supplies.foundation`
  - `app-name` = the visible app name, name of class containing your Desktop `main()` function, and shows as MacOS menu item
  - `app-description` = a brief description of your app
  - `app-copyright` = your copyright information, if applicable
  - `app-vendor` = your organization name
  - `app-versionName` = your version, for example "1.0.0" (must be > 1.0.0 if deploying to Desktop)
  - `app-versionCode` = your build, for example "202501010" format representing YYYYMMDD0
  - `app-minSdk`, `app-targetSdk`, and `app-compileSdk` for your Android API targets
* Update `strings.xml`
  - `app_name` to your app name
* Update Android and Desktop Application classes
  - `Foundation.android.kt` to `YourAppName.android.kt`
  - `Foundation.desktop.kt` to `YourAppName.desktop.kt`
* Update `AndroidManifest.xml`
  - <application `android:name`/> = the full package name + Application class name
    - ie `bridge.supplies.foundation.Foundation`
* Update iOS build files
  - Update `Config.xcconfig`
    - BUNDLE_ID = full package name + app name, for example `bridge.supplies.foundation.Foundation`
    - APP_NAME = visible app name, for example "Foundation"
  - Update `project.pbxproj`
    - replace `Foundation_` = your app name
    - `MARKETING_VERSION` = the same value as `app-versionName` in `libs.versions.toml`
    - `CURRENT_PROJECT_VERSION` = the same value as `app-versionCode` in `libs.versions.toml`
    - `PRODUCT_BUNDLE_IDENTIFIER`, `PRODUCT_NAME`, and `INFOPLIST_KEY_CFBundleDisplayName` = your app name
  - Android Studio may require reboot to successfully build iOS

### After a successful Android build
* Update run configurations
  - Delete extra `composeApp` (Android) and `iosApp` configs
  - Update Desktop config to use correct Main Application class name, ie: `app-name` value
* Update `README.md`, if applicable
* Update `AppConfig`
  - Update `shareUrl` if applicable
* Update `SettingsAboutScreen` Easter Egg
  - `clickable` modifier on Build information
* Update `Navigation.kt`
  - Create new screens by adding a new entry to the `Screens` enum class
  - If screens require navigation arguments, add a new entry to `NavArgument` enum class
  - Add screens to specific `NavigationTab` enum class entries
    - Screens can only exist inside one `NavigationTab`

### Icons
- Shared
  - `composeApp/src/commonMain/composeResources/drawable` (if you want to draw it in-app)
    - `ic_launcher_foreground.xml`
    - `ic_launcher_background.xml`
- Android
  - `composeApp/src/androidMain/kotlin/res/drawable` (needed for icon generation)
    - `ic_launcher_foreground.xml`
    - `ic_launcher_background.xml`
    - `ic_launcher_monochrome.xml` ([if needed](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive))
  - `composeApp/src/androidMain/kotlin/res/mipmap`
    - `ic_launcher.xml`
- iOS
  - `iosApp/Assets.xcassets/AppIcon.appiconset`
    - 1024x1024 `.png`
    - Also update corresponding `Contents.json`
- Desktop
  - Mac
    - `assets/`
    - `.icns` file
  - Windows
    - `assets/`
    - `.ico` file
  - Linux
    - `assets/`
    - `.png` file

### Run configurations
- `Android`
  - using `Foundation.composeApp.main` module
- `iOS`
  - using `iosApp.xcodeproj` project file
- `Desktop`
  - using `desktopRun -DmainClass=Foundation --quiet` run options
  - `DmainClass` should match `app-mainName` without the `.`

### Publishing
- Android
  - Create a `keystore.properties` file in the repository root directory for production signing. Do not check this file into version control, or your `upload-keystore.jks`
```
storeFile=/Users/Example/Documents/Keystores/upload-keystore.jks
storePassword=myStorePassword
keyAlias=upload
keyPassword=myKeyPassword
encryptionKey=dataTransferEncryptionKey
```
- iOS
  - _TBD_
- Desktop
  - _TBD_
