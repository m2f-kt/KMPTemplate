#!/usr/bin/env bash
# Dev-only watch-restart loop for the Ktor backend.
#
# WHY a restart loop (not true hot reload): the server uses embeddedServer + SuspendApp
# with a context-receiver `module()` passed as a lambda (server/.../Application.kt). Ktor's
# in-process auto-reload only works with EngineMain/application.conf (or a function-reference
# module), so the only reliable "auto-update on change" here is: recompile + restart the JVM.
# Expect a few seconds per cycle, and in-memory state is lost on each restart.
#
# No external watcher (entr/fswatch) is required — this polls source mtimes every POLL_SECONDS.
set -uo pipefail
cd "$(dirname "$0")/.."

APP_MAIN="com.m2f.template.ApplicationKt"
WATCH_DIRS=(server shared core)
EXTRA_FILES=(.env gradle/libs.versions.toml)
POLL_SECONDS=2
GRADLE_PID=""

# Cross-platform mtime args: BSD/macOS uses `stat -f '%m'`, GNU/Linux uses `stat -c '%Y'`.
# `xargs` invokes an external command, so resolve the right stat flags up front.
if stat -f '%m' . >/dev/null 2>&1; then
  STAT_ARGS=(-f '%m')   # BSD/macOS
else
  STAT_ARGS=(-c '%Y')   # GNU/Linux
fi

# Newest modification time across all backend build inputs (awk avoids SIGPIPE).
signature() {
  {
    find "${WATCH_DIRS[@]}" \( -name '*.kt' -o -name '*.kts' \) -not -path '*/build/*' -print0 2>/dev/null
    printf '%s\0' "${EXTRA_FILES[@]}"
  } | xargs -0 stat "${STAT_ARGS[@]}" 2>/dev/null | awk 'BEGIN{m=0}{if($1>m)m=$1}END{print m}'
}

stop_server() {
  pkill -f "${APP_MAIN}" 2>/dev/null || true
  [ -n "${GRADLE_PID}" ] && kill "${GRADLE_PID}" 2>/dev/null || true
  GRADLE_PID=""
}

start_server() {
  echo "[server-watch] starting :server:run ..."
  ./gradlew :server:run &
  GRADLE_PID=$!
}

trap 'echo "[server-watch] shutting down"; stop_server; exit 0' INT TERM

# Take over from any server already bound to the port (e.g. a previously-started instance).
pkill -f "${APP_MAIN}" 2>/dev/null || true
last="$(signature)"
start_server

while true; do
  sleep "${POLL_SECONDS}"
  cur="$(signature)"
  if [ "${cur}" != "${last}" ]; then
    echo "[server-watch] change detected — recompiling + restarting"
    last="${cur}"
    stop_server
    sleep 1
    start_server
  fi
done
