package com.m2f.core.config.configuration

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val PORT: Int = 8080
private const val AUTH_SECRET: String = "ThisIsAReallyReallyReallyStrongSecretKeyForJWT123!@#$%^&*()"
private const val AUTH_AUDIENCE: String = "jwt-audience"
private const val AUTH_ISSUER: String = "IssuerName"
private const val REALM: String = "Access to Your Application"
private const val ACCESS_TOKEN_EXPIRY: Long = 900000L // 15 minutes in milliseconds
private const val REFRESH_TOKEN_EXPIRY: Long = 604800000L // 7 days in milliseconds

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
        val audience: String = System.getenv("JWT_AUDIENCE") ?: AUTH_AUDIENCE,
        val issuer: String = System.getenv("JWT_ISSUER") ?: AUTH_ISSUER,
        val jwtRealm: String = System.getenv("JWT_REALM") ?: REALM,
        val accessTokenExpiry: Long = System.getenv("JWT_ACCESS_EXPIRY")?.toLongOrNull()
            ?: ACCESS_TOKEN_EXPIRY,
        val refreshTokenExpiry: Long = System.getenv("JWT_REFRESH_EXPIRY")?.toLongOrNull()
            ?: REFRESH_TOKEN_EXPIRY,
    )

    data class ServerConfig(
        val preWait: Duration = 30.seconds,
        val grace: Duration = 500.milliseconds,
        val timeout: Duration = 500.milliseconds,
    )
}
