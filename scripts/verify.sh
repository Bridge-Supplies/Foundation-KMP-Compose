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
    quick) ;;
    ui|e2e)
        echo "not wired yet (FND-08)"
        exit 2
        ;;
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
