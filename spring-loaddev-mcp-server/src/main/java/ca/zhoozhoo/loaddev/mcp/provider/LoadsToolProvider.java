package ca.zhoozhoo.loaddev.mcp.provider;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INTERNAL_ERROR;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_PARAMS;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_REQUEST;

import java.util.List;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.zhoozhoo.loaddev.mcp.config.McpToolRegistrationConfig.ReactiveContextHolder;
import ca.zhoozhoo.loaddev.mcp.dto.GroupDto;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDetails;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.dto.RifleDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import ca.zhoozhoo.loaddev.mcp.service.RiflesService;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Component
@Log4j2
public class LoadsToolProvider {

    @Autowired
    private LoadsService loadsService;

    @Autowired
    private RiflesService riflesService;

    /**
     * Retrieves all loads accessible to the current user.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param context Tool execution context
     * @return List of all accessible loads
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing or service discovery fails
     * @throws McpError with INVALID_REQUEST code if authentication fails
     */
    @Tool(description = "Retrieve all available loads in the system", name = "getLoads")
    public List<LoadDto> getLoads(ToolContext context) {
        log.debug("Retrieving all loads");

        Mono<List<LoadDto>> loadsMono = loadsService.getLoads()
            .contextWrite(ctx -> ctx.putAll(getReactiveContext()))
            .collectList()
            .doOnSuccess(list -> log.debug("Successfully retrieved {} loads", list.size()))
            .doOnError(e -> log.error("Error retrieving loads: {}", e.getMessage()));

        try {
            return loadsMono.block();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof McpError) {
                throw (McpError) cause;
            }
            throw handleException("Failed to retrieve loads", e);
        }
    }

     /**
     * Retrieves a specific load by its ID.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param id      ID of the load to retrieve
     * @return The requested load
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing or service discovery fails
     * @throws McpError with INVALID_REQUEST code if authentication fails
     * @throws McpError with INVALID_PARAMS code if load is not found or id is invalid
     */
     @McpTool(description = "Find a specific load by its unique identifier", name = "getLoad", annotations = @McpTool.McpAnnotations(title = "Get Load by ID", readOnlyHint = true, destructiveHint = false, idempotentHint = true))
     public LoadDto getLoadById(
            @McpToolParam(description = "Numeric ID of the load to retrieve", required = true) Long id) {
        log.debug("Retrieving load with ID: {}", id);

        if (id == null || id <= 0) {
            throw new McpError(new JSONRPCError(
                    INVALID_PARAMS,
                    "Load ID must be a positive number",
                    null));
        }

        Mono<LoadDto> loadMono = loadsService.getLoadById(id)
            .contextWrite(ctx -> ctx.putAll(getReactiveContext()))
            .doOnSuccess(load -> log.debug("Successfully retrieved load: {}", load))
            .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage()));

        try {
            return loadMono.block();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof McpError) {
                throw (McpError) cause;
            }
            throw handleException("Failed to retrieve load", e);
        }
    }

    /**
     * Retrieves detailed information for a specific load.
     * Includes load details, associated rifle information, and group statistics.
     * Ensures reactive context propagation and proper error handling.
     *
     * @param id      ID of the load to retrieve details for
     * @param context Tool execution context
     * @return LoadDetails containing load, rifle and group statistics
     * @throws McpError with INTERNAL_ERROR code if reactive context is missing or service discovery fails
     * @throws McpError with INVALID_REQUEST code if authentication fails
     * @throws McpError with INVALID_PARAMS code if load is not found or id is invalid
     */
    @Tool(description = "Get detailed information for a specific load", name = "getLoadDetails")
    public LoadDetails getLoadDetailsById(
        @ToolParam(description = "Numeric ID of the load", required = true) Long id,
        ToolContext context) {
            log.debug("Retrieving detailed information for load ID: {}", id);

            if (id == null || id <= 0) {
                throw new McpError(new JSONRPCError(
                        INVALID_PARAMS,
                        "Load ID must be a positive number",
                        null));
            }

            try {
                // Get the reactive context once and reuse it
                ContextView reactiveContext = getReactiveContext();

                // Get load details
                LoadDto load = loadsService.getLoadById(id)
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .doOnSuccess(l -> log.debug("Retrieved load: {}", l))
                    .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage()))
                    .block();

                // Get rifle details
                RifleDto rifle = riflesService.getRifleById(load.rifleId())
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .doOnSuccess(r -> log.debug("Retrieved rifle: {}", r))
                    .doOnError(e -> log.error("Error retrieving rifle for load {}: {}", id, e.getMessage()))
                    .block();

                // Get group statistics
                List<GroupDto> groups = loadsService.getGroupsByLoadId(id)
                    .contextWrite(ctx -> ctx.putAll(reactiveContext))
                    .collectList()
                    .doOnSuccess(stats -> log.debug("Retrieved {} statistics for load {}", stats.size(), id))
                    .doOnError(e -> log.error("Error retrieving statistics for load {}: {}", id, e.getMessage()))
                    .block();

                return new LoadDetails(load, rifle, groups);

            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause instanceof McpError) {
                    throw (McpError) cause;
                }
                throw handleException("Failed to retrieve load details", e);
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
