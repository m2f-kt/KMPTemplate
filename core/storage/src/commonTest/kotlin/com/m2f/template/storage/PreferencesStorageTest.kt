package com.m2f.template.storage

import io.kotest.matchers.shouldBe
import kotlin.test.Test

/**
 * Round-trip tests for [PreferencesStorage]. Each preference must expose its documented default and
 * must survive an "app restart" — modelled here as a fresh [PreferencesStorage] reading the same
 * backing [com.russhwolf.settings.Settings] (proves the value is persisted, not just held in the
 * in-memory StateFlow).
 */
class PreferencesStorageTest {

    @Test
    fun `theme defaults to system`() {
        PreferencesStorage(InMemorySettings()).theme shouldBe "system"
    }

    @Test
    fun `theme persists across a fresh PreferencesStorage over the same settings`() {
        val settings = InMemorySettings()
        PreferencesStorage(settings).theme = "dark"
        PreferencesStorage(settings).theme shouldBe "dark"
    }

    @Test
    fun `language defaults to en`() {
        PreferencesStorage(InMemorySettings()).language shouldBe "en"
    }

    @Test
    fun `language persists across a fresh PreferencesStorage over the same settings`() {
        val settings = InMemorySettings()
        PreferencesStorage(settings).language = "es"
        PreferencesStorage(settings).language shouldBe "es"
    }

    @Test
    fun `telemetryConsent defaults to false`() {
        PreferencesStorage(InMemorySettings()).telemetryConsent shouldBe false
    }

    @Test
    fun `telemetryConsent persists across a fresh PreferencesStorage over the same settings`() {
        val settings = InMemorySettings()
        PreferencesStorage(settings).telemetryConsent = true
        // A new instance reads the persisted value — proves the toggle survives an app restart, not
        // just the in-memory StateFlow.
        PreferencesStorage(settings).telemetryConsent shouldBe true
    }
}
