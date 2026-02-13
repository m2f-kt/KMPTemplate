---
status: diagnosed
trigger: "setup.sh CLI script doesn't rename all package references when customizing the template"
created: 2026-02-13T00:00:00Z
updated: 2026-02-13T00:00:00Z
---

## Current Focus

hypothesis: The script has a hardcoded list of app modules (auth, dashboard, designsystem) that is missing `profile`, plus a brittle architecture that will break whenever new modules are added.
test: Compare SOURCE_SETS list against actual filesystem directories
expecting: Gaps in coverage for newer modules
next_action: Return diagnosis

## Symptoms

expected: Running `bash setup.sh` renames ALL occurrences of old package names across all .kt, .kts, .xml files and moves source directories accordingly
actual: TerminalTypography and other references remain with old package names after running the script; source directories not moved for some modules
errors: Post-rename verification step shows remaining com.m2f references
reproduction: Run `bash setup.sh` with any new package name
started: Since app/profile module was added (commit abd0984)

## Eliminated

- hypothesis: The find commands in steps 1-2 miss app/profile .kt files
  evidence: Steps 1 and 2 use `find . -name "*.kt"` which is fully recursive and DOES find app/profile files. Verified with direct find+grep.
  timestamp: 2026-02-13

- hypothesis: sed replacement ordering causes partial matches
  evidence: The three packages (com.m2f.template, com.m2f.server, com.m2f.core) have distinct suffixes and do not overlap. Order is irrelevant.
  timestamp: 2026-02-13

- hypothesis: Files in buildSrc, .toml, .properties, .yaml, Dockerfile contain com.m2f references
  evidence: Grepped all these file types -- zero matches found outside .kt/.kts files.
  timestamp: 2026-02-13

## Evidence

- timestamp: 2026-02-13
  checked: Actual filesystem vs script SOURCE_SETS for app modules
  found: Script hardcodes `for mod in auth dashboard designsystem` (line 204) but `app/profile/` exists with 9 .kt files containing com.m2f references
  implication: Directory move (step 6) will NOT move app/profile source files. Files get text-replaced but left in old directory path.

- timestamp: 2026-02-13
  checked: app/profile/build.gradle.kts
  found: Contains `namespace = "com.m2f.template.app.profile"` (line 37). The .kts find in step 2 DOES cover this file for text replacement.
  implication: Text replacement works, but directory structure remains broken.

- timestamp: 2026-02-13
  checked: Step 1 (find .kt) and Step 2 (find .kts) coverage
  found: Both use recursive `find .` which covers ALL modules including app/profile. Text replacement IS applied to all 157 .kt files and 16 .kts files with com.m2f references.
  implication: The bug is NOT about text replacement -- it is specifically about directory moves in step 6.

- timestamp: 2026-02-13
  checked: Architecture of the SOURCE_SETS approach
  found: The script builds SOURCE_SETS by hardcoding module names in four separate loops. Any new module added to the project requires manually updating setup.sh.
  implication: Brittle design -- every new module is a potential regression.

## Resolution

root_cause: |
  TWO distinct issues found:

  **Issue 1 (Primary): `app/profile` module missing from hardcoded module list (line 204)**

  The script at line 204 hardcodes:
  ```bash
  for mod in auth dashboard designsystem; do
  ```
  But the project has FOUR app modules: auth, dashboard, designsystem, AND profile.

  This means step 6 ("Moving source directories") will never move
  `app/profile/src/commonMain/kotlin/com/m2f/template/` to the new package path.
  After the script runs, the files will have their package declarations and imports
  text-replaced (steps 1-2 DO work via recursive find), but the files will physically
  remain under the old `com/m2f/template/` directory tree. This causes a mismatch
  between the `package` declaration in the file and its filesystem location, which
  will break Kotlin compilation.

  **Issue 2 (Architectural): Brittle hardcoded module lists**

  The script maintains FIVE separate hardcoded module lists:
  - Line 184: composeApp variants (commonMain, androidMain, iosMain, jvmMain, wasmJsMain, commonTest)
  - Line 196: core modules (models, sdk, storage)
  - Line 204: app modules (auth, dashboard, designsystem) -- MISSING profile
  - Line 218: server feature modules (auth, ai)
  - Line 226: server core modules (config, database, security)

  Every time a new module is added to the project, setup.sh must be manually
  updated in the corresponding list. There is no discovery mechanism.

fix: NOT APPLIED (diagnosis only)
verification: NOT PERFORMED
files_changed: []
