---
phase: 10-mvi-viewmodel-foundation
plan: 01
subsystem: ui
tags: [mvi, viewmodel, arrow-either, stateflow, sharedflow, kmp, coroutines]

# Dependency graph
requires:
  - phase: none
    provides: "First phase of v1.1 milestone"
provides:
  - "MviViewModel<Intent, Model, Mutation, Event> abstract base class in core:mvi module"
  - "Unified Either<Event, Mutation> pipeline with StateFlow state and SharedFlow events"
  - "core:mvi Gradle module registered in settings.gradle.kts"
affects: [11-testing-infrastructure, 12-viewmodel-migration]

# Tech tracking
tech-stack:
  added: []
  patterns: ["MVI ViewModel with unified Either stream split via filterIsInstance", "Lazy StateFlow/SharedFlow initialization from MutableSharedFlow pipeline"]

key-files:
  created:
    - "core/mvi/build.gradle.kts"
    - "core/mvi/src/commonMain/kotlin/com/m2f/template/core/mvi/MviViewModel.kt"
  modified:
    - "settings.gradle.kts"

key-decisions:
  - "extraBufferCapacity=64 on pipeline MutableSharedFlow to prevent emit suspension before collectors ready"
  - "koin-core kept as implementation dependency per plan (not exposed to consumers)"
  - "reduce is suspend per user decision to support IO in reducer"

patterns-established:
  - "MVI pattern: Intent/Model/Mutation/Event as nested sealed types inside each ViewModel"
  - "Unified pipeline: MutableSharedFlow<Either<Event, Mutation>> split via filterIsInstance"
  - "State via scan+stateIn, events via shareIn with WhileSubscribed()"

requirements-completed: [MVI-01, MVI-02, MVI-03, MVI-04]

# Metrics
duration: 2min
completed: 2026-02-18
---

# Phase 10 Plan 01: MVI ViewModel Foundation Summary

**Abstract MviViewModel base class with Arrow Either pipeline, StateFlow state, SharedFlow events in new core:mvi KMP module**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-17T23:34:19Z
- **Completed:** 2026-02-17T23:36:50Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- Created `core:mvi` KMP module with all targets (Android, iOS, JVM, WASM) compiling successfully
- Implemented `MviViewModel<Intent, Model, Mutation, Event>` abstract class (~53 lines) with exact API shape from locked decisions
- Verified ROADMAP.md already reflects SharedFlow approach (no changes needed)

## Task Commits

Each task was committed atomically:

1. **Task 1: Create core:mvi Gradle module and MviViewModel base class** - `1814f95` (feat)
2. **Task 2: Update ROADMAP.md success criterion wording** - no commit (ROADMAP.md already had correct SharedFlow wording)

## Files Created/Modified
- `core/mvi/build.gradle.kts` - KMP module build config with arrow-core, coroutines, lifecycle-viewmodel-compose
- `core/mvi/src/commonMain/kotlin/com/m2f/template/core/mvi/MviViewModel.kt` - Abstract MVI base class with unified Either pipeline
- `settings.gradle.kts` - Added `include("core:mvi")` in shared core modules section

## Decisions Made
- Kept `koin-core` as `implementation` dependency per plan, even though research noted it could be omitted (base class does not use Koin directly). Kept for consistency with plan specification.
- Used `extraBufferCapacity = 64` on the pipeline MutableSharedFlow to buffer emissions before lazy collectors are initialized, preventing `emit()` from suspending.
- `reduce` is `suspend` (diverges from Airalo reference) per user decision to support IO in the reducer.

## Deviations from Plan

### Task 2: No Changes Needed

The plan expected ROADMAP.md to contain "Channel" references in Phase 10, but the roadmap was created after the context discussion and already incorporated the SharedFlow decision. Both the summary bullet ("SharedFlow effects") and success criterion #3 ("SharedFlow<Event> with replay=0") were already correct.

**Note:** Phase 12 success criterion #2 still references "Channel-based effects" (line 71 of ROADMAP.md). This is outside the scope of this plan's Phase 10 update but should be addressed when Phase 12 is planned.

---

**Total deviations:** 0 auto-fixed
**Impact on plan:** Task 2 was a no-op because the roadmap already had correct wording. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- `core:mvi` module is ready for Phase 11 (Testing Infrastructure) to build test utilities against
- `MviViewModel` base class is ready for Phase 12 (ViewModel Migration) to extend
- All KMP targets compile successfully -- no target-specific issues to resolve

## Self-Check: PASSED

- FOUND: core/mvi/build.gradle.kts
- FOUND: core/mvi/src/commonMain/kotlin/com/m2f/template/core/mvi/MviViewModel.kt
- FOUND: .planning/phases/10-mvi-viewmodel-foundation/10-01-SUMMARY.md
- FOUND: commit 1814f95

---
*Phase: 10-mvi-viewmodel-foundation*
*Completed: 2026-02-18*
