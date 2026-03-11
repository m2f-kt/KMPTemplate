package com.m2f.server.auth.contract.security

/**
 * Password hashing abstraction.
 * All operations should be dispatched to a compute dispatcher to avoid blocking the event loop.
 */
interface PasswordHasher {
    suspend fun hash(password: String): String
    suspend fun verify(password: String, hash: String): Boolean
}
