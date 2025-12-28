package ca.zhoozhoo.loaddev.mcp.provider;

import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.zhoozhoo.loaddev.mcp.config.SpringObjectMapperMcpJsonMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for {@link PreSerializationUtils}.
 * Tests all public methods and error handling paths.
 */
class PreSerializationUtilsTest {

    private McpJsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SpringObjectMapperMcpJsonMapper(new JsonMapper());
    }

    @Test
    void serializeSuccessfullyWithId() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("id", 123);
        testData.put("name", "Test Entity");

        var json = PreSerializationUtils.serialize(mapper, testData, "test-entity", 123);

        assertThat(json).isNotNull();
        assertThat(json).contains("\"id\":123");
        assertThat(json).contains("\"name\":\"Test Entity\"");
    }

    @Test
    void serializeSuccessfullyWithoutId() {
        var json = PreSerializationUtils.serialize(mapper, of("status", "active", "type", "demo"), "status-info", null);

        assertThat(json).isNotNull();
        assertThat(json).contains("\"status\":\"active\"");
        assertThat(json).contains("\"type\":\"demo\"");
    }

    @Test
    void serializeSimpleString() {
        var json = PreSerializationUtils.serialize(mapper, "test string", "string", null);

        assertThat(json).isEqualTo("\"test string\"");
    }

    @Test
    void serializeNumber() {
        var json = PreSerializationUtils.serialize(mapper, 42, "number", 1);

        assertThat(json).isEqualTo("42");
    }

    @Test
    void serializeNull() throws Exception {
        var json = PreSerializationUtils.serialize(mapper, null, "null-value", null);

        assertThat(json).isEqualTo("null");
    }

    @Test
    void serializeHandlesExceptionFromMapper() {
        // Create a mapper that will fail
        McpJsonMapper failingMapper = new McpJsonMapper() {
            @Override
            public String writeValueAsString(Object value) {
                throw new RuntimeException("Serialization failed");
            }

            @Override
            public byte[] writeValueAsBytes(Object value) {
                return new byte[0];
            }

            @Override
            public <T> T readValue(byte[] src, Class<T> valueType) {
                return null;
            }

            @Override
            public <T> T readValue(String content, Class<T> valueType) {
                return null;
            }

            @Override
            public <T> T readValue(byte[] src, io.modelcontextprotocol.json.TypeRef<T> valueTypeRef) {
                return null;
            }

            @Override
            public <T> T readValue(String content, io.modelcontextprotocol.json.TypeRef<T> valueTypeRef) {
                return null;
            }

            @Override
            public <T> T convertValue(Object fromValue, Class<T> toValueType) {
                return null;
            }

            @Override
            public <T> T convertValue(Object fromValue, io.modelcontextprotocol.json.TypeRef<T> toValueTypeRef) {
                return null;
            }
        };

        assertThatThrownBy(() -> PreSerializationUtils.serialize(failingMapper, "test", "entity", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Serialization failed");
    }

    @Test
    void constructorThrowsException() {
        assertThatThrownBy(() -> {
            var constructor = PreSerializationUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .hasStackTraceContaining("Utility class cannot be instantiated");
    }
}
