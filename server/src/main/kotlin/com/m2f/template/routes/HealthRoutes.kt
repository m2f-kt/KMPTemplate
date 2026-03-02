package com.m2f.template.routes

import com.m2f.core.config.configuration.Env
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

@Serializable
data class HealthStatus(
    val status: String,
    val services: Map<String, ServiceHealth>,
)

@Serializable
data class ServiceHealth(
    val status: String,
    val message: String? = null,
)

fun Route.healthRoutes(database: R2dbcDatabase, env: Env) {
    get("/health") {
        val services = mutableMapOf<String, ServiceHealth>()

        // Check DB
        services["database"] = try {
            suspendTransaction(db = database) {
                exec("SELECT 1")
            }
            ServiceHealth("up")
        } catch (e: Exception) {
            ServiceHealth("down", e.message)
        }

        // Check MinIO
        services["minio"] = try {
            val url = java.net.URI("${env.s3.endpoint}/minio/health/live").toURL()
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 2000
            connection.readTimeout = 2000
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            connection.disconnect()
            if (responseCode == 200) ServiceHealth("up")
            else ServiceHealth("down", "HTTP $responseCode")
        } catch (e: Exception) {
            ServiceHealth("down", e.message)
        }

        // Check SMTP
        services["smtp"] = try {
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(env.email.host, env.email.port), 2000)
            }
            ServiceHealth("up")
        } catch (e: Exception) {
            ServiceHealth("down", e.message)
        }

        val allUp = services.values.all { it.status == "up" }
        val status = HealthStatus(
            status = if (allUp) "ok" else "degraded",
            services = services,
        )
        val httpStatus = if (allUp) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
        call.respond(httpStatus, status)
    }
}
