#!/bin/bash

# PostToolUse hook: on every Kotlin edit, AUTO-FIX formatting and SURFACE the issues detekt can't
# auto-fix (complexity, naming, magic numbers, empty blocks, potential bugs, ...).
#
# Why the standalone CLI instead of Gradle:
#   - Detekt is applied ONLY to server modules (server-module-convention); the client/Compose/MVI
#     modules have no `:detekt` task, so `./gradlew detekt` can't lint most edits.
#   - A per-edit `./gradlew detekt` is far too slow (whole-build, daemon spin-up).
# The detekt CLI runs on JUST the edited file (~1-2s/pass) with configs DERIVED from
# build-config/detekt-config.yml so it always matches the project style:
#   pass 1: formatting ruleset + --auto-correct      -> fixes whitespace/indent/imports/commas
#   pass 2: the other built-in rulesets (NO formatting, NO Compose plugin) -> REPORTS manual fixes
# Server files apply their module detekt-baseline.xml so pre-existing tech-debt stays quiet.
#
# Limitations (by design): type-resolution rules (e.g. UnsafeCallOnNullableType / `!!`) need a
# compiled classpath and only run in the full Gradle detekt — they are NOT reported here. The
# Compose ruleset (a separate plugin) also stays on the Gradle + pre-commit gate.
#
# Non-blocking: always exits 0.

set -uo pipefail

DETEKT_VERSION="1.23.8"
REPORT_CAP=10

# --- resolve the edited file path (stdin JSON preferred; CLAUDE_TOOL_INPUT as fallback) ---
PAYLOAD="$(cat 2>/dev/null)"
[ -z "$PAYLOAD" ] && PAYLOAD="${CLAUDE_TOOL_INPUT:-}"
FILE_PATH="$(printf '%s' "$PAYLOAD" \
  | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)"

case "$FILE_PATH" in
  *.kt | *.kts) ;;
  *) exit 0 ;;
esac
[ -f "$FILE_PATH" ] || exit 0

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT" || exit 0

SRC_CONFIG="build-config/detekt-config.yml"
[ -f "$SRC_CONFIG" ] || exit 0

CACHE="$ROOT/.claude/hooks/.detekt"   # git-ignored; jars fetched once
CLI="$CACHE/detekt-cli.jar"
FMT_PLUGIN="$CACHE/detekt-formatting.jar"
FMT_CFG="$CACHE/detekt-format.yml"        # formatting ruleset only (auto-fix)
REPORT_CFG="$CACHE/detekt-nonformat.yml"  # everything except formatting + Compose (report)
BASE="https://github.com/detekt/detekt/releases/download/v${DETEKT_VERSION}"

command -v java >/dev/null 2>&1 || exit 0
mkdir -p "$CACHE"

# One-time fetch of the detekt CLI + formatting plugin (matched to the project's detekt version).
[ -f "$CLI" ] || curl -fsSL -o "$CLI" "$BASE/detekt-cli-${DETEKT_VERSION}-all.jar" || exit 0
[ -f "$FMT_PLUGIN" ] || curl -fsSL -o "$FMT_PLUGIN" "$BASE/detekt-formatting-${DETEKT_VERSION}.jar" || exit 0

# (Re)derive both configs from the canonical detekt config whenever it changes.
if [ ! -f "$FMT_CFG" ] || [ "$SRC_CONFIG" -nt "$FMT_CFG" ]; then
  {
    printf 'config:\n  validation: false\n\n'
    awk '/^[A-Za-z][A-Za-z0-9_-]*:[[:space:]]*$/ { inblk = ($0 ~ /^formatting:/) } inblk' "$SRC_CONFIG"
  } > "$FMT_CFG"
fi
if [ ! -f "$REPORT_CFG" ] || [ "$SRC_CONFIG" -nt "$REPORT_CFG" ]; then
  awk '/^[A-Za-z][A-Za-z0-9_-]*:[[:space:]]*$/ { skip = ($0 ~ /^(formatting|Compose):/) } !skip' \
    "$SRC_CONFIG" > "$REPORT_CFG"
fi

# --- pass 1: auto-fix formatting ---
FMT_OUT="$(java -jar "$CLI" --config "$FMT_CFG" --plugins "$FMT_PLUGIN" --auto-correct --input "$FILE_PATH" 2>&1)"
if printf '%s' "$FMT_OUT" | grep -q "was modified"; then
  echo "detekt: auto-formatted $(basename "$FILE_PATH")"
fi

# --- pass 2: report the non-auto-correctable issues (apply the module baseline if present) ---
BASELINE_ARG=()
DIR="$(dirname "$FILE_PATH")"
while [ "$DIR" != "/" ] && [ "$DIR" != "." ]; do
  if [ -f "$DIR/detekt-baseline.xml" ]; then
    BASELINE_ARG=(--baseline "$DIR/detekt-baseline.xml")
    break
  fi
  [ "$DIR" = "$ROOT" ] && break
  DIR="$(dirname "$DIR")"
done

REPORT_OUT="$(java -jar "$CLI" --config "$REPORT_CFG" "${BASELINE_ARG[@]}" --input "$FILE_PATH" 2>&1)"
# Drop rules the syntactic CLI false-flags on this codebase. UnusedImports fires on imports used only
# in `context(...)` receivers (detekt 1.23.8 predates context parameters) — pervasive in server code;
# the authoritative Gradle detekt handles them correctly, so they'd just be noise here.
FINDINGS="$(printf '%s' "$REPORT_OUT" | grep -E '\.kt[s]?:[0-9]+:[0-9]+:' | grep -vF '[UnusedImports]' | sed 's#.*/##')"
COUNT="$(printf '%s' "$FINDINGS" | grep -c . || true)"

if [ "${COUNT:-0}" -gt 0 ]; then
  echo "detekt (best-effort — Gradle detekt is authoritative): $COUNT manual-fix issue(s) in $(basename "$FILE_PATH"):"
  printf '%s\n' "$FINDINGS" | head -n "$REPORT_CAP" | sed 's/^/  /'
  if [ "$COUNT" -gt "$REPORT_CAP" ]; then
    echo "  … (+$((COUNT - REPORT_CAP)) more)"
  fi
fi
exit 0
