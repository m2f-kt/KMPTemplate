---
phase: 01-foundation-module-structure
plan: 01
subsystem: infra
tags: [gradle, kotlin, arrow, kermit, log4j, koin, compose-multiplatform, buildSrc]

requires:
  - phase: none
    provides: first plan in first phase
provides:
  - Version catalog with Compose 1.9.3, Exposed 1.0.0, Arrow 2.2.0
  - Kermit, Log4j, and Koin multiplatform entries in catalog
  - Arrow BOM for transitive dependency alignment
  - server-module-convention and kmp-library-convention buildSrc plugins
  - Eliminated duplicated allprojects build logic
affects: [01-02, 01-03, 01-04, all-future-modules]

tech-stack:
  added: [kermit-2.0.5, log4j-2.24.3, arrow-bom]
  patterns: [buildSrc-convention-plugins, version-catalog-boms]

key-files:
  created:
    - buildSrc/build.gradle.kts
    - buildSrc/settings.gradle.kts
    - buildSrc/src/main/kotlin/server-module-convention.gradle.kts
    - buildSrc/src/main/kotlin/kmp-library-convention.gradle.kts
  modified:
    - gradle/libs.versions.toml
    - server/build.gradle.kts
    - server/core/build.gradle.kts
    - server/core/config/build.gradle.kts
    - server/core/database/build.gradle.kts
    - server/core/security/build.gradle.kts
    - composeApp/build.gradle.kts
    - shared/build.gradle.kts

key-decisions:
  - "Used hardcoded plugin coordinates in buildSrc/build.gradle.kts (libs.plugins dynamic access unreliable in buildSrc classpath)"
  - "Removed logback from ktor-monitoring bundle; logging-server bundle uses Log4j exclusively"

patterns-established:
  - "server-module-convention: JVM modules apply this for Kotlin, serialization, kover, detekt, -Xcontext-parameters"
  - "kmp-library-convention: KMP modules apply this for multiplatform + serialization setup"
  - "All dependency versions centralized in libs.versions.toml with BOMs for transitive alignment"

duration: ~8min
completed: 2026-02-10
---

# Plan 01-01: Dependency Upgrades & Convention Plugins Summary

**Compose 1.9.3, Exposed 1.0.0, Arrow 2.2.0 with Kermit/Log4j/Koin catalog entries and 2 buildSrc convention plugins replacing duplicated build logic**

## Performance

- **Duration:** ~8 min
- **Tasks:** 2
- **Files modified:** 12

## Accomplishments
- Upgraded Compose Multiplatform to 1.9.3, Exposed to 1.0.0, Arrow to 2.2.0
- Added Kermit, Log4j, Koin multiplatform, and Arrow BOM to version catalog
- Created `server-module-convention` and `kmp-library-convention` buildSrc plugins
- Eliminated duplicated `allprojects` blocks in server/build.gradle.kts and server/core/build.gradle.kts
- All existing modules compile successfully with `./gradlew assemble`

## Task Commits

1. **Task 1: Upgrade version catalog and add missing dependencies** - `e8cf667` (feat)
2. **Task 2: Create buildSrc convention plugins and refactor module build files** - `227317d` (feat)

## Files Created/Modified
- `gradle/libs.versions.toml` - Version bumps, new library entries, new bundles
- `buildSrc/build.gradle.kts` - BuildSrc Gradle setup with plugin dependencies
- `buildSrc/settings.gradle.kts` - Version catalog forwarding to buildSrc
- `buildSrc/src/main/kotlin/server-module-convention.gradle.kts` - Shared server JVM build logic
- `buildSrc/src/main/kotlin/kmp-library-convention.gradle.kts` - Shared KMP library build logic
- `server/build.gradle.kts` - Applies convention plugin, removed allprojects
- `server/core/build.gradle.kts` - Removed allprojects block
- `server/core/config/build.gradle.kts` - Applies convention plugin
- `server/core/database/build.gradle.kts` - Applies convention plugin
- `server/core/security/build.gradle.kts` - Applies convention plugin

## Decisions Made
- Used hardcoded plugin coordinates in buildSrc (dynamic `libs.plugins` access unreliable)
- Removed logback from ktor-monitoring bundle; server logging moves to Log4j

## Deviations from Plan
None - plan executed as specified.

## Issues Encountered
- WASM yarn lock file needed updating after dependency changes (`kotlinWasmUpgradeYarnLock`)

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Convention plugins ready for new modules in Plan 02
- Version catalog has all entries needed for Plans 02-04

---
*Plan: 01-01-foundation-module-structure*
*Completed: 2026-02-10*
