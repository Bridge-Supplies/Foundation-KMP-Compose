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

## Integrating into `scripts/verify.sh`

App repositories can wire E2E tests into their verification tiers (`scripts/verify.sh e2e`):
1. Compile the debug APK (`:composeApp:assembleDebug` or `:androidApp:assembleDebug`).
2. Run `maestro test -e APP_ID=<app-id> .maestro/smoke.yaml`.
3. If a baseline APK exists in `baselines/`, execute `scripts/e2e/over-install.sh`.
