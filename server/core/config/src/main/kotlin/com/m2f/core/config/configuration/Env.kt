package com.m2f.core.config.configuration

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val PORT: Int = 8080
private const val AUTH_SECRET: String = "ThisIsAReallyReallyReallyStrongSecretKeyForJWT123!@#$%^&*()"
private const val AUTH_AUDIENCE: String = "jwt-audience"
private const val AUTH_ISSUER: String = "IssuerName"
private const val REALM: String = "Access to Your Application"
private const val ACCESS_TOKEN_EXPIRY: Long = 86400000L // 1 day in milliseconds (24 * 60 * 60 * 1000)
private const val REFRESH_TOKEN_EXPIRY: Long = 2592000000L // 30 days in milliseconds (30 * 24 * 60 * 60 * 1000)

data class Env(
    val http: Http = Http(),
    val auth: Auth = Auth(),
    val oauth: OAuth = OAuth(),
    val ai: Ai = Ai(),
    val s3: S3 = S3(),
    val email: Email = Email(),
    val serverConfig: ServerConfig = ServerConfig(),
) {
    data class Http(
        val host: String = System.getenv("HOST") ?: "0.0.0.0",
        val port: Int = System.getenv("PORT")?.toIntOrNull() ?: PORT,
        val baseUrl: String = System.getenv("BASE_URL") ?: "http://localhost:$port",
        /** Frontend app URL for generating user-facing links (e.g., invitation links). */
        val appUrl: String = System.getenv("APP_URL") ?: "http://localhost:8080",
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

    /**
     * OAuth provider configuration.
     * Empty defaults mean OAuth is non-functional until env vars are configured.
     */
    data class OAuth(
        val googleClientId: String = System.getenv("GOOGLE_CLIENT_ID") ?: "",
        val googleClientSecret: String = System.getenv("GOOGLE_CLIENT_SECRET") ?: "",
        val appleClientId: String = System.getenv("APPLE_CLIENT_ID") ?: "",
        val appleClientSecret: String = System.getenv("APPLE_CLIENT_SECRET") ?: "",
        val appleTeamId: String = System.getenv("APPLE_TEAM_ID") ?: "",
        val appleKeyId: String = System.getenv("APPLE_KEY_ID") ?: "",
        /** WASM app URL for OAuth callback redirect. */
        val wasmRedirectUrl: String = System.getenv("OAUTH_WASM_REDIRECT_URL")
            ?: "http://localhost:8080/auth/callback",
        /** Custom URL scheme for Android/iOS deep link callbacks. */
        val mobileScheme: String = System.getenv("OAUTH_MOBILE_SCHEME") ?: "template",
        /** Localhost port for desktop (JVM) OAuth callback. */
        val desktopLocalhostPort: Int = System.getenv("OAUTH_DESKTOP_LOCALHOST_PORT")?.toIntOrNull()
            ?: 9876,
    )

    /**
     * AI provider configuration.
     * Defaults to disabled so the server starts without any AI env vars set.
     * Set AI_ENABLED=true and GOOGLE_API_KEY to activate AI endpoints.
     */
    data class Ai(
        val enabled: Boolean = System.getenv("AI_ENABLED")?.toBooleanStrictOrNull() ?: false,
        val googleApiKey: String = System.getenv("GOOGLE_API_KEY") ?: "",
    )

    /**
     * S3-compatible object storage configuration.
     * Defaults point to local MinIO from docker-compose.yml.
     * Set env vars for production S3/R2/GCS deployments.
     */
    data class S3(
        val endpoint: String = System.getenv("S3_ENDPOINT") ?: "http://localhost:9002",
        val bucket: String = System.getenv("S3_BUCKET") ?: "uploads",
        val region: String = System.getenv("S3_REGION") ?: "us-east-1",
        val accessKey: String = System.getenv("S3_ACCESS_KEY") ?: "minioadmin",
        val secretKey: String = System.getenv("S3_SECRET_KEY") ?: "minioadmin",
    )

    /**
     * SMTP email configuration.
     * Defaults point to local MailHog from docker-compose.yml.
     * MailHog accepts any credentials, so username/password default to empty.
     */
    data class Email(
        val host: String = System.getenv("SMTP_HOST") ?: "localhost",
        val port: Int = System.getenv("SMTP_PORT")?.toIntOrNull() ?: 1025,
        val username: String = System.getenv("SMTP_USERNAME") ?: "",
        val password: String = System.getenv("SMTP_PASSWORD") ?: "",
        val fromAddress: String = System.getenv("SMTP_FROM") ?: "noreply@template.local",
    )

    data class ServerConfig(
        val preWait: Duration = 30.seconds,
        val grace: Duration = 500.milliseconds,
        val timeout: Duration = 500.milliseconds,
    )
}
