package com.m2f.core.config.configuration

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val PORT: Int = 8080
private const val AUTH_SECRET: String = "ThisIsAReallyReallyReallyStrongSecretKeyForJWT123!@#$%^&*()"
private const val AUTH_ISSUER: String = "IssuerName"
private const val REALM: String = "Access to Your Application"

data class Env(
    val http: Http = Http(),
    val auth: Auth = Auth(),
    val serverConfig: ServerConfig = ServerConfig(),
) {
    data class Http(
        val host: String = System.getenv("HOST") ?: "0.0.0.0",
        val port: Int = System.getenv("PORT")?.toIntOrNull() ?: PORT,
    )

    data class Auth(
        val secret: String = System.getenv("JWT_SECRET") ?: AUTH_SECRET,
        val issuer: String = System.getenv("JWT_ISSUER") ?: AUTH_ISSUER,
        val jwtRealm: String = System.getenv("JWT_REALM") ?: REALM,
    )

    data class ServerConfig(
        val preWait: Duration = 30.seconds,
        val grace: Duration = 500.milliseconds,
        val timeout: Duration = 500.milliseconds,
    )
}
