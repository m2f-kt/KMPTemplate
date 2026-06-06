package com.m2f.core.config.configuration

import io.github.cdimascio.dotenv.Dotenv
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// Walk up from CWD to locate the .env file. Gradle's `:server:run` sets CWD to the
// `server/` subproject, so a project-root .env is otherwise invisible. Falling back
// to CWD preserves the previous behavior when no .env exists anywhere up the tree.
private val dotenvDirectory: String = run {
    val cwd = File(System.getProperty("user.dir"))
    generateSequence(cwd) { it.parentFile }
        .firstOrNull { File(it, ".env").isFile }
        ?.absolutePath
        ?: cwd.absolutePath
}

private val dotenv: Dotenv = Dotenv.configure()
    .directory(dotenvDirectory)
    .ignoreIfMissing()
    .load()

// System environment takes precedence over the .env file (standard 12-factor behavior): a
// launch-time override (e.g. an exported HOST=...) wins without editing the committed .env.
private fun env(key: String): String? = System.getenv(key) ?: dotenv[key]

private const val DEFAULT_HOST: String = "0.0.0.0"
private const val DEFAULT_PORT: Int = 8080
private const val DEFAULT_APP_PORT: Int = 8081

/**
 * The committed placeholder JWT secret. Safe for local development, but boot-time
 * validation refuses to start with this value in production (see
 * [com.m2f.core.config.configuration.Configuration.validate]).
 */
const val DEFAULT_JWT_SECRET: String = "ThisIsAReallyReallyReallyStrongSecretKeyForJWT123!@#$%^&*()"
private const val AUTH_AUDIENCE: String = "jwt-audience"
private const val AUTH_ISSUER: String = "IssuerName"
private const val REALM: String = "Access to Your Application"
private const val ACCESS_TOKEN_EXPIRY: Long = 86400000L // 1 day in milliseconds (24 * 60 * 60 * 1000)
private const val REFRESH_TOKEN_EXPIRY: Long = 2592000000L // 30 days in milliseconds (30 * 24 * 60 * 60 * 1000)

// Resolved once from the environment so derived URLs (BASE_URL, OAuth callbacks)
// stay consistent with HOST/PORT without each consumer re-reading dotenv.
private val resolvedHost: String = env("HOST") ?: DEFAULT_HOST
private val resolvedPort: Int = env("PORT")?.toIntOrNull() ?: DEFAULT_PORT
private val resolvedBaseUrl: String = env("BASE_URL") ?: "http://localhost:$resolvedPort"

data class Env(
    val http: Http = Http(),
    val auth: Auth = Auth(),
    val oauth: OAuth = OAuth(),
    val ai: Ai = Ai(),
    val observability: Observability = Observability(),
    val s3: S3 = S3(),
    val email: Email = Email(),
    val serverConfig: ServerConfig = ServerConfig(),
) {
    data class Http(
        val host: String = resolvedHost,
        val port: Int = resolvedPort,
        val baseUrl: String = resolvedBaseUrl,
        /** Frontend app URL for generating user-facing links (e.g., invitation links). */
        val appUrl: String = env("APP_URL") ?: "http://localhost:$DEFAULT_APP_PORT",
        /**
         * Comma-separated `host:port` entries appended to CORS `allowHost`. Use
         * to whitelist additional dev origins (e.g. a LAN IP for phone testing).
         * Example: `CORS_ALLOWED_ORIGINS=192.168.3.5:8080,my-laptop.local:8080`
         */
        val corsAllowedOrigins: List<String> = env("CORS_ALLOWED_ORIGINS")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList(),
    )

    data class Auth(
        val secret: String = env("JWT_SECRET") ?: DEFAULT_JWT_SECRET,
        val audience: String = env("JWT_AUDIENCE") ?: AUTH_AUDIENCE,
        val issuer: String = env("JWT_ISSUER") ?: AUTH_ISSUER,
        val jwtRealm: String = env("JWT_REALM") ?: REALM,
        val accessTokenExpiry: Long = env("JWT_ACCESS_EXPIRY")?.toLongOrNull()
            ?: ACCESS_TOKEN_EXPIRY,
        val refreshTokenExpiry: Long = env("JWT_REFRESH_EXPIRY")?.toLongOrNull()
            ?: REFRESH_TOKEN_EXPIRY,
    )

    /**
     * OAuth provider configuration.
     * Empty defaults mean OAuth is non-functional until env vars are configured.
     */
    data class OAuth(
        val googleClientId: String = env("GOOGLE_CLIENT_ID") ?: "",
        val googleClientSecret: String = env("GOOGLE_CLIENT_SECRET") ?: "",
        val appleClientId: String = env("APPLE_CLIENT_ID") ?: "",
        val appleClientSecret: String = env("APPLE_CLIENT_SECRET") ?: "",
        val appleTeamId: String = env("APPLE_TEAM_ID") ?: "",
        val appleKeyId: String = env("APPLE_KEY_ID") ?: "",
        /** WASM app URL for OAuth callback redirect. Defaults to `${BASE_URL}/auth/callback`. */
        val wasmRedirectUrl: String = env("OAUTH_WASM_REDIRECT_URL")
            ?: "$resolvedBaseUrl/auth/callback",
        /** Custom URL scheme for Android/iOS deep link callbacks. */
        val mobileScheme: String = env("OAUTH_MOBILE_SCHEME") ?: "template",
        /** Localhost port for desktop (JVM) OAuth callback. */
        val desktopLocalhostPort: Int = env("OAUTH_DESKTOP_LOCALHOST_PORT")?.toIntOrNull()
            ?: 9876,
    )

    /**
     * AI provider configuration.
     * Defaults to disabled so the server starts without any AI env vars set.
     * Set AI_ENABLED=true and GOOGLE_API_KEY to activate AI endpoints.
     */
    data class Ai(
        val enabled: Boolean = env("AI_ENABLED")?.toBooleanStrictOrNull() ?: false,
        val googleApiKey: String = env("GOOGLE_API_KEY") ?: "",
    )

    /**
     * Observability (Langfuse + OpenTelemetry) configuration for `server:core:observability`.
     *
     * Keyless by default ⇒ [langfuseEnabled] is false ⇒ the OTLP exporter is NOT installed and the
     * agent graph traces through nothing (byte-identical to the untraced path, zero tracing cost).
     * Set both `LANGFUSE_PUBLIC_KEY` and `LANGFUSE_SECRET_KEY` to turn tracing on.
     *
     * [langfuseEnvironment] alone drives content visibility (see the observability module's
     * `traceContentAllowed`): `development` (and consented `production`) emit the transcript +
     * generation content; `production` without per-request consent withholds them (zero-retention).
     */
    data class Observability(
        /** Langfuse OTLP base URL (host only; the module appends the OTLP/REST paths). */
        val langfuseHost: String = env("LANGFUSE_HOST") ?: "https://cloud.langfuse.com",
        /** Langfuse project public key (`pk-lf-…`). Null/blank ⇒ tracing disabled. */
        val langfusePublicKey: String? = env("LANGFUSE_PUBLIC_KEY"),
        /** Langfuse project secret key (`sk-lf-…`). Null/blank ⇒ tracing disabled. */
        val langfuseSecretKey: String? = env("LANGFUSE_SECRET_KEY"),
        /** Langfuse environment tag (`development` / `production`). Drives content visibility. */
        val langfuseEnvironment: String = env("LANGFUSE_ENVIRONMENT") ?: "development",
    ) {
        /** Tracing is ON only when BOTH keys are present — keyless deploys pay zero tracing cost. */
        val langfuseEnabled: Boolean
            get() = !langfusePublicKey.isNullOrBlank() && !langfuseSecretKey.isNullOrBlank()
    }

    /**
     * S3-compatible object storage configuration.
     * Defaults point to local MinIO from docker-compose.yml.
     * Set env vars for production S3/R2/GCS deployments.
     */
    data class S3(
        val endpoint: String = env("S3_ENDPOINT") ?: "http://localhost:9002",
        val bucket: String = env("S3_BUCKET") ?: "uploads",
        val region: String = env("S3_REGION") ?: "us-east-1",
        val accessKey: String = env("S3_ACCESS_KEY") ?: "minioadmin",
        val secretKey: String = env("S3_SECRET_KEY") ?: "minioadmin",
    )

    /**
     * SMTP email configuration.
     * Defaults point to local MailHog from docker-compose.yml.
     * MailHog accepts any credentials, so username/password default to empty.
     */
    data class Email(
        val host: String = env("SMTP_HOST") ?: "localhost",
        val port: Int = env("SMTP_PORT")?.toIntOrNull() ?: 1025,
        val username: String = env("SMTP_USERNAME") ?: "",
        val password: String = env("SMTP_PASSWORD") ?: "",
        val fromAddress: String = env("SMTP_FROM") ?: "noreply@template.local",
    )

    data class ServerConfig(
        val preWait: Duration = 30.seconds,
        val grace: Duration = 500.milliseconds,
        val timeout: Duration = 500.milliseconds,
    )
}
