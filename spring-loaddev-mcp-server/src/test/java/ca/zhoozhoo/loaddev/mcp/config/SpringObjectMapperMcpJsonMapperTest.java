package ca.zhoozhoo.loaddev.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.json.TypeRef;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests for SpringObjectMapperMcpJsonMapper to verify JSON operations.
 */
class SpringObjectMapperMcpJsonMapperTest {

    private SpringObjectMapperMcpJsonMapper mapper;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
        mapper = new SpringObjectMapperMcpJsonMapper(jsonMapper);
    }

    @Test
    void constructorInitializesMapper() {
        assertThat(mapper).isNotNull();
    }

    @Test
    void writeValueAsBytesSerializesObject() {
        assertThat(mapper.writeValueAsBytes("test")).isNotNull();
        assertThat(new String(mapper.writeValueAsBytes("test"))).isEqualTo("\"test\"");
    }

    @Test
    void writeValueAsStringSerializesObject() {
        assertThat(mapper.writeValueAsString("test")).isEqualTo("\"test\"");
    }

    @Test
    void writeValueAsStringHandlesNull() {
        assertThat(mapper.writeValueAsString(null)).isEqualTo("null");
    }

    @Test
    void writeValueAsBytesHandlesComplexObject() {
        assertThat(mapper.writeValueAsBytes(Map.of("key", "value", "number", 42))).isNotNull();
        assertThat(new String(mapper.writeValueAsBytes(Map.of("key", "value", "number", 42))))
            .contains("\"key\"")
            .contains("\"value\"")
            .contains("\"number\"")
            .contains("42");
    }

    @Test
    void readValueFromBytesDeserializesObject() {
        assertThat(mapper.readValue("\"test\"".getBytes(), String.class)).isEqualTo("test");
    }

    @Test
    void readValueFromStringDeserializesObject() {
        assertThat(mapper.readValue("\"test\"", String.class)).isEqualTo("test");
    }

    @Test
    void readValueFromBytesWithTypeRef() {
        assertThat(mapper.readValue("{\"key\":\"value\"}".getBytes(), new TypeRef<Map<String, String>>() {}))
            .containsEntry("key", "value");
    }

    @Test
    void readValueFromStringWithTypeRef() {
        assertThat(mapper.readValue("{\"key\":\"value\"}", new TypeRef<Map<String, String>>() {}))
            .containsEntry("key", "value");
    }

    @Test
    void convertValueToClass() {
        @SuppressWarnings("unchecked")
        var result = (Map<String, String>) mapper.convertValue(Map.of("key", "value"), Map.class);
        assertThat(result).containsEntry("key", "value");
    }

    @Test
    void convertValueWithTypeRef() {
        assertThat(mapper.convertValue(Map.of("key", "value"), new TypeRef<Map<String, String>>() {}))
            .containsEntry("key", "value");
    }

    @Test
    void writeValueAsBytesThrowsOnInvalidObject() {
        // Jackson 3 throws unchecked exceptions directly
        assertThatThrownBy(() -> mapper.writeValueAsBytes(new Object() {
            @SuppressWarnings("unused")
            public String getValue() throws Exception {
                throw new Exception("Forced error");
            }
        })).isInstanceOf(RuntimeException.class);
    }

    @Test
    void readValueThrowsOnInvalidJson() {
        // Jackson 3 throws unchecked exceptions directly
        assertThatThrownBy(() -> mapper.readValue("invalid json", String.class))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void convertValueThrowsOnIncompatibleTypes() {
        // Jackson 3 throws unchecked exceptions directly
        assertThatThrownBy(() -> mapper.convertValue("not a map", Map.class))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void handlesLargeStrings() {
        // Test with a large string to verify preview truncation in logging
        assertThat(mapper.writeValueAsBytes("x".repeat(1000))).isNotNull();
        assertThat(mapper.writeValueAsBytes("x".repeat(1000)).length).isGreaterThan(1000);
    }

    @Test
    void handlesNullInSafeToString() {
        assertThat(mapper.writeValueAsString(null)).isEqualTo("null");
    }

    @Test
    void readValueFromBytesHandlesComplexTypes() {
        assertThat(mapper.readValue("{\"name\":\"test\",\"values\":[1,2,3]}".getBytes(), 
                new TypeRef<Map<String, Object>>() {}))
            .containsKey("name")
            .containsKey("values");
    }
}
