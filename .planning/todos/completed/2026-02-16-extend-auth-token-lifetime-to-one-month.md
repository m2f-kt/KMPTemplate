---
created: 2026-02-16T00:54:46.023Z
title: Extend auth token lifetime to one month
area: auth
files:
  - server/auth/src/main/kotlin/com/m2f/server/auth/security/JwtConfig.kt
---

## Problem

The current JWT access token and refresh token lifetimes are too short. Users get logged out too frequently, which is frustrating for a template app where long-lived sessions are expected. The user wants the auth token to survive for at least one month.

## Solution

- Update JWT access token expiry to a longer duration (e.g., 1 hour or 1 day)
- Update refresh token expiry to 30 days (1 month)
- The refresh token rotation mechanism already exists (Phase 2), so extending the refresh lifetime is safe
- Consider making token lifetimes configurable via environment variables
