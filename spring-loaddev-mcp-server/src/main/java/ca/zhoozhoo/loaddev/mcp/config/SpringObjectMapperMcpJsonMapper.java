package ca.zhoozhoo.loaddev.mcp.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import tools.jackson.databind.json.JsonMapper;

/**
 * MCP JSON mapper using Spring-managed JsonMapper (Jackson 3).
 * <p>
 * Ensures {@link ca.zhoozhoo.loaddev.common.jackson.QuantityModule} is applied for
 * serialization/deserialization of JSR-385 Quantity types.
 *
 * @author Zhubin Salehi
 * @see io.modelcontextprotocol.json.McpJsonMapper
 */
public class SpringObjectMapperMcpJsonMapper implements McpJsonMapper {

    private static final Logger logger = LoggerFactory.getLogger(SpringObjectMapperMcpJsonMapper.class);
    private static final int PREVIEW_MAX_LENGTH = 200;

    private final JsonMapper jsonMapper;

    /**
     * Constructs mapper with Spring-managed JsonMapper.
     * <p>
     * Note: JsonMapper is stored directly (not copied) as it's effectively immutable
     * after Spring Boot auto-configuration.
     *
     * @param jsonMapper the Spring-managed JsonMapper with all modules
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
        justification = "JsonMapper is effectively immutable after Spring Boot auto-configuration; defensive copy would break lazy initialization")
    public SpringObjectMapperMcpJsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        logger.info("Initialized SpringObjectMapperMcpJsonMapper with ObjectMapper id={}",
                System.identityHashCode(this.jsonMapper));
    }

    @Override
    public byte[] writeValueAsBytes(Object value) {
        logger.debug("MCP mapper writeValueAsBytes invoked for {}", sanitizeForLog(getTypeName(value)));
        var bytes = jsonMapper.writeValueAsBytes(value);
        if (logger.isTraceEnabled()) {
            var preview = new String(bytes, UTF_8);
            if (preview.length() > PREVIEW_MAX_LENGTH) {
                preview = preview.substring(0, PREVIEW_MAX_LENGTH) + "...";
            }
            logger.trace("MCP mapper writeValueAsBytes success for {} preview='{}'", sanitizeForLog(getTypeName(value)), sanitizeForLog(preview));
        }
        return bytes;
    }

    @Override
    public String writeValueAsString(Object value) {
        logger.debug("MCP mapper writeValueAsString invoked for {}", sanitizeForLog(getTypeName(value)));
        return jsonMapper.writeValueAsString(value);
    }

    @Override
    public <T> T readValue(byte[] src, Class<T> valueType) {
        return jsonMapper.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(String src, Class<T> valueType) {
        return jsonMapper.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(byte[] src, TypeRef<T> typeRef) {
        return jsonMapper.readValue(src, jsonMapper.constructType(typeRef.getType()));
    }

    @Override
    public <T> T readValue(String src, TypeRef<T> typeRef) {
        return jsonMapper.readValue(src, jsonMapper.constructType(typeRef.getType()));
    }

    @Override
    public <T> T convertValue(Object fromValue, Class<T> toValueType) {
        logger.debug("MCP mapper convertValue(Object, Class) from {} to {}",
            sanitizeForLog(getTypeName(fromValue)), sanitizeForLog(toValueType.getName()));
        return jsonMapper.convertValue(fromValue, toValueType);
    }

    @Override
    public <T> T convertValue(Object fromValue, TypeRef<T> toValueTypeRef) {
        logger.debug("MCP mapper convertValue(Object, TypeRef) from {} to {}",
            sanitizeForLog(getTypeName(fromValue)), sanitizeForLog(toValueTypeRef.toString()));
        return jsonMapper.convertValue(fromValue, jsonMapper.constructType(toValueTypeRef.getType()));
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

    private String sanitizeForLog(String input) {
        if (input == null) {
            return "null";
        }
        // Replace CR/LF to mitigate CRLF_INJECTION_LOGS warnings
        return input.replace('\r', ' ').replace('\n', ' ');
    }
}
