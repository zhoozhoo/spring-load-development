package ca.zhoozhoo.loaddev.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.TypeRef;

/**
 * Tests for SpringObjectMapperMcpJsonMapper to verify JSON operations.
 */
class SpringObjectMapperMcpJsonMapperTest {

    private SpringObjectMapperMcpJsonMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mapper = new SpringObjectMapperMcpJsonMapper(objectMapper);
    }

    @Test
    void constructorInitializesMapper() {
        assertThat(mapper).isNotNull();
    }

    @Test
    void writeValueAsBytesSerializesObject() throws Exception {
        var value = "test";
        var bytes = mapper.writeValueAsBytes(value);
        
        assertThat(bytes).isNotNull();
        assertThat(new String(bytes)).isEqualTo("\"test\"");
    }

    @Test
    void writeValueAsStringSerializesObject() {
        var value = "test";
        var json = mapper.writeValueAsString(value);
        
        assertThat(json).isEqualTo("\"test\"");
    }

    @Test
    void writeValueAsStringHandlesNull() {
        var json = mapper.writeValueAsString(null);
        assertThat(json).isEqualTo("null");
    }

    @Test
    void writeValueAsBytesHandlesComplexObject() {
        var bytes = mapper.writeValueAsBytes(Map.of("key", "value", "number", 42));
        
        assertThat(bytes).isNotNull();
        var json = new String(bytes);
        assertThat(json).contains("\"key\"");
        assertThat(json).contains("\"value\"");
        assertThat(json).contains("\"number\"");
        assertThat(json).contains("42");
    }

    @Test
    void readValueFromBytesDeserializesObject() {
        var bytes = "\"test\"".getBytes();
        var result = mapper.readValue(bytes, String.class);
        
        assertThat(result).isEqualTo("test");
    }

    @Test
    void readValueFromStringDeserializesObject() {
        var json = "\"test\"";
        var result = mapper.readValue(json, String.class);
        
        assertThat(result).isEqualTo("test");
    }

    @Test
    void readValueFromBytesWithTypeRef() {
        var bytes = "{\"key\":\"value\"}".getBytes();
        var typeRef = new TypeRef<Map<String, String>>() {};
        var result = mapper.readValue(bytes, typeRef);
        
        assertThat(result).containsEntry("key", "value");
    }

    @Test
    void readValueFromStringWithTypeRef() {
        var json = "{\"key\":\"value\"}";
        var typeRef = new TypeRef<Map<String, String>>() {};
        var result = mapper.readValue(json, typeRef);
        
        assertThat(result).containsEntry("key", "value");
    }

    @Test
    void convertValueToClass() {
        var source = Map.of("key", "value");
        @SuppressWarnings("unchecked")
        var result = (Map<String, String>) mapper.convertValue(source, Map.class);
        
        assertThat(result).containsEntry("key", "value");
    }

    @Test
    void convertValueWithTypeRef() {
        var source = Map.of("key", "value");
        var typeRef = new TypeRef<Map<String, String>>() {};
        var result = mapper.convertValue(source, typeRef);
        
        assertThat(result).containsEntry("key", "value");
    }

    @Test
    void writeValueAsBytesThrowsOnInvalidObject() {
        // Create an object that Jackson can't serialize
        var invalidObject = new Object() {
            @SuppressWarnings("unused")
            public String getValue() throws Exception {
                throw new Exception("Forced error");
            }
        };
        
        assertThatThrownBy(() -> mapper.writeValueAsBytes(invalidObject))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JSON operation failed");
    }

    @Test
    void readValueThrowsOnInvalidJson() {
        assertThatThrownBy(() -> mapper.readValue("invalid json", String.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JSON operation failed");
    }

    @Test
    void convertValueThrowsOnIncompatibleTypes() {
        assertThatThrownBy(() -> mapper.convertValue("not a map", Map.class))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JSON operation failed");
    }

    @Test
    void handlesLargeStrings() {
        // Test with a large string to verify preview truncation in logging
        var largeValue = "x".repeat(1000);
        var bytes = mapper.writeValueAsBytes(largeValue);
        
        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(1000);
    }

    @Test
    void handlesNullInSafeToString() {
        // writeValueAsString with null should not throw
        var json = mapper.writeValueAsString(null);
        assertThat(json).isEqualTo("null");
    }

    @Test
    void readValueFromBytesHandlesComplexTypes() {
        var typeRef = new TypeRef<Map<String, Object>>() {};
        var result = mapper.readValue("{\"name\":\"test\",\"values\":[1,2,3]}".getBytes(), typeRef);
        
        assertThat(result).containsKey("name");
        assertThat(result).containsKey("values");
    }
}
