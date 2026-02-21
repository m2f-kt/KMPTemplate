package com.m2f.server.auth.security

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

private const val BCRYPT_ROUNDS = 12

/**
 * Password hashing utility using BCrypt.
 * All operations are dispatched to the injected [computeDispatcher] to avoid blocking
 * the event loop, since BCrypt is CPU-bound.
 */
class PasswordHasher(
    private val computeDispatcher: CoroutineDispatcher,
) {

    /**
     * Hash a plaintext password using BCrypt with [BCRYPT_ROUNDS] rounds.
     */
    suspend fun hash(password: String): String = withContext(computeDispatcher) {
        BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS))
    }

    /**
     * Verify a plaintext password against a BCrypt hash.
     */
    suspend fun verify(password: String, hash: String): Boolean = withContext(computeDispatcher) {
        BCrypt.checkpw(password, hash)
    }
}
