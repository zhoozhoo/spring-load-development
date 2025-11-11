package ca.zhoozhoo.loaddev.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * McpJsonMapper implementation that delegates to the Spring-managed ObjectMapper.
 * Ensures custom Jackson modules (e.g., QuantityModule) are applied for MCP
 * serialization/deserialization.
 */
public class SpringObjectMapperMcpJsonMapper implements McpJsonMapper {

    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(SpringObjectMapperMcpJsonMapper.class);

    public SpringObjectMapperMcpJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        logger.info("Initialized SpringObjectMapperMcpJsonMapper with ObjectMapper id={} modules={}", System.identityHashCode(objectMapper), objectMapper.getRegisteredModuleIds());
    }

    @Override
    public byte[] writeValueAsBytes(Object value) {
        String type = value == null ? "null" : value.getClass().getName();
        logger.debug("MCP mapper writeValueAsBytes invoked for {}", type);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(value);
            if (logger.isTraceEnabled()) {
                String preview = new String(bytes);
                if (preview.length() > 200) preview = preview.substring(0, 200) + "...";
                logger.trace("MCP mapper writeValueAsBytes success for {} preview='{}'", type, preview);
            }
            return bytes;
        } catch (Exception e) {
            throw wrap("writeValueAsBytes", value, e);
        }
    }

    @Override
    public String writeValueAsString(Object value) {
        logger.debug("MCP mapper writeValueAsString invoked for {}", value == null ? "null" : value.getClass().getName());
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw wrap("writeValueAsString", value, e);
        }
    }

    @Override
    public <T> T readValue(byte[] src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (Exception e) {
            throw wrap("readValue(byte[],Class=" + valueType.getName() + ")", null, e);
        }
    }

    @Override
    public <T> T readValue(String src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (Exception e) {
            throw wrap("readValue(String,Class=" + valueType.getName() + ")", null, e);
        }
    }

    @Override
    public <T> T readValue(byte[] src, TypeRef<T> typeRef) {
        try {
            return objectMapper.readValue(src, objectMapper.constructType(typeRef.getType()));
        } catch (Exception e) {
            throw wrap("readValue(byte[],TypeRef=" + typeRef + ")", null, e);
        }
    }

    @Override
    public <T> T readValue(String src, TypeRef<T> typeRef) {
        try {
            return objectMapper.readValue(src, objectMapper.constructType(typeRef.getType()));
        } catch (Exception e) {
            throw wrap("readValue(String,TypeRef=" + typeRef + ")", null, e);
        }
    }

    @Override
    public <T> T convertValue(Object fromValue, Class<T> toValueType) {
        logger.debug("MCP mapper convertValue(Object, Class) from {} to {}", fromValue == null ? "null" : fromValue.getClass().getName(), toValueType.getName());
        try {
            return objectMapper.convertValue(fromValue, toValueType);
        } catch (IllegalArgumentException e) {
            throw wrap("convertValue(Object,Class=" + toValueType.getName() + ")", fromValue, e);
        }
    }

    @Override
    public <T> T convertValue(Object fromValue, TypeRef<T> toValueTypeRef) {
        logger.debug("MCP mapper convertValue(Object, TypeRef) from {} to {}", fromValue == null ? "null" : fromValue.getClass().getName(), toValueTypeRef);
        try {
            return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueTypeRef.getType()));
        } catch (IllegalArgumentException e) {
            throw wrap("convertValue(Object,TypeRef=" + toValueTypeRef + ")", fromValue, e);
        }
    }

    /**
     * Unified error wrapping to eliminate repetitive logging blocks.
     */
    private RuntimeException wrap(String operation, Object value, Exception e) {
        logger.error("MCP JSON {} FAILED for type={} value='{}': {}", operation,
            value == null ? "null" : value.getClass().getName(), safeToString(value), e.getMessage(), e);
        return new RuntimeException("JSON operation failed: " + operation, e);
    }

    /** Safely obtain toString without risking additional exceptions or huge output. */
    private String safeToString(Object value) {
        if (value == null) return "null";
        try {
            String s = value.toString();
            return s.length() > 500 ? s.substring(0, 500) + "..." : s;
        } catch (Exception ex) {
            return "<toString failed: " + ex.getClass().getSimpleName() + ">";
        }
    }
}
