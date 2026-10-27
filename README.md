# Foundation (KMP-Compose Library & Template)
## Presented by [Bridge Supplies](https://bridge.supplies)
### Latest version: 2.1.0 ([Releases](https://github.com/Bridge-Supplies/Foundation-KMP-Compose/releases))

Foundation is an opinionated [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) and [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html) library and starter template targeting Android, iOS, and Desktop JVM (macOS, Windows, Linux). It provides a shared app shell, theming, persistence, offline QR sharing, and common UI components so apps can focus on their own features without duplicating infrastructure.

## Features
- Material You & Material 3 Expressive theming (dynamic system colors on Android 12+, preset seed colors, palette styles, Dark/AMOLED modes)
- Type-safe multiple-backstack navigation built on Navigation 3 (`NavDisplay`)
  - Adaptive `BottomAppBar` (portrait) and `NavigationRail` (landscape)
  - Automatic `TopAppBar` title and back button handling, per-screen top bar actions, and immersive chrome-free screens
- Offline data sharing via QR codes (`QrCodec`, `ShareQrCode`, `ScanQrScreen`)
  - Android and iOS camera scanning with permission handling
  - QR generation on all platforms with debounced rendering
  - Versioned payload format with GZIP compression and AES-CBC encryption (random per-payload IV, with backward-compatible v1 decoding)
- Single-instance `DataStore<Preferences>` settings storage (`FoundationSettings` and typed `SettingKey`s) shared between Foundation and the consuming app
- Modal bottom sheet host (`SheetController`) with built-in date picker (`DateSheet`) and app-sharing (`ShareAppSheet`) sheets
- Built-in Settings, About, and open-source license screens with declarative extension points
- Platform services for URLs (`openUrl` with in-app browser support), system app settings (`openAppSettings`), haptics (`rememberHapticFeedback`), and window orientation (`isPortrait`)

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
> Foundation pre-configures KMP-ready libraries across Android, iOS, and Desktop and exports its version catalog so consuming apps can share the exact same versions.

- UI
  - [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html)
  - [Material 3](https://m3.material.io/)
- KotlinX
  - [Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
  - [Serialization](https://github.com/Kotlin/kotlinx.serialization)
  - [DateTime](https://github.com/Kotlin/kotlinx-datetime)
- AndroidX & JetBrains
  - [Lifecycle & ViewModels](https://developer.android.com/jetpack/androidx/releases/lifecycle)
  - [DataStore Preferences](https://developer.android.com/kotlin/multiplatform/datastore)
  - [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) (`org.jetbrains.androidx.navigation3:navigation3-ui`)
  - [AndroidX Browser](https://developer.android.com/jetpack/androidx/releases/browser) (Custom Tabs on Android)
- Dependency injection
  - [Koin](https://insert-koin.io/docs/reference/koin-mp/kmp/)
- Permission handling
  - [icerockdev's Moko Permissions](https://github.com/icerockdev/moko-permissions)
- QR code handling
  - [g0dkar's QRCode](https://github.com/g0dkar/qrcode-kotlin) (Android, iOS, Desktop - QR generation)
  - [ismai117's KScan](https://github.com/ismai117/KScan) (Android, iOS - QR scanning)
- Build
  - [gmazzo's BuildConfig](https://github.com/gmazzo/gradle-buildconfig-plugin) (shared BuildConfig in the demo app)
- Cryptography & compression
  - [Korlibs Crypto](https://github.com/korlibs/korlibs-crypto) (AES-CBC encryption/decryption and secure random IVs)
  - [Korlibs Compression](https://github.com/korlibs/korlibs-compression) (GZIP compression/decompression)
- Theming
  - [jordond's MaterialKolor](https://github.com/jordond/materialkolor) (dynamic Material color scheme generation)
- Licensing
  - [jk1's Gradle License Report](https://github.com/jk1/Gradle-License-Report) (automated dependency license reporting)

## Project structure

* `/foundation-core` (`bridge.supplies.foundation.core`) — Kotlin Multiplatform library without Compose:
  - `config/` — `FoundationConfig`, `StorageConfig`, `FeatureFlags`
  - `settings/` & `storage/` — `FoundationSettings`, `DataStoreSettings`, typed `SettingKey`s (`BooleanSetting`, `IntSetting`, `LongSetting`, `StringSetting`, `StringSetSetting`, `EnumSetting`, `JsonSetting`), `settingName()`, and the per-platform DataStore location (internal)
  - `crypto/` — `QrCodec`, `FoundationQrCodec` (v2 random-IV encoding, legacy v1 decoding; the low-level crypto is internal)
  - `platform/` — `FoundationPlatform`, `PlatformType`, `PlatformFeature`, `currentPlatform()`, `openUrl()`, `openAppSettings()`, `randomUuid()`, `FoundationAndroid`
  - `sheets/` — `SheetRequest`, `SheetController`, `DefaultSheetController`
  - `time/` & `text/` — date/time helpers (`getTodayUtcMs()`, `getDateDisplay()`, `getDateTime()`, etc.) and number formatting
  - `di/` — `foundationCoreModule(config)`
* `/foundation-ui` (`bridge.supplies.foundation.ui`) — Compose Multiplatform UI library (depends on `foundation-core` as `api`):
  - `FoundationViewModel`, `foundationUiModule()`, `foundationViewModel()`, `rememberHapticFeedback()`, `isPortrait()`
  - `shell/` — `FoundationApp`, `FoundationTab`, `RouteRegistry`, `Navigator`, `LocalNavigator`, `screenMetadata()`, `Chrome`, `TopBarAction`, `TopBarActions`, `Landing`, `ShellOptions`, platform transitions
  - `theme/` — `FoundationTheme`, `ThemeConfig`, `ColorTheme`, `Palette`, `DarkMode`
  - `settings/` — `FoundationSettingsScreen`, `BuiltInSettings`, `FoundationSettingKeys`, `SettingSwitch`, `SettingChoice`, `SettingsScope` DSL
  - `sheets/` — `SheetHost`, `SheetContent`, `SheetScope`, `sheetController()`, `DateSheet`, `ShareAppSheet`, `SheetColumn`, `CloseButton`
  - `about/` — `FoundationAboutScreen`, `AboutConfig`
  - `qr/` — `ShareQrCode`, `ScanQrScreen`, `QrCodeScanner`
  - `components/` & `util/` — `EdgeFade*` and `IndexedLazy*` lists, `TitledCard`, `ExpandableTitledCard`, `DatePickerCard`, `PastOrPresentSelectableDates`, `SettingsSwitch`, `SettingsSelector`, `QrCodeCard`, `koinViewModel()`, clipboard and bitmap utilities
* Demo application (built strictly on the public API of `foundation-core` and `foundation-ui`):
  - `/composeApp` (`bridge.supplies.foundation.demo`) — shared KMP library (`com.android.kotlin.multiplatform.library`) containing the demo screens, routes, view model, and resources
  - `/androidApp` (`bridge.supplies.foundation`) — Android application module (`com.android.application`) with `Foundation` (`Application`), `MainActivity`, manifest, launcher icons, and signing config
  - `/desktopApp` (`bridge.supplies.foundation`) — Desktop JVM entry point (`Foundation.main`) and `nativeDistributions`
  - `/iosApp` — Xcode project linking the `ComposeApp` framework
* `/consumer-test` — standalone Gradle build that includes Foundation via `includeBuild` and verifies library consumption, resource packaging, and `QrCodec` across Android, Desktop, and iOS
* `/scripts` — `verify.sh` (`quick` / `ui` / `e2e` tiers), `consumer-test.sh`, and `e2e/over-install.sh` (reusable upgrade test for apps; see `scripts/e2e/README.md`)
* `/.maestro` — Maestro flows for the demo app (`smoke.yaml`, `seed-settings.yaml`, `assert-settings.yaml`)
* `/gradle/libs.versions.toml` — version catalog exported as `foundationLibs` for consuming apps

## Use Foundation as a library

Apps consume Foundation as a **Git submodule** combined with a **Gradle composite build**. Gradle substitutes the Maven coordinates `bridge.supplies.foundation:foundation-ui` with the `:foundation-ui` subproject inside the submodule. Gradle configures the whole included build (including Foundation's demo modules), but your app only compiles and links `:foundation-core` and `:foundation-ui`. The included demo logs a harmless `keystore.properties not found` warning because Foundation's own root has no keystore.

### 1. Add the submodule

From your app repository's root directory:

```bash
git submodule add https://github.com/Bridge-Supplies/Foundation-KMP-Compose.git foundation
cd foundation && git checkout v2.0.0 && cd ..   # pin a release tag
git add .gitmodules foundation
```

Anyone cloning your app then needs the submodule too: `git clone --recurse-submodules <your-repo>` (or `git submodule update --init` in an existing clone).

### 2. Import `foundationLibs` and include the build in `settings.gradle.kts`

In your app's `settings.gradle.kts`, import `foundation/gradle/libs.versions.toml` as a version catalog named `foundationLibs` and register the submodule with `includeBuild("foundation")`. Importing `foundationLibs` ensures your app uses the exact same Kotlin, AGP, and Compose plugin versions as the included Foundation build (mismatched plugin versions across a composite build cause Gradle classloader conflicts):

```kotlin
rootProject.name = "MyApp"

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
    versionCatalogs {
        // Foundation's catalog, so plugin and library versions match the included build exactly
        create("foundationLibs") {
            from(files("foundation/gradle/libs.versions.toml"))
        }
    }
}

includeBuild("foundation")

include(":shared")
```

In your root `build.gradle.kts`, alias the plugins from `foundationLibs`:

```kotlin
plugins {
    alias(foundationLibs.plugins.androidApplication) apply false // androidApp
    alias(foundationLibs.plugins.androidMultiplatformLibrary) apply false
    alias(foundationLibs.plugins.jetbrainsCompose) apply false
    alias(foundationLibs.plugins.compose.compiler) apply false
    alias(foundationLibs.plugins.kotlinMultiplatform) apply false
    alias(foundationLibs.plugins.kotlinxSerialization) apply false
    alias(foundationLibs.plugins.kotlinJvm) apply false // desktopApp
}
```

Keep your own `gradle/libs.versions.toml` (the default `libs` catalog) for your app's identity (`app-*` versions) and app-only libraries. Take every shared library and plugin from `foundationLibs`.

### 3. Depend on `foundation-ui` in your shared module

In your shared KMP module's `build.gradle.kts` (adapted from `consumer-test/shared/build.gradle.kts`), add `bridge.supplies.foundation:foundation-ui` to `commonMain`. Because `:foundation-ui` exposes `:foundation-core` as an `api` dependency, you get both modules automatically.

```kotlin
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
        namespace = "com.example.myapp.shared"
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
            baseName = "Shared"
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
```

### 4. App entry modules (AGP 9 layout)

AGP 9 doesn't allow an Android application inside a KMP module, so apps follow Foundation's own layout:
- `:composeApp` (or your shared module): the KMP library above, with all shared code and the iOS framework.
- `:androidApp`: `com.android.application` with your `Application` class, `MainActivity`, manifest, launcher icons, and signing. It depends on `projects.composeApp`. Copy Foundation's `androidApp/build.gradle.kts` as a starting point (AGP 9 has built-in Kotlin, so there's no `kotlin-android` plugin).
- `:desktopApp`: `kotlin("jvm")` with your `main()` and `compose.desktop { application { … } }`. Copy Foundation's `desktopApp/build.gradle.kts`.
- `iosApp`: the Xcode build phase runs `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`. Only `iosArm64` and `iosSimulatorArm64` are built, so command-line simulator builds need `ARCHS=arm64`.

The shared module can't reference classes in `:androidApp`. Pass the Android `Context` in through `FoundationAndroid.init(context)` and Koin instead.

### 5. Toolchain, `local.properties`, and desktop tests

- Use Foundation's Gradle version and JDK: copy `gradle/wrapper/` and `gradle/gradle-daemon-jvm.properties` (JDK 21, provisioned by the Foojay plugin in `settings.gradle.kts`) from the submodule. Your app's Gradle runs the included build too.
- Ensure `local.properties` points `sdk.dir` to your Android SDK, **both in your app root and in `foundation/`** (the included build resolves the SDK from its own root; `local.properties` is git-ignored, so copy it after cloning). Setting `ANDROID_HOME` also works.
- If your shared module runs `desktopTest` and touches Compose UI/graphics types (such as `ColorTheme`), include `implementation(compose.desktop.currentOs)` in the `desktopTest` source set so the host OS Skia native library is on the test classpath.

---

## Building an app on Foundation

### 1. `FoundationConfig` and Koin initialization

Create a `FoundationConfig` with your app's identity and register `foundationCoreModule(config)` and `foundationUiModule()` in Koin:

```kotlin
val foundationConfig = FoundationConfig(
    appName = "MyApp",
    packageName = "com.example.myapp",
    version = "1.0.0",
    build = "1",
    encryptionKey = "My16ByteSecret!!", // must be exactly 16 bytes
    shareUrl = "https://example.com/myapp",
    storage = StorageConfig(
        // Defaults to "settings.preferences_pb"; never change this for an existing app
        // Optional iosDirectory / desktopDirectory overrides
    ),
    features = FeatureFlags(
        qrSharing = true,
        landingTips = true,
    ),
)

val foundationModule: Module = module {
    includes(foundationCoreModule(foundationConfig), foundationUiModule())
}
```

On Android, call `FoundationAndroid.init(applicationContext)` before `startKoin`:

```kotlin
FoundationAndroid.init(context)
startKoin {
    androidContext(context)
    modules(appViewModelModule, foundationModule)
}
```

### 2. Navigation shell (`FoundationApp`, `RouteRegistry`, `FoundationTab`, `screenMetadata`)

Define `@Serializable` routes implementing `NavKey`, register them in a `RouteRegistry` (required for multiplatform saved-state serialization), and list your top-level `FoundationTab`s:

```kotlin
@Serializable data object HomeInfo : NavKey
@Serializable data class HomeDate(val selectedDateMs: Long) : NavKey
@Serializable data object SettingsOptions : NavKey
@Serializable data object SettingsAbout : NavKey

val appRoutes = RouteRegistry {
    subclass(HomeInfo::class, HomeInfo.serializer())
    subclass(HomeDate::class, HomeDate.serializer())
    subclass(SettingsOptions::class, SettingsOptions.serializer())
    subclass(SettingsAbout::class, SettingsAbout.serializer())
}

val appTabs = listOf(
    FoundationTab(
        id = "home",
        title = Res.string.tab_home_title,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        root = HomeInfo
    ),
    FoundationTab(
        id = "settings",
        title = Res.string.tab_settings_title,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        root = SettingsOptions
    )
)
```

Render `FoundationApp` at the root of your Compose hierarchy. Each entry can customize its top-bar title or request an immersive chrome-free layout (`Chrome.Immersive`) via `screenMetadata`:

```kotlin
@Composable
fun App(
    onShowSystemUi: (Boolean) -> Unit = {},
    onCloseApplication: () -> Unit = {},
) {
    FoundationApp(
        tabs = appTabs,
        routes = appRoutes,
        landing = Landing.Default(icon = Res.drawable.ic_launcher_foreground),
        theme = ThemeConfig(),
        shell = ShellOptions(),
        onShowSystemUi = onShowSystemUi,
        onCloseApplication = onCloseApplication,
        sheets = { request ->
            // Render app-specific SheetRequest types here
        }
    ) {
        entry<HomeInfo> {
            val navigator = LocalNavigator.current
            HomeInfoScreen(
                onNavigateDateScreen = { dateMs -> navigator.navigate(HomeDate(dateMs)) }
            )
        }

        entry<HomeDate>(metadata = screenMetadata(title = Res.string.screen_home_date_title)) { route ->
            HomeDateScreen(initialSelectedDateMs = route.selectedDateMs)
        }

        // Example of an immersive screen without top bar, bottom bar, or rail:
        // entry<BattleRoute>(metadata = screenMetadata(chrome = Chrome.Immersive(hideSystemBars = true))) { ... }
    }
}
```

### 3. `LocalNavigator` and `TopBarActions`

Inside any screen entry, read `LocalNavigator.current` to navigate within the active tab, jump across tabs, pop, or handle external deep links/shortcuts:

```kotlin
val navigator = LocalNavigator.current
navigator.navigate(HomeDate(selectedDateMs))      // push onto current tab
navigator.navigate("settings", SettingsAbout)     // switch to another tab and push
navigator.selectTab("home")                       // switch tab (or pop to root if already selected)
navigator.popToRoot()                             // pop current tab to its root route
navigator.back()                                  // pop current tab, or return to first tab
navigator.openFromOutside("home", HomeDate(ms))   // deep link / shortcut entry point
```

Screens can attach dynamic icon buttons to the top app bar while they are visible using `TopBarActions`:

```kotlin
val sheets = sheetController()
val hapticFeedback = rememberHapticFeedback()

TopBarActions(
    listOf(
        TopBarAction(Icons.Default.Share, Res.string.action_share) {
            hapticFeedback()
            sheets.show(ShareAppSheet)
        }
    )
)
```

### 4. Settings storage and `FoundationSettingsScreen`

Foundation owns the single `DataStore<Preferences>` instance (`settings.preferences_pb`). Define your app's keys with `settingName("<suffix>")` (which prefixes `"Settings_"` for backward compatibility):

```kotlin
val showHintsKey = BooleanSetting(settingName("show_hints"), default = true)
val sortOrderKey = EnumSetting(settingName("sort_order"), SortOrder.DEFAULT, SortOrder.entries)
val userProfileKey = JsonSetting(settingName("self_user"), UserProfile(), UserProfile.serializer())
```

> [!IMPORTANT]
> `EnumSetting` stores the enum's ordinal (`Int`). Never reorder or remove enum entries in an existing app—only append new entries to the end.

Read and write settings in Compose through `FoundationViewModel` (or inject `FoundationSettings` directly into your own ViewModels):

```kotlin
val foundation = foundationViewModel()
val showHints by foundation.setting(showHintsKey).collectAsState()
foundation.set(showHintsKey, false)
```

Build your settings screen with `FoundationSettingsScreen`. It renders Foundation's built-in Theming and System cards (configurable via `BuiltInSettings`) followed by your app's custom sections (see the note on motion below):

```kotlin
entry<SettingsOptions> {
    val navigator = LocalNavigator.current
    FoundationSettingsScreen(
        builtIns = BuiltInSettings(),
        onNavigateToAbout = { navigator.navigate(SettingsAbout) }
    ) {
        section(Res.string.app_custom_settings_title) {
            switch(
                key = showHintsKey,
                title = Res.string.setting_show_hints_title,
                subtitle = Res.string.setting_show_hints_subtitle
            )
            choice(
                key = sortOrderKey,
                title = Res.string.setting_sort_order_title,
                label = { it.titleRes }
            )
            link(
                title = Res.string.setting_advanced_title,
                route = AdvancedSettingsRoute
            )
            custom {
                // Any arbitrary @Composable content inside the section card
            }
        }
    }
}
```

**Motion (Material 3 Expressive):** apps use expressive motion by default and Settings shows no switch for it. To fix it off, pass `ThemeConfig(expressiveAnimation = ExpressiveAnimation.OFF)`; to let users choose, pass `ExpressiveAnimation.USER_CHOICE`, which adds the "Use Expressive animation" switch to the built-in System card.

### 5. Bottom sheets (`SheetController`, `DateSheet`, `ShareAppSheet`, custom `SheetContent`)

Trigger built-in or custom modal bottom sheets from anywhere in Compose using `sheetController()` (or by injecting `SheetController` in a ViewModel):

```kotlin
val sheets = sheetController()

// Built-in date picker sheet (pass PastOrPresentSelectableDates to disallow future dates)
sheets.show(
    DateSheet(
        initialDateMs = getTodayUtcMs(),
        selectableDates = PastOrPresentSelectableDates,
        onSelected = { selectedDateMs ->
            navigator.navigate(HomeDate(selectedDateMs))
        }
    )
)

// Built-in share-app QR + copy link sheet
sheets.show(ShareAppSheet)
```

To add app-specific sheets, implement `SheetRequest` and pass a `sheets` lambda (`SheetContent`) to `FoundationApp`:

```kotlin
data class ConfirmDeleteSheet(val itemName: String, val onConfirm: () -> Unit) : SheetRequest

FoundationApp(
    tabs = appTabs,
    routes = appRoutes,
    sheets = { request ->
        when (request) {
            is ConfirmDeleteSheet -> SheetColumn {
                BodyText(text = "Delete ${request.itemName}?")
                TextButton(text = "Delete") {
                    request.onConfirm()
                    dismiss()
                    hapticFeedback()
                }
                CloseButton()
            }
        }
    }
) { ... }
```

### 6. Landing, About, and licenses

- **Landing screen:**
  - `Landing.Default(icon = Res.drawable.ic_launcher_foreground, extraTips = listOf(Res.string.my_tip))` shows the circular reveal with the app icon, name, and a random tip (combining Foundation's platform-aware tips with `extraTips`).
  - `Landing.Custom { onDone -> MyOnboardingScreen(onFinish = onDone) }` renders a custom onboarding flow.
  - `Landing.None` skips the landing screen.
- **About & licenses screen:**
  Each app generates its own `licenses.json` in its shared module using the `com.github.jk1.dependency-license-report` plugin (see `composeApp/build.gradle.kts` for the `licenseReport` + `prepareLicenseReport` task setup) and passes the bytes to `FoundationAboutScreen`:

```kotlin
entry<SettingsAbout>(metadata = screenMetadata(title = Res.string.screen_about_title)) {
    FoundationAboutScreen(
        AboutConfig(
            licensesJson = { Res.readBytes("files/json/licenses.json") },
            onVersionClick = { openUrl("https://bridge.supplies") }
        )
    )
}
```

### 7. QR code generation and scanning (`QrCodec`, `ShareQrCode`, `ScanQrScreen`)

Any `@Serializable` type can be encoded into or decoded from a QR code:

```kotlin
@Serializable
data class SharedData(@SerialName("m") val message: String)

// Display a debounced, theme-colored QR code for `sharedData` (or null to hide):
ShareQrCode(
    modifier = Modifier.weight(1f),
    value = sharedData,
    serializer = SharedData.serializer()
)

// Full-screen camera scanner that decodes v2 and legacy v1 payloads:
ScanQrScreen(
    serializer = SharedData.serializer(),
    onScanned = { data ->
        viewModel.setSharedText(data.message)
        navigator.back()
    },
    onPermissionDenied = {
        hapticFeedback()
        navigator.back()
    }
)
```

For direct string encoding/decoding outside of UI components, inject `QrCodec` (`koinInject<QrCodec>()`):

```kotlin
val encoded: String = qrCodec.encode(payload, SharedData.serializer(), encrypt = true)
val decoded: Result<SharedData> = qrCodec.decode(encoded, SharedData.serializer())
```

Apps with their own compact binary format (for example a game turn packed into ~150 bytes) can skip JSON: `encodeBytes` uses the same encryption and writes Base45 text with only QR alphanumeric characters, which makes a noticeably smaller QR code (150 bytes: QR version 8 instead of 12). Show it with `CodeDisplay` and read it with `QrCodeScanner`, which both work with plain text:

```kotlin
val text: String = qrCodec.encodeBytes(bytes, encrypt = true)
val back: Result<ByteArray> = qrCodec.decodeBytes(text) // fails for other payloads or another key
```

### 8. Platform services and UI helpers

- `openUrl(url, inApp = false)` — opens a URL in the external browser, or inside the app when `inApp = true` (Custom Tabs on Android, `SFSafariViewController` on iOS; Desktop always uses the system browser).
- `openAppSettings()` — opens the operating system's settings page for the app.
- `rememberHapticFeedback()` — returns a `() -> Unit` lambda that triggers haptic feedback when supported by the device and enabled in user settings.
- `isPortrait()` — returns `true` when the window height is greater than or equal to its width.

---

## Upgrading Foundation in an app

Each release is a tag `vX.Y.Z` on `main`. `main` gets one squashed commit per version, and patch releases come from a `release/vX.Y.Z` branch.

1. Bump the submodule to the target release tag:
   ```bash
   cd foundation && git fetch --tags && git checkout v2.1.0 && cd ..
   git add foundation && git commit -m "Bump Foundation to v2.1.0"
   ```
2. Read [`CHANGELOG.md`](CHANGELOG.md) for any breaking changes or migration steps.
3. Run your app's verification script (`scripts/verify.sh quick`) and test on your target platforms.

---

## Use as a starter template

If you are forking or cloning this repository as a standalone starter template instead of consuming it as a submodule library:

> [!CAUTION]
> Naming an iOS target "Foundation" conflicts with Apple's `Foundation` framework, so the iOS target in this repository is named `Foundation_`.

### Before building
* Rename the demo package name and directories:
  - `bridge.supplies.foundation.demo` (in `/composeApp/src/commonMain/kotlin/...`, `androidMain`, `desktopMain`, `iosMain`) to your app package
  - `bridge.supplies.foundation` in `/androidApp/src/main/kotlin/bridge/supplies/foundation` and `/desktopApp/src/main/kotlin/bridge/supplies/foundation` to your app package
* Optionally create a `keystore.properties` file in the root directory (see [Publishing](#publishing)). Without `keystore.properties`, debug builds automatically fall back to a development encryption key and skip release signing.
* Find + Replace across the project:
  - `foundation.composeapp.generated.resources` = `[app-name].composeapp.generated.resources`
  - `bridge.supplies.foundation.BuildConfig` = `your.package.name.BuildConfig`
* Update `settings.gradle.kts`:
  - `rootProject.name` = your app name
* Update `gradle/libs.versions.toml`:
  - `app-packageName` = the full package name, for example `bridge.supplies.foundation`
  - `app-name` = the visible app name, name of the class containing your Desktop `main()` function, and macOS menu item name
  - `app-description` = a brief description of your app
  - `app-copyright` = your copyright information
  - `app-vendor` = your organization name
  - `app-versionName` = your version, for example `"1.0.0"` (must be `>= 1.0.0` for Desktop packaging)
  - `app-versionCode` = your build number, for example `"202610240"` (`YYYYMMDD0`)
  - `android-minSdk`, `android-targetSdk`, and `android-compileSdk` for your Android API targets
* Update `composeApp/src/commonMain/composeResources/values/strings.xml`:
  - `app_name` to your app name
* Update Android and Desktop Application classes:
  - `androidApp/src/main/kotlin/.../Foundation.kt` to `YourAppName.kt`
  - `desktopApp/src/main/kotlin/.../Foundation.kt` to `YourAppName.kt` (keep it in your package directory with a matching `package` declaration)
* Update `androidApp/src/main/AndroidManifest.xml`:
  - `<application android:name="..." />` = the full package name + Application class name (e.g. `bridge.supplies.foundation.Foundation`)
* Update iOS build files in `/iosApp`:
  - Update `Configuration/Config.xcconfig`:
    - `BUNDLE_ID` = full package name + app name (e.g. `bridge.supplies.foundation.Foundation`)
    - `APP_NAME` = visible app name (e.g. `Foundation`)
  - Update `iosApp.xcodeproj/project.pbxproj`:
    - replace `Foundation_` = your app name
    - `MARKETING_VERSION` = the same value as `app-versionName` in `libs.versions.toml`
    - `CURRENT_PROJECT_VERSION` = the same value as `app-versionCode` in `libs.versions.toml`
    - `PRODUCT_BUNDLE_IDENTIFIER`, `PRODUCT_NAME`, and `INFOPLIST_KEY_CFBundleDisplayName` = your app name

### After a successful build
* Update run configurations in `.run/` if needed:
  - Update the Desktop configuration if you changed `app-packageName` or `app-name`
* Update `foundationConfig` in `composeApp/.../config/AppConfig.kt`:
  - Set `shareUrl` to your store or repository URL
* Update `FoundationAboutScreen` in `composeApp/.../ui/Navigation.kt`:
  - Customize or remove the `onVersionClick` easter egg
* Define your routes and tabs in `composeApp/.../ui/Navigation.kt`

### Icons
- Shared
  - `composeApp/src/commonMain/composeResources/drawable` (used in-app, such as on the default landing screen)
    - `ic_launcher_foreground.xml`
    - `ic_launcher_background.xml`
- Android
  - `androidApp/src/main/res/drawable`
    - `ic_launcher_foreground.xml`
    - `ic_launcher_background.xml`
    - `ic_launcher_monochrome.xml` ([adaptive icon guide](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive))
  - `androidApp/src/main/res/mipmap`
    - `ic_launcher.xml`
- iOS
  - `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset`
    - 1024x1024 `.png` and corresponding `Contents.json`
- Desktop
  - macOS: `assets/*.icns`
  - Windows: `assets/*.ico`
  - Linux: `assets/*.png`

### Run configurations
- `Android`: runs the `Foundation.androidApp` module (`./gradlew :androidApp:assembleDebug`)
- `iOS`: runs via `iosApp/iosApp.xcodeproj`
- `Desktop`: runs `./gradlew :desktopApp:run` (main class is `app-packageName` + `.` + `app-name` from `gradle/libs.versions.toml`)

## Testing

- **Repository verification** with `scripts/verify.sh <tier>` (a shared lock keeps parallel runs from overlapping):
  - `quick`: compiles the Android debug app and the Desktop app, runs common/Desktop unit tests in all modules, and runs (or links) iOS simulator tests when iOS-relevant files changed.
  - `ui`: `quick` + Compose UI and screenshot-golden tests (`:foundation-ui:desktopTest`; record new goldens with `-DrecordGolden=true`) + the consumer test below.
  - `e2e`: `ui` + [Maestro](https://maestro.dev) flows on an Android emulator and an iOS simulator (`.maestro/smoke.yaml`), plus an Android over-install test from the 1.2.2 baseline APK when one is available. Needs Maestro, an Android emulator, and Xcode.
- **Composite-build consumer test:** run `scripts/consumer-test.sh` (or `scripts/consumer-test.sh --no-ios` to skip iOS simulator linking/tests) to verify that `consumer-test/` compiles and passes unit tests against Foundation as an included build.

## Publishing
- Android
  - Create a `keystore.properties` file in the repository root directory for production signing (and a production 16-byte `encryptionKey`). Never commit `keystore.properties` or your `.jks` keystore to version control.
  - The demo app isn't published, so its release build type leaves `signingConfig = signingConfigs.getByName("release")` commented out in `androidApp/build.gradle.kts`. Enable it (or use `signingConfigs.findByName("release")`) in a real app.
```properties
storeFile=/Users/Example/Documents/Keystores/upload-keystore.jks
storePassword=myStorePassword
keyAlias=upload
keyPassword=myKeyPassword
encryptionKey=16ByteSecretKey!
```
- iOS
  - Archive and distribute through Xcode using `iosApp/iosApp.xcodeproj`.
- Desktop
  - Package native distributions with `./gradlew :desktopApp:packageDistributionForCurrentOS` (or `packageDmg` / `packageMsi` / `packageDeb`).
