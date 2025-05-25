/**
 * Tool implementation for load-related operations.
 * Provides AI-enabled tools for retrieving load information while maintaining
 * reactive context and security propagation.
 */
package ca.zhoozhoo.loaddev.mcp.tools;

import static reactor.core.publisher.Mono.just;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.config.McpToolRegistrationConfig.ReactiveContextHolder;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
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
     * @throws IllegalStateException if reactive context is missing or authentication fails
     * @throws IllegalArgumentException if load is not found
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
                throw new IllegalStateException("Load not found: " + e.getCause().getMessage());
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
     * @throws IllegalStateException if reactive context is missing or authentication fails
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
                    .doOnError(e -> log.error("Error retrieving loads: {}", e.getMessage()))
                    .block();
        } catch (Exception e) {
            throw handleException("Failed to retrieve loads", e);
        }
    }

    /**
     * Retrieves the reactive context from thread-local storage.
     *
     * @return The current reactive context
     * @throws IllegalStateException if no reactive context is available
     */
    private ContextView getReactiveContext() {
        var reactiveContext = ReactiveContextHolder.reactiveContext.get();
        if (reactiveContext == null) {
            throw new IllegalStateException("No reactive context available");
        }
        return reactiveContext;
    }

    /**
     * Handles exceptions by mapping them to appropriate runtime exceptions.
     * Special handling for authentication and state-related errors.
     *
     * @param message Base error message
     * @param e Original exception
     * @return Appropriate runtime exception
     */
    private RuntimeException handleException(String message, Exception e) {
        if (e instanceof IllegalStateException) {
            return (IllegalStateException) e;
        }
        if (e.getCause() instanceof SecurityException) {
            return new IllegalStateException("Authentication failed: " + e.getMessage(), e);
        }
        log.error(message, e);
        return new IllegalStateException(message + ": " + e.getMessage(), e);
    }
}
