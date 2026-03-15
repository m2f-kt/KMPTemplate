---
phase: quick-3
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/profile/impl/src/commonMain/composeResources/values-es/strings.xml
  - app/privacy/impl/src/commonMain/composeResources/values/strings.xml
  - app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateScreen.kt
  - app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/LegalDocumentScreen.kt
autonomous: true
requirements: [QUICK-3]
must_haves:
  truths:
    - "All privacy module screens use stringResource() instead of hardcoded English strings"
    - "Profile module Spanish translations are complete (no missing entries vs English)"
    - "Privacy module has both English and Spanish string resource files"
  artifacts:
    - path: "app/privacy/impl/src/commonMain/composeResources/values/strings.xml"
      provides: "English string resources for all 4 privacy screens"
    - path: "app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml"
      provides: "Spanish translations for all privacy screen strings"
    - path: "app/profile/impl/src/commonMain/composeResources/values-es/strings.xml"
      provides: "Complete Spanish translations including 7 missing entries"
  key_links:
    - from: "PrivacySettingsScreen.kt"
      to: "values/strings.xml"
      via: "stringResource(Res.string.xxx)"
      pattern: "stringResource\\(Res\\.string\\."
    - from: "AccountDeletionScreen.kt"
      to: "values/strings.xml"
      via: "stringResource(Res.string.xxx)"
      pattern: "stringResource\\(Res\\.string\\."
    - from: "ConsentGateScreen.kt"
      to: "values/strings.xml"
      via: "stringResource(Res.string.xxx)"
      pattern: "stringResource\\(Res\\.string\\."
    - from: "LegalDocumentScreen.kt"
      to: "values/strings.xml"
      via: "stringResource(Res.string.xxx)"
      pattern: "stringResource\\(Res\\.string\\."
---

<objective>
Extract all hardcoded English strings from the privacy module's 4 screen files into Compose string resources, create corresponding Spanish translations, and add the 7 missing Spanish translations to the profile module.

Purpose: Complete i18n coverage for privacy and profile modules so the app displays correctly in both English and Spanish.
Output: String resource XML files (EN + ES) for privacy module, updated ES strings for profile module, refactored screen files using stringResource().
</objective>

<execution_context>
@/Users/marc/.claude/get-shit-done/workflows/execute-plan.md
@/Users/marc/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@app/profile/impl/src/commonMain/composeResources/values/strings.xml
@app/profile/impl/src/commonMain/composeResources/values-es/strings.xml
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateScreen.kt
@app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/LegalDocumentScreen.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create privacy module string resources (EN + ES) and add missing profile ES translations</name>
  <files>
    app/privacy/impl/src/commonMain/composeResources/values/strings.xml
    app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml
    app/profile/impl/src/commonMain/composeResources/values-es/strings.xml
  </files>
  <action>
    **1a. Create English strings.xml for privacy module.**

    Create directory `app/privacy/impl/src/commonMain/composeResources/values/` and write `strings.xml` with ALL hardcoded strings from the 4 screen files. Use naming convention `privacy_` prefix. Group by screen with XML comment headers.

    String keys to create (exhaustive list from reading the 4 source files):

    ```
    <!-- PrivacySettingsScreen -->
    privacy_settings_title = "Privacy Settings"
    privacy_settings_back = "Back"
    privacy_settings_loading = "Loading privacy settings..."
    privacy_settings_error_title = "Error"
    privacy_settings_consents_header = "Your Consents"
    privacy_settings_export_header = "Data Export"
    privacy_settings_export_desc = "Request a copy of all your personal data."
    privacy_settings_export_status = "Export status:"
    privacy_settings_export_download = "Download Export"
    privacy_settings_export_request = "Request Data Export"
    privacy_settings_delete_header = "Delete Account"
    privacy_settings_delete_desc = "Permanently delete your account and all associated data. This action cannot be undone."
    privacy_settings_delete_button = "Delete My Account"
    privacy_consent_required_badge = "Required"
    privacy_consent_view = "View"
    privacy_consent_granted_prefix = "Granted: %1$s"  (use string format param for date)

    <!-- AccountDeletionScreen -->
    privacy_deletion_title = "Delete Account"
    privacy_deletion_processing = "Processing..."
    privacy_deletion_error_title = "Error"
    privacy_deletion_warning_title = "Warning"
    privacy_deletion_warning_message = "This action is permanent and cannot be undone. All your personal data, documents, and account information will be permanently deleted after a grace period."
    privacy_deletion_warning_consider = "Before proceeding, please consider:"
    privacy_deletion_warning_bullet1 = "  - Download your data export first"
    privacy_deletion_warning_bullet2 = "  - This cannot be reversed after the grace period"
    privacy_deletion_warning_bullet3 = "  - All active sessions will be terminated"
    privacy_deletion_go_back = "Go Back"
    privacy_deletion_continue = "Continue"
    privacy_deletion_reauth_message = "Please verify your identity by entering your password."
    privacy_deletion_reauth_label = "Password"
    privacy_deletion_reauth_placeholder = "Enter your password"
    privacy_deletion_reauth_verify = "Verify"
    privacy_deletion_reason_message = "Optionally, let us know why you are leaving. This helps us improve our service."
    privacy_deletion_reason_label = "Reason (optional)"
    privacy_deletion_reason_placeholder = "Tell us why you're leaving..."
    privacy_deletion_confirm_message = "You are about to permanently delete your account. This is your last chance to cancel."
    privacy_deletion_confirm_title = "Final Confirmation"
    privacy_deletion_confirm_question = "Are you absolutely sure?"
    privacy_deletion_confirm_cancel = "Cancel"
    privacy_deletion_confirm_button = "Delete My Account"
    privacy_deletion_scheduled_message = "Your account has been scheduled for deletion."
    privacy_deletion_scheduled_message_date = " It will be permanently removed on %1$s."  (format param for date)
    privacy_deletion_scheduled_title = "Deletion Scheduled"
    privacy_deletion_scheduled_info = "You can cancel this request before the scheduled date to keep your account."
    privacy_deletion_scheduled_cancel = "Cancel Deletion"

    <!-- ConsentGateScreen -->
    privacy_gate_title = "Privacy Policy Agreement"
    privacy_gate_subtitle = "Please review and accept the following to continue."
    privacy_gate_loading = "Loading consent requirements..."
    privacy_gate_error_title = "Error"
    privacy_gate_accept_all = "Accept All"
    privacy_gate_decline = "Decline"
    privacy_gate_read = "Read"

    <!-- ConsentGateScreen + PrivacySettingsScreen (shared consentTypeLabel function) -->
    privacy_consent_type_privacy_policy = "Privacy Policy"
    privacy_consent_type_terms_of_service = "Terms of Service"
    privacy_consent_type_marketing = "Marketing Communications"
    privacy_consent_type_analytics = "Analytics &amp; Usage Data"  (escape ampersand for XML)

    <!-- LegalDocumentScreen -->
    privacy_document_back = "Back"
    privacy_document_loading = "Loading document..."
    privacy_document_error_title = "Error"
    privacy_document_published_prefix = "Published: %1$s"  (format param for date)
    ```

    **1b. Create Spanish strings.xml for privacy module.**

    Create directory `app/privacy/impl/src/commonMain/composeResources/values-es/` and write `strings.xml` with Spanish translations for every key above. Use natural, contextual Spanish (match the tone of the existing profile ES translations -- informal "tu" form).

    Spanish translations:
    - "Privacy Settings" -> "Configuracion de Privacidad"
    - "Back" -> "Atras" (use &lt; atrás pattern if profile uses it, but here it is a button label so just "Atras")
    - "Loading privacy settings..." -> "Cargando configuracion de privacidad..."
    - "Error" -> "Error"
    - "Your Consents" -> "Tus Consentimientos"
    - "Data Export" -> "Exportacion de Datos"
    - "Request a copy of all your personal data." -> "Solicita una copia de todos tus datos personales."
    - "Export status:" -> "Estado de exportacion:"
    - "Download Export" -> "Descargar Exportacion"
    - "Request Data Export" -> "Solicitar Exportacion de Datos"
    - "Delete Account" -> "Eliminar Cuenta"
    - "Permanently delete..." -> "Elimina permanentemente tu cuenta y todos los datos asociados. Esta accion no se puede deshacer."
    - "Delete My Account" -> "Eliminar Mi Cuenta"
    - "Required" -> "Obligatorio"
    - "View" -> "Ver"
    - "Granted: %1$s" -> "Otorgado: %1$s"
    - "Processing..." -> "Procesando..."
    - "Warning" -> "Advertencia"
    - Warning message -> full Spanish translation
    - "Before proceeding..." -> "Antes de continuar, considera:"
    - 3 bullets -> Spanish translations
    - "Go Back" -> "Volver"
    - "Continue" -> "Continuar"
    - "Please verify..." -> "Verifica tu identidad ingresando tu contrasena."
    - "Password" -> "Contrasena"
    - "Enter your password" -> "Ingresa tu contrasena"
    - "Verify" -> "Verificar"
    - Reason message -> Spanish translation
    - "Reason (optional)" -> "Razon (opcional)"
    - "Tell us why..." -> "Cuentanos por que te vas..."
    - Confirm message -> Spanish translation
    - "Final Confirmation" -> "Confirmacion Final"
    - "Are you absolutely sure?" -> "Estas absolutamente seguro?"
    - "Cancel" -> "Cancelar"
    - Scheduled messages -> Spanish translations
    - "Deletion Scheduled" -> "Eliminacion Programada"
    - "Cancel Deletion" -> "Cancelar Eliminacion"
    - "Privacy Policy Agreement" -> "Acuerdo de Politica de Privacidad"
    - "Please review..." -> "Revisa y acepta lo siguiente para continuar."
    - "Loading consent requirements..." -> "Cargando requisitos de consentimiento..."
    - "Accept All" -> "Aceptar Todo"
    - "Decline" -> "Rechazar"
    - "Read" -> "Leer"
    - ConsentType labels -> "Politica de Privacidad", "Terminos de Servicio", "Comunicaciones de Marketing", "Analiticas y Datos de Uso"
    - "Loading document..." -> "Cargando documento..."
    - "Published: %1$s" -> "Publicado: %1$s"

    IMPORTANT: Use proper accented characters (a with accent, o with accent, n with tilde) in the actual XML. The action above shows simplified for readability but the actual file MUST use UTF-8 accented characters (e.g., "Configuracion" -> "Configuración", "Atrás", "Razón", "están", etc.).

    **1c. Add 7 missing entries to profile module Spanish strings.**

    Edit `app/profile/impl/src/commonMain/composeResources/values-es/strings.xml` to add these entries after `profile_logout` (line 23), before the error messages section:

    ```xml
    <string name="profile_avatar_tap_hint">Toca para cambiar avatar</string>
    <string name="profile_avatar_uploading">Subiendo avatar…</string>
    <string name="profile_crop_dialog_title">Recortar Imagen</string>
    <string name="profile_crop_dialog_desc">// ajusta tu foto de perfil</string>
    <string name="profile_crop_confirm">&gt; confirmar</string>
    <string name="profile_crop_cancel">cancelar</string>
    ```

    Also add the `<!-- Nav: Common -->` section header and `sidebar_nav_privacy` entry before the `<!-- Nav: Free tier -->` line (around line 95):

    ```xml
    <!-- Nav: Common (all tiers) -->
    <string name="sidebar_nav_privacy">Privacidad y Datos</string>
    ```
  </action>
  <verify>
    Verify all 3 XML files are well-formed: `xmllint --noout app/privacy/impl/src/commonMain/composeResources/values/strings.xml app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml app/profile/impl/src/commonMain/composeResources/values-es/strings.xml` (install xmllint via `brew install libxml2` if needed, or use `python3 -c "import xml.etree.ElementTree as ET; ET.parse('path')"` for each file). Also verify key counts match between EN and ES for privacy module.
  </verify>
  <done>
    - Privacy module has values/strings.xml with all extracted English strings (~56 entries)
    - Privacy module has values-es/strings.xml with matching Spanish translations (~56 entries)
    - Profile module values-es/strings.xml has all 7 missing entries plus the Nav: Common section header
    - All XML files are well-formed
  </done>
</task>

<task type="auto">
  <name>Task 2: Refactor privacy screens to use stringResource() instead of hardcoded strings</name>
  <files>
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/PrivacySettingsScreen.kt
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/AccountDeletionScreen.kt
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/ConsentGateScreen.kt
    app/privacy/impl/src/commonMain/kotlin/com/m2f/template/app/privacy/LegalDocumentScreen.kt
  </files>
  <action>
    Add these imports to ALL 4 screen files:
    ```kotlin
    import org.jetbrains.compose.resources.stringResource
    import template.app.privacy.generated.resources.Res
    import template.app.privacy.generated.resources.*
    ```

    **PrivacySettingsScreen.kt** -- Replace all hardcoded strings:
    - `"Privacy Settings"` -> `stringResource(Res.string.privacy_settings_title)`
    - `"Back"` -> `stringResource(Res.string.privacy_settings_back)`
    - `"Loading privacy settings..."` -> `stringResource(Res.string.privacy_settings_loading)`
    - `"Error"` -> `stringResource(Res.string.privacy_settings_error_title)`
    - `"Your Consents"` in SectionHeader -> `stringResource(Res.string.privacy_settings_consents_header)`
    - `"Data Export"` in SectionHeader -> `stringResource(Res.string.privacy_settings_export_header)`
    - `"Request a copy..."` -> `stringResource(Res.string.privacy_settings_export_desc)`
    - `"Export status:"` -> `stringResource(Res.string.privacy_settings_export_status)`
    - `"Download Export"` -> `stringResource(Res.string.privacy_settings_export_download)`
    - `"Request Data Export"` -> `stringResource(Res.string.privacy_settings_export_request)`
    - `"Delete Account"` in SectionHeader -> `stringResource(Res.string.privacy_settings_delete_header)`
    - `"Permanently delete..."` -> `stringResource(Res.string.privacy_settings_delete_desc)`
    - `"Delete My Account"` -> `stringResource(Res.string.privacy_settings_delete_button)`
    - `"Required"` badge -> `stringResource(Res.string.privacy_consent_required_badge)`
    - `"View"` button -> `stringResource(Res.string.privacy_consent_view)`
    - `"Granted: ${consent.grantedAt}"` -> `stringResource(Res.string.privacy_consent_granted_prefix, consent.grantedAt ?: "")`

    Note: The `SectionHeader` composable takes a `title: String` param. Either pass `stringResource()` at the call site (preferred -- keep SectionHeader simple), or the `consentTypeLabel()` function needs refactoring (see below).

    **consentTypeLabel() function** (in ConsentGateScreen.kt, used by both ConsentGateScreen and PrivacySettingsScreen):
    This is a non-composable function returning String. It needs to become a `@Composable` function to use `stringResource()`:
    ```kotlin
    @Composable
    internal fun consentTypeLabel(type: ConsentType): String = when (type) {
        ConsentType.PRIVACY_POLICY -> stringResource(Res.string.privacy_consent_type_privacy_policy)
        ConsentType.TERMS_OF_SERVICE -> stringResource(Res.string.privacy_consent_type_terms_of_service)
        ConsentType.MARKETING -> stringResource(Res.string.privacy_consent_type_marketing)
        ConsentType.ANALYTICS -> stringResource(Res.string.privacy_consent_type_analytics)
    }
    ```
    Since it is already called from `@Composable` contexts (ConsentRow and ConsentStatusRow), adding `@Composable` annotation is safe.

    **AccountDeletionScreen.kt** -- Replace all hardcoded strings:
    - `"Delete Account"` title -> `stringResource(Res.string.privacy_deletion_title)`
    - `"Processing..."` -> `stringResource(Res.string.privacy_deletion_processing)`
    - `"Error"` -> `stringResource(Res.string.privacy_deletion_error_title)`
    - WarningStep: `"Warning"` title -> `stringResource(Res.string.privacy_deletion_warning_title)`
    - Warning message string -> `stringResource(Res.string.privacy_deletion_warning_message)`
    - `"Before proceeding..."` -> `stringResource(Res.string.privacy_deletion_warning_consider)`
    - 3 bullet texts -> `stringResource(Res.string.privacy_deletion_warning_bullet1/2/3)`
    - `"Go Back"` buttons (appears in Warning + ReAuth steps) -> `stringResource(Res.string.privacy_deletion_go_back)`
    - `"Continue"` buttons (Warning + Reason steps) -> `stringResource(Res.string.privacy_deletion_continue)`
    - ReAuthStep: verify identity message -> `stringResource(Res.string.privacy_deletion_reauth_message)`
    - `"Password"` label -> `stringResource(Res.string.privacy_deletion_reauth_label)`
    - `"Enter your password"` placeholder -> `stringResource(Res.string.privacy_deletion_reauth_placeholder)`
    - `"Verify"` button -> `stringResource(Res.string.privacy_deletion_reauth_verify)`
    - ReasonStep: message -> `stringResource(Res.string.privacy_deletion_reason_message)`
    - `"Reason (optional)"` label -> `stringResource(Res.string.privacy_deletion_reason_label)`
    - `"Tell us why..."` placeholder -> `stringResource(Res.string.privacy_deletion_reason_placeholder)`
    - ConfirmStep: message -> `stringResource(Res.string.privacy_deletion_confirm_message)`
    - `"Final Confirmation"` title -> `stringResource(Res.string.privacy_deletion_confirm_title)`
    - `"Are you absolutely sure?"` -> `stringResource(Res.string.privacy_deletion_confirm_question)`
    - `"Cancel"` button -> `stringResource(Res.string.privacy_deletion_confirm_cancel)`
    - `"Delete My Account"` button -> `stringResource(Res.string.privacy_deletion_confirm_button)`
    - ScheduledStep: Build the message using: if scheduledAt != null, use `stringResource(Res.string.privacy_deletion_scheduled_message) + stringResource(Res.string.privacy_deletion_scheduled_message_date, scheduledAt)`, else just `stringResource(Res.string.privacy_deletion_scheduled_message)`
    - `"Deletion Scheduled"` title -> `stringResource(Res.string.privacy_deletion_scheduled_title)`
    - `"You can cancel..."` -> `stringResource(Res.string.privacy_deletion_scheduled_info)`
    - `"Cancel Deletion"` -> `stringResource(Res.string.privacy_deletion_scheduled_cancel)`

    **ConsentGateScreen.kt** -- Replace all hardcoded strings:
    - `"Privacy Policy Agreement"` -> `stringResource(Res.string.privacy_gate_title)`
    - `"Please review..."` -> `stringResource(Res.string.privacy_gate_subtitle)`
    - `"Loading consent requirements..."` -> `stringResource(Res.string.privacy_gate_loading)`
    - `"Error"` -> `stringResource(Res.string.privacy_gate_error_title)`
    - `"Accept All"` -> `stringResource(Res.string.privacy_gate_accept_all)`
    - `"Decline"` -> `stringResource(Res.string.privacy_gate_decline)`
    - `"Read"` -> `stringResource(Res.string.privacy_gate_read)`
    - `consentTypeLabel()` function becomes `@Composable` (see above)

    **LegalDocumentScreen.kt** -- Replace all hardcoded strings:
    - `"Back"` -> `stringResource(Res.string.privacy_document_back)`
    - `"Loading document..."` -> `stringResource(Res.string.privacy_document_loading)`
    - `"Error"` -> `stringResource(Res.string.privacy_document_error_title)`
    - `"Published: ${state.document.publishedAt}"` -> `stringResource(Res.string.privacy_document_published_prefix, state.document.publishedAt)`
    - `consentTypeLabel()` is called here too -- it is defined in ConsentGateScreen.kt with `internal` visibility, so it is accessible within the same module. No change needed for this call site.
  </action>
  <verify>
    Run `./gradlew :app:privacy:impl:compileKotlinDesktop` to verify the Kotlin files compile with the new stringResource() calls and the generated Res class resolves all string keys. If compileKotlinDesktop is not available, try `./gradlew :app:privacy:impl:allTests` or `./gradlew :app:privacy:impl:compileCommonMainKotlinMetadata`.
  </verify>
  <done>
    - Zero hardcoded English strings remain in the 4 privacy screen files (grep for quoted English text returns none except non-translatable values like "en", "es", format strings)
    - All screen files import stringResource and Res
    - consentTypeLabel() function is @Composable and uses stringResource()
    - Project compiles successfully
  </done>
</task>

</tasks>

<verification>
1. `grep -rn '"[A-Z][a-z]' app/privacy/impl/src/commonMain/kotlin/ | grep -v 'import\|package\|//\|ConsentType\.\|DeletionStep\.\|ExportStatus\.\|BadgeVariant\.\|AlertVariant\.\|ButtonVariant\.\|"en"\|"es"'` -- should return no hardcoded English UI strings
2. `python3 -c "import xml.etree.ElementTree as ET; en=ET.parse('app/privacy/impl/src/commonMain/composeResources/values/strings.xml'); es=ET.parse('app/privacy/impl/src/commonMain/composeResources/values-es/strings.xml'); en_keys=sorted(s.get('name') for s in en.findall('.//string')); es_keys=sorted(s.get('name') for s in es.findall('.//string')); assert en_keys==es_keys, f'Mismatch: EN={len(en_keys)} ES={len(es_keys)}'"` -- EN and ES key counts match
3. `./gradlew :app:privacy:impl:compileCommonMainKotlinMetadata` compiles without errors
</verification>

<success_criteria>
- All privacy module screens use stringResource(Res.string.xxx) for every user-visible string
- Privacy module has values/strings.xml (English) and values-es/strings.xml (Spanish) with matching keys
- Profile module values-es/strings.xml has all 7 previously missing translations plus Nav: Common section
- Project compiles successfully
</success_criteria>

<output>
After completion, create `.planning/quick/3-add-translations-for-privacy-policy-and-/3-SUMMARY.md`
</output>
