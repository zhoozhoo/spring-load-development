package ca.zhoozhoo.loaddev.mcp.provider;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INTERNAL_ERROR;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_REQUEST;

import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.config.McpToolRegistrationConfig.ReactiveContextHolder;
import ca.zhoozhoo.loaddev.mcp.dto.RifleDto;
import ca.zhoozhoo.loaddev.mcp.service.RiflesService;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Service
@Log4j2
public class RiflesToolProvider {

    @Autowired
    private RiflesService riflesService;

    /**
     * Retrieves all rifles accessible to the current user.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param context Tool execution context
     * @return List of all accessible rifles
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing or
     *                  service discovery fails
     * @throws McpError with INVALID_REQUEST code if authentication fails
     */
    @Tool(description = "Retrieve all available rifles in the system", name = "getRifles")
    public List<RifleDto> getRifles(ToolContext context) {
        log.debug("Retrieving all rifles");

        Mono<List<RifleDto>> riflesMono = riflesService.getRifles()
                .contextWrite(ctx -> ctx.putAll(getReactiveContext()))
                .collectList()
                .doOnSuccess(list -> log.debug("Successfully retrieved {} rifles", list.size()))
                .doOnError(e -> log.error("Error retrieving rifles: {}", e.getMessage()));

        try {
            return riflesMono.block();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof McpError) {
                throw (McpError) cause;
            }
            throw handleException("Failed to retrieve rifles", e);
        }
    }

    /**
     * Retrieves a specific rifle by its ID.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param id      ID of the rifle to retrieve
     * @param context Tool execution context
     * @return The requested rifle
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing or service discovery fails
     * @throws McpError with INVALID_REQUEST code if authentication fails
     */
    @Tool(description = "Find a specific rifle by its unique identifier", name = "getRifleById")
    public RifleDto getRifleById(
            @org.springframework.ai.tool.annotation.ToolParam(description = "Numeric ID of the rifle to retrieve", required = true) Long id,
            ToolContext context) {
        log.debug("Retrieving rifle with ID: {}", id);

        Mono<RifleDto> rifleMono = riflesService.getRifleById(id)
                .contextWrite(ctx -> ctx.putAll(getReactiveContext()))
                .doOnSuccess(rifle -> log.debug("Successfully retrieved rifle: {}", rifle))
                .doOnError(e -> log.error("Error retrieving rifle {}: {}", id, e.getMessage()));

        try {
            return rifleMono.block();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof McpError) {
                throw (McpError) cause;
            }
            throw handleException("Failed to retrieve rifle", e);
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
            throw new McpError(new JSONRPCError(
                    INTERNAL_ERROR,
                    "No reactive context available",
                    null));
        }
        return reactiveContext;
    }

    /**
     * Handles exceptions by mapping them to appropriate McpError with JSON-RPC
     * error codes.
     * Special handling for authentication and state-related errors.
     *
     * @param message Base error message
     * @param e       Original exception
     * @return McpError with appropriate JSON-RPC error code
     */
    private McpError handleException(String message, Exception e) {
        if (e instanceof IllegalStateException) {
            return new McpError(new JSONRPCError(
                    INTERNAL_ERROR,
                    e.getMessage(),
                    null));
        }
        if (e.getCause() instanceof SecurityException) {
            return new McpError(new JSONRPCError(
                    INVALID_REQUEST,
                    "Authentication failed: " + e.getMessage(),
                    null));
        }
        log.error(message, e);
        return new McpError(new JSONRPCError(
                INTERNAL_ERROR,
                message + ": " + e.getMessage(),
                null));
    }
}
