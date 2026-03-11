package com.m2f.server.auth

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.m2f.core.config.configuration.Configuration
import com.m2f.core.config.configuration.Env
import com.m2f.server.auth.service.SmtpEmailService
import io.kotest.matchers.shouldBe
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class EmailServiceTest {

    private lateinit var greenMail: GreenMail
    private lateinit var emailService: SmtpEmailService

    @Before
    fun setup() {
        // GreenMail with random SMTP port (port 0 = OS-assigned)
        greenMail = GreenMail(ServerSetup(0, null, ServerSetup.PROTOCOL_SMTP))
        greenMail.start()

        val smtpPort = greenMail.smtp.port

        val config = Configuration(
            env = Env(
                email = Env.Email(
                    host = "localhost",
                    port = smtpPort,
                    username = "",
                    password = "",
                    fromAddress = "noreply@test.example",
                ),
            ),
        )
        emailService = SmtpEmailService(config)
    }

    @After
    fun teardown() {
        greenMail.stop()
    }

    @Test
    fun `email is delivered with correct recipient, subject, and body`() = runTest {
        emailService.sendEmail(
            to = "user@example.com",
            subject = "Welcome!",
            body = "Hello from the integration test.",
        )

        val messages: Array<MimeMessage> = greenMail.receivedMessages
        messages.size shouldBe 1

        val msg = messages.first()
        msg.allRecipients.first().toString() shouldBe "user@example.com"
        msg.subject shouldBe "Welcome!"
        msg.content.toString().trim() shouldBe "Hello from the integration test."
    }

    @Test
    fun `email is sent from the configured from-address`() = runTest {
        emailService.sendEmail(
            to = "recipient@example.com",
            subject = "From Address Test",
            body = "Checking the from address.",
        )

        val messages: Array<MimeMessage> = greenMail.receivedMessages
        messages.size shouldBe 1

        val msg = messages.first()
        msg.from.first().toString() shouldBe "noreply@test.example"
    }
}
