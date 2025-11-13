package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.json.McpJsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration class for Model Context Protocol (MCP) server.
 * Provides beans for web client configuration and MCP JSON mapper customization.
 * <p>
 * Note: Tool registration is handled automatically by MCP auto-configuration
 * which discovers {@code @McpTool} annotated methods.
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

    /**
     * Customizes the MCP JSON mapper to use the Spring-managed ObjectMapper.
     * <p>
     * This is <b>critical</b> for ensuring all registered Jackson modules
     * (especially {@link ca.zhoozhoo.loaddev.common.jackson.QuantityModule})
     * are applied during MCP tool/resource serialization and deserialization.
     * <p>
     * Without this customization, the default {@code McpJsonMapper.createDefault()}
     * would create its own ObjectMapper instance that lacks custom modules,
     * causing serialization failures when handling {@code Quantity<?>} types
     * in load and rifle DTOs.
     *
     * @param objectMapper the Spring Boot auto-configured ObjectMapper with all modules
     * @return customized McpJsonMapper wrapping the Spring ObjectMapper
     * @see SpringObjectMapperMcpJsonMapper
     */
    @Bean
    @Primary
    public McpJsonMapper mcpJsonMapper(ObjectMapper objectMapper) {
        return new SpringObjectMapperMcpJsonMapper(objectMapper);
    }
}
