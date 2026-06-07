#!/usr/bin/env bash
#
# dev-run.sh — bring up the backend on a free port, ready for the hot-reload desktop frontend.
#
# What it does:
#   1. Picks the backend port: the project's .env PORT (or 8080); if occupied, the next free port.
#   2. Writes the resolved backend URL to a port-file the JVM desktop app reads
#      (PlatformConfig.jvm.kt → defaultBaseUrl), so the hot-reload frontend connects to the
#      backend wherever it landed — no recompile, even if the port moved.
#   3. Starts `:server:run` (detached) on that port and waits until it answers HTTP.
#   4. Prints the chosen ports + how to launch/drive the frontend.
#
# The frontend (JVM Desktop, Hot Reload) is launched + driven via the `compose-hot-reload` MCP
# (configured in .mcp.json) — see the `launch-app` skill. Or run it directly:
#   ./gradlew :composeApp:hotRunJvm --mainClass=com.m2f.template.MainKt
#
# Usage:  bash scripts/dev-run.sh            # backend only (default)
#         bash scripts/dev-run.sh --web      # also start the wasmJs web frontend on a free port
#         bash scripts/dev-run.sh --stop     # stop a backend previously started by this script
#
set -uo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT" || exit 1

TMP="${TMPDIR:-/tmp}"
URL_FILE="$TMP/template-dev-server.url"
PID_FILE="$TMP/template-dev-server.pid"
SRV_LOG="$TMP/template-dev-server.log"
WEB_LOG="$TMP/template-dev-web.log"

port_free() { ! lsof -iTCP:"$1" -sTCP:LISTEN -t >/dev/null 2>&1; }
find_free() { local p="$1"; while ! port_free "$p"; do p=$((p + 1)); done; printf '%s' "$p"; }

if [ "${1:-}" = "--stop" ]; then
  if [ -f "$PID_FILE" ]; then
    PID="$(cat "$PID_FILE")"
    if kill -0 "$PID" 2>/dev/null; then kill "$PID" && echo "[dev-run] stopped backend pid $PID"; fi
    rm -f "$PID_FILE" "$URL_FILE"
  else
    echo "[dev-run] no recorded backend pid ($PID_FILE absent)"
  fi
  exit 0
fi

# --- backend port ---
BASE_PORT="$(grep -E '^PORT=' .env 2>/dev/null | head -1 | cut -d= -f2 | tr -d '[:space:]')"
BASE_PORT="${BASE_PORT:-8080}"
SERVER_PORT="$(find_free "$BASE_PORT")"
SERVER_URL="http://localhost:$SERVER_PORT"
printf '%s' "$SERVER_URL" > "$URL_FILE"

if [ "$SERVER_PORT" != "$BASE_PORT" ]; then
  echo "[dev-run] port $BASE_PORT busy → backend on $SERVER_PORT"
else
  echo "[dev-run] backend on $SERVER_PORT"
fi
echo "[dev-run] url-file → $URL_FILE  (desktop hot-reload app reads this)"

# --- start backend detached on the chosen port ---
nohup env PORT="$SERVER_PORT" ./gradlew -q :server:run > "$SRV_LOG" 2>&1 &
SRV_PID=$!
echo "$SRV_PID" > "$PID_FILE"
echo "[dev-run] backend pid $SRV_PID  log $SRV_LOG"

# --- wait for HTTP (any response = up) ---
UP=0
for i in $(seq 1 180); do
  if ! kill -0 "$SRV_PID" 2>/dev/null; then
    echo "[dev-run] ERROR: backend exited early — last log lines:"; tail -25 "$SRV_LOG"; rm -f "$PID_FILE"; exit 1
  fi
  code="$(curl -s -o /dev/null -w '%{http_code}' "$SERVER_URL/" 2>/dev/null)"; code="${code:-000}"
  if [ "$code" != "000" ]; then echo "[dev-run] backend up (HTTP $code) after ${i}s"; UP=1; break; fi
  sleep 1
done
[ "$UP" = 1 ] || { echo "[dev-run] WARN: backend not answering after 180s — see $SRV_LOG"; }

# --- optional web frontend ---
if [ "${1:-}" = "--web" ]; then
  WEB_PORT="$(find_free 8081)"
  echo "[dev-run] web frontend on $WEB_PORT (wasmJs dev server)"
  nohup ./gradlew -q ":composeApp:wasmJsBrowserDevelopmentRun" \
    -Pcompose.web.dev.port="$WEB_PORT" > "$WEB_LOG" 2>&1 &
  echo "[dev-run] web pid $!  log $WEB_LOG  → http://localhost:$WEB_PORT"
fi

cat <<EOF

[dev-run] READY
  backend : $SERVER_URL   (stop: bash scripts/dev-run.sh --stop)
  frontend: JVM Desktop with Hot Reload — drive via the 'compose-hot-reload' MCP (see the launch-app skill),
            or run directly: ./gradlew :composeApp:hotRunJvm --mainClass=com.m2f.template.MainKt
  The desktop app auto-reads $URL_FILE, so it targets this backend even on a non-default port.
EOF
