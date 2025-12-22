package ca.zhoozhoo.loaddev.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

import ca.zhoozhoo.loaddev.common.opentelemetry.ContextPropagationConfiguration;
import ca.zhoozhoo.loaddev.common.opentelemetry.OpenTelemetryConfiguration;

/**
 * Main application class for the Spring Load Development API Gateway.
 * 
 * <p>This gateway serves as the single entry point for all client requests,
 * routing them to appropriate microservices (loads, rifles, components, mcp-server).</p>
 * 
 * <p><b>Auto-Configuration Exclusions:</b></p>
 * <ul>
 *   <li>{@code SimpleMetricsExportAutoConfiguration} - Excluded to prevent SimpleMeterRegistry
 *       from being created. We use OpenTelemetry for metrics export instead.</li>
 * </ul>
 * 
 * <p><b>OpenTelemetry Integration:</b></p>
 * <p>The gateway imports OpenTelemetry configurations for comprehensive observability:</p>
 * <ul>
 *   <li>{@link ca.zhoozhoo.loaddev.common.opentelemetry.OpenTelemetryConfiguration} - Configures
 *       OpenTelemetry SDK with OTLP exporters for metrics, traces, and logs</li>
 *   <li>{@link ca.zhoozhoo.loaddev.common.opentelemetry.ContextPropagationConfiguration} - Enables
 *       automatic propagation of trace context (W3C Trace Context) across service boundaries</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 * @see ca.zhoozhoo.loaddev.common.opentelemetry.OpenTelemetryConfiguration
 * @see ca.zhoozhoo.loaddev.common.opentelemetry.ContextPropagationConfiguration
 */
@SpringBootApplication(excludeName = {
        "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
})
@EnableDiscoveryClient
@Import({OpenTelemetryConfiguration.class, ContextPropagationConfiguration.class})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
