package ca.zhoozhoo.loaddev.mcp.provider;

import io.modelcontextprotocol.json.McpJsonMapper;
import lombok.extern.log4j.Log4j2;

/**
 * Small helper to centralize MCP pre-serialization of DTOs that contain JSR-385 Quantity fields.
 * Avoids repeating try/catch and logging logic across tool providers.
 */
@Log4j2
public final class PreSerializationUtils {

    private PreSerializationUtils() {}

    /**
     * Serialize the given object using the provided {@link McpJsonMapper}, logging success/failure with a
     * consistent pattern. Returns the JSON string or throws a RuntimeException on failure.
     *
     * @param mapper      mapper to use
     * @param value       object to serialize
     * @param entityLabel human-readable entity label for logs (e.g., "load", "rifle", "details")
     * @param id          optional identifier (may be null)
     * @return JSON string representation
     */
    public static String serialize(McpJsonMapper mapper, Object value, String entityLabel, Object id) {
        try {
            String json = mapper.writeValueAsString(value);
            if (id != null) {
                log.debug("MCP pre-serialization success for {} id={}: {}", entityLabel, id, json);
            } else {
                log.debug("MCP pre-serialization success for {}: {}", entityLabel, json);
            }
            return json;
        } catch (Exception ex) {
            if (id != null) {
                log.error("MCP pre-serialization FAILED for {} id={}: {}", entityLabel, id, ex.getMessage(), ex);
            } else {
                log.error("MCP pre-serialization FAILED for {}: {}", entityLabel, ex.getMessage(), ex);
            }
            throw ex instanceof RuntimeException re ? re : new RuntimeException(ex);
        }
    }
}
