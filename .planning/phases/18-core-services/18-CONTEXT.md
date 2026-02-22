# Phase 18: Core Services - Context

**Gathered:** 2026-02-22
**Status:** Ready for planning
**Mode:** Auto-generated (no user discussion — sensible defaults applied)

<domain>
## Phase Boundary

Files can be uploaded to S3 and emails can be sent — the two infrastructure services that other features (RAG document ingestion, group invitations, profile avatars) depend on. Integration tests verify both services work end-to-end.

Scope from ROADMAP.md requirements: FILE-01, FILE-04, EMAIL-01, EMAIL-04, DEBT-02

</domain>

<decisions>
## Implementation Decisions

### File upload constraints
- Max file size: 10MB (sufficient for documents and avatars; can be made configurable later)
- Allowed types whitelist: images (jpeg, png, gif, webp), documents (pdf, txt, md) — covers avatar uploads (Phase 21) and RAG document ingestion (Phase 19)
- Rejection returns 413 (Payload Too Large) for size violations, 415 (Unsupported Media Type) for type violations
- Files stored with UUID-based keys to avoid collisions: `{userId}/{uuid}.{ext}`

### Upload API design
- Single multipart upload endpoint: `POST /api/files/upload` (authenticated via conduitAuth)
- Response returns file metadata: `FileResponse(id, key, originalName, contentType, size, url, createdAt)`
- Retrieval via presigned URL or proxy endpoint — presigned URL preferred (offloads serving to MinIO)
- Type-safe Resource: `@Resource("/api/files")` with nested `Upload` and `Get` resources
- Follow existing patterns: `fun Route.fileRoutes(fileService: FileService)` with `conduitAuth`

### S3 service design
- `FileService` as plain class (not interface) matching existing service pattern — constructor-injected with S3 client and config
- Uses AWS SDK for Kotlin (S3 client) — the standard choice for S3-compatible APIs including MinIO
- `context(raise: Raise<DomainError>)` for error handling, consistent with AuthService/GroupService
- Registered in a new `fileModule` Koin module, included by `serverModule`

### Email service design
- `EmailService` interface with `suspend fun sendEmail(to: String, subject: String, body: String)` — interface because downstream phases need to fake it in tests
- `SmtpEmailService` implementation using Jakarta Mail (javax.mail successor) or kotlinx simple SMTP — keeps it lightweight
- Plain text emails for now (HTML templates are Phase 21 concern for invitation emails)
- Registered in a new `emailModule` Koin module
- Config from `Configuration.env.email` (host, port, username, password, fromAddress)

### Password reset email migration (DEBT-02)
- Inject `EmailService` into `PasswordResetService` (replace println on line 58)
- Email contains reset link: `{baseUrl}/reset-password?token={rawToken}`
- Subject: "Password Reset Request"
- Body: plain text with reset link and expiry notice
- Maintain existing security pattern: always return success message regardless of whether email exists

### Integration test strategy
- MinIO Testcontainer for file upload round-trip tests (upload → retrieve → verify content matches)
- SMTP test: use GreenMail Testcontainer or simple mock — verify email delivery in test
- Follow existing `TestDatabase` singleton pattern: `TestMinIO` object with container lifecycle
- Tests use JUnit 4 + Kotest matchers + Ktor testApplication, consistent with GroupRoutesTest
- Test rejection cases: oversized file, disallowed type

### SDK exposure
- `FileApi` interface in SDK: `uploadFile(fileName, bytes, contentType): Either<AppError, FileResponse>`
- `FileApiImpl` uses multipart form data via Ktor client
- Added to `Sdk` class via delegation, wired in `sdkModule`
- No SDK email API needed (email is server-side only — users don't send emails via SDK)

### Claude's Discretion
- Exact S3 client library choice (AWS SDK for Kotlin vs MinIO Java SDK)
- Presigned URL expiry duration
- Email body wording and formatting
- Test helper organization (shared test module vs per-module)
- Whether to add a files database table for metadata tracking or keep it S3-only

</decisions>

<specifics>
## Specific Ideas

No specific requirements — auto mode applied standard approaches based on existing codebase patterns:
- Services follow AuthService/GroupService pattern (plain class, Raise context, Koin single)
- Routes follow GroupRoutes pattern (conduitAuth, type-safe Resources)
- Tests follow GroupRoutesTest pattern (Testcontainers, JUnit 4, Kotest matchers)
- SDK follows GroupApi pattern (interface + impl + apiCall + Sdk delegation)

</specifics>

<deferred>
## Deferred Ideas

None — auto mode stayed within phase scope.

Downstream phases that depend on these services:
- Phase 19: RAG pipeline will use FileService for document ingestion
- Phase 21: Invitations will use EmailService for invite emails; Profiles will use FileService for avatar uploads

</deferred>

---

*Phase: 18-core-services*
*Context gathered: 2026-02-22*
