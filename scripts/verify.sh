#!/usr/bin/env bash
# Verification entry point. Usage: scripts/verify.sh quick|ui|e2e
#   quick: compile Android debug (+ desktop if present) + JVM/common unit tests,
#          plus iosSimulatorArm64Test when iOS-related files changed.
#   ui/e2e: not wired yet.
# Env: VERIFY_BASE = git base for the iOS-change check (default release/v2.0.0).
# Compatible with macOS bash 3.2.

set -u

TIER="${1:-}"
case "$TIER" in
    quick|ui|e2e) ;;
    *)
        echo "Usage: scripts/verify.sh quick|ui|e2e" >&2
        exit 2
        ;;
esac

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_NAME="$(basename "$REPO")"
cd "$REPO" || exit 1

LOCK_ROOT="$HOME/.cache/bridge-supplies"
LOCK_WAIT_SECONDS=1800
LOCK_POLL_SECONDS=10
HELD_LOCKS=""

# --- Locks -------------------------------------------------------------------

# acquire_lock <name>: atomic mkdir lock at $LOCK_ROOT/<name>.lock
acquire_lock() {
    local name="$1"
    local dir="$LOCK_ROOT/$name.lock"
    local waited=0
    local announced=0
    mkdir -p "$LOCK_ROOT"
    while true; do
        if mkdir "$dir" 2>/dev/null; then
            echo "$$ $REPO_NAME $TIER $(date '+%Y-%m-%dT%H:%M:%S')" > "$dir/info"
            HELD_LOCKS="$HELD_LOCKS $name"
            return 0
        fi
        local holder=""
        if [ -f "$dir/info" ]; then
            holder="$(cut -d' ' -f1 "$dir/info" 2>/dev/null)"
        fi
        if [ -n "$holder" ] && ! kill -0 "$holder" 2>/dev/null; then
            echo "Lock '$name' is stale (PID $holder is gone); taking it over."
            # Atomic rename so only one contender removes the stale lock.
            if mv "$dir" "$dir.stale.$$" 2>/dev/null; then
                rm -rf "$dir.stale.$$"
            fi
            continue
        fi
        if [ "$waited" -ge "$LOCK_WAIT_SECONDS" ]; then
            echo "Timed out after ${LOCK_WAIT_SECONDS}s waiting for lock '$name'." >&2
            return 1
        fi
        if [ "$announced" -eq 0 ] || [ $((waited % 60)) -eq 0 ]; then
            echo "Waiting for lock '$name' held by: $(cat "$dir/info" 2>/dev/null || echo unknown) (waited ${waited}s)"
            announced=1
        fi
        sleep "$LOCK_POLL_SECONDS"
        waited=$((waited + LOCK_POLL_SECONDS))
    done
}

release_lock() {
    local name="$1"
    rm -rf "$LOCK_ROOT/$name.lock"
}

release_all_locks() {
    local n
    for n in $HELD_LOCKS; do
        release_lock "$n"
    done
    HELD_LOCKS=""
}

# Helpers for the emulator lock (unused by `quick`; for ui/e2e tiers later).
acquire_emulator_lock() { acquire_lock emulator; }
release_emulator_lock() { release_lock emulator; HELD_LOCKS="$(echo "$HELD_LOCKS" | sed 's/ emulator//')"; }

trap release_all_locks EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

# --- Module detection --------------------------------------------------------

SETTINGS=""
for f in settings.gradle.kts settings.gradle; do
    [ -f "$f" ] && SETTINGS="$f" && break
done
if [ -z "$SETTINGS" ]; then
    echo "No settings.gradle(.kts) found in $REPO" >&2
    exit 1
fi

has_module() {
    grep -E "include" "$SETTINGS" | grep -q "[\"']:$1[\"']"
}

if has_module androidApp; then
    ANDROID_TASK=":androidApp:assembleDebug"
    ANDROID_UNIT_TEST=":androidApp:testDebugUnitTest"
elif has_module composeApp; then
    ANDROID_TASK=":composeApp:assembleDebug"
    ANDROID_UNIT_TEST=":composeApp:testDebugUnitTest"
elif has_module app; then
    ANDROID_TASK=":app:assembleDebug"
    ANDROID_UNIT_TEST=":app:testDebugUnitTest"
else
    echo "Could not detect an Android module in $SETTINGS" >&2
    exit 1
fi

# Does a module's build script mention a desktop JVM target?
module_has_desktop_target() {
    local f="$REPO/$1/build.gradle.kts"
    [ -f "$f" ] || f="$REPO/$1/build.gradle"
    [ -f "$f" ] && grep -Eq 'jvm\("desktop"\)|jvm\("desktop"|jvm\(.desktop.\)' "$f"
}

DESKTOP_TASK=""
TEST_TASK=""
if has_module desktopApp; then
    DESKTOP_TASK=":desktopApp:compileKotlin"
fi
if has_module composeApp && module_has_desktop_target composeApp; then
    [ -z "$DESKTOP_TASK" ] && DESKTOP_TASK=":composeApp:compileKotlinDesktop"
    TEST_TASK="desktopTest" # every module with a desktop target
fi
if [ -z "$TEST_TASK" ]; then
    TEST_TASK="$ANDROID_UNIT_TEST"
fi

# --- iOS change detection ----------------------------------------------------

BASE="${VERIFY_BASE:-release/v2.0.0}"
IOS_CHANGED=0
CHANGED="$( { git diff --name-only "$BASE"...HEAD 2>/dev/null; git diff --name-only HEAD 2>/dev/null; } | sort -u)"
# iOS sources and Gradle build config (the memory caps in gradle.properties don't count).
if echo "$CHANGED" | grep -Eq '(^|/)iosMain/|^iosApp/|\.gradle(\.kts)?$|libs\.versions\.toml$|gradle-wrapper\.properties$'; then
    IOS_CHANGED=1
fi

# --- Run ---------------------------------------------------------------------

STAMP="$(date '+%Y%m%d-%H%M%S')"
mkdir -p "$REPO/build/verify"
LOG="$REPO/build/verify/$TIER-$STAMP.log"
: > "$LOG"

echo "verify.sh $TIER in $REPO_NAME (log: build/verify/$TIER-$STAMP.log)"
acquire_lock build || exit 1

SUMMARY=""
OVERALL=0
FAILED_STEP=""

record() { # name status seconds
    SUMMARY="$SUMMARY$(printf '  %-6s %-40s %ss' "$2" "$1" "$3")
"
}

run_gradle_step() { # name gradle-task
    local name="$1" task="$2" start end rc
    echo ">> $name: ./gradlew $task"
    echo "=== $name: ./gradlew $task ===" >> "$LOG"
    start="$(date +%s)"
    ./gradlew "$task" --console=plain --no-daemon >> "$LOG" 2>&1
    rc=$?
    end="$(date +%s)"
    if [ "$rc" -eq 0 ]; then
        record "$name" PASS $((end - start))
    else
        record "$name" FAIL $((end - start))
        OVERALL=1
        FAILED_STEP="$name"
    fi
    return "$rc"
}

skip_step() { # name reason
    echo ">> $1: skipped ($2)"
    record "$1 ($2)" SKIP 0
}

run_consumer_test_step() {
    local name="consumer-test"
    local start end rc
    local cmd="$REPO/scripts/consumer-test.sh"
    if [ ! -f "$cmd" ]; then
        skip_step "$name" "no consumer-test.sh"
        return 0
    fi
    echo ">> $name: scripts/consumer-test.sh"
    echo "=== $name: scripts/consumer-test.sh ===" >> "$LOG"
    start="$(date +%s)"
    if [ "$IOS_CHANGED" -eq 1 ]; then
        "$cmd" >> "$LOG" 2>&1
    else
        "$cmd" --no-ios >> "$LOG" 2>&1
    fi
    rc=$?
    end="$(date +%s)"
    if [ "$rc" -eq 0 ]; then
        record "$name" PASS $((end - start))
    else
        record "$name" FAIL $((end - start))
        OVERALL=1
        FAILED_STEP="$name"
    fi
    return "$rc"
}

run_maestro_step() {
    local name="$1"
    shift
    local start end rc
    echo ">> $name: $*"
    echo "=== $name: $* ===" >> "$LOG"
    start="$(date +%s)"
    "$@" >> "$LOG" 2>&1
    rc=$?
    end="$(date +%s)"
    if [ "$rc" -eq 0 ]; then
        record "$name" PASS $((end - start))
    else
        record "$name" FAIL $((end - start))
        OVERALL=1
        FAILED_STEP="$name"
    fi
    return "$rc"
}

RUN_START="$(date +%s)"

run_gradle_step "android-debug" "$ANDROID_TASK"
if [ "$OVERALL" -eq 0 ]; then
    if [ -n "$DESKTOP_TASK" ]; then
        run_gradle_step "desktop-compile" "$DESKTOP_TASK"
    else
        skip_step "desktop-compile" "no desktop target"
    fi
fi
if [ "$OVERALL" -eq 0 ]; then
    run_gradle_step "unit-tests" "$TEST_TASK"
fi
if [ "$OVERALL" -eq 0 ]; then
    if [ "$IOS_CHANGED" -eq 1 ]; then
        if xcrun simctl list devices available 2>/dev/null | grep -q "iPhone"; then
            run_gradle_step "ios-tests" "iosSimulatorArm64Test"
        else
            # No simulator runtime installed: still compile + link the test binary so code breakage is caught.
            echo "no iOS simulator runtime installed: linking iOS tests without running them"
            run_gradle_step "ios-link (no simulator)" "linkDebugTestIosSimulatorArm64"
        fi
    else
        echo "skipped iOS (no iOS-related changes)"
        skip_step "ios-tests" "no iOS-related changes"
    fi
fi

# --- UI tier (FND-08) --------------------------------------------------------
if [ "$OVERALL" -eq 0 ] && [ "$TIER" != "quick" ]; then
    if has_module foundation-ui; then
        run_gradle_step "ui-tests" ":foundation-ui:desktopTest"
    fi
    if [ "$OVERALL" -eq 0 ]; then
        run_consumer_test_step
    fi
fi

# --- E2E tier (FND-08) -------------------------------------------------------
if [ "$OVERALL" -eq 0 ] && [ "$TIER" = "e2e" ]; then
    acquire_emulator_lock || exit 1

    # Locate adb
    ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
    if [ ! -x "$ADB" ] && command -v adb >/dev/null 2>&1; then
        ADB="$(command -v adb)"
    fi

    # Locate maestro
    MAESTRO="${MAESTRO:-maestro}"
    if ! command -v "$MAESTRO" >/dev/null 2>&1; then
        if [ -x "$HOME/.maestro/bin/maestro" ]; then
            MAESTRO="$HOME/.maestro/bin/maestro"
        fi
    fi

    # Ensure Android emulator is running
    EMULATOR_WAS_RUNNING=0
    if "$ADB" devices 2>/dev/null | grep -E "emulator-[0-9]+" | grep -q "device"; then
        EMULATOR_WAS_RUNNING=1
    fi

    if [ "$EMULATOR_WAS_RUNNING" -eq 0 ]; then
        echo ">> Booting Android emulator medium_phone..."
        EMU_BIN="$HOME/Library/Android/sdk/emulator/emulator"
        if [ ! -x "$EMU_BIN" ] && command -v emulator >/dev/null 2>&1; then
            EMU_BIN="$(command -v emulator)"
        fi
        "$EMU_BIN" -avd medium_phone -netdelay none -netspeed full &
        "$ADB" wait-for-device >> "$LOG" 2>&1
        while [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
            sleep 2
        done
        echo ">> Android emulator ready."
    fi

    # Locate Android debug APK
    DEMO_APK=""
    if [ -f "$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk" ]; then
        DEMO_APK="$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
    elif [ -f "$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
        DEMO_APK="$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk"
    fi

    if [ -z "$DEMO_APK" ] || [ ! -f "$DEMO_APK" ]; then
        run_gradle_step "demo-apk-build" "$ANDROID_TASK"
        if [ -f "$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk" ]; then
            DEMO_APK="$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
        elif [ -f "$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
            DEMO_APK="$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk"
        fi
    fi

    # Run Android Maestro smoke flow
    if [ "$OVERALL" -eq 0 ] && [ -n "$DEMO_APK" ] && [ -f "$DEMO_APK" ]; then
        "$ADB" install -r "$DEMO_APK" >> "$LOG" 2>&1
        run_maestro_step "e2e-smoke-android" "$MAESTRO" --platform android test -e APP_ID=bridge.supplies.foundation.debug "$REPO/.maestro/smoke.yaml"
    fi

    # Run iOS Maestro smoke flow if simulator runtime exists
    if [ "$OVERALL" -eq 0 ]; then
        if xcrun simctl list devices available 2>/dev/null | grep -q "iPhone"; then
            IOS_APP_PATH="$REPO/build/ios-derived/Build/Products/Debug-iphonesimulator/Foundation_.app"
            if [ ! -d "$IOS_APP_PATH" ] && [ -f "$REPO/iosApp/iosApp.xcodeproj/project.pbxproj" ]; then
                echo ">> Building iOS app for simulator..."
                xcodebuild -project "$REPO/iosApp/iosApp.xcodeproj" -scheme iOS \
                    -destination "generic/platform=iOS Simulator" \
                    -derivedDataPath "$REPO/build/ios-derived" \
                    CODE_SIGNING_ALLOWED=NO build >> "$LOG" 2>&1
            fi
            if [ -d "$IOS_APP_PATH" ]; then
                BOOTED_SIM="$(xcrun simctl list devices 2>/dev/null | grep -E "iPhone.*\(Booted\)" | sed -E 's/.*\(([A-F0-9-]+)\).*/\1/' | head -n 1)"
                if [ -z "$BOOTED_SIM" ]; then
                    BOOTED_SIM="CCE1DBF4-83D7-4A2E-A689-52F4D6419A8E"
                    xcrun simctl boot "$BOOTED_SIM" 2>/dev/null || true
                fi
                xcrun simctl install "$BOOTED_SIM" "$IOS_APP_PATH" >> "$LOG" 2>&1
                run_maestro_step "e2e-smoke-ios" "$MAESTRO" --platform ios test -e APP_ID=bridge.supplies.foundation.BridgeFoundation "$REPO/.maestro/smoke.yaml"
            else
                skip_step "e2e-smoke-ios" "iOS app build not available"
            fi
        else
            skip_step "e2e-smoke-ios" "no iOS simulator runtime"
        fi
    fi

    # Run over-install harness if baseline exists
    if [ "$OVERALL" -eq 0 ] && [ -n "$DEMO_APK" ] && [ -f "$DEMO_APK" ]; then
        BASELINES_DIR=""
        for b in "$REPO/baselines/foundation" "$REPO/../baselines/foundation" "$REPO/../../baselines/foundation" "$HOME/Documents/GitHub/Bridge Supplies/baselines/foundation"; do
            if [ -d "$b" ]; then
                BASELINES_DIR="$b"
                break
            fi
        done
        BASELINE_APK=""
        if [ -n "$BASELINES_DIR" ]; then
            BASELINE_APK="$(find "$BASELINES_DIR" -name "*.apk" 2>/dev/null | sort -V | tail -n 1)"
        fi
        if [ -n "$BASELINE_APK" ] && [ -f "$BASELINE_APK" ]; then
            run_maestro_step "e2e-over-install" "$REPO/scripts/e2e/over-install.sh" "$BASELINE_APK" "$DEMO_APK" "$REPO/.maestro/seed-settings.yaml" "$REPO/.maestro/assert-settings.yaml"
        else
            skip_step "e2e-over-install" "no baseline APK found"
        fi
    fi

    release_emulator_lock
fi

RUN_END="$(date +%s)"

echo
echo "===== verify $TIER summary ====="
printf '%s' "$SUMMARY"
if [ "$OVERALL" -eq 0 ]; then
    echo "RESULT: PASS ($((RUN_END - RUN_START))s total)"
    echo "Log: $LOG"
    exit 0
fi

echo "RESULT: FAIL at '$FAILED_STEP' ($((RUN_END - RUN_START))s total)"
echo "Log: $LOG"
echo "----- last relevant error lines -----"
ERRS="$(grep -E '^e: |error:|FAILED|What went wrong|Exception|Execution failed|> Task .* FAILED' "$LOG" | tail -n 40)"
if [ -n "$ERRS" ]; then
    echo "$ERRS"
else
    tail -n 40 "$LOG"
fi
exit 1
