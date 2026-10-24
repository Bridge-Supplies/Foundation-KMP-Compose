#!/bin/bash
# Builds consumer-test/ against this checkout as an included build: the same wiring apps use.
# Usage: scripts/consumer-test.sh [--no-ios]
set -euo pipefail
cd "$(dirname "$0")/.."
TASKS=(":shared:compileAndroidMain" ":shared:desktopTest")
if [ "${1:-}" != "--no-ios" ]; then
    TASKS+=(":shared:iosSimulatorArm64Test" ":shared:linkDebugFrameworkIosSimulatorArm64")
fi
./gradlew -p consumer-test "${TASKS[@]}" --no-daemon --console=plain
