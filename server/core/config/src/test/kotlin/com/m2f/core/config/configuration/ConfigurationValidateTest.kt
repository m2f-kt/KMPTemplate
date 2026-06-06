package com.m2f.core.config.configuration

import com.m2f.core.config.server.BootError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class ConfigurationValidateTest {

    private fun env(
        baseUrl: String = "http://localhost:8080",
        appUrl: String = "http://localhost:8081",
        secret: String = "a-real-and-sufficiently-long-production-secret",
    ): Env = Env(
        http = Env.Http(baseUrl = baseUrl, appUrl = appUrl),
        auth = Env.Auth(secret = secret),
    )

    // --- (a) JWT secret placeholder guard (production only) -------------------

    @Test
    fun `validate raises MissingRequiredEnv for JWT_SECRET when placeholder used in production`() {
        val invalid = env(baseUrl = "https://api.example.com", secret = DEFAULT_JWT_SECRET)
        val err = Configuration.validate(invalid).shouldBeLeft()
        err.shouldBeInstanceOf<BootError.MissingRequiredEnv>()
        err.name shouldBe "JWT_SECRET"
    }

    @Test
    fun `validate allows the placeholder JWT secret on a local host`() {
        val local = env(baseUrl = "http://localhost:8080", secret = DEFAULT_JWT_SECRET)
        Configuration.validate(local).shouldBeRight() shouldBe local
    }

    @Test
    fun `validate allows a real JWT secret in production`() {
        val prod = env(baseUrl = "https://api.example.com", secret = "real-prod-secret-value")
        Configuration.validate(prod).shouldBeRight() shouldBe prod
    }

    // --- (b) URL parse guard -------------------------------------------------

    @Test
    fun `validate raises InvalidConfig when baseUrl does not parse`() {
        val invalid = env(baseUrl = "not a url")
        val err = Configuration.validate(invalid).shouldBeLeft()
        err.shouldBeInstanceOf<BootError.InvalidConfig>()
        err.field shouldBe "http.baseUrl"
    }

    @Test
    fun `validate raises InvalidConfig when appUrl has no scheme or host`() {
        val invalid = env(appUrl = "://missing-scheme")
        val err = Configuration.validate(invalid).shouldBeLeft()
        err.shouldBeInstanceOf<BootError.InvalidConfig>()
        err.field shouldBe "http.appUrl"
    }

    @Test
    fun `validate returns Right for a fully valid production config`() {
        val valid = env(
            baseUrl = "https://api.example.com",
            appUrl = "https://app.example.com",
            secret = "real-prod-secret-value",
        )
        Configuration.validate(valid).shouldBeRight() shouldBe valid
    }
}
