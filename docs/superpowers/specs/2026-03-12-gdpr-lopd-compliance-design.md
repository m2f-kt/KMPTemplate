# GDPR/LOPD Compliance Design

## Context

B2B2C Kotlin Multiplatform + Ktor template app. Multi-tenant with full isolation — each business (tenant) has its own user base. AI features (Google Gemini) are part of the core service (contract lawful basis). Legal documents are server-managed with versioning (served via API, stored in database, seeded via migrations for initial content). Full self-service for all user data rights.

**Multi-tenancy scoping:** Tenant isolation is inherited via the `userId -> users -> tenant` relationship. All privacy queries are scoped through the authenticated user's userId, which inherently limits data to the user's tenant. No separate `tenantId` column is needed on privacy tables since the userId FK provides the scoping chain.

**LOPD-GDD scope:** This template targets Spanish market users. LOPD-GDD requirements beyond GDPR are addressed where applicable (age of consent at 14, Spanish-language legal documents). Digital testament (Article 96) and workplace-specific provisions (Articles 87-90) are out of scope for this template — they can be added by implementers if their B2B tenants require them.

## Approach

Compliance-as-a-Module: a dedicated `server:privacy` module and `app:privacy` client module centralizing all compliance logic. Follows existing contract/impl/wire submodule pattern.

---

## 1. Data Model & Database Schema

### New Tables (server:privacy)

**`consent_records`** (append-only ledger)

| Column | Type | Notes |
|--------|------|-------|
| id | UUID | PK |
| userId | UUID | FK -> users |
| consentType | VARCHAR | `PRIVACY_POLICY`, `TERMS_OF_SERVICE`, `MARKETING`, `ANALYTICS` |
| granted | BOOLEAN | true = granted, false = withdrawn |
| legalDocumentVersion | VARCHAR | version of the document shown at time of consent |
| ipAddress | VARCHAR(45) | nullable, for audit |
| userAgent | TEXT | nullable, for audit |
| createdAt | TIMESTAMP | immutable |

**`legal_documents`** (versioned, server-managed, seeded via migrations)

| Column | Type | Notes |
|--------|------|-------|
| id | UUID | PK |
| type | VARCHAR | `PRIVACY_POLICY`, `TERMS_OF_SERVICE` |
| version | VARCHAR | semver, e.g. "1.0.0" |
| locale | VARCHAR(5) | `es`, `en` |
| content | TEXT | markdown content |
| publishedAt | TIMESTAMP | when this version became active |
| createdAt | TIMESTAMP | |

**`data_export_requests`** (tracks export jobs)

| Column | Type | Notes |
|--------|------|-------|
| id | UUID | PK |
| userId | UUID | FK -> users |
| status | VARCHAR | `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED` |
| fileKey | VARCHAR | MinIO key for the export archive, nullable |
| completedAt | TIMESTAMP | nullable |
| expiresAt | TIMESTAMP | auto-delete the export file after N days |
| createdAt | TIMESTAMP | |

**`account_deletion_requests`** (tracks deletion workflow)

| Column | Type | Notes |
|--------|------|-------|
| id | UUID | PK |
| userId | UUID | FK -> users |
| status | VARCHAR | `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED` |
| reason | TEXT | nullable, user-provided |
| scheduledAt | TIMESTAMP | grace period before actual deletion |
| completedAt | TIMESTAMP | nullable |
| createdAt | TIMESTAMP | |

### Modifications to Existing Tables

**`users`** -- add column:
- `processingRestricted` BOOLEAN DEFAULT false (Article 18)

---

## 2. Server Module -- `server:privacy`

### Structure

```
server/privacy/
  contract/   -- Routes, DTOs, shared types
  impl/       -- Services, repositories, tables, routes
  wire/       -- Koin module, migration registration
```

### API Endpoints

**Consent Management**
- `GET /api/privacy/consent` -- get current user's active consents
- `POST /api/privacy/consent` -- grant consent (appends to ledger)
- `DELETE /api/privacy/consent/{type}` -- withdraw consent (appends withdrawal record). Note: must NOT require re-authentication per GDPR Article 7(3) — withdrawal must be as easy as granting
- `GET /api/privacy/consent/required` -- check if user needs to accept updated policies

**Legal Documents**
- `GET /api/privacy/legal/{type}?locale={locale}` -- get current version of a legal document
- `GET /api/privacy/legal/{type}/versions` -- list all versions (admin/audit)

**Data Export (Article 20)**
- `POST /api/privacy/export` -- request data export (async job)
- `GET /api/privacy/export/{id}` -- check export status
- `GET /api/privacy/export/{id}/download` -- download export archive (presigned MinIO URL)

**Account Deletion (Article 17)**
- `POST /api/privacy/deletion` -- request account deletion (requires re-authentication)
- `GET /api/privacy/deletion` -- check pending deletion status
- `DELETE /api/privacy/deletion` -- cancel pending deletion (during grace period)

**Processing Restriction (Article 18)**
- `POST /api/privacy/restrict` -- restrict processing of user's data
- `DELETE /api/privacy/restrict` -- lift restriction

### Services

**ConsentService** -- validates consent operations, checks policy versions, queries active consents. Uses `context(Raise<DomainError>)`.

**LegalDocumentService** -- serves documents by type/locale, resolves current version, compares user's last accepted version.

**DataExportService** -- orchestrates export via an `ExportContributor` interface that each module implements. Each contributor provides `suspend fun export(userId: UUID): ExportSection` returning its data slice. Contributors are registered via Koin (multibinding). The export service collects all sections, packages into a ZIP archive (JSON + files), and uploads to MinIO with 7-day expiry. Contributors: AuthExportContributor (profile, login history), GroupExportContributor (memberships), FileExportContributor (document metadata + files from MinIO), AiExportContributor (conversation history), ConsentExportContributor (consent records).

**AccountDeletionService** -- orchestrates cascading deletion:
1. Validates re-authentication
2. Creates deletion request with 7-day grace period
3. When grace period expires, deletes in order: AI data -> documents/files -> group memberships -> refresh tokens -> anonymize consent records -> export archives -> user record
4. Sends confirmation email before deletion executes

**ProcessingRestrictionService** -- sets/clears the flag on the user record.

### Scheduled Jobs

- **Deletion Executor** (daily) -- processes deletion requests past grace period
- **Export Cleanup** (daily) -- deletes expired export archives from MinIO
- **Token Cleanup** (daily) -- purges expired/revoked refresh tokens

Implementation: coroutine-based scheduling via `Application.monitor` with supervised scope. Each job is a `PrivacyJob` interface with `suspend fun execute()`.

---

## 3. Client Module -- `app:privacy`

### Structure

```
app/privacy/
  contract/   -- ConsentGateRoute, PrivacySettingsRoute, LegalDocumentRoute, AccountDeletionRoute
  impl/       -- ViewModels, MVI types, Screen composables
  wire/       -- Koin module, navigation extensions
```

### Screens

**ConsentGateScreen** -- forced acceptance of privacy policy + ToS before proceeding. Full-screen flow with checkboxes, document preview links, accept/decline.

**PrivacySettingsScreen** -- central hub listing: active consents (with withdrawal toggles), data export (request/status/download), processing restriction toggle.

**LegalDocumentScreen** -- scrollable markdown viewer with version info header and locale switcher.

**AccountDeletionScreen** -- multi-step flow: warning -> re-authenticate -> optional reason -> confirm with grace period info.

### MVI Flows

**ConsentGateViewModel**
- Intents: `LoadRequiredConsents`, `ToggleConsent(type)`, `AcceptAll`, `ViewDocument(type)`
- Mutations: `SetConsents(List<ConsentItem>)`, `UpdateConsentToggle(type, granted)`, `SetLoading(Boolean)`, `SetError(String?)`
- Model: `consents: List<ConsentItem>`, `allAccepted: Boolean`, `loading: Boolean`, `error: String?`
- Events: `NavigateToDocument(type)`, `ConsentCompleted`, `ShowError(message)`

**PrivacySettingsViewModel**
- Intents: `Load`, `RequestExport`, `DownloadExport`, `RequestDeletion`, `ToggleRestriction`, `ViewDocument(type)`, `WithdrawConsent(type)`
- Mutations: `SetConsents(List<ConsentStatus>)`, `SetExportStatus(DataExportResponse?)`, `SetDeletionStatus(DeletionResponse?)`, `SetRestricted(Boolean)`, `SetLoading(Boolean)`, `SetError(String?)`
- Model: `activeConsents`, `exportStatus`, `deletionStatus`, `isRestricted`, `loading`, `error`
- Events: `NavigateToDeletion`, `NavigateToDocument`, `ExportReady(downloadUrl)`, `ShowError(message)`

**AccountDeletionViewModel**
- Intents: `Load`, `ReAuthenticate(password)`, `SetReason(text)`, `ConfirmDeletion`, `CancelDeletion`
- Mutations: `SetStep(DeletionStep)`, `SetPendingDeletion(DeletionResponse?)`, `SetLoading(Boolean)`, `SetError(String?)`
- Model: `step: DeletionStep` (WARNING, RE_AUTH, REASON, CONFIRM, SCHEDULED), `pendingDeletion`, `loading`, `error`
- Events: `DeletionScheduled`, `DeletionCancelled`, `NavigateToLogin`, `ShowError(message)`

**LegalDocumentViewModel**
- Intents: `Load(type, locale)`, `SwitchLocale(locale)`
- Mutations: `SetDocument(LegalDocumentResponse?)`, `SetLoading(Boolean)`, `SetError(String?)`
- Model: `document: LegalDocumentResponse?`, `loading: Boolean`, `error: String?`
- Events: `ShowError(message)`

---

## 4. SDK Layer

### PrivacyApi (in core:sdk)

```
getActiveConsents(): Either<AppError, List<ConsentStatus>>
grantConsent(request): Either<AppError, Unit>
withdrawConsent(type: ConsentType): Either<AppError, Unit>
getRequiredConsents(): Either<AppError, RequiredConsentsResponse>
getLegalDocument(type, locale): Either<AppError, LegalDocumentResponse>
requestDataExport(): Either<AppError, DataExportResponse>
getExportStatus(id): Either<AppError, DataExportResponse>
getExportDownloadUrl(id): Either<AppError, String>
requestAccountDeletion(request): Either<AppError, DeletionResponse>
getDeletionStatus(): Either<AppError, DeletionResponse?>
cancelDeletion(): Either<AppError, Unit>
restrictProcessing(): Either<AppError, Unit>
liftRestriction(): Either<AppError, Unit>
```

Added to Sdk facade via `by` delegation.

### DTOs (in core:models)

All `@Serializable`:
- `ConsentStatus(type, granted, grantedAt, documentVersion)`
- `GrantConsentRequest(type, documentVersion)`
- `WithdrawConsentRequest(type)`
- `RequiredConsentsResponse(consents, hasOutdated)`
- `LegalDocumentResponse(type, version, locale, content, publishedAt)`
- `DataExportResponse(id, status, downloadUrl?, createdAt, expiresAt?)`
- `DeletionRequest(password, reason?)`
- `DeletionResponse(id, status, scheduledAt, completedAt?)`

### Error Variants

New `AppError.Privacy` sealed class (follows existing pattern: `AppError.Auth`, `AppError.Group`, etc.):
- `AppError.Privacy.ProcessingRestricted` (code: `PRIVACY_PROCESSING_RESTRICTED`) -- user's data processing is restricted
- `AppError.Privacy.ConsentRequired` (code: `PRIVACY_CONSENT_REQUIRED`) -- user hasn't accepted required policies
- `AppError.Privacy.DeletionPending` (code: `PRIVACY_DELETION_PENDING`) -- account scheduled for deletion
- `AppError.Privacy.ExportNotReady` (code: `PRIVACY_EXPORT_NOT_READY`) -- export still processing or expired

---

## 5. Cross-Cutting Concerns

### Processing Restriction Middleware

Extend `conduitAuth` to check `user.processingRestricted`. When true:
- **Allow:** authentication, profile viewing, privacy settings, data export, deletion, lifting restriction
- **Block:** all other processing with `ProcessingRestricted` error (403)

### Consent Gate Navigation Guard

Post-login in the auth ViewModel:
1. Call `getRequiredConsents()`
2. If consents missing or outdated -> emit navigation event to `ConsentGateRoute`
3. Otherwise -> proceed to dashboard

Mid-session: server returns `ConsentRequired` error -> client navigates to `ConsentGateRoute`.

### Rate Limiting

High-impact privacy endpoints enforce limits to prevent abuse:
- **Data export:** one active request at a time per user (reject if PENDING/PROCESSING exists)
- **Account deletion:** one request per user (reject if PENDING exists)
- **Consent withdrawal:** no rate limit (GDPR Article 7(3) requires it be easy)
- **Processing restriction:** no rate limit (immediate toggle)

---

## 6. Data Retention Policy

| Data Category | Retention | Justification |
|---------------|-----------|---------------|
| User account | Until deletion requested | Contract basis |
| Refresh tokens | 30 days active, purged when expired/revoked | Security |
| Consent records | Indefinitely (anonymized after account deletion) | Legal obligation |
| Data exports | 7 days after generation | Temporary |
| Deletion grace period | 7 days | Allow cancellation |
| AI conversations | Until account deletion | Part of service |
| Uploaded documents | Until user/account deletion | Part of service |
| Legal document versions | Indefinitely | Audit trail |

---

## 7. Feature -> Pencil Screen -> Implementation Map

### New Screens

| # | Feature | Pencil Screen | Server | Client | SDK |
|---|---------|--------------|--------|--------|-----|
| 1 | Consent gate | `consent-gate` | `POST /consent`, `GET /consent/required` | ConsentGateViewModel + Screen | `getRequiredConsents()`, `grantConsent()` |
| 2 | Privacy settings hub | `privacy-settings` | `GET /consent` | PrivacySettingsViewModel + Screen | `getActiveConsents()` |
| 3 | Legal document viewer | `legal-document` | `GET /legal/{type}` | LegalDocumentViewModel + Screen | `getLegalDocument()` |
| 4 | Data export | `privacy-settings` (section) | `POST /export`, `GET /export/{id}`, `GET /export/{id}/download` | PrivacySettingsViewModel | `requestDataExport()`, `getExportStatus()`, `getExportDownloadUrl()` |
| 5 | Account deletion | `account-deletion` | `POST /deletion`, `GET /deletion`, `DELETE /deletion` | AccountDeletionViewModel + Screen | `requestAccountDeletion()`, `getDeletionStatus()`, `cancelDeletion()` |
| 6 | Processing restriction | `privacy-settings` (section) | `POST /restrict`, `DELETE /restrict` | PrivacySettingsViewModel | `restrictProcessing()`, `liftRestriction()` |
| 7 | Consent withdrawal | `privacy-settings` (section) | `DELETE /consent/{type}` | PrivacySettingsViewModel | `withdrawConsent(type)` |

> Note: Rows 2, 4, 6, and 7 all render within the same `privacy-settings` Pencil screen as distinct sections. All endpoint paths above are shorthand — full prefix is `/api/privacy/`.

### Existing Screen Updates

| # | Screen | Pencil Update | Change |
|---|--------|--------------|--------|
| 8 | Login/Register | Update existing design | Add legal links footer |
| 9 | Profile | Update existing design | Add "Privacy & Data" menu entry |
