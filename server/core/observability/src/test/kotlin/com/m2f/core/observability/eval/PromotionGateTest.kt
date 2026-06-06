package com.m2f.core.observability.eval

import com.m2f.core.observability.langfuse.LangfusePromptClient
import com.m2f.core.observability.langfuse.LangfuseRestClient
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * The promotion gate is the door that reconciles "centralize + iterate" with "no regression ships".
 * The truth table proves a version reaches `production` ONLY when corpus + every judge + the score
 * threshold all clear, and that [PromptPromoter] performs the label move exclusively in that case.
 */
class PromotionGateTest {

    private val passing = ExperimentSummary(
        perJudge = listOf(JudgeAggregate(FaithfulnessJudge, meanScore = 0.9, passRate = 1.0, allPass = true)),
        sampleCount = 1,
    )

    @Test
    fun `shouldPromote requires corpus, all-pass judges, and the score threshold`() {
        PromotionGate.shouldPromote(corpusPassed = true, experiment = passing, minMeanScore = 0.8).shouldBeTrue()
        PromotionGate.shouldPromote(corpusPassed = false, experiment = passing, minMeanScore = 0.8).shouldBeFalse()

        val judgeFailed = passing.copy(
            perJudge = listOf(JudgeAggregate(FaithfulnessJudge, meanScore = 0.9, passRate = 0.5, allPass = false)),
        )
        PromotionGate.shouldPromote(corpusPassed = true, experiment = judgeFailed, minMeanScore = 0.8).shouldBeFalse()
        PromotionGate.shouldPromote(corpusPassed = true, experiment = passing, minMeanScore = 0.95).shouldBeFalse()
        PromotionGate.shouldPromote(
            corpusPassed = true,
            experiment = ExperimentSummary(perJudge = emptyList(), sampleCount = 0),
            minMeanScore = 0.0,
        ).shouldBeFalse()
    }

    private class PatchCapture {
        var patched = false
        var path: String? = null
    }

    private fun promoter(capture: PatchCapture): PromptPromoter {
        val engine = MockEngine { request ->
            if (request.method == HttpMethod.Patch) {
                capture.patched = true
                capture.path = request.url.encodedPath
            }
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val http = HttpClient(engine) { install(ContentNegotiation) { json() } }
        return PromptPromoter(LangfusePromptClient(LangfuseRestClient(http, "https://lf.test", "pk", "sk")))
    }

    @Test
    fun `promoteIfPassed PATCHes the production label only when the gate passes`() = runTest {
        val pass = PatchCapture()
        val promoted = promoter(pass).promoteIfPassed(
            name = "example",
            version = 2,
            corpusPassed = true,
            experiment = passing,
            minMeanScore = 0.8,
        )
        promoted.shouldBeTrue()
        pass.patched.shouldBeTrue()
        pass.path shouldBe "/api/public/v2/prompts/example/versions/2"

        val blocked = PatchCapture()
        val blockedResult = promoter(blocked).promoteIfPassed(
            name = "example",
            version = 2,
            corpusPassed = false,
            experiment = passing,
            minMeanScore = 0.8,
        )
        blockedResult.shouldBeFalse()
        blocked.patched.shouldBeFalse()
    }
}
