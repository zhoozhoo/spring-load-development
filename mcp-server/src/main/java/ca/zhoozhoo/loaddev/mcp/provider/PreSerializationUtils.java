package ca.zhoozhoo.loaddev.mcp.provider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.modelcontextprotocol.json.McpJsonMapper;
import lombok.extern.log4j.Log4j2;

/**
 * Utility class for pre-serializing DTOs with JSR-385 Quantity fields to JSON.
 * <p>
 * Centralizes the pattern of converting DTOs to JSON strings before returning them
 * from MCP tool methods. This workaround addresses an issue where the MCP framework's
 * generic envelope serialization fails to properly handle {@code Quantity<?>} types,
 * even when the ObjectMapper has the QuantityModule registered.
 * <p>
 * By pre-serializing to JSON strings, we bypass the framework's problematic
 * re-serialization and ensure Quantity fields are correctly represented.
 *
 * @author Zhubin Salehi
 * @see ca.zhoozhoo.loaddev.common.jackson.QuantityModule
 */
@Log4j2
@SuppressFBWarnings(value = {
    "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION"
}, justification = "Runtime exceptions used for uniform failure propagation; suppression narrows scope to actual need.")
public final class PreSerializationUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private PreSerializationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

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
            var json = mapper.writeValueAsString(value);
            if (id != null) {
                log.debug("MCP pre-serialization success for {} id={}: {}", 
                    sanitize(entityLabel), sanitize(String.valueOf(id)), sanitize(json));
            } else {
                log.debug("MCP pre-serialization success for {}: {}", sanitize(entityLabel), sanitize(json));
            }
            return json;
        } catch (java.io.IOException ex) {
            if (id != null) {
                log.error("MCP pre-serialization FAILED (IO) for {} id={}: {}", 
                    sanitize(entityLabel), sanitize(String.valueOf(id)), sanitize(ex.getMessage()), ex);
            } else {
                log.error("MCP pre-serialization FAILED (IO) for {}: {}", sanitize(entityLabel), sanitize(ex.getMessage()), ex);
            }
            throw new RuntimeException("Pre-serialization IO failure", ex);
        } catch (RuntimeException ex) {
            if (id != null) {
                log.error("MCP pre-serialization FAILED for {} id={}: {}", 
                    sanitize(entityLabel), sanitize(String.valueOf(id)), sanitize(ex.getMessage()), ex);
            } else {
                log.error("MCP pre-serialization FAILED for {}: {}", sanitize(entityLabel), sanitize(ex.getMessage()), ex);
            }
            throw ex; // propagate unchanged
        }
    }

    private static String sanitize(String input) {
        if (input == null) {
            return "null";
        }
        return input.replace('\r', ' ').replace('\n', ' ');
    }
}
