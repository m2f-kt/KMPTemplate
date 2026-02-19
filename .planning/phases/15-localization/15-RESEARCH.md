# Phase 15: Localization — Research

**Phase Goal:** Extract all hardcoded English strings into Compose Multiplatform resource files, introduce a shared `StringKey` enum bridging server error codes and client UI strings, add server-side Accept-Language header support for localized error messages, and enable runtime locale switching on all KMP targets.

**Depends on:** Phase 12 (ViewModel MVI migration), Phase 14 (Group Admin UI — all screens exist)

**Requirements:** L10N-01, L10N-02, L10N-03, L10N-04, L10N-05, L10N-06

---

## 1. Standard Stack

### Core (Already in Project)

| Library | Version | Purpose | Status |
|---------|---------|---------|--------|
| Compose Multiplatform Resources | 1.10.1 | `stringResource(Res.string.*)`, locale-qualified `values-XX/strings.xml` | Already depended on in `app:designsystem` and `composeApp` |
| Kotlin Serialization | (project) | `@Serializable` for StringKey enum wire format | Already used everywhere |

### No New Dependencies Needed

Compose Multiplatform 1.10.1 includes full multiplatform string resource support with locale qualifiers, plurals, and template arguments. The `compose.components.resources` dependency is already present in `app:designsystem` and `composeApp`. Feature modules (`app:auth`, `app:admin`, `app:dashboard`, `app:profile`) need it added to their `build.gradle.kts`.

The server side needs no new dependencies — localized string maps are plain Kotlin maps.

---

## 2. Existing Hardcoded Strings (Inventory)

### 2.1 ViewModel Validation Strings

**ValidationSupport.kt** (`core/models/src/commonMain/kotlin/.../validation/ValidationSupport.kt`):
- `"Email must not be blank"` (line 13)
- `"Email format is invalid"` (line 15)
- `"Password must be at least 8 characters"` (line 26)
- `"Name must not be blank"` (line 36)
- `"Name must be between 2 and 100 characters"` (line 38)
- `"$fieldName must not be blank"` (line 48 — dynamic field name)

**LoginViewModel.kt** (`app/auth/.../LoginViewModel.kt:30-37`):
- `"Email must not be blank"` (line 31)
- `"Email format is invalid"` (line 32)
- `"Password must not be blank"` (line 35)

**ProfileViewModel.kt** (`app/profile/.../ProfileViewModel.kt:57-64`):
- `"Name is required"` (line 58)
- `"Email is required"` (line 61)
- `"Invalid email format"` (line 63)

**RegisterMemberViewModel.kt** (`app/admin/.../RegisterMemberViewModel.kt`) — uses `validateName`, `validateEmail`, `validatePassword` from ValidationSupport, plus field remapping via `withError`.

### 2.2 AppError Default Messages

**AppError.kt** (`core/models/.../AppError.kt`) — every sealed subclass has a hardcoded English `message` default:
- `AUTH_INVALID_CREDENTIALS` -> `"Email or password is incorrect"`
- `AUTH_TOKEN_EXPIRED` -> `"Authentication token has expired"`
- `AUTH_UNAUTHORIZED` -> `"Authentication required"`
- `AUTH_USER_ALREADY_EXISTS` -> `"A user with this email already exists"`
- `USER_NOT_FOUND` -> `"User not found"`
- `USER_FORBIDDEN` -> `"You do not have permission to access this resource"`
- `GROUP_NOT_FOUND` -> `"Group not found"`
- `GROUP_FORBIDDEN` -> `"You do not have permission to access this group"`
- Plus ~10 more across Client, Server, Validation, AI, Group subhierarchies

**Key insight:** `AppError.code` values (e.g., `"AUTH_INVALID_CREDENTIALS"`) are already structured as localization keys. The `StringKey` enum should mirror these.

### 2.3 Server DomainError Messages

**AuthErrors.kt** (`server/auth/.../AuthErrors.kt`) — duplicates of AppError messages:
- `InvalidCredentials.detail` = `"Email or password is incorrect"`
- `UserAlreadyExists.detail` = `"A user with this email already exists"`
- etc.

**GroupErrors.kt** (`server/groups/.../GroupErrors.kt`):
- `GroupNotFound.detail` = `"Group not found"`
- `GroupForbidden.detail` = `"You do not have permission to access this group"`
- `MemberAlreadyInGroup.detail` = `"User is already a member of this group"`
- `CannotRemoveOwner.detail` = `"Cannot remove the group owner"`
- `MemberNotInGroup.detail` = `"User is not a member of this group"`

**Error flow:** `DomainError.respond()` calls helpers like `routingContext.unauthorized(error.code, error.message)` which sends `ErrorResponse(code, message)` to the client. The `ErrorMapper.kt` on the SDK side preserves `errorResponse?.message` in mapped errors.

### 2.4 Screen UI Strings

Every screen has extensive hardcoded English text. Example from **LoginScreen.kt**:
- `"terminal"`, `"$ authenticate --user"`, `"Welcome back"`, `"Sign in to your workspace"`
- `"$ authenticate"`, `"// enter your credentials to continue"`
- `"username"`, `"enter_username"`, `"password"`
- `"--remember-me"`, `"$ reset_password"`, `"$ authenticating..."`, `"$ login()"`
- `"or"`, `"Google"`, `"Apple"`, `"// new user?"`, `"$ create_account()"`
- ASCII art block, brand text, status line, version, quotes

Similar patterns exist in RegisterScreen, ForgotPasswordScreen, DashboardScreen, DashboardSidebar, ProfileScreen, AdminPanelScreen, RegisterMemberScreen.

**Rough count:** ~100-150 unique UI strings across all screens.

---

## 3. Architecture Patterns

### 3.1 StringKey Enum (L10N-01)

A shared `StringKey` enum in `core:models` that bridges server error codes and client string resource references.

**Design:**
```kotlin
// core/models/.../localization/StringKey.kt
enum class StringKey(val code: String) {
    // Auth errors (mirror AppError.code values)
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS"),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED"),
    AUTH_UNAUTHORIZED("AUTH_UNAUTHORIZED"),
    AUTH_USER_ALREADY_EXISTS("AUTH_USER_ALREADY_EXISTS"),

    // Validation errors
    VALIDATION_EMAIL_BLANK("VALIDATION_EMAIL_BLANK"),
    VALIDATION_EMAIL_INVALID("VALIDATION_EMAIL_INVALID"),
    VALIDATION_PASSWORD_TOO_SHORT("VALIDATION_PASSWORD_TOO_SHORT"),
    VALIDATION_NAME_BLANK("VALIDATION_NAME_BLANK"),
    VALIDATION_NAME_LENGTH("VALIDATION_NAME_LENGTH"),
    VALIDATION_FIELD_REQUIRED("VALIDATION_FIELD_REQUIRED"),

    // Group errors
    GROUP_NOT_FOUND("GROUP_NOT_FOUND"),
    GROUP_FORBIDDEN("GROUP_FORBIDDEN"),
    // ...

    // UI strings (optional — only if ViewModels need them)
    // Most UI strings stay in Compose resources and are accessed directly in screens.
    ;

    companion object {
        fun fromCode(code: String): StringKey? = entries.find { it.code == code }
    }
}
```

**Scope decision:** StringKey covers error/validation messages that flow through ViewModels. Pure UI strings (labels, placeholders, button text) go directly in `strings.xml` and are accessed via `stringResource(Res.string.*)` in composables — they do NOT need StringKey entries.

**Confidence:** HIGH — this is the simplest approach that satisfies L10N-01 without over-engineering.

### 3.2 Compose Resource Files (L10N-02, L10N-04)

**Directory structure:**
```
composeApp/src/commonMain/composeResources/
  values/
    strings.xml          # Default (English)
  values-es/
    strings.xml          # Spanish (example second locale)
```

**Format:**
```xml
<resources>
    <!-- Auth screen -->
    <string name="login_title">$ authenticate</string>
    <string name="login_subtitle">// enter your credentials to continue</string>
    <string name="login_email_label">username</string>
    <string name="login_email_placeholder">enter_username</string>
    <string name="login_button">$ login()</string>
    <string name="login_button_loading">$ authenticating...</string>

    <!-- Error messages (keyed to match StringKey codes) -->
    <string name="error_auth_invalid_credentials">Email or password is incorrect</string>
    <string name="error_validation_email_blank">Email must not be blank</string>
    <string name="error_validation_password_too_short">Password must be at least %1$d characters</string>

    <!-- Shared -->
    <string name="app_name">terminal</string>
</resources>
```

**Where to put strings.xml:**
- **Option A (recommended for template):** All strings in `composeApp/src/commonMain/composeResources/values/strings.xml`. Single source, simplest.
- **Option B (per-module):** Each feature module has its own `composeResources/values/strings.xml`. More modular but requires `compose.components.resources` in every module's build.gradle.kts and generates separate `Res` classes per module.

**Recommendation:** Option A — single `strings.xml` in `composeApp`. The template is small enough that a single file is maintainable, and it avoids the complexity of cross-module Res class imports. Feature modules pass resolved strings from the composable layer (which lives in `composeApp/AppNavHost.kt`) down to screens.

**Access pattern:**
```kotlin
// In composables (screens):
stringResource(Res.string.login_title)

// In non-composable contexts (e.g., ViewModel):
// NOT recommended — ViewModels should pass StringKey, screens resolve to strings
```

**Confidence:** HIGH — this is the standard Compose Multiplatform approach.

### 3.3 StringKey to Res Bridge Function (L10N-06)

A composable or mapping function in `composeApp` that maps `StringKey` to the corresponding `Res.string.*` accessor:

```kotlin
// composeApp/.../localization/StringKeyResolver.kt
@Composable
fun stringResource(key: StringKey, vararg args: Any): String =
    when (key) {
        StringKey.AUTH_INVALID_CREDENTIALS -> stringResource(Res.string.error_auth_invalid_credentials)
        StringKey.VALIDATION_EMAIL_BLANK -> stringResource(Res.string.error_validation_email_blank)
        StringKey.VALIDATION_PASSWORD_TOO_SHORT -> stringResource(Res.string.error_validation_password_too_short, *args)
        // ... exhaustive when
    }
```

**Why manual mapping:** The REQUIREMENTS.md explicitly lists "Code-generated string bridge" as out of scope. A manual `when` expression over the `StringKey` enum is ~30-40 lines and provides compile-time exhaustiveness checking.

**Confidence:** HIGH

### 3.4 ViewModel Migration Pattern

**Before (current):**
```kotlin
// LoginViewModel.kt
val emailError = when {
    current.email.isBlank() -> "Email must not be blank"
    ...
}
```

**After:**
```kotlin
val emailError = when {
    current.email.isBlank() -> StringKey.VALIDATION_EMAIL_BLANK
    ...
}
```

**Model change:** Error fields change from `String?` to `StringKey?`:
```kotlin
// Before
data class LoginModel(val emailError: String? = null, ...)

// After
data class LoginModel(val emailError: StringKey? = null, ...)
```

**Screen change:** Screens resolve StringKey to localized string:
```kotlin
// Before
errorMessage = state.emailError

// After
errorMessage = state.emailError?.let { stringResource(it) }
```

**Server error handling change:**
```kotlin
// Before
sdk.login(request).fold(
    ifLeft = { error -> sendMutation(SetServerError(error.message)) },
    ...
)

// After
sdk.login(request).fold(
    ifLeft = { error ->
        val key = StringKey.fromCode(error.code) ?: StringKey.GENERIC_ERROR
        sendMutation(SetServerError(key))
    },
    ...
)
```

**Confidence:** HIGH — straightforward refactor that preserves the existing MVI pattern.

### 3.5 Server-Side Localization (L10N-03)

**Approach:** Parse `Accept-Language` header, resolve localized messages from a server-side string map, return in `ErrorResponse.message`.

**Implementation:**
```kotlin
// server/core/config/.../localization/ServerStrings.kt
object ServerStrings {
    private val strings: Map<String, Map<String, String>> = mapOf(
        "en" to mapOf(
            "AUTH_INVALID_CREDENTIALS" to "Email or password is incorrect",
            "GROUP_NOT_FOUND" to "Group not found",
            // ...
        ),
        "es" to mapOf(
            "AUTH_INVALID_CREDENTIALS" to "El correo o la contraseña son incorrectos",
            "GROUP_NOT_FOUND" to "Grupo no encontrado",
            // ...
        ),
    )

    fun resolve(code: String, locale: String): String {
        val lang = locale.take(2).lowercase()
        return strings[lang]?.get(code) ?: strings["en"]?.get(code) ?: code
    }
}
```

**Integration point:** Modify `DomainError.respond()` to use the request's locale:
```kotlin
context(routingContext: RoutingContext)
override suspend fun respond() {
    val locale = routingContext.call.request.acceptLanguage() ?: "en"
    val message = ServerStrings.resolve(toAppError().code, locale)
    routingContext.unauthorized(toAppError().code, message)
}
```

**Alternative (simpler for template):** Server always returns English. Client uses `StringKey.fromCode(error.code)` to look up the localized version from Compose resources. This means L10N-03 is satisfied at the client layer rather than the server layer.

**Recommendation:** Implement the full server-side approach as described — it's a template demonstrating the pattern. The server string map is small (~20-30 entries) and shows developers how to add server i18n.

**Confidence:** MEDIUM — the Accept-Language parsing is straightforward but the DomainError respond() refactor touches all error classes.

### 3.6 Runtime Locale Switching (L10N-05)

**Compose Multiplatform approach:** `expect/actual` pattern for setting the app locale at runtime.

```kotlin
// composeApp/src/commonMain/.../localization/AppLocale.kt
expect fun setAppLocale(languageTag: String)
expect fun getAppLocale(): String
```

**Platform implementations:**

**Android:**
```kotlin
actual fun setAppLocale(languageTag: String) {
    val locale = Locale.forLanguageTag(languageTag)
    val config = Configuration(Resources.getSystem().configuration)
    config.setLocale(locale)
    // Store preference, recreate activity or use AppCompatDelegate.setApplicationLocales
}
```

**iOS:**
```kotlin
actual fun setAppLocale(languageTag: String) {
    NSUserDefaults.standardUserDefaults.setObject(listOf(languageTag), forKey = "AppleLanguages")
    NSUserDefaults.standardUserDefaults.synchronize()
}
```

**Desktop (JVM):**
```kotlin
actual fun setAppLocale(languageTag: String) {
    Locale.setDefault(Locale.forLanguageTag(languageTag))
}
```

**WASM:**
```kotlin
actual fun setAppLocale(languageTag: String) {
    // WASM reads navigator.languages for locale detection.
    // Override requires a JS interop workaround:
    js("window.__customLocale = languageTag")
    // Compose resources on WASM check a custom locale property.
    // This is the least reliable platform — see Pitfall 5.2.
}
```

**Persistence:** Store the user's locale preference in the existing `PreferencesStorage` (multiplatform-settings). On app startup, read the stored locale and call `setAppLocale()` before `setContent {}`.

**Confidence:** MEDIUM — Android/Desktop/iOS are well-documented. WASM locale override is fragile (see pitfalls).

---

## 4. Don't Hand-Roll

### Compose Resources Already Handles

| What | Built-in | Don't reinvent |
|------|----------|----------------|
| String resource loading | `stringResource(Res.string.key)` | Don't build a custom string loader |
| Locale-qualified directory fallback | `values-es/strings.xml` auto-selected | Don't implement manual locale file selection |
| Template string formatting | `stringResource(Res.string.key, arg1, arg2)` with `%1$s`, `%2$d` | Don't build string interpolation |
| Pluralization | `<plurals>` element in strings.xml with `pluralStringResource()` | Don't hand-roll plural rules |
| Font loading from resources | Already working (`Res.font.*`) | Don't change the font loading approach |
| Generated Res class | Gradle plugin auto-generates typed accessors | Don't manually create resource references |

### Reuse These Existing Utilities

| What | Where | Don't reinvent |
|------|-------|----------------|
| `AppError.code` values | `core/models/AppError.kt` | Use as StringKey codes — don't create a parallel code system |
| `DomainError.respond()` pattern | `server/core/config/DomainError.kt` | Extend with locale param — don't replace the error response chain |
| `ErrorResponse(code, message)` | `core/models/dto/ErrorResponse.kt` | Message becomes localized — format stays the same |
| `PreferencesStorage` | `core/storage` | Store locale preference here — don't create a new storage mechanism |
| `MviViewModel` test DSL | `core/testing` | Tests assert on `StringKey` values — DSL stays unchanged |
| `fakeSdk {}` builder | `core/testing` | No changes needed — fakes return `AppError` which already has `.code` |

### Patterns to Follow Exactly

| Pattern | Reference | Notes |
|---------|-----------|-------|
| ViewModel uses StringKey (not raw strings) for errors | Will be new pattern | Screens resolve StringKey -> localized string |
| Screens call `stringResource(Res.string.*)` | Compose Multiplatform standard | All UI text from resources, never hardcoded |
| Server returns `ErrorResponse(code, localizedMessage)` | Existing `conduit/conduitAuth` pattern | `message` field becomes locale-aware |
| Tests assert on StringKey values | Existing test DSL | `model(LoginModel(emailError = StringKey.VALIDATION_EMAIL_BLANK))` |

---

## 5. Common Pitfalls

### 5.1 WASM Async Resource Loading (CRITICAL)

**What goes wrong:** On WASM, Compose resources are loaded asynchronously (unlike all other platforms where they're synchronous). Strings loaded via `stringResource()` in composables work fine (Compose handles the async internally). But if you try to access resources outside of composition (e.g., in a ViewModel or init block), it may fail or return empty.

**Why it matters:** This was flagged in STATE.md as "WASM async resource loading timing needs smoke test in Phase 15."

**How to avoid:**
- NEVER access `Res.string.*` from ViewModels or non-composable code
- ViewModels use `StringKey` (an enum value, no resource access)
- Only screens (composables) call `stringResource()`
- The StringKey-to-string bridge is a `@Composable` function
- Run a WASM smoke test after implementation to verify strings render

**Confidence:** HIGH — well-documented limitation in JetBrains docs.

### 5.2 WASM Locale Override Fragility

**What goes wrong:** WASM/browser determines locale from `navigator.languages`. Overriding this at runtime requires JS interop hacks (`window.__customLocale`). The Compose resource library may or may not respect this override depending on version.

**How to avoid:** For the template, WASM locale switching can be a known limitation documented in code comments. The pattern works on Android/iOS/Desktop. WASM falls back to browser language settings.

**Confidence:** MEDIUM — WASM locale override is under-documented. Implement best-effort.

### 5.3 Model Type Change Cascades

**What goes wrong:** Changing error fields from `String?` to `StringKey?` in Model data classes breaks every test that asserts on error strings. This is a significant refactor touching all 7 ViewModels and their tests.

**How to avoid:** Plan the migration methodically:
1. Add StringKey enum and bridge function first
2. Migrate one ViewModel at a time (ViewModel + Screen + Tests as a unit)
3. Keep old string-based tests passing until each ViewModel is migrated

**Confidence:** HIGH — this is the biggest mechanical effort in the phase.

### 5.4 Server Error Message Dual Path

**What goes wrong:** After localization, there are two paths for error messages:
- **Server path:** `DomainError.respond()` -> `ErrorResponse(code, localizedMessage)` — server resolves locale
- **Client path:** `StringKey.fromCode(error.code)` -> `stringResource(key)` — client resolves locale

If both paths are active, the client gets a server-localized message AND has its own localized message. Which one wins?

**How to avoid:** Decide on one authoritative source:
- **Recommended:** Client always resolves. Server can also localize (for API consumers that aren't this app), but the Compose UI layer always uses `StringKey.fromCode(error.code)` and ignores `error.message`.
- The `serverError` field in Models becomes `StringKey?` (not `String?`), so the server's English message is intentionally discarded in favor of the client-resolved localized string.

**Confidence:** HIGH — clear architectural decision needed upfront.

### 5.5 Locale Persistence Timing

**What goes wrong:** If the locale is stored in `PreferencesStorage` but applied after the first composition, the UI flashes in the default locale before switching.

**How to avoid:** Read the stored locale synchronously before `setContent {}` in each platform's entry point (Activity.onCreate, main() on Desktop, etc.). Apply `setAppLocale()` BEFORE the Compose tree starts.

**Confidence:** HIGH — standard practice for locale initialization.

### 5.6 Test Assertions After Migration

**What goes wrong:** Tests currently assert on hardcoded English strings:
```kotlin
model(LoginModel(emailError = "Email must not be blank"))
```
After migration, they assert on StringKey:
```kotlin
model(LoginModel(emailError = StringKey.VALIDATION_EMAIL_BLANK))
```

This is actually BETTER for testing — tests become locale-independent. But every existing test file needs updating.

**Files affected:**
- `LoginViewModelTest.kt` — 3 tests
- `RegisterViewModelTest.kt` — tests
- `ForgotPasswordViewModelTest.kt` — tests
- `ProfileViewModelTest.kt` — tests
- `DashboardViewModelTest.kt` — tests
- `AdminPanelViewModelTest.kt` — tests
- `RegisterMemberViewModelTest.kt` — tests

**Confidence:** HIGH — mechanical find-and-replace once the Model types change.

---

## 6. Implementation Recommendations

### 6.1 Suggested Plan Ordering

**Plan 1: Foundation (StringKey + strings.xml + bridge)**
- Create `StringKey` enum in `core:models`
- Create `composeApp/src/commonMain/composeResources/values/strings.xml` with all English strings
- Create `composeApp/.../localization/StringKeyResolver.kt` bridge function
- Add `compose.components.resources` to feature module build.gradle.kts files that need it
- Verify WASM resource loading works (smoke test)

**Plan 2: ViewModel + Test Migration**
- Migrate all 7 ViewModels: error fields from `String?` to `StringKey?`
- Migrate `ValidationSupport.kt` to return `StringKey` in `FieldError.message` (or introduce a parallel `StringKey`-based field)
- Update all ViewModel tests to assert on `StringKey` values
- Update screens to resolve `StringKey` via `stringResource()`

**Plan 3: Screen UI String Extraction**
- Extract all hardcoded screen strings to `strings.xml`
- Replace inline text with `stringResource(Res.string.*)` calls
- This is the highest-volume work (~100-150 strings)

**Plan 4: Server Localization + Accept-Language**
- Create `ServerStrings` map for server-side error messages
- Add Accept-Language header parsing
- Refactor `DomainError.respond()` to use locale-resolved messages
- Add a second locale (Spanish) as proof-of-concept

**Plan 5: Runtime Locale Switching**
- Implement `expect/actual` `setAppLocale()`/`getAppLocale()` per platform
- Wire locale persistence to `PreferencesStorage`
- Add locale selector UI (could be a simple dropdown in settings/profile)
- Create `values-es/strings.xml` with Spanish translations for demonstration

### 6.2 Wave Dependencies

```
Plan 1 (foundation) -> Plan 2 (ViewModel migration) -> Plan 3 (screen strings)
Plan 1 (foundation) -> Plan 4 (server i18n) [parallel with Plan 2/3]
Plans 2+3+4 -> Plan 5 (runtime switching)
```

Wave 1: Plan 1
Wave 2: Plans 2, 3, 4 (parallelizable)
Wave 3: Plan 5

### 6.3 Scope Boundaries

**In scope (template demonstration):**
- English as default locale, Spanish as example second locale
- StringKey enum for error/validation messages (~30-40 keys)
- All UI strings extracted to strings.xml
- Server Accept-Language support
- Runtime locale switching on Android/iOS/Desktop
- WASM best-effort (browser locale detection, no guaranteed runtime override)

**Out of scope (per REQUIREMENTS.md):**
- Code-generated string bridge (manual mapping is sufficient)
- Server-driven localization (resource files preferred)
- Pluralization rules beyond simple singular/plural
- RTL language support
- Date/number formatting per locale

### 6.4 Key Architectural Decisions for Planner

1. **StringKey covers errors only.** Pure UI strings (labels, buttons) go directly in `strings.xml` and are accessed via `stringResource()` in composables. StringKey is for messages that flow through ViewModels.

2. **Client resolves, server also resolves.** Both sides can localize. The Compose client always uses `StringKey.fromCode()` for its own UI. The server localizes for other API consumers. They operate independently.

3. **All strings in one `strings.xml`.** Single file in `composeApp`, not per-module. The template is small enough, and this avoids cross-module Res imports.

4. **FieldError.message stays String.** The `FieldError` data class in `core:models` keeps `message: String` for wire compatibility. ViewModels map `FieldError.message` to `StringKey` using a `StringKey.fromValidationMessage()` helper or by changing validation functions to produce `StringKey` directly.

---

## 7. Open Questions for Planner

1. **FieldError approach:** Should `FieldError.message` become `StringKey` (breaking change to core:models) or should ViewModels map string messages to StringKey at the boundary? The former is cleaner but touches the wire format. The latter is more isolated.

2. **Second locale choice:** Spanish (`es`) is the example in this research. The planner can choose a different second locale if preferred.

3. **Locale selector placement:** Where should the runtime locale toggle live? Options: Profile screen settings section, Dashboard settings tab, or a dedicated Settings screen. Profile screen is simplest since it already exists.

4. **WASM locale override effort:** How much effort to invest in the WASM JS interop hack for runtime locale switching? Recommendation: document as known limitation, implement best-effort, move on.

5. **Server strings refactor scope:** Should ALL DomainError classes get the locale-aware respond() method, or just the most common ones (auth + group errors)? Recommendation: all of them — it's mechanical and shows the complete pattern.

---

## 8. Sources

### Primary (HIGH confidence)
- **Codebase analysis:** All 7 ViewModels, all screens, AppError.kt, ValidationSupport.kt, DomainError.kt, ErrorMapper.kt — complete string inventory
- **Compose Multiplatform Resources docs:** String resources, locale qualifiers, stringResource() API — verified for 1.10.x
- **Existing resource setup:** Fonts already working via `compose.components.resources` in `app:designsystem` — proves the Gradle plugin is configured

### Secondary (MEDIUM confidence)
- **JetBrains WASM resource docs:** Async loading behavior documented but WASM locale override behavior is under-documented
- **Platform locale APIs:** Android `Configuration.setLocale`, iOS `NSUserDefaults.AppleLanguages`, JVM `Locale.setDefault` — standard APIs, well-documented

---

**Confidence breakdown:**
- Standard stack: HIGH — Compose Resources already in project, no new deps
- Architecture (StringKey + bridge): HIGH — straightforward enum + when mapping
- ViewModel migration: HIGH — mechanical refactor following existing MVI pattern
- Screen string extraction: HIGH — tedious but straightforward
- Server i18n: MEDIUM — DomainError.respond() refactor touches all error classes
- Runtime locale switching: MEDIUM — Android/iOS/Desktop reliable, WASM fragile
- WASM resource loading: MEDIUM — needs smoke test to confirm

**Research date:** 2026-02-19
**Valid until:** 2026-03-19 (stable — Compose Multiplatform 1.10.1, no dependency changes expected)
