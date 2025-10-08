package ca.zhoozhoo.loaddev.mcp.provider;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_PARAMS;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.zhoozhoo.loaddev.mcp.dto.LoadDetails;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import ca.zhoozhoo.loaddev.mcp.service.RiflesService;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * MCP tool provider for load-related operations.
 * <p>
 * Exposes load management functionality through Model Context Protocol (MCP) tools.
 * All tools support reactive return types (Flux/Mono) and automatic authentication
 * context propagation via ReactiveSecurityContextHolder.
 * <p>
 * Tools are automatically discovered and registered by the MCP framework through
 * the {@code @McpTool} annotation. Authentication is handled transparently using
 * JWT tokens from the security context.
 * 
 * @author Zhubin Salehi
 * @see org.springaicommunity.mcp.annotation.McpTool
 * @see org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder
 */
@Component
@Log4j2
public class LoadsToolProvider {

    @Autowired
    private LoadsService loadsService;

    @Autowired
    private RiflesService riflesService;

    /**
     * Retrieves all loads accessible to the current user.
     * <p>
     * Streams loads as they become available for better performance with reactive execution.
     * Authentication is automatically propagated from the security context.
     *
     * @return Flux stream of LoadDto objects
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails
     */
    @McpTool(description = "Retrieve all available loads in the system", name = "getLoads")
    public Flux<LoadDto> getLoads() {
        log.debug("=== LoadsToolProvider.getLoads() called ===");
        log.debug("ToolCallReactiveContextHolder.getContext(): {}", ToolCallReactiveContextHolder.getContext());

        return withReactiveContext(
            loadsService.getLoads()
                .doOnNext(load -> log.debug("Streaming load: {}", load))
                .doOnComplete(() -> log.debug("Completed streaming all loads"))
                .doOnError(e -> log.error("Error retrieving loads: {}", e.getMessage(), e))
        );
    }

     /**
     * Retrieves a specific load by its unique identifier.
     * <p>
     * Returns detailed information about a single load including all its properties.
     * Authentication is automatically propagated from the security context.
     *
     * @param id the unique identifier of the load to retrieve (must be positive)
     * @return Mono emitting the requested LoadDto
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails
     * @throws McpError with INVALID_PARAMS if id is null, non-positive, or load not found
     */
     @McpTool(description = "Find a specific load by its unique identifier", name = "getLoad", annotations = @McpTool.McpAnnotations(title = "Get Load by ID", readOnlyHint = true, destructiveHint = false, idempotentHint = true))
     public Mono<LoadDto> getLoadById(
            @McpToolParam(description = "Numeric ID of the load to retrieve", required = true) Long id) {
        log.debug("=== LoadsToolProvider.getLoadById({}) called ===", id);
        log.debug("ToolCallReactiveContextHolder.getContext(): {}", ToolCallReactiveContextHolder.getContext());

        if (id == null || id <= 0) {
            log.error("Invalid load ID: {}", id);
            return Mono.error(new McpError(new JSONRPCError(
                    INVALID_PARAMS,
                    "Load ID must be a positive number",
                    null)));
        }

        return withReactiveContext(
            loadsService.getLoadById(id)
                .doOnSuccess(load -> log.debug("Successfully retrieved load: {}", load))
                .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage(), e))
        );
    }

    /**
     * Retrieves comprehensive details for a specific load.
     * <p>
     * Combines load information with its associated rifle data and group statistics
     * into a single comprehensive response. Makes parallel calls to retrieve all
     * related information efficiently.
     * <p>
     * Authentication is automatically propagated from the security context to all
     * downstream service calls.
     *
     * @param id the unique identifier of the load (must be positive)
     * @return Mono emitting LoadDetails containing load, rifle, and statistics data
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails
     * @throws McpError with INVALID_PARAMS if id is null, non-positive, or load/rifle not found
     */
    @McpTool(description = "Get detailed information for a specific load", name = "getLoadDetails")
    public Mono<LoadDetails> getLoadDetailsById(
        @McpToolParam(description = "Numeric ID of the load", required = true) Long id) {
            log.debug("Retrieving detailed information for load ID: {}", id);

            if (id == null || id <= 0) {
                return Mono.error(new McpError(new JSONRPCError(
                        INVALID_PARAMS,
                        "Load ID must be a positive number",
                        null)));
            }

            return withReactiveContext(
                loadsService.getLoadById(id)
                    .doOnSuccess(l -> log.debug("Retrieved load: {}", l))
                    .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage()))
            ).flatMap(load ->
                withReactiveContext(
                    riflesService.getRifleById(load.rifleId())
                        .doOnSuccess(r -> log.debug("Retrieved rifle: {}", r))
                        .doOnError(e -> log.error("Error retrieving rifle for load {}: {}", id, e.getMessage()))
                ).flatMap(rifle ->
                    withReactiveContext(
                        loadsService.getGroupsByLoadId(id)
                            .collectList()
                            .doOnSuccess(stats -> log.debug("Retrieved {} statistics for load {}", stats.size(), id))
                            .doOnError(e -> log.error("Error retrieving statistics for load {}: {}", id, e.getMessage()))
                    ).map(groups -> new LoadDetails(load, rifle, groups))
                )
            );
    }
    
    /**
     * Wraps a Mono with reactive context propagation.
     * <p>
     * Applies the security context and other contextual information from
     * {@link ToolCallReactiveContextHolder} to the reactive chain. This ensures
     * authentication tokens and other request context are available throughout
     * the reactive execution pipeline.
     * <p>
     * The MCP framework automatically populates the ToolCallReactiveContextHolder
     * with the necessary context before tool execution.
     *
     * @param <T> the type of elements emitted by the Mono
     * @param mono the Mono to wrap with context
     * @return a Mono with reactive context applied
     */
    private <T> Mono<T> withReactiveContext(Mono<T> mono) {
        log.debug("Adding reactive context to Mono");
        var reactiveContext = ToolCallReactiveContextHolder.getContext();
        log.debug("Reactive context size: {}", reactiveContext.size());
        reactiveContext.forEach((key, value) -> 
            log.debug("  Context key: {}, value type: {}", key, value != null ? value.getClass().getName() : "null")
        );
        return mono.contextWrite(ctx -> ctx.putAll(reactiveContext));
    }

    /**
     * Wraps a Flux with reactive context propagation.
     * <p>
     * Applies the security context and other contextual information from
     * {@link ToolCallReactiveContextHolder} to the reactive chain. This ensures
     * authentication tokens and other request context are available throughout
     * the reactive execution pipeline.
     * <p>
     * The MCP framework automatically populates the ToolCallReactiveContextHolder
     * with the necessary context before tool execution.
     *
     * @param <T> the type of elements emitted by the Flux
     * @param flux the Flux to wrap with context
     * @return a Flux with reactive context applied
     */
    private <T> Flux<T> withReactiveContext(Flux<T> flux) {
        log.debug("Adding reactive context to Flux");
        var reactiveContext = ToolCallReactiveContextHolder.getContext();
        log.debug("Reactive context size: {}", reactiveContext.size());
        reactiveContext.forEach((key, value) -> 
            log.debug("  Context key: {}, value type: {}", key, value != null ? value.getClass().getName() : "null")
        );
        return flux.contextWrite(ctx -> ctx.putAll(reactiveContext));
    }
}
