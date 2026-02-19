---
phase: 15-localization
plan: 04
subsystem: localization
tags: [i18n, server-side, accept-language, ktor, error-messages, spanish]

# Dependency graph
requires:
  - phase: 15-localization
    provides: "StringKey enum with AppError.code mappings and English strings.xml"
provides:
  - "ServerStrings object with English + Spanish server error translations"
  - "preferredLanguage() RoutingContext extension for Accept-Language parsing"
  - "Locale-aware DomainError.respond() across all server error types"
affects: [server-error-responses, api-localization]

# Tech tracking
tech-stack:
  added: []
  patterns: [ServerStrings locale map with language fallback, preferredLanguage() Accept-Language header parsing]

key-files:
  created:
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/localization/ServerStrings.kt
  modified:
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt
    - server/core/config/src/main/kotlin/com/m2f/core/config/server/DomainError.kt
    - server/auth/src/main/kotlin/com/m2f/server/auth/errors/AuthErrors.kt
    - server/groups/src/main/kotlin/com/m2f/server/groups/errors/GroupErrors.kt

key-decisions:
  - "ServerStrings keys match AppError.code values exactly for direct lookup"
  - "preferredLanguage() takes first 2 chars of Accept-Language header for language code extraction"
  - "Validation errors (IncorrectInput, MissingParameter, etc.) use ServerStrings for base message but keep field-level detail in formattedErrors"

patterns-established:
  - "ServerStrings.resolve(code, locale): centralized server-side error localization with en fallback"
  - "preferredLanguage() on RoutingContext: standard locale extraction for all server error responses"

# Metrics
duration: 2min
completed: 2026-02-19
---

# Phase 15 Plan 04: Server-Side i18n Summary

**ServerStrings object with 17 error codes in English + Spanish, preferredLanguage() Accept-Language parser, and locale-aware DomainError.respond() across auth/group/validation/server errors**

## Performance

- **Duration:** 2 min
- **Started:** 2026-02-19T17:36:21Z
- **Completed:** 2026-02-19T17:38:58Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- ServerStrings object with English and Spanish translations for all 17 server error codes with resolve() fallback chain (locale → en → raw code)
- preferredLanguage() extension on RoutingContext parsing Accept-Language header into 2-char language code
- All 19 DomainError.respond() methods refactored to use ServerStrings.resolve() for locale-aware error messages (7 in DomainError.kt, 6 in AuthErrors.kt, 6 in GroupErrors.kt)

## Task Commits

Each task was committed atomically:

1. **Task 1: Create ServerStrings localization object** - `5941d4b` (feat)
2. **Task 2: Refactor DomainError.respond() to locale-aware resolution** - `3677eab` (feat)

## Files Created/Modified
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/localization/ServerStrings.kt` - Server-side localized string map with en/es translations for 17 error codes
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/Error.kt` - Added preferredLanguage() extension function
- `server/core/config/src/main/kotlin/com/m2f/core/config/server/DomainError.kt` - Refactored 7 respond() methods to use ServerStrings
- `server/auth/src/main/kotlin/com/m2f/server/auth/errors/AuthErrors.kt` - Refactored 6 respond() methods to use ServerStrings
- `server/groups/src/main/kotlin/com/m2f/server/groups/errors/GroupErrors.kt` - Refactored 6 respond() methods to use ServerStrings

## Decisions Made
- **ServerStrings keys = AppError.code:** Direct lookup by error code string (e.g., "AUTH_INVALID_CREDENTIALS") with no indirection layer
- **First 2 chars for language:** `preferredLanguage()` takes first 2 characters of Accept-Language header value for simple ISO 639-1 extraction
- **Validation detail preserved:** IncorrectInput keeps field-level formattedErrors list alongside localized base message from ServerStrings

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Server-side i18n complete for all existing error types
- Adding new error codes requires adding entries to both ServerStrings en/es maps
- Ready for Plan 05 (remaining localization tasks)
- Spanish locale tested via ServerStrings.resolve() fallback chain

---
*Phase: 15-localization*
*Completed: 2026-02-19*
