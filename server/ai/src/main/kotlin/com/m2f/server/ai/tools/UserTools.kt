package com.m2f.server.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.m2f.server.auth.repository.UserRepository

/**
 * Annotation-based ToolSet that gives agents access to user data from the database.
 * Tools accept UserRepository via constructor (Koin-injected in plan 06-03).
 * Tool methods return String because Koog serializes tool results as text for the LLM.
 */
@LLMDescription("Tools for querying user information from the application database")
class UserTools(
    private val userRepository: UserRepository,
) : ToolSet {

    @Tool
    @LLMDescription("Look up a user's profile by their email address. Returns user name, email, and role.")
    suspend fun getUserByEmail(
        @LLMDescription("The email address of the user to look up")
        email: String
    ): String {
        val user = userRepository.findByEmail(email)
            ?: return "No user found with email: $email"
        return "User: ${user.name} (${user.email}), role: ${user.role}"
    }

    @Tool
    @LLMDescription("Count the total number of registered users in the system")
    suspend fun getUserCount(): String {
        val count = userRepository.count()
        return "Total registered users: $count"
    }
}
