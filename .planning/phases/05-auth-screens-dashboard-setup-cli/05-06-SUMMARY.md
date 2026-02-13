---
phase: 05-auth-screens-dashboard-setup-cli
plan: 06
subsystem: infra
tags: [bash, cli, setup, template, sed, package-rename]

# Dependency graph
requires:
  - phase: 04-navigation-ui-components
    provides: "Final project structure with all modules and packages established"
provides:
  - "Interactive setup.sh CLI for template project customization"
  - "Automated package rename (com.m2f.template, com.m2f.server, com.m2f.core)"
  - "Source directory relocation to match new package paths"
  - "Database name configuration in docker-compose and DataSource.kt"
affects: []

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Platform-aware sed function (macOS sed -i '' vs Linux sed -i)"
    - "move_package_dir helper for safe directory relocation with cleanup"

key-files:
  created:
    - "setup.sh"
  modified: []

key-decisions:
  - "sed_inplace function wrapper instead of variable expansion for reliable quoting"
  - "Three distinct package mappings: com.m2f.template->new, com.m2f.server->first_two.server, com.m2f.core->first_two.core"
  - "Database default is 'application' (matching actual DataSource.kt), not 'template' as plan assumed"
  - "cp -R + rm -rf instead of mv for cross-platform directory move reliability"
  - "Delete .iml files rather than updating (IDE regenerates them)"

patterns-established:
  - "Interactive CLI pattern: prompt -> validate -> preview -> confirm -> execute -> verify"

# Metrics
duration: 2min
completed: 2026-02-13
---

# Phase 5 Plan 6: Setup CLI Summary

**Bash setup script with interactive prompts, input validation, three-package rename, directory relocation, and post-rename verification**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-13T15:35:41Z
- **Completed:** 2026-02-13T15:37:25Z
- **Tasks:** 1
- **Files modified:** 1

## Accomplishments
- Created setup.sh that interactively renames the entire template project after cloning
- Handles all three package namespaces: com.m2f.template, com.m2f.server, com.m2f.core
- Moves source directories across 30+ source sets to match the new package path
- Input validation, dry-run preview, confirmation, and post-rename verification

## Task Commits

Each task was committed atomically:

1. **Task 1: Create the setup CLI script** - `fd0b965` (feat)

**Plan metadata:** pending (docs: complete plan)

## Files Created/Modified
- `setup.sh` - Interactive project setup CLI script (335 lines); prompts for project name, package name, database name; validates inputs; renames packages in .kt/.kts/.xml files; moves source directories; updates docker-compose and DataSource.kt; verifies no old references remain

## Decisions Made
- Used a `sed_inplace` function wrapper instead of storing `sed -i ''` in a variable -- function approach avoids word-splitting issues with the empty-string argument on macOS
- Mapped three distinct old packages to three new packages: `com.m2f.template` maps to the user's full package, while `com.m2f.server` and `com.m2f.core` map to `{first_two_segments}.server` and `{first_two_segments}.core` respectively
- Corrected the plan's assumption that the database name is "template" -- the actual default in DataSource.kt and docker-compose.yml is "application"
- Used `cp -R` + `rm -rf` instead of `mv` for directory moves to handle nested subdirectory structures reliably across platforms
- Delete `.iml` files rather than attempting to update them, since the IDE regenerates them on project import

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Corrected database name from "template" to "application"**
- **Found during:** Task 1 (Setup CLI script creation)
- **Issue:** Plan specified `sed -i '' "s/POSTGRES_DB: template/POSTGRES_DB: $DB_NAME/g"` but actual docker-compose.yml uses `POSTGRES_DB: application` and DataSource.kt uses `R2DBC_DATABASE = "application"`
- **Fix:** Changed OLD_DB_NAME to "application" and updated all sed patterns accordingly; also updates R2DBC URL and the pg_isready healthcheck command
- **Files modified:** setup.sh
- **Verification:** Confirmed by reading docker-compose.yml and DataSource.kt source
- **Committed in:** fd0b965 (Task 1 commit)

**2. [Rule 2 - Missing Critical] Added container_name rename in docker-compose.yml**
- **Found during:** Task 1 (Setup CLI script creation)
- **Issue:** Plan didn't mention updating `container_name: template-postgres` in docker-compose.yml
- **Fix:** Added sed command to rename container_name to `${PROJECT_NAME_LOWER}-postgres`
- **Files modified:** setup.sh
- **Verification:** grep confirms the sed_inplace command for container_name is present
- **Committed in:** fd0b965 (Task 1 commit)

---

**Total deviations:** 2 auto-fixed (1 bug, 1 missing critical)
**Impact on plan:** Both fixes necessary for correctness. The database name fix prevents the script from failing silently (no match found). The container_name fix prevents Docker naming conflicts when running multiple template-derived projects.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Setup CLI is complete and ready for use
- Script handles all current modules; if new modules are added in future phases, the SOURCE_SETS array in setup.sh may need updating

## Self-Check: PASSED

- FOUND: setup.sh
- FOUND: fd0b965 (Task 1 commit)
- FOUND: 05-06-SUMMARY.md

---
*Phase: 05-auth-screens-dashboard-setup-cli*
*Completed: 2026-02-13*
