package ca.zhoozhoo.loaddev.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.json.McpJsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    /**
     * Customizes the MCP JSON mapper to use the Spring-managed ObjectMapper so that
     * all registered Jackson modules (e.g. QuantityModule) are applied during MCP
     * tool/resource serialization and deserialization.
     *
     * Without this, the default McpJsonMapper#createDefault() would miss custom
     * modules, causing serialization failures for Quantity types.
     */
    @Bean
    @Primary // Ensure our mapper is preferred if multiple McpJsonMapper beans exist
    public McpJsonMapper mcpJsonMapper(ObjectMapper objectMapper) {
        return new SpringObjectMapperMcpJsonMapper(objectMapper);
    }

    @Bean
    public CommandLineRunner logMcpMappers(org.springframework.context.ApplicationContext ctx) {
        return args -> {
            var mapperBeans = ctx.getBeansOfType(McpJsonMapper.class);
            System.out.println("[MCP] Discovered McpJsonMapper beans:" );
            mapperBeans.forEach((name, mapper) -> {
                System.out.println("  beanName=" + name + ", class=" + mapper.getClass().getName() + ", identityHash=" + System.identityHashCode(mapper));
            });
        };
    }
}
