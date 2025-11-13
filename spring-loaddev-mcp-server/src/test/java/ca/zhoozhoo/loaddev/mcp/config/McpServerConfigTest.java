package ca.zhoozhoo.loaddev.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.json.McpJsonMapper;

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
    private ObjectMapper objectMapper;

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
    void mcpJsonMapperIsSpringObjectMapperWrapper() {
        assertThat(mcpJsonMapper).isNotNull();
        assertThat(mcpJsonMapper).isInstanceOf(SpringObjectMapperMcpJsonMapper.class);
    }

    @Test
    void mcpJsonMapperUsesSpringObjectMapper() throws Exception {
        var mapper = mcpServerConfig.mcpJsonMapper(objectMapper);
        assertThat(mapper).isInstanceOf(SpringObjectMapperMcpJsonMapper.class);
        
        // Verify it can serialize/deserialize
        var json = mapper.writeValueAsString("test");
        assertThat(json).isEqualTo("\"test\"");
    }

    @Test
    void mcpJsonMapperHasQuantitySupport() throws Exception {
        // Verify the mapper uses an ObjectMapper with QuantityModule registered
        var moduleIds = objectMapper.getRegisteredModuleIds();
        assertThat(moduleIds).anyMatch(id -> id.toString().contains("UnitJsonSerializationModule"));
    }
}
