#!/usr/bin/env bash
# Verification entry point. Usage: scripts/verify.sh quick|ui|e2e
#   quick: formatting check, compile Android debug (+ desktop if present) + JVM/common unit tests,
#          plus iosSimulatorArm64Test when iOS-related files changed since the base.
#   ui: quick + UI tests + the composite-build consumer test.
#   e2e: ui + iosSimulatorArm64Test (always) + Maestro flows on Android and iOS
#        + over-install from a baseline APK (flows in .maestro/).
# Env:
#   VERIFY_BASE                      git base for the iOS-change check. Default: the merge-base of
#                                    HEAD and origin/main (on a release branch, the whole release), else main.
#   VERIFY_TIMEOUT_GRADLE_MIN        limit per Gradle step and for the iOS app build (default 25).
#   VERIFY_TIMEOUT_MAESTRO_MIN       limit per Maestro flow (default 10).
#   VERIFY_TIMEOUT_OVER_INSTALL_MIN  limit for the over-install flow (default 15).
#   VERIFY_KEEP_EMULATOR=1           keep the Android emulator running when this script booted it.
# A step that runs past its limit is killed with its child processes and fails.
# Each run appends one line to ~/.cache/bridge-supplies/verify-history.tsv.
# Compatible with macOS bash 3.2 (which has no `timeout` command).

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
HISTORY_FILE="$LOCK_ROOT/verify-history.tsv"

GRADLE_LIMIT_MIN="${VERIFY_TIMEOUT_GRADLE_MIN:-25}"
MAESTRO_LIMIT_MIN="${VERIFY_TIMEOUT_MAESTRO_MIN:-10}"
OVER_INSTALL_LIMIT_MIN="${VERIFY_TIMEOUT_OVER_INSTALL_MIN:-15}"
EMULATOR_BOOT_LIMIT_MIN=10
for limit in "$GRADLE_LIMIT_MIN" "$MAESTRO_LIMIT_MIN" "$OVER_INSTALL_LIMIT_MIN"; do
    case "$limit" in
        ''|*[!0-9]*|0)
            echo "VERIFY_TIMEOUT_*_MIN must be a whole number of minutes above 0 (got '$limit')." >&2
            exit 2
            ;;
    esac
done

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

# The emulator lock covers the Android emulator and the iOS simulator (e2e tier).
acquire_emulator_lock() { acquire_lock emulator; }
release_emulator_lock() {
    case " $HELD_LOCKS " in
        *" emulator "*)
            release_lock emulator
            HELD_LOCKS="$(echo "$HELD_LOCKS" | sed 's/ emulator//')"
            ;;
    esac
}

# --- Processes and time limits -----------------------------------------------

STEP_PID=""
STEP_TIMED_OUT=0
EMULATOR_PID=""
EMULATOR_STARTED=0
RUN_START=""
HISTORY_WRITTEN=0

# is_alive <pid>: the process exists and isn't a zombie.
is_alive() {
    local state
    state="$(ps -o stat= -p "$1" 2>/dev/null)"
    case "$state" in
        ''|Z*) return 1 ;;
    esac
    return 0
}

# descendants <pid>: every descendant PID of the process, children before their parents.
descendants() {
    local child
    for child in $(pgrep -P "$1" 2>/dev/null); do
        descendants "$child"
        echo "$child"
    done
}

# kill_tree <pid>: TERM the process and its descendants (children first), then KILL whatever is left
# after 10 s. Children that appear during the grace period are added to the tree; nothing outside the
# tree under <pid> is signalled.
kill_tree() {
    local pids p q alive waited=0
    pids="$(descendants "$1") $1"
    # shellcheck disable=SC2086 # split the PID list on purpose
    kill -TERM $pids 2>/dev/null
    while true; do
        alive=""
        for p in $pids; do
            is_alive "$p" && alive="$alive $p"
        done
        [ -z "$alive" ] && return 0
        for p in $alive; do
            for q in $(descendants "$p"); do
                case " $pids " in
                    *" $q "*) ;;
                    *)
                        pids="$q $pids"
                        alive="$q $alive"
                        kill -TERM "$q" 2>/dev/null
                        ;;
                esac
            done
        done
        [ "$waited" -ge 10 ] && break
        sleep 1
        waited=$((waited + 1))
    done
    # shellcheck disable=SC2086
    kill -KILL $alive 2>/dev/null
    return 0
}

# run_limited <minutes> <command...>: run the command in the background with its output in the log,
# and kill its process tree once it runs longer than <minutes>. Sets STEP_TIMED_OUT=1 on a timeout.
run_limited() {
    local minutes="$1" start rc
    shift
    STEP_TIMED_OUT=0
    start="$(date +%s)"
    "$@" >> "$LOG" 2>&1 &
    STEP_PID=$!
    while is_alive "$STEP_PID"; do
        if [ $(($(date +%s) - start)) -ge $((minutes * 60)) ]; then
            STEP_TIMED_OUT=1
            echo "!! timed out after $minutes min; stopping PID $STEP_PID and its children" >> "$LOG"
            kill_tree "$STEP_PID"
            break
        fi
        sleep 1
    done
    wait "$STEP_PID" 2>/dev/null
    rc=$?
    STEP_PID=""
    [ "$STEP_TIMED_OUT" -eq 1 ] && rc=124
    return "$rc"
}

stop_current_step() {
    if [ -n "$STEP_PID" ]; then
        kill_tree "$STEP_PID"
        STEP_PID=""
    fi
}

# Shut down the Android emulator if this run booted it (unless VERIFY_KEEP_EMULATOR=1).
# The flags are cleared only at the end, so an interrupt during the shutdown makes cleanup() finish it.
shutdown_emulator_if_ours() {
    local serial waited=0
    [ "$EMULATOR_STARTED" -eq 1 ] || return 0
    if [ "${VERIFY_KEEP_EMULATOR:-0}" = "1" ]; then
        echo ">> Leaving the Android emulator running (VERIFY_KEEP_EMULATOR=1)."
        EMULATOR_STARTED=0
        return 0
    fi
    echo ">> Shutting down the Android emulator this run booted..."
    serial="$("$ADB" devices 2>/dev/null | grep -Eo '^emulator-[0-9]+' | head -n 1)"
    if [ -n "$serial" ]; then
        "$ADB" -s "$serial" emu kill >> "$LOG" 2>&1 || true
    fi
    while [ -n "$EMULATOR_PID" ] && is_alive "$EMULATOR_PID" && [ "$waited" -lt 60 ]; do
        sleep 1
        waited=$((waited + 1))
    done
    if [ -n "$EMULATOR_PID" ] && is_alive "$EMULATOR_PID"; then
        kill_tree "$EMULATOR_PID"
    fi
    EMULATOR_STARTED=0
    EMULATOR_PID=""
    return 0
}

cleanup() {
    trap '' INT TERM # a second interrupt must not cut the cleanup short
    stop_current_step
    shutdown_emulator_if_ours
    if [ -n "$RUN_START" ] && [ "$HISTORY_WRITTEN" -eq 0 ]; then
        write_history ABORTED
    fi
    release_all_locks
}

trap cleanup EXIT
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
TEST_TASKS="" # space-separated Gradle task list
if has_module desktopApp; then
    DESKTOP_TASK=":desktopApp:compileKotlin"
fi
if has_module composeApp && module_has_desktop_target composeApp; then
    [ -z "$DESKTOP_TASK" ] && DESKTOP_TASK=":composeApp:compileKotlinDesktop"
    TEST_TASKS="desktopTest" # every module with a desktop target
fi
if [ -z "$TEST_TASKS" ]; then
    if has_module androidApp && has_module composeApp; then
        TEST_TASKS=":composeApp:testAndroidHostTest" # mobile-only app: androidApp has no tests of its own
    else
        TEST_TASKS="$ANDROID_UNIT_TEST"
    fi
fi

# --- iOS change detection ----------------------------------------------------

if [ -n "${VERIFY_BASE:-}" ]; then
    BASE="$VERIFY_BASE"
else
    BASE="$(git merge-base HEAD origin/main 2>/dev/null)"
    [ -n "$BASE" ] || BASE="main"
fi
IOS_CHANGED=0
if ! git rev-parse --verify --quiet "$BASE^{commit}" >/dev/null 2>&1; then
    echo "Base '$BASE' not found: running the iOS tests."
    IOS_CHANGED=1
fi
CHANGED="$( { git diff --name-only "$BASE"...HEAD 2>/dev/null; git diff --name-only HEAD 2>/dev/null; git status --porcelain -uall 2>/dev/null | sed 's/^...//'; } | sort -u)"
# iOS sources, shared code, the Foundation submodule and Gradle build config (the memory caps in gradle.properties don't count).
if echo "$CHANGED" | grep -Eq '(^|/)(iosMain|iosTest|commonMain|commonTest)/|^iosApp/|^foundation$|\.gradle(\.kts)?$|libs\.versions\.toml$|gradle-wrapper\.properties$'; then
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
FAILED_STEP=""   # the first failure
FAILED_STEPS=""  # every failure, comma-separated
ANY_TIMEOUT=0
STEP_TIMES=""    # "step=seconds" pairs for the history file

record() { # name status seconds
    SUMMARY="$SUMMARY$(printf '  %-6s %-40s %ss' "$2" "$1" "$3")
"
}

# Step name without a "(reason)" suffix and without spaces, for the history file.
step_key() {
    echo "${1%% (*}" | tr ' \t' '__'
}

mark_failed() { # name
    OVERALL=1
    [ -z "$FAILED_STEP" ] && FAILED_STEP="$1"
    FAILED_STEPS="${FAILED_STEPS:+$FAILED_STEPS,}$(step_key "$1")"
}

# run_step <name> <limit-minutes> <command...>: one timed step, output in the log.
run_step() {
    local name="$1" minutes="$2" start secs rc
    shift 2
    echo ">> $name: $*"
    echo "=== $name: $* ===" >> "$LOG"
    start="$(date +%s)"
    run_limited "$minutes" "$@"
    rc=$?
    secs=$(($(date +%s) - start))
    STEP_TIMES="$STEP_TIMES $(step_key "$name")=$secs"
    if [ "$STEP_TIMED_OUT" -eq 1 ]; then
        echo "!! $name timed out after $minutes min"
        record "$name (timed out after $minutes min)" FAIL "$secs"
        ANY_TIMEOUT=1
        mark_failed "$name"
    elif [ "$rc" -eq 0 ]; then
        record "$name" PASS "$secs"
    else
        record "$name" FAIL "$secs"
        mark_failed "$name"
    fi
    return "$rc"
}

run_gradle_step() { # name gradle-task...
    local name="$1"
    shift
    run_step "$name" "$GRADLE_LIMIT_MIN" ./gradlew "$@" --console=plain --no-daemon
}

run_maestro_step() { # name command...
    local name="$1"
    shift
    run_step "$name" "$MAESTRO_LIMIT_MIN" "$@"
}

skip_step() { # name reason
    echo ">> $1: skipped ($2)"
    record "$1 ($2)" SKIP 0
}

# A step that can't run is a failure: a tier must never pass without having tested the app.
fail_step() { # name reason
    echo "Error: $1: $2" | tee -a "$LOG" >&2
    record "$1" FAIL 0
    STEP_TIMES="$STEP_TIMES $(step_key "$1")=0"
    mark_failed "$1"
}

run_consumer_test_step() {
    local cmd="$REPO/scripts/consumer-test.sh"
    if [ ! -f "$cmd" ]; then
        skip_step "consumer-test" "no consumer-test.sh"
        return 0
    fi
    if [ "$IOS_CHANGED" -eq 1 ]; then
        run_step "consumer-test" "$GRADLE_LIMIT_MIN" "$cmd"
    else
        run_step "consumer-test" "$GRADLE_LIMIT_MIN" "$cmd" --no-ios
    fi
}

# write_history <result>: append this run to the shared history file.
write_history() {
    local branch commit
    HISTORY_WRITTEN=1
    branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)"
    commit="$(git rev-parse --short HEAD 2>/dev/null || echo unknown)"
    if ! git diff --quiet HEAD 2>/dev/null; then
        commit="$commit-dirty"
    fi
    mkdir -p "$LOCK_ROOT"
    if [ ! -f "$HISTORY_FILE" ]; then
        printf 'time\trepo\tbranch\tcommit\ttier\tresult\tfailed\tseconds\tsteps\n' >> "$HISTORY_FILE"
    fi
    printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$(date '+%Y-%m-%dT%H:%M:%S%z')" "$REPO_NAME" "$branch" "$commit" \
        "$TIER" "$1" "${FAILED_STEPS:--}" "$((${RUN_END:-$(date +%s)} - RUN_START))" "${STEP_TIMES# }" >> "$HISTORY_FILE"
}

RUN_START="$(date +%s)"

# Formatting first: it's the quickest check (./gradlew spotlessApply fixes it)
run_gradle_step "format" "spotlessCheck"

run_gradle_step "android-debug" "$ANDROID_TASK"
if [ "$OVERALL" -eq 0 ]; then
    if [ -n "$DESKTOP_TASK" ]; then
        run_gradle_step "desktop-compile" "$DESKTOP_TASK"
    else
        skip_step "desktop-compile" "no desktop target"
    fi
fi
if [ "$OVERALL" -eq 0 ]; then
    # shellcheck disable=SC2086 # split the task list on purpose
    run_gradle_step "unit-tests" $TEST_TASKS
fi
if [ "$OVERALL" -eq 0 ]; then
    # e2e always runs the iOS tests; quick and ui only when iOS-related files changed.
    if [ "$IOS_CHANGED" -eq 1 ] || [ "$TIER" = "e2e" ]; then
        if xcrun simctl list devices available 2>/dev/null | grep -q "iPhone"; then
            run_gradle_step "ios-tests" "iosSimulatorArm64Test"
        else
            # No simulator runtime installed: still compile + link the test binary so code breakage is caught.
            echo "no iOS simulator runtime installed: linking iOS tests without running them"
            run_gradle_step "ios-link (no simulator)" "linkDebugTestIosSimulatorArm64"
        fi
    else
        skip_step "ios-tests" "no iOS-related changes"
    fi
fi

# --- UI tier -----------------------------------------------------------------
if [ "$OVERALL" -eq 0 ] && [ "$TIER" != "quick" ]; then
    if has_module foundation-ui; then
        run_gradle_step "ui-tests" ":foundation-ui:desktopTest"
    fi
    if [ "$OVERALL" -eq 0 ]; then
        run_consumer_test_step
    fi
fi

# --- E2E tier ----------------------------------------------------------------
# App identity for the e2e flows. Apps that copy this script change only this block.
E2E_ANDROID_APP_ID="bridge.supplies.foundation.debug"
E2E_IOS_APP_ID="bridge.supplies.foundation"
E2E_IOS_APP_NAME="Foundation_.app"
E2E_ANDROID_FLOWS="smoke" # .maestro/<flow>.yaml, in order
E2E_IOS_FLOWS="smoke"
E2E_BASELINE_NAME="foundation"
E2E_SEED="$REPO/.maestro/seed-settings.yaml" # how scripts/e2e/over-install.sh seeds the baseline
E2E_ASSERT_FLOW="$REPO/.maestro/assert-settings.yaml"
E2E_STRICT=0 # 1: a missing baseline APK or iPhone simulator fails the tier instead of being skipped

wait_for_android_boot() {
    "$ADB" wait-for-device
    while [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
        sleep 2
    done
}

# Remove simulator apps whose bundle ID extends this app's (an old "<id>.<Name>" build): Maestro's
# clearState hangs forever when the current ID is a prefix of an installed one. Keeps "<id>.debug".
remove_stale_ios_apps() { # simulator
    local id
    for id in $(xcrun simctl listapps "$1" 2>/dev/null | sed -n 's/^[[:space:]]*CFBundleIdentifier = "\{0,1\}\([^";]*\)"\{0,1\};[[:space:]]*$/\1/p' | sort -u); do
        case "$id" in
            "$E2E_IOS_APP_ID.debug") ;;
            "$E2E_IOS_APP_ID".*)
                echo ">> Uninstalling stale simulator app $id" | tee -a "$LOG"
                xcrun simctl uninstall "$1" "$id" >> "$LOG" 2>&1 || echo "Could not uninstall $id" | tee -a "$LOG"
                ;;
        esac
    done
}

if [ "$OVERALL" -eq 0 ] && [ "$TIER" = "e2e" ]; then
    if ! acquire_emulator_lock; then
        fail_step "emulator-lock" "timed out waiting for the emulator lock"
    fi
fi

if [ "$OVERALL" -eq 0 ] && [ "$TIER" = "e2e" ]; then
    # Locate adb
    ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
    if [ ! -x "$ADB" ] && command -v adb >/dev/null 2>&1; then
        ADB="$(command -v adb)"
    fi

    # Locate maestro
    export MAESTRO_CLI_NO_ANALYTICS=1 # opt out of Maestro's anonymous analytics
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
        "$EMU_BIN" -avd medium_phone -netdelay none -netspeed full >/dev/null 2>&1 &
        EMULATOR_PID=$!
        EMULATOR_STARTED=1
        if run_step "android-emulator-boot" "$EMULATOR_BOOT_LIMIT_MIN" wait_for_android_boot; then
            echo ">> Android emulator ready."
        fi
    fi

    # Locate Android debug APK
    DEMO_APK=""
    if [ -f "$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk" ]; then
        DEMO_APK="$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
    elif [ -f "$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
        DEMO_APK="$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk"
    fi

    if [ "$OVERALL" -eq 0 ] && { [ -z "$DEMO_APK" ] || [ ! -f "$DEMO_APK" ]; }; then
        run_gradle_step "demo-apk-build" "$ANDROID_TASK"
        if [ -f "$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk" ]; then
            DEMO_APK="$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
        elif [ -f "$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
            DEMO_APK="$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk"
        fi
    fi

    # Android Maestro flows
    if [ "$OVERALL" -eq 0 ]; then
        if [ -z "$DEMO_APK" ] || [ ! -f "$DEMO_APK" ]; then
            fail_step "android-install" "no debug APK to install"
        elif ! "$ADB" install -r "$DEMO_APK" >> "$LOG" 2>&1; then
            fail_step "android-install" "could not install $DEMO_APK"
        else
            for flow in $E2E_ANDROID_FLOWS; do
                if [ "$OVERALL" -eq 0 ]; then
                    "$ADB" shell am force-stop "$E2E_ANDROID_APP_ID" >> "$LOG" 2>&1 || true
                    run_maestro_step "e2e-$flow-android" "$MAESTRO" --platform android test -e APP_ID="$E2E_ANDROID_APP_ID" "$REPO/.maestro/$flow.yaml"
                fi
            done
        fi
    fi

    # iOS Maestro flows
    if [ "$OVERALL" -eq 0 ]; then
        if ! xcrun simctl list devices available 2>/dev/null | grep -q "iPhone"; then
            if [ "$E2E_STRICT" -eq 1 ]; then
                fail_step "e2e-ios" "no iPhone simulator available"
            else
                skip_step "e2e-ios" "no iOS simulator runtime"
            fi
        else
            IOS_APP_PATH="$REPO/build/ios-derived/Build/Products/Debug-iphonesimulator/$E2E_IOS_APP_NAME"
            # Always rebuild (incremental) so the flows never run against a stale app.
            # Simulator builds are arm64-only: the KMP framework has no x86_64 simulator slice.
            if run_step "ios-app-build" "$GRADLE_LIMIT_MIN" xcodebuild -project "$REPO/iosApp/iosApp.xcodeproj" -scheme iOS \
                -destination "generic/platform=iOS Simulator" \
                -derivedDataPath "$REPO/build/ios-derived" \
                ARCHS=arm64 CODE_SIGNING_ALLOWED=NO build; then
                BOOTED_SIM="$(xcrun simctl list devices 2>/dev/null | grep -E "iPhone.*\(Booted\)" | sed -E 's/.*\(([A-F0-9-]+)\).*/\1/' | head -n 1)"
                if [ -z "$BOOTED_SIM" ]; then
                    BOOTED_SIM="$(xcrun simctl list devices available 2>/dev/null | grep -E "iPhone" | head -n 1 | sed -E 's/.*\(([A-F0-9-]+)\).*/\1/')"
                    if [ -n "$BOOTED_SIM" ]; then
                        xcrun simctl boot "$BOOTED_SIM" 2>/dev/null || true
                    fi
                fi
                remove_stale_ios_apps "$BOOTED_SIM"
                if [ ! -d "$IOS_APP_PATH" ]; then
                    fail_step "ios-install" "built app not found at $IOS_APP_PATH"
                elif ! xcrun simctl install "$BOOTED_SIM" "$IOS_APP_PATH" >> "$LOG" 2>&1; then
                    fail_step "ios-install" "could not install the app on simulator $BOOTED_SIM"
                else
                    for flow in $E2E_IOS_FLOWS; do
                        if [ "$OVERALL" -eq 0 ]; then
                            xcrun simctl terminate "$BOOTED_SIM" "$E2E_IOS_APP_ID" >> "$LOG" 2>&1 || true
                            run_maestro_step "e2e-$flow-ios" "$MAESTRO" --platform ios test -e APP_ID="$E2E_IOS_APP_ID" "$REPO/.maestro/$flow.yaml"
                        fi
                    done
                fi
            fi
        fi
    fi

    # Over-install from the published version's debug baseline (baselines aren't committed)
    if [ "$OVERALL" -eq 0 ]; then
        BASELINES_DIR=""
        for b in ${E2E_BASELINES_DIR:+"$E2E_BASELINES_DIR"} "$REPO/baselines/$E2E_BASELINE_NAME" "$REPO/../baselines/$E2E_BASELINE_NAME" "$REPO/../../baselines/$E2E_BASELINE_NAME"; do
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
            run_step "e2e-over-install" "$OVER_INSTALL_LIMIT_MIN" "$REPO/scripts/e2e/over-install.sh" "$BASELINE_APK" "$DEMO_APK" "$E2E_SEED" "$E2E_ASSERT_FLOW"
        elif [ "$E2E_STRICT" -eq 1 ]; then
            fail_step "e2e-over-install" "baseline APK for $E2E_BASELINE_NAME not found; it isn't committed (see scripts/e2e/README.md)"
        else
            skip_step "e2e-over-install" "no baseline APK found"
        fi
    fi
fi

if [ "$TIER" = "e2e" ]; then
    shutdown_emulator_if_ours
    release_emulator_lock
fi

# --- Summary -----------------------------------------------------------------

RUN_END="$(date +%s)"
if [ "$OVERALL" -eq 0 ]; then
    RESULT_LINE="RESULT: PASS ($((RUN_END - RUN_START))s total)"
    HISTORY_RESULT="PASS"
else
    RESULT_LINE="RESULT: FAIL at '$FAILED_STEP' ($((RUN_END - RUN_START))s total)"
    HISTORY_RESULT="FAIL"
    [ "$ANY_TIMEOUT" -eq 1 ] && HISTORY_RESULT="TIMEOUT"
    ERRS="$(grep -E '^e: |error:|FAILED|What went wrong|Exception|Execution failed|> Task .* FAILED|timed out after' "$LOG" | tail -n 40)"
    [ -n "$ERRS" ] || ERRS="$(tail -n 40 "$LOG")"
fi

# The summary goes to the terminal and the log.
{
    echo
    echo "===== verify $TIER summary ====="
    printf '%s' "$SUMMARY"
    echo "$RESULT_LINE"
    echo "Log: $LOG"
} | tee -a "$LOG"
write_history "$HISTORY_RESULT"

if [ "$OVERALL" -eq 0 ]; then
    exit 0
fi
echo "----- last relevant error lines -----"
echo "$ERRS"
exit 1
