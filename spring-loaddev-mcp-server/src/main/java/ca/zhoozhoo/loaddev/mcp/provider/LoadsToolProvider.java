package ca.zhoozhoo.loaddev.mcp.provider;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_PARAMS;

import java.util.List;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.zhoozhoo.loaddev.mcp.dto.GroupDto;
import ca.zhoozhoo.loaddev.mcp.dto.LoadDetails;
import ca.zhoozhoo.loaddev.mcp.dto.RifleDto;
import ca.zhoozhoo.loaddev.mcp.service.LoadsService;
import ca.zhoozhoo.loaddev.mcp.service.RiflesService;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.log4j.Log4j2;
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

    @Autowired
    private McpJsonMapper mcpJsonMapper;

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
    public Mono<String> getLoads() {
        log.debug("=== LoadsToolProvider.getLoads() called ===");
        log.debug("ToolCallReactiveContextHolder.getContext(): {}", ToolCallReactiveContextHolder.getContext());

        // Serialize each LoadDto to JSON then aggregate to a JSON array string to minimize framework-side serialization
        return ToolReactiveContext.applyTo(
            loadsService.getLoads()
                .map(load -> PreSerializationUtils.serialize(mcpJsonMapper, load, "load", load.id()))
                .collectList()
                .map(list -> {
                    String joined = String.join(",", list);
                    String arrayJson = "[" + joined + "]";
                    log.debug("Returning JSON array for loads: {}", arrayJson);
                    return arrayJson;
                })
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
    public Mono<String> getLoadById(
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

        return ToolReactiveContext.applyTo(
            loadsService.getLoadById(id)
                .map(load -> {
                    log.debug("Successfully retrieved load: {}", load);
                    return PreSerializationUtils.serialize(mcpJsonMapper, load, "load", id);
                })
                .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage(), e))
        );
    }

    /**
     * Retrieves comprehensive details for a specific load.
     * <p>
     * Uses optimized parallel fetching with Mono.zip() to retrieve load, rifle, and statistics
     * concurrently. This is inspired by Structured Concurrency (JEP 480) principles,
     * ensuring all subtasks complete successfully or fail together with proper error handling.
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
    public Mono<String> getLoadDetailsById(
        @McpToolParam(description = "Numeric ID of the load", required = true) Long id) {
            log.debug("Retrieving detailed information for load ID: {}", id);

            if (id == null || id <= 0) {
                return Mono.error(new McpError(new JSONRPCError(
                        INVALID_PARAMS,
                        "Load ID must be a positive number",
                        null)));
            }

            // First fetch the load to get the rifle ID
            return ToolReactiveContext.applyTo(
                loadsService.getLoadById(id)
                    .doOnSuccess(l -> log.debug("Retrieved load: {}", l))
                    .doOnError(e -> log.error("Error retrieving load {}: {}", id, e.getMessage()))
            ).flatMap(load -> {
                // Now fetch rifle and statistics in parallel (Structured Concurrency pattern)
                // Both operations must complete successfully, or the whole operation fails
                Mono<RifleDto> rifleMono = ToolReactiveContext.applyTo(
                    riflesService.getRifleById(load.rifleId())
                        .doOnSuccess(r -> log.debug("Retrieved rifle: {}", r))
                        .doOnError(e -> log.error("Error retrieving rifle for load {}: {}", id, e.getMessage()))
                );
                
                Mono<List<GroupDto>> groupsMono = ToolReactiveContext.applyTo(
                    loadsService.getGroupsByLoadId(id)
                        .collectList()
                        .doOnSuccess(stats -> log.debug("Retrieved {} statistics for load {}", stats.size(), id))
                        .doOnError(e -> log.error("Error retrieving statistics for load {}: {}", id, e.getMessage()))
                );
                
                // Parallel execution with structured error handling
                return Mono.zip(rifleMono, groupsMono)
                    .map(tuple -> new LoadDetails(load, tuple.getT1(), tuple.getT2()))
                    .map(details -> {
                        log.debug("Successfully assembled LoadDetails for load {}", id);
                        return PreSerializationUtils.serialize(mcpJsonMapper, details, "loadDetails", id);
                    });
            });
    }
}
