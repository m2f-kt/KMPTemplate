package com.m2f.core.config.server.localization

object ServerStrings {
    private val strings: Map<String, Map<String, String>> = mapOf(
        "en" to mapOf(
            // Auth errors
            "AUTH_INVALID_CREDENTIALS" to "Email or password is incorrect",
            "AUTH_TOKEN_EXPIRED" to "Authentication token has expired",
            "AUTH_TOKEN_INVALID" to "Authentication token is invalid",
            "AUTH_UNAUTHORIZED" to "Authentication required",
            "AUTH_USER_ALREADY_EXISTS" to "A user with this email already exists",
            // User errors
            "USER_NOT_FOUND" to "User not found",
            "USER_FORBIDDEN" to "You do not have permission to access this resource",
            // Group errors
            "GROUP_NOT_FOUND" to "Group not found",
            "GROUP_FORBIDDEN" to "You do not have permission to access this group",
            "GROUP_ALREADY_EXISTS" to "A group with this slug already exists",
            "GROUP_MEMBER_ALREADY_EXISTS" to "User is already a member of this group",
            // Validation errors
            "VALIDATION_INVALID_FIELD" to "Invalid field value",
            "VALIDATION_INVALID_INPUT" to "Invalid input",
            "VALIDATION_MISSING_FIELD" to "Required field is missing",
            // Server errors
            "SERVER_INTERNAL_ERROR" to "An unexpected error occurred",
            "SERVER_SERVICE_UNAVAILABLE" to "Service is temporarily unavailable",
        ),
        "es" to mapOf(
            // Auth errors
            "AUTH_INVALID_CREDENTIALS" to "El correo o la contraseña son incorrectos",
            "AUTH_TOKEN_EXPIRED" to "El token de autenticación ha expirado",
            "AUTH_TOKEN_INVALID" to "El token de autenticación no es válido",
            "AUTH_UNAUTHORIZED" to "Se requiere autenticación",
            "AUTH_USER_ALREADY_EXISTS" to "Ya existe un usuario con este correo electrónico",
            // User errors
            "USER_NOT_FOUND" to "Usuario no encontrado",
            "USER_FORBIDDEN" to "No tienes permiso para acceder a este recurso",
            // Group errors
            "GROUP_NOT_FOUND" to "Grupo no encontrado",
            "GROUP_FORBIDDEN" to "No tienes permiso para acceder a este grupo",
            "GROUP_ALREADY_EXISTS" to "Ya existe un grupo con este identificador",
            "GROUP_MEMBER_ALREADY_EXISTS" to "El usuario ya es miembro de este grupo",
            // Validation errors
            "VALIDATION_INVALID_FIELD" to "Valor de campo no válido",
            "VALIDATION_INVALID_INPUT" to "Entrada no válida",
            "VALIDATION_MISSING_FIELD" to "Falta un campo obligatorio",
            // Server errors
            "SERVER_INTERNAL_ERROR" to "Ha ocurrido un error inesperado",
            "SERVER_SERVICE_UNAVAILABLE" to "El servicio no está disponible temporalmente",
        ),
    )

    /**
     * Resolve a localized message for the given error code and locale.
     * Falls back to English, then to the raw code if no translation found.
     */
    fun resolve(code: String, locale: String): String {
        val lang = locale.take(2).lowercase()
        return strings[lang]?.get(code)
            ?: strings["en"]?.get(code)
            ?: code
    }
}
