#!/usr/bin/env bash
# Release gate: fail the build if the packaged macOS dmg exceeds the binary-size
# cap (MB). Run AFTER `:composeApp:packageReleaseDistributionForCurrentOS`.
set -euo pipefail
MAX_MB="${1:-80}"
DMG=$(ls composeApp/build/compose/binaries/main-release/dmg/*.dmg 2>/dev/null | head -1)
if [ -z "$DMG" ]; then
  echo "ERROR: no dmg found — run :composeApp:packageReleaseDistributionForCurrentOS first" >&2
  exit 1
fi

# Cross-platform byte size: BSD/macOS uses `stat -f%z`, GNU/Linux uses `stat -c%s`.
if stat -f%z "$DMG" >/dev/null 2>&1; then
  BYTES=$(stat -f%z "$DMG")
else
  BYTES=$(stat -c%s "$DMG")
fi
SIZE_MB=$(( BYTES / 1024 / 1024 ))

echo "dmg=$DMG size=${SIZE_MB}MB max=${MAX_MB}MB"
if [ "$SIZE_MB" -gt "$MAX_MB" ]; then
  echo "FAIL: dmg exceeds ${MAX_MB} MB cap" >&2
  exit 2
fi
echo "PASS"
