#!/usr/bin/env node

// PreToolUse hook: Block edits to .env files containing secrets
const input = process.env.CLAUDE_TOOL_INPUT || "";

try {
  const parsed = JSON.parse(input);
  const filePath = parsed.file_path || parsed.path || "";

  if (/\/\.env$/.test(filePath) || /\/\.env\.local$/.test(filePath)) {
    process.stderr.write(
      "BLOCKED: .env files contain secrets and must not be edited by Claude. " +
      "Use .env.example as reference and edit .env manually.\n"
    );
    process.exit(2);
  }
} catch {
  // If input is not JSON, check raw string
  if (/\.env["'\s]/.test(input) && !/\.env\.example/.test(input)) {
    process.stderr.write(
      "BLOCKED: .env files contain secrets and must not be edited by Claude.\n"
    );
    process.exit(2);
  }
}
