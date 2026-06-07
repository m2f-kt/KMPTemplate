#!/usr/bin/env bash
#
# build-ios-release.sh — link the optimized RELEASE iOS Compose framework with enough heap.
#
# The release iOS Kotlin/Native link of a full Compose app is memory-hungry and OOMs at the
# committed 6G daemon heap. This wrapper overrides the Kotlin daemon heap just for this run so
# the release framework links cleanly, WITHOUT raising the committed default (which CI/dev keep
# lean). After this succeeds the framework is ready for an Xcode archive (see docs/ios-release.md).
#
# Usage:  bash scripts/build-ios-release.sh [arch]      # arch: Arm64 (default) | X64 | SimulatorArm64
#         HEAP=14g bash scripts/build-ios-release.sh    # override heap if 12g still OOMs
#
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

ARCH="${1:-Arm64}"
HEAP="${HEAP:-12g}"
TASK=":composeApp:linkReleaseFrameworkIos${ARCH}"

echo "[ios-release] linking $TASK with Kotlin daemon heap -Xmx${HEAP}"
# -Pkotlin.daemon.jvmargs overrides the committed gradle.properties value for THIS invocation only.
./gradlew "$TASK" \
  -Pkotlin.daemon.jvmargs="-Xmx${HEAP}" \
  --no-configuration-cache

echo "[ios-release] done. Framework: composeApp/build/bin/ios${ARCH}/releaseFramework/"
echo "[ios-release] Next: archive + sign + notarize in Xcode (iosApp/) — see docs/ios-release.md"
