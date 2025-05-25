package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import ca.zhoozhoo.loaddev.mcp.tools.LoadTools;
import io.micrometer.observation.ObservationRegistry;

/**
 * Configuration class for Model Context Protocol (MCP) server.
 * Provides beans for tool callbacks and web client configuration.
 */
@Configuration
public class McpServerConfig {

    /**
     * Creates a ToolCallbackProvider for Load operations.
     * This bean enables the MCP server to discover and execute tool methods
     * defined in the LoadTools class.
     *
     * @param loadTools the LoadTools instance containing tool methods
     * @return configured ToolCallbackProvider
     */
    @Bean
    public ToolCallbackProvider loadToolsCallbackProvider(LoadTools loadTools) {
        return MethodToolCallbackProvider.builder().toolObjects(loadTools).build();
    }

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
