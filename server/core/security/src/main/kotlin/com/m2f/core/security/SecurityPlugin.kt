package com.m2f.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.m2f.core.config.configuration.Configuration
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

context(config: Configuration)
@Suppress("ForbiddenComment")
fun Application.configureSecurity() {
    with(config) {
        val jwtAudience = env.auth.audience
        val jwtDomain = env.auth.issuer
        val jwtRealm = env.auth.jwtRealm
        val jwtSecret = env.auth.secret
        val verifier = JWT
            .require(Algorithm.HMAC256(jwtSecret))
            .withAudience(jwtAudience)
            .withIssuer(jwtDomain)
            .build()
        val validateBlock: io.ktor.server.auth.jwt.JWTAuthenticationProvider.Config.() -> Unit = {
            realm = jwtRealm
            verifier(verifier)
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
        authentication {
            // Default unnamed provider — used by `authenticate { ... }` blocks in
            // every Phase 1 route.
            jwt(name = null, configure = validateBlock)
            // Named "auth-jwt" provider — required by Phase 2-05's WebSocket
            // route (`authenticate("auth-jwt") { webSocket(...) }`). The two
            // providers are functionally identical — they share the same
            // verifier and validator — but the WS upgrade plugin needs an
            // explicitly-named provider so the close-frame audit trail
            // distinguishes the WS path from REST.
            jwt(name = "auth-jwt", configure = validateBlock)
        }
    }
}
