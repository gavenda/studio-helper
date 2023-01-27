package bogus.lib.metrics

import com.kotlindiscord.kord.extensions.utils.envOrNull
import com.kotlindiscord.kord.extensions.utils.loadModule
import com.sun.net.httpserver.HttpServer
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.exporter.HTTPServer
import mu.KotlinLogging
import java.net.InetSocketAddress

/**
 * Sets up metrics for this bot.
 */
suspend fun setupMetrics() {
    val log = KotlinLogging.logger { }
    val metricsPort = envOrNull("METRICS_PORT")?.toInt() ?: 8080

    loadModule {
        single<MeterRegistry>(createdAtStart = true) {
            PrometheusMeterRegistry(PrometheusConfig.DEFAULT).apply {
                HTTPServer(InetSocketAddress(metricsPort), prometheusRegistry, true)
                log.info { "Metrics port started @ port $metricsPort" }
            }
        }
    }
}
