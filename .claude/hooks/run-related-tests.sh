#!/bin/bash

# PostToolUse hook: Run related tests when a .kt file is edited
# Detects which Gradle module was affected and runs its tests

INPUT="${CLAUDE_TOOL_INPUT:-}"

# Check if the edited file is a Kotlin file
if echo "$INPUT" | grep -q '\.kt"'; then
  # Extract the file path from the JSON input
  FILE_PATH=$(echo "$INPUT" | grep -oP '"file_path"\s*:\s*"([^"]+)"' | sed 's/"file_path"\s*:\s*"//' | sed 's/"$//')

  if [ -z "$FILE_PATH" ]; then
    exit 0
  fi

  cd "$(git rev-parse --show-toplevel 2>/dev/null || echo '.')"

  # Convert file path to Gradle module path
  # e.g., app/auth/impl/src/... -> :app:auth:impl
  # e.g., server/auth/src/... -> :server:auth
  REL_PATH="${FILE_PATH#$(pwd)/}"

  # Find the nearest build.gradle.kts by walking up from the file
  DIR=$(dirname "$REL_PATH")
  MODULE_PATH=""
  while [ "$DIR" != "." ] && [ -n "$DIR" ]; do
    if [ -f "$DIR/build.gradle.kts" ]; then
      MODULE_PATH="$DIR"
      break
    fi
    DIR=$(dirname "$DIR")
  done

  if [ -z "$MODULE_PATH" ]; then
    exit 0
  fi

  # Convert path separators to Gradle module notation
  GRADLE_MODULE=":$(echo "$MODULE_PATH" | sed 's/\//:/g')"

  # Skip if it's a test file being edited (avoid recursive test runs)
  if echo "$REL_PATH" | grep -q '/test/'; then
    exit 0
  fi

  # Run tests for the affected module with daemon for speed
  OUTPUT=$(./gradlew "${GRADLE_MODULE}:test" --daemon 2>&1)
  EXIT_CODE=$?

  if [ $EXIT_CODE -ne 0 ]; then
    echo "$OUTPUT" | grep -E '(FAILED|> Task|failures|Exception)' | head -15
    echo ""
    echo "Tests failed in ${GRADLE_MODULE}. Run './gradlew ${GRADLE_MODULE}:test' for full output."
  fi
fi
