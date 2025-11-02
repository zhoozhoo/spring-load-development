package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.observation.ObservationRegistry;

/**
 * Configuration class for Model Context Protocol (MCP) server.
 * Provides beans for web client configuration.
 * <p>
 * Note: Tool registration is handled automatically by MCP auto-configuration
 * which discovers @McpTool annotated methods.
 * 
 * @author Zhubin Salehi
 */
@Configuration
public class McpServerConfig {

    /**
     * Creates a WebClient for Keycloak authentication server communication.
     * Configured with observation support for monitoring and metrics.
     *
     * @param observationRegistry registry for collecting metrics and traces
     * @return configured WebClient instance
     */
    @Bean
    public WebClient keycloakWebClient(ObservationRegistry observationRegistry) {
        return WebClient.builder().observationRegistry(observationRegistry).build();
    }

    /**
     * Creates an ObservationRegistry for metrics and tracing.
     * Used for monitoring WebClient operations and other observable components.
     *
     * @return new ObservationRegistry instance
     */
    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }
}
