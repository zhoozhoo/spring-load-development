/**
 * Tool implementation for load-related operations.
 * Provides AI-enabled tools for retrieving load information while maintaining
 * reactive context and security propagation.
 * 
 * <p>All tools in this class follow a consistent error handling pattern:
 * <ul>
 *   <li>Errors are wrapped in {@link McpError} with appropriate JSON-RPC error codes</li>
 *   <li>Missing reactive context results in INTERNAL_ERROR code</li>
 *   <li>Authentication failures result in INVALID_REQUEST code</li>
 *   <li>Invalid load IDs result in INVALID_PARAMS code</li>
 *   <li>Other errors result in INTERNAL_ERROR code</li>
 * </ul>
 */
package ca.zhoozhoo.loaddev.mcp.tools;

import static reactor.core.publisher.Mono.just;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.config.McpToolRegistrationConfig.ReactiveContextHolder;
import ca.zhoozhoo.loaddev.mcp.dto.GroupStatisticsDto;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.util.context.ContextView;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoadsTools {
    private final LoadsService loadsService;

    /**
     * Retrieves a specific load by its ID.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param id ID of the load to retrieve
     * @param context Tool execution context
     * @return The requested load
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing
     * @throws McpError with INVALID_REQUEST code if authentication fails
     * @throws McpError with INVALID_PARAMS code if load is not found
     */
    @Tool(description = "Find a specific load by its unique identifier", name = "getLoadById")
    public LoadDto getLoadById(
            @ToolParam(description = "Numeric ID of the load to retrieve") Long id,
            ToolContext context) {
        log.debug("Retrieving load with ID: {}", id);
        ContextView reactiveContext = getReactiveContext();

        try {
            return just(id)
                    .flatMap(loadId -> loadsService.getLoadById(loadId))
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .doOnSuccess(load -> log.debug("Successfully retrieved load: {}", load))
                    .doOnError(IllegalArgumentException.class, 
                        e -> log.debug("Load not found with ID {}: {}", id, e.getMessage()))
                    .doOnError(SecurityException.class, 
                        e -> log.error("Authentication error retrieving load {}: {}", id, e.getMessage()))
                    .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage()))
                    .block();
        } catch (Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw new McpError(new McpSchema.JSONRPCResponse.JSONRPCError(
                    McpSchema.ErrorCodes.INVALID_PARAMS, 
                    "Load not found: " + e.getCause().getMessage(), 
                    null));
            }
            throw handleException("Failed to retrieve load", e);
        }
    }

    /**
     * Retrieves all loads accessible to the current user.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param context Tool execution context
     * @return List of all accessible loads
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing
     * @throws McpError with INVALID_REQUEST code if authentication fails
     */
    @Tool(description = "Retrieve all available loads in the system", name = "getLoads")
    public List<LoadDto> getLoads(ToolContext context) {
        log.debug("Retrieving all loads");
        ContextView reactiveContext = getReactiveContext();

        try {
            return loadsService.getLoads()
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .collectList()
                    .doOnSuccess(list -> log.debug("Successfully retrieved {} loads", list.size()))
                    .doOnError(SecurityException.class, 
                        e -> log.error("Authentication error retrieving loads: {}", e.getMessage()))
                    .doOnError(e -> log.error("Error retrieving loads: {}", e.getMessage()))
                    .block();
        } catch (Exception e) {
            if (e.getCause() instanceof SecurityException) {
                throw new McpError(new McpSchema.JSONRPCResponse.JSONRPCError(
                    McpSchema.ErrorCodes.INVALID_REQUEST,
                    "Authentication failed while retrieving loads: " + e.getMessage(),
                    null));
            }
            throw handleException("Failed to retrieve loads", e);
        }
    }

    /**
     * Retrieves statistics for a specific load.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param id ID of the load to retrieve statistics for
     * @param context Tool execution context
     * @return List of group statistics for the load
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing
     * @throws McpError with INVALID_REQUEST code if authentication fails
     * @throws McpError with INVALID_PARAMS code if load is not found
     */
    @Tool(description = "Get statistics for a specific load", name = "getLoadStatistics")
    public List<GroupStatisticsDto> getLoadStatistics(
            @ToolParam(description = "Numeric ID of the load") Long id,
            ToolContext context) {
        log.debug("Retrieving statistics for load ID: {}", id);
        ContextView reactiveContext = getReactiveContext();

        try {
            return just(id)
                    .flatMapMany(loadId -> loadsService.getLoadStatistics(loadId))
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .collectList()
                    .doOnSuccess(stats -> log.debug("Retrieved {} statistics for load {}", stats.size(), id))
                    .doOnError(IllegalArgumentException.class, 
                        e -> log.debug("Load not found with ID {}: {}", id, e.getMessage()))
                    .doOnError(SecurityException.class, 
                        e -> log.error("Authentication error retrieving statistics for load {}: {}", id, e.getMessage()))
                    .block();
        } catch (Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw new McpError(new McpSchema.JSONRPCResponse.JSONRPCError(
                    McpSchema.ErrorCodes.INVALID_PARAMS, 
                    "Load not found: " + e.getCause().getMessage(), 
                    null));
            }
            throw handleException("Failed to retrieve load statistics", e);
        }
    }

    /**
     * Retrieves the reactive context from thread-local storage.
     *
     * @return The current reactive context
     * @throws McpError with INTERNAL_ERROR code if no reactive context is available
     */
    private ContextView getReactiveContext() {
        var reactiveContext = ReactiveContextHolder.reactiveContext.get();
        if (reactiveContext == null) {
            throw new McpError(new McpSchema.JSONRPCResponse.JSONRPCError(
                McpSchema.ErrorCodes.INTERNAL_ERROR, 
                "No reactive context available", 
                null));
        }

        return reactiveContext;
    }

    /**
     * Handles exceptions by mapping them to appropriate McpError with JSON-RPC error codes.
     * Special handling for authentication and state-related errors.
     *
     * @param message Base error message
     * @param e Original exception
     * @return McpError with appropriate JSON-RPC error code
     */
    private McpError handleException(String message, Exception e) {
        if (e instanceof IllegalStateException) {
            return new McpError(new McpSchema.JSONRPCResponse.JSONRPCError(
                McpSchema.ErrorCodes.INTERNAL_ERROR,
                e.getMessage(),
                null));
        }
        if (e.getCause() instanceof SecurityException) {
            return new McpError(new McpSchema.JSONRPCResponse.JSONRPCError(
                McpSchema.ErrorCodes.INVALID_REQUEST,
                "Authentication failed: " + e.getMessage(),
                null));
        }
        log.error(message, e);
        return new McpError(new McpSchema.JSONRPCResponse.JSONRPCError(
            McpSchema.ErrorCodes.INTERNAL_ERROR,
            message + ": " + e.getMessage(),
            null));
    }
}
