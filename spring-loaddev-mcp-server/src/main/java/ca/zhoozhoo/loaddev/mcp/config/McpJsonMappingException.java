package ca.zhoozhoo.loaddev.mcp.config;

/**
 * Custom unchecked exception for JSON mapping failures.
 * <p>
 * Note: No longer thrown after Jackson 3 migration. Retained for backward compatibility.
 * 
 * @deprecated Jackson 3 throws unchecked exceptions directly.
 * @author Zhubin Salehi
 */
public class McpJsonMappingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public McpJsonMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
