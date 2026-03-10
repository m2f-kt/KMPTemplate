#!/bin/bash

# PostToolUse hook: Run detekt on Kotlin file edits
# Only triggers when a .kt file was edited

INPUT="${CLAUDE_TOOL_INPUT:-}"

# Check if the edited file is a Kotlin file
if echo "$INPUT" | grep -q '\.kt"'; then
  cd "$(git rev-parse --show-toplevel 2>/dev/null || echo '.')"

  # Run detekt with daemon for speed, only show summary
  OUTPUT=$(./gradlew detekt --daemon 2>&1)
  EXIT_CODE=$?

  if [ $EXIT_CODE -ne 0 ]; then
    # Show only the relevant error lines (file:line:col patterns and summary)
    echo "$OUTPUT" | grep -E '(\.kt:[0-9]+:[0-9]+|BUILD FAILED|detekt found)' | head -20
    echo ""
    echo "Detekt found issues. Run './gradlew detekt' for full report."
  fi
fi
