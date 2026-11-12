#!/usr/bin/env bash
# Verification entry point. Usage: scripts/verify.sh quick|ui|e2e [--only step[,step...] | --failed] [--full]
#   quick: formatting check, compile Android debug (+ desktop if present) + JVM/common unit tests,
#          plus iosSimulatorArm64Test when iOS-related files changed since the base. Warns (doesn't
#          fail) when iosApp/Configuration/Config.xcconfig's version differs from the catalog's.
#   ui: quick + UI tests + the composite-build consumer test.
#   e2e: ui + iosSimulatorArm64Test (always) + Maestro flows on Android and iOS
#        + over-install from a baseline APK (flows in .maestro/). After the shared build and test steps,
#        the Android lane and the iOS lane run at the same time (each with its own log, one
#        `maestro test` call per lane); over-install runs last in the Android lane, only when the
#        branch touches saved-data paths (E2E_SAVED_DATA_PATTERN), or with --full / VERIFY_OVER_INSTALL=1.
# Options:
#   --only a,b   run just these steps (names as the summary prints them, e.g. e2e-maps-android),
#                plus the builds and device boots they need. Format, unit tests and iOS tests are
#                skipped unless named.
#   --failed     rerun the steps that failed in the last run of this tier in this worktree.
#   --full       also run the over-install check.
# A partial run prints "RESULT: PASS (partial: N of M steps, ...)": it never counts as "verify passes".
# Env:
#   VERIFY_BASE                      git base for the iOS-change check. Default: the merge-base of
#                                    HEAD and origin/main (on a release branch, the whole release), else main.
#   VERIFY_TIMEOUT_GRADLE_MIN        limit per Gradle step and for the iOS app build (default 25).
#   VERIFY_TIMEOUT_MAESTRO_MIN       limit per Maestro flow (default 10).
#   VERIFY_TIMEOUT_OVER_INSTALL_MIN  limit for the over-install flow (default 15).
#   VERIFY_KEEP_EMULATOR=1           keep the Android emulator running when this script booted it.
#   VERIFY_OVER_INSTALL=1            run the over-install check (same as --full).
#   VERIFY_RELEASE_BRANCH            branch the over-install check diffs against. Default: the branch
#                                    checked out in the repo's main worktree.
#   VERIFY_MAESTRO_CALLS=flow        one `maestro test` per flow instead of one per lane.
# A step that runs past its limit is killed with its child processes and fails (a lane's single
# Maestro call gets the per-flow limit times its number of flows).
# Each run appends one line to ~/.cache/bridge-supplies/verify-history.tsv.
# Compatible with macOS bash 3.2 (which has no `timeout` command).

set -u

USAGE="Usage: scripts/verify.sh quick|ui|e2e [--only step[,step...] | --failed] [--full]"
TIER="${1:-}"
case "$TIER" in
    quick|ui|e2e) ;;
    *)
        echo "$USAGE" >&2
        exit 2
        ;;
esac
shift
ONLY=""
RERUN_FAILED=0
FULL=0
while [ $# -gt 0 ]; do
    case "$1" in
        --only)
            [ $# -ge 2 ] || { echo "$USAGE" >&2; exit 2; }
            ONLY="${ONLY:+$ONLY,}$2"
            shift 2
            ;;
        --only=*) ONLY="${ONLY:+$ONLY,}${1#--only=}"; shift ;;
        --failed) RERUN_FAILED=1; shift ;;
        --full) FULL=1; shift ;;
        *) echo "$USAGE" >&2; exit 2 ;;
    esac
done
if [ "$RERUN_FAILED" -eq 1 ] && [ -n "$ONLY" ]; then
    echo "--only and --failed can't be combined." >&2
    exit 2
fi

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
STEP_DEVICE_LOST=0
DEVICE_CHECK="" # a command that fails while the device is gone; run_limited stops the step then
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
    local minutes="$1" start rc lost=0 ticks=0
    shift
    STEP_TIMED_OUT=0
    STEP_DEVICE_LOST=0
    start="$(date +%s)"
    "$@" >> "$LOG" 2>&1 &
    STEP_PID=$!
    while is_alive "$STEP_PID"; do
        ticks=$((ticks + 1))
        if [ -n "$DEVICE_CHECK" ] && [ $((ticks % 10)) -eq 0 ]; then
            if $DEVICE_CHECK >/dev/null 2>&1; then
                lost=0
            else
                lost=$((lost + 1))
            fi
            if [ "$lost" -ge 3 ]; then
                STEP_DEVICE_LOST=1
                echo "!! the device stopped answering (adb); stopping PID $STEP_PID and its children" >> "$LOG"
                kill_tree "$STEP_PID"
                break
            fi
        fi
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
    [ "$STEP_DEVICE_LOST" -eq 1 ] && rc=125
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

LANE_PIDS=""
cleanup() {
    trap '' INT TERM # a second interrupt must not cut the cleanup short
    stop_current_step
    local lane
    for lane in $LANE_PIDS; do
        kill_tree "$lane"
    done
    LANE_PIDS=""
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

# --- E2E settings --------------------------------------------------------------
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
# Changed paths that put saved user data at risk (extended regex). The over-install check runs when the
# branch touches one of them (or with --full, or VERIFY_OVER_INSTALL=1).
E2E_SAVED_DATA_PATTERN='/(settings|storage)/|SettingKey|AndroidManifest\.xml$|^\.maestro/(seed|assert)-|^scripts/e2e/'

# --- Step selection ------------------------------------------------------------
# Every selectable step, in summary order: a --only/--failed run runs just these (plus the builds and
# device boots they need).

ALL_STEPS="format android-debug desktop-compile unit-tests ios-tests"
if [ "$TIER" != "quick" ]; then
    has_module foundation-ui && ALL_STEPS="$ALL_STEPS ui-tests"
    ALL_STEPS="$ALL_STEPS consumer-test"
fi
if [ "$TIER" = "e2e" ]; then
    for flow in $E2E_ANDROID_FLOWS; do ALL_STEPS="$ALL_STEPS e2e-$flow-android"; done
    for flow in $E2E_IOS_FLOWS; do ALL_STEPS="$ALL_STEPS e2e-$flow-ios"; done
    ALL_STEPS="$ALL_STEPS e2e-over-install"
fi

in_list() { # word list...
    local w="$1"
    shift
    case " $* " in
        *" $w "*) return 0 ;;
    esac
    return 1
}

# Map the failed step names of the last run (as the history file keeps them) to selectable steps.
failed_to_selection() { # names (comma-separated)
    local n out="" s
    for n in $(echo "$1" | tr ',' ' '); do
        if in_list "$n" $ALL_STEPS; then
            s="$n"
        else
            case "$n" in
                ios-link) s="ios-tests" ;;
                ios-app-build|ios-install|e2e-ios)
                    s=""
                    for flow in $E2E_IOS_FLOWS; do s="$s e2e-$flow-ios"; done
                    ;;
                android-emulator-boot|android-install|demo-apk-build)
                    s=""
                    for flow in $E2E_ANDROID_FLOWS; do s="$s e2e-$flow-android"; done
                    ;;
                emulator-lock)
                    s=""
                    for flow in $E2E_ANDROID_FLOWS; do s="$s e2e-$flow-android"; done
                    for flow in $E2E_IOS_FLOWS; do s="$s e2e-$flow-ios"; done
                    ;;
                *)
                    echo "Note: failed step '$n' of the last run has no matching step in this tier; ignoring it." >&2
                    s=""
                    ;;
            esac
        fi
        for s in $s; do
            in_list "$s" $out || out="$out $s"
        done
    done
    echo "$out"
}

PARTIAL=0
SELECTED=""
PREV_FAILED=""
if [ "$RERUN_FAILED" -eq 1 ]; then
    PREV_FAILED="$(awk -F'\t' -v r="$REPO_NAME" -v t="$TIER" '$2 == r && $5 == t { l = $7 } END { print l }' "$HISTORY_FILE" 2>/dev/null)"
    if [ -z "$PREV_FAILED" ]; then
        echo "No earlier $TIER run of $REPO_NAME in $HISTORY_FILE: nothing to rerun. Run scripts/verify.sh $TIER first." >&2
        exit 2
    fi
    if [ "$PREV_FAILED" = "-" ]; then
        echo "The last $TIER run of $REPO_NAME had no failed steps: nothing to rerun."
        exit 0
    fi
    ONLY="$(failed_to_selection "$PREV_FAILED" | tr ' ' ',')"
    ONLY="${ONLY#,}"
    if [ -z "$ONLY" ]; then
        echo "The failed steps of the last run ($PREV_FAILED) match no step of this tier: run scripts/verify.sh $TIER." >&2
        exit 2
    fi
    echo "Rerunning the failed steps of the last $TIER run: $ONLY"
fi
if [ -n "$ONLY" ]; then
    PARTIAL=1
    for s in $(echo "$ONLY" | tr ',' ' '); do
        # "computer-ios" is short for "e2e-computer-ios"
        in_list "$s" $ALL_STEPS || ! in_list "e2e-$s" $ALL_STEPS || s="e2e-$s"
        if ! in_list "$s" $ALL_STEPS; then
            echo "Unknown step '$s' for the $TIER tier. Steps: $ALL_STEPS" >&2
            exit 2
        fi
        in_list "$s" $SELECTED || SELECTED="$SELECTED $s"
    done
else
    SELECTED="$ALL_STEPS"
fi
if [ -z "$PREV_FAILED" ] && [ "$PARTIAL" -eq 1 ]; then
    # --only: the failures the history keeps for --failed include those of the last run this one doesn't cover
    PREV_FAILED="$(awk -F'\t' -v r="$REPO_NAME" -v t="$TIER" '$2 == r && $5 == t { l = $7 } END { print l }' "$HISTORY_FILE" 2>/dev/null)"
fi

selected() { # step
    in_list "$1" $SELECTED
}
count_words() { echo $#; }
N_SELECTED="$(count_words $SELECTED)"
N_ALL="$(count_words $ALL_STEPS)"

# Flows of one platform that are selected, in order.
selected_flows() { # android|ios
    local flow flows="" list="$E2E_ANDROID_FLOWS"
    [ "$1" = ios ] && list="$E2E_IOS_FLOWS"
    for flow in $list; do
        selected "e2e-$flow-$1" && flows="$flows $flow"
    done
    echo "${flows# }"
}

# --- Over-install: only when saved data may be affected ----------------------------

OI_REASON=""
# over_install_wanted: sets OI_REASON when it returns 1 (the reason it is skipped).
over_install_wanted() {
    local release main_co base files
    if [ "$PARTIAL" -eq 1 ]; then
        return 0 # named on purpose
    fi
    if [ "$FULL" -eq 1 ]; then return 0; fi
    if [ "${VERIFY_OVER_INSTALL:-0}" = "1" ]; then return 0; fi
    release="${VERIFY_RELEASE_BRANCH:-}"
    if [ -z "$release" ]; then
        main_co="$(git worktree list --porcelain 2>/dev/null | sed -n '1s/^worktree //p')"
        [ -n "$main_co" ] && release="$(git -C "$main_co" branch --show-current 2>/dev/null)"
    fi
    base=""
    [ -n "$release" ] && base="$(git merge-base HEAD "$release" 2>/dev/null)"
    if [ -z "$base" ]; then
        return 0 # can't tell what changed: run it
    fi
    files="$( { git diff --name-only "$base"...HEAD 2>/dev/null; git diff --name-only HEAD 2>/dev/null; git status --porcelain -uall 2>/dev/null | sed 's/^...//'; } | sort -u | grep -E "$E2E_SAVED_DATA_PATTERN" | head -n 3 | tr '\n' ' ')"
    if [ -n "$files" ]; then
        return 0
    fi
    OI_REASON="no saved-data changes since $release; --full runs it"
    return 1
}

# --- Run ---------------------------------------------------------------------

STAMP="$(date '+%Y%m%d-%H%M%S')"
mkdir -p "$REPO/build/verify"
LOG="$REPO/build/verify/$TIER-$STAMP.log"
LANE_DIR="$REPO/build/verify/lanes-$STAMP"
: > "$LOG"

echo "verify.sh $TIER in $REPO_NAME (log: build/verify/$TIER-$STAMP.log)"
if [ "$PARTIAL" -eq 1 ]; then
    echo "Partial run: $N_SELECTED of $N_ALL steps ($(echo $SELECTED | tr ' ' ','))"
fi
acquire_lock build || exit 1

SUMMARY=""
OVERALL=0
FAILED_STEP=""   # the first failure
FAILED_STEPS=""  # every failure, comma-separated
ANY_TIMEOUT=0
STEP_TIMES=""    # "step=seconds" pairs for the history file
TAG=""           # "[android] " in a lane's progress lines

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
    echo ">> $TAG$name: $*"
    echo "=== $name: $* ===" >> "$LOG"
    start="$(date +%s)"
    run_limited "$minutes" "$@"
    rc=$?
    secs=$(($(date +%s) - start))
    STEP_TIMES="$STEP_TIMES $(step_key "$name")=$secs"
    if [ "$STEP_TIMED_OUT" -eq 1 ]; then
        echo "!! $TAG$name timed out after $minutes min"
        record "$name (timed out after $minutes min)" FAIL "$secs"
        ANY_TIMEOUT=1
        mark_failed "$name"
    elif [ "$STEP_DEVICE_LOST" -eq 1 ]; then
        echo "!! $TAG$name: the device stopped answering (adb)"
        record "$name (device lost: adb stopped answering)" FAIL "$secs"
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

skip_step() { # name reason
    echo ">> $TAG$1: skipped ($2)"
    record "$1 ($2)" SKIP 0
}

# A step that can't run is a failure: a tier must never pass without having tested the app.
fail_step() { # name reason
    echo "Error: $TAG$1: $2" | tee -a "$LOG" >&2
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

# write_history <result>: append this run to the shared history file. The failed column holds this
# run's failures; a partial run also keeps the earlier failures of steps it didn't run, so --failed
# still reaches them.
write_history() {
    local branch commit failed="${FAILED_STEPS:-}" ran f
    HISTORY_WRITTEN=1
    branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)"
    commit="$(git rev-parse --short HEAD 2>/dev/null || echo unknown)"
    if ! git diff --quiet HEAD 2>/dev/null; then
        commit="$commit-dirty"
    fi
    if [ "$PARTIAL" -eq 1 ] && [ -n "$PREV_FAILED" ] && [ "$PREV_FAILED" != "-" ]; then
        ran=" $(echo "$STEP_TIMES" | sed 's/=[0-9]*//g') "
        for f in $(echo "$PREV_FAILED" | tr ',' ' '); do
            case "$ran" in
                *" $f "*) ;;
                *) in_list "$f" $(echo "$failed" | tr ',' ' ') || failed="${failed:+$failed,}$f" ;;
            esac
        done
    fi
    mkdir -p "$LOCK_ROOT"
    if [ ! -f "$HISTORY_FILE" ]; then
        printf 'time\trepo\tbranch\tcommit\ttier\tresult\tfailed\tseconds\tsteps\tscope\n' >> "$HISTORY_FILE"
    fi
    printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$(date '+%Y-%m-%dT%H:%M:%S%z')" "$REPO_NAME" "$branch" "$commit" \
        "$TIER" "$1" "${failed:--}" "$((${RUN_END:-$(date +%s)} - RUN_START))" "${STEP_TIMES# }" \
        "$([ "$PARTIAL" -eq 1 ] && echo partial || echo full)" >> "$HISTORY_FILE"
}

RUN_START="$(date +%s)"

# iOS version check (warn-only): Config.xcconfig must repeat the version catalog's app version.
check_ios_version() {
    local xcconfig="$REPO/iosApp/Configuration/Config.xcconfig" toml="$REPO/gradle/libs.versions.toml"
    local catalog_name catalog_code ios_name ios_code
    [ -f "$xcconfig" ] && [ -f "$toml" ] || return 0
    catalog_name="$(sed -n 's/^app-versionName *= *"\([^"]*\)".*/\1/p' "$toml" | head -n 1)"
    catalog_code="$(sed -n 's/^app-versionCode *= *"\([^"]*\)".*/\1/p' "$toml" | head -n 1)"
    ios_name="$(sed -n 's/^MARKETING_VERSION *= *\([^ ]*\).*/\1/p' "$xcconfig" | head -n 1)"
    ios_code="$(sed -n 's/^CURRENT_PROJECT_VERSION *= *\([^ ]*\).*/\1/p' "$xcconfig" | head -n 1)"
    [ -n "$ios_name$ios_code" ] || return 0 # the project doesn't keep its version in Config.xcconfig
    if [ "$ios_name" != "$catalog_name" ] || [ "$ios_code" != "$catalog_code" ]; then
        echo "Warning: iosApp/Configuration/Config.xcconfig has version $ios_name ($ios_code), but gradle/libs.versions.toml has $catalog_name ($catalog_code). Update MARKETING_VERSION and CURRENT_PROJECT_VERSION." | tee -a "$LOG"
        record "ios-version (Config.xcconfig differs from libs.versions.toml)" WARN 0
    fi
}
[ "$PARTIAL" -eq 1 ] || check_ios_version

# The e2e lanes need these before the shared steps, to know which builds to run.
ANDROID_FLOWS_SEL=""
IOS_FLOWS_SEL=""
OVER_INSTALL_RUN=0
ANDROID_LANE=0
IOS_LANE=0
if [ "$TIER" = "e2e" ]; then
    ANDROID_FLOWS_SEL="$(selected_flows android)"
    IOS_FLOWS_SEL="$(selected_flows ios)"
    if selected e2e-over-install && over_install_wanted; then
        OVER_INSTALL_RUN=1
    fi
    { [ -n "$ANDROID_FLOWS_SEL" ] || [ "$OVER_INSTALL_RUN" -eq 1 ]; } && ANDROID_LANE=1
    [ -n "$IOS_FLOWS_SEL" ] && IOS_LANE=1
fi

# Formatting first: it's the quickest check (./gradlew spotlessApply fixes it)
selected format && run_gradle_step "format" "spotlessCheck"

if selected android-debug || [ "$ANDROID_LANE" -eq 1 ]; then
    # (a partial run that needs the APK builds it too: an incremental build when nothing changed)
    run_gradle_step "android-debug" "$ANDROID_TASK"
fi
if [ "$OVERALL" -eq 0 ] && selected desktop-compile; then
    if [ -n "$DESKTOP_TASK" ]; then
        run_gradle_step "desktop-compile" "$DESKTOP_TASK"
    else
        skip_step "desktop-compile" "no desktop target"
    fi
fi
if [ "$OVERALL" -eq 0 ] && selected unit-tests; then
    # shellcheck disable=SC2086 # split the task list on purpose
    run_gradle_step "unit-tests" $TEST_TASKS
fi
if [ "$OVERALL" -eq 0 ] && selected ios-tests; then
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
    if selected ui-tests; then
        run_gradle_step "ui-tests" ":foundation-ui:desktopTest"
    fi
    if [ "$OVERALL" -eq 0 ] && selected consumer-test; then
        run_consumer_test_step
    fi
fi

# --- E2E tier ----------------------------------------------------------------
# After the shared steps above, the Android lane (emulator boot, install, flows, over-install) and the
# iOS lane (app build, install, flows) run at the same time, each with its own log. This process holds
# the one emulator lock for both; each device runs one Maestro session at a time.

# find_emulator_serial <seconds>: print the serial of the first emulator adb lists (never a physical
# phone), waiting up to <seconds> for one.
find_emulator_serial() {
    local n=0 serial
    while true; do
        serial="$("$ADB" devices 2>/dev/null | grep -Eo '^emulator-[0-9]+[[:space:]]+device' | head -n 1 | cut -d' ' -f1 | cut -f1)"
        if [ -n "$serial" ]; then
            echo "$serial"
            return 0
        fi
        [ "$n" -ge "$1" ] && return 1
        sleep 2
        n=$((n + 2))
    done
}

wait_for_android_boot() {
    "$ADB" wait-for-device
    while [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
        sleep 2
    done
    # Booted and steady: the package manager answers (a device that just reported boot_completed can still flap)
    while ! "$ADB" shell pm path android >/dev/null 2>&1; do
        sleep 2
    done
}

# wait_for_android_boot_steady: a running emulator answers adb (30 s at most).
wait_for_android_boot_steady() {
    local n=0
    while ! "$ADB" shell pm path android >/dev/null 2>&1; do
        n=$((n + 1))
        [ "$n" -ge 15 ] && return 1
        sleep 2
    done
    return 0
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

# junit_field <report> <flow> <status|time>: one flow's result from a Maestro JUnit report.
# Prints nothing when the report has no such flow.
junit_field() {
    awk -v flow="$2" -v want="$3" '
        function attr(s, name,   m) {
            if (match(s, " " name "=\"[^\"]*\"")) {
                m = substr(s, RSTART + length(name) + 3, RLENGTH - length(name) - 4)
                return m
            }
            return ""
        }
        /<testcase / {
            n = attr($0, "name"); id = attr($0, "id")
            if (n == flow || id == flow) {
                found = 1; t = attr($0, "time"); st = attr($0, "status")
                if ($0 ~ /\/>[ \t]*$/) { done = 1 } else { incase = 1 }
                if (st == "") st = "SUCCESS"
                next
            }
        }
        incase && /<(failure|error)/ { st = "FAILED" }
        incase && /<\/testcase>/ { incase = 0; done = 1 }
        END {
            if (!found) exit
            if (want == "time") { if (t == "") t = 0; printf "%d\n", t + 0.5 }
            else { if (st == "SUCCESS" || st == "PASSED") print "PASS"; else print "FAIL" }
        }' "$1" 2>/dev/null
}

# run_maestro_call <android|ios> <flow>...: one `maestro test` for these flows (in this order, a failed
# flow doesn't stop the rest), and a summary row per flow from its JUnit report.
run_maestro_call() {
    local plat="$1" appid junit names n start secs rc flow status ftime udid=""
    shift
    names="$*"
    n=$#
    if [ "$plat" = "android" ]; then
        appid="$E2E_ANDROID_APP_ID"
        "$ADB" shell am force-stop "$appid" >> "$LOG" 2>&1 || true
    else
        appid="$E2E_IOS_APP_ID"
        xcrun simctl terminate "$BOOTED_SIM" "$appid" >> "$LOG" 2>&1 || true
    fi
    junit="$LANE_DIR/$plat-${names%% *}.xml"
    rm -f "$junit"
    for flow in "$@"; do
        set -- "$@" "$REPO/.maestro/$flow.yaml"
    done
    shift "$n"
    [ "$plat" = "android" ] && [ -n "${ANDROID_SERIAL:-}" ] && udid="$ANDROID_SERIAL"
    echo ">> ${TAG}maestro $plat: $names"
    echo "=== maestro $plat: $names ===" >> "$LOG"
    start="$(date +%s)"
    run_limited $((MAESTRO_LIMIT_MIN * n)) "$MAESTRO" --platform "$plat" test ${udid:+--udid "$udid"} -e APP_ID="$appid" --format junit --output "$junit" "$@"
    rc=$?
    secs=$(($(date +%s) - start))
    for flow in $names; do
        if [ "$STEP_TIMED_OUT" -eq 1 ]; then
            status="TIMEOUT"
            ftime="$secs"
        elif [ "$STEP_DEVICE_LOST" -eq 1 ]; then
            status="DEVICELOST"
            ftime="$secs"
        else
            status="$(junit_field "$junit" "$flow" status)"
            ftime="$(junit_field "$junit" "$flow" time)"
            [ -n "$status" ] || { status="FAIL"; ftime=0; echo "No result for $flow in the Maestro report (exit $rc)" >> "$LOG"; }
        fi
        STEP_TIMES="$STEP_TIMES e2e-$flow-$plat=${ftime:-0}"
        case "$status" in
            PASS) record "e2e-$flow-$plat" PASS "${ftime:-0}" ;;
            DEVICELOST)
                echo "!! ${TAG}e2e-$flow-$plat: the device stopped answering (adb)"
                record "e2e-$flow-$plat (device lost: adb stopped answering)" FAIL "$ftime"
                mark_failed "e2e-$flow-$plat"
                ;;
            TIMEOUT)
                echo "!! ${TAG}e2e-$flow-$plat: the Maestro call timed out after $((MAESTRO_LIMIT_MIN * n)) min"
                record "e2e-$flow-$plat (timed out after $((MAESTRO_LIMIT_MIN * n)) min)" FAIL "$ftime"
                ANY_TIMEOUT=1
                mark_failed "e2e-$flow-$plat"
                ;;
            *)
                record "e2e-$flow-$plat" FAIL "${ftime:-0}"
                mark_failed "e2e-$flow-$plat"
                ;;
        esac
    done
}

# run_maestro_flows <android|ios> <flow>...: the lane's flows in one Maestro call, or with
# VERIFY_MAESTRO_CALLS=flow one call per flow.
run_maestro_flows() {
    local plat="$1" flow
    shift
    if [ "${VERIFY_MAESTRO_CALLS:-lane}" = "flow" ]; then
        for flow in "$@"; do
            run_maestro_call "$plat" "$flow"
        done
    else
        run_maestro_call "$plat" "$@"
    fi
}

run_over_install_step() {
    local b BASELINES_DIR="" BASELINE_APK=""
    for b in ${E2E_BASELINES_DIR:+"$E2E_BASELINES_DIR"} "$REPO/baselines/$E2E_BASELINE_NAME" "$REPO/../baselines/$E2E_BASELINE_NAME" "$REPO/../../baselines/$E2E_BASELINE_NAME"; do
        if [ -d "$b" ]; then
            BASELINES_DIR="$b"
            break
        fi
    done
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
}

# The Android lane: runs in its own process. Over-install comes last in the lane, so it never overlaps
# the Android flows; its row is kept apart so the summary can list it last.
android_lane() { # flows over-install(0|1)
    local flows="$1" over_install="$2" SUMMARY_FLOWS
    DEVICE_CHECK="$ADB shell true" # fail clearly, not hang, if the emulator drops mid-lane
    DEMO_APK=""
    if [ -f "$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk" ]; then
        DEMO_APK="$REPO/androidApp/build/outputs/apk/debug/androidApp-debug.apk"
    elif [ -f "$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
        DEMO_APK="$REPO/composeApp/build/outputs/apk/debug/composeApp-debug.apk"
    fi
    if [ -z "$DEMO_APK" ] || [ ! -f "$DEMO_APK" ]; then
        fail_step "android-install" "no debug APK to install"
        return 0
    fi
    if ! "$ADB" install -r "$DEMO_APK" >> "$LOG" 2>&1; then
        fail_step "android-install" "could not install $DEMO_APK"
        return 0
    fi
    if [ -n "$flows" ]; then
        # shellcheck disable=SC2086 # split the flow list on purpose
        run_maestro_flows android $flows
    fi
    SUMMARY_FLOWS="$SUMMARY"
    SUMMARY=""
    if [ "$over_install" -eq 1 ]; then
        run_over_install_step
    fi
    printf '%s' "$SUMMARY" > "$LANE_DIR/android.over-install"
    SUMMARY="$SUMMARY_FLOWS"
}

ios_lane() { # flows
    local flows="$1" IOS_APP_PATH
    if ! xcrun simctl list devices available 2>/dev/null | grep -q "iPhone"; then
        if [ "$E2E_STRICT" -eq 1 ]; then
            fail_step "e2e-ios" "no iPhone simulator available"
        else
            skip_step "e2e-ios" "no iOS simulator runtime"
        fi
        return 0
    fi
    IOS_APP_PATH="$REPO/build/ios-derived/Build/Products/Debug-iphonesimulator/$E2E_IOS_APP_NAME"
    # Always rebuild (incremental) so the flows never run against a stale app.
    # Simulator builds are arm64-only: the KMP framework has no x86_64 simulator slice.
    run_step "ios-app-build" "$GRADLE_LIMIT_MIN" xcodebuild -project "$REPO/iosApp/iosApp.xcodeproj" -scheme iOS \
        -destination "generic/platform=iOS Simulator" \
        -derivedDataPath "$REPO/build/ios-derived" \
        ARCHS=arm64 CODE_SIGNING_ALLOWED=NO build || return 0
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
        # shellcheck disable=SC2086 # split the flow list on purpose
        run_maestro_flows ios $flows
    fi
}

# launch_lane <name> <function> <args...>: run a lane in the background with its own log. Its summary
# rows, step times and failures go to files in $LANE_DIR for the main process to merge.
launch_lane() {
    local name="$1"
    shift
    (
        trap - EXIT INT TERM
        LOG="$REPO/build/verify/$TIER-$STAMP-$name.log"
        : > "$LOG"
        TAG="[$name] "
        SUMMARY=""
        STEP_TIMES=""
        FAILED_STEPS=""
        FAILED_STEP=""
        OVERALL=0
        ANY_TIMEOUT=0
        lane_start="$(date +%s)"
        "$@"
        printf '%s' "$SUMMARY" > "$LANE_DIR/$name.summary"
        printf '%s' "${STEP_TIMES# }" > "$LANE_DIR/$name.times"
        printf '%s' "$FAILED_STEPS" > "$LANE_DIR/$name.failed"
        printf '%s' "$FAILED_STEP" > "$LANE_DIR/$name.first"
        printf '%s %s %s\n' "$OVERALL" "$ANY_TIMEOUT" "$(($(date +%s) - lane_start))" > "$LANE_DIR/$name.status"
    ) &
    LANE_PIDS="$LANE_PIDS $!"
}

LANE_LINE=""
append_rows() { # file of summary rows (each ends with a newline)
    [ -s "$1" ] && SUMMARY="$SUMMARY$(cat "$1")
"
}
merge_lane() { # name
    local name="$1" st overall timeout secs f
    [ -f "$LANE_DIR/$name.status" ] || {
        # the lane process died without reporting
        SUMMARY="$SUMMARY$(printf '  %-6s %-40s %ss' FAIL "$name-lane (no result)" 0)
"
        mark_failed "$name-lane"
        return 0
    }
    read -r overall timeout secs < "$LANE_DIR/$name.status"
    append_rows "$LANE_DIR/$name.summary"
    STEP_TIMES="$STEP_TIMES $(cat "$LANE_DIR/$name.times")"
    f="$(cat "$LANE_DIR/$name.failed")"
    if [ "$overall" -ne 0 ]; then
        OVERALL=1
        [ -z "$FAILED_STEP" ] && FAILED_STEP="$(cat "$LANE_DIR/$name.first")"
        [ -n "$f" ] && FAILED_STEPS="${FAILED_STEPS:+$FAILED_STEPS,}$f"
    fi
    [ "$timeout" -ne 0 ] && ANY_TIMEOUT=1
    LANE_LINE="$LANE_LINE ${name} ${secs}s,"
}

if [ "$OVERALL" -eq 0 ] && [ "$TIER" = "e2e" ] && { [ "$ANDROID_LANE" -eq 1 ] || [ "$IOS_LANE" -eq 1 ]; }; then
    mkdir -p "$LANE_DIR"
    if ! acquire_emulator_lock; then
        fail_step "emulator-lock" "timed out waiting for the emulator lock"
    fi
fi

if [ "$OVERALL" -eq 0 ] && [ "$TIER" = "e2e" ] && { [ "$ANDROID_LANE" -eq 1 ] || [ "$IOS_LANE" -eq 1 ]; }; then
    ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
    if [ ! -x "$ADB" ] && command -v adb >/dev/null 2>&1; then
        ADB="$(command -v adb)"
    fi
    export MAESTRO_CLI_NO_ANALYTICS=1 # opt out of Maestro's anonymous analytics
    MAESTRO="${MAESTRO:-maestro}"
    if ! command -v "$MAESTRO" >/dev/null 2>&1; then
        if [ -x "$HOME/.maestro/bin/maestro" ]; then
            MAESTRO="$HOME/.maestro/bin/maestro"
        fi
    fi

    # Start the Android emulator here (not in the lane), so it is shut down with this process.
    if [ "$ANDROID_LANE" -eq 1 ] && ! "$ADB" devices 2>/dev/null | grep -E "emulator-[0-9]+" | grep -q "device"; then
        echo ">> Booting Android emulator medium_phone..."
        EMU_BIN="$HOME/Library/Android/sdk/emulator/emulator"
        if [ ! -x "$EMU_BIN" ] && command -v emulator >/dev/null 2>&1; then
            EMU_BIN="$(command -v emulator)"
        fi
        "$EMU_BIN" -avd medium_phone -netdelay none -netspeed full >/dev/null 2>&1 &
        EMULATOR_PID=$!
        EMULATOR_STARTED=1
    fi
    # Every adb and Maestro call targets one emulator: ANDROID_SERIAL if set, else the emulator that was
    # running or this run booted. A physical phone attached next to it is never used.
    if [ "$ANDROID_LANE" -eq 1 ] && [ -z "${ANDROID_SERIAL:-}" ]; then
        if ANDROID_SERIAL="$(find_emulator_serial $((EMULATOR_BOOT_LIMIT_MIN * 60)))"; then
            echo ">> Using $ANDROID_SERIAL for adb and Maestro."
        else
            ANDROID_SERIAL=""
            fail_step "android-emulator-boot" "no emulator showed up in adb"
            ANDROID_LANE=0
        fi
    fi
    export ANDROID_SERIAL
    # The emulator is booted and steady before the lanes split.
    if [ "$ANDROID_LANE" -eq 1 ]; then
        if [ "$EMULATOR_STARTED" -eq 1 ]; then
            if run_step "android-emulator-boot" "$EMULATOR_BOOT_LIMIT_MIN" wait_for_android_boot; then
                echo ">> Android emulator ready."
            else
                ANDROID_LANE=0
            fi
        elif ! wait_for_android_boot_steady; then
            fail_step "android-device" "the running emulator doesn't answer adb"
            ANDROID_LANE=0
        fi
    fi

    LANES_START="$(date +%s)"
    [ "$ANDROID_LANE" -eq 1 ] && launch_lane android android_lane "$ANDROID_FLOWS_SEL" "$OVER_INSTALL_RUN"
    [ "$IOS_LANE" -eq 1 ] && launch_lane ios ios_lane "$IOS_FLOWS_SEL"
    for pid in $LANE_PIDS; do
        wait "$pid" 2>/dev/null
    done
    LANE_PIDS=""
    [ "$ANDROID_LANE" -eq 1 ] && merge_lane android
    [ "$IOS_LANE" -eq 1 ] && merge_lane ios
    if [ -f "$LANE_DIR/android.over-install" ]; then
        append_rows "$LANE_DIR/android.over-install"
    fi
fi

# Over-install skipped on purpose: say why (a full run lists every step).
if [ "$TIER" = "e2e" ] && [ "$PARTIAL" -eq 0 ] && [ "$OVER_INSTALL_RUN" -eq 0 ]; then
    skip_step "e2e-over-install" "$OI_REASON"
fi

if [ "$TIER" = "e2e" ]; then
    shutdown_emulator_if_ours
    release_emulator_lock
fi

# --- Summary -----------------------------------------------------------------

RUN_END="$(date +%s)"
TOTAL_NOTE="$((RUN_END - RUN_START))s total"
[ "$PARTIAL" -eq 1 ] && TOTAL_NOTE="partial: $N_SELECTED of $N_ALL steps, $TOTAL_NOTE"
if [ "$OVERALL" -eq 0 ]; then
    RESULT_LINE="RESULT: PASS ($TOTAL_NOTE)"
    HISTORY_RESULT="PASS"
else
    RESULT_LINE="RESULT: FAIL at '$FAILED_STEP' ($TOTAL_NOTE)"
    HISTORY_RESULT="FAIL"
    [ "$ANY_TIMEOUT" -eq 1 ] && HISTORY_RESULT="TIMEOUT"
    ERRS="$(cat "$LOG" "$REPO"/build/verify/"$TIER-$STAMP"-*.log 2>/dev/null | grep -E '^e: |error:|FAILED|What went wrong|Exception|Execution failed|> Task .* FAILED|timed out after' | tail -n 40)"
    [ -n "$ERRS" ] || ERRS="$(tail -n 40 "$LOG")"
fi

# The summary goes to the terminal and the log.
{
    echo
    echo "===== verify $TIER summary ====="
    printf '%s' "$SUMMARY"
    if [ -n "$LANE_LINE" ]; then
        echo "Lanes (parallel, wall-clock):${LANE_LINE%,}"
    fi
    echo "$RESULT_LINE"
    echo "Log: $LOG"
    for f in "$REPO"/build/verify/"$TIER-$STAMP"-*.log; do
        [ -f "$f" ] && echo "Lane log: $f"
    done
} | tee -a "$LOG"
write_history "$HISTORY_RESULT"

if [ "$OVERALL" -eq 0 ]; then
    exit 0
fi
echo "----- last relevant error lines -----"
echo "$ERRS"
exit 1
