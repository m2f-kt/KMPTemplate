package com.m2f.core.config.server

/**
 * Errors that prevent the server from starting. Distinct from [DomainError]
 * (which translates request-time errors into HTTP responses): a [BootError]
 * crashes startup with a clear human-readable message so the operator knows
 * exactly which env var to set or which config field to fix before retrying.
 */
sealed interface BootError {
    val message: String

    /**
     * A required environment variable is unset (or left at an unsafe placeholder
     * default) in a context where it must be explicitly provided.
     *
     * @property name the env var the operator must set, e.g. `JWT_SECRET`.
     */
    data class MissingRequiredEnv(val name: String) : BootError {
        override val message: String =
            "Refusing to start: required environment variable $name is missing or " +
                "left at its insecure default. Set $name to a real value before starting."
    }

    /**
     * A config value is present but malformed (e.g. an unparseable URL).
     *
     * @property field the logical config field, e.g. `http.baseUrl`.
     * @property reason a human-readable explanation of why it is invalid.
     */
    data class InvalidConfig(val field: String, val reason: String) : BootError {
        override val message: String =
            "Refusing to start: invalid configuration for $field — $reason."
    }
}
