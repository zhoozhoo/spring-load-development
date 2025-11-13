package ca.zhoozhoo.loaddev.mcp.provider;

import static ca.zhoozhoo.loaddev.mcp.provider.PreSerializationUtils.serialize;
import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_PARAMS;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import ca.zhoozhoo.loaddev.mcp.service.RiflesService;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

/**
 * MCP tool provider for rifle-related operations.
 * <p>
 * Provides tools for searching and retrieving rifle information.
 * All operations use reactive programming for efficient execution.
 * </p>
 */
@Component
@Log4j2
public class RiflesToolProvider {

    @Autowired
    private RiflesService riflesService;
    @Autowired
    private McpJsonMapper mcpJsonMapper;

    /**
     * Retrieve all rifles accessible to the current user.
     * <p>
     * Returns a JSON array string containing serialized RifleDto objects with
     * properly formatted JSR-385 Quantity fields. Uses PreSerializationUtils
     * to ensure consistent serialization with QuantityModule.
     *
     * @return Mono emitting JSON array string of rifles
     */
    @McpTool(description = "Retrieve all available rifles in the system", name = "getRifles")
    public Mono<String> getRifles() {
        log.debug("RiflesToolProvider.getRifles() invoked");

        return ToolReactiveContext.applyTo(
            riflesService.getRifles()
                .map(rifle -> serialize(mcpJsonMapper, rifle, "rifle", rifle.id()))
                .collectList()
                .map(list -> {
                    log.debug("Returning JSON array for rifles: {}", list);
                    return "[" + String.join(",", list) + "]";
                })
                .doOnError(e -> log.error("Error retrieving rifles: {}", e.getMessage(), e))
        );
    }

    /**
     * Retrieve a specific rifle by ID.
     * <p>
     * Returns a JSON string containing the serialized RifleDto with properly
     * formatted JSR-385 Quantity fields. Uses PreSerializationUtils to ensure
     * consistent serialization with QuantityModule.
     *
     * @param id numeric rifle identifier (must be positive)
     * @return Mono emitting JSON string of rifle
     */
    @McpTool(description = "Find a specific rifle by its unique identifier", name = "getRifleById")
    public Mono<String> getRifleById(
        @McpToolParam(description = "Numeric ID of the rifle to retrieve", required = true) Long id) {
        log.debug("RiflesToolProvider.getRifleById({}) invoked", id);

        if (id == null || id <= 0) {
            log.error("Invalid rifle ID: {}", id);
            return Mono.error(new McpError(new JSONRPCError(
                INVALID_PARAMS,
                "Rifle ID must be a positive number",
                null)));
        }

        return ToolReactiveContext.applyTo(
            riflesService.getRifleById(id)
                .map(rifle -> {
                    log.debug("Successfully retrieved rifle: {}", rifle);
                    return serialize(mcpJsonMapper, rifle, "rifle", id);
                })
                .doOnError(e -> log.error("Error retrieving rifle {}: {}", id, e.getMessage(), e))
        );
    }
}
