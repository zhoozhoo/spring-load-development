package ca.zhoozhoo.loaddev.mcp.config;

import java.util.Arrays;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerProperties;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;

/**
 * Configuration class responsible for registering and initializing MCP tools.
 * Handles the dynamic registration of tool callbacks with the MCP server,
 * ensuring proper reactive context propagation and error handling during tool
 * execution.
 */
@Component
@Log4j2
public class McpToolRegistrationConfig {

    @Autowired
    private McpAsyncServer mcpAsyncServer;

    @Autowired
    private McpServerProperties serverProperties;

    @Autowired
    private ToolCallbackProvider callbackProvider;

    /**
     * Initializes tools when the application is ready.
     * For each tool callback:
     * 1. Removes any existing tool registration
     * 2. Creates a new AsyncToolSpecification with proper reactive context handling
     * 3. Registers the tool with the MCP server
     * 
     * The method ensures that the reactive context is properly propagated to tool
     * executions
     * and handles any errors that occur during tool initialization.
     */
    @SuppressWarnings("null")
    @EventListener(ApplicationReadyEvent.class)
    public void initializeTools() {
        Flux.fromIterable(Arrays.asList(callbackProvider.getToolCallbacks()))
                .flatMap(toolCallback -> {
                    var toolName = toolCallback.getToolDefinition().name();
                    var mimeType = serverProperties.getToolResponseMimeType()
                            .containsKey(toolName)
                                    ? MimeType.valueOf(serverProperties.getToolResponseMimeType().get(toolName))
                                    : null;

                    log.debug("Configuring tool: {}", toolName);

                    return mcpAsyncServer.removeTool(toolName)
                            .then(mcpAsyncServer.addTool(new AsyncToolSpecification(
                                    McpToolUtils.toSyncToolSpecification(toolCallback, mimeType).tool(),
                                    (exchange, map) -> Mono.deferContextual(c -> Mono.fromCallable(() -> {
                                        try {
                                            ReactiveContextHolder.reactiveContext.set(c);
                                            var syncExchange = new McpSyncServerExchange(exchange);
                                            return McpToolUtils.toSyncToolSpecification(toolCallback, mimeType)
                                                    .call()
                                                    .apply(syncExchange, map);
                                        } finally {
                                            ReactiveContextHolder.reactiveContext.remove();
                                        }
                                    })
                                            .subscribeOn(Schedulers.boundedElastic())))))
                            .doOnSuccess(v -> log.debug("Tool {} configured successfully", toolName))
                            .onErrorResume(e -> {
                                log.error("Failed to configure tool {}: {}", toolName, e.getMessage());
                                return Mono.empty();
                            });
                })
                .doOnComplete(() -> log.info("All tools initialized successfully"))
                .doOnError(e -> log.error("Error initializing tools: {}", e.getMessage()))
                .subscribe();
    }

    /**
     * Thread-local holder for reactive context.
     * Used to propagate reactive context to synchronous tool executions.
     */
    public static class ReactiveContextHolder {
        public static final ThreadLocal<ContextView> reactiveContext = new ThreadLocal<>();
    }
}