package com.m2f.server.auth.security

import com.m2f.server.auth.contract.security.PasswordHasher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

private const val BCRYPT_ROUNDS = 12

/**
 * BCrypt implementation of [PasswordHasher].
 * All operations are dispatched to the injected [computeDispatcher] to avoid blocking
 * the event loop, since BCrypt is CPU-bound.
 */
class BcryptPasswordHasher(
    private val computeDispatcher: CoroutineDispatcher,
) : PasswordHasher {

    override suspend fun hash(password: String): String = withContext(computeDispatcher) {
        BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS))
    }

    override suspend fun verify(password: String, hash: String): Boolean = withContext(computeDispatcher) {
        BCrypt.checkpw(password, hash)
    }
}
