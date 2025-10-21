package ca.zhoozhoo.loaddev.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the Spring Load Development API Gateway.
 * 
 * <p>This gateway serves as the single entry point for all client requests,
 * routing them to appropriate microservices (loads, rifles, components, mcp-server).</p>
 * 
 * <p><b>Auto-Configuration Exclusions:</b></p>
 * <ul>
 *   <li>{@code SimpleMetricsExportAutoConfiguration} - Excluded to prevent SimpleMeterRegistry
 *       from being created. We use OpenTelemetry for metrics export instead, configured in
 *       {@link ca.zhoozhoo.loaddev.api.config.MetricsConfiguration}.</li>
 * </ul>
 * 
 * @author Zhubin Salehi
 */
@SpringBootApplication(excludeName = {
        "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
})
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
