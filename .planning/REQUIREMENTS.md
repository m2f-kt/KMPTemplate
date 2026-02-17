# Requirements: KMP Full-Stack Template

**Defined:** 2026-02-17
**Core Value:** A developer can clone this template, run the setup CLI, and immediately have a working full-stack app with auth, database, DI, AI agents, and a component library -- no infrastructure decisions required.

## v1.1 Requirements

Requirements for milestone v1.1 Architecture. Each maps to roadmap phases.

### MVI ViewModel

- [ ] **MVI-01**: Developer can extend a generic MVI ViewModel base class with Intent/Model/Mutation/Event type parameters
- [ ] **MVI-02**: ViewModel exposes state as StateFlow<Model> with a pure reduce(Model, Mutation) function
- [ ] **MVI-03**: ViewModel emits one-shot events via Channel/SharedFlow<Event> (no double-firing)
- [ ] **MVI-04**: ViewModels are injectable via Koin across all KMP targets
- [ ] **MVI-05**: All 5 existing ViewModels are migrated to the MVI pattern
- [ ] **MVI-06**: Developer can test ViewModels using a Turbine-based DSL that asserts intent/model/event sequences

### Groups & Admin

- [ ] **GRP-01**: Admin can create a group with name and description
- [ ] **GRP-02**: User belongs to one group (schema designed for future multi-group)
- [ ] **GRP-03**: Admin can view group information and member list
- [ ] **GRP-04**: Admin can register new users directly into their group
- [ ] **GRP-05**: Admin sees an admin panel with different dashboard content than regular users
- [ ] **GRP-06**: Navigation is role-gated (admin routes vs regular user routes)
- [ ] **GRP-07**: Group SDK functions return Either<ClientError, T> with shared @Resource routes
- [ ] **GRP-08**: Group data is isolated -- users cannot see other groups' data

### Testing

- [ ] **TEST-01**: core:testing module provides MVI ViewModel test DSL with Turbine
- [ ] **TEST-02**: Server integration tests run via Ktor testApplication with test database
- [ ] **TEST-03**: SDK API classes extracted to interfaces for fake substitution in tests
- [ ] **TEST-04**: Hand-written fake implementations exist for SDK contracts
- [ ] **TEST-05**: Shared test fixtures and utilities available across modules
- [ ] **TEST-06**: Kotest assertions work with Arrow Either/Raise in multiplatform tests

### Localization

- [ ] **L10N-01**: Shared StringKey enum bridges server and client string references
- [ ] **L10N-02**: Compose resource files (strings.xml) support locale qualifiers
- [ ] **L10N-03**: Server returns localized error messages based on Accept-Language header
- [ ] **L10N-04**: Client UI strings load from platform resource files
- [ ] **L10N-05**: User can switch locale at runtime
- [ ] **L10N-06**: Bridge function maps StringKey to Compose Res.strings accessors

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Groups (Advanced)

- **GRP-ADV-01**: User can belong to multiple groups simultaneously
- **GRP-ADV-02**: Group-scoped roles (Owner/Admin/Member) as second-tier RBAC
- **GRP-ADV-03**: Admin can invite users via email link
- **GRP-ADV-04**: Group activity feed showing member actions

### Localization (Advanced)

- **L10N-ADV-01**: Pluralization rules beyond simple singular/plural
- **L10N-ADV-02**: RTL language support
- **L10N-ADV-03**: Date/number formatting per locale

## Out of Scope

| Feature | Reason |
|---------|--------|
| Third-party MVI library (Orbit, MVIKotlin) | Template should own its patterns, ~30 LOC base class |
| Email delivery service (SMTP/SendGrid) | Too infrastructure-specific for a template |
| OAuth/social login | Email/password sufficient, OAuth adds provider dependencies |
| Real-time group notifications | WebSocket already used for AI; group notifications deferred |
| Server-driven localization | Resource files are simpler, no network dependency for strings |
| Code-generated string bridge | Manual mapping sufficient for template scope |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| MVI-01 | Phase 10 | Pending |
| MVI-02 | Phase 10 | Pending |
| MVI-03 | Phase 10 | Pending |
| MVI-04 | Phase 10 | Pending |
| MVI-05 | Phase 12 | Pending |
| MVI-06 | Phase 11 | Pending |
| GRP-01 | Phase 13 | Pending |
| GRP-02 | Phase 13 | Pending |
| GRP-03 | Phase 14 | Pending |
| GRP-04 | Phase 14 | Pending |
| GRP-05 | Phase 14 | Pending |
| GRP-06 | Phase 14 | Pending |
| GRP-07 | Phase 13 | Pending |
| GRP-08 | Phase 13 | Pending |
| TEST-01 | Phase 11 | Pending |
| TEST-02 | Phase 13 | Pending |
| TEST-03 | Phase 11 | Pending |
| TEST-04 | Phase 11 | Pending |
| TEST-05 | Phase 11 | Pending |
| TEST-06 | Phase 11 | Pending |
| L10N-01 | Phase 15 | Pending |
| L10N-02 | Phase 15 | Pending |
| L10N-03 | Phase 15 | Pending |
| L10N-04 | Phase 15 | Pending |
| L10N-05 | Phase 15 | Pending |
| L10N-06 | Phase 15 | Pending |

**Coverage:**
- v1.1 requirements: 26 total
- Mapped to phases: 26
- Unmapped: 0

---
*Requirements defined: 2026-02-17*
*Last updated: 2026-02-17 after roadmap creation*
