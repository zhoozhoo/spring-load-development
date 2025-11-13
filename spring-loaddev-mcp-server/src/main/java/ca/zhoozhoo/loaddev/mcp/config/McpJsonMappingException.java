package ca.zhoozhoo.loaddev.mcp.config;

/**
 * Custom unchecked exception representing failures during JSON mapping operations
 * performed by {@link SpringObjectMapperMcpJsonMapper}. Introduced to replace
 * generic {@link RuntimeException} usage and address SpotBugs warnings about
 * intentional throwing of raw runtime exceptions.
 */
public class McpJsonMappingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public McpJsonMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
