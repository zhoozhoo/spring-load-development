package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.json.McpJsonMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * MCP server configuration for web client and JSON mapper.
 * <p>
 * Tool registration is handled automatically by MCP auto-configuration.
 * 
 * @author Zhubin Salehi
 */
@Configuration
public class McpServerConfig {

    /**
     * WebClient for Keycloak authentication with observation support.
     *
     * @param observationRegistry registry for metrics and traces
     * @return configured WebClient instance
     */
    @Bean
    public WebClient keycloakWebClient(ObservationRegistry observationRegistry) {
        return WebClient.builder().observationRegistry(observationRegistry).build();
    }

    /**
     * ObservationRegistry for metrics and tracing.
     *
     * @return new ObservationRegistry instance
     */
    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    /**
     * MCP JSON mapper using Spring-managed JsonMapper (Jackson 3).
     * <p>
     * Ensures all Jackson modules (especially {@link ca.zhoozhoo.loaddev.common.jackson.QuantityModule})
     * are applied during MCP serialization/deserialization of {@code Quantity<?>} types.
     *
     * @param jsonMapper the Spring Boot auto-configured JsonMapper with all modules
     * @return customized McpJsonMapper
     * @see SpringObjectMapperMcpJsonMapper
     */
    @Bean
    @Primary
    public McpJsonMapper mcpJsonMapper(JsonMapper jsonMapper) {
        return new SpringObjectMapperMcpJsonMapper(jsonMapper);
    }
}
