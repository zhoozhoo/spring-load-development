package ca.zhoozhoo.loaddev.mcp.provider;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.zhoozhoo.loaddev.mcp.dto.RifleDto;
import ca.zhoozhoo.loaddev.mcp.service.RiflesService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * MCP tool provider for rifle-related operations.
 * <p>
 * Exposes rifle management functionality through Model Context Protocol (MCP) tools.
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
@Service
@Log4j2
public class RiflesToolProvider {

    @Autowired
    private RiflesService riflesService;

    /**
     * Retrieves all rifles accessible to the current user.
     * <p>
     * Streams rifles as they become available for better performance with reactive execution.
     * Authentication is automatically propagated from the security context.
     *
     * @return Flux stream of RifleDto objects
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails
     */
    @McpTool(description = "Retrieve all available rifles in the system", name = "getRifles")
    public Flux<RifleDto> getRifles() {
        log.debug("Retrieving all rifles");

        return withReactiveContext(
                riflesService.getRifles()
                    .doOnNext(rifle -> log.debug("Streaming rifle: {}", rifle))
                    .doOnComplete(() -> log.debug("Completed streaming all rifles"))
                    .doOnError(e -> log.error("Error retrieving rifles: {}", e.getMessage()))
        );
    }

    /**
     * Retrieves a specific rifle by its unique identifier.
     * <p>
     * Returns detailed information about a single rifle including all its properties.
     * Authentication is automatically propagated from the security context.
     *
     * @param id the unique identifier of the rifle to retrieve (must be positive)
     * @return Mono emitting the requested RifleDto
     * @throws McpError with INTERNAL_ERROR if service discovery fails
     * @throws McpError with INVALID_REQUEST if authentication fails
     * @throws McpError with INVALID_PARAMS if rifle not found
     */
        @McpTool(description = "Find a specific rifle by its unique identifier", name = "getRifleById")
    public Mono<RifleDto> getRifleById(
            @McpToolParam(description = "Numeric ID of the rifle to retrieve", required = true) Long id) {
        log.debug("Retrieving rifle with ID: {}", id);

        return withReactiveContext(
                riflesService.getRifleById(id)
                    .doOnSuccess(rifle -> log.debug("Successfully retrieved rifle: {}", rifle))
                    .doOnError(e -> log.error("Error retrieving rifle {}: {}", id, e.getMessage()))
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
        return mono.contextWrite(ctx -> ctx.putAll(ToolCallReactiveContextHolder.getContext()));
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
        return flux.contextWrite(ctx -> ctx.putAll(ToolCallReactiveContextHolder.getContext()));
    }
}
