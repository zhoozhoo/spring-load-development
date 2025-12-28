package ca.zhoozhoo.loaddev.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;


import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.json.McpJsonMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests for McpServerConfig to verify MCP server bean configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
class McpServerConfigTest {

    @Autowired
    private McpServerConfig mcpServerConfig;

    @Autowired
    private ObservationRegistry observationRegistry;

    @Autowired
    private WebClient keycloakWebClient;

    @Autowired
    private McpJsonMapper mcpJsonMapper;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void contextLoads() {
        assertThat(mcpServerConfig).isNotNull();
    }

    @Test
    void observationRegistryBeanIsCreated() {
        assertThat(observationRegistry).isNotNull();
    }

    @Test
    void observationRegistryFromConfigIsUsable() {
        var registry = mcpServerConfig.observationRegistry();
        assertThat(registry).isNotNull();
        
        // Verify it can create observations
        assertThat(registry.getCurrentObservation()).isNull(); // No observation active initially
    }

    @Test
    void keycloakWebClientIsConfigured() {
        assertThat(keycloakWebClient).isNotNull();
    }

    @Test
    void keycloakWebClientCanBeBuilt() {
        var client = mcpServerConfig.keycloakWebClient(observationRegistry);
        assertThat(client).isNotNull();
    }

    @Test
    void mcpJsonMapperIsSpringJsonMapperWrapper() {
        assertThat(mcpJsonMapper).isNotNull();
        assertThat(mcpJsonMapper).isInstanceOf(SpringObjectMapperMcpJsonMapper.class);
    }

    @Test
    void mcpJsonMapperUsesSpringJsonMapper() throws Exception {
        var mapper = mcpServerConfig.mcpJsonMapper(jsonMapper);
        assertThat(mapper).isInstanceOf(SpringObjectMapperMcpJsonMapper.class);
        
        // Verify it can serialize/deserialize
        var json = mapper.writeValueAsString("test");
        assertThat(json).isEqualTo("\"test\"");
    }

    @Test
    void mcpJsonMapperHasQuantitySupport() {
        // Verify the mapper is properly initialized and can be used
        // (QuantityModule support is tested in integration tests with actual Quantity objects)
        var mapper = mcpServerConfig.mcpJsonMapper(jsonMapper);
        assertThat(mapper).isNotNull();
        assertThat(mapper).isInstanceOf(SpringObjectMapperMcpJsonMapper.class);
    }
}
