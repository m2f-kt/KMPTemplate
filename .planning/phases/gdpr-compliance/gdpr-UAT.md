---
status: diagnosed
phase: gdpr-compliance
source: docs/superpowers/plans/2026-03-12-gdpr-lopd-compliance.md, docs/superpowers/specs/2026-03-12-gdpr-lopd-compliance-design.md
started: 2026-03-15T10:00:00Z
updated: 2026-03-15T10:45:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Cold Start Smoke Test
expected: Kill any running server. Start Docker services with `./gradlew devUp`, then start the server with `./gradlew :server:run`. Server boots without errors, privacy migrations run, and `GET http://localhost:8080/api/privacy/legal/PRIVACY_POLICY?locale=es` returns a legal document.
result: pass

### 2. Consent Gate After Login
expected: Register a new user or log in for the first time (without prior consent). After login, instead of going to the dashboard, you are redirected to the Consent Gate screen showing required consents (Privacy Policy + Terms of Service) with checkboxes and document preview links.
result: issue
reported: "Registration screen has a consent checkbox AND post-login shows Consent Gate — duplicated flow. Preference: option B — checkbox on registration blocks account creation, Consent Gate only appears when policy version changed since last acceptance. Also padding issue between checkbox and text on registration screen."
severity: major

### 3. Consent Gate Accept Flow
expected: On the Consent Gate screen, toggle both Privacy Policy and Terms of Service checkboxes. The "Accept" button becomes enabled. Clicking Accept grants consent and navigates to the dashboard.
result: pass

### 4. Consent Gate Decline Flow
expected: On the Consent Gate screen, clicking Decline (or closing without accepting) logs the user out and returns to the login screen.
result: pass

### 5. Consent Gate After Registration
expected: After registering a new account, the user is redirected to the Consent Gate before reaching the dashboard.
result: pass

### 6. Legal Document Viewer
expected: From the Consent Gate or Privacy Settings, clicking a legal document link opens the Legal Document screen showing the document content in a scrollable markdown viewer with version info header.
result: pass

### 7. Privacy Settings Screen Access
expected: From the Profile sidebar, clicking "Privacy & Data" navigates to the Privacy Settings screen showing: active consents with withdrawal toggles, data export section, processing restriction toggle.
result: issue
reported: "Privacy & Data opens as a separate full screen instead of replacing the main content area within the profile dashboard. The sidebar should stay visible and the selected item's content should fill the main panel."
severity: major

### 8. Withdraw Consent (Marketing/Analytics)
expected: In Privacy Settings, toggling off a non-required consent (Marketing or Analytics) immediately withdraws it. The toggle reflects the updated state.
result: issue
reported: "Marketing and Analytics consent toggles are not displayed in Privacy Settings. Only required consents (Terms of Service, Privacy Policy) are shown with no withdrawal toggles."
severity: major

### 9. Request Data Export
expected: In Privacy Settings, clicking "Request Export" triggers a data export request. The UI shows the export status (Pending/Processing). When complete, a download link appears.
result: issue
reported: "Export initially shows PENDING badge, but waiting does nothing. Refreshing the screen makes the badge disappear. Export status is not persistent across screen re-entries. Should always show current status when returning to the screen."
severity: major

### 10. Request Account Deletion
expected: Navigating to Account Deletion shows a multi-step flow: warning screen -> re-authentication (password) -> optional reason -> confirmation with grace period info. After confirming, the deletion is scheduled and the user sees a "scheduled" status.
result: issue
reported: "Three bugs: (1) /api/privacy/deletion/status returns {status:none} but DeletionStatus enum has no 'none' value → JsonConvertException → error banner on screen load. (2) After token refresh, retry sends duplicate Authorization header (Bearer token1; Bearer token2) → malformed. (3) Deletion request POST returns 500 Internal Server Error — cannot complete the flow."
severity: blocker

### 11. Cancel Account Deletion
expected: If a deletion is pending (within grace period), the user can cancel it from Privacy Settings or the Account Deletion screen. After cancellation, the account continues normally.
result: skipped
reason: Blocked by Test 10 — account deletion flow is broken

### 12. Processing Restriction Toggle
expected: In Privacy Settings, toggling "Restrict Processing" ON restricts the user's data processing. While restricted, API calls to non-privacy endpoints return a "Processing Restricted" error (the app should show an appropriate message). Toggling OFF lifts the restriction.
result: issue
reported: "Toggle has no visible effect — dashboard and other areas remain accessible. User wants this feature removed entirely."
severity: major

### 13. Registration Screen Legal Links
expected: The registration screen shows clickable links to the Privacy Policy and Terms of Service legal documents.
result: issue
reported: "Links work correctly but there is insufficient padding between the checkbox and the text. Looks ugly."
severity: cosmetic

### 14. Server Tests Pass
expected: `./gradlew :server:privacy:impl:test` passes all tests.
result: issue
reported: "Server privacy module has no test sources (compileTestKotlin NO-SOURCE). Tests pass vacuously."
severity: major

### 15. Client Tests Pass
expected: `./gradlew :app:privacy:impl:jvmTest` passes all tests.
result: pass

## Summary

total: 15
passed: 6
issues: 8
pending: 0
skipped: 1

## Gaps

- truth: "Registration and Consent Gate should not duplicate consent acceptance flow"
  status: failed
  reason: "User reported: Registration screen has a consent checkbox AND post-login shows Consent Gate — duplicated flow. Preference: option B — checkbox on registration blocks account creation, Consent Gate only appears when policy version changed since last acceptance. Also padding issue between checkbox and text on registration screen."
  severity: major
  test: 2
  root_cause: "RegisterViewModel calls navigateWithConsentCheck() after registration but never calls sdk.grantConsent() — so server always reports hasOutdated=true. Registration checkbox is client-side-only validation, not server-recorded."
  artifacts:
    - path: "app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterViewModel.kt"
      issue: "Lines 94-133: after successful register(), must call grantConsent() for PRIVACY_POLICY and TERMS_OF_SERVICE before navigateWithConsentCheck()"
    - path: "app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterScreen.kt"
      issue: "Line 680: Row in TermsCheckboxWithLinks has no spacing between TerminalCheckbox and ClickableText"
  missing:
    - "Call sdk.grantConsent() for both consent types after successful registration"
    - "Add Spacer(Modifier.width(8.dp)) or Arrangement.spacedBy(8.dp) in TermsCheckboxWithLinks Row"
    - "Update RegisterViewModelTest line 98 to expect grantConsent calls and dashboard navigation"

- truth: "Privacy Settings should display within profile dashboard, not as separate screen"
  status: failed
  reason: "User reported: Privacy & Data opens as a separate full screen instead of replacing the main content area within the profile dashboard. The sidebar should stay visible."
  severity: major
  test: 7
  root_cause: "ProfileScreen.kt line 208 special-cases 'privacy' to call onNavigateToPrivacy() (which pushes PrivacySettingsRoute onto backStack) instead of setting selectedNavItem like all other sidebar items."
  artifacts:
    - path: "app/profile/impl/src/commonMain/kotlin/com/m2f/template/app/profile/ProfileScreen.kt"
      issue: "Lines 208-210: privacy key calls onNavigateToPrivacy() instead of selectedNavItem=key. Lines 224-254: main content Column needs when(selectedNavItem) to swap content."
    - path: "app/profile/wire/src/commonMain/kotlin/com/m2f/template/app/profile/wire/ProfileNavigation.kt"
      issue: "Line 40: onNavigateToPrivacy pushes PrivacySettingsRoute to global backStack"
  missing:
    - "Remove special-case for 'privacy' key — set selectedNavItem instead"
    - "Add when(selectedNavItem) in main content Column to render privacy content inline"
    - "Pass privacyContent composable slot from wire layer to avoid profile→privacy module dependency"

- truth: "Marketing and Analytics consent toggles should be visible and withdrawable"
  status: failed
  reason: "User reported: Marketing and Analytics consent toggles are not displayed in Privacy Settings. Only required consents shown."
  severity: major
  test: 8
  root_cause: "Three layers: (1) getActiveConsents() only returns DB rows with granted=true — Marketing/Analytics never have records. (2) ConsentGate only grants PRIVACY_POLICY and TERMS_OF_SERVICE. (3) No seed data for Marketing/Analytics legal documents."
  artifacts:
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ConsentServiceImpl.kt"
      issue: "Line 79: requiredTypes hardcodes only PRIVACY_POLICY, TERMS_OF_SERVICE"
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedConsentRepository.kt"
      issue: "Lines 56-67: findAllActiveByUser filters granted=true only"
  missing:
    - "Service must synthesize default ConsentStatus(granted=false) for consent types with no DB records"
    - "Seed migration for Marketing/Analytics legal documents in legal_documents table"

- truth: "Data export status should persist and be visible when returning to Privacy Settings"
  status: failed
  reason: "User reported: Export initially shows PENDING badge, but waiting does nothing. Refreshing the screen makes the badge disappear. Export status is not persistent across screen re-entries."
  severity: major
  test: 9
  root_cause: "PrivacySettingsViewModel.handleLoad() never fetches existing export status — only fetches consents and deletion status. No server endpoint exists to get active export by user (only by ID). Export ID is not persisted client-side."
  artifacts:
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt"
      issue: "Lines 29-50: handleLoad() missing call to get active export status"
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/ExportRoutes.kt"
      issue: "No GET /api/privacy/export/active endpoint (repository has findActiveByUser already)"
    - path: "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt"
      issue: "No getActiveExport() method"
  missing:
    - "Add GET /api/privacy/export/active server route using existing repository.findActiveByUser()"
    - "Add DataExportService.getActiveExport(userId) method"
    - "Add sdk.getActiveExport() to PrivacyApi + PrivacyApiImpl"
    - "Call sdk.getActiveExport() in PrivacySettingsViewModel.handleLoad()"

- truth: "Account deletion flow should complete without errors"
  status: failed
  reason: "User reported: (1) /api/privacy/deletion/status returns {status:none} but DeletionStatus enum has no 'none' value → JsonConvertException. (2) After token refresh, duplicate Authorization header sent. (3) Deletion request POST returns 500."
  severity: blocker
  test: 10
  root_cause: "Bug 1: DeletionRoutes.kt line 23 returns mapOf('status' to 'none') when no deletion pending — not a valid DeletionResponse. Bug 2: AuthInterceptor.kt line 104 appends bearerAuth() without removing old header. Bug 3: AccountDeletionServiceImpl doesn't handle null passwordHash for OAuth users."
  artifacts:
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/DeletionRoutes.kt"
      issue: "Lines 23-24: returns mapOf('status' to 'none') instead of HTTP 204 or null"
    - path: "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/AuthInterceptor.kt"
      issue: "Lines 104, 110: bearerAuth() appends duplicate Authorization header on retry — must remove old header first"
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/AccountDeletionServiceImpl.kt"
      issue: "Line 47: passwordHasher.verify() crashes if user.passwordHash is null (OAuth users)"
  missing:
    - "Return HTTP 204 No Content when no deletion pending, handle in client as null"
    - "Add request.headers.remove(HttpHeaders.Authorization) before bearerAuth() on retry"
    - "Guard passwordHash null check in AccountDeletionServiceImpl — raise InvalidCredentials for OAuth users"

- truth: "Processing restriction feature should be removed"
  status: failed
  reason: "User reported: Toggle has no visible effect. User wants the feature removed entirely."
  severity: major
  test: 12
  root_cause: "conduitAuth was never modified to check processingRestricted flag. Feature is half-implemented. User wants full removal."
  artifacts:
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/routes/RestrictionRoutes.kt"
      issue: "Entire file — delete"
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/service/ProcessingRestrictionServiceImpl.kt"
      issue: "Entire file — delete"
    - path: "server/privacy/contract/src/main/kotlin/com/m2f/server/privacy/contract/service/ProcessingRestrictionService.kt"
      issue: "Entire file — delete"
    - path: "server/auth/contract/src/main/kotlin/com/m2f/server/auth/contract/tables/UsersTable.kt"
      issue: "Line 26: remove processingRestricted column"
    - path: "core/models/src/commonMain/kotlin/com/m2f/template/models/AppError.kt"
      issue: "Lines 276-278: remove AppError.Privacy.ProcessingRestricted"
    - path: "core/models/src/commonMain/kotlin/com/m2f/template/models/routes/ApiRoutes.kt"
      issue: "Lines 182-186: remove RestrictProcessing and LiftRestriction routes"
    - path: "core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt"
      issue: "Lines 26-27: remove restrictProcessing() and liftRestriction()"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt"
      issue: "Lines 210-222: remove Processing Restriction section"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt"
      issue: "Lines 87-100: remove ToggleRestriction handler"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsIntent.kt"
      issue: "Line 9: remove ToggleRestriction intent"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsMutation.kt"
      issue: "Line 12: remove SetRestricted mutation"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsModel.kt"
      issue: "Line 12: remove isRestricted field"
  missing:
    - "Delete all restriction-related files, types, routes, SDK methods, UI sections, string resources, and DI bindings"
    - "Add forward migration to drop processing_restricted column from users table"
    - "Remove from ~30 files across server, core, and app modules (see full manifest in diagnosis)"

- truth: "Registration checkbox should have proper padding between checkbox and text"
  status: failed
  reason: "User reported: Insufficient padding between checkbox and text on registration screen."
  severity: cosmetic
  test: 13
  root_cause: "TermsCheckboxWithLinks Row at RegisterScreen.kt line 680 has no spacing. TerminalCheckbox only adds Spacer when a label is provided, but here no label is passed."
  artifacts:
    - path: "app/auth/impl/src/commonMain/kotlin/com/m2f/template/app/auth/RegisterScreen.kt"
      issue: "Line 680: Row needs Arrangement.spacedBy(8.dp) or Spacer between checkbox and text"
  missing:
    - "Add horizontalArrangement = Arrangement.spacedBy(8.dp) to the Row"

- truth: "Consent list endpoint returns duplicate items instead of latest per type"
  status: failed
  reason: "User reported: GET /api/privacy/consent/list returns all historical consent records (multiple PRIVACY_POLICY and TERMS_OF_SERVICE entries with different timestamps). Should only return the latest record per consent type."
  severity: major
  test: 8
  root_cause: "ExposedConsentRepository.findAllActiveByUser() returns all rows with granted=true without deduplication. Since consent_records is an append-only ledger, each grant creates a new row. Query needs GROUP BY consentType with MAX(createdAt)."
  artifacts:
    - path: "server/privacy/impl/src/main/kotlin/com/m2f/server/privacy/repository/ExposedConsentRepository.kt"
      issue: "Lines 56-67: findAllActiveByUser returns all granted rows without deduplication by type"
  missing:
    - "Add subquery or window function to return only the latest consent record per type per user"

- truth: "Server privacy module should have test sources"
  status: failed
  reason: "Server privacy module has no test sources (compileTestKotlin NO-SOURCE). Tests pass vacuously."
  severity: major
  test: 14
  root_cause: "No test files exist under server/privacy/impl/src/test/. Module was shipped without server-side tests."
  artifacts:
    - path: "server/privacy/impl/src/test/"
      issue: "Directory is empty — no test classes"
  missing:
    - "Write integration tests for ConsentService, LegalDocumentService, DataExportService, AccountDeletionService"
    - "Write route tests for all privacy endpoints"
