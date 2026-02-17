---
status: complete
phase: 02-server-auth-users
source: [02-01-SUMMARY.md, 02-02-SUMMARY.md, 02-03-SUMMARY.md]
started: 2026-02-11T18:10:00Z
updated: 2026-02-11T18:30:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Register with valid data
expected: POST /api/auth/register with valid email/password/name returns 201 with JSON body containing accessToken (JWT string), refreshToken (UUID string), and expiresIn (number).
result: pass

### 2. Register with invalid fields (accumulated errors)
expected: POST /api/auth/register with invalid email, short password, and empty name returns 422 with ALL field errors at once (not just the first one). Response should contain errors for email, password, and name simultaneously.
result: pass

### 3. Register with duplicate email
expected: POST /api/auth/register with an already-registered email returns 422 with error code AUTH_USER_ALREADY_EXISTS.
result: pass

### 4. Login with valid credentials
expected: POST /api/auth/login with a registered email and correct password returns 200 with accessToken, refreshToken, and expiresIn.
result: pass

### 5. Login with wrong password
expected: POST /api/auth/login with a registered email but wrong password returns 401 with AUTH_INVALID_CREDENTIALS. The error message should NOT reveal whether the email exists.
result: pass

### 6. Refresh token rotation
expected: POST /api/auth/refresh with a valid refreshToken returns 200 with NEW accessToken and refreshToken. Reusing the OLD refreshToken again returns 401 (token was revoked by rotation).
result: pass

### 7. Logout revokes tokens
expected: POST /api/auth/logout with a valid Bearer access token returns 200. After logout, refreshing with any previous refresh token returns 401.
result: pass

### 8. Get own profile
expected: GET /api/users/me with a valid Bearer access token returns 200 with JSON containing id, email, name, and role fields matching the registered user.
result: pass

### 9. Update own profile
expected: PUT /api/users/me with a valid Bearer token and {"name":"New Name"} returns 200 with the updated UserResponse showing the new name.
result: pass

### 10. RBAC - User role denied admin endpoint
expected: GET /api/users/{some-uuid} with a regular USER's access token returns 403 with USER_FORBIDDEN error. Only ADMIN role should access this endpoint.
result: pass

## Summary

total: 10
passed: 10
issues: 0
pending: 0
skipped: 0

## Gaps

[none]
