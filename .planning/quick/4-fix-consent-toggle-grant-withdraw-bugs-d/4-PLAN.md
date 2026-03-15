---
phase: quick-4
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsIntent.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
  - app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModelTest.kt
  - app/privacy/impl/src/commonMain/composeResources/values/strings.xml
  - app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
  - app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
autonomous: true
requirements: []

must_haves:
  truths:
    - "Toggling an OFF consent switch calls grantConsent (not withdrawConsent)"
    - "Toggling an ON consent switch calls withdrawConsent"
    - "Export status badge shows translated text instead of raw enum name"
  artifacts:
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsIntent.kt"
      provides: "ToggleConsent intent replacing WithdrawConsent"
      contains: "ToggleConsent"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt"
      provides: "Branching logic: grant if not granted, withdraw if granted"
      contains: "grantConsent"
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt"
      provides: "Translated export status badge text"
      contains: "exportStatusLabel"
    - path: "app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt"
      provides: "Updated wire passing onToggleConsent callback to screen"
      contains: "onToggleConsent"
  key_links:
    - from: "PrivacySettingsScreen.kt"
      to: "PrivacySettingsViewModel.kt"
      via: "ToggleConsent intent with consent object"
      pattern: "ToggleConsent"
    - from: "PrivacySettingsViewModel.kt"
      to: "Sdk.grantConsent / Sdk.withdrawConsent"
      via: "conditional call based on consent.granted"
      pattern: "consent\\.granted"
    - from: "PrivacyNavigation.kt"
      to: "PrivacySettingsScreen.kt"
      via: "onToggleConsent callback wiring ViewModel intent"
      pattern: "onToggleConsent"
---

<objective>
Fix three bugs in the privacy settings screen:

1. **Consent toggle inverted logic**: The `onWithdrawConsent` callback is always called when toggling, regardless of current state. When a consent is OFF (`granted=false`), toggling should GRANT it; when ON (`granted=true`), toggling should WITHDRAW it.
2. **Export status badge not translated**: `TerminalBadge` displays `state.exportStatus.status.name` (raw enum like "PENDING") instead of a localized string.
3. **Missing string resources**: Add translated strings for export status values in both EN and ES resource files.

Purpose: Users cannot grant optional consents (Marketing/Analytics), and non-English users see raw enum values.
Output: Working consent toggles with correct grant/withdraw logic, translated status badges.
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@core/models/src/commonMain/kotlin/com/m2f/template/models/dto/privacy/PrivacyDtos.kt
@core/sdk/src/commonMain/kotlin/com/m2f/template/sdk/api/PrivacyApi.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsIntent.kt
@app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModelTest.kt
@app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt

<interfaces>
<!-- Key types the executor needs -->

From PrivacyDtos.kt:
```kotlin
@Serializable
data class ConsentStatus(
    val type: ConsentType,
    val granted: Boolean,
    val grantedAt: String?,
    val documentVersion: String?,
)

@Serializable
data class GrantConsentRequest(
    val type: ConsentType,
    val documentVersion: String,
)

@Serializable
enum class ExportStatus { PENDING, PROCESSING, COMPLETED, FAILED, EXPIRED }
```

From PrivacyApi.kt:
```kotlin
suspend fun grantConsent(request: GrantConsentRequest): Either<AppError, Unit>
suspend fun withdrawConsent(type: ConsentType): Either<AppError, Unit>
suspend fun getLegalDocument(type: ConsentType, locale: String? = null): Either<AppError, LegalDocumentResponse>
```

The Sdk class delegates to PrivacyApi via `by`, so `sdk.grantConsent(...)` and `sdk.getLegalDocument(...)` are available.
</interfaces>
</context>

<tasks>

<task type="auto" tdd="true">
  <name>Task 1: Fix consent toggle to grant or withdraw based on current state</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsIntent.kt,
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModel.kt,
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt,
    app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt,
    app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/PrivacySettingsViewModelTest.kt
  </files>
  <behavior>
    - Test: Toggling an already-granted consent (granted=true) calls withdrawConsent and reloads consents showing it withdrawn
    - Test: Toggling a not-granted consent (granted=false) calls grantConsent (after fetching latest document version via getLegalDocument) and reloads consents showing it granted
    - Test: Grant consent failure shows error in model
  </behavior>
  <action>
    **Bug root cause:** `PrivacySettingsIntent.WithdrawConsent` is the only intent for toggle actions. The screen's `ConsentStatusRow` calls `onWithdraw()` on any toggle change regardless of `consent.granted` state. The ViewModel's `handleWithdrawConsent` always calls `sdk.withdrawConsent()`.

    **Fix:**

    1. **PrivacySettingsIntent.kt**: Replace `WithdrawConsent(val type: ConsentType)` with `ToggleConsent(val consent: ConsentStatus)` -- pass the full `ConsentStatus` so the ViewModel knows the current `granted` state.

    2. **PrivacySettingsViewModel.kt**:
       - Replace `is PrivacySettingsIntent.WithdrawConsent -> handleWithdrawConsent(intent.type)` with `is PrivacySettingsIntent.ToggleConsent -> handleToggleConsent(intent.consent)`.
       - Create `handleToggleConsent(consent: ConsentStatus)`:
         - If `consent.granted` is TRUE: call `sdk.withdrawConsent(consent.type)` then reload consents (existing logic).
         - If `consent.granted` is FALSE: first call `sdk.getLegalDocument(consent.type)` to get the current `version`, then call `sdk.grantConsent(GrantConsentRequest(type = consent.type, documentVersion = version))`, then reload consents.
         - On any error: set error mutation as usual.
       - Remove old `handleWithdrawConsent` method.

    3. **PrivacySettingsScreen.kt**:
       - Change `onWithdrawConsent: (ConsentType) -> Unit` to `onToggleConsent: (ConsentStatus) -> Unit` in both `PrivacySettingsScreen` and `PrivacySettingsContent` composable signatures.
       - In `ConsentStatusRow`: change `onWithdraw: () -> Unit` to `onToggle: () -> Unit`, update `TerminalSwitch`'s `onCheckedChange` to call `onToggle()`.
       - Update all call sites: `onWithdrawConsent(consent.type)` becomes `onToggleConsent(consent)` passing the full `ConsentStatus`.

    4. **PrivacyNavigation.kt** (wire file): Update the call to `PrivacySettingsScreen` to replace the `onWithdrawConsent` parameter with `onToggleConsent`. The lambda should dispatch `PrivacySettingsIntent.ToggleConsent(consent)` to the ViewModel, matching the new `(ConsentStatus) -> Unit` signature.

    5. **PrivacySettingsViewModelTest.kt**:
       - Update existing `withdraw consent reloads consents after success` test to use `ToggleConsent(consentWithGrantedTrue)` and configure `withdrawConsent` fake.
       - Add new test `grant consent fetches document version and grants` that uses `ToggleConsent(consentWithGrantedFalse)` and configures `getLegalDocument` + `grantConsent` + `getActiveConsents` fakes. Verify model shows the consent as granted after reload.
       - Add test for grant consent error case.
  </action>
  <verify>
    <automated>./gradlew :app:privacy:allTests</automated>
  </verify>
  <done>
    - ToggleConsent intent replaces WithdrawConsent
    - When consent.granted=false, toggle calls grantConsent with latest document version
    - When consent.granted=true, toggle calls withdrawConsent
    - PrivacyNavigation.kt wire file passes onToggleConsent to screen
    - All existing and new tests pass
  </done>
</task>

<task type="auto">
  <name>Task 2: Translate export status badge and add string resources</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt,
    app/privacy/impl/src/commonMain/composeResources/values/strings.xml,
    app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
  </files>
  <action>
    **Bug root cause:** Line 182 of PrivacySettingsScreen.kt uses `state.exportStatus.status.name` which outputs raw enum names like "PENDING", "COMPLETED", etc.

    **Fix:**

    1. **strings.xml (EN)**: Add export status strings after the existing `privacy_settings_export_request` entry:
       ```xml
       <string name="privacy_export_status_pending">Pending</string>
       <string name="privacy_export_status_processing">Processing</string>
       <string name="privacy_export_status_completed">Completed</string>
       <string name="privacy_export_status_failed">Failed</string>
       <string name="privacy_export_status_expired">Expired</string>
       ```

    2. **strings.xml (ES)**: Add corresponding Spanish translations:
       ```xml
       <string name="privacy_export_status_pending">Pendiente</string>
       <string name="privacy_export_status_processing">Procesando</string>
       <string name="privacy_export_status_completed">Completado</string>
       <string name="privacy_export_status_failed">Fallido</string>
       <string name="privacy_export_status_expired">Expirado</string>
       ```

    3. **PrivacySettingsScreen.kt**: Create a `@Composable` helper function `exportStatusLabel(status: ExportStatus): String` that maps each enum value to its `stringResource(Res.string.privacy_export_status_xxx)`. Replace `state.exportStatus.status.name` on line 182 with `exportStatusLabel(state.exportStatus.status)`.
  </action>
  <verify>
    <automated>./gradlew :app:privacy:impl:compileCommonMainKotlinMetadata</automated>
  </verify>
  <done>
    - Export status badge shows "Pending"/"Pendiente" instead of "PENDING"
    - All five ExportStatus values have EN and ES translations
    - No raw enum names displayed in the UI
  </done>
</task>

</tasks>

<verification>
- `./gradlew :app:privacy:allTests` passes (consent toggle logic)
- `./gradlew :app:privacy:impl:compileCommonMainKotlinMetadata` compiles (string resources)
- No references to `WithdrawConsent` remain in privacy module (replaced by `ToggleConsent`)
- No `status.name` used for badge text in PrivacySettingsScreen
</verification>

<success_criteria>
- Toggling an OFF optional consent (Marketing/Analytics) calls grantConsent with current document version
- Toggling an ON optional consent calls withdrawConsent
- Export status badges show translated text in both EN and ES
- All privacy module tests pass
</success_criteria>

<output>
After completion, create `.planning/quick/4-fix-consent-toggle-grant-withdraw-bugs-d/4-SUMMARY.md`
</output>
