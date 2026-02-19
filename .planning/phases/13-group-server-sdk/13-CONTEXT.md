# Phase 13: Group Server & SDK - Context

**Gathered:** 2026-02-19
**Status:** Ready for planning

<domain>
## Phase Boundary

Server-side group management with CRUD operations, membership management, RBAC enforcement, and SDK client. Users belong to groups via a join table (multi-group ready). Group isolation applies to group endpoints only — existing features (chat, profiles) stay unscoped. Accessible through the shared SDK with type-safe routes.

</domain>

<decisions>
## Implementation Decisions

### Group data model
- Join table (`user_group_memberships`) from day one — multi-group ready without future migration
- Group fields: id, name, slug (URL-friendly), description, created_by (user_id), created_at, updated_at
- Membership table: user_id, group_id, role (group-specific role column) — a user could be admin in one group and member in another

### API contract design
- Full CRUD for groups: create, read, update, delete + list all groups (power admin)
- Member management: add/remove existing users AND register new users directly into a group
- Member list is paginated with cursor-based pagination
- All SDK functions return `Either<ClientError, T>` using shared `@Resource` route definitions

### Authorization & isolation
- Admins manage group (CRUD + member management); members can only read their own membership, not the full member list
- Cross-group access returns 403 Forbidden (explicit, not 404)
- Existing power admin role gets cross-group visibility — can see and manage all groups
- Group isolation applies only to group endpoints — existing features stay as-is for now

### Seeding & defaults
- No auto-create on server startup — seed scripts handle group creation
- Migration creates a default group and assigns all existing users to it
- Setup CLI stays unchanged — group creation is a separate concern
- Dev/test seed creates 2 groups with users in each for out-of-the-box isolation testing

### Claude's Discretion
- Exact table naming and index strategy
- Cursor pagination implementation details
- Error response message wording
- Test fixture organization

</decisions>

<specifics>
## Specific Ideas

- Existing power admin user role should serve as the system-wide super-admin for cross-group operations
- Two seeded groups with users for dev/test so developers can immediately test cross-group isolation

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 13-group-server-sdk*
*Context gathered: 2026-02-19*
