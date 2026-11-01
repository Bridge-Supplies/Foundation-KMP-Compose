# End-to-End (E2E) Test Harness

This directory contains reusable automation scripts for running Maestro-based end-to-end tests and over-install regression verifications across Android and iOS platforms.

## Prerequisites

1. **Maestro CLI** (v2.0+):
   ```bash
   curl -FsSL "https://get.maestro.mobile.dev" | bash
   # Ensure ~/.maestro/bin is on your PATH
   ```
2. **Android SDK Platform Tools** (`adb`):
   - Located on your `PATH` or at `$ANDROID_HOME/platform-tools/adb` / `~/Library/Android/sdk/platform-tools/adb`.
3. **Running Android Emulator**:
   - Start an AVD (e.g., `emulator -avd medium_phone`).
4. **Running iOS Simulator** *(optional, for iOS verification)*:
   - Boot a simulator via `xcrun simctl boot <UDID>`.

---

## Scripts

### `over-install.sh`

Verifies that upgrading an application preserves user state and data across major versions (e.g., upgrading from v1.x to v2.x):

1. Uninstalls any existing installation of the app on the connected device.
2. Installs the **baseline** APK (e.g., previous stable release).
3. Executes a **seed** Maestro flow to configure persistent state (e.g., user preferences).
4. Installs the **new** APK over the baseline using `adb install -r`.
5. Executes an **assert** Maestro flow to verify that all persisted settings survived the upgrade.

#### Usage

```bash
scripts/e2e/over-install.sh <baseline.apk> <new.apk> <seed-flow.yaml> <assert-flow.yaml>
```

#### Environment Variables

- `E2E_BASELINES_DIR`: (`verify.sh e2e`) folder holding the baseline APK for the over-install check. Default: a `baselines/foundation/` folder in or above the repo; the check is skipped when none is found.
- `APP_ID`: Application ID (e.g., `com.example.app.debug`). If omitted, `over-install.sh` automatically detects the package name from `<baseline.apk>` using `aapt`.
- `ADB`: Explicit path to `adb` binary.
- `MAESTRO`: Explicit path to `maestro` binary.

#### Example

```bash
APP_ID=com.example.app.debug \
scripts/e2e/over-install.sh \
  baselines/app-v1.0.0-debug.apk \
  build/outputs/apk/debug/app-debug.apk \
  .maestro/seed-settings.yaml \
  .maestro/assert-settings.yaml
```

---

## Flow Structure & Best Practices

Maestro flows are located in `.maestro/`:
- `smoke.yaml`: Core application navigation, bottom sheet interaction, tab switching, and dialogs.
- `seed-settings.yaml`: Mutates user preferences on a fresh installation.
- `assert-settings.yaml`: Asserts that previously seeded settings remain active.

### Cross-Platform Selectors

Material 3 Compose maps selection semantics differently on Android and iOS:
- **Android**: `SegmentedButton` and toggleable elements expose `checked: true` on parent container nodes while child labels contain the option text.
- **iOS**: Accessibility nodes expose `selected: true` directly on the text element.

Use Maestro's conditional `when` blocks to handle these platform variations cleanly:

```yaml
appId: ${APP_ID}
---
- launchApp
- tapOn: "Settings"
- runFlow:
    when:
      platform: Android
    commands:
      - assertVisible:
          checked: true
          containsChild:
            text: "Option A"
- runFlow:
    when:
      platform: iOS
    commands:
      - assertVisible:
          text: "Option A"
          selected: true
```

---

### Maestro tips for Compose apps

- **First launch after an install is slow** on the emulator (a fresh debug build can take ~15 s until the first screen's text is there). Use a 30 s `extendedWaitUntil` for the first screen.
- **`hideKeyboard` only works on Android.** On iOS it fails with Compose text fields; close the text keyboard with `pressKey: Enter` (the field's Done action), and wrap `hideKeyboard` in `runFlow: when: platform: Android`.
- **iOS sheets move when the keyboard appears,** and Maestro can tap where an element was before the move. Close the keyboard before tapping buttons, and wrap tap-then-type steps in `retry`.
- **`scrollUntilVisible` counts an element half under the screen edge as visible.** Swipe explicitly when a button must be fully on screen.
- **Switches are often siblings of their labels.** Assert a switch's state with `checked` plus `rightOf`/`below`/`above` bounds around it.

---

## Integrating into `scripts/verify.sh`

Apps copy `scripts/verify.sh` and `scripts/e2e/over-install.sh` and change only the `E2E_*` block at the top of the e2e tier: the Android and iOS app ids, the iOS `.app` name, the baseline folder name, and the seed/assert flows. The tier then:
1. Builds the Android debug APK and runs `.maestro/smoke.yaml` on the emulator.
2. Builds the iOS app for the simulator (incremental, `ARCHS=arm64`) and runs the same smoke flow there.
3. If a baseline APK is found, runs `scripts/e2e/over-install.sh` with the seed and assert flows.
