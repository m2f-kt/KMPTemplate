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
        authentication {
            jwt {
                realm = jwtRealm
                verifier(
                    JWT
                        .require(Algorithm.HMAC256(jwtSecret))
                        .withAudience(jwtAudience)
                        .withIssuer(jwtDomain)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
                }
            }
        }
    }
}
