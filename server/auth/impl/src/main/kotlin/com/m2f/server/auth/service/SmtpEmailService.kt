package com.m2f.server.auth.service

import com.m2f.server.auth.contract.service.EmailService
import com.m2f.core.config.configuration.Configuration
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

class SmtpEmailService(
    private val config: Configuration,
) : EmailService {

    override suspend fun sendEmail(to: String, subject: String, body: String) {
        withContext(Dispatchers.IO) {
            val emailConfig = config.env.email
            val properties = Properties().apply {
                put("mail.smtp.host", emailConfig.host)
                put("mail.smtp.port", emailConfig.port.toString())
                put("mail.smtp.auth", (emailConfig.username.isNotEmpty()).toString())
                put("mail.smtp.starttls.enable", "false") // MailHog doesn't need TLS
            }

            val session = if (emailConfig.username.isNotEmpty()) {
                Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication() =
                        PasswordAuthentication(emailConfig.username, emailConfig.password)
                })
            } else {
                Session.getInstance(properties)
            }

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(emailConfig.fromAddress))
                setRecipient(Message.RecipientType.TO, InternetAddress(to))
                setSubject(subject)
                setText(body)
            }

            Transport.send(message)
        }
    }
}
