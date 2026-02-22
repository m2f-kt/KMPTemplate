package com.m2f.server.auth.service

/**
 * Abstraction for sending emails. Interface allows faking in tests
 * and swapping implementations (SMTP, SES, etc.) without changing consumers.
 */
interface EmailService {
    suspend fun sendEmail(to: String, subject: String, body: String)
}
