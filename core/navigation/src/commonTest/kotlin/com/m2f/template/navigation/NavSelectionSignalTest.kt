package com.m2f.template.navigation

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class NavSelectionSignalTest {

    @Test
    fun `starts with no pending request`() {
        NavSelectionSignal().requestedNavKey.value shouldBe null
    }

    @Test
    fun `request sets the pending nav key`() {
        val signal = NavSelectionSignal()
        signal.request("settings")
        signal.requestedNavKey.value shouldBe "settings"
    }

    @Test
    fun `consume clears the pending request`() {
        val signal = NavSelectionSignal()
        signal.request("settings")
        signal.consume()
        signal.requestedNavKey.value shouldBe null
    }

    @Test
    fun `request is idempotent and last write wins`() {
        val signal = NavSelectionSignal()
        signal.request("settings")
        signal.request("settings")
        signal.requestedNavKey.value shouldBe "settings"
        signal.request("home")
        signal.requestedNavKey.value shouldBe "home"
    }
}
