#!/usr/bin/env bash
# scripts/e2e/over-install.sh
# Over-install verification harness:
# 1. Uninstalls target app
# 2. Installs baseline APK
# 3. Runs seed Maestro flow to persist user settings
# 4. Installs new APK over the baseline with adb install -r
# 5. Runs assert Maestro flow to verify settings migrated correctly
#
# Usage:
#   scripts/e2e/over-install.sh <baseline.apk> <new.apk> <seed-flow> <assert-flow>

set -euo pipefail
export MAESTRO_CLI_NO_ANALYTICS=1 # opt out of Maestro's anonymous analytics

if [ "$#" -lt 4 ]; then
    echo "Usage: $0 <baseline.apk> <new.apk> <seed-flow> <assert-flow>"
    exit 1
fi

BASELINE_APK="$1"
NEW_APK="$2"
SEED_FLOW="$3"
ASSERT_FLOW="$4"

if [ ! -f "$BASELINE_APK" ]; then
    echo "Error: baseline APK not found at $BASELINE_APK" >&2
    exit 1
fi

if [ ! -f "$NEW_APK" ]; then
    echo "Error: new APK not found at $NEW_APK" >&2
    exit 1
fi

if [ ! -f "$SEED_FLOW" ]; then
    echo "Error: seed flow not found at $SEED_FLOW" >&2
    exit 1
fi

if [ ! -f "$ASSERT_FLOW" ]; then
    echo "Error: assert flow not found at $ASSERT_FLOW" >&2
    exit 1
fi

# Locate adb
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
if [ ! -x "$ADB" ] && command -v adb >/dev/null 2>&1; then
    ADB="$(command -v adb)"
fi
if [ ! -x "$ADB" ]; then
    echo "Error: adb not found (checked $ADB and PATH)" >&2
    exit 1
fi

# Locate maestro
MAESTRO="${MAESTRO:-maestro}"
if ! command -v "$MAESTRO" >/dev/null 2>&1; then
    if [ -x "$HOME/.maestro/bin/maestro" ]; then
        MAESTRO="$HOME/.maestro/bin/maestro"
    else
        echo "Error: maestro not found on PATH or ~/.maestro/bin" >&2
        exit 1
    fi
fi

# Extract APP_ID from baseline APK if not provided
if [ -z "${APP_ID:-}" ]; then
    AAPT_BIN="$(find "$HOME/Library/Android/sdk/build-tools" -name "aapt" 2>/dev/null | sort -V | tail -n 1)"
    if [ -n "$AAPT_BIN" ] && [ -x "$AAPT_BIN" ]; then
        APP_ID="$("$AAPT_BIN" dump badging "$BASELINE_APK" 2>/dev/null | sed -n "s/package: name='\([^']*\)'.*/\1/p")"
    fi
fi

if [ -z "${APP_ID:-}" ]; then
    echo "Error: could not determine APP_ID from $BASELINE_APK and APP_ID env var is not set" >&2
    exit 1
fi

echo "==> Target APP_ID: $APP_ID"

echo "==> Step 1: Uninstalling existing $APP_ID..."
"$ADB" uninstall "$APP_ID" >/dev/null 2>&1 || true

echo "==> Step 2: Installing baseline APK ($BASELINE_APK)..."
"$ADB" install "$BASELINE_APK"

echo "==> Step 3: Running seed flow ($SEED_FLOW)..."
"$MAESTRO" --platform android test -e APP_ID="$APP_ID" "$SEED_FLOW"

echo "==> Step 4: Installing new APK over baseline ($NEW_APK)..."
"$ADB" install -r "$NEW_APK"

echo "==> Step 5: Running assert flow ($ASSERT_FLOW)..."
"$MAESTRO" --platform android test -e APP_ID="$APP_ID" "$ASSERT_FLOW"

echo "==> Over-install verification SUCCEEDED for $APP_ID"
