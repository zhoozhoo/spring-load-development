package ca.zhoozhoo.loaddev.mcp.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;

/**
 * McpJsonMapper implementation that delegates to the Spring-managed ObjectMapper.
 * <p>
 * Ensures custom Jackson modules (especially {@link ca.zhoozhoo.loaddev.common.jackson.QuantityModule})
 * are applied for MCP serialization/deserialization of DTOs containing JSR-385 Quantity types.
 * <p>
 * This adapter wraps the Spring Boot auto-configured ObjectMapper, which has all necessary
 * modules registered, and provides consistent error handling and logging for all JSON operations.
 *
 * @author Zhubin Salehi
 * @see io.modelcontextprotocol.json.McpJsonMapper
 * @see ca.zhoozhoo.loaddev.common.jackson.QuantityModule
 */
@SuppressFBWarnings(value = {
    "CRLF_INJECTION_LOGS",
    "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION"
}, justification = "All log parameters are sanitized; runtime exception wrapping is intentional for uniform API error handling.")
public class SpringObjectMapperMcpJsonMapper implements McpJsonMapper {

    private static final Logger logger = LoggerFactory.getLogger(SpringObjectMapperMcpJsonMapper.class);
    private static final int PREVIEW_MAX_LENGTH = 200;
    private static final int SAFE_TO_STRING_MAX_LENGTH = 500;

    private final ObjectMapper objectMapper;

    /**
     * Constructs a new SpringObjectMapperMcpJsonMapper.
     * <p>
     * Note: This stores the ObjectMapper directly rather than creating a defensive copy,
     * as ObjectMapper is effectively immutable once configured by Spring Boot auto-configuration.
     * The ObjectMapper's registered modules cannot be changed after registration, and Spring Boot
     * configures the ObjectMapper completely before any beans that depend on it are created.
     *
     * @param objectMapper the Spring-managed ObjectMapper with all modules registered
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
        justification = "ObjectMapper is effectively immutable after Spring Boot auto-configuration; defensive copy would break lazy initialization")
    public SpringObjectMapperMcpJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        logger.info("Initialized SpringObjectMapperMcpJsonMapper with ObjectMapper id={} modules={}",
                System.identityHashCode(this.objectMapper), sanitizeForLog(this.objectMapper.getRegisteredModuleIds().toString()));
    }

    @Override
    public byte[] writeValueAsBytes(Object value) {
        logger.debug("MCP mapper writeValueAsBytes invoked for {}", sanitizeForLog(getTypeName(value)));
        try {
            var bytes = objectMapper.writeValueAsBytes(value);
            if (logger.isTraceEnabled()) {
                var preview = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                if (preview.length() > PREVIEW_MAX_LENGTH) {
                    preview = preview.substring(0, PREVIEW_MAX_LENGTH) + "...";
                }
                logger.trace("MCP mapper writeValueAsBytes success for {} preview='{}'", sanitizeForLog(getTypeName(value)), sanitizeForLog(preview));
            }
            return bytes;
        } catch (JsonProcessingException e) {
            throw wrap("writeValueAsBytes", value, e);
        }
    }

    @Override
    public String writeValueAsString(Object value) {
        logger.debug("MCP mapper writeValueAsString invoked for {}", sanitizeForLog(getTypeName(value)));
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw wrap("writeValueAsString", value, e);
        }
    }

    @Override
    public <T> T readValue(byte[] src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            throw wrap("readValue(byte[],Class=" + valueType.getName() + ")", null, e);
        }
    }

    @Override
    public <T> T readValue(String src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            throw wrap("readValue(String,Class=" + valueType.getName() + ")", null, e);
        }
    }

    @Override
    public <T> T readValue(byte[] src, TypeRef<T> typeRef) {
        try {
            return objectMapper.readValue(src, objectMapper.constructType(typeRef.getType()));
        } catch (IOException e) {
            throw wrap("readValue(byte[],TypeRef=" + typeRef + ")", null, e);
        }
    }

    @Override
    public <T> T readValue(String src, TypeRef<T> typeRef) {
        try {
            return objectMapper.readValue(src, objectMapper.constructType(typeRef.getType()));
        } catch (IOException e) {
            throw wrap("readValue(String,TypeRef=" + typeRef + ")", null, e);
        }
    }

    @Override
    public <T> T convertValue(Object fromValue, Class<T> toValueType) {
        logger.debug("MCP mapper convertValue(Object, Class) from {} to {}",
            sanitizeForLog(getTypeName(fromValue)), sanitizeForLog(toValueType.getName()));
        try {
            return objectMapper.convertValue(fromValue, toValueType);
        } catch (IllegalArgumentException e) {
            throw wrap("convertValue(Object,Class=" + toValueType.getName() + ")", fromValue, e);
        }
    }

    @Override
    public <T> T convertValue(Object fromValue, TypeRef<T> toValueTypeRef) {
        logger.debug("MCP mapper convertValue(Object, TypeRef) from {} to {}",
            sanitizeForLog(getTypeName(fromValue)), sanitizeForLog(toValueTypeRef.toString()));
        try {
            return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueTypeRef.getType()));
        } catch (IllegalArgumentException e) {
            throw wrap("convertValue(Object,TypeRef=" + toValueTypeRef + ")", fromValue, e);
        }
    }

    /**
     * Gets the type name for logging purposes.
     *
     * @param value the object to get the type name for
     * @return "null" if value is null, otherwise the class name
     */
    private String getTypeName(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }

    /**
     * Wraps exceptions with consistent error logging.
     * <p>
     * Logs the error with operation details and creates a RuntimeException
     * with the operation context in the message.
     *
     * @param operation the name of the JSON operation that failed
     * @param value the value being processed (may be null)
     * @param e the exception that occurred
     * @return RuntimeException wrapping the original exception
     */
    private RuntimeException wrap(String operation, Object value, Exception e) {
        logger.error("MCP JSON {} FAILED for type={} value='{}': {}",
            sanitizeForLog(operation), sanitizeForLog(getTypeName(value)), sanitizeForLog(safeToString(value)), sanitizeForLog(e.getMessage()), e);
        return new McpJsonMappingException("JSON operation failed: " + operation, e);
    }

    /**
     * Safely converts an object to string for logging.
     * <p>
     * Limits the output length and catches any exceptions during toString()
     * to prevent additional errors during error handling.
     *
     * @param value the object to convert
     * @return safe string representation, truncated if too long
     */
    private String safeToString(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            var sanitized = sanitizeForLog(value.toString());
            return sanitized.length() > SAFE_TO_STRING_MAX_LENGTH 
                ? sanitized.substring(0, SAFE_TO_STRING_MAX_LENGTH) + "..." 
                : sanitized;
        } catch (RuntimeException ex) {
            return "<toString failed: " + sanitizeForLog(ex.getClass().getSimpleName()) + ">";
        }
    }

    private String sanitizeForLog(String input) {
        if (input == null) {
            return "null";
        }
        // Replace CR/LF to mitigate CRLF_INJECTION_LOGS warnings
        return input.replace('\r', ' ').replace('\n', ' ');
    }
}
