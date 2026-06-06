#!/usr/bin/env bash
# Manual gate: launch the packaged macOS app, sleep 60 s, then measure RSS.
# macOS-only (uses `ps -o rss=`). Long-running and machine-specific — run
# manually before merge.
#
# Usage: check-idle-rss.sh [MAX_MB] [APP_NAME]
#   MAX_MB    idle-RSS cap in MB (default 150)
#   APP_NAME  packaged .app/binary name (default "template")
set -euo pipefail
MAX_MB="${1:-150}"
APP_NAME="${2:-template}"
APP="composeApp/build/compose/binaries/main-release/app/${APP_NAME}.app/Contents/MacOS/${APP_NAME}"
if [ ! -x "$APP" ]; then
  echo "ERROR: app binary not found at $APP — package first" >&2
  exit 1
fi
"$APP" &
PID=$!
trap 'kill $PID 2>/dev/null || true' EXIT
sleep 60
RSS_KB=$(ps -o rss= -p "$PID")
RSS_MB=$(( RSS_KB / 1024 ))
echo "pid=$PID rss=${RSS_MB}MB max=${MAX_MB}MB after 60s"
if [ "$RSS_MB" -gt "$MAX_MB" ]; then
  echo "FAIL: idle RSS exceeds ${MAX_MB} MB cap" >&2
  exit 2
fi
echo "PASS"
