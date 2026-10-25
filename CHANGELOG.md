# Changelog

All notable changes to Foundation are documented in this file.

## 2.0.1

### Fixed
- **Day counts across daylight-saving changes:** `daysBetween` (and `getDaysSinceBirth`, which uses it) counted days in the device's time zone, although dates are stored as UTC midnight. In zones with daylight-saving time, spans crossing a clock change came out one day short (for example, a summer birthday viewed in winter). Days are now counted in UTC, which is exact for UTC-midnight dates. Apps that show day numbers will show the corrected value.
- **Lost keystrokes in `TextInput`:** the field reset its text whenever the caller's `text` changed. Callers that store each change asynchronously could hand back an older value mid-typing, and the next keystroke landed on that older text, dropping letters for fast typists. The field now keeps its own text while focused and takes outside changes only when it isn't being edited. It also no longer calls `onValueChange` with a value equal to `text` (including on first composition).

## 2.0.0

Foundation 2.0.0 extracts the reusable infrastructure into the `:foundation-core` and `:foundation-ui` multiplatform library modules for consumption via Git submodules and Gradle composite builds, upgrades the toolchain to AGP 9 and Navigation 3, and replaces template file edits with declarative extension points while preserving 100% backward compatibility with persisted 1.x DataStore preferences and legacy v1 QR codes.

### Added
- **Library modules & composite-build support:**
  - `:foundation-core` (`bridge.supplies.foundation.core`): Compose-free KMP library providing `FoundationConfig`, `StorageConfig`, `FeatureFlags`, `FoundationSettings` / `DataStoreSettings`, typed `SettingKey`s (`BooleanSetting`, `IntSetting`, `LongSetting`, `StringSetting`, `StringSetSetting`, `EnumSetting`, `JsonSetting`, `settingName()`), `QrCodec` / `FoundationQrCodec`, `FoundationPlatform` (`PlatformType`, `PlatformFeature`, `currentPlatform()`), platform services (`openUrl`, `openAppSettings`, `randomUuid`), `SheetController` / `SheetRequest`, date/time and formatting utilities, and `foundationCoreModule(config)`.
  - `:foundation-ui` (`bridge.supplies.foundation.ui`): Compose Multiplatform UI library providing `FoundationApp`, `FoundationViewModel` (`foundationUiModule()`, `foundationViewModel()`), `FoundationTheme` (`ThemeConfig`, `ColorTheme`, `Palette`, `DarkMode`), `FoundationSettingsScreen` (`BuiltInSettings`, `SettingsScope` DSL, `SettingSwitch`, `SettingChoice`, `FoundationSettingKeys`), `SheetHost` (`SheetContent`, `SheetScope`, `sheetController()`, `DateSheet`, `ShareAppSheet`, `SheetColumn`, `CloseButton`), `FoundationAboutScreen` (`AboutConfig`), `ShareQrCode`, `ScanQrScreen`, `QrCodeScanner`, and reusable UI components (`EdgeFade*`, `IndexedLazy*`, `TitledCard`, `ExpandableTitledCard`, `DatePickerCard`, `PastOrPresentSelectableDates`, `SettingsSwitch`, `SettingsSelector`, `QrCodeCard`).
  - `consumer-test/` standalone composite-build verification project and `scripts/verify.sh` / `scripts/consumer-test.sh`.
- **Material 3 Expressive motion setting:** Added `FoundationSettingKeys.expressiveAnimation` and a built-in toggle in Settings that switches `MaterialTheme` between `MotionScheme.expressive()` and `MotionScheme.standard()`.
- **Versioned QR codec (`QrCodec` v2):** New `"BS2"` format (`E` encrypted / `P` plain + URL-safe Base64 of GZIP-compressed JSON, encrypted with AES-CBC/PKCS7 using a fresh 16-byte `SecureRandom` IV prepended to each payload) while retaining full read support for legacy v1 hex AES-CBC and plain JSON payloads.
- **In-app browser mode:** `openUrl(url, inApp = true)` opens URLs in Android Custom Tabs (`androidx.browser`) and iOS `SFSafariViewController`, falling back to the system browser on Desktop.
- **Immersive screen chrome & custom landing flows:** `screenMetadata(chrome = Chrome.Immersive(hideSystemBars = ...))` hides top bar, bottom bar, and navigation rail for fullscreen screens; `Landing.Default(icon, extraTips)`, `Landing.Custom`, and `Landing.None` configure launch onboarding.
- **Customizable `DatePickerCard`:** Added `title` and `trailing` composable slots to `DatePickerCard` and made past-date restriction opt-in via `PastOrPresentSelectableDates`.
- **Kotlin Explicit API mode:** Enabled `explicitApi()` on `:foundation-core` and `:foundation-ui` so all public library declarations carry explicit visibility and types.

### Changed
- **Toolchain & dependencies:**
  - Gradle **9.8.2** with Foojay JVM toolchain provisioning (JDK 21 via `gradle/gradle-daemon-jvm.properties`)
  - Android Gradle Plugin **9.4.1** (`compileSdk = 37`, `targetSdk = 36`, `minSdk = 28`)
  - Kotlin **2.4.21**
  - Compose Multiplatform **1.12.2** and Material 3 **1.12.0-alpha03** (replaced deprecated `compose.*` Gradle plugin aliases with direct version catalog coordinates)
  - Navigation 3 (`org.jetbrains.androidx.navigation3:navigation3-ui:1.2.0`) replacing `navigation-compose`
  - AndroidX Lifecycle / ViewModel **2.11.0**, DataStore **1.3.0**, Core KTX **1.19.1**, Activity Compose **1.13.0**, Browser **1.10.0**
  - Koin **4.2.2**, kotlinx-coroutines **1.11.0**, kotlinx-serialization **1.12.0**, kotlinx-datetime **0.8.0** (using `kotlin.time.Instant` / `Clock`)
  - MaterialKolor **5.0.1**, KScan **0.10.0**, korlibs-crypto **6.0.1** (migrated from `com.soywiz.korlibs.krypto`)
- **AGP 9 module structure:** Split the monolithic `:composeApp` module into `:composeApp` (`com.android.kotlin.multiplatform.library`, demo package `bridge.supplies.foundation.demo`), `:androidApp` (`com.android.application` with built-in Kotlin), `:desktopApp` (`kotlin("jvm")`), and `iosApp`.
- **Navigation shell:** Replaced string-based Nav 2 routes and the `NavigationTab` / `Screen` / `NavArgument` / `AppBarAction` enums with `FoundationApp`, `@Serializable` `NavKey` routes registered in `RouteRegistry`, per-tab `NavBackStack`s, `LocalNavigator`, and composition-scoped `TopBarActions`.
- **Desktop DataStore location:** Desktop preferences now live in the per-user application data directory (`~/Library/Application Support/<packageName>` on macOS, `%APPDATA%` on Windows, `$XDG_DATA_HOME` on Linux), with a one-time automatic copy if a legacy `settings.preferences_pb` file exists in the working directory.
- **Optional `keystore.properties`:** Builds no longer fail when `keystore.properties` is absent; debug builds log a warning, use a development fallback encryption key, and skip release signing configuration.
- **Platform service names:** Renamed `browseWeb(url)` to `openUrl(url, inApp = false)` and `systemAppSettings()` to `openAppSettings()`.
- **Implementation details are now `internal`:** the `md_theme_*` color constants, `seed`, `LightColorScheme` / `DarkColorScheme` / `Typography` (use `FoundationTheme` + `ThemeConfig`), the shell's `TRANSITION_*` constants and `Screen*Transition()` / `TopLevel*Transition()` functions, the DataStore path helpers (`createDataStore`, `createSettingsDataStore`, `settingsFilePath`, `dataStoreFileName`; use `FoundationSettings`), and the v1 crypto functions (`QrCodec.decode` still reads v1 payloads).

### Removed
- `org.jetbrains.androidx.navigation:navigation-compose` dependency and the old `Screen`, `NavigationTab`, `NavArgument`, and `AppBarAction` enums.
- Monolithic `DataRepository` and `StoredValue` hierarchy in `composeApp` (replaced by `FoundationSettings`, `SettingKey`, and `FoundationSettingKeys`).
- Hard-coded `org.gradle.java.home` path in `gradle.properties` and `junit` dependency in `commonMain`.
- Deprecated `Window.statusBarColor` and `Window.navigationBarColor` writes on Android (handled via edge-to-edge insets controller).

### Fixed
- **QR typing lag:** Debounced QR code generation in `ShareQrCode` (300 ms delay inside `LaunchedEffect`), cancelling in-flight encryption/compression/rendering jobs on new keystrokes instead of spawning concurrent uncancelled jobs.
- **About screen Version/Build label swap:** Fixed swapped `app_about_version` and `app_about_build` string resource values on the About screen.
- **Empty open-source license list:** Configured the `jk1` license report plugin to scan `androidRuntimeClasspath` and `desktopRuntimeClasspath` (excluding `bridge.supplies.foundation`) and wired `prepareLicenseReport` before `prepareComposeResourcesTaskFor*` so `licenses.json` is populated in KMP library builds.
- **Desktop window size persistence:** Read `desktopResolutionX` and `desktopResolutionY` synchronously with `runBlocking` before opening the Desktop window so the initial default window size cannot overwrite the user's saved dimensions.
- **Desktop main class resolution:** Packaged the Desktop entry point under `bridge.supplies.foundation.Foundation` to match `mainClass`, fixing `./gradlew :desktopApp:run` and native distribution packaging.

---

### Migrating an app from Foundation 1.x

Follow this checklist in order when migrating an existing Foundation 1.x app (such as Adepticadian or Archetypist) to Foundation 2.0.0:

1. **Submodule + composite build**
   - Add Foundation as a submodule at `foundation/`:
     ```bash
     git submodule add https://github.com/Bridge-Supplies/Foundation-KMP-Compose.git foundation
     ```
   - In `settings.gradle.kts`, create the `foundationLibs` version catalog from `foundation/gradle/libs.versions.toml` and add `includeBuild("foundation")` (see `consumer-test/settings.gradle.kts`).
   - Alias all Gradle plugins (`kotlinMultiplatform`, `androidMultiplatformLibrary`, `jetbrainsCompose`, `compose.compiler`, `kotlinxSerialization`) from `foundationLibs` so plugin versions match the included build.
   - Split your app module for AGP 9 into `:composeApp` (`com.android.kotlin.multiplatform.library`), `:androidApp` (`com.android.application`), and `:desktopApp` (if targeting Desktop).
   - Add `implementation("bridge.supplies.foundation:foundation-ui:${foundationLibs.versions.app.versionName.get()}")` to `commonMain.dependencies` and `implementation(compose.desktop.currentOs)` to `desktopTest.dependencies`.

2. **Settings**
   - Keep the default file name (`settings.preferences_pb`) and the exact existing `"Settings_*"` key strings using `settingName("<suffix>")`.
   - Delete the app's copy of `DataRepository` and `StoredValue`.
   - Replace references to the 1.x built-in `SettingKeys` and theme enums with `FoundationSettingKeys` (`bridge.supplies.foundation.ui.settings.FoundationSettingKeys`) and `ColorTheme`, `Palette`, and `DarkMode` (`bridge.supplies.foundation.ui.theme.*`). Their stored Int ordinals are unchanged.
   - Define app-specific keys with `BooleanSetting`, `IntSetting`, `LongSetting`, `StringSetting`, `StringSetSetting`, `EnumSetting`, or `JsonSetting` (never reorder enum entries), and read/write them via `FoundationViewModel.setting(key)` / `set(key, value)` or injected `FoundationSettings`.

3. **Navigation onto `FoundationApp` + routes**
   - Replace `navigation-compose` and the `NavigationTab`, `Screen`, and `NavArgument` enums with `@Serializable` route classes/objects implementing `NavKey`, registered in a `RouteRegistry`.
   - Define your top-level tabs as `List<FoundationTab>` and wrap your root UI in `FoundationApp(tabs, routes, ...)`.
   - Attach titles and `Chrome` (`Chrome.Standard` or `Chrome.Immersive`) to entries with `metadata = screenMetadata(...)`.
   - Use `LocalNavigator.current` for navigation (`navigate(route)`, `navigate(tabId, route)` for cross-tab deep links, `selectTab(id)`, `popToRoot()`, `back()`, `openFromOutside(tabId, route)`).
   - Replace `AppBarAction` state plumbing with `TopBarActions(listOf(TopBarAction(icon, label) { ... }))` inside the screens that show top-bar actions.

4. **Sheets**
   - Replace `ActiveBottomSheet` enum entries with classes/objects implementing `SheetRequest`.
   - Open sheets via `sheetController().show(request)` (or inject `SheetController` in ViewModels).
   - Use the built-in `DateSheet` and `ShareAppSheet`, and render app-specific requests in `FoundationApp(sheets = { request -> ... })` using `SheetColumn` and `CloseButton`.

5. **Settings screen DSL**
   - Delete the app's modified `SettingsOptionsScreen` and render `FoundationSettingsScreen(builtIns = BuiltInSettings(...), onNavigateToAbout = { navigator.navigate(AboutRoute) }) { section(title) { switch(...) / choice(...) / link(...) / custom { ... } } }`.
   - To hide QR-sharing settings and tips completely in apps without QR features, set `features = FeatureFlags(qrSharing = false)` on `FoundationConfig`.

6. **About + licenses**
   - Keep the `com.github.jk1.dependency-license-report` plugin in your app's shared module, configured with `configurations = arrayOf("androidRuntimeClasspath", "desktopRuntimeClasspath")`, `excludeGroups = arrayOf("bridge.supplies.foundation")`, and `prepareComposeResourcesTaskFor*` task dependency (matching `composeApp/build.gradle.kts`).
   - Replace the app's `SettingsAboutScreen` copy with `FoundationAboutScreen(AboutConfig(licensesJson = { Res.readBytes("files/json/licenses.json") }, onVersionClick = ..., extraContent = ...))`.

7. **QR**
   - Replace custom `Encryption.kt` / `SharedData` boilerplate, `ShareCodeScreen` QR state in `MainViewModel`, and `ScanCodeScreen` with `QrCodec`, `ShareQrCode`, and `ScanQrScreen`.
   - `QrCodec.decode` automatically reads both v2 (`"BS2..."`) and legacy v1 codes (pass your 1.x payload serializer if decoding legacy codes). Note that Foundation 1.x app versions cannot decode v2 QR codes emitted by 2.0.0+.

8. **Renamed services**
   - Replace `browseWeb(url)` with `openUrl(url, inApp = false)` (or `inApp = true` for in-app Custom Tabs / Safari View Controller).
   - Replace `systemAppSettings()` with `openAppSettings()`.
   - Replace `viewModel.hapticFeedback(haptics)` / custom portrait checks with `rememberHapticFeedback()` and `isPortrait()`.

9. **`DateSheet` selectable dates**
   - `DateSheet` now allows all dates by default (`DatePickerDefaults.AllDates`). Pass `selectableDates = PastOrPresentSelectableDates` to `DateSheet` if your screen requires the 1.x past-or-present date restriction.

10. **Verify over-install with existing user data**
    - Install the migrated debug/release build directly over an existing 1.x installation populated with user data and non-default theme/app settings, and verify that all user data and preferences load without loss.

---

## 1.2.2

Last 1.x release prior to the v2.0.0 multi-module library extraction and AGP 9 / Navigation 3 migration.
