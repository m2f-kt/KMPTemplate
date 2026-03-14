package com.m2f.server.privacy.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

interface PrivacyJob {
    val name: String
    val interval: Duration get() = 24.hours
    suspend fun execute()
}

class PrivacyJobScheduler(
    private val scope: CoroutineScope,
    private val jobs: List<PrivacyJob>,
) {
    fun start() {
        jobs.forEach { job ->
            scope.launch {
                while (true) {
                    try {
                        job.execute()
                    } catch (_: Exception) {
                        // Log and continue
                    }
                    delay(job.interval)
                }
            }
        }
    }
}
