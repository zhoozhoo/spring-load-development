package ca.zhoozhoo.loaddev.mcp.provider;

import static io.modelcontextprotocol.spec.McpSchema.ErrorCodes.INVALID_PARAMS;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.model.tool.internal.ToolCallReactiveContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.zhoozhoo.loaddev.mcp.dto.RifleDto;
import ca.zhoozhoo.loaddev.mcp.service.RiflesService;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * MCP tool provider for rifle-related operations.
 * <p>
 * Mirrors {@link LoadsToolProvider}'s pre-serialization workaround to ensure
 * reliable MCP envelope serialization when {@link RifleDto} contains JSR-385
 * Quantity fields (e.g., barrelLength, freeBore). Direct reactive return of
 * typed DTOs was causing the framework to mark results as errors despite
 * successful service calls.
 * <p>
 * Strategy: Serialize each emitted {@link RifleDto} to a JSON string early and
 * return JSON string(s) to the MCP layer, avoiding Jackson re-serialization of
 * Quantity graphs inside the framework's generic envelope conversion.
 *
 * Tools:
 * <ul>
 *   <li>getRifles - Streams all rifles as a JSON array string</li>
 *   <li>getRifleById - Returns a single rifle as a JSON string</li>
 * </ul>
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
     * Emits rifles from the backend service, pre-serializing each to JSON, then
     * aggregates them into a single JSON array string. This minimizes the
     * framework's need to serialize complex Quantity fields.
     *
     * @return Mono emitting JSON array string of rifle objects.
     */
    @McpTool(description = "Retrieve all available rifles in the system", name = "getRifles")
    public Mono<String> getRifles() {
        log.debug("RiflesToolProvider.getRifles() invoked");
        log.debug("ToolCallReactiveContextHolder.getContext(): {}", ToolCallReactiveContextHolder.getContext());

        return ToolReactiveContext.applyTo(
            riflesService.getRifles()
                .map(rifle -> PreSerializationUtils.serialize(mcpJsonMapper, rifle, "rifle", rifle.id()))
                .collectList()
                .map(list -> {
                    String arrayJson = "[" + String.join(",", list) + "]";
                    log.debug("Returning rifles JSON array: {}", arrayJson);
                    return arrayJson;
                })
                .doOnError(e -> log.error("Error retrieving rifles: {}", e.getMessage(), e))
        );
    }

    /**
     * Retrieve a specific rifle by ID.
     * <p>
     * Pre-serializes the resulting {@link RifleDto} into JSON for reliable MCP
     * envelope transport when Quantity fields are present.
     *
     * @param id numeric rifle identifier (must be positive)
     * @return Mono emitting JSON string of the rifle
     */
    @McpTool(description = "Find a specific rifle by its unique identifier", name = "getRifleById")
    public Mono<String> getRifleById(
        @McpToolParam(description = "Numeric ID of the rifle to retrieve", required = true) Long id) {
        log.debug("RiflesToolProvider.getRifleById({}) invoked", id);
        log.debug("ToolCallReactiveContextHolder.getContext(): {}", ToolCallReactiveContextHolder.getContext());

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
                    return PreSerializationUtils.serialize(mcpJsonMapper, rifle, "rifle", id);
                })
                .doOnError(e -> log.error("Error retrieving rifle {}: {}", id, e.getMessage(), e))
        );
    }
}
