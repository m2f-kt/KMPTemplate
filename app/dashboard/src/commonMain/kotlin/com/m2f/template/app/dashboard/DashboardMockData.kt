package com.m2f.template.app.dashboard

import org.jetbrains.compose.resources.StringResource
import template.app.dashboard.generated.resources.Res
import template.app.dashboard.generated.resources.activity_auto_scaling
import template.app.dashboard.generated.resources.activity_deploy
import template.app.dashboard.generated.resources.activity_memory_alert
import template.app.dashboard.generated.resources.activity_ssl_renewed
import template.app.dashboard.generated.resources.metric_avg_latency
import template.app.dashboard.generated.resources.metric_error_rate
import template.app.dashboard.generated.resources.metric_requests
import template.app.dashboard.generated.resources.metric_uptime

/**
 * Static mock data for the dashboard screen.
 *
 * Values are locked per user decision and must not be changed:
 * uptime 99.98%, requests 1.2M, latency 42ms, error rate 0.03%.
 */
object DashboardMockData {

    data class MetricItem(
        val labelRes: StringResource,
        val value: String,
        val change: String,
        val isUp: Boolean,
        val isHighlighted: Boolean = false,
    )

    data class ProcessItem(
        val name: String,
        val pid: Int,
        val cpu: String,
        val memory: String,
        val status: String,
    )

    data class ActivityItem(
        val titleRes: StringResource,
        val location: String,
        val time: String,
        val icon: String,
    )

    data class DeploymentStatus(
        val build: Float,
        val tests: Float,
        val deploy: Float,
    )

    val metrics = listOf(
        MetricItem(Res.string.metric_uptime, "99.98%", "+0.02%", true),
        MetricItem(Res.string.metric_requests, "1.2M", "+18.3%", true),
        MetricItem(Res.string.metric_avg_latency, "42ms", "-8ms", false, isHighlighted = true),
        MetricItem(Res.string.metric_error_rate, "0.03%", "-0.01%", false),
    )

    val processes = listOf(
        ProcessItem("node server.js", 1337, "12.4%", "256MB", "running"),
        ProcessItem("postgres", 2048, "3.2%", "512MB", "running"),
        ProcessItem("redis-server", 3001, "1.8%", "128MB", "running"),
        ProcessItem("nginx", 80, "0.5%", "64MB", "running"),
        ProcessItem("cron-worker", 4200, "8.1%", "192MB", "running"),
    )

    val activities = listOf(
        ActivityItem(Res.string.activity_deploy, "prod", "2 min ago", "git-commit-horizontal"),
        ActivityItem(Res.string.activity_memory_alert, "worker-3", "12 min ago", "triangle-alert"),
        ActivityItem(Res.string.activity_ssl_renewed, "*.example.com", "1h ago", "circle-check"),
        ActivityItem(Res.string.activity_auto_scaling, "cluster-2", "3h ago", "arrow-up-right"),
    )

    val deployment = DeploymentStatus(build = 1.0f, tests = 1.0f, deploy = 0.78f)
}
