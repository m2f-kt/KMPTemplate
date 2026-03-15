---
phase: quick-8
plan: 1
type: execute
wave: 1
depends_on: []
files_modified:
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionMutation.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionEvent.kt
  - app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
  - app/privacy/impl/src/commonMain/composeResources/values/strings.xml
  - app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
autonomous: true
requirements: [QUICK-8]

must_haves:
  truths:
    - "Step 1 (Warning) shows Error alert with [DANGER] title, deletion scope TerminalCard, grace period text, and 'I Understand, Continue' + 'Cancel' buttons"
    - "Step 2 (Re-Auth) shows 'Verify Your Identity' title, subtitle, PASSWORD label (uppercase), 'Verify & Continue' (Destructive) + 'Back' (Ghost) buttons"
    - "Step 3 (Reason) shows 'Help Us Improve' title, FEEDBACK (OPTIONAL) label, 'Continue' (Destructive) + 'Skip' (Ghost) + 'Back' (Ghost) buttons"
    - "Step 4 (Confirm) shows step label, 'Final Confirmation' title, deletion summary Accent card with email + date, warning alert with date, 'Delete My Account' (Destructive) + 'Cancel' (Ghost) buttons"
    - "Step 5 (Scheduled) shows Success alert, Info card with scheduled details, 'Cancel Deletion Request' (Default) + 'Log Out Now' (Ghost) buttons"
    - "Desktop layout shows two-column layout: left column (main flow) + right column (480dp, contextual info panel with border-left)"
    - "All strings have English and Spanish translations"
  artifacts:
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt"
      provides: "Redesigned 5-step deletion flow with mobile + desktop layouts"
      min_lines: 200
    - path: "app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt"
      provides: "Model with userEmail field for confirm/scheduled steps"
      contains: "userEmail"
    - path: "app/privacy/impl/src/commonMain/composeResources/values/strings.xml"
      provides: "All English string resources for redesigned flow"
      contains: "privacy_deletion_warning_danger_title"
    - path: "app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml"
      provides: "All Spanish translations for redesigned flow"
      contains: "privacy_deletion_warning_danger_title"
  key_links:
    - from: "AccountDeletionScreen.kt"
      to: "strings.xml"
      via: "stringResource(Res.string.*)"
      pattern: "stringResource\\(Res\\.string"
    - from: "AccountDeletionScreen.kt"
      to: "AccountDeletionModel.kt"
      via: "state.userEmail, state.pendingDeletion"
      pattern: "state\\.(userEmail|pendingDeletion)"
    - from: "PrivacyNavigation.kt"
      to: "AccountDeletionScreen.kt"
      via: "onLogout callback + onSkipReason callback"
      pattern: "onLogout|onSkipReason"
---

<objective>
Redesign the AccountDeletionScreen to match Pencil designs across all 5 steps (Warning, Re-Auth, Reason, Confirm, Scheduled) for both mobile and desktop layouts, and update all string resources with new/changed English and Spanish translations.

Purpose: Align the delete account flow UI with the approved Pencil designs -- new card-based layouts, proper button variants, two-column desktop layout, step indicators, and updated copy.
Output: Rewritten AccountDeletionScreen.kt, updated Model/Intent/Event/Mutation, updated strings.xml (EN + ES), updated PrivacyNavigation.kt wiring.
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionEvent.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionMutation.kt
@app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
@app/privacy/impl/src/commonMain/composeResources/values/strings.xml
@app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml

<interfaces>
<!-- Design system components used by the screen -->

From designsystem - TerminalCard:
```kotlin
fun TerminalCard(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    variant: CardVariant = CardVariant.Default,
    icon: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
)
enum class CardVariant { Default, Accent, Info, Highlighted, Compact }
```

From designsystem - TerminalButton:
```kotlin
fun TerminalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
)
enum class ButtonVariant { Default, Secondary, Ghost, Destructive, Success }
```

From designsystem - TerminalAlert:
```kotlin
fun TerminalAlert(message: String, variant: AlertVariant, title: String?, onDismiss: (() -> Unit)?)
enum class AlertVariant { Info, Success, Warning, Error }
```

From models - DeletionResponse:
```kotlin
data class DeletionResponse(
    val id: String,
    val status: DeletionStatus,
    val scheduledAt: String,
    val completedAt: String? = null,
)
```

From existing AccountDeletionModel:
```kotlin
data class AccountDeletionModel(
    val step: DeletionStep = DeletionStep.WARNING,
    val password: String = "",
    val reason: String = "",
    val pendingDeletion: DeletionResponse? = null,
    val loading: Boolean = false,
    val error: StringKey? = null,
)
```

From existing screen callback signature:
```kotlin
fun AccountDeletionScreen(
    state: AccountDeletionModel,
    onProceedToReAuth: () -> Unit,
    onReAuthenticate: (String) -> Unit,
    onSetReason: (String) -> Unit,
    onConfirmDeletion: () -> Unit,
    onCancelDeletion: () -> Unit,
    onBack: () -> Unit,
)
```
</interfaces>
</context>

<tasks>

<task type="auto">
  <name>Task 1: Update model, intents, events, mutations, and string resources</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionModel.kt,
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionIntent.kt,
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionEvent.kt,
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionMutation.kt,
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModel.kt,
    app/privacy/impl/src/commonMain/composeResources/values/strings.xml,
    app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
  </files>
  <action>
**1. AccountDeletionModel.kt** -- Add `userEmail: String = ""` field. The confirm and scheduled steps need the user's email for display in cards.

**2. AccountDeletionIntent.kt** -- Add `data object SkipReason : AccountDeletionIntent` (step 3 Skip button) and `data object LogOut : AccountDeletionIntent` (step 5 Log Out Now button). Read the file first to see existing intents.

**3. AccountDeletionEvent.kt** -- Add `data object LoggedOut : AccountDeletionEvent` for the "Log Out Now" action from step 5 (separate from NavigateToLogin which fires after deletion confirm).

**4. AccountDeletionMutation.kt** -- Add `data class SetUserEmail(val email: String) : AccountDeletionMutation`. Read file first.

**5. AccountDeletionViewModel.kt** -- Update:
  - In `handleLoad()`: After checking deletion status, also call `sdk.getProfile()` to fetch user email and send `SetUserEmail(response.email)` mutation. Do this before the deletion status check so email is available for all steps.
  - Add `handleSkipReason()`: Same as `handleSetReason("")` -- skip reason sets empty reason and moves to CONFIRM step.
  - Add `handleLogOut()`: Call `sdk.logout()` then send `LoggedOut` event.
  - Add cases in `take()` for SkipReason and LogOut intents.
  - Add `SetUserEmail` case in `reduce()`: `model.copy(userEmail = mutation.email)`.

**6. strings.xml (EN)** -- Replace the entire `<!-- AccountDeletionScreen -->` section. Remove old strings that are no longer used. Add all new strings:

```
<!-- Step 1: Warning -->
privacy_deletion_title = "Delete Your Account"
privacy_deletion_processing = "Processing..." (keep)
privacy_deletion_error_title = "Error" (keep)
privacy_deletion_warning_danger_title = "DANGER"
privacy_deletion_warning_danger_message = "This action is permanent and cannot be undone."
privacy_deletion_warning_scope_label = "THIS WILL PERMANENTLY DELETE:"
privacy_deletion_warning_scope_card_title = "deletion_scope"
privacy_deletion_warning_scope_card_desc = "// items to be removed"
privacy_deletion_warning_scope_item1 = "▸ Your profile and personal data"
privacy_deletion_warning_scope_item2 = "▸ All your documents and files"
privacy_deletion_warning_scope_item3 = "▸ Your group memberships"
privacy_deletion_warning_scope_item4 = "▸ Consent and activity history"
privacy_deletion_warning_grace = "▸ A 7-day grace period applies. Your account will be scheduled for deletion and permanently removed after 7 days. You may cancel this during the grace period."
privacy_deletion_warning_understand = "I Understand, Continue"
privacy_deletion_cancel = "Cancel"
privacy_deletion_back = "Back"

<!-- Step 2: Re-Auth -->
privacy_deletion_reauth_title = "Verify Your Identity"
privacy_deletion_reauth_subtitle = "Enter your password to confirm this action."
privacy_deletion_reauth_label = "PASSWORD" (update to uppercase)
privacy_deletion_reauth_placeholder = "Enter your password" (keep)
privacy_deletion_reauth_error_title = "ERROR"
privacy_deletion_reauth_error_message = "Invalid password. Please try again."
privacy_deletion_reauth_verify = "Verify & Continue"

<!-- Step 3: Reason -->
privacy_deletion_reason_title = "Help Us Improve"
privacy_deletion_reason_subtitle = "Optionally tell us why you are leaving."
privacy_deletion_reason_label = "FEEDBACK (OPTIONAL)" (update to uppercase)
privacy_deletion_reason_placeholder = "Tell us why you're leaving..." (keep)
privacy_deletion_reason_continue = "Continue"
privacy_deletion_reason_skip = "Skip"

<!-- Step 4: Confirm -->
privacy_deletion_confirm_step = "STEP 4 OF 5"
privacy_deletion_confirm_title = "Final Confirmation"
privacy_deletion_confirm_subtitle = "Review the details below before confirming deletion"
privacy_deletion_confirm_card_title = "deletion_summary"
privacy_deletion_confirm_card_desc = "// scheduled for removal"
privacy_deletion_confirm_account = "Account: %1$s"
privacy_deletion_confirm_scheduled = "Scheduled deletion: %1$s"
privacy_deletion_confirm_grace = "Grace period: 7 days"
privacy_deletion_confirm_warn = "After %1$s, this action cannot be undone."
privacy_deletion_confirm_button = "Delete My Account" (keep)

<!-- Step 5: Scheduled -->
privacy_deletion_scheduled_step = "STEP 5 OF 5"
privacy_deletion_scheduled_success_title = "Deletion Scheduled"
privacy_deletion_scheduled_success_message = "Your deletion request has been confirmed."
privacy_deletion_scheduled_card_title = "deletion_scheduled"
privacy_deletion_scheduled_card_desc = "// account marked for removal"
privacy_deletion_scheduled_card_line1 = "Your account will be permanently deleted on %1$s"
privacy_deletion_scheduled_card_line2 = "You will receive an email confirmation"
privacy_deletion_scheduled_card_line3 = "You can cancel this request anytime before the scheduled date"
privacy_deletion_scheduled_cancel = "Cancel Deletion Request"
privacy_deletion_scheduled_logout = "Log Out Now"

<!-- Desktop right panel -->
privacy_deletion_desktop_step1_indicator = "STEP 01 / 03 — ACCOUNT DELETION"
privacy_deletion_desktop_step1_header = "DELETION IMPACT"
privacy_deletion_desktop_step1_title = "What gets removed"
privacy_deletion_desktop_step1_desc = "When your account is deleted, all associated data will be permanently removed from our systems."
privacy_deletion_desktop_step1_bullet1 = "▸ Profile information and settings"
privacy_deletion_desktop_step1_bullet2 = "▸ Documents and uploaded files"
privacy_deletion_desktop_step1_bullet3 = "▸ Group memberships and roles"
privacy_deletion_desktop_step1_bullet4 = "▸ Consent records and activity logs"
privacy_deletion_desktop_step1_note = "A 7-day grace period allows you to cancel before permanent deletion."

privacy_deletion_desktop_step2_indicator = "STEP 02 / 03 — VERIFY IDENTITY"
privacy_deletion_desktop_step2_header = "SECURITY NOTICE"
privacy_deletion_desktop_step2_title = "Why we verify"
privacy_deletion_desktop_step2_desc = "Account deletion is irreversible. We verify your identity to prevent unauthorized deletions."
privacy_deletion_desktop_step2_bullet1 = "▸ Protects against unauthorized access"
privacy_deletion_desktop_step2_bullet2 = "▸ Confirms you are the account owner"
privacy_deletion_desktop_step2_bullet3 = "▸ Required by our security policy"

privacy_deletion_desktop_step3_indicator = "STEP 03 / 03 — FEEDBACK"
privacy_deletion_desktop_step3_header = "PRIVACY NOTE"
privacy_deletion_desktop_step3_title = "Your feedback"
privacy_deletion_desktop_step3_desc = "Your feedback is completely optional and helps us improve our service."
privacy_deletion_desktop_step3_bullet1 = "▸ Feedback is anonymous after account deletion"
privacy_deletion_desktop_step3_bullet2 = "▸ Used only for service improvement"
privacy_deletion_desktop_step3_bullet3 = "▸ You can skip this step entirely"

privacy_deletion_desktop_step4_header = "DELETION IMPACT"
privacy_deletion_desktop_step4_bullet1 = "▸ All data will be permanently erased"
privacy_deletion_desktop_step4_bullet2 = "▸ Active sessions will be terminated"
privacy_deletion_desktop_step4_bullet3 = "▸ Shared files will become inaccessible"
privacy_deletion_desktop_step4_note = "Grace period allows cancellation before final removal."

privacy_deletion_desktop_step5_header = "WHAT HAPPENS NEXT"
privacy_deletion_desktop_step5_step1 = "1. Your account is marked for deletion"
privacy_deletion_desktop_step5_step2 = "2. A confirmation email will be sent"
privacy_deletion_desktop_step5_step3 = "3. After the grace period, all data is removed"
privacy_deletion_desktop_step5_cancel = "To cancel, use the button on the left or contact support before the scheduled date."
```

Remove old strings: `privacy_deletion_warning_title`, `privacy_deletion_warning_message`, `privacy_deletion_warning_consider`, `privacy_deletion_warning_bullet1/2/3`, `privacy_deletion_go_back`, `privacy_deletion_continue`, `privacy_deletion_reauth_message`, `privacy_deletion_reason_message`, `privacy_deletion_confirm_message`, `privacy_deletion_confirm_question`, `privacy_deletion_confirm_cancel`, `privacy_deletion_confirm_title` (replaced by new one), `privacy_deletion_scheduled_message`, `privacy_deletion_scheduled_message_date`, `privacy_deletion_scheduled_title`, `privacy_deletion_scheduled_info`, `privacy_deletion_scheduled_cancel`.

**7. strings.xml (ES)** -- Translate all new strings to Spanish. Match the same string resource names. Key translations:
- "Delete Your Account" = "Eliminar Tu Cuenta"
- "DANGER" = "PELIGRO"
- "This action is permanent and cannot be undone." = "Esta accion es permanente y no se puede deshacer."
- "THIS WILL PERMANENTLY DELETE:" = "ESTO ELIMINARA PERMANENTEMENTE:"
- "deletion_scope" = "deletion_scope" (keep as code-style terminal text)
- "// items to be removed" = "// elementos a eliminar"
- Scope items: translate the text after the bullet marker
- "I Understand, Continue" = "Entiendo, Continuar"
- "Cancel" = "Cancelar"
- "Verify Your Identity" = "Verifica Tu Identidad"
- "Enter your password to confirm this action." = "Ingresa tu contrasena para confirmar esta accion."
- "PASSWORD" = "CONTRASENA"
- "Verify & Continue" = "Verificar y Continuar"
- "Help Us Improve" = "Ayudanos a Mejorar"
- "Optionally tell us why you are leaving." = "Opcionalmente cuentanos por que te vas."
- "FEEDBACK (OPTIONAL)" = "COMENTARIOS (OPCIONAL)"
- "Skip" = "Omitir"
- "Final Confirmation" = "Confirmacion Final"
- "Review the details below before confirming deletion" = "Revisa los detalles a continuacion antes de confirmar la eliminacion"
- "deletion_summary" = "deletion_summary" (keep as terminal text)
- "// scheduled for removal" = "// programado para eliminacion"
- "Delete My Account" = "Eliminar Mi Cuenta"
- "Deletion Scheduled" = "Eliminacion Programada"
- "Your deletion request has been confirmed." = "Tu solicitud de eliminacion ha sido confirmada."
- "Cancel Deletion Request" = "Cancelar Solicitud de Eliminacion"
- "Log Out Now" = "Cerrar Sesion Ahora"
- Desktop panel strings: translate all headers, titles, descriptions, bullets
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:compileKotlinJvm 2>&1 | tail -5</automated>
  </verify>
  <done>Model has userEmail field, SkipReason and LogOut intents exist, LoggedOut event exists, ViewModel handles all new intents, all EN and ES strings updated with no old unused strings remaining, project compiles.</done>
</task>

<task type="auto">
  <name>Task 2: Rewrite AccountDeletionScreen with Pencil design layouts</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt,
    app/privacy/wire/src/commonMain/kotlin/com/m2f/template/app/privacy/wire/PrivacyNavigation.kt
  </files>
  <action>
**Complete rewrite of AccountDeletionScreen.kt** to match all 5 Pencil design steps with mobile + desktop layouts.

**Screen signature change** -- Add new callbacks:
```kotlin
fun AccountDeletionScreen(
    state: AccountDeletionModel,
    onProceedToReAuth: () -> Unit,
    onReAuthenticate: (String) -> Unit,
    onSetReason: (String) -> Unit,
    onSkipReason: () -> Unit,        // NEW: Step 3 Skip button
    onConfirmDeletion: () -> Unit,
    onCancelDeletion: () -> Unit,
    onLogout: () -> Unit,            // NEW: Step 5 Log Out Now button
    onBack: () -> Unit,
)
```

**Top-level layout (BoxWithConstraints):**
- Mobile (<=840dp): Single column, full-width, padding 24dp. Shows loading/error at top, then step content.
- Desktop (>840dp): Row layout. Left column takes remaining space with max ~800dp, contains the main step flow. Right column is 480dp wide with `border-left` (1dp, colors.border), contains contextual information that varies per step.

**Step 1 -- Warning (mobile):**
- Title: "Delete Your Account" (fontSize 22, bold, letterSpacing -0.5, color = colors.error)
- TerminalAlert(variant = AlertVariant.Error, title = "[DANGER]" string, message = danger message string)
- Label: TerminalText with "THIS WILL PERMANENTLY DELETE:" (fontSize 10, letterSpacing 2, uppercase, color = colors.textDim)
- TerminalCard(title = "deletion_scope", description = "// items to be removed", variant = CardVariant.Default) with icon composable showing a TerminalText("X") or similar. Body content: 4 scope items as TerminalText lines with lineHeight 1.8. No footer.
- Grace period text as TerminalText (fontSize 13, color = colors.textDim)
- Buttons Row: "I Understand, Continue" (Destructive) + "Cancel" (Ghost)

**Step 1 -- Warning (desktop right panel):**
- Step indicator: "STEP 01 / 03 -- ACCOUNT DELETION" (fontSize 10, letterSpacing 2, color = colors.textDim)
- Header: "DELETION IMPACT" (fontSize 10, letterSpacing 2, color = colors.accent)
- Title: "What gets removed" (fontSize 16, bold)
- Description text
- TerminalDivider
- Bullet list (4 items)
- Warning note text

**Step 2 -- Re-Auth (mobile):**
- Title: "Verify Your Identity" (fontSize 22, bold)
- Subtitle: "Enter your password to confirm this action." (fontSize 13, color = textDim)
- TerminalPasswordInput with label = "PASSWORD" (uppercase)
- If state.error != null: TerminalAlert(variant = Error, title = "[ERROR]", message = "Invalid password...")
- Buttons: "Verify & Continue" (Destructive, enabled when password not blank) + "Back" (Ghost)

**Step 2 -- Desktop right panel:**
- Step indicator: "STEP 02 / 03 -- VERIFY IDENTITY"
- Header: "SECURITY NOTICE" / Title: "Why we verify" / Description / Bullets

**Step 3 -- Reason (mobile):**
- Title: "Help Us Improve" (fontSize 22, bold)
- Subtitle: "Optionally tell us why you are leaving." (fontSize 13, textDim)
- TerminalTextarea with label = "FEEDBACK (OPTIONAL)"
- 3 Buttons: "Continue" (Destructive, calls onSetReason with local text) + "Skip" (Ghost, calls onSkipReason) + "Back" (Ghost, calls onBack)

**Step 3 -- Desktop right panel:**
- Step indicator: "STEP 03 / 03 -- FEEDBACK"
- Header: "PRIVACY NOTE" / Title: "Your feedback" / Description / Bullets

**Step 4 -- Confirm (mobile):**
- Step label: "STEP 4 OF 5" (fontSize 10, letterSpacing 2, color = textDim)
- Title: "Final Confirmation" (fontSize 22, bold, color = error)
- Subtitle: "Review the details below before confirming deletion" (fontSize 12, textDim)
- TerminalCard(title = "deletion_summary", description = "// scheduled for removal", variant = CardVariant.Accent). Body: 3 lines -- "Account: {email}", "Scheduled deletion: {7 days from now}", "Grace period: 7 days". For the date, compute 7 days from now using kotlinx.datetime or display a placeholder date. Use stringResource with format args for email.
- TerminalAlert(variant = Warning, title = "[WARN]", message = "After {date}, this action cannot be undone.")
- Buttons: "Delete My Account" (Destructive) + "Cancel" (Ghost)

**Step 4 -- Desktop right panel:**
- Header: "DELETION IMPACT" / Bullets / Grace note

**Step 5 -- Scheduled (mobile):**
- Step label: "STEP 5 OF 5"
- TerminalAlert(variant = Success, title = "[SUCCESS] Deletion Scheduled", message = "Your deletion request has been confirmed.")
- TerminalCard(title = "deletion_scheduled", description = "// account marked for removal", variant = CardVariant.Info). Body: 3 info lines using scheduledAt from state.pendingDeletion.
- Buttons: "Cancel Deletion Request" (Default) + "Log Out Now" (Ghost, calls onLogout)

**Step 5 -- Desktop right panel:**
- Header: "WHAT HAPPENS NEXT" / Numbered steps / Cancel instructions

**Shared patterns:**
- Loading state: Show TerminalProgress at top of content when state.loading is true.
- Error state: Show TerminalAlert(Error) when state.error is non-null, displayed WITHIN each step where relevant (Step 2 for password errors) rather than globally at top.
- Use `Arrangement.spacedBy(20.dp)` for main column spacing.
- Desktop right panel: Column with padding 32dp, background = colors.bg.

**Helper composables to create:**
- `DesktopRightPanel(step: DeletionStep, state: AccountDeletionModel)` -- Renders the contextual right panel based on current step.
- Keep each step as a private composable function: `WarningStep(...)`, `ReAuthStep(...)`, `ReasonStep(...)`, `ConfirmStep(...)`, `ScheduledStep(...)`.

**PrivacyNavigation.kt update:**
Update the `entry<AccountDeletionRoute>` block to pass the two new callbacks:
```kotlin
onSkipReason = { viewModel.take(AccountDeletionIntent.SkipReason) },
onLogout = { viewModel.take(AccountDeletionIntent.LogOut) },
```
Also handle the new `AccountDeletionEvent.LoggedOut` event in the event collector:
```kotlin
is AccountDeletionEvent.LoggedOut -> {
    onAccountDeleted()
}
```
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:compileKotlinJvm :app:privacy:wire:compileKotlinJvm 2>&1 | tail -5</automated>
  </verify>
  <done>AccountDeletionScreen renders all 5 steps matching Pencil designs. Mobile shows single-column flow. Desktop shows two-column layout with contextual right panel (480dp, border-left). All buttons use correct variants (Destructive/Ghost/Default per design). TerminalCard used in Steps 1 (Default), 4 (Accent), 5 (Info). Step indicators shown on Steps 4-5. PrivacyNavigation wires onSkipReason, onLogout callbacks and handles LoggedOut event. Compiles successfully.</done>
</task>

<task type="auto">
  <name>Task 3: Run tests and verify compilation across all targets</name>
  <files>
    app/privacy/impl/src/commonTest/kotlin/com/m2f/template/app/privacy/AccountDeletionViewModelTest.kt
  </files>
  <action>
**1. Run existing AccountDeletionViewModelTest** to check if model changes break tests. Read the test file first.

**2. Fix any broken tests** caused by:
- New `userEmail` field in AccountDeletionModel (all model assertions must include `userEmail = ""` or the fetched value)
- New intents (SkipReason, LogOut) -- add test cases
- New event (LoggedOut) -- verify it fires on LogOut intent
- The ViewModel now calls `sdk.getProfile()` in `handleLoad()` -- existing tests must provide a fake getProfile response

**3. Add new test cases:**
- `skip reason moves to confirm step` -- SkipReason intent sets empty reason and advances to CONFIRM
- `log out triggers logout and navigates` -- LogOut intent calls sdk.logout() and sends LoggedOut event
- `load fetches user email` -- After Load, model.userEmail should contain the profile email

**4. Run full test suite** to verify nothing else is broken.

**5. Compile for JVM desktop target** to ensure Compose code compiles:
```
./gradlew :app:privacy:impl:allTests
./gradlew :composeApp:compileKotlinJvm
```
  </action>
  <verify>
    <automated>cd /Users/marc/IdeaProjects/Template && ./gradlew :app:privacy:impl:allTests :composeApp:compileKotlinJvm 2>&1 | tail -10</automated>
  </verify>
  <done>All existing tests pass with updated model assertions. New tests for SkipReason, LogOut, and email loading pass. Full project compiles for JVM target.</done>
</task>

</tasks>

<verification>
1. `./gradlew :app:privacy:impl:allTests` -- All ViewModel tests pass
2. `./gradlew :composeApp:compileKotlinJvm` -- Full Compose compilation succeeds
3. Grep for old removed string resource names to confirm no leftover references:
   `grep -r "privacy_deletion_warning_title\|privacy_deletion_go_back\|privacy_deletion_warning_consider" app/privacy/impl/src/`
</verification>

<success_criteria>
- All 5 deletion steps render per Pencil designs (mobile layout)
- Desktop shows two-column layout with contextual right panel
- TerminalCard variants used correctly: Default (Step 1), Accent (Step 4), Info (Step 5)
- Button variants match designs: Destructive for primary actions, Ghost for secondary/back
- Step 3 has Skip + Back + Continue buttons
- Step 5 has "Log Out Now" button that triggers logout
- All EN + ES translations present and correct
- Existing tests updated and passing
- New test coverage for SkipReason, LogOut, email loading
</success_criteria>

<output>
After completion, create `.planning/quick/8-redesign-delete-account-flow-to-match-pe/8-SUMMARY.md`
</output>
